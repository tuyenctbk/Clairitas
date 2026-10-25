package com.example.data.remote

import com.example.data.model.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * High-SNR news fetcher querying Hacker News API (Algolia / Firebase).
 * Provides community-vetted technology, computing, and AI breakthroughs with zero clickbait.
 */
object HackerNewsFetcher {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun fetchTopTechStories(): List<Article> = withContext(Dispatchers.IO) {
        val articles = mutableListOf<Article>()
        try {
            val url = "https://hn.algolia.com/api/v1/search?tags=front_page&hitsPerPage=12"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "SiftNewsRadar/1.6 (Android)")
                .header("Accept", "application/json")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyList()

            val jsonString = response.body?.string() ?: return@withContext emptyList()
            val root = JSONObject(jsonString)
            val hits = root.optJSONArray("hits") ?: return@withContext emptyList()

            val now = System.currentTimeMillis()

            for (i in 0 until hits.length()) {
                val hit = hits.optJSONObject(i) ?: continue
                val title = hit.optString("title", "").trim()
                if (title.isBlank()) continue

                val objectID = hit.optString("objectID", "")
                val rawUrl = hit.optString("url", "").trim()
                val sourceUrl = if (rawUrl.isNotBlank() && rawUrl.startsWith("http")) {
                    rawUrl
                } else {
                    "https://news.ycombinator.com/item?id=$objectID"
                }

                val author = hit.optString("author", "HN Community")
                val points = hit.optInt("points", 0)
                val numComments = hit.optInt("num_comments", 0)
                val createdAtI = hit.optLong("created_at_i", now / 1000) * 1000L

                val isAiTopic = title.contains("AI", ignoreCase = true) ||
                        title.contains("LLM", ignoreCase = true) ||
                        title.contains("GPT", ignoreCase = true) ||
                        title.contains("Neural", ignoreCase = true) ||
                        title.contains("Machine Learning", ignoreCase = true)

                val category = if (isAiTopic) "AI" else "Tech"

                val bullet1 = "High-SNR technical submission vetted by $author ($points points, $numComments comments)."
                val bullet2 = "Primary domain: ${extractDomain(sourceUrl)}"
                val bullet3 = "Curated directly from Hacker News front page community signal."

                val fullText = "$title. Technical discussion submitted by $author with $points points on Hacker News. Source: $sourceUrl."

                articles.add(
                    Article(
                        id = "hn_$objectID".ifBlank { "hn_${UUID.randomUUID()}" },
                        title = title,
                        originalTitle = title,
                        publisher = "Hacker News",
                        category = category,
                        snrScore = 0.92f,
                        biasCategory = "Neutral/Technical",
                        summaryBullets = listOf(bullet1, bullet2, bullet3),
                        fullContent = fullText,
                        sourceUrl = sourceUrl,
                        imageUrl = "https://news.ycombinator.com/favicon.ico",
                        publishedAt = createdAtI,
                        timeEstimateMinutes = 2,
                        isBookmarked = false,
                        isRead = false,
                        matchedTrapKeywords = emptyList(),
                        processingModeUsed = "Hacker News Signal (SNR 92%)"
                    )
                )
            }
        } catch (_: Exception) {
            // Fail gracefully if network or parse error
        }
        articles
    }

    private fun extractDomain(url: String): String {
        return try {
            val uri = java.net.URI(url)
            val host = uri.host ?: "ycombinator.com"
            if (host.startsWith("www.")) host.substring(4) else host
        } catch (_: Exception) {
            "ycombinator.com"
        }
    }
}
