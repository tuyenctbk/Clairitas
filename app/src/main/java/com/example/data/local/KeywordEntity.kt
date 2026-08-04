package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a user-defined keyword alert for news tracking and offline access.
 *
 * @param id Unique identifier for the keyword alert.
 * @param keyword The keyword string entered by the user to trigger alerts.
 * @param alertFrequency User preference for alert notifications (e.g., "INSTANT", "HOURLY", "DAILY").
 * @param lastCheckedTimestamp Timestamp (in milliseconds) when the keyword alert was last processed or checked.
 * @param categoryFilter Category scope for filtering news (default "ALL").
 * @param isActive Whether the alert is currently active.
 * @param createdAt Timestamp when the keyword alert was created.
 * @param matchCount Number of articles matched by this keyword alert.
 */
@Entity(tableName = "keyword_alerts")
data class KeywordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val keyword: String,
    val alertFrequency: String = "INSTANT",
    val lastCheckedTimestamp: Long = System.currentTimeMillis(),
    val categoryFilter: String = "ALL",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val matchCount: Int = 0
)
