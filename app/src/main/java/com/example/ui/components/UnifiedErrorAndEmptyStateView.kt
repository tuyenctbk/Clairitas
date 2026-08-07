package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class UnifiedStateType {
    API_FAILURE,       // rate limits, invalid API keys, server down
    EMPTY_FEED,        // no articles available at all
    EMPTY_BOOKMARKS,   // no saved bookmarks
    EMPTY_HISTORY,     // no reading history yet
    NO_RESULTS,        // search results returned empty
    NETWORK_ERROR      // offline or network timeout
}

@Composable
fun UnifiedErrorAndEmptyStateView(
    type: UnifiedStateType,
    customErrorMessage: String? = null,
    onActionClick: (() -> Unit)? = null,
    actionButtonText: String? = null,
    modifier: Modifier = Modifier
) {
    val (icon, title, defaultDesc, buttonText) = when (type) {
        UnifiedStateType.API_FAILURE -> Quadruple(
            Icons.Default.Warning,
            "AI Processing Failed",
            "Sift intelligence synthesis failed to process news feeds. This might be due to a missing or rate-limited API key.",
            actionButtonText ?: "Retry Synthesis"
        )
        UnifiedStateType.EMPTY_FEED -> Quadruple(
            Icons.Default.Inbox,
            "Your Feed is Empty",
            "No high-signal articles are available. Pull down to trigger an on-demand web scan and curate new articles.",
            actionButtonText ?: "Scan News Feeds"
        )
        UnifiedStateType.EMPTY_BOOKMARKS -> Quadruple(
            Icons.Default.BookmarkBorder,
            "No Bookmarks Saved",
            "Save important articles with key insights here for distraction-free offline access later.",
            actionButtonText ?: "Browse Articles"
        )
        UnifiedStateType.EMPTY_HISTORY -> Quadruple(
            Icons.Default.History,
            "Reading History is Empty",
            "Articles you read will show up here to help track your content digestion metrics.",
            null
        )
        UnifiedStateType.NO_RESULTS -> Quadruple(
            Icons.Default.Search,
            "No Matching Articles Found",
            "Try refining your search query or choosing a different category chip filter.",
            actionButtonText ?: "Clear Filter"
        )
        UnifiedStateType.NETWORK_ERROR -> Quadruple(
            Icons.Default.CloudOff,
            "Network Connection Offline",
            "Sift cannot connect to external news nodes. Viewing locally cached Room database content in offline mode.",
            actionButtonText ?: "Try Offline Scan"
        )
    }

    val description = customErrorMessage ?: defaultDesc

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                // Icon within soft container
                Surface(
                    shape = CircleShape,
                    color = when (type) {
                        UnifiedStateType.API_FAILURE, UnifiedStateType.NETWORK_ERROR -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    },
                    modifier = Modifier.size(60.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = when (type) {
                                UnifiedStateType.API_FAILURE, UnifiedStateType.NETWORK_ERROR -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // Optional Action Button
                if (onActionClick != null && buttonText != null) {
                    Spacer(modifier = Modifier.height(20.dp))
                    if (type == UnifiedStateType.API_FAILURE || type == UnifiedStateType.NETWORK_ERROR) {
                        OutlinedButton(
                            onClick = onActionClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.testTag("unified_retry_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retry",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = buttonText, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onActionClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("unified_action_button")
                        ) {
                            Text(text = buttonText, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
