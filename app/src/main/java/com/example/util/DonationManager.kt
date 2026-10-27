package com.example.util

import android.content.SharedPreferences

/**
 * Manages donation triggers, supporter status, and satisfaction safeguard logic.
 *
 * Rules:
 * - Satisfaction Safeguard: NEVER show donation banners/dialogs to new or casual users.
 *   Requires >= 3 operations, >= 3 launches, >= 2 days since install for home banner.
 *   Requires >= 7 operations, >= 2 days since install, 30-day cooldown for auto-prompt dialog.
 * - Permanent suppression: Any valid donation marks user as an Active Supporter,
 *   which permanently disables all donation banners & auto prompts.
 */
object DonationManager {
    const val PREF_FIRST_INSTALL_TIME = "donation_first_install_time"
    const val PREF_IS_ACTIVE_SUPPORTER = "donation_is_active_supporter"
    const val PREF_SUPPORTER_TIER_ID = "donation_supporter_tier_id"
    const val PREF_SUPPORTER_SINCE = "donation_supporter_since"
    const val PREF_SUCCESSFUL_OPERATIONS = "donation_successful_operations"
    const val PREF_LAST_BANNER_DISMISSAL = "donation_last_banner_dismissal"
    const val PREF_LAST_AUTO_PROMPT = "donation_last_auto_prompt"
    const val PREF_BANNER_DISMISSED_COUNT = "donation_banner_dismissed_count"

    const val PAYPAL_BASE_URL = "https://paypal.me/IndependentNewsApp"
    const val KO_FI_URL = "https://ko-fi.com/independentnews"

    // Cooldown intervals
    const val BANNER_COOLDOWN_DAYS = 14
    const val AUTO_PROMPT_COOLDOWN_DAYS = 30
    const val MIN_DAYS_SINCE_INSTALL = 2
    const val MIN_OPERATIONS_FOR_BANNER = 3
    const val MIN_LAUNCHES_FOR_BANNER = 3
    const val MIN_OPERATIONS_FOR_AUTO_PROMPT = 7

    fun initializeFirstInstall(prefs: SharedPreferences) {
        if (!prefs.contains(PREF_FIRST_INSTALL_TIME)) {
            prefs.edit().putLong(PREF_FIRST_INSTALL_TIME, System.currentTimeMillis()).apply()
        }
    }

    fun isActiveSupporter(prefs: SharedPreferences): Boolean {
        return prefs.getBoolean(PREF_IS_ACTIVE_SUPPORTER, false)
    }

    fun getSupporterTier(prefs: SharedPreferences): String? {
        return prefs.getString(PREF_SUPPORTER_TIER_ID, null)
    }

    fun setSupporterStatus(prefs: SharedPreferences, active: Boolean, tierId: String? = null) {
        val editor = prefs.edit()
            .putBoolean(PREF_IS_ACTIVE_SUPPORTER, active)
        if (active) {
            editor.putString(PREF_SUPPORTER_TIER_ID, tierId ?: "tier_1")
            editor.putLong(PREF_SUPPORTER_SINCE, System.currentTimeMillis())
        }
        editor.apply()
    }

    fun recordSuccessfulOperation(prefs: SharedPreferences): Int {
        val current = prefs.getInt(PREF_SUCCESSFUL_OPERATIONS, 0) + 1
        prefs.edit().putInt(PREF_SUCCESSFUL_OPERATIONS, current).apply()
        return current
    }

    fun getSuccessfulOperationsCount(prefs: SharedPreferences): Int {
        return prefs.getInt(PREF_SUCCESSFUL_OPERATIONS, 0)
    }

    fun checkAndGetMilestone(prefs: SharedPreferences, currentCount: Int): Int? {
        val milestones = listOf(10, 25, 50, 100)
        for (m in milestones) {
            if (currentCount == m) {
                val key = "donation_milestone_${m}_celebrated"
                if (!prefs.getBoolean(key, false)) {
                    prefs.edit().putBoolean(key, true).apply()
                    return m
                }
            }
        }
        return null
    }

    fun canShowHomeScreenBanner(prefs: SharedPreferences): Boolean {
        if (isActiveSupporter(prefs)) return false

        // Satisfaction Safeguard:
        // 1. Min 3 successful operations
        val opCount = getSuccessfulOperationsCount(prefs)
        if (opCount < MIN_OPERATIONS_FOR_BANNER) return false

        // 2. Min 3 app launches
        val launchCount = prefs.getInt(SuggestionPolicy.PREF_APP_LAUNCH_COUNT, 0)
        if (launchCount < MIN_LAUNCHES_FOR_BANNER) return false

        // 3. Min 2 days since first installation
        val installTime = prefs.getLong(PREF_FIRST_INSTALL_TIME, System.currentTimeMillis())
        val daysSinceInstall = (System.currentTimeMillis() - installTime) / (1000L * 60 * 60 * 24)
        if (daysSinceInstall < MIN_DAYS_SINCE_INSTALL) return false

        // 4. Cooldown: Dismissal silences for 14 days
        val lastDismissal = prefs.getLong(PREF_LAST_BANNER_DISMISSAL, 0L)
        if (lastDismissal > 0L) {
            val daysSinceDismissal = (System.currentTimeMillis() - lastDismissal) / (1000L * 60 * 60 * 24)
            if (daysSinceDismissal < BANNER_COOLDOWN_DAYS) return false
        }

        return true
    }

    fun recordBannerDismissed(prefs: SharedPreferences) {
        val count = prefs.getInt(PREF_BANNER_DISMISSED_COUNT, 0) + 1
        prefs.edit()
            .putLong(PREF_LAST_BANNER_DISMISSAL, System.currentTimeMillis())
            .putInt(PREF_BANNER_DISMISSED_COUNT, count)
            .apply()
    }

    fun canShowAutoPromptDialog(prefs: SharedPreferences): Boolean {
        if (isActiveSupporter(prefs)) return false

        // 1. Min 7 successful operations
        val opCount = getSuccessfulOperationsCount(prefs)
        if (opCount < MIN_OPERATIONS_FOR_AUTO_PROMPT) return false

        // 2. Min 2 days since install
        val installTime = prefs.getLong(PREF_FIRST_INSTALL_TIME, System.currentTimeMillis())
        val daysSinceInstall = (System.currentTimeMillis() - installTime) / (1000L * 60 * 60 * 24)
        if (daysSinceInstall < MIN_DAYS_SINCE_INSTALL) return false

        // 3. 30-day cooldown between auto prompts
        val lastPrompt = prefs.getLong(PREF_LAST_AUTO_PROMPT, 0L)
        if (lastPrompt > 0L) {
            val daysSincePrompt = (System.currentTimeMillis() - lastPrompt) / (1000L * 60 * 60 * 24)
            if (daysSincePrompt < AUTO_PROMPT_COOLDOWN_DAYS) return false
        }

        return true
    }

    fun recordAutoPromptShown(prefs: SharedPreferences) {
        prefs.edit().putLong(PREF_LAST_AUTO_PROMPT, System.currentTimeMillis()).apply()
    }
}
