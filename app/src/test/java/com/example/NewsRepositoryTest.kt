package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.ArticleEntity
import com.example.data.model.ProcessingMode
import com.example.data.repository.NewsRepository
import com.example.data.repository.TextRankSummarizer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NewsRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: NewsRepository
    private lateinit var context: Context

    @Before
    fun createDb() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        db.articleDao().insertArticle(
            ArticleEntity(
                id = "test_art_01",
                title = "Federal Reserve Adjusts Benchmark Rates",
                originalTitle = "SHOCKING: Federal Reserve adjusts policy rates!",
                publisher = "CNBC",
                category = "Markets",
                summaryBulletsJson = "Rate increase of 25 bps|||Inflation stabilizing|||Market reaction measured",
                fullContent = "The Federal Reserve adjusted benchmark interest rates by 25 basis points to stabilize inflation.",
                sourceUrl = "https://cnbc.com",
                imageUrl = "https://images.unsplash.com/photo-1611974789855",
                publishedAt = System.currentTimeMillis(),
                snrScore = 0.90f,
                biasCategory = "Factual Data",
                timeEstimateMinutes = 2,
                processingModeUsed = "TextRank",
                isBookmarked = false,
                isRead = false,
                matchedTrapKeywordsStr = ""
            )
        )
        repository = NewsRepository(context, db)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun textRankSummarizer_rewritesClickbaitTitle() {
        val rawTitle = "SHOCKING: Federal Reserve adjusts policy rates!"
        val content = "The Federal Reserve adjusted benchmark interest rates by 25 basis points to stabilize inflation."
        val rewritten = TextRankSummarizer.rewriteTitle(rawTitle, content)
        
        assertTrue(rewritten.isNotBlank())
        assertTrue(!rewritten.startsWith("SHOCKING:"))
    }

    @Test
    fun repository_refreshFeed_populatesArticles() = runBlocking {
        val articles = repository.refreshNewsFeed(ProcessingMode.SUPER_FAST)
        assertTrue(articles.isNotEmpty())
        
        val dbArticles = repository.allArticles.first()
        assertTrue(dbArticles.isNotEmpty())
        assertTrue(dbArticles.size >= articles.size)
    }

    @Test
    fun repository_toggleBookmark_updatesBookmarkState() = runBlocking {
        repository.toggleBookmark("test_art_01", false)
        val updated = repository.getArticleById("test_art_01")
        assertNotNull(updated)
        assertTrue(updated!!.isBookmarked)
    }
}
