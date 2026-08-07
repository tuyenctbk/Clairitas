package com.example.util

/**
 * Utility for calculating estimated reading time based on standard words-per-minute (WPM).
 */
object ReadingTimeCalculator {
    private const val STANDARD_WPM = 200

    fun calculateReadingTimeMinutes(text: String): Int {
        if (text.isBlank()) return 1
        val words = text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
        return ((words + STANDARD_WPM - 1) / STANDARD_WPM).coerceAtLeast(1)
    }

    fun formatReadingTime(fullContent: String, summaryBullets: List<String> = emptyList()): String {
        val combinedText = if (fullContent.isNotBlank()) {
            fullContent + " " + summaryBullets.joinToString(" ")
        } else {
            summaryBullets.joinToString(" ")
        }
        val mins = calculateReadingTimeMinutes(combinedText)
        return "$mins min read"
    }
}
