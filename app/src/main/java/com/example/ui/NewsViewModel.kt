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
import com.example.service.AudioDigestManager
import com.example.service.AudioQueueItem
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

    private val prefs = application.getSharedPreferences("claritas_prefs", Context.MODE_PRIVATE)

    private val repository = NewsRepository(application)
    val audioManager = AudioDigestManager(application)
    val firebaseService = FirebaseService.getInstance(application)

    private val _themeMode = MutableStateFlow(
        ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(prefs.getBoolean("onboarding_completed", false))
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _articlesReadCount = MutableStateFlow(0)
    val articlesReadCount: StateFlow<Int> = _articlesReadCount.asStateFlow()

    private val _showRatingPrompt = MutableStateFlow(false)
    val showRatingPrompt: StateFlow<Boolean> = _showRatingPrompt.asStateFlow()

    private val _hasActionedRatingPrompt = MutableStateFlow(false)

    private val _selectedTimeBudget = MutableStateFlow(TimeBudget.DEEP_DIVE)
    val selectedTimeBudget: StateFlow<TimeBudget> = _selectedTimeBudget.asStateFlow()

    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedProcessingMode = MutableStateFlow(
        ProcessingMode.valueOf(prefs.getString("processing_mode", ProcessingMode.SUPER_FAST.name) ?: ProcessingMode.SUPER_FAST.name)
    )
    val selectedProcessingMode: StateFlow<ProcessingMode> = _selectedProcessingMode.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _onlyHighSnr = MutableStateFlow(false)
    val onlyHighSnr: StateFlow<Boolean> = _onlyHighSnr.asStateFlow()

    private val _customApiKey = MutableStateFlow(prefs.getString("custom_api_key", "") ?: "")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val keywordTraps: StateFlow<List<KeywordTrapEntity>> = repository.allKeywordTraps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedArticles: StateFlow<List<Article>> = repository.bookmarkedArticles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredArticles: StateFlow<List<Article>> = combine(
        repository.allArticles,
        _selectedTimeBudget,
        _selectedCategory,
        _searchQuery,
        _onlyHighSnr
    ) { articles, budget, category, query, highSnrOnly ->
        var list = articles

        // Category filter
        if (category != "ALL") {
            list = list.filter { it.category.equals(category, ignoreCase = true) }
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

    fun dismissRatingPrompt() {
        _showRatingPrompt.value = false
        _hasActionedRatingPrompt.value = true
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
        _selectedCategory.value = category
    }

    fun setProcessingMode(mode: ProcessingMode) {
        _selectedProcessingMode.value = mode
        prefs.edit().putString("processing_mode", mode.name).apply()
        refreshFeed()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
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
            repository.refreshNewsFeed(
                processingMode = _selectedProcessingMode.value,
                customApiKey = _customApiKey.value
            )
            _isRefreshing.value = false
        }
    }

    fun toggleBookmark(articleId: String, current: Boolean) {
        viewModelScope.launch {
            repository.toggleBookmark(articleId, current)
            firebaseService.logBookmarkToggle(articleId, !current)
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

    override fun onCleared() {
        super.onCleared()
        audioManager.shutdown()
    }
}
