package com.example.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

data class WikiContextData(
    val entityTitle: String,
    val description: String,
    val extract: String,
    val thumbnailUrl: String? = null,
    val wikipediaUrl: String = "https://en.wikipedia.org"
)

/**
 * MediaWiki REST API Service for instant encyclopedic context on entities,
 * companies, technologies, conflicts, or geopolitical subjects mentioned in news headlines.
 */
object WikipediaContextService {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun getContextForHeadline(headline: String): WikiContextData? = withContext(Dispatchers.IO) {
        val searchKeywords = extractSearchEntity(headline)
        if (searchKeywords.isBlank()) return@withContext null

        try {
            // 1. Search MediaWiki API for the most relevant encyclopedic topic
            val encodedQuery = URLEncoder.encode(searchKeywords, StandardCharsets.UTF_8.toString())
            val searchUrl = "https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=$encodedQuery&format=json&utf8=1&srlimit=1"

            val searchReq = Request.Builder()
                .url(searchUrl)
                .header("User-Agent", "SiftNewsRadar/1.6 (Android; MediaWiki-Client)")
                .build()

            val searchRes = okHttpClient.newCall(searchReq).execute()
            if (!searchRes.isSuccessful) return@withContext null

            val searchBody = searchRes.body?.string() ?: return@withContext null
            val searchJson = JSONObject(searchBody)
            val searchResults = searchJson.optJSONObject("query")?.optJSONArray("search")
            if (searchResults == null || searchResults.length() == 0) return@withContext null

            val topResult = searchResults.getJSONObject(0)
            val pageTitle = topResult.optString("title", "").trim()
            if (pageTitle.isBlank()) return@withContext null

            // 2. Fetch clean page summary from MediaWiki REST API
            val encodedTitle = URLEncoder.encode(pageTitle.replace(" ", "_"), StandardCharsets.UTF_8.toString())
            val summaryUrl = "https://en.wikipedia.org/api/rest_v1/page/summary/$encodedTitle"

            val summaryReq = Request.Builder()
                .url(summaryUrl)
                .header("User-Agent", "SiftNewsRadar/1.6 (Android; MediaWiki-Client)")
                .build()

            val summaryRes = okHttpClient.newCall(summaryReq).execute()
            if (!summaryRes.isSuccessful) return@withContext null

            val summaryBody = summaryRes.body?.string() ?: return@withContext null
            val summaryJson = JSONObject(summaryBody)

            val displayTitle = summaryJson.optString("displaytitle", pageTitle)
                .replace(Regex("<[^>]*>"), "") // strip any html tags
            val description = summaryJson.optString("description", "Encyclopedic background via Wikipedia")
            val extract = summaryJson.optString("extract", "").trim()

            if (extract.isBlank()) return@withContext null

            val thumbnailUrl = summaryJson.optJSONObject("thumbnail")?.optString("source")?.ifBlank { null }
            val wikiPageUrl = summaryJson.optJSONObject("content_urls")
                ?.optJSONObject("desktop")
                ?.optString("page", "https://en.wikipedia.org/wiki/$encodedTitle")
                ?: "https://en.wikipedia.org/wiki/$encodedTitle"

            WikiContextData(
                entityTitle = displayTitle,
                description = description,
                extract = extract,
                thumbnailUrl = thumbnailUrl,
                wikipediaUrl = wikiPageUrl
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Extracts salient search tokens from news headlines to query encyclopedic background.
     * Strips clickbait lead-ins, quotes, punctuation, and filler words.
     */
    internal fun extractSearchEntity(headline: String): String {
        val clean = headline
            .replace(Regex("(?i)^(exclusive|breaking|report|update|watch|opinion|analysis):\\s*"), "")
            .replace(Regex("['\"“”‘’]"), "")
            .trim()

        val tokens = clean.split(Regex("\\s+"))
            .filter { token ->
                val lower = token.lowercase()
                lower !in setOf("the", "a", "an", "and", "or", "in", "on", "at", "to", "for", "with", "by", "from", "of", "about", "is", "are", "says", "how", "why", "what", "new")
            }

        return tokens.take(4).joinToString(" ")
    }
}
