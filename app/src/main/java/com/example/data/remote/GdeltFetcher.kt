package com.example.data.remote

import com.example.data.model.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * GDELT Project 2.0 API client for real-time global macro events and international intelligence.
 * Aggregates objective worldwide event reporting across international sources.
 */
object GdeltFetcher {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun fetchGlobalEvents(): List<Article> = withContext(Dispatchers.IO) {
        val articles = mutableListOf<Article>()
        try {
            val url = "https://api.gdeltproject.org/api/v2/doc/doc?query=global%20macro%20international&mode=artlist&maxrecords=10&format=json&sort=DateDesc"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "SiftNewsRadar/1.6 (Android; GDELT-Client)")
                .header("Accept", "application/json")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyList()

            val jsonString = response.body?.string() ?: return@withContext emptyList()
            val root = JSONObject(jsonString)
            val jsonArticles = root.optJSONArray("articles") ?: return@withContext emptyList()

            val dateFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US)
            val now = System.currentTimeMillis()

            for (i in 0 until jsonArticles.length()) {
                val item = jsonArticles.optJSONObject(i) ?: continue
                val title = item.optString("title", "").trim()
                val itemUrl = item.optString("url", "").trim()
                if (title.isBlank() || itemUrl.isBlank()) continue

                val domain = item.optString("domain", "GDELT Project")
                val seendate = item.optString("seendate", "")
                val country = item.optString("sourcecountry", "Global")
                val language = item.optString("language", "English")

                val pubDateMs = try {
                    if (seendate.isNotBlank()) dateFormat.parse(seendate)?.time ?: now else now
                } catch (_: Exception) {
                    now
                }

                val bullet1 = "Global macro event tracked by GDELT Project event database."
                val bullet2 = "Source origin: $domain ($country, language: $language)."
                val bullet3 = "Monitored across international diplomatic and journalistic channels."

                val fullText = "$title. Global event observed by GDELT Project via $domain in $country. URL: $itemUrl"

                articles.add(
                    Article(
                        id = "gdelt_${itemUrl.hashCode()}".ifBlank { "gdelt_${UUID.randomUUID()}" },
                        title = title,
                        originalTitle = title,
                        publisher = "GDELT Project ($domain)",
                        category = "World",
                        snrScore = 0.88f,
                        biasCategory = "Global Macro Event",
                        summaryBullets = listOf(bullet1, bullet2, bullet3),
                        fullContent = fullText,
                        sourceUrl = itemUrl,
                        imageUrl = "",
                        publishedAt = pubDateMs,
                        timeEstimateMinutes = 3,
                        isBookmarked = false,
                        isRead = false,
                        matchedTrapKeywords = emptyList(),
                        processingModeUsed = "GDELT Macro Event Signal (SNR 88%)"
                    )
                )
            }
        } catch (_: Exception) {
            // Fail gracefully if network or parse error
        }
        articles
    }
}
