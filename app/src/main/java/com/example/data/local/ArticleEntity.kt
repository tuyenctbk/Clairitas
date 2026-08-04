package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Article

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val originalTitle: String,
    val publisher: String,
    val category: String,
    val summaryBulletsJson: String, // Pipe-separated or JSON list
    val fullContent: String,
    val sourceUrl: String,
    val imageUrl: String,
    val publishedAt: Long,
    val snrScore: Float,
    val biasCategory: String,
    val timeEstimateMinutes: Int,
    val processingModeUsed: String,
    val isBookmarked: Boolean = false,
    val isRead: Boolean = false,
    val matchedTrapKeywordsStr: String = "" // Comma separated
) {
    fun toArticle(): Article {
        val bullets = if (summaryBulletsJson.isBlank()) emptyList() else summaryBulletsJson.split("|||")
        val traps = if (matchedTrapKeywordsStr.isBlank()) emptyList() else matchedTrapKeywordsStr.split(",")
        return Article(
            id = id,
            title = title,
            originalTitle = originalTitle,
            publisher = publisher,
            category = category,
            summaryBullets = bullets,
            fullContent = fullContent,
            sourceUrl = sourceUrl,
            imageUrl = imageUrl,
            publishedAt = publishedAt,
            snrScore = snrScore,
            biasCategory = biasCategory,
            timeEstimateMinutes = timeEstimateMinutes,
            processingModeUsed = processingModeUsed,
            isBookmarked = isBookmarked,
            isRead = isRead,
            matchedTrapKeywords = traps
        )
    }

    companion object {
        fun fromArticle(article: Article): ArticleEntity {
            return ArticleEntity(
                id = article.id,
                title = article.title,
                originalTitle = article.originalTitle,
                publisher = article.publisher,
                category = article.category,
                summaryBulletsJson = article.summaryBullets.joinToString("|||"),
                fullContent = article.fullContent,
                sourceUrl = article.sourceUrl,
                imageUrl = article.imageUrl,
                publishedAt = article.publishedAt,
                snrScore = article.snrScore,
                biasCategory = article.biasCategory,
                timeEstimateMinutes = article.timeEstimateMinutes,
                processingModeUsed = article.processingModeUsed,
                isBookmarked = article.isBookmarked,
                isRead = article.isRead,
                matchedTrapKeywordsStr = article.matchedTrapKeywords.joinToString(",")
            )
        }
    }
}
