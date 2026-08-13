package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.KeywordTrapEntity
import com.example.data.model.Article
import com.example.data.model.ProcessingMode
import com.example.data.model.TimeBudget
import com.example.data.repository.NewsRepository
import com.example.data.repository.StorageStats
import com.example.service.AudioDigestManager
import com.example.service.AudioQueueItem
import com.example.service.BackgroundSyncInfo
import com.example.service.BackgroundSyncManager
import com.example.service.FirebaseService
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NewsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("sift_prefs", Context.MODE_PRIVATE)

    private val repository = NewsRepository(application)
    val audioManager = AudioDigestManager(application)
    val firebaseService = FirebaseService.getInstance(application)
    private val syncManager = BackgroundSyncManager(application)
    val syncInfo: StateFlow<BackgroundSyncInfo> = syncManager.syncInfo

    private val _themeMode = MutableStateFlow(
        ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _appLanguage = MutableStateFlow(prefs.getString("app_language", "en") ?: "en")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(prefs.getBoolean("onboarding_completed", false))
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _articlesReadCount = MutableStateFlow(0)
    val articlesReadCount: StateFlow<Int> = _articlesReadCount.asStateFlow()

    private val _showRatingPrompt = MutableStateFlow(false)
    val showRatingPrompt: StateFlow<Boolean> = _showRatingPrompt.asStateFlow()

    private val _hasActionedRatingPrompt = MutableStateFlow(false)

    private val _selectedTimeBudget = MutableStateFlow(TimeBudget.DEEP_DIVE)
    val selectedTimeBudget: StateFlow<TimeBudget> = _selectedTimeBudget.asStateFlow()

    private val _selectedCategoryTags = MutableStateFlow<Set<String>>(setOf("ALL"))
    val selectedCategoryTags: StateFlow<Set<String>> = _selectedCategoryTags.asStateFlow()

    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedProcessingMode = MutableStateFlow(
        ProcessingMode.valueOf(prefs.getString("processing_mode", ProcessingMode.SUPER_FAST.name) ?: ProcessingMode.SUPER_FAST.name)
    )
    val selectedProcessingMode: StateFlow<ProcessingMode> = _selectedProcessingMode.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(
        prefs.getStringSet("search_history", setOf("AI", "Tech", "Science", "Inflation", "Quantum"))?.toList() ?: listOf("AI", "Tech", "Science", "Inflation", "Quantum")
    )
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    private val _onlyHighSnr = MutableStateFlow(false)
    val onlyHighSnr: StateFlow<Boolean> = _onlyHighSnr.asStateFlow()

    private val _customApiKey = MutableStateFlow(prefs.getString("custom_api_key", "") ?: "")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _selectedCountry = MutableStateFlow(
        prefs.getString("selected_country", "Global / International (English)") ?: "Global / International (English)"
    )
    val selectedCountry: StateFlow<String> = _selectedCountry.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _feedError = MutableStateFlow<String?>(null)
    val feedError: StateFlow<String?> = _feedError.asStateFlow()

    private val _storageStats = MutableStateFlow(StorageStats())
    val storageStats: StateFlow<StorageStats> = _storageStats.asStateFlow()

    private val _clearCacheMessage = MutableStateFlow<String?>(null)
    val clearCacheMessage: StateFlow<String?> = _clearCacheMessage.asStateFlow()

    private val _autoClearRetentionDays = MutableStateFlow(prefs.getInt("auto_clear_retention_days", 30))
    val autoClearRetentionDays: StateFlow<Int> = _autoClearRetentionDays.asStateFlow()

    private val _readerFontSize = MutableStateFlow(prefs.getInt("reader_font_size", 18))
    val readerFontSize: StateFlow<Int> = _readerFontSize.asStateFlow()

    private val _readerTypeface = MutableStateFlow(prefs.getString("reader_typeface", "Serif") ?: "Serif")
    val readerTypeface: StateFlow<String> = _readerTypeface.asStateFlow()

    
    private val _preCacheForOffline = MutableStateFlow(prefs.getBoolean("pre_cache_offline", false))
    val preCacheForOffline: StateFlow<Boolean> = _preCacheForOffline.asStateFlow()

    private val _briefingHour = MutableStateFlow(prefs.getInt("briefing_hour", 8))
    val briefingHour: StateFlow<Int> = _briefingHour.asStateFlow()

    private val _briefingMinute = MutableStateFlow(prefs.getInt("briefing_minute", 0))
    val briefingMinute: StateFlow<Int> = _briefingMinute.asStateFlow()

    fun setPreCacheForOffline(enabled: Boolean) {
        _preCacheForOffline.value = enabled
        prefs.edit().putBoolean("pre_cache_offline", enabled).apply()
        if (enabled) {
            viewModelScope.launch {
                repository.preCacheBookmarkedArticles()
            }
        }
    }

    fun setBriefingSchedule(hour: Int, minute: Int) {
        _briefingHour.value = hour
        _briefingMinute.value = minute
        prefs.edit().putInt("briefing_hour", hour).putInt("briefing_minute", minute).apply()
        com.example.service.NewsSyncWorker.schedulePeriodicSync(getApplication())
    }

private val _isLowPowerMode = MutableStateFlow(prefs.getBoolean("is_low_power_mode", false))
    val isLowPowerMode: StateFlow<Boolean> = _isLowPowerMode.asStateFlow()

    fun setReaderFontSize(size: Int) {
        _readerFontSize.value = size
        prefs.edit().putInt("reader_font_size", size).apply()
    }

    fun setReaderTypeface(font: String) {
        _readerTypeface.value = font
        prefs.edit().putString("reader_typeface", font).apply()
    }

    fun setAutoClearRetentionDays(days: Int) {
        _autoClearRetentionDays.value = days
        prefs.edit().putInt("auto_clear_retention_days", days).apply()
        if (days > 0) {
            viewModelScope.launch {
                repository.autoClearOldArticlesAndCache(days)
                loadStorageStats()
            }
        }
    }

    fun setLowPowerMode(enabled: Boolean) {
        _isLowPowerMode.value = enabled
        prefs.edit().putBoolean("is_low_power_mode", enabled).apply()
        if (enabled) {
            androidx.work.WorkManager.getInstance(getApplication()).cancelUniqueWork(com.example.service.NewsSyncWorker.PERIODIC_WORK_NAME)
        } else {
            com.example.service.NewsSyncWorker.schedulePeriodicSync(getApplication())
        }
    }

    private val _intelligenceBriefingSummary = MutableStateFlow(
        prefs.getString("latest_intelligence_briefing", "Compiling daily top-priority intelligence briefing from high-SNR sources...") ?: "Daily intelligence briefing ready."
    )
    val intelligenceBriefingSummary: StateFlow<String> = _intelligenceBriefingSummary.asStateFlow()

    init {
        loadStorageStats()
    }

    val keywordTraps: StateFlow<List<KeywordTrapEntity>> = repository.allKeywordTraps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedArticles: StateFlow<List<Article>> = repository.bookmarkedArticles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val readArticles: StateFlow<List<Article>> = repository.readArticles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearReadingHistory() {
        viewModelScope.launch {
            repository.clearReadingHistory()
        }
    }

    private val _dismissedArticleIds = MutableStateFlow<Set<String>>(
        prefs.getStringSet("dismissed_article_ids", emptySet()) ?: emptySet()
    )
    val dismissedArticleIds: StateFlow<Set<String>> = _dismissedArticleIds.asStateFlow()

    val filteredArticles: StateFlow<List<Article>> = combine(
        repository.allArticles,
        _selectedTimeBudget,
        _selectedCategoryTags,
        _searchQuery,
        combine(_onlyHighSnr, _dismissedArticleIds) { highSnr, dismissed -> Pair(highSnr, dismissed) }
    ) { articles, budget, categoryTags, query, extraPair ->
        val (highSnrOnly, dismissedIds) = extraPair
        var list = articles.filter { !dismissedIds.contains(it.id) }

        // Multi-tag Category Filter
        if (!categoryTags.contains("ALL") && categoryTags.isNotEmpty()) {
            list = list.filter { article ->
                categoryTags.any { tag -> article.category.equals(tag, ignoreCase = true) }
            }
        }

        // High SNR filter (>80% SNR)
        if (highSnrOnly) {
            list = list.filter { it.snrScore >= 0.80f }
        }

        // Search query
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.title.lowercase().contains(q) ||
                        it.originalTitle.lowercase().contains(q) ||
                        it.publisher.lowercase().contains(q) ||
                        it.category.lowercase().contains(q) ||
                        it.biasCategory.lowercase().contains(q) ||
                        it.summaryBullets.any { b -> b.lowercase().contains(q) }
            }
        }

        // Time Budget Filter
        when (budget) {
            TimeBudget.TWO_MINUTES -> list.take(3)
            TimeBudget.FIVE_MINUTES -> list.take(7)
            TimeBudget.DEEP_DIVE -> list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        syncManager.startMonitoring()
        com.example.service.IntelligenceBriefingWorker.scheduleDailyBriefing(application)
        refreshFeed()
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
        firebaseService.logEvent("theme_changed", mapOf("mode" to mode.name))
    }

    fun completeOnboarding() {
        _isOnboardingCompleted.value = true
        prefs.edit().putBoolean("onboarding_completed", true).apply()
        firebaseService.logEvent("onboarding_completed")
    }

    fun setAppLanguage(context: android.content.Context, langCode: String) {
        if (_appLanguage.value != langCode) {
            _appLanguage.value = langCode
            prefs.edit().putString("app_language", langCode).apply()
            com.example.util.LanguageHelper.setAppLanguage(context, langCode)
            (context as? android.app.Activity)?.recreate()
        }
    }

    fun setSelectedCountry(country: String) {
        _selectedCountry.value = country
        prefs.edit().putString("selected_country", country).apply()
        firebaseService.logEvent("country_selected", mapOf("country" to country))
    }

    fun dismissRatingPrompt() {
        _showRatingPrompt.value = false
        _hasActionedRatingPrompt.value = true
    }

    fun dismissArticle(articleId: String) {
        val current = _dismissedArticleIds.value.toMutableSet()
        current.add(articleId)
        val capped = if (current.size > 100) current.toList().takeLast(100).toSet() else current
        _dismissedArticleIds.value = capped
        prefs.edit().putStringSet("dismissed_article_ids", capped).apply()
        firebaseService.logEvent("article_dismissed", mapOf("article_id" to articleId))
    }

    fun undoDismissArticle(articleId: String) {
        val current = _dismissedArticleIds.value.toMutableSet()
        current.remove(articleId)
        _dismissedArticleIds.value = current
        prefs.edit().putStringSet("dismissed_article_ids", current).apply()
        firebaseService.logEvent("article_dismiss_undone", mapOf("article_id" to articleId))
    }

    fun submitAppRating(stars: Int) {
        firebaseService.logAppRating(stars)
        _showRatingPrompt.value = false
        _hasActionedRatingPrompt.value = true
    }

    fun triggerShareApp() {
        firebaseService.logAppShare()
    }

    fun setTimeBudget(budget: TimeBudget) {
        _selectedTimeBudget.value = budget
    }

    fun setCategory(category: String) {
        toggleCategoryTag(category)
    }

    fun toggleCategoryTag(tag: String) {
        val current = _selectedCategoryTags.value
        if (tag.equals("ALL", ignoreCase = true)) {
            _selectedCategoryTags.value = setOf("ALL")
            _selectedCategory.value = "ALL"
        } else {
            val newSet = if (current.contains(tag)) {
                val updated = current - tag
                if (updated.isEmpty()) setOf("ALL") else updated
            } else {
                (current - "ALL") + tag
            }
            _selectedCategoryTags.value = newSet
            _selectedCategory.value = newSet.firstOrNull() ?: "ALL"
        }
        firebaseService.logEvent("category_toggled", mapOf("tag" to tag))
    }

    fun setProcessingMode(mode: ProcessingMode) {
        _selectedProcessingMode.value = mode
        prefs.edit().putString("processing_mode", mode.name).apply()
        refreshFeed()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.trim().length >= 2) {
            saveSearchQueryToHistory(query.trim())
        }
    }

    fun saveSearchQueryToHistory(query: String) {
        val currentList = _searchHistory.value.toMutableList()
        currentList.remove(query)
        currentList.add(0, query)
        val updatedList = currentList.take(10)
        _searchHistory.value = updatedList
        prefs.edit().putStringSet("search_history", updatedList.toSet()).apply()
    }

    fun clearSearchHistory() {
        _searchHistory.value = emptyList()
        prefs.edit().remove("search_history").apply()
    }

    fun syncBookmarksWithCloud() {
        viewModelScope.launch {
            repository.firebaseSyncManager.restoreBookmarksFromCloud()
            val currentBookmarks = bookmarkedArticles.value
            firebaseService.syncBookmarksToCloud(currentBookmarks)
        }
    }

    fun toggleOnlyHighSnr() {
        _onlyHighSnr.value = !_onlyHighSnr.value
    }

    fun setCustomApiKey(key: String) {
        _customApiKey.value = key
        prefs.edit().putString("custom_api_key", key).apply()
    }

    fun refreshFeed() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _feedError.value = null
            try {
                repository.refreshNewsFeed(
                    processingMode = _selectedProcessingMode.value,
                    customApiKey = _customApiKey.value
                )
            } catch (e: Exception) {
                _feedError.value = e.localizedMessage ?: e.message ?: "An unexpected error occurred during refresh"
            } finally {
                _isRefreshing.value = false
                loadStorageStats()
            }
        }
    }

    fun clearFeedError() {
        _feedError.value = null
    }

    fun loadStorageStats() {
        viewModelScope.launch {
            _storageStats.value = repository.getStorageStats()
        }
    }

    fun clearOfflineCache(keepBookmarks: Boolean = true) {
        viewModelScope.launch {
            val result = repository.clearOfflineArticlesCache(keepBookmarks)
            val freedKb = (result.freedBytes / 1024).coerceAtLeast(120)
            _clearCacheMessage.value = "Cleared ${result.articlesCleared} offline articles (~${freedKb} KB storage freed)"
            _storageStats.value = repository.getStorageStats()
            firebaseService.logEvent("cache_cleared", mapOf("cleared_count" to result.articlesCleared))
        }
    }

    fun dismissClearCacheMessage() {
        _clearCacheMessage.value = null
    }

    fun toggleBookmark(articleId: String, current: Boolean) {
        viewModelScope.launch {
            repository.toggleBookmark(articleId, current)
            firebaseService.logBookmarkToggle(articleId, !current)
            syncBookmarksWithCloud()
            if (!current && !_hasActionedRatingPrompt.value) {
                _showRatingPrompt.value = true
            }
        }
    }

    fun markAsRead(articleId: String) {
        viewModelScope.launch {
            repository.markAsRead(articleId)
            _articlesReadCount.value += 1
            firebaseService.logArticleView(articleId, _selectedCategory.value, 0.85f)
            if (_articlesReadCount.value >= 2 && !_hasActionedRatingPrompt.value) {
                _showRatingPrompt.value = true
            }
        }
    }

    fun addKeywordTrap(keyword: String, category: String = "ALL") {
        viewModelScope.launch {
            repository.addKeywordTrap(keyword, category)
        }
    }

    fun deleteKeywordTrap(id: Int) {
        viewModelScope.launch {
            repository.deleteKeywordTrap(id)
        }
    }

    fun toggleKeywordTrap(id: Int, isActive: Boolean) {
        viewModelScope.launch {
            repository.toggleKeywordTrap(id, isActive)
        }
    }

    fun playArticleAudio(article: Article) {
        audioManager.playArticle(
            title = article.title,
            publisher = article.publisher,
            bullets = article.summaryBullets
        )
    }

    fun play3MinuteMorningDigest() {
        val currentFeed = filteredArticles.value.take(5)
        val queue = currentFeed.map { article ->
            AudioQueueItem(
                title = article.title,
                bullets = article.summaryBullets,
                publisher = article.publisher
            )
        }
        audioManager.playDigestQueue(queue)
    }

    fun sendDailyDigestPushNotification() {
        val currentFeed = filteredArticles.value.take(5)
        if (currentFeed.isNotEmpty()) {
            val notificationManager = com.example.service.RadarNotificationManager(getApplication())
            notificationManager.sendDailyDigestNotification(currentFeed)
        }
    }

    override fun onCleared() {
        super.onCleared()
        syncManager.stopMonitoring()
        audioManager.shutdown()
    }
}
