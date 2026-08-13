package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.EmptyStateView

@Composable
fun ActivityEmptyStateScreen(
    onStartScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyStateView(
        icon = Icons.AutoMirrored.Filled.DirectionsRun,
        title = "No Activity Logs Collected",
        description = "No recent reading sessions, background sync events, or keyword trap logs recorded yet. Start reading articles or running intelligence scans to log activity.",
        badgeText = "0 Activity Logs",
        actionButtonText = "Run Activity Scan",
        onActionButtonClick = onStartScan,
        iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
        iconTint = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}

@Composable
fun SosEmptyStateScreen(
    onTestTrigger: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyStateView(
        icon = Icons.Default.Shield,
        title = "SOS Emergency Monitor Clear",
        description = "No emergency SOS alerts or critical trap violations triggered. All noise reduction safeguards and high-priority push streams are operating normally.",
        badgeText = "All Safeguards Normal",
        actionButtonText = "Test SOS Alert Stream",
        onActionButtonClick = onTestTrigger,
        iconContainerColor = Color(0xFFDC2626).copy(alpha = 0.15f),
        iconTint = Color(0xFFDC2626),
        modifier = modifier
    )
}

@Composable
fun GeoFenceEmptyStateScreen(
    onConfigureRegion: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyStateView(
        icon = Icons.Default.LocationOn,
        title = "Geo-Fence Radar Inactive",
        description = "No regional geo-fence boundaries or location-specific news filters currently active. Select a specific country or regional curation source to activate geo-fence radar.",
        badgeText = "No Boundaries Set",
        actionButtonText = "Configure Geo-Fence Region",
        onActionButtonClick = onConfigureRegion,
        iconContainerColor = Color(0xFF059669).copy(alpha = 0.15f),
        iconTint = Color(0xFF059669),
        modifier = modifier
    )
}

/**
 * Empty States Container Screen allowing users to view Empty State feedback for Activity, SOS, and Geo-fence tabs.
 */
@Composable
fun EmptyStatesTabContainerScreen(
    onStartScan: () -> Unit,
    onTestSos: () -> Unit,
    onConfigureRegion: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Activity Log", "SOS Monitor", "Geo-Fence Radar")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 16.dp,
            modifier = Modifier.testTag("empty_states_tab_row")
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    modifier = Modifier.testTag("empty_state_tab_$index")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (selectedTab) {
                0 -> ActivityEmptyStateScreen(onStartScan = onStartScan)
                1 -> SosEmptyStateScreen(onTestTrigger = onTestSos)
                2 -> GeoFenceEmptyStateScreen(onConfigureRegion = onConfigureRegion)
            }
        }
    }
}
