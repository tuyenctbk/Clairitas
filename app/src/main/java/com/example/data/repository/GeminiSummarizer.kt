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
                Biến bài báo sau thành tin tức tình báo chuẩn xác (Anti-Clickbait News Intelligence):
                Tiêu đề gốc: $rawTitle
                Nội dung: ${content.take(2000)}

                Yêu cầu phản hồi định dạng JSON chính xác:
                {
                  "honestTitle": "Tiêu đề viết lại ngắn gọn, hoàn toàn khách quan, bỏ hết các từ giật gân/clickbait",
                  "summaryBullets": ["Ý chính 1 có số liệu/dữ liệu cụ thể", "Ý chính 2 thông tin cốt lõi", "Ý chính 3 tác động/kết luận"],
                  "snrScore": 0.85,
                  "biasCategory": "Dữ liệu thực tế / Phân tích thị trường / Góc nhìn cá nhân / Suy đoán"
                }
            """.trimIndent()

            // Construct Gemini Request JSON payload
            val textPart = JSONObject().put("text", prompt)
            val partsArray = JSONArray().put(textPart)
            val contentObj = JSONObject().put("parts", partsArray)
            val contentsArray = JSONArray().put(contentObj)

            val systemPart = JSONObject().put("text", "Bạn là biên tập viên tin tức tình báo tin cậy. Trả về đúng JSON theo cấu trúc được yêu cầu, không kèm markdown code block.")
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
}
