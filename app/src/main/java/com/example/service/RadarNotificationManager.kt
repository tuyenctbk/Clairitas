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
            val radarChannel = NotificationChannel(
                CHANNEL_ID,
                "News Radar Keyword Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Instant alerts when news articles match your keyword traps"
                enableVibration(true)
            }

            val digestChannel = NotificationChannel(
                DIGEST_CHANNEL_ID,
                "Morning Daily Digest",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Morning compiled briefing of top-priority high-SNR news"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(radarChannel)
            notificationManager.createNotificationChannel(digestChannel)
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

        if (!com.example.util.PermissionHelper.hasNotificationPermission(context)) {
            android.util.Log.w("RadarNotification", "Notification permission not granted. Skipping radar notification.")
            return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.example.R.drawable.ic_notification_sift)
            .setContentTitle("🎯 Sift Radar: $matchedKeyword")
            .setContentText(article.title)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${article.title}\n\n📍 ${article.publisher} • High Signal ${(article.snrScore * 100).toInt()}%\n\n${article.summaryBullets.firstOrNull() ?: ""}")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            notificationManager.notify(article.id.hashCode(), notification)
        } catch (e: Exception) {
            android.util.Log.w("RadarNotification", "Could not post radar notification: ${e.message}")
        }
    }

    fun sendDailyDigestNotification(topArticles: List<Article>) {
        if (topArticles.isEmpty()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("SHOW_DAILY_DIGEST", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            10001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (!com.example.util.PermissionHelper.hasNotificationPermission(context)) {
            android.util.Log.w("RadarNotification", "Notification permission not granted. Skipping daily digest notification.")
            return
        }

        val firstArticle = topArticles.first()
        val headlineCount = topArticles.size
        val digestText = topArticles.take(3).joinToString("\n• ") { it.title }

        val notification = NotificationCompat.Builder(context, DIGEST_CHANNEL_ID)
            .setSmallIcon(com.example.R.drawable.ic_notification_sift)
            .setContentTitle("🌅 Your Morning Daily Digest ($headlineCount Top Stories)")
            .setContentText(firstArticle.title)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Top High-Signal Stories for Today:\n\n• $digestText\n\nTap to open full AI Morning Audio Digest & Briefing.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            notificationManager.notify(DAILY_DIGEST_NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            android.util.Log.w("RadarNotification", "Could not post daily digest notification: ${e.message}")
        }
    }

    companion object {
        const val CHANNEL_ID = "sift_radar_alerts"
        const val DIGEST_CHANNEL_ID = "sift_daily_digest"
        const val DAILY_DIGEST_NOTIFICATION_ID = 9901
    }
}
