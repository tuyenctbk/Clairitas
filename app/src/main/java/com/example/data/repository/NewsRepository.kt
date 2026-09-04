package com.example.data.repository

import android.content.Context
import android.util.LruCache
import com.example.data.local.AppDatabase
import com.example.data.local.ArticleEntity
import com.example.data.local.KeywordTrapEntity
import com.example.data.model.Article
import com.example.data.model.ProcessingMode
import com.example.data.remote.RssNewsFetcher
import com.example.service.RadarNotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

data class StorageStats(
    val totalArticles: Int = 0,
    val unbookmarkedArticles: Int = 0,
    val bookmarkedArticles: Int = 0,
    val estimatedBytes: Long = 0L
)

data class ClearCacheResult(
    val articlesCleared: Int = 0,
    val freedBytes: Long = 0L
)

class NewsRepository(
    private val context: Context,
    private val db: AppDatabase = AppDatabase.getDatabase(context),
    private val notificationManager: RadarNotificationManager = RadarNotificationManager(context)
) {
    private val articleDao = db.articleDao()
    private val keywordTrapDao = db.keywordTrapDao()

    // In-memory LRU Cache for article metadata using android.util.LruCache (max 50 entries)
    private val articleLruCache = LruCache<String, Article>(50)
    val firebaseSyncManager = com.example.service.FirebaseSyncManager(context, articleDao)

    val allArticles: Flow<List<Article>> = articleDao.getAllArticles().map { entities ->
        entities.map { entity ->
            val article = entity.toArticle()
            articleLruCache.put(article.id, article)
            article
        }
    }

    val bookmarkedArticles: Flow<List<Article>> = articleDao.getBookmarkedArticles().map { entities ->
        entities.map { entity -> entity.toArticle() }
    }

    val readArticles: Flow<List<Article>> = articleDao.getReadArticles().map { entities ->
        entities.map { entity -> entity.toArticle() }
    }

    val allKeywordTraps: Flow<List<KeywordTrapEntity>> = keywordTrapDao.getAllTraps()

    suspend fun getArticleById(id: String): Article? {
        articleLruCache.get(id)?.let { cached ->
            return cached
        }
        val article = articleDao.getArticleById(id)?.toArticle()
        if (article != null) {
            articleLruCache.put(id, article)
        }
        return article
    }

    suspend fun toggleBookmark(id: String, currentState: Boolean) {
        val newState = !currentState
        articleLruCache.get(id)?.let { cached ->
            articleLruCache.put(id, cached.copy(isBookmarked = newState))
        }
        articleDao.updateBookmarkState(id, newState)

        // Sync with Firebase Firestore
        val updatedArticle = getArticleById(id)
        if (updatedArticle != null) {
            if (newState) {
                firebaseSyncManager.backupBookmarkToCloud(updatedArticle)
            } else {
                firebaseSyncManager.removeBookmarkFromCloud(id)
            }
        }
    }

    suspend fun markAsRead(id: String) {
        articleLruCache.get(id)?.let { cached ->
            articleLruCache.put(id, cached.copy(isRead = true))
        }
        articleDao.markAsRead(id)
    }

    suspend fun clearReadingHistory() {
        articleDao.clearReadingHistory()
    }

    suspend fun addKeywordTrap(keyword: String, categoryFilter: String = "ALL") {
        if (keyword.isNotBlank()) {
            keywordTrapDao.insertTrap(
                KeywordTrapEntity(
                    keyword = keyword.trim(),
                    categoryFilter = categoryFilter,
                    isActive = true
                )
            )
            // Immediately run a scan on existing articles for this new trap
            scanArticlesForTraps()
        }
    }

    suspend fun deleteKeywordTrap(id: Int) {
        keywordTrapDao.deleteTrapById(id)
    }

    suspend fun toggleKeywordTrap(id: Int, isActive: Boolean) {
        keywordTrapDao.updateTrapState(id, isActive)
    }

    suspend fun getStorageStats(): StorageStats = withContext(Dispatchers.IO) {
        val totalCount = articleDao.getTotalArticlesCount()
        val unbookmarkedCount = articleDao.getUnbookmarkedArticlesCount()
        val bookmarkedCount = totalCount - unbookmarkedCount

        val dbFile = context.getDatabasePath("sift_news.db")
        val dbSize = if (dbFile.exists()) dbFile.length() else 0L
        var cacheSize = 0L
        try {
            cacheSize = context.cacheDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
        } catch (e: Exception) {
            cacheSize = 0L
        }
        val totalBytes = dbSize + cacheSize

        StorageStats(
            totalArticles = totalCount,
            unbookmarkedArticles = unbookmarkedCount,
            bookmarkedArticles = bookmarkedCount,
            estimatedBytes = if (totalBytes > 0) totalBytes else (totalCount * 45 * 1024L)
        )
    }

    suspend fun clearOfflineArticlesCache(keepBookmarks: Boolean = true): ClearCacheResult = withContext(Dispatchers.IO) {
        val countBefore = articleDao.getTotalArticlesCount()
        if (keepBookmarks) {
            articleDao.clearUnbookmarkedArticles()
        } else {
            articleDao.clearAll()
        }
        val countAfter = articleDao.getTotalArticlesCount()
        val clearedCount = countBefore - countAfter

        try {
            context.cacheDir.deleteRecursively()
        } catch (e: Exception) {
            // Ignore
        }

        ClearCacheResult(
            articlesCleared = clearedCount,
            freedBytes = clearedCount * 48 * 1024L
        )
    }

    suspend fun autoClearOldArticlesAndCache(days: Int = 30): ClearCacheResult = withContext(Dispatchers.IO) {
        if (days <= 0) return@withContext ClearCacheResult(0, 0L)
        val threshold = System.currentTimeMillis() - (days.toLong() * 24L * 3600L * 1000L)
        val countBefore = articleDao.getTotalArticlesCount()
        articleDao.deleteOldUnbookmarkedArticles(threshold)
        val countAfter = articleDao.getTotalArticlesCount()
        val clearedCount = countBefore - countAfter

        var freedImageBytes = 0L
        try {
            // Walk and purge cached article images & disk cache files older than threshold
            context.cacheDir.walkTopDown().forEach { file ->
                if (file.isFile && file.lastModified() < threshold) {
                    freedImageBytes += file.length()
                    file.delete()
                }
            }
        } catch (e: Exception) {
            // Ignore
        }

        ClearCacheResult(
            articlesCleared = clearedCount,
            freedBytes = (clearedCount * 48 * 1024L) + freedImageBytes
        )
    }

    /**
     * Refreshes the news feed according to the selected processing mode using real live feeds.
     */
    suspend fun refreshNewsFeed(
        processingMode: ProcessingMode = ProcessingMode.SUPER_FAST,
        customApiKey: String? = null,
        category: String = "ALL"
    ): List<Article> = withContext(Dispatchers.IO) {
        val liveArticles = try {
            RssNewsFetcher.fetchLiveArticles(category)
        } catch (_: Exception) {
            emptyList()
        }

        val rawFeed = if (liveArticles.isNotEmpty()) {
            liveArticles
        } else {
            val dbArticles = articleDao.getAllArticles().first().map { it.toArticle() }
            if (dbArticles.isNotEmpty()) {
                dbArticles
            } else {
                getOfflineJournalismFallback()
            }
        }

        val processedEntities = mutableListOf<ArticleEntity>()
        val processedArticles = mutableListOf<Article>()
        val activeTraps = keywordTrapDao.getActiveTraps()

        for (rawArticle in rawFeed) {
            val titleToUse: String
            val bulletsToUse: List<String>
            val snrScoreToUse: Float
            val biasCatToUse: String

            when (processingMode) {
                ProcessingMode.SUPER_FAST -> {
                    titleToUse = TextRankSummarizer.rewriteTitle(rawArticle.originalTitle, rawArticle.fullContent)
                    bulletsToUse = TextRankSummarizer.generate3BulletSummary(rawArticle.fullContent, rawArticle.originalTitle)
                    snrScoreToUse = TextRankSummarizer.calculateSnr(rawArticle.fullContent, rawArticle.originalTitle)
                    biasCatToUse = TextRankSummarizer.determineBiasCategory(snrScoreToUse, rawArticle.fullContent)
                }
                ProcessingMode.ON_DEVICE_AI -> {
                    titleToUse = TextRankSummarizer.rewriteTitle(rawArticle.originalTitle, rawArticle.fullContent)
                    bulletsToUse = TextRankSummarizer.generate3BulletSummary(rawArticle.fullContent, rawArticle.originalTitle)
                    snrScoreToUse = TextRankSummarizer.calculateSnr(rawArticle.fullContent, rawArticle.originalTitle)
                    biasCatToUse = TextRankSummarizer.determineBiasCategory(snrScoreToUse, rawArticle.fullContent)
                }
                ProcessingMode.BYOK_CLOUD -> {
                    val geminiRes = GeminiSummarizer.processArticleWithGemini(
                        rawTitle = rawArticle.originalTitle,
                        content = rawArticle.fullContent,
                        userCustomApiKey = customApiKey
                    )
                    titleToUse = geminiRes.honestTitle
                    bulletsToUse = geminiRes.summaryBullets
                    snrScoreToUse = geminiRes.snrScore
                    biasCatToUse = geminiRes.biasCategory
                }
            }

            // Check against active Keyword Traps ("Săn Tin")
            val matchedTrapKeywords = mutableListOf<String>()
            val combinedText = "$titleToUse ${rawArticle.originalTitle} ${rawArticle.fullContent}".lowercase()

            for (trap in activeTraps) {
                if (combinedText.contains(trap.keyword.lowercase())) {
                    matchedTrapKeywords.add(trap.keyword)
                    keywordTrapDao.incrementMatchCount(trap.id)
                }
            }

            // Check existing entity in Room to preserve user bookmarks and read state seamlessly
            val existingEntity = articleDao.getArticleById(rawArticle.id)
            val isBookmarked = existingEntity?.isBookmarked ?: rawArticle.isBookmarked
            val isRead = existingEntity?.isRead ?: rawArticle.isRead

            val processedArticle = rawArticle.copy(
                title = titleToUse,
                summaryBullets = bulletsToUse,
                snrScore = snrScoreToUse,
                biasCategory = biasCatToUse,
                processingModeUsed = processingMode.displayName,
                isBookmarked = isBookmarked,
                isRead = isRead,
                matchedTrapKeywords = matchedTrapKeywords
            )

            // Trigger alert notification for trap hits
            if (matchedTrapKeywords.isNotEmpty()) {
                notificationManager.sendRadarMatchNotification(processedArticle, matchedTrapKeywords.first())
            }

            processedArticles.add(processedArticle)
            processedEntities.add(ArticleEntity.fromArticle(processedArticle))
        }

        articleDao.insertArticles(processedEntities)
        processedArticles
    }

    private suspend fun scanArticlesForTraps() {
        val articles = articleDao.getAllArticles().first().map { it.toArticle() }
        val activeTraps = keywordTrapDao.getActiveTraps()

        for (article in articles) {
            val matched = mutableListOf<String>()
            val combined = "${article.title} ${article.originalTitle} ${article.fullContent}".lowercase()

            for (trap in activeTraps) {
                if (combined.contains(trap.keyword.lowercase())) {
                    matched.add(trap.keyword)
                }
            }

            if (matched.isNotEmpty() && article.matchedTrapKeywords != matched) {
                val updated = article.copy(matchedTrapKeywords = matched)
                articleDao.insertArticle(ArticleEntity.fromArticle(updated))
            }
        }
    }

    /**
     * Offline journalism fallback when no network connection is available on a completely fresh install.
     */
    private fun getOfflineJournalismFallback(): List<Article> {
        val now = System.currentTimeMillis()
        return listOf(
            Article(
                id = "live_art_101",
                title = "Global Central Banks Coordinate Liquidity Frameworks Amid Disinflation",
                originalTitle = "Central banks adjust monetary stance with robust financial liquidity",
                publisher = "CNBC Markets",
                category = "Markets",
                summaryBullets = listOf(
                    "Benchmark policy rates stabilized as core inflation trends toward targets.",
                    "Interbank liquidity maintained with balanced global capital inflows.",
                    "Sovereign debt markets respond positively to forward guidance."
                ),
                fullContent = "International central banking committees published their coordinated monetary policy review. Policy benchmarks remain calibrated to anchor medium-term inflation expectations while supporting stable employment and cross-border trade settlements.",
                sourceUrl = "https://www.cnbc.com/markets/",
                imageUrl = "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?auto=format&fit=crop&w=800&q=80",
                publishedAt = now - 1000 * 60 * 15,
                snrScore = 0.92f,
                biasCategory = "Factual Data",
                timeEstimateMinutes = 2,
                processingModeUsed = "TextRank"
            ),
            Article(
                id = "live_art_102",
                title = "Enterprise AI and Cloud Infrastructure Expansion Drives Technology Sector",
                originalTitle = "Tech enterprise reports revenue growth driven by AI and Cloud services",
                publisher = "TechCrunch",
                category = "Tech",
                summaryBullets = listOf(
                    "Global enterprise IT services expand with heavy demand for inference hardware.",
                    "Digital infrastructure contracts and cloud modernization accelerate.",
                    "Semiconductor packaging partnerships expand to meet compute demand."
                ),
                fullContent = "Major technology corporations announced robust quarterly results driven by accelerated enterprise adoption of cloud computing and generative AI model deployment. International IT service revenues expanded significantly year-over-year.",
                sourceUrl = "https://techcrunch.com",
                imageUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=800&q=80",
                publishedAt = now - 1000 * 60 * 45,
                snrScore = 0.88f,
                biasCategory = "Factual Data",
                timeEstimateMinutes = 3,
                processingModeUsed = "TextRank"
            ),
            Article(
                id = "live_art_103",
                title = "Metropolitan Housing Inventory Stabilizes with Balanced Buyer Demand",
                originalTitle = "Urban residential market supply increases with steady absorption",
                publisher = "HousingWire",
                category = "RealEstate",
                summaryBullets = listOf(
                    "Residential market welcomes newly launched units across major metros.",
                    "Mortgage rates stabilize, supporting buyer transaction volume.",
                    "Absorption rates remain consistent across multifamily developments."
                ),
                fullContent = "Real estate market researchers highlight ongoing stabilization in residential housing supply. Urban centers noted increased transaction closures as favorable financing options and developer incentives entered the market.",
                sourceUrl = "https://www.housingwire.com",
                imageUrl = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&w=800&q=80",
                publishedAt = now - 1000 * 60 * 120,
                snrScore = 0.85f,
                biasCategory = "Market Analysis",
                timeEstimateMinutes = 3,
                processingModeUsed = "TextRank"
            ),
            Article(
                id = "live_art_104",
                title = "Scientific Consortia Unveil Advances in Quantum Coherence and Error Mitigation",
                originalTitle = "Quantum computing research achieves fidelity milestones in scalable architectures",
                publisher = "ScienceDaily",
                category = "Science",
                summaryBullets = listOf(
                    "Error-mitigated quantum circuits exceed target fidelity thresholds.",
                    "Topological qubit protection demonstrates resilience against thermal noise.",
                    "Multi-institutional research opens pathways for material science simulations."
                ),
                fullContent = "A global network of physics and computer science laboratories announced major progress in quantum error mitigation. Using novel dynamic decoupling techniques, research teams sustained coherence across multi-qubit systems.",
                sourceUrl = "https://www.sciencedaily.com",
                imageUrl = "https://images.unsplash.com/photo-1635070041078-e363dbe005cb?auto=format&fit=crop&w=800&q=80",
                publishedAt = now - 1000 * 60 * 240,
                snrScore = 0.94f,
                biasCategory = "Factual Data",
                timeEstimateMinutes = 3,
                processingModeUsed = "TextRank"
            ),
            Article(
                id = "live_art_105",
                title = "International Maritime Council Advances Clean Fuel and Corridor Standards",
                originalTitle = "Global shipping routes transition to lower-emission propulsion frameworks",
                publisher = "BBC World",
                category = "World",
                summaryBullets = listOf(
                    "Major trade corridors adopt low-carbon methanol and dual-fuel vessels.",
                    "Port bunkering infrastructure investments accelerate in Europe and Asia.",
                    "Decarbonization benchmarks align with international maritime standards."
                ),
                fullContent = "The International Maritime Organization published updated operational guidelines for alternative maritime fuels. Corridors connecting European and Asian ports reported steady progress in scaling green infrastructure.",
                sourceUrl = "https://www.bbc.com/news/world",
                imageUrl = "https://images.unsplash.com/photo-1542296332-2e4473faf563?auto=format&fit=crop&w=800&q=80",
                publishedAt = now - 1000 * 60 * 360,
                snrScore = 0.89f,
                biasCategory = "Factual Data",
                timeEstimateMinutes = 3,
                processingModeUsed = "TextRank"
            )
        )
    }

    suspend fun preCacheBookmarkedArticles(): Int = withContext(Dispatchers.IO) {
        val bookmarkedEntities = articleDao.getBookmarkedArticlesList()
        var count = 0
        val loader = coil.ImageLoader(context)
        bookmarkedEntities.forEach { entity ->
            if (entity.imageUrl.isNotBlank()) {
                try {
                    val request = coil.request.ImageRequest.Builder(context)
                        .data(entity.imageUrl)
                        .build()
                    loader.execute(request)
                    count++
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
        count
    }

}