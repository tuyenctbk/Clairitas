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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Shield
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
    onFinish: (ProcessingMode, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf(ProcessingMode.SUPER_FAST) }
    var apiKeyInput by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val pages = listOf(
        OnboardingPageData(
            badge = "CHỐNG TIN RÁC & TRỤC LỢI",
            title = "Lọc Sạch Clickbait • Đo Lường Tín Hiệu SNR",
            subtitle = "Trải nghiệm nguồn tin tức tinh khiết thực sự",
            description = "Claritas ứng dụng thuật toán trí tuệ nhân tạo để đo lường tỷ lệ thông tin sạch (SNR Score > 80%). Loại bỏ 100% các tiêu đề giật gân, quảng cáo chen ngang & tin tức rác lặp lại.",
            icon = Icons.Default.Shield,
            primaryColor = MaterialTheme.colorScheme.primary
        ),
        OnboardingPageData(
            badge = "TIẾT KIỆM THỜI GIAN",
            title = "Nghe Bản Tin Tóm Tắt Morning Audio Digest",
            subtitle = "Lắng nghe tin tức chất lượng cao trong 3 phút",
            description = "Tiết kiệm hàng giờ lướt web mỗi ngày. Chỉ cần bật Audio Digest, Claritas sẽ tóm tắt & chuyển đổi giọng nói chuẩn cho 5 bài viết quan trọng nhất theo đúng tốc độ bạn muốn.",
            icon = Icons.Default.Headphones,
            primaryColor = Color(0xFF10B981) // Emerald Green
        ),
        OnboardingPageData(
            badge = "SĂN TIN CHỦ ĐỘNG",
            title = "Thiết Lập Radar Săn Tin Không Bỏ Sót",
            subtitle = "Hệ thống tự động quét & đẩy cảnh báo tức thì",
            description = "Chỉ cần cài đặt các từ khóa bạn quan tâm (như Lãi suất, Cổ phiếu FPT, Tỷ giá). Khi phát hiện luồng tin tức sạch tương thích, Radar sẽ phát chuông cảnh báo ngay lập tức.",
            icon = Icons.Default.Radar,
            primaryColor = Color(0xFFF59E0B) // Amber/Yellow
        ),
        OnboardingPageData(
            badge = "CẤU HÌNH TRẢI NGHIỆM",
            title = "Cá Nhân Hóa Trí Tuệ Nhân Tạo Phân Tích",
            subtitle = "Lựa chọn phương thức tóm tắt thông minh",
            description = "Chọn chế độ thuật toán xử lý tin phù hợp với bạn. Claritas hỗ trợ cả xử lý ngoại tuyến miễn phí lẫn điện toán đám mây cao cấp qua khóa Gemini cá nhân của riêng bạn.",
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
                                    text = "CLARITAS INTELLIGENCE",
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
                                    onApiKeyChanged = { apiKeyInput = it }
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
                                    onApiKeyChanged = { apiKeyInput = it }
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
                        onFinish(selectedMode, apiKeyInput)
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
    onApiKeyChanged: (String) -> Unit
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

        // Render interactive config fields only on slide 3 (index 3)
        if (currentPage == 3) {
            Column {
                Text(
                    text = "Lựa chọn Chế độ Thuật toán:",
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
                                text = "Thuật toán NLP cục bộ gọn nhẹ (0.01s, 100% offline, miễn phí)",
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
                                text = "Sử dụng mô hình Gemini 1.5 Pro phân tích chuyên sâu chống clickbait cực đỉnh",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = selectedMode == ProcessingMode.BYOK_CLOUD) {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        Text(
                            text = "Nhập khóa API Gemini của bạn (Tùy chọn - Có thể nhập sau):",
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
                    text = "Quay lại",
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
                text = if (currentPage == totalPages - 1) "Bắt đầu" else "Tiếp",
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
