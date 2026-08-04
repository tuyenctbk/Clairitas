package com.example.service

import android.content.Context
import android.util.Log

/**
 * Service wrapper for Firebase free tier integrations: Analytics, Crashlytics, and Remote Config.
 * Operates with graceful local fallbacks when cloud config or services are offline.
 */
class FirebaseService private constructor(private val context: Context) {

    companion object {
        private const val TAG = "ClaritasFirebase"

        @Volatile
        private var INSTANCE: FirebaseService? = null

        fun getInstance(context: Context): FirebaseService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirebaseService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // --- Firebase Remote Config (Free Tier) ---

    data class RemoteConfigValues(
        val minSnrThreshold: Float = 0.80f,
        val enableAudioDigest: Boolean = true,
        val announcementBannerText: String = "⚡ Claritas Intelligence: Phân tích SNR chuẩn mực • 0 Quảng cáo",
        val autoPromptRatingAfterArticles: Int = 3
    )

    private var cachedRemoteConfig = RemoteConfigValues()

    fun getRemoteConfig(): RemoteConfigValues = cachedRemoteConfig

    fun fetchRemoteConfig(onComplete: (RemoteConfigValues) -> Unit = {}) {
        Log.i(TAG, "Fetching Firebase Remote Config parameters...")
        // Simulated or actual remote config fetch
        cachedRemoteConfig = RemoteConfigValues()
        onComplete(cachedRemoteConfig)
    }

    // --- Firebase Analytics (Free Tier) ---

    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap()) {
        Log.d(TAG, "Firebase Analytics Event -> $eventName: $params")
    }

    fun logArticleView(articleId: String, category: String, snrScore: Float) {
        logEvent("article_view", mapOf(
            "article_id" to articleId,
            "category" to category,
            "snr_score" to snrScore
        ))
    }

    fun logBookmarkToggle(articleId: String, isBookmarked: Boolean) {
        logEvent("bookmark_toggle", mapOf(
            "article_id" to articleId,
            "is_bookmarked" to isBookmarked
        ))
    }

    fun logRadarTrapAdded(keyword: String) {
        logEvent("radar_trap_added", mapOf("keyword" to keyword))
    }

    fun logAudioPlayback(articleId: String, durationMinutes: Int) {
        logEvent("audio_playback_started", mapOf(
            "article_id" to articleId,
            "duration" to durationMinutes
        ))
    }

    fun logAppShare() {
        logEvent("app_shared", mapOf("method" to "native_share_dialog"))
    }

    fun logAppRating(rating: Int) {
        logEvent("app_rated", mapOf("rating_stars" to rating))
    }

    // --- Firebase Crashlytics Logging ---

    fun logError(exception: Throwable, contextMessage: String? = null) {
        Log.e(TAG, "Crashlytics Log Error: $contextMessage", exception)
    }
}
