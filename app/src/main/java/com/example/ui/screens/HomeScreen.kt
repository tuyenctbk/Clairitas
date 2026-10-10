package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalCafe
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Article
import com.example.data.model.ProcessingMode
import com.example.data.model.TimeBudget
import com.example.service.BackgroundSyncInfo
import com.example.ui.components.ArticleCard
import com.example.ui.components.UnifiedErrorAndEmptyStateView
import com.example.ui.components.UnifiedStateType

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    articles: List<Article>,
    timeBudget: TimeBudget,
    selectedCategory: String,
    selectedCategoryTags: Set<String> = setOf("ALL"),
    processingMode: ProcessingMode,
    searchQuery: String,
    isRefreshing: Boolean,
    onlyHighSnr: Boolean = false,
    syncInfo: BackgroundSyncInfo = BackgroundSyncInfo(),
    intelligenceBriefingSummary: String = "",
    feedError: String? = null,
    onClearError: () -> Unit = {},
    onTimeBudgetChanged: (TimeBudget) -> Unit,
    onCategoryChanged: (String) -> Unit,
    onToggleCategoryTag: (String) -> Unit = {},
    onSearchQueryChanged: (String) -> Unit,
    searchHistory: List<String> = emptyList(),
    onClearSearchHistory: () -> Unit = {},
    onToggleOnlyHighSnr: () -> Unit = {},
    onRefresh: () -> Unit,
    onArticleClick: (Article) -> Unit,
    onBookmarkToggle: (Article) -> Unit,
    onPlayAudio: (Article) -> Unit,
    onDismissArticle: (Article) -> Unit = {},
    onPlayMorningDigest: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        "ALL" to stringResource(R.string.category_all),
        "Tech" to stringResource(R.string.category_tech),
        "Science" to stringResource(R.string.category_science),
        "World" to stringResource(R.string.category_global),
        "Business" to stringResource(R.string.category_business),
        "Markets" to stringResource(R.string.category_markets),
        "AI" to stringResource(R.string.category_ai),
        "RealEstate" to stringResource(R.string.category_real_estate)
    )

    val context = LocalContext.current
    var isSearchFocused by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Sift Shield Logo",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SIFT",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp,
                                    fontSize = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            // Search bar in the top app bar
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = onSearchQueryChanged,
                                placeholder = { Text(stringResource(R.string.search_hint), fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(
                                            onClick = { onSearchQueryChanged("") },
                                            modifier = Modifier
                                                .size(24.dp)
                                                .testTag("clear_search_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear search",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .onFocusChanged { isSearchFocused = it.isFocused }
                                    .testTag("search_input_field"),
                                shape = RoundedCornerShape(19.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                )
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://paypal.me/tuyenphamvn"))
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("top_bar_coffee_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalCafe,
                                contentDescription = "Buy Me a Coffee",
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        if (onPlayMorningDigest != null) {
                            IconButton(
                                onClick = onPlayMorningDigest,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("play_morning_digest_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Audio Digest",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("refresh_button")
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh feed",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )

                // Ultra-compact category filter row to minimize static view height
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                ) {
                    item {
                        FilterChip(
                            selected = onlyHighSnr,
                            onClick = onToggleOnlyHighSnr,
                            label = { Text(text = "🎯 >80%", fontSize = 10.sp) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .height(28.dp)
                                .testTag("high_snr_chip")
                        )
                    }

                    items(categories) { (code, label) ->
                        val isAllSelected = selectedCategory.equals("ALL", ignoreCase = true) ||
                                selectedCategoryTags.isEmpty() ||
                                (selectedCategoryTags.size == 1 && selectedCategoryTags.any { it.equals("ALL", ignoreCase = true) })

                        val selected = if (code.equals("ALL", ignoreCase = true)) {
                            isAllSelected
                        } else {
                            !isAllSelected && (selectedCategory.equals(code, ignoreCase = true) || selectedCategoryTags.any { it.equals(code, ignoreCase = true) })
                        }

                        FilterChip(
                            selected = selected,
                            onClick = {
                                onCategoryChanged(code)
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("category_chip_$code")
                        )
                    }
                }

                // Search History & Topic Suggestion Row
                val filteredSuggestions = remember(searchHistory, searchQuery) {
                    if (searchQuery.isBlank()) {
                        searchHistory
                    } else {
                        searchHistory.filter { it.contains(searchQuery, ignoreCase = true) }
                    }
                }

                AnimatedVisibility(visible = isSearchFocused && (filteredSuggestions.isNotEmpty() || searchHistory.isNotEmpty())) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) "History:" else "Suggestions:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            val itemsToShow = if (searchQuery.isBlank()) searchHistory else filteredSuggestions
                            items(itemsToShow) { historyItem ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .clickable { onSearchQueryChanged(historyItem) }
                                        .testTag("search_history_chip_$historyItem")
                                ) {
                                    Text(
                                        text = historyItem,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.clear_button),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .clickable { onClearSearchHistory() }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .testTag("clear_history_button")
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 720.dp)
                        .padding(horizontal = 12.dp)
                ) {
                    // Active search filter status banner
                    AnimatedVisibility(visible = searchQuery.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "Filtering by: \"$searchQuery\" (${articles.size} found)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.clear_filter),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { onSearchQueryChanged("") }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Intelligence Briefing Section Card
                    if (searchQuery.isBlank() && selectedCategoryTags.contains("ALL") && intelligenceBriefingSummary.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        androidx.compose.material3.Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = androidx.compose.material3.CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("intelligence_briefing_card")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "Intelligence Briefing",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = stringResource(R.string.daily_briefing_title),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    if (onPlayMorningDigest != null) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable(onClick = onPlayMorningDigest)
                                                .testTag("play_briefing_audio_btn")
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                    contentDescription = stringResource(R.string.listen_action),
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = stringResource(R.string.listen_action),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimary
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = intelligenceBriefingSummary,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        lineHeight = 18.sp,
                                        fontSize = 12.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Articles List or Empty State
                    if (feedError != null) {
                        UnifiedErrorAndEmptyStateView(
                            type = UnifiedStateType.API_FAILURE,
                            customErrorMessage = feedError,
                            onActionClick = {
                                onClearError()
                                onRefresh()
                            },
                            actionButtonText = stringResource(R.string.retry_action)
                        )
                    } else if (articles.isEmpty()) {
                        if (searchQuery.isNotBlank()) {
                            UnifiedErrorAndEmptyStateView(
                                type = UnifiedStateType.NO_RESULTS,
                                customErrorMessage = "No articles matched \"$searchQuery\". Try search keywords like 'AI', 'Tech', or 'Markets'.",
                                onActionClick = { onSearchQueryChanged("") },
                                actionButtonText = "Clear Search"
                            )
                        } else {
                            UnifiedErrorAndEmptyStateView(
                                type = UnifiedStateType.EMPTY_FEED,
                                onActionClick = onRefresh,
                                actionButtonText = "Trigger Curation"
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("article_feed_list")
                        ) {
                            items(articles, key = { it.id }) { article ->
                                val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = { value ->
                                        if (value == SwipeToDismissBoxValue.StartToEnd || value == SwipeToDismissBoxValue.EndToStart) {
                                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                            onDismissArticle(article)
                                            true
                                        } else {
                                            false
                                        }
                                    }
                                )

                                SwipeToDismissBox(
                                    state = dismissState,
                                    backgroundContent = {
                                        val color = if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) {
                                            MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                        } else {
                                            Color.Transparent
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(color, shape = RoundedCornerShape(12.dp))
                                                .padding(horizontal = 20.dp),
                                            contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                                                Alignment.CenterStart
                                            } else {
                                                Alignment.CenterEnd
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove article",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .animateItem()
                                        .testTag("swipe_dismiss_${article.id}")
                                ) {
                                    ArticleCard(
                                        article = article,
                                        onArticleClick = onArticleClick,
                                        onCategoryClick = onCategoryChanged,
                                        onBookmarkToggle = { onBookmarkToggle(it) },
                                        onPlayAudio = { onPlayAudio(it) }
                                    )
                                }
                            }
                            item {
                                Spacer(modifier = Modifier.height(80.dp)) // padding for bottom nav
                            }
                        }
                    }
                }
            }
        }
    }
}
