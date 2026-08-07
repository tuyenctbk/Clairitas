package com.example.ui.screens

import android.content.Intent
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.data.model.Article
import com.example.ui.components.SnrBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    article: Article?,
    onBack: () -> Unit,
    onBookmarkToggle: (Article) -> Unit,
    onPlayAudio: (Article) -> Unit,
    customApiKey: String = "",
    modifier: Modifier = Modifier
) {
    if (article == null) return

    val context = androidx.compose.ui.platform.LocalContext.current
    var showInAppBrowser by remember { mutableStateOf(false) }
    var isReaderMode by remember { mutableStateOf(true) }
    var readerTheme by remember { mutableStateOf("Light") } // "Light", "Sepia", "Dark", "Sage"
    var readerFontSizeSp by remember { mutableStateOf(17) } // 14, 17, 20, 23

    // 60+ Language translation states
    var selectedTranslateLang by remember { mutableStateOf<Pair<String, String>?>(null) } // Name, Code
    var isTranslating by remember { mutableStateOf(false) }
    var translatedTitle by remember { mutableStateOf<String?>(null) }
    var translatedBullets by remember { mutableStateOf<List<String>?>(null) }
    var translatedContent by remember { mutableStateOf<String?>(null) }
    var showLanguageBottomSheet by remember { mutableStateOf(false) }

    // LaunchedEffect to translate article whenever language selection changes
    androidx.compose.runtime.LaunchedEffect(selectedTranslateLang) {
        if (selectedTranslateLang != null) {
            isTranslating = true
            try {
                val result = com.example.data.repository.GeminiSummarizer.translateArticle(
                    title = article.title,
                    bullets = article.summaryBullets,
                    content = article.fullContent,
                    targetLanguage = selectedTranslateLang!!.first,
                    userCustomApiKey = customApiKey
                )
                translatedTitle = result.title
                translatedBullets = result.bullets
                translatedContent = result.content
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isTranslating = false
            }
        } else {
            translatedTitle = null
            translatedBullets = null
            translatedContent = null
        }
    }

    val currentPlayArticle = if (translatedTitle != null && translatedBullets != null && translatedContent != null) {
        article.copy(
            title = translatedTitle!!,
            summaryBullets = translatedBullets!!,
            fullContent = translatedContent!!
        )
    } else {
        article
    }

    val shareArticleSummary = {
        val shareText = "Sift News Curation & Intelligence:\n" +
                "📌 ${translatedTitle ?: article.title}\n\n" +
                "⚡ 3 Core Key Takeaways:\n" +
                (translatedBullets ?: article.summaryBullets).joinToString("\n") { "• $it" } + "\n\n" +
                "🔗 Source (${article.publisher}): ${article.sourceUrl}"
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Sift Summary")
        context.startActivity(shareIntent)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = article.publisher,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (isReaderMode) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "Reader Mode",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { isReaderMode = !isReaderMode },
                        modifier = Modifier.testTag("reader_mode_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = "Toggle Reader Mode",
                            tint = if (isReaderMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = {
                        readerFontSizeSp = when (readerFontSizeSp) {
                            14 -> 17
                            17 -> 20
                            20 -> 23
                            else -> 14
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = "Font Size",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = shareArticleSummary,
                        modifier = Modifier.testTag("share_article_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Summary",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { onPlayAudio(article) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Listen to Audio",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { onBookmarkToggle(article) }) {
                        Icon(
                            imageVector = if (article.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save Article",
                            tint = if (article.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showInAppBrowser = true }) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "Open Original Source",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 720.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
            // SNR Signal Scanner Header Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "SNR Scanner",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SNR & Bias Scanner",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        SnrBadge(snrScore = article.snrScore, biasCategory = article.biasCategory)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Classification: ${article.biasCategory}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { article.snrScore },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "AI Translation (60+ Languages):",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            val quickLanguages = listOf(
                "Original" to "en",
                "Vietnamese" to "vi",
                "Spanish" to "es",
                "French" to "fr",
                "German" to "de",
                "Japanese" to "ja",
                "Chinese" to "zh-CN"
            )
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickLanguages.size) { index ->
                    val lang = quickLanguages[index]
                    val isSelected = (selectedTranslateLang == null && lang.first == "Original") || 
                                     (selectedTranslateLang != null && selectedTranslateLang!!.first == lang.first)
                    androidx.compose.material3.InputChip(
                        selected = isSelected,
                        onClick = {
                            if (lang.first == "Original") {
                                selectedTranslateLang = null
                            } else {
                                selectedTranslateLang = lang
                            }
                        },
                        label = { Text(text = lang.first, fontSize = 12.sp) },
                        leadingIcon = if (isSelected) {
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
                item {
                    androidx.compose.material3.InputChip(
                        selected = selectedTranslateLang != null && !quickLanguages.map { it.first }.contains(selectedTranslateLang!!.first),
                        onClick = { showLanguageBottomSheet = true },
                        label = { Text(text = if (selectedTranslateLang != null && !quickLanguages.map { it.first }.contains(selectedTranslateLang!!.first)) selectedTranslateLang!!.first else "More...", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "More",
                                modifier = Modifier.size(16.dp)
                              )
                        }
                    )
                }
            }

            if (isTranslating) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Translating to ${selectedTranslateLang?.first ?: ""}... Please wait",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Honest Title
            Text(
                text = translatedTitle ?: article.title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    lineHeight = 30.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            // Clickbait Original Title Callout
            if (article.originalTitle != article.title) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "⚠️ Raw Clickbait Headline:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "\"${article.originalTitle}\"",
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3-Bullet TL;DR Summary Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚡ Executive Summary (3-Bullet TL;DR):",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onPlayAudio(currentPlayArticle) }
                                .testTag("listen_tts_summary_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Listen to TTS",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Listen TTS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    (translatedBullets ?: article.summaryBullets).forEach { bullet ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Bullet",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = bullet,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = 22.sp,
                                    fontSize = 14.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { shareArticleSummary() }
                                .testTag("share_summary_action_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Share Key Takeaways",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main Featured Image - Stripped out in Reader View for distraction-free reading
            if (!isReaderMode && article.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = article.imageUrl,
                    contentDescription = "Featured Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Distraction-Free Reader Mode Theme & Font Slider Controls
            if (isReaderMode) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        // Row 1: Theme Palette
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "Reader Theme",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Reader Theme:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("Light", "Sepia", "Dark", "Sage").forEach { themeName ->
                                    val isSelected = readerTheme == themeName
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = when (themeName) {
                                            "Sepia" -> Color(0xFFFBF0D9)
                                            "Dark" -> Color(0xFF18181B)
                                            "Sage" -> Color(0xFFEAF4EE)
                                            else -> Color(0xFFFFFFFF)
                                        },
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { readerTheme = themeName }
                                            .testTag("reader_theme_$themeName")
                                    ) {
                                        Text(
                                            text = themeName,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = when (themeName) {
                                                "Sepia" -> Color(0xFF3E2723)
                                                "Dark" -> Color(0xFFE4E4E7)
                                                "Sage" -> Color(0xFF1B3B2B)
                                                else -> Color(0xFF1F2937)
                                            },
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Row 2: Font Size Slider
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatSize,
                                contentDescription = "Font Size",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Font Size (${readerFontSizeSp} sp):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "A-",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Slider(
                                value = readerFontSizeSp.toFloat(),
                                onValueChange = { readerFontSizeSp = it.toInt() },
                                valueRange = 12f..26f,
                                steps = 6,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reader_font_size_slider")
                            )
                            Text(
                                text = "A+",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Distraction-Free Medium Style Reader Body Content
            val readerBgColor = if (isReaderMode) {
                when (readerTheme) {
                    "Sepia" -> Color(0xFFFBF0D9)
                    "Dark" -> Color(0xFF18181B)
                    "Sage" -> Color(0xFFEAF4EE)
                    else -> MaterialTheme.colorScheme.surface
                }
            } else {
                MaterialTheme.colorScheme.surface
            }

            val readerTextColor = if (isReaderMode) {
                when (readerTheme) {
                    "Sepia" -> Color(0xFF3E2723)
                    "Dark" -> Color(0xFFE4E4E7)
                    "Sage" -> Color(0xFF1B3B2B)
                    else -> MaterialTheme.colorScheme.onSurface
                }
            } else {
                MaterialTheme.colorScheme.onBackground
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = readerBgColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (isReaderMode) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isReaderMode) "📖 Distraction-Free Reader View (Ads Stripped)" else "Full Content:",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isReaderMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${article.timeEstimateMinutes} min read",
                            style = MaterialTheme.typography.labelSmall,
                            color = readerTextColor.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val rawContent = translatedContent ?: article.fullContent
                    val displayedContent = if (isReaderMode) {
                        rawContent.lines().filter { line ->
                            val l = line.lowercase()
                            !l.contains("advertisement") &&
                            !l.contains("sponsored") &&
                            !l.contains("subscribe now") &&
                            !l.contains("click here") &&
                            !l.contains("promo code") &&
                            !l.contains("ad banner")
                        }.joinToString("\n")
                    } else {
                        rawContent
                    }

                    Text(
                        text = displayedContent,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = readerFontSizeSp.sp,
                            lineHeight = (readerFontSizeSp + 10).sp,
                            letterSpacing = 0.2.sp
                        ),
                        color = readerTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Open Original Publisher Button (Legal & Copyright compliance)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showInAppBrowser = true }
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Copyright & Publisher Attribution",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "View full original interface on ${article.publisher} via the In-App Browser.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Web",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Open original article on ${article.publisher}",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    // Modal Sheet for In-App Browser
    if (showInAppBrowser) {
        ModalBottomSheet(
            onDismissRequest = { showInAppBrowser = false },
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "In-App Browser: ${article.publisher}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showInAppBrowser = false }) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            webViewClient = WebViewClient()
                            settings.javaScriptEnabled = true
                            loadUrl(article.sourceUrl)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    if (showLanguageBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLanguageBottomSheet = false },
            modifier = Modifier.fillMaxHeight(0.7f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Translate to 60+ Languages",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    val allLanguages = listOf(
                        "Original" to "en",
                        "Vietnamese" to "vi", "Spanish" to "es", "French" to "fr", "German" to "de",
                        "Japanese" to "ja", "Chinese (Simp)" to "zh-CN", "Chinese (Trad)" to "zh-TW", "Korean" to "ko",
                        "Arabic" to "ar", "Hindi" to "hi", "Russian" to "ru", "Portuguese" to "pt", "Italian" to "it",
                        "Bengali" to "bn", "Dutch" to "nl", "Swedish" to "sv", "Polish" to "pl", "Turkish" to "tr",
                        "Indonesian" to "id", "Thai" to "th", "Malay" to "ms", "Tagalog" to "tl", "Tamil" to "ta",
                        "Telugu" to "te", "Czech" to "cs", "Danish" to "da", "Finnish" to "fi", "Greek" to "el",
                        "Hebrew" to "he", "Hungarian" to "hu", "Norwegian" to "no", "Romanian" to "ro", "Slovak" to "sk",
                        "Ukrainian" to "uk", "Afrikaans" to "af", "Albanian" to "sq", "Amharic" to "am", "Armenian" to "hy",
                        "Azerbaijani" to "az", "Basque" to "eu", "Belarusian" to "be", "Bosnian" to "bs", "Bulgarian" to "bg",
                        "Catalan" to "ca", "Croatian" to "hr", "Esperanto" to "eo", "Estonian" to "et", "Galician" to "gl",
                        "Georgian" to "ka", "Gujarati" to "gu", "Haitian Creole" to "ht", "Icelandic" to "is", "Irish" to "ga",
                        "Kannada" to "kn", "Latvian" to "lv", "Lithuanian" to "lt", "Macedonian" to "mk", "Maltese" to "mt",
                        "Persian" to "fa", "Serbian" to "sr", "Slovenian" to "sl", "Swahili" to "sw", "Urdu" to "ur",
                        "Welsh" to "cy", "Yiddish" to "yi"
                    )
                    items(allLanguages.size) { index ->
                        val lang = allLanguages[index]
                        val isSelected = (selectedTranslateLang == null && lang.first == "Original") || 
                                         (selectedTranslateLang != null && selectedTranslateLang!!.first == lang.first)

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (lang.first == "Original") {
                                        selectedTranslateLang = null
                                    } else {
                                        selectedTranslateLang = lang
                                    }
                                    showLanguageBottomSheet = false
                                }
                        ) {
                            Text(
                                text = lang.first,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
}
