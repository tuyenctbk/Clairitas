package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProcessingMode
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val title: String,
    val subtitle: String,
    val description: String,
    val badge: String,
    val icon: ImageVector,
    val primaryColor: Color
)

@OptIn(ExperimentalAnimationApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: (ProcessingMode, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedMode by remember { mutableStateOf(ProcessingMode.SUPER_FAST) }
    var apiKeyInput by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf("Global / International (English)") }
    var onboardingLanguageCode by remember { mutableStateOf("en") }
    val coroutineScope = rememberCoroutineScope()

    val pages = listOf(
        OnboardingPageData(
            badge = "ANTI-CLICKBAIT & HIGH SIGNAL",
            title = "Purify Your News Feed • High SNR Scoring",
            subtitle = "Experience truly clean, high-signal journalism",
            description = "Sift uses AI to measure information signal quality (SNR Score > 80%). Eliminates 100% of clickbait headlines, banner ads, and repetitive noise.",
            icon = Icons.Default.Shield,
            primaryColor = MaterialTheme.colorScheme.primary
        ),
        OnboardingPageData(
            badge = "SAVE TIME & RADAR",
            title = "3-Minute Audio Digest & Keyword Radar",
            subtitle = "Listen to top curated stories on the go",
            description = "Save hours of daily web browsing. Play the Audio Digest for voice-narrated summaries and set custom keyword traps to catch critical market movements instantly.",
            icon = Icons.Default.Headphones,
            primaryColor = Color(0xFF10B981) // Emerald Green
        ),
        OnboardingPageData(
            badge = "COUNTRY & REGION",
            title = "Select Your Nation & Region",
            subtitle = "Tailor news feeds for your region of interest",
            description = "Choose your primary country or region to curate customized global and local news feeds.",
            icon = Icons.Default.Radar,
            primaryColor = Color(0xFFF59E0B) // Amber/Yellow
        ),
        OnboardingPageData(
            badge = "AI INTELLIGENCE",
            title = "Personalize AI Analysis Mode",
            subtitle = "Choose your preferred processing mode",
            description = "Select your AI processing algorithm. Sift supports free offline local processing and high-end cloud intelligence via your personal Gemini key.",
            icon = Icons.Default.AutoAwesome,
            primaryColor = MaterialTheme.colorScheme.primary
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val currentPage = pagerState.currentPage
    val currentPageData = pages[currentPage]

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val isTablet = maxWidth >= 720.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isTablet) 32.dp else 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { pageIndex ->
                val pageData = pages[pageIndex]
                if (isTablet) {
                    // Adaptive Two-Column Layout for Tablets & Foldables
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                OnboardingArtCanvas(
                                    pageIndex = pageIndex,
                                    tintColor = pageData.primaryColor,
                                    modifier = Modifier.size(280.dp)
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = "SIFT INTELLIGENCE",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = pageData.primaryColor,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                )
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .weight(1.2f)
                                .fillMaxHeight()
                                .padding(16.dp)
                                .testTag("onboarding_tablet_panel")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(28.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.Center
                            ) {
                                OnboardingDetails(
                                    pageData = pageData,
                                    currentPage = pageIndex,
                                    selectedMode = selectedMode,
                                    onModeSelected = { selectedMode = it },
                                    apiKeyInput = apiKeyInput,
                                    onApiKeyChanged = { apiKeyInput = it },
                                    selectedCountry = selectedCountry,
                                    onCountrySelected = { selectedCountry = it },
                                    onboardingLanguageCode = onboardingLanguageCode,
                                    onLanguageCodeChanged = { lang ->
                                        onboardingLanguageCode = lang
                                        com.example.util.LanguageHelper.setAppLanguage(context, lang)
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Mobile Standard One-Column Flow
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        OnboardingArtCanvas(
                            pageIndex = pageIndex,
                            tintColor = pageData.primaryColor,
                            modifier = Modifier
                                .size(190.dp)
                                .padding(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(20.dp)
                                    .fillMaxWidth()
                            ) {
                                OnboardingDetails(
                                    pageData = pageData,
                                    currentPage = pageIndex,
                                    selectedMode = selectedMode,
                                    onModeSelected = { selectedMode = it },
                                    apiKeyInput = apiKeyInput,
                                    onApiKeyChanged = { apiKeyInput = it },
                                    selectedCountry = selectedCountry,
                                    onCountrySelected = { selectedCountry = it },
                                    onboardingLanguageCode = onboardingLanguageCode,
                                    onLanguageCodeChanged = { lang ->
                                        onboardingLanguageCode = lang
                                        com.example.util.LanguageHelper.setAppLanguage(context, lang)
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OnboardingNavigationRow(
                currentPage = currentPage,
                totalPages = pages.size,
                primaryColor = currentPageData.primaryColor,
                onPrev = {
                    if (currentPage > 0) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(currentPage - 1)
                        }
                    }
                },
                onNext = {
                    if (currentPage < pages.size - 1) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(currentPage + 1)
                        }
                    } else {
                        onFinish(selectedMode, apiKeyInput, selectedCountry, onboardingLanguageCode)
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun OnboardingDetails(
    pageData: OnboardingPageData,
    currentPage: Int,
    selectedMode: ProcessingMode,
    onModeSelected: (ProcessingMode) -> Unit,
    apiKeyInput: String,
    onApiKeyChanged: (String) -> Unit,
    selectedCountry: String,
    onCountrySelected: (String) -> Unit,
    onboardingLanguageCode: String,
    onLanguageCodeChanged: (String) -> Unit
) {
    Column {
        // Upper Badge Indicator
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = pageData.primaryColor.copy(alpha = 0.15f),
            modifier = Modifier.testTag("onboarding_badge")
        ) {
            Text(
                text = pageData.badge,
                style = MaterialTheme.typography.labelSmall,
                color = pageData.primaryColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Large display text
        Text(
            text = pageData.title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 28.sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.testTag("onboarding_title")
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = pageData.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = pageData.primaryColor
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = pageData.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Render Country Selection on slide 2 (index 2)
        if (currentPage == 2) {
            Column {
                Text(
                    text = "Select Country / Region:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                val countries = listOf(
                    "Global / International (English)",
                    "United States (US)",
                    "United Kingdom (UK)",
                    "Europe (EU)",
                    "Japan (JP)",
                    "Germany (DE)",
                    "France (FR)",
                    "South Korea (KR)",
                    "India (IN)",
                    "China / East Asia (CN/APAC)",
                    "Singapore / SE Asia (SG/APAC)",
                    "Vietnam (VN)",
                    "Latin America (LATAM)",
                    "Australia / New Zealand (ANZ)",
                    "Canada (CA)",
                    "Brazil (BR)"
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    countries.forEach { country ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedCountry == country) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .border(
                                    width = 1.dp,
                                    color = if (selectedCountry == country) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onCountrySelected(country) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = country,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (selectedCountry == country) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Select Default App Language:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                val onboardingLangs = listOf(
                    "English" to "en",
                    "Vietnamese" to "vi",
                    "Spanish" to "es",
                    "French" to "fr",
                    "German" to "de",
                    "Japanese" to "ja",
                    "Korean" to "ko",
                    "Chinese (Simp)" to "zh-CN",
                    "Chinese (Trad)" to "zh-TW",
                    "Portuguese" to "pt",
                    "Italian" to "it",
                    "Hindi" to "hi",
                    "Russian" to "ru",
                    "Arabic" to "ar",
                    "Dutch" to "nl",
                    "Indonesian" to "id",
                    "Thai" to "th"
                )

                @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    onboardingLangs.forEach { (langName, langCode) ->
                        val isLangSelected = onboardingLanguageCode == langCode
                        androidx.compose.material3.InputChip(
                            selected = isLangSelected,
                            onClick = {
                                onLanguageCodeChanged(langCode)
                            },
                            label = { Text(text = langName, fontSize = 12.sp) },
                            leadingIcon = if (isLangSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null
                        )
                    }
                }
            }
        }

        // Render interactive config fields only on slide 3 (index 3)
        if (currentPage == 3) {
            Column {
                Text(
                    text = "Select AI Processing Mode:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                // TextRank Option
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedMode == ProcessingMode.SUPER_FAST) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = if (selectedMode == ProcessingMode.SUPER_FAST) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onModeSelected(ProcessingMode.SUPER_FAST) }
                        .testTag("onboarding_opt_textrank")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Fast NLP",
                            tint = if (selectedMode == ProcessingMode.SUPER_FAST) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Super-Fast TextRank",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Lightweight local NLP (0.01s, 100% offline, free)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Cloud BYOK Gemini Option
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedMode == ProcessingMode.BYOK_CLOUD) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = if (selectedMode == ProcessingMode.BYOK_CLOUD) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onModeSelected(ProcessingMode.BYOK_CLOUD) }
                        .testTag("onboarding_opt_gemini")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Cloud AI",
                            tint = if (selectedMode == ProcessingMode.BYOK_CLOUD) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "BYOK Gemini Cloud Intelligence",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Advanced Gemini 1.5 Pro deep analysis & anti-clickbait",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = selectedMode == ProcessingMode.BYOK_CLOUD) {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        Text(
                            text = "Enter your Gemini API Key (Optional - Can add later):",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        androidx.compose.material3.OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = onApiKeyChanged,
                            placeholder = { Text("AIzaSy...", fontSize = 12.sp) },
                            singleLine = true,
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("onboarding_api_key_field")
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingNavigationRow(
    currentPage: Int,
    totalPages: Int,
    primaryColor: Color,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back/Skip Text Button
        Box(modifier = Modifier.width(72.dp)) {
            if (currentPage > 0) {
                Text(
                    text = "Back",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { onPrev() }
                        .padding(8.dp)
                        .testTag("onboarding_back_btn")
                )
            }
        }

        // Dot Page Indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            (0 until totalPages).forEach { index ->
                val active = index == currentPage
                Box(
                    modifier = Modifier
                        .size(if (active) 18.dp else 8.dp, 8.dp)
                        .clip(CircleShape)
                        .background(if (active) primaryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )
            }
        }

        // Next/Finish Action Button
        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .width(100.dp)
                .testTag("onboarding_next_btn")
        ) {
            Text(
                text = if (currentPage == totalPages - 1) "Start" else "Next",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Custom Art Canvas draws fluid shapes, radar wave echoes, sound waves or orbital grids
 * to make the onboarding visually breathtaking, responsive, and lightweight.
 */
@Composable
fun OnboardingArtCanvas(
    pageIndex: Int,
    tintColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "onboarding_pulse")
    val pulseProg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    val waveProg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "soundwave"
    )

    val radarRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar"
    )

    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val baseRadius = size.width.coerceAtMost(size.height) * 0.35f

        when (pageIndex) {
            0 -> {
                // Slide 1: Secure Shield / Signal SNR pulse circles
                // Draw 3 expanding signal radar waves
                for (i in 0..2) {
                    val radius = baseRadius * 1.5f * ((pulseProg + i / 3f) % 1f)
                    val alpha = 1f - ((pulseProg + i / 3f) % 1f)
                    drawCircle(
                        color = tintColor,
                        radius = radius,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 3.dp.toPx()),
                        alpha = alpha * 0.4f
                    )
                }

                // Centered Shield base glow
                drawCircle(
                    color = tintColor.copy(alpha = 0.12f),
                    radius = baseRadius,
                    center = Offset(centerX, centerY)
                )

                // Outer boundary ring
                drawCircle(
                    color = tintColor,
                    radius = baseRadius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 2.dp.toPx())
                )

                // Diamond icon simulation inside the center
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(centerX, centerY - baseRadius * 0.5f)
                    lineTo(centerX + baseRadius * 0.4f, centerY - baseRadius * 0.2f)
                    lineTo(centerX + baseRadius * 0.4f, centerY + baseRadius * 0.2f)
                    lineTo(centerX, centerY + baseRadius * 0.6f)
                    lineTo(centerX - baseRadius * 0.4f, centerY + baseRadius * 0.2f)
                    lineTo(centerX - baseRadius * 0.4f, centerY - baseRadius * 0.2f)
                    close()
                }
                drawPath(path = path, color = tintColor, alpha = 0.85f)
            }
            1 -> {
                // Slide 2: Audio Digest - 6 pulsing soundwaves bars
                drawCircle(
                    color = tintColor.copy(alpha = 0.1f),
                    radius = baseRadius * 1.2f,
                    center = Offset(centerX, centerY)
                )

                val barCount = 7
                val spacing = 20.dp.toPx()
                val startX = centerX - (barCount / 2f) * spacing

                for (i in 0 until barCount) {
                    val barX = startX + i * spacing
                    // Stagger heights
                    val offsetVal = (i - barCount / 2f) * (i - barCount / 2f) * 12f
                    val heightModifier = (waveProg + offsetVal) % 70f + 30f
                    val barHeight = baseRadius * (heightModifier / 100f) * 1.2f

                    drawLine(
                        color = tintColor,
                        start = Offset(barX, centerY - barHeight / 2),
                        end = Offset(barX, centerY + barHeight / 2),
                        strokeWidth = 6.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
            2 -> {
                // Slide 3: News Radar sweeps
                // concentric rings
                drawCircle(
                    color = tintColor.copy(alpha = 0.08f),
                    radius = baseRadius * 1.4f,
                    center = Offset(centerX, centerY)
                )
                drawCircle(
                    color = tintColor,
                    radius = baseRadius * 1.3f,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 1.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f)))
                )
                drawCircle(
                    color = tintColor,
                    radius = baseRadius * 0.8f,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 1.5f.dp.toPx())
                )
                drawCircle(
                    color = tintColor,
                    radius = baseRadius * 0.3f,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 2.dp.toPx())
                )

                // Grid lines (vertical/horizontal)
                drawLine(
                    color = tintColor.copy(alpha = 0.4f),
                    start = Offset(centerX - baseRadius * 1.4f, centerY),
                    end = Offset(centerX + baseRadius * 1.4f, centerY),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = tintColor.copy(alpha = 0.4f),
                    start = Offset(centerX, centerY - baseRadius * 1.4f),
                    end = Offset(centerX, centerY + baseRadius * 1.4f),
                    strokeWidth = 1.dp.toPx()
                )

                // Sweeping radar arc vector line
                val angleRad = Math.toRadians(radarRotation.toDouble())
                val sweepEndX = centerX + baseRadius * 1.3f * Math.cos(angleRad).toFloat()
                val sweepEndY = centerY + baseRadius * 1.3f * Math.sin(angleRad).toFloat()
                drawLine(
                    color = tintColor,
                    start = Offset(centerX, centerY),
                    end = Offset(sweepEndX, sweepEndY),
                    strokeWidth = 2.5f.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
            else -> {
                // Slide 4: AI sparkles & deep grid background
                drawCircle(
                    color = tintColor.copy(alpha = 0.05f),
                    radius = baseRadius * 1.5f,
                    center = Offset(centerX, centerY)
                )

                // Draw rotating sparkling orbital lines
                val angleRad1 = Math.toRadians((radarRotation * 0.5f).toDouble())
                val radiusX1 = baseRadius * 1.1f * Math.cos(angleRad1).toFloat()
                val radiusY1 = baseRadius * 0.6f * Math.sin(angleRad1).toFloat()

                // Oval path
                drawCircle(
                    color = tintColor,
                    radius = baseRadius * 0.9f,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 1.5f.dp.toPx())
                )

                // Center Sparkle
                val sparkRadius = baseRadius * 0.4f
                val sparkPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(centerX, centerY - sparkRadius)
                    quadraticTo(centerX, centerY, centerX + sparkRadius, centerY)
                    quadraticTo(centerX, centerY, centerX, centerY + sparkRadius)
                    quadraticTo(centerX, centerY, centerX - sparkRadius, centerY)
                    quadraticTo(centerX, centerY, centerX, centerY - sparkRadius)
                    close()
                }
                drawPath(path = sparkPath, color = tintColor)
            }
        }
    }
}
