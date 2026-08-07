package com.example.data.repository

import com.example.BuildConfig
import com.example.data.model.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ProcessedNewsResult(
    val honestTitle: String,
    val summaryBullets: List<String>,
    val snrScore: Float,
    val biasCategory: String
)

object GeminiSummarizer {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun processArticleWithGemini(
        rawTitle: String,
        content: String,
        userCustomApiKey: String? = null
    ): ProcessedNewsResult = withContext(Dispatchers.IO) {
        val apiKey = userCustomApiKey?.takeIf { it.isNotBlank() } ?: BuildConfig.GEMINI_API_KEY

        val localFallback = ProcessedNewsResult(
            honestTitle = TextRankSummarizer.rewriteTitle(rawTitle, content),
            summaryBullets = TextRankSummarizer.generate3BulletSummary(content, rawTitle),
            snrScore = TextRankSummarizer.calculateSnr(content, rawTitle),
            biasCategory = TextRankSummarizer.determineBiasCategory(TextRankSummarizer.calculateSnr(content, rawTitle), content)
        )

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext localFallback
        }

        try {
            val prompt = """
                Transform the following article into high-signal anti-clickbait intelligence:
                Original Title: $rawTitle
                Content: ${content.take(2000)}

                Respond in exact JSON format:
                {
                  "honestTitle": "Concise, completely objective title removing all clickbait/sensationalism",
                  "summaryBullets": ["Key point 1 with data/metrics", "Key point 2 core insight", "Key point 3 impact/conclusion"],
                  "snrScore": 0.85,
                  "biasCategory": "Factual Data / Market Analysis / Opinion / Perspective"
                }
            """.trimIndent()

            // Construct Gemini Request JSON payload
            val textPart = JSONObject().put("text", prompt)
            val partsArray = JSONArray().put(textPart)
            val contentObj = JSONObject().put("parts", partsArray)
            val contentsArray = JSONArray().put(contentObj)

            val systemPart = JSONObject().put("text", "You are an expert news intelligence analyst. Return raw JSON matching the required schema without markdown code blocks.")
            val systemInstructionObj = JSONObject().put("parts", JSONArray().put(systemPart))

            val requestJson = JSONObject()
                .put("contents", contentsArray)
                .put("systemInstruction", systemInstructionObj)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBodyStr = response.body?.string() ?: ""

            if (response.isSuccessful && responseBodyStr.isNotBlank()) {
                val rootObj = JSONObject(responseBodyStr)
                val candidates = rootObj.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val contentRes = candidate.optJSONObject("content")
                    val resParts = contentRes?.optJSONArray("parts")
                    if (resParts != null && resParts.length() > 0) {
                        val rawResponseText = resParts.getJSONObject(0).optString("text", "")
                        val cleanJsonText = rawResponseText.replace("```json", "").replace("```", "").trim()

                        val parsedRes = JSONObject(cleanJsonText)
                        val honestTitle = parsedRes.optString("honestTitle", localFallback.honestTitle)
                        val snrScore = parsedRes.optDouble("snrScore", localFallback.snrScore.toDouble()).toFloat()
                        val biasCat = parsedRes.optString("biasCategory", localFallback.biasCategory)

                        val bulletsArray = parsedRes.optJSONArray("summaryBullets")
                        val bulletsList = mutableListOf<String>()
                        if (bulletsArray != null) {
                            for (i in 0 until bulletsArray.length()) {
                                bulletsList.add(bulletsArray.getString(i))
                            }
                        }

                        val finalBullets = if (bulletsList.isNotEmpty()) bulletsList else localFallback.summaryBullets

                        return@withContext ProcessedNewsResult(
                            honestTitle = honestTitle,
                            summaryBullets = finalBullets,
                            snrScore = snrScore,
                            biasCategory = biasCat
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        localFallback
    }

    suspend fun translateArticle(
        title: String,
        bullets: List<String>,
        content: String,
        targetLanguage: String,
        userCustomApiKey: String? = null
    ): TranslatedArticleResult = withContext(Dispatchers.IO) {
        val apiKey = userCustomApiKey?.takeIf { it.isNotBlank() } ?: BuildConfig.GEMINI_API_KEY

        val localFallback = TranslatedArticleResult(
            title = "[Translated to $targetLanguage] $title",
            bullets = bullets.map { "[Translated to $targetLanguage] $it" },
            content = "[Translated to $targetLanguage]\n\n$content"
        )

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext localFallback
        }

        try {
            val prompt = """
                You are a professional translator. Translate the following news content into $targetLanguage. Maintain the professional journalistic tone and ensure accurate vocabulary.

                Original Title: $title

                Original Summary Bullets:
                ${bullets.joinToString("\n") { "- $it" }}

                Original Main Body:
                ${content.take(1500)}

                Respond in exact JSON format:
                {
                  "title": "translated title here",
                  "bullets": ["translated bullet 1", "translated bullet 2", "translated bullet 3"],
                  "content": "translated main body text here"
                }
            """.trimIndent()

            val textPart = JSONObject().put("text", prompt)
            val partsArray = JSONArray().put(textPart)
            val contentObj = JSONObject().put("parts", partsArray)
            val contentsArray = JSONArray().put(contentObj)

            val systemPart = JSONObject().put("text", "You are an expert multi-lingual translator. Return raw JSON matching the required schema without markdown code blocks.")
            val systemInstructionObj = JSONObject().put("parts", JSONArray().put(systemPart))

            val requestJson = JSONObject()
                .put("contents", contentsArray)
                .put("systemInstruction", systemInstructionObj)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBodyStr = response.body?.string() ?: ""

            if (response.isSuccessful && responseBodyStr.isNotBlank()) {
                val rootObj = JSONObject(responseBodyStr)
                val candidates = rootObj.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val contentRes = candidate.optJSONObject("content")
                    val resParts = contentRes?.optJSONArray("parts")
                    if (resParts != null && resParts.length() > 0) {
                        val rawResponseText = resParts.getJSONObject(0).optString("text", "")
                        val cleanJsonText = rawResponseText.replace("```json", "").replace("```", "").trim()

                        val parsedRes = JSONObject(cleanJsonText)
                        val transTitle = parsedRes.optString("title", localFallback.title)
                        val transContent = parsedRes.optString("content", localFallback.content)

                        val bulletsArray = parsedRes.optJSONArray("bullets")
                        val bulletsList = mutableListOf<String>()
                        if (bulletsArray != null) {
                            for (i in 0 until bulletsArray.length()) {
                                bulletsList.add(bulletsArray.getString(i))
                            }
                        }
                        val finalBullets = if (bulletsList.isNotEmpty()) bulletsList else localFallback.bullets

                        return@withContext TranslatedArticleResult(
                            title = transTitle,
                            bullets = finalBullets,
                            content = transContent
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        localFallback
    }
}

data class TranslatedArticleResult(
    val title: String,
    val bullets: List<String>,
    val content: String
)
