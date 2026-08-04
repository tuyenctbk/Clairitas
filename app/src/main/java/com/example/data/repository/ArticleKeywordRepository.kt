package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.ArticleDao
import com.example.data.local.ArticleEntity
import com.example.data.local.KeywordDao
import com.example.data.local.KeywordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository class to handle data operations for Article and Keyword entities,
 * abstracting Room DAO access for offline access, saved news articles, and keyword alerts.
 */
class ArticleKeywordRepository(
    private val articleDao: ArticleDao,
    private val keywordDao: KeywordDao
) {

    constructor(context: Context) : this(
        AppDatabase.getDatabase(context).articleDao(),
        AppDatabase.getDatabase(context).keywordDao()
    )

    // --- Article Operations ---

    /**
     * Flow emitting all saved articles ordered by publish date (newest first).
     */
    val allArticles: Flow<List<ArticleEntity>> = articleDao.getAllArticles()

    /**
     * Flow emitting bookmarked articles for quick offline reading.
     */
    val bookmarkedArticles: Flow<List<ArticleEntity>> = articleDao.getBookmarkedArticles()

    /**
     * Fetches a single article by its unique ID.
     */
    suspend fun getArticleById(id: String): ArticleEntity? = withContext(Dispatchers.IO) {
        articleDao.getArticleById(id)
    }

    /**
     * Inserts or replaces a list of articles for offline storage.
     */
    suspend fun saveArticles(articles: List<ArticleEntity>) = withContext(Dispatchers.IO) {
        articleDao.insertArticles(articles)
    }

    /**
     * Inserts or replaces a single article for offline storage.
     */
    suspend fun saveArticle(article: ArticleEntity) = withContext(Dispatchers.IO) {
        articleDao.insertArticle(article)
    }

    /**
     * Toggles or updates the bookmarked state for an article.
     */
    suspend fun toggleBookmark(id: String, isBookmarked: Boolean) = withContext(Dispatchers.IO) {
        articleDao.updateBookmarkState(id, isBookmarked)
    }

    /**
     * Marks an article as read.
     */
    suspend fun markAsRead(id: String) = withContext(Dispatchers.IO) {
        articleDao.markAsRead(id)
    }

    /**
     * Clears all cached articles from the database.
     */
    suspend fun clearAllArticles() = withContext(Dispatchers.IO) {
        articleDao.clearAll()
    }


    // --- Keyword Operations ---

    /**
     * Flow emitting all user-defined keyword alerts ordered by creation timestamp.
     */
    val allKeywords: Flow<List<KeywordEntity>> = keywordDao.getAllKeywords()

    /**
     * Retrieves currently active keyword alerts.
     */
    suspend fun getActiveKeywords(): List<KeywordEntity> = withContext(Dispatchers.IO) {
        keywordDao.getActiveKeywords()
    }

    /**
     * Gets a specific keyword alert entity by ID.
     */
    suspend fun getKeywordById(id: Int): KeywordEntity? = withContext(Dispatchers.IO) {
        keywordDao.getKeywordById(id)
    }

    /**
     * Adds a new user-defined keyword alert for news tracking and offline notification.
     */
    suspend fun addKeyword(
        keyword: String,
        alertFrequency: String = "INSTANT",
        categoryFilter: String = "ALL"
    ): Long = withContext(Dispatchers.IO) {
        if (keyword.isBlank()) return@withContext -1L
        val keywordEntity = KeywordEntity(
            keyword = keyword.trim(),
            alertFrequency = alertFrequency,
            categoryFilter = categoryFilter,
            lastCheckedTimestamp = System.currentTimeMillis(),
            isActive = true
        )
        keywordDao.insertKeyword(keywordEntity)
    }

    /**
     * Updates an existing keyword alert.
     */
    suspend fun updateKeyword(keyword: KeywordEntity) = withContext(Dispatchers.IO) {
        keywordDao.updateKeyword(keyword)
    }

    /**
     * Toggles active status of a keyword alert.
     */
    suspend fun toggleKeywordState(id: Int, isActive: Boolean) = withContext(Dispatchers.IO) {
        keywordDao.updateKeywordActiveState(id, isActive)
    }

    /**
     * Updates the last checked timestamp for a keyword alert.
     */
    suspend fun updateLastCheckedTimestamp(id: Int, timestamp: Long = System.currentTimeMillis()) = withContext(Dispatchers.IO) {
        keywordDao.updateLastCheckedTimestamp(id, timestamp)
    }

    /**
     * Increments the match count for a keyword alert when articles match.
     */
    suspend fun incrementMatchCount(id: Int) = withContext(Dispatchers.IO) {
        keywordDao.incrementMatchCount(id)
    }

    /**
     * Deletes a keyword alert by ID.
     */
    suspend fun deleteKeywordById(id: Int) = withContext(Dispatchers.IO) {
        keywordDao.deleteKeywordById(id)
    }

    /**
     * Deletes a specific keyword alert entity.
     */
    suspend fun deleteKeyword(keyword: KeywordEntity) = withContext(Dispatchers.IO) {
        keywordDao.deleteKeyword(keyword)
    }

    /**
     * Clears all keyword alerts.
     */
    suspend fun clearAllKeywords() = withContext(Dispatchers.IO) {
        keywordDao.clearAll()
    }
}
