package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.ArticleEntity
import com.example.data.local.KeywordTrapEntity
import com.example.data.model.Article
import com.example.data.model.ProcessingMode
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

    val allArticles: Flow<List<Article>> = articleDao.getAllArticles().map { entities ->
        entities.map { it.toArticle() }
    }

    val bookmarkedArticles: Flow<List<Article>> = articleDao.getBookmarkedArticles().map { entities ->
        entities.map { it.toArticle() }
    }

    val allKeywordTraps: Flow<List<KeywordTrapEntity>> = keywordTrapDao.getAllTraps()

    suspend fun getArticleById(id: String): Article? {
        return articleDao.getArticleById(id)?.toArticle()
    }

    suspend fun toggleBookmark(id: String, currentState: Boolean) {
        articleDao.updateBookmarkState(id, !currentState)
    }

    suspend fun markAsRead(id: String) {
        articleDao.markAsRead(id)
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

    /**
     * Refreshes the news feed according to the selected processing mode.
     */
    suspend fun refreshNewsFeed(
        processingMode: ProcessingMode = ProcessingMode.SUPER_FAST,
        customApiKey: String? = null
    ): List<Article> = withContext(Dispatchers.IO) {
        val rawFeed = getCuratedSampleNewsFeed()
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
     * Returns curated high-density news items across Tech, Markets, Real Estate, Business, Science, World.
     */
    private fun getCuratedSampleNewsFeed(): List<Article> {
        val now = System.currentTimeMillis()
        return listOf(
            Article(
                id = "art_101",
                title = "Central bank adjusts policy rates, robust banking liquidity",
                originalTitle = "SHOCKING: Central bank makes unprecedented interest rate move shaking global financial markets!",
                publisher = "Financial News / Banking",
                category = "Markets",
                summaryBullets = listOf(
                    "Benchmark policy rate maintained at competitive levels with ample system liquidity.",
                    "Overnight interbank rates ease down to 3.8% annually amid balanced cash flows.",
                    "Institutional foreign inflows concentrate heavily on top-tier banking equities."
                ),
                fullContent = "The central bank officially announced monetary adjustments to stabilize interbank liquidity, bringing lending rates down to 3.8%. Financial analysts note this proactive step stabilizes investor sentiment and mitigates imported inflationary pressures.",
                sourceUrl = "https://finance.example.com",
                imageUrl = "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?auto=format&fit=crop&w=800&q=80",
                publishedAt = now - 1000 * 60 * 15,
                snrScore = 0.92f,
                biasCategory = "Factual Data",
                timeEstimateMinutes = 2,
                processingModeUsed = "TextRank"
            ),
            Article(
                id = "art_102",
                title = "Leading tech enterprise reports robust 19.5% revenue growth driven by AI and Cloud services",
                originalTitle = "UNBELIEVABLE: Secret profit surge of tech giant stuns industry competitors!",
                publisher = "Global Tech News",
                category = "Tech",
                summaryBullets = listOf(
                    "Global IT services revenue expands 28% year-over-year.",
                    "Digital transformation contracts and semiconductor solutions drive major profitability.",
                    "Technology division maintains primary growth driver status representing 62% of total revenue."
                ),
                fullContent = "A leading technology corporation published its robust seven-month financial results, achieving 58% of its annual plan. International IT services spearheaded growth with 28% expansion, highlighted by enterprise AI solution deployments and semiconductor manufacturing partnerships.",
                sourceUrl = "https://technews.example.com",
                imageUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=800&q=80",
                publishedAt = now - 1000 * 60 * 45,
                snrScore = 0.88f,
                biasCategory = "Factual Data",
                timeEstimateMinutes = 3,
                processingModeUsed = "TextRank"
            ),
            Article(
                id = "art_103",
                title = "Metropolitan residential supply increases 40% with high absorption rates",
                originalTitle = "CRISIS: Real estate prices skyrocket in unprecedented property bubble!",
                publisher = "Property & Economy",
                category = "RealEstate",
                summaryBullets = listOf(
                    "Market welcomes over 3,200 newly launched premium apartments.",
                    "Luxury segment accounts for 70% of total new inventory.",
                    "Transaction absorption rate reaches 68% backed by extended developer payment incentives."
                ),
                fullContent = "The latest quarterly real estate research report highlights a clear recovery in housing supply. Major urban districts recorded over 3,200 newly launched units, primarily in the high-end segment, supported by favorable mortgage interest rate reductions.",
                sourceUrl = "https://property.example.com",
                imageUrl = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&w=800&q=80",
                publishedAt = now - 1000 * 60 * 120,
                snrScore = 0.85f,
                biasCategory = "Market Analysis",
                timeEstimateMinutes = 3,
                processingModeUsed = "TextRank"
            ),
            Article(
                id = "art_104",
                title = "Federal Reserve maintains benchmark interest rates, signaling potential easing in upcoming cycle",
                originalTitle = "BREATHTAKING: Fed decision shocks trillion-dollar global markets overnight!",
                publisher = "Reuters / World",
                category = "Business",
                summaryBullets = listOf(
                    "FOMC committee keeps target federal funds rate steady at 5.25% - 5.50%.",
                    "Chair Jerome Powell highlights PCE inflation cooling closer to the 2% objective.",
                    "Global equities rally 1.2% following dovish policy guidance."
                ),
                fullContent = "Federal Reserve Chair Jerome Powell concluded the monetary policy meeting by maintaining steady interest rates. However, post-meeting commentary opened the door for potential rate reductions in upcoming meetings as employment and inflation data align with projections.",
                sourceUrl = "https://reuters.com",
                imageUrl = "https://images.unsplash.com/photo-1526304640581-d334cdbbf45e?auto=format&fit=crop&w=800&q=80",
                publishedAt = now - 1000 * 60 * 240,
                snrScore = 0.90f,
                biasCategory = "Factual Data",
                timeEstimateMinutes = 4,
                processingModeUsed = "TextRank"
            ),
            Article(
                id = "art_105",
                title = "National strategy launched for advanced semiconductor research and microchip engineering training",
                originalTitle = "EXPLOSIVE: Nation launches massive high-tech initiative leaving superpowers stunned!",
                publisher = "Science Daily",
                category = "Science",
                summaryBullets = listOf(
                    "Semiconductor workforce development initiative targets 50,000 engineers by 2030.",
                    "Establishment of three shared microchip design and testing laboratories.",
                    "Major technology conglomerates commit millions in EDA software license grants."
                ),
                fullContent = "The government approved a national strategy for semiconductor industry development. The country aims to become a premier regional hub for chip design and packaging, focusing on high-quality engineering education and state-of-the-art laboratory testing infrastructure.",
                sourceUrl = "https://sciencedaily.example.com",
                imageUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=800&q=80",
                publishedAt = now - 1000 * 60 * 360,
                snrScore = 0.89f,
                biasCategory = "Factual Data",
                timeEstimateMinutes = 3,
                processingModeUsed = "TextRank"
            ),
            Article(
                id = "art_106",
                title = "United States: Silicon Valley tech giants unveil quantum computing breakthrough with 99.9% fidelity",
                originalTitle = "UNBELIEVABLE: Quantum computers just shattered every encryption code on Earth overnight!",
                publisher = "TechCrunch / US",
                category = "Tech",
                summaryBullets = listOf(
                    "Researchers achieved error-corrected quantum operations exceeding 99.9% fidelity across 1,000 qubits.",
                    "Commercial cloud access will be made available to enterprise research labs starting Q4.",
                    "Cybersecurity standards will require post-quantum cryptographic migration by 2027."
                ),
                fullContent = "Leading research institutes in Silicon Valley announced a major milestone in fault-tolerant quantum computing. By utilizing novel topological qubit stabilization, the processor successfully executed complex molecular simulations in seconds that would take classical supercomputers millennia. Industry experts emphasize the importance of upgrading enterprise security frameworks.",
                sourceUrl = "https://techcrunch.com",
                imageUrl = "https://images.unsplash.com/photo-1635070041078-e363dbe005cb?auto=format&fit=crop&w=800&q=80",
                publishedAt = now - 1000 * 60 * 400,
                snrScore = 0.94f,
                biasCategory = "Factual Data",
                timeEstimateMinutes = 3,
                processingModeUsed = "TextRank"
            ),
            Article(
                id = "art_107",
                title = "European Union: ECB signals upcoming 25bps rate reduction amid stabilizing eurozone inflation",
                originalTitle = "CRISIS: European economy faces catastrophic collapse as ECB loses control of rates!",
                publisher = "Financial Times / Europe",
                category = "Markets",
                summaryBullets = listOf(
                    "Eurozone Harmonized Index of Consumer Prices (HICP) cooled to exactly 2.0% year-on-year.",
                    "Governing council members indicate broad consensus for a September monetary easing cycle.",
                    "European banking stocks rallied 1.8% following favorable liquidity stress test results."
                ),
                fullContent = "Frankfurt-based European Central Bank officials noted that inflationary pressures have successfully converged toward the medium-term 2% target. With wage growth moderating and energy costs stabilizing, analysts widely anticipate a quarter-point rate reduction at the upcoming policy meeting to support industrial manufacturing recovery across Germany and France.",
                sourceUrl = "https://ft.com",
                imageUrl = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&w=800&q=80",
                publishedAt = now - 1000 * 60 * 500,
                snrScore = 0.91f,
                biasCategory = "Market Analysis",
                timeEstimateMinutes = 4,
                processingModeUsed = "TextRank"
            ),
            Article(
                id = "art_108",
                title = "Asia-Pacific: Japan and Singapore forge bilateral AI pact to standardize cross-border data governance",
                originalTitle = "SHOCKING ALLIANCE: Asian superpowers form secret tech bloc to dominate global AI!",
                publisher = "Nikkei Asia / APAC",
                category = "Business",
                summaryBullets = listOf(
                    "Tokyo and Singapore signed a comprehensive digital economy agreement covering secure LLM training.",
                    "Over $2 billion allocated for joint semiconductor packaging and green data center infrastructure.",
                    "Facilitates seamless regulatory compliance for multinational fintech and AI startups."
                ),
                fullContent = "In a landmark regional summit, economic ministers from Japan and Singapore solidified a comprehensive digital and AI governance pact. The framework establishes interoperable data trust standards, accelerates semiconductor supply chain resilience, and provides grants for cross-border artificial intelligence research initiatives.",
                sourceUrl = "https://asia.nikkei.com",
                imageUrl = "https://images.unsplash.com/photo-1526304640581-d334cdbbf45e?auto=format&fit=crop&w=800&q=80",
                publishedAt = now - 1000 * 60 * 600,
                snrScore = 0.88f,
                biasCategory = "Factual Data",
                timeEstimateMinutes = 3,
                processingModeUsed = "TextRank"
            ),
            Article(
                id = "art_109",
                title = "Global Trade Council issues report on sustainable maritime logistics and clean fuel transitions",
                originalTitle = "CRISIS: Worldwide shipping supply chains threatened by massive green fuel mandates!",
                publisher = "Global Logistics Watch",
                category = "Global",
                summaryBullets = listOf(
                    "International maritime routes adopt low-emission methanol and green ammonia propulsion.",
                    "Port infrastructure investments total $45B across major European and Asian shipping hubs.",
                    "Freight rates remain stable while decarbonization targets move forward for 2030."
                ),
                fullContent = "The Global Maritime Trade Organization published its comprehensive status report on zero-emission freight vessels. Key shipping corridors report steady progress in adopting green ammonia and dual-fuel container fleets.",
                sourceUrl = "https://globallogistics.example.com",
                imageUrl = "https://images.unsplash.com/photo-1542296332-2e4473faf563?auto=format&fit=crop&w=800&q=80",
                publishedAt = now - 1000 * 60 * 700,
                snrScore = 0.87f,
                biasCategory = "Factual Data",
                timeEstimateMinutes = 3,
                processingModeUsed = "TextRank"
            ),
            Article(
                id = "art_110",
                title = "AI Ethics Board releases unified benchmark for open-source model transparency and security",
                originalTitle = "SHOCKING: Artificial Intelligence watchdog exposes hidden risks in open source models!",
                publisher = "AI Horizon Brief",
                category = "AI",
                summaryBullets = listOf(
                    "Standardized safety benchmark evaluates reasoning accuracy and bias mitigation across 50 LLMs.",
                    "Leading open weights developers commit to automated red-teaming prior to weight release.",
                    "Industry consensus aims to harmonize safety standards for enterprise AI deployments."
                ),
                fullContent = "A consortium of international AI researchers and safety laboratories introduced a comprehensive evaluation suite for large language models. The benchmark provides standardized metrics for evaluating hallucinations, alignment, and security boundaries.",
                sourceUrl = "https://aihorizon.example.com",
                imageUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=800&q=80",
                publishedAt = now - 1000 * 60 * 800,
                snrScore = 0.93f,
                biasCategory = "Factual Data",
                timeEstimateMinutes = 3,
                processingModeUsed = "TextRank"
            )
        )
    }
}
