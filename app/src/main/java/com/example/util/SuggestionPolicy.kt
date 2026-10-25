package com.example.util

import android.content.SharedPreferences

enum class SuggestionType {
    NONE,
    RATING,
    SHARE,
    UPDATE
}

/**
 * Intelligent rate-limiting policy and anti-spam controller for app suggestions.
 * Guarantees that rating, share, and update prompts:
 * 1. Never show repeatedly or frequently (5-day cooldown after dismissal, 3-day global gap).
 * 2. Only show after positive user milestones (articles read, bookmarks saved, launches).
 * 3. Never stack or clash (strictly max 1 prompt per app session).
 * 4. Permanently silence after 2 dismissals or user completion.
 */
object SuggestionPolicy {
    const val PREF_RATING_COMPLETED = "rating_prompt_completed"
    const val PREF_RATING_DISMISS_COUNT = "rating_prompt_dismiss_count"
    const val PREF_RATING_LAST_DISMISS_TIME = "rating_prompt_last_dismiss_time"

    const val PREF_SHARE_COMPLETED = "share_prompt_completed"
    const val PREF_SHARE_DISMISS_COUNT = "share_prompt_dismiss_count"
    const val PREF_SHARE_LAST_DISMISS_TIME = "share_prompt_last_dismiss_time"

    const val PREF_LAST_PROMPT_SHOWN_TIME = "last_prompt_shown_timestamp"
    const val PREF_LAST_SEEN_VERSION_CODE = "last_seen_version_code"
    const val PREF_APP_LAUNCH_COUNT = "app_launch_count"
    const val PREF_TOTAL_READ_COUNT = "total_articles_read_count"
    const val PREF_BOOKMARKS_COUNT = "total_bookmarks_saved_count"

    // Cooldown intervals:
    // Minimum 5 days after a dismissal before showing another prompt of same or different type
    const val COOLDOWN_AFTER_DISMISS_MS = 5 * 24 * 60 * 60 * 1000L // 5 days
    // Minimum 3 days global cooldown between any prompt presentations
    const val GLOBAL_PROMPT_INTERVAL_MS = 3 * 24 * 60 * 60 * 1000L // 3 days
    // Max dismissals before permanent suppression
    const val MAX_DISMISSALS = 2

    // Thresholds for proper timing
    const val MIN_LAUNCHES_FOR_RATING = 3
    const val MIN_ARTICLES_READ_FOR_RATING = 5

    const val MIN_LAUNCHES_FOR_SHARE = 2
    const val MIN_BOOKMARKS_FOR_SHARE = 3

    fun canShowRatingPrompt(
        prefs: SharedPreferences,
        hasShownThisSession: Boolean,
        currentTimeMs: Long = System.currentTimeMillis()
    ): Boolean {
        if (hasShownThisSession) return false
        if (prefs.getBoolean(PREF_RATING_COMPLETED, false)) return false

        val dismissCount = prefs.getInt(PREF_RATING_DISMISS_COUNT, 0)
        if (dismissCount >= MAX_DISMISSALS) return false

        val lastDismiss = prefs.getLong(PREF_RATING_LAST_DISMISS_TIME, 0L)
        if (lastDismiss > 0 && (currentTimeMs - lastDismiss) < COOLDOWN_AFTER_DISMISS_MS) return false

        val lastPromptTime = prefs.getLong(PREF_LAST_PROMPT_SHOWN_TIME, 0L)
        if (lastPromptTime > 0 && (currentTimeMs - lastPromptTime) < GLOBAL_PROMPT_INTERVAL_MS) return false

        val launches = prefs.getInt(PREF_APP_LAUNCH_COUNT, 0)
        val readCount = prefs.getInt(PREF_TOTAL_READ_COUNT, 0)
        return launches >= MIN_LAUNCHES_FOR_RATING && readCount >= MIN_ARTICLES_READ_FOR_RATING
    }

