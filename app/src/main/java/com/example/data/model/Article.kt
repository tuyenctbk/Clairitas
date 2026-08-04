package com.example.data.model

enum class ProcessingMode(val displayName: String, val description: String) {
    SUPER_FAST("Super-Fast TextRank", "Lightweight on-device algorithm (0.01s, 100% offline, $0 cost)"),
    ON_DEVICE_AI("On-Device Local AI", "Smart local NLP rule engine & signal density scanner"),
    BYOK_CLOUD("BYOK Gemini Cloud", "Gemini 3.5 Flash cloud intelligence with deep anti-clickbait rewriting")
}

enum class TimeBudget(val minutes: Int, val label: String, val subtitle: String) {
    TWO_MINUTES(2, "2 Mins", "Top 3 highest priority highlights"),
    FIVE_MINUTES(5, "5 Mins", "7 curated highlights with 3-bullet TL;DRs"),
    DEEP_DIVE(0, "Deep Dive", "Full personalized feed with topic breakdown")
}

enum class SnrRating(val label: String, val colorHex: Long) {
    HIGH_SIGNAL("High Signal (Fact-Dense)", 0xFF059669), // Emerald Green
    NEUTRAL("Neutral Fact", 0xFF0284C7),               // Sky Blue
    SPECULATIVE("Speculative / Hype", 0xFFD97706),       // Amber
    LOW_SIGNAL("Low Signal / Gossip", 0xFFDC2626)        // Red
}

data class Article(
    val id: String,
    val title: String,
    val originalTitle: String,
    val publisher: String,
    val category: String,
    val summaryBullets: List<String>,
    val fullContent: String,
    val sourceUrl: String,
    val imageUrl: String,
    val publishedAt: Long,
    val snrScore: Float, // 0.0 to 1.0 (e.g. 0.85 = 85%)
    val biasCategory: String,
    val timeEstimateMinutes: Int,
    val processingModeUsed: String,
    val isBookmarked: Boolean = false,
    val isRead: Boolean = false,
    val matchedTrapKeywords: List<String> = emptyList()
)
