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

    /**
     * Refreshes the news feed according to the selected processing mode.
     */
    suspend fun refreshNewsFeed(
        processingMode: ProcessingMode = ProcessingMode.SUPER_FAST,
        customApiKey: String? = null
    ) = withContext(Dispatchers.IO) {
        val rawFeed = getCuratedSampleNewsFeed()
        val processedEntities = mutableListOf<ArticleEntity>()
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

            val processedArticle = rawArticle.copy(
                title = titleToUse,
                summaryBullets = bulletsToUse,
                snrScore = snrScoreToUse,
                biasCategory = biasCatToUse,
                processingModeUsed = processingMode.displayName,
                matchedTrapKeywords = matchedTrapKeywords
            )

            // Trigger alert notification for trap hits
            if (matchedTrapKeywords.isNotEmpty()) {
                notificationManager.sendRadarMatchNotification(processedArticle, matchedTrapKeywords.first())
            }

            processedEntities.add(ArticleEntity.fromArticle(processedArticle))
        }

        articleDao.insertArticles(processedEntities)
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
                title = "NHNN điều chỉnh tỷ giá trung tâm giảm 15 đồng, thanh khoản ngân hàng dồi dào",
                originalTitle = "SỐC: Ngân hàng Nhà nước ra quyết định chấn động về tỷ giá làm chao đảo thị trường tài chính!",
                publisher = "CafeF / Ngân hàng",
                category = "Markets",
                summaryBullets = listOf(
                    "NHNN niêm yết tỷ giá trung tâm ở mức 24.230 VND/USD, giảm 15 đồng so với phiên trước.",
                    "Lãi suất liên ngân hàng kỳ hạn qua đêm duy trì ở mức thấp 3,8%/năm nhờ lượng thanh khoản dồi dào.",
                    "Khối ngoại ghi nhận mua ròng 320 tỷ đồng trên sàn HOSE tập trung vào nhóm cổ phiếu ngân hàng."
                ),
                fullContent = "Ngân hàng Nhà nước hôm nay chính thức công bố điều chỉnh tỷ giá trung tâm giảm 15 đồng xuống mức 24.230 VND/USD. Lượng thanh khoản hệ thống ngân hàng dồi dào giúp lãi suất liên ngân hàng hạ nhiệt về 3.8%. Các chuyên gia tài chính đánh giá bước đi này giúp ổn định tâm lý nhà đầu tư và giảm áp lực lạm phát nhập khẩu.",
                sourceUrl = "https://cafef.vn",
                imageUrl = "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?auto=format&fit=crop&w=800&q=80",
                publishedAt = now - 1000 * 60 * 15, // 15 mins ago
                snrScore = 0.92f,
                biasCategory = "Dữ liệu thực tế (Factual Data)",
                timeEstimateMinutes = 2,
                processingModeUsed = "TextRank"
            ),
            Article(
                id = "art_102",
                title = "FPT công bố doanh thu 7 tháng đạt 32.800 tỷ đồng, tăng trưởng 19,5% nhờ mảng AI & Semiconductor",
                originalTitle = "BẬT MÍ: Bí mật doanh thu khủng của tập đoàn FPT khiến đối thủ điếng người!",
                publisher = "TechNews VN",
                category = "Tech",
                summaryBullets = listOf(
                    "Doanh thu dịch vụ công nghệ thông tin nước ngoài đạt 18.500 tỷ đồng, tăng 28% so với cùng kỳ.",
                    "Hợp đồng chuyển đổi số và bán chip bán dẫn tại thị trường Nhật Bản và Bắc Mỹ đóng góp lớn vào lợi nhuận.",
                    "Khối công nghệ tiếp tục là động lực chính chiếm 62% tổng doanh thu toàn tập đoàn."
                ),
                fullContent = "Tập đoàn FPT vừa công bố báo cáo kết quả kinh doanh 7 tháng đầu năm. Tổng doanh thu đạt 32.800 tỷ đồng, hoàn thành 58% kế hoạch năm. Khối Công nghệ thông tin nước ngoài tăng trưởng bứt phá 28%, nổi bật là các hợp đồng cung cấp giải pháp AI và gia công chip bán dẫn cho đối tác Châu Mỹ.",
                sourceUrl = "https://vnexpress.net",
                imageUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=800&q=80",
                publishedAt = now - 1000 * 60 * 45, // 45 mins ago
                snrScore = 0.88f,
                biasCategory = "Dữ liệu thực tế (Factual Data)",
                timeEstimateMinutes = 3,
                processingModeUsed = "TextRank"
            ),
            Article(
                id = "art_103",
                title = "Nguồn cung căn hộ mở bán tại TP.HCM tăng 40%, giá chung cư tiệm cận 75 triệu/m2",
                originalTitle = "NÓNG: Giá bất động sản bùng nổ đỉnh điểm, sốt đất càn quét TP.HCM!",
                publisher = "VnEconomy / BĐS",
                category = "RealEstate",
                summaryBullets = listOf(
                    "Thị trường đón nhận hơn 3.200 căn hộ mới mở bán tại khu vực TP. Thủ Đức và Quận 7.",
                    "Phân khúc cao cấp chiếm 70% tổng nguồn cung, mức giá trung bình tiệm cận 75 triệu đồng/m2.",
                    "Tỷ lệ hấp thụ đạt 68% nhờ chính sách gia hạn thanh toán 3 năm của các chủ đầu tư lớn."
                ),
                fullContent = "Báo cáo nghiên cứu thị trường bất động sản quý mới nhất chỉ ra sự phục hồi rõ nét về nguồn cung. TP.HCM ghi nhận hơn 3.200 căn hộ mở bán mới, chủ yếu thuộc phân khúc cao cấp. Tỷ lệ giao dịch thành công đạt gần 70% nhờ dòng tiền tín dụng bất động sản hạ lãi suất về mức 6,5%/năm.",
                sourceUrl = "https://vneconomy.vn",
                imageUrl = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&w=800&q=80",
                publishedAt = now - 1000 * 60 * 120, // 2 hours ago
                snrScore = 0.85f,
                biasCategory = "Phân tích thị trường (Market Analysis)",
                timeEstimateMinutes = 3,
                processingModeUsed = "TextRank"
            ),
            Article(
                id = "art_104",
                title = "Cục Dự trữ Liên bang Mỹ (Fed) giữ nguyên lãi suất 5,25% - 5,5%, tín hiệu hạ lãi suất tháng 9",
                originalTitle = "HÉ LỘ: Quyết định nghẹt thở từ Fed khiến cả thế giới chao đảo nghìn tỷ USD!",
                publisher = "Reuters / World",
                category = "Business",
                summaryBullets = listOf(
                    "Ủy ban FOMC giữ nguyên lãi suất điều hành ở vùng 5,25% - 5,50% đúng như dự báo.",
                    "Chủ tịch Jerome Powell cho biết chỉ số lạm phát PCE hạ nhiệt về 2,6% tiến gần mục tiêu 2%.",
                    "Thị trường chứng khoán toàn cầu tăng nhẹ 1,2% sau phát biểu phát đi tín hiệu hạ lãi suất."
                ),
                fullContent = "Chủ tịch Cục Dự trữ Liên bang Mỹ Jerome Powell vừa kết thúc phiên họp chính sách tiền tệ. Fed quyết định duy trì lãi suất ổn định ở mức 5.25%-5.50%. Tuy nhiên, tuyên bố sau họp mở ra khả năng cắt giảm lãi suất ngay trong kỳ họp tháng 9 tới nếu dữ liệu việc làm và lạm phát tiếp tục đi đúng lộ trình.",
                sourceUrl = "https://reuters.com",
                imageUrl = "https://images.unsplash.com/photo-1526304640581-d334cdbbf45e?auto=format&fit=crop&w=800&q=80",
                publishedAt = now - 1000 * 60 * 240, // 4 hours ago
                snrScore = 0.90f,
                biasCategory = "Dữ liệu thực tế (Factual Data)",
                timeEstimateMinutes = 4,
                processingModeUsed = "TextRank"
            ),
            Article(
                id = "art_105",
                title = "Việt Nam chuẩn bị xây dựng trung tâm nghiên cứu bán dẫn và đào tạo 50.000 kỹ sư microchip",
                originalTitle = "CHẤN ĐỘNG: Việt Nam ra cú đấm thép khiến các cường quốc công nghệ ngỡ ngàng!",
                publisher = "Dân Trí / Khoa Học",
                category = "Science",
                summaryBullets = listOf(
                    "Đề án phát triển nguồn nhân lực ngành bán dẫn phê duyệt mục tiêu đào tạo 50.000 kỹ sư đến năm 2030.",
                    "Thành lập 3 trung tâm dùng chung về thiết kế và kiểm thử vi mạch tại Hà Nội, Đà Nẵng và TP.HCM.",
                    "Các tập đoàn công nghệ lớn cam kết tài trợ bản quyền phần mềm EDA trị giá hàng chục triệu USD."
                ),
                fullContent = "Chính phủ vừa phê duyệt chiến lược quốc gia về phát triển công nghiệp bán dẫn. Việt Nam đặt mục tiêu trở thành trung tâm thiết kế chip và kiểm thử đóng gói hàng đầu khu vực, tập trung đào tạo 50.000 kỹ sư chất lượng cao và đầu tư trang thiết bị đo kiểm thử phòng lab hiện đại.",
                sourceUrl = "https://dantri.com.vn",
                imageUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=800&q=80",
                publishedAt = now - 1000 * 60 * 360, // 6 hours ago
                snrScore = 0.89f,
                biasCategory = "Dữ liệu thực tế (Factual Data)",
                timeEstimateMinutes = 3,
                processingModeUsed = "TextRank"
            )
        )
    }
}