    fun canShowSharePrompt(
        prefs: SharedPreferences,
        hasShownThisSession: Boolean,
        currentTimeMs: Long = System.currentTimeMillis()
    ): Boolean {
        if (hasShownThisSession) return false
        if (prefs.getBoolean(PREF_SHARE_COMPLETED, false)) return false

        val dismissCount = prefs.getInt(PREF_SHARE_DISMISS_COUNT, 0)
        if (dismissCount >= MAX_DISMISSALS) return false

        val lastDismiss = prefs.getLong(PREF_SHARE_LAST_DISMISS_TIME, 0L)
        if (lastDismiss > 0 && (currentTimeMs - lastDismiss) < COOLDOWN_AFTER_DISMISS_MS) return false

        val lastPromptTime = prefs.getLong(PREF_LAST_PROMPT_SHOWN_TIME, 0L)
        if (lastPromptTime > 0 && (currentTimeMs - lastPromptTime) < GLOBAL_PROMPT_INTERVAL_MS) return false

        val launches = prefs.getInt(PREF_APP_LAUNCH_COUNT, 0)
        val bookmarks = prefs.getInt(PREF_BOOKMARKS_COUNT, 0)
        return launches >= MIN_LAUNCHES_FOR_SHARE && bookmarks >= MIN_BOOKMARKS_FOR_SHARE
    }

    fun canShowVersionUpdateNotice(
        prefs: SharedPreferences,
        currentVersionCode: Int,
        hasShownThisSession: Boolean
    ): Boolean {
        if (hasShownThisSession) return false
        val lastSeenVersion = prefs.getInt(PREF_LAST_SEEN_VERSION_CODE, 0)
        val launches = prefs.getInt(PREF_APP_LAUNCH_COUNT, 0)
        // Only trigger update notice if user upgraded from an older version and has explored the app
        return lastSeenVersion in 1 until currentVersionCode && launches >= 2
    }

    fun markPromptShown(prefs: SharedPreferences, currentTimeMs: Long = System.currentTimeMillis()) {
        prefs.edit().putLong(PREF_LAST_PROMPT_SHOWN_TIME, currentTimeMs).apply()
    }

    fun markVersionNoticeSeen(prefs: SharedPreferences, currentVersionCode: Int) {
        prefs.edit().putInt(PREF_LAST_SEEN_VERSION_CODE, currentVersionCode).apply()
    }

    fun recordDismissal(
        prefs: SharedPreferences,
        type: SuggestionType,
        neverAskAgain: Boolean = false,
        currentTimeMs: Long = System.currentTimeMillis()
    ) {
        val editor = prefs.edit()
        when (type) {
            SuggestionType.RATING -> {
                val count = if (neverAskAgain) MAX_DISMISSALS else prefs.getInt(PREF_RATING_DISMISS_COUNT, 0) + 1
                editor.putInt(PREF_RATING_DISMISS_COUNT, count)
                editor.putLong(PREF_RATING_LAST_DISMISS_TIME, currentTimeMs)
                if (count >= MAX_DISMISSALS) {
                    editor.putBoolean(PREF_RATING_COMPLETED, true)
                }
            }
            SuggestionType.SHARE -> {
                val count = if (neverAskAgain) MAX_DISMISSALS else prefs.getInt(PREF_SHARE_DISMISS_COUNT, 0) + 1
                editor.putInt(PREF_SHARE_DISMISS_COUNT, count)
                editor.putLong(PREF_SHARE_LAST_DISMISS_TIME, currentTimeMs)
                if (count >= MAX_DISMISSALS) {
                    editor.putBoolean(PREF_SHARE_COMPLETED, true)
                }
            }
            SuggestionType.UPDATE -> {
                // Update notices are acknowledged once per version
            }
            SuggestionType.NONE -> {}
        }
        editor.putLong(PREF_LAST_PROMPT_SHOWN_TIME, currentTimeMs)
        editor.apply()
    }

    fun recordCompleted(prefs: SharedPreferences, type: SuggestionType) {
        val editor = prefs.edit()
        when (type) {
            SuggestionType.RATING -> editor.putBoolean(PREF_RATING_COMPLETED, true)
            SuggestionType.SHARE -> editor.putBoolean(PREF_SHARE_COMPLETED, true)
            SuggestionType.UPDATE -> {}
            SuggestionType.NONE -> {}
        }
        editor.apply()
    }
}
