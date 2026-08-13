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
    fun createDb() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
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
        assertEquals(articles.size, dbArticles.size)
    }

    @Test
    fun repository_toggleBookmark_updatesBookmarkState() = runBlocking {
        val articles = repository.refreshNewsFeed(ProcessingMode.SUPER_FAST)
        val firstArticle = articles.first()
        
        repository.toggleBookmark(firstArticle.id, false)
        val updated = repository.getArticleById(firstArticle.id)
        assertNotNull(updated)
        assertTrue(updated!!.isBookmarked)
    }
}
