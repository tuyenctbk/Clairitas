package com.example.service

import android.content.Context
import android.util.Log
import com.example.data.local.ArticleDao
import com.example.data.model.Article
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
/ * Service handling cloud synchronization with Firebase Firestore for user-bookmarked articles,
 * ensuring bi-directional consistency with the local Room database state.
 */
class FirebaseSyncManager(
    private val context: Context,
    private val articleDao: ArticleDao
) {
    companion object {
        private const val TAG = "FirebaseSyncManager"
        private const val BOOKMARKS_COLLECTION = "user_bookmarks"
    }

    private var db: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (e: Exception) {
        Log.w(TAG, "FirebaseFirestore initialization failed or missing config: ${e.message}")
        null
    }

    /**
     * Backs up a bookmarked article to Firebase Firestore.
     */
    suspend fun backupBookmarkToCloud(article: Article) = withContext(Dispatchers.IO) {
        val firestore = db ?: return@withContext
        try {
            val bookmarkData = mapOf(
                "id" to article.id,
                "title" to article.title,
                "publisher" to article.publisher,
                "category" to article.category,
                "sourceUrl" to article.sourceUrl,
                "publishedAt" to article.publishedAt,
                "isBookmarked" to article.isBookmarked,
                "timestamp" to System.currentTimeMillis()
            )
            firestore.collection(BOOKMARKS_COLLECTION)
                .document(article.id)
                .set(bookmarkData)
                .await()
            Log.d(TAG, "Successfully backed up bookmark ${article.id} to Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to backup bookmark to Firestore: ${e.message}")
        }
    }

    /**
     * Removes a bookmarked article from Firebase Firestore cloud store.
     */
    suspend fun removeBookmarkFromCloud(articleId: String) = withContext(Dispatchers.IO) {
        val firestore = db ?: return@withContext
        try {
            firestore.collection(BOOKMARKS_COLLECTION)
                .document(articleId)
                .delete()
                .await()
            Log.d(TAG, "Successfully removed bookmark $articleId from Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove bookmark from Firestore: ${e.message}")
        }
    }

    /**
     * Restores bookmarks from Firebase Firestore and updates local Room database.
     */
    suspend fun restoreBookmarksFromCloud(): Int = withContext(Dispatchers.IO) {
        val firestore = db ?: return@withContext 0
        try {
            val snapshot = firestore.collection(BOOKMARKS_COLLECTION).get().await()
            var count = 0
            for (doc in snapshot.documents) {
                val articleId = doc.getString("id") ?: continue
                val existing = articleDao.getArticleById(articleId)
                if (existing != null && !existing.isBookmarked) {
                    articleDao.updateBookmarkState(articleId, isBookmarked = true)
                    count++
                }
            }
            Log.d(TAG, "Restored $count bookmarks from Firestore into Room database")
            count
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring bookmarks from Firestore: ${e.message}")
            0
        }
    }
}
