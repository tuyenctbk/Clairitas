package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.model.Article

class RadarNotificationManager(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "News Radar Keyword Alerts (Săn Tin)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Cảnh báo tức thì khi tin tức khớp với từ khóa/bẫy tin săn lùng của bạn"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendRadarMatchNotification(article: Article, matchedKeyword: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("ARTICLE_ID", article.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            article.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🎯 Săn Tin Claritas: $matchedKeyword")
            .setContentText(article.title)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${article.title}\n\n📍 ${article.publisher} • High Signal ${ (article.snrScore * 100).toInt() }%\n\n${article.summaryBullets.firstOrNull() ?: ""}")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(article.id.hashCode(), notification)
    }

    companion object {
        const val CHANNEL_ID = "claritas_radar_alerts"
    }
}
