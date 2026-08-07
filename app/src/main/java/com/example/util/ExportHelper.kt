package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.Article
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportHelper {

    fun generateMarkdown(articles: List<Article>): String {
        return buildString {
            append("# Sift News Curation & Intelligence Export\n")
            append("Generated on: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}\n\n")
            append("---\n\n")
            articles.forEachIndexed { index, article ->
                append("## [${index + 1}] ${article.title}\n\n")
                append("- **Publisher:** ${article.publisher}\n")
                append("- **Category:** ${article.category}\n")
                append("- **SNR Integrity Score:** ${article.snrScore}/100 (${article.biasCategory})\n")
                append("- **Source URL:** [Link to original source](${article.sourceUrl})\n\n")
                
                append("### ⚡ AI Key Takeaways & Summary\n")
                article.summaryBullets.forEach { bullet ->
                    append("- $bullet\n")
                }
                append("\n")
                
                append("### 📝 Full Curated Analysis\n")
                append("${article.fullContent}\n\n")
                append("---\n\n")
            }
        }
    }

    fun shareMarkdown(context: Context, filename: String, articles: List<Article>) {
        try {
            val content = generateMarkdown(articles)
            val cacheFile = File(context.cacheDir, "$filename.md")
            FileOutputStream(cacheFile).use { fos ->
                fos.write(content.toByteArray())
            }
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "$filename (Markdown)")
                putExtra(Intent.EXTRA_TEXT, content)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Export Markdown"))
            Toast.makeText(context, "Markdown export ready to share!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to export Markdown: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun sharePdf(context: Context, filename: String, articles: List<Article>) {
        try {
            val pdfDocument = PdfDocument()
            val paint = Paint().apply {
                isAntiAlias = true
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            val titlePaint = Paint().apply {
                isAntiAlias = true
                textSize = 16f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val headerPaint = Paint().apply {
                isAntiAlias = true
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            var y = 50f
            val margin = 50f
            val maxWidth = 595f - (margin * 2)

            fun checkPageEnd(neededHeight: Float) {
                if (y + neededHeight > 800f) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    y = 50f
                }
            }

            canvas.drawText("Sift News - Intelligence Export", margin, y, titlePaint)
            y += 24f
            paint.color = Color.DKGRAY
            canvas.drawText("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}", margin, y, paint)
            y += 26f

            articles.forEachIndexed { index, article ->
                checkPageEnd(50f)
                
                val articleTitle = "[${index + 1}] ${article.title}"
                val titleLines = wrapText(articleTitle, titlePaint, maxWidth)
                titleLines.forEach { line ->
                    checkPageEnd(20f)
                    canvas.drawText(line, margin, y, headerPaint)
                    y += 18f
                }
                
                checkPageEnd(18f)
                paint.color = Color.parseColor("#0284C7")
                canvas.drawText("Source: ${article.publisher} | SNR Score: ${article.snrScore}/100", margin, y, paint)
                paint.color = Color.BLACK
                y += 20f

                checkPageEnd(18f)
                canvas.drawText("AI Key Takeaways:", margin, y, headerPaint)
                y += 16f

                article.summaryBullets.forEach { bullet ->
                    val bulletText = "• $bullet"
                    val wrappedBullet = wrapText(bulletText, paint, maxWidth)
                    wrappedBullet.forEach { line ->
                        checkPageEnd(16f)
                        canvas.drawText(line, margin, y, paint)
                        y += 14f
                    }
                }
                y += 6f

                checkPageEnd(18f)
                canvas.drawText("Curated Analysis:", margin, y, headerPaint)
                y += 16f

                val wrappedBody = wrapText(article.fullContent, paint, maxWidth)
                wrappedBody.forEach { line ->
                    checkPageEnd(15f)
                    canvas.drawText(line, margin, y, paint)
                    y += 13f
                }

                y += 20f
            }

            pdfDocument.finishPage(page)

            val cacheFile = File(context.cacheDir, "$filename.pdf")
            pdfDocument.writeTo(FileOutputStream(cacheFile))
            pdfDocument.close()

            val uri: Uri = try {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", cacheFile)
            } catch (e: Exception) {
                Uri.fromFile(cacheFile)
            }

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_SUBJECT, "$filename (PDF)")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Export PDF"))
            Toast.makeText(context, "PDF export ready to share!", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to export PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun shareSummaryAsImage(context: Context, article: Article) {
        try {
            val width = 1080
            val height = 1080
            val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val backPaint = Paint().apply {
                isAntiAlias = true
                shader = android.graphics.LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    Color.parseColor("#0F172A"),
                    Color.parseColor("#020617"),
                    android.graphics.Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backPaint)

            val barPaint = Paint().apply {
                color = Color.parseColor("#38BDF8")
            }
            canvas.drawRect(0f, 0f, width.toFloat(), 12f, barPaint)

            val brandPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#38BDF8")
                textSize = 24f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                letterSpacing = 0.15f
            }

            val titlePaint = Paint().apply {
                isAntiAlias = true
                color = Color.WHITE
                textSize = 44f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val metaPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#94A3B8")
                textSize = 26f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }

            val bulletPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#E2E8F0")
                textSize = 30f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }

            val bulletIconPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#38BDF8")
                textSize = 32f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val watermarkPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#475569")
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                letterSpacing = 0.1f
            }

            var y = 90f
            val margin = 80f
            val contentWidth = width - (margin * 2)

            canvas.drawText("SIFT NEWS INTELLIGENCE • VERIFIED SUMMARY", margin, y, brandPaint)
            y += 65f

            val titleLines = wrapText(article.title, titlePaint, contentWidth)
            titleLines.take(3).forEach { line ->
                canvas.drawText(line, margin, y, titlePaint)
                y += 56f
            }
            y += 15f

            canvas.drawText("Publisher: ${article.publisher}   |   Integrity SNR: ${article.snrScore}/100", margin, y, metaPaint)
            y += 40f

            val dividerPaint = Paint().apply {
                color = Color.parseColor("#1E293B")
                strokeWidth = 3f
            }
            canvas.drawLine(margin, y, width - margin, y, dividerPaint)
            y += 60f

            val subHeaderPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#06B6D4")
                textSize = 26f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("AI KEY TAKEAWAYS:", margin, y, subHeaderPaint)
            y += 50f

            article.summaryBullets.take(3).forEach { bullet ->
                canvas.drawText("⚡", margin, y + 2f, bulletIconPaint)
                
                val wrappedBullet = wrapText(bullet, bulletPaint, contentWidth - 45f)
                wrappedBullet.take(3).forEach { line ->
                    canvas.drawText(line, margin + 45f, y, bulletPaint)
                    y += 40f
                }
                y += 15f
            }

            y = height - 70f
            canvas.drawText("SUMMARIZED BY GEMINI 1.5 PRO", margin, y, watermarkPaint)
            
            val signaturePaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#38BDF8")
                textSize = 22f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("SIFTNEWS.APP", width - margin, y, signaturePaint)

            val cacheFile = File(context.cacheDir, "sift_share_${article.id}.png")
            FileOutputStream(cacheFile).use { fos ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, fos)
            }

            val shareUri: Uri = try {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", cacheFile)
            } catch (e: Exception) {
                Uri.fromFile(cacheFile)
            }

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, shareUri)
                putExtra(Intent.EXTRA_TEXT, "Read the AI Curation and integrity-verified report on \"${article.title}\" via Sift News.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share Image Card"))
            Toast.makeText(context, "Social snippet card generated!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to share image snippet: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""
        
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val width = paint.measureText(testLine)
            if (width > maxWidth) {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine)
                }
                currentLine = word
            } else {
                currentLine = testLine
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }
        return lines
    }
}
