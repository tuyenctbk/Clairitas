package com.example.data.remote.model

import com.example.data.local.ArticleEntity
import com.example.data.model.Article
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.UUID

/**
 * Data Transfer Objects (DTOs) for Moshi JSON parsing of remote News API responses.
 */
@JsonClass(generateAdapter = true)
data class NewsResponseDto(
    @field:Json(name = "status") val status: String? = null,
    @field:Json(name = "totalResults") val totalResults: Int? = null,
    @field:Json(name = "articles") val articles: List<NewsArticleDto>? = null,
    @field:Json(name = "code") val code: String? = null,
    @field:Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class NewsArticleDto(
    @field:Json(name = "source") val source: NewsSourceDto? = null,
    @field:Json(name = "author") val author: String? = null,
    @field:Json(name = "title") val title: String? = null,
    @field:Json(name = "description") val description: String? = null,
    @field:Json(name = "url") val url: String? = null,
    @field:Json(name = "urlToImage") val urlToImage: String? = null,
    @field:Json(name = "publishedAt") val publishedAt: String? = null,
    @field:Json(name = "content") val content: String? = null
)

@JsonClass(generateAdapter = true)
data class NewsSourceDto(
    @field:Json(name = "id") val id: String? = null,
    @field:Json(name = "name") val name: String? = null
)

/**
 * Extension mapper to convert a remote [NewsArticleDto] to local Room [ArticleEntity]
 * for integration with the database persistence layer.
 */
fun NewsArticleDto.toArticleEntity(category: String = "General"): ArticleEntity {
    val cleanTitle = title?.takeIf { it.isNotBlank() } ?: "Untitled Article"
    val publisherName = source?.name ?: author ?: "Public News"
    val articleUrl = url ?: ""
    val articleId = if (articleUrl.isNotBlank()) {
        articleUrl.hashCode().toString()
    } else {
        UUID.randomUUID().toString()
    }
    val contentBody = content ?: description ?: cleanTitle
    val summaryText = description ?: contentBody.take(200)

    return ArticleEntity(
        id = articleId,
        title = cleanTitle,
        originalTitle = cleanTitle,
        publisher = publisherName,
        category = category,
        summaryBulletsJson = summaryText,
        fullContent = contentBody,
        sourceUrl = articleUrl,
        imageUrl = urlToImage ?: "",
        publishedAt = parseDateToMillis(publishedAt),
        snrScore = 0.85f,
        biasCategory = "Balanced",
        timeEstimateMinutes = calculateReadTimeMinutes(contentBody),
        processingModeUsed = "TextRank",
        isBookmarked = false,
        isRead = false,
        matchedTrapKeywordsStr = ""
    )
}

/**
 * Extension mapper to convert a remote [NewsArticleDto] directly to domain [Article].
 */
fun NewsArticleDto.toArticle(category: String = "General"): Article {
    return toArticleEntity(category).toArticle()
}

private fun parseDateToMillis(dateStr: String?): Long {
    if (dateStr.isNullByOrBlank()) return System.currentTimeMillis()
    return try {
        // Simple fallback calculation or return current time
        System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}

private fun String?.isNullByOrBlank(): Boolean = this == null || this.isBlank()

private fun calculateReadTimeMinutes(text: String): Int {
    val words = text.split("\\s+".toRegex()).size
    val minutes = (words / 200).coerceAtLeast(1)
    return minutes
}
