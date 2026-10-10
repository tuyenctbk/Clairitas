package com.example.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.data.model.Article
import com.example.service.PlaybackState
import com.example.ui.NewsViewModel
import com.example.ui.components.NavTab
import com.example.ui.screens.ArticleDetailScreen
import com.example.ui.screens.AudioDigestScreen
import com.example.ui.screens.BookmarksScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NewsRadarScreen
import com.example.ui.screens.SettingsScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object ArticleDetail : Screen("article/{articleId}") {
        fun createRoute(articleId: String) = "article/$articleId"
    }
    object Radar : Screen("radar")
    object Audio : Screen("audio")
    object Bookmarks : Screen("bookmarks")
    object Settings : Screen("settings")
}

@Composable
fun SiftNavGraph(
    navController: NavHostController,
    viewModel: NewsViewModel,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val filteredArticles by viewModel.filteredArticles.collectAsStateWithLifecycle()
    val bookmarkedArticles by viewModel.bookmarkedArticles.collectAsStateWithLifecycle()
    val keywordTraps by viewModel.keywordTraps.collectAsStateWithLifecycle()
    val timeBudget by viewModel.selectedTimeBudget.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedCategoryTags by viewModel.selectedCategoryTags.collectAsStateWithLifecycle()
    val processingMode by viewModel.selectedProcessingMode.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchHistory by viewModel.searchHistory.collectAsStateWithLifecycle()
    val onlyHighSnr by viewModel.onlyHighSnr.collectAsStateWithLifecycle()
    val customApiKey by viewModel.customApiKey.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val syncInfo by viewModel.syncInfo.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val storageStats by viewModel.storageStats.collectAsStateWithLifecycle()
    val clearCacheMessage by viewModel.clearCacheMessage.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val selectedCountry by viewModel.selectedCountry.collectAsStateWithLifecycle()
    val intelligenceBriefingSummary by viewModel.intelligenceBriefingSummary.collectAsStateWithLifecycle()
    val autoClearRetentionDays by viewModel.autoClearRetentionDays.collectAsStateWithLifecycle()
    val isLowPowerMode by viewModel.isLowPowerMode.collectAsStateWithLifecycle()
    val articlesReadCount by viewModel.articlesReadCount.collectAsStateWithLifecycle()
    val preCacheForOffline by viewModel.preCacheForOffline.collectAsStateWithLifecycle()
    val briefingHour by viewModel.briefingHour.collectAsStateWithLifecycle()
    val briefingMinute by viewModel.briefingMinute.collectAsStateWithLifecycle()
    val readerFontSize by viewModel.readerFontSize.collectAsStateWithLifecycle()
    val readerTypeface by viewModel.readerTypeface.collectAsStateWithLifecycle()
    val readArticles by viewModel.readArticles.collectAsStateWithLifecycle()
    val feedError by viewModel.feedError.collectAsStateWithLifecycle()

    val playbackState by viewModel.audioManager.playbackState.collectAsStateWithLifecycle()
    val currentAudioTitle by viewModel.audioManager.currentTitle.collectAsStateWithLifecycle()
    val speechSpeed by viewModel.audioManager.currentSpeechSpeed.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        // 1. Home Screen Destination
        composable(
            route = Screen.Home.route,
            enterTransition = { fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(200)) }
        ) {
            HomeScreen(
                feedError = feedError,
                onClearError = { viewModel.clearFeedError() },
                articles = filteredArticles,
                timeBudget = timeBudget,
                selectedCategory = selectedCategory,
                selectedCategoryTags = selectedCategoryTags,
                processingMode = processingMode,
                searchQuery = searchQuery,
                searchHistory = searchHistory,
                onClearSearchHistory = { viewModel.clearSearchHistory() },
                isRefreshing = isRefreshing,
                onlyHighSnr = onlyHighSnr,
                syncInfo = syncInfo,
                intelligenceBriefingSummary = intelligenceBriefingSummary,
                onTimeBudgetChanged = { viewModel.setTimeBudget(it) },
                onCategoryChanged = { viewModel.setCategory(it) },
                onToggleCategoryTag = { viewModel.toggleCategoryTag(it) },
                onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                onToggleOnlyHighSnr = { viewModel.toggleOnlyHighSnr() },
                onRefresh = { viewModel.refreshFeed(isManualPullToRefresh = true) },
                onArticleClick = { article ->
                    viewModel.markAsRead(article.id)
                    navController.navigate(Screen.ArticleDetail.createRoute(article.id))
                },
                onBookmarkToggle = { article -> viewModel.toggleBookmark(article.id, article.isBookmarked) },
                onPlayAudio = { article -> viewModel.playArticleAudio(article) },
                onDismissArticle = { article ->
                    viewModel.dismissArticle(article.id)
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = "Archived: " + article.title,
                            actionLabel = "Undo",
                            duration = SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.undoDismissArticle(article.id)
                        }
                    }
                },
                onPlayMorningDigest = { viewModel.play3MinuteMorningDigest() }
            )
        }

        // 2. Individual Article View Screen Destination
        composable(
            route = Screen.ArticleDetail.route,
            arguments = listOf(navArgument("articleId") { type = NavType.StringType }),
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(250)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(250)) }
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getString("articleId") ?: ""
            val article = filteredArticles.find { it.id == articleId } 
                ?: bookmarkedArticles.find { it.id == articleId }
                ?: readArticles.find { it.id == articleId }

            ArticleDetailScreen(
                article = article,
                onBack = { navController.popBackStack() },
                onBookmarkToggle = { art -> viewModel.toggleBookmark(art.id, art.isBookmarked) },
                onPlayAudio = { art -> viewModel.playArticleAudio(art) },
                customApiKey = customApiKey,
                speechSpeed = speechSpeed,
                onSpeedChange = { viewModel.audioManager.setSpeed(it) },
                initialFontSize = readerFontSize,
                initialTypeface = readerTypeface,
                onFontSizeChanged = { viewModel.setReaderFontSize(it) },
                onTypefaceChanged = { viewModel.setReaderTypeface(it) }
            )
        }

        // 3. News Radar Destination
        composable(route = Screen.Radar.route) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                Box(modifier = Modifier.widthIn(max = 680.dp).fillMaxHeight()) {
                    NewsRadarScreen(
                        keywordTraps = keywordTraps,
                        allArticles = filteredArticles,
                        onAddTrap = { keyword -> viewModel.addKeywordTrap(keyword) },
                        onDeleteTrap = { id -> viewModel.deleteKeywordTrap(id) },
                        onToggleTrap = { id, active -> viewModel.toggleKeywordTrap(id, active) },
                        onScanNow = { viewModel.refreshFeed() },
                        onArticleClick = { article ->
                            viewModel.markAsRead(article.id)
                            navController.navigate(Screen.ArticleDetail.createRoute(article.id))
                        },
                        onBookmarkToggle = { article -> viewModel.toggleBookmark(article.id, article.isBookmarked) },
                        onPlayAudio = { article -> viewModel.playArticleAudio(article) },
                        onTestAlert = { viewModel.sendDailyDigestPushNotification() }
                    )
                }
            }
        }

        // 4. Audio Digest Destination
        composable(route = Screen.Audio.route) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                Box(modifier = Modifier.widthIn(max = 680.dp).fillMaxHeight()) {
                    AudioDigestScreen(
                        articles = filteredArticles,
                        playbackState = playbackState,
                        currentAudioTitle = currentAudioTitle,
                        speechSpeed = speechSpeed,
                        onPlay3MinuteDigest = { viewModel.play3MinuteMorningDigest() },
                        onPlayArticle = { article -> viewModel.playArticleAudio(article) },
                        onPlayPauseToggle = {
                            if (playbackState == PlaybackState.PLAYING) {
                                viewModel.audioManager.pause()
                            } else {
                                viewModel.audioManager.resume()
                            }
                        },
                        onSpeedChange = { speed -> viewModel.audioManager.setSpeed(speed) },
                        onTestNotification = { viewModel.sendDailyDigestPushNotification() }
                    )
                }
            }
        }

        // 5. Bookmarks Destination
        composable(route = Screen.Bookmarks.route) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                Box(modifier = Modifier.widthIn(max = 680.dp).fillMaxHeight()) {
                    BookmarksScreen(
                        bookmarkedArticles = bookmarkedArticles,
                        onArticleClick = { article ->
                            viewModel.markAsRead(article.id)
                            navController.navigate(Screen.ArticleDetail.createRoute(article.id))
                        },
                        onBookmarkToggle = { article -> viewModel.toggleBookmark(article.id, article.isBookmarked) },
                        onPlayAudio = { article -> viewModel.playArticleAudio(article) }
                    )
                }
            }
        }

        // 6. Settings Destination
        composable(route = Screen.Settings.route) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                Box(modifier = Modifier.widthIn(max = 680.dp).fillMaxHeight()) {
                    SettingsScreen(
                        currentProcessingMode = processingMode,
                        customApiKey = customApiKey,
                        currentThemeMode = themeMode,
                        currentLanguageCode = appLanguage,
                        selectedCountry = selectedCountry,
                        onLanguageCodeChanged = { viewModel.setAppLanguage(context, it) },
                        onCountryChanged = { viewModel.setSelectedCountry(it) },
                        syncInfo = syncInfo,
                        storageStats = storageStats,
                        clearCacheMessage = clearCacheMessage,
                        isLowPowerMode = isLowPowerMode,
                        autoClearRetentionDays = autoClearRetentionDays,
                        articlesReadCount = articlesReadCount,
                        onProcessingModeChanged = { viewModel.setProcessingMode(it) },
                        onCustomApiKeySaved = { viewModel.setCustomApiKey(it) },
                        onThemeModeChanged = { viewModel.setThemeMode(it) },
                        onTriggerSync = { viewModel.refreshFeed() },
                        onSendTestDailyDigest = { viewModel.sendDailyDigestPushNotification() },
                        onClearOfflineCache = { keepBookmarks -> viewModel.clearOfflineCache(keepBookmarks) },
                        onDismissClearCacheMsg = { viewModel.dismissClearCacheMessage() },
                        onLowPowerModeChanged = { viewModel.setLowPowerMode(it) },
                        onAutoClearRetentionDaysChanged = { viewModel.setAutoClearRetentionDays(it) },
                        preCacheForOffline = preCacheForOffline,
                        briefingHour = briefingHour,
                        briefingMinute = briefingMinute,
                        readerFontSize = readerFontSize,
                        readerTypeface = readerTypeface,
                        readArticles = readArticles,
                        currentUser = currentUser,
                        onSignInGoogle = { viewModel.signInWithGoogle(context) { _, _ -> } },
                        onSignOut = { viewModel.signOut() },
                        onArticleClick = { article ->
                            viewModel.markAsRead(article.id)
                            navController.navigate(Screen.ArticleDetail.createRoute(article.id))
                        },
                        onClearReadingHistory = { viewModel.clearReadingHistory() },
                        onReaderFontSizeChanged = { viewModel.setReaderFontSize(it) },
                        onReaderTypefaceChanged = { viewModel.setReaderTypeface(it) },
                        onPreCacheForOfflineChanged = { viewModel.setPreCacheForOffline(it) },
                        onBriefingScheduleChanged = { h, m -> viewModel.setBriefingSchedule(h, m) }
                    )
                }
            }
        }
    }
}
