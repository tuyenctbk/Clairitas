package com.example.data.repository

import com.example.data.model.SnrRating
import java.util.Locale

object TextRankSummarizer {

    private val CLICKBAIT_PATTERNS = listOf(
        Regex("(?i)^(sốc|nóng|bất ngờ|xôn xao|chấn động|kinh hoàng|hé lộ|bật mí|sự thật về|lý do vì sao|bạn có biết|chưa từng có):?\\s*"),
        Regex("(?i)\\b(bạn sẽ sốc|không ai ngờ|bí mật|khiến cả thế giới|xem ngay|cực hot|chao đảo|điếng người|sốt sình sịch)\\b"),
        Regex("(?i)^(shocking|must see|unbelievable|revealed|you won't believe|this is why|here is what):?\\s*")
    )

    private val SENSATIONAL_WORDS = listOf(
        "sốc", "nóng", "chấn động", "kinh hoàng", "bất ngờ", "hé lộ", "bật mí",
        "bí mật", "cực hot", "chao đảo", "điếng người", "khủng", "siêu khủng",
        "shocking", "unbelievable", "mindblowing", "insane", "devastating"
    )

    /**
     * Rewrites clickbait headlines into objective, factual statements.
     */
    fun rewriteTitle(rawTitle: String, content: String): String {
        var cleanTitle = rawTitle.trim()

        // Strip clickbait prefix or buzzwords
        for (pattern in CLICKBAIT_PATTERNS) {
            cleanTitle = pattern.replace(cleanTitle, "")
        }

        // If the title still looks like a question or vague teaser, extract the lead factual sentence
        if (cleanTitle.endsWith("?") || cleanTitle.length < 15 || cleanTitle.contains("...")) {
            val firstFactSentence = extractLeadFact(content)
            if (firstFactSentence.isNotBlank()) {
                return firstFactSentence.take(90)
            }
        }

        // Capitalize first letter
        return cleanTitle.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    /**
     * Extracts 3 key bullet points using extractive sentence ranking.
     */
    fun generate3BulletSummary(content: String, title: String): List<String> {
        val sentences = content.split(Regex("(?<=[.!?])\\s+"))
            .map { it.trim() }
            .filter { it.length > 20 && !it.contains("http") }

        if (sentences.isEmpty()) return listOf("Tóm tắt nhanh đang được cập nhật.")
        if (sentences.size <= 3) return sentences

        // Calculate word frequency map
        val words = content.lowercase(Locale.getDefault())
            .replace(Regex("[^\\a-z0-9àáảãạăắằẳẵặânấầẩẫậèéẻẽẹêếềểễệđìíỉĩịòóỏõọôốồổỗộơớờởỡợùúủũụưứừửữựỳýỷỹỵ\\s]"), "")
            .split(Regex("\\s+"))
            .filter { it.length > 3 && !isStopWord(it) }

        val wordFreq = words.groupingBy { it }.eachCount()

        // Score sentences
        val scoredSentences = sentences.mapIndexed { index, sentence ->
            var score = 0.0
            val sentenceWords = sentence.lowercase(Locale.getDefault()).split(Regex("\\s+"))

            // 1. Word frequency score
            sentenceWords.forEach { word ->
                score += (wordFreq[word] ?: 0) * 0.1
            }

            // 2. Fact density bonus (contains numbers, %, dates, currencies like VND, USD, tỷ, triệu)
            if (sentence.contains(Regex("\\d+(\\.\\d+)?%?"))) score += 3.0
            if (sentence.contains(Regex("(?i)(tỷ|triệu|USD|VND|VNĐ|đồng|quyết định|thông báo|tăng|giảm)"))) score += 2.5

            // 3. Position bias (first few sentences carry highest summary weight)
            if (index == 0) score += 4.0
            else if (index == 1) score += 2.0

            Pair(sentence, score)
        }

        // Return top 3 unique sentences preserving natural article order
        val topSentences = scoredSentences
            .sortedByDescending { it.second }
            .take(3)

        // Sort back by original sequence in article for coherent narrative flow
        val orderedBullets = sentences.filter { s -> topSentences.any { it.first == s } }
        return if (orderedBullets.isNotEmpty()) orderedBullets.take(3) else sentences.take(3)
    }

    /**
     * Calculates Signal-to-Noise Ratio (SNR) score from 0.00 to 1.00.
     */
    fun calculateSnr(content: String, title: String): Float {
        val combined = "$title $content".lowercase(Locale.getDefault())
        val tokens = combined.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (tokens.isEmpty()) return 0.5f

        var factCount = 0
        var noiseCount = 0

        tokens.forEach { token ->
            if (token.matches(Regex(".*\\d+.*")) || token.contains("%") || token.contains("$")) {
                factCount += 2 // Hard numbers & stats contribute double signal
            } else if (SENSATIONAL_WORDS.any { token.contains(it) }) {
                noiseCount += 2
            } else if (token.length > 5 && !isStopWord(token)) {
                factCount += 1
            }
        }

        val totalScoreTokens = (factCount + noiseCount).coerceAtLeast(1)
        val rawRatio = factCount.toFloat() / totalScoreTokens.toFloat()
        // Clamp between 0.35 and 0.98 for realistic readability display
        return (0.35f + rawRatio * 0.60f).coerceIn(0.35f, 0.98f)
    }

    fun determineBiasCategory(snrScore: Float, content: String): String {
        val lower = content.lowercase(Locale.getDefault())
        return when {
            snrScore > 0.80f -> "Dữ liệu thực tế (Factual Data)"
            lower.contains("dự báo") || lower.contains("phân tích") || lower.contains("chuyên gia") -> "Phân tích thị trường (Market Analysis)"
            lower.contains("ý kiến") || lower.contains("cho rằng") || lower.contains("quan điểm") -> "Góc nhìn cá nhân (Opinion)"
            snrScore < 0.50f -> "Nội dung suy đoán / Hype (Speculative)"
            else -> "Thông tin khách quan (Neutral Fact)"
        }
    }

    private fun extractLeadFact(content: String): String {
        return content.split(Regex("(?<=[.!?])\\s+"))
            .map { it.trim() }
            .firstOrNull { it.length in 25..110 && !SENSATIONAL_WORDS.any { word -> it.lowercase(Locale.getDefault()).contains(word) } }
            ?: ""
    }

    private fun isStopWord(word: String): Boolean {
        val stopWords = setOf(
            "và", "của", "cho", "với", "như", "trong", "trên", "tại", "được", "người", "các", "những", "này",
            "khi", "thì", "mà", "đã", "đang", "sẽ", "để", "ra", "vào", "về", "the", "and", "that", "this", "with"
        )
        return stopWords.contains(word)
    }
}
