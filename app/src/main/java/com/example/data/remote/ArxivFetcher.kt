package com.example.data.remote

import android.util.Xml
import com.example.data.model.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Direct arXiv API client for peer-reviewed preprints and scientific discoveries.
 * Fetches highest-signal raw science, AI, and physics research papers with zero clickbait.
 */
object ArxivFetcher {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun fetchRecentPapers(): List<Article> = withContext(Dispatchers.IO) {
        val articles = mutableListOf<Article>()
        try {
            val url = "https://export.arxiv.org/api/query?search_query=cat:cs.AI+OR+cat:cs.LG+OR+cat:physics+OR+cat:q-bio&sortBy=submittedDate&sortOrder=descending&max_results=10"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "SiftNewsRadar/1.6 (Android; arXiv-API-Client)")
                .header("Accept", "application/atom+xml, application/xml")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyList()

            val xmlData = response.body?.string() ?: return@withContext emptyList()
            articles.addAll(parseAtomXml(xmlData))
        } catch (_: Exception) {
            // Fail gracefully if network or parse error
        }
        articles
    }

    private fun parseAtomXml(xml: String): List<Article> {
        val list = mutableListOf<Article>()
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(StringReader(xml))

        var eventType = parser.eventType
        var insideEntry = false
        var currentTitle = ""
        var currentId = ""
        var currentSummary = ""
        var currentPublished = ""
        var currentAuthor = ""

        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name?.lowercase(Locale.ROOT) ?: ""

            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (tagName == "entry") {
                        insideEntry = true
                        currentTitle = ""
                        currentId = ""
                        currentSummary = ""
                        currentPublished = ""
                        currentAuthor = ""
                    } else if (insideEntry) {
                        when (tagName) {
                            "title" -> currentTitle = parser.nextText().replace("\n", " ").trim()
                            "id" -> currentId = parser.nextText().trim()
                            "summary" -> currentSummary = parser.nextText().replace("\n", " ").trim()
                            "published" -> currentPublished = parser.nextText().trim()
                            "name" -> if (currentAuthor.isBlank()) currentAuthor = parser.nextText().trim()
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (tagName == "entry" && insideEntry) {
                        insideEntry = false
                        if (currentTitle.isNotBlank() && currentId.isNotBlank()) {
                            val publishedMs = try {
                                isoFormat.parse(currentPublished)?.time ?: System.currentTimeMillis()
                            } catch (_: Exception) {
                                System.currentTimeMillis()
                            }

                            val cleanId = currentId.substringAfterLast("/")
                            val isAi = currentTitle.contains("learning", ignoreCase = true) ||
                                    currentTitle.contains("neural", ignoreCase = true) ||
                                    currentTitle.contains("intelligence", ignoreCase = true) ||
                                    currentTitle.contains("model", ignoreCase = true) ||
                                    currentTitle.contains("transformer", ignoreCase = true)

                            val category = if (isAi) "AI" else "Science"

                            val bullet1 = "Peer-reviewed scientific preprint from arXiv Research Library."
                            val bullet2 = if (currentAuthor.isNotBlank()) "Primary author / investigator: $currentAuthor" else "Collaborative academic publication."
                            val bullet3 = currentSummary.take(160).trim().let { if (it.isNotBlank()) "$it..." else "Published scientific methodology and empirical research." }

                            list.add(
                                Article(
                                    id = "arxiv_$cleanId".ifBlank { "arxiv_${UUID.randomUUID()}" },
                                    title = currentTitle,
                                    originalTitle = currentTitle,
                                    publisher = "arXiv Science",
                                    category = category,
                                    snrScore = 0.96f,
                                    biasCategory = "Scientific/Peer-Reviewed",
                                    summaryBullets = listOf(bullet1, bullet2, bullet3),
                                    fullContent = "$currentTitle. Abstract: $currentSummary. Primary author: $currentAuthor. Source link: $currentId",
                                    sourceUrl = currentId,
                                    imageUrl = "https://static.arxiv.org/static/browse/0.3.4/images/arxiv-logo-web.png",
                                    publishedAt = publishedMs,
                                    timeEstimateMinutes = 4,
                                    isBookmarked = false,
                                    isRead = false,
                                    matchedTrapKeywords = emptyList(),
                                    processingModeUsed = "arXiv Scientific Signal (SNR 96%)"
                                )
                            )
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return list
    }
}
