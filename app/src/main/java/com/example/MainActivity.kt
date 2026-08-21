package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Article
import com.example.ui.NewsViewModel
import com.example.ui.components.AudioPlayerBar
import com.example.ui.components.BottomNavigationBar
import com.example.ui.components.NavTab
import com.example.ui.components.SmartRatingDialog
import com.example.ui.screens.ArticleDetailScreen
import com.example.ui.screens.AudioDigestScreen
import com.example.ui.screens.BookmarksScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NewsRadarScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.components.GlobalLoadingOverlay
import com.example.ui.navigation.SiftNavGraph
import com.example.ui.navigation.Screen
import androidx.navigation.compose.rememberNavController
import com.example.ui.theme.SiftTheme

class MainActivity : ComponentActivity() {

    private val viewModel: NewsViewModel by viewModels()

    override fun attachBaseContext(newBase: android.content.Context) {
        val prefs = newBase.getSharedPreferences("sift_prefs", android.content.Context.MODE_PRIVATE)
        val langCode = prefs.getString("app_language", "en") ?: "en"
        val wrappedContext = com.example.util.LanguageHelper.wrapContext(newBase, langCode)
        super.attachBaseContext(wrappedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Apply saved language locale on startup
        com.example.util.LanguageHelper.setAppLanguage(this, viewModel.appLanguage.value)

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
            SiftTheme(themeMode = themeMode) {
                val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsStateWithLifecycle()

                if (!isOnboardingCompleted) {
                    OnboardingScreen(
                        onFinish = { selectedMode, apiKey, country, langCode ->
                            viewModel.setProcessingMode(selectedMode)
                            if (apiKey.isNotBlank()) {
                                viewModel.setCustomApiKey(apiKey)
                            }
                            viewModel.setSelectedCountry(country)
                            viewModel.setAppLanguage(this@MainActivity, langCode)
                            viewModel.completeOnboarding()
                        }
                    )
                } else {
                    SiftApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun SiftApp(viewModel: NewsViewModel) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(NavTab.HOME) }
    var selectedArticleForDetail by remember { mutableStateOf<Article?>(null) }

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
    val showRatingPrompt by viewModel.showRatingPrompt.collectAsStateWithLifecycle()
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

    val isGlobalLoading by viewModel.isGlobalLoading.collectAsStateWithLifecycle()
    val globalLoadingMessage by viewModel.globalLoadingMessage.collectAsStateWithLifecycle()
    val isGeminiOperation by viewModel.isGeminiOperation.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val isTablet = LocalConfiguration.current.screenWidthDp >= 720
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    if (showRatingPrompt) {
        SmartRatingDialog(
            onDismiss = { viewModel.dismissRatingPrompt() },
            onRateSubmitted = { stars -> viewModel.submitAppRating(stars) },
            onShareApp = { viewModel.triggerShareApp() }
        )
    }

    // Global Overlay for Gemini API & Firestore operation feedback
    GlobalLoadingOverlay(
        isVisible = isGlobalLoading,
        message = globalLoadingMessage,
        isGeminiOperation = isGeminiOperation
    )

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (!isTablet) {
                BottomNavigationBar(
                    currentTab = currentTab,
                    onTabSelected = { tab ->
                        currentTab = tab
                        when (tab) {
                            NavTab.HOME -> navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } }
                            NavTab.RADAR -> navController.navigate(Screen.Radar.route) { launchSingleTop = true }
                            NavTab.AUDIO -> navController.navigate(Screen.Audio.route) { launchSingleTop = true }
                            NavTab.BOOKMARKS -> navController.navigate(Screen.Bookmarks.route) { launchSingleTop = true }
                            NavTab.SETTINGS -> navController.navigate(Screen.Settings.route) { launchSingleTop = true }
                        }
                    },
                    appLanguage = appLanguage
                )
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isTablet) {
                NavigationRail(
                    modifier = Modifier.testTag("tablet_navigation_rail"),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Sift Shield Logo",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .padding(10.dp)
                                .size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    NavTab.entries.forEach { tab ->
                        val selected = tab == currentTab
                        NavigationRailItem(
                            selected = selected,
                            onClick = {
                                currentTab = tab
                                when (tab) {
                                    NavTab.HOME -> navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } }
                                    NavTab.RADAR -> navController.navigate(Screen.Radar.route) { launchSingleTop = true }
                                    NavTab.AUDIO -> navController.navigate(Screen.Audio.route) { launchSingleTop = true }
                                    NavTab.BOOKMARKS -> navController.navigate(Screen.Bookmarks.route) { launchSingleTop = true }
                                    NavTab.SETTINGS -> navController.navigate(Screen.Settings.route) { launchSingleTop = true }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            modifier = Modifier.testTag("nav_rail_item_${tab.route}")
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                SiftNavGraph(
                    navController = navController,
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState,
                    scope = scope,
                    modifier = Modifier.fillMaxSize()
                )

                AudioPlayerBar(
                    playbackState = playbackState,
                    currentTitle = currentAudioTitle,
                    speechSpeed = speechSpeed,
                    onPlayPauseToggle = {
                        if (playbackState == com.example.service.PlaybackState.PLAYING) {
                            viewModel.audioManager.pause()
                        } else {
                            viewModel.audioManager.resume()
                        }
                    },
                    onSpeedChange = { speed -> viewModel.audioManager.setSpeed(speed) },
                    onClose = { viewModel.audioManager.stop() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = if (isTablet) 16.dp else 0.dp)
                )
            }
        }
    }
}

@Composable
fun TabletWelcomePlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.widthIn(max = 420.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Sift",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(18.dp)
                        .size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Welcome to Sift News",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Secure AI-powered news summarization, anti-clickbait & 0 ads. Select any article on the left column to begin reading summaries and SNR analysis.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}
