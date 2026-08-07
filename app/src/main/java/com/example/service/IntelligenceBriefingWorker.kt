package com.example.service

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.repository.NewsRepository
import java.util.concurrent.TimeUnit

class IntelligenceBriefingWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : androidx.work.CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "IntelligenceBriefingWorker started daily briefing sync")
        return try {
            val repository = NewsRepository(applicationContext)
            val freshArticles = repository.refreshNewsFeed()
            
            val topStories = freshArticles
                .sortedByDescending { it.snrScore }
                .take(5)

            if (topStories.isNotEmpty()) {
                val briefingText = topStories.joinToString("\n• ") { "${it.publisher}: ${it.title}" }
                val prefs = applicationContext.getSharedPreferences("sift_prefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("latest_intelligence_briefing", briefingText)
                    .putLong("latest_intelligence_briefing_time", System.currentTimeMillis())
                    .apply()

                val notificationManager = RadarNotificationManager(applicationContext)
                notificationManager.sendDailyDigestNotification(topStories)
                Log.d(TAG, "Successfully generated daily Intelligence Briefing with ${topStories.size} top stories")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in IntelligenceBriefingWorker", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "IntelligenceBriefingWorker"
        const val BRIEFING_WORK_NAME = "com.example.daily_intelligence_briefing_work"

        fun scheduleDailyBriefing(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val briefingRequest = PeriodicWorkRequestBuilder<IntelligenceBriefingWorker>(24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                BRIEFING_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                briefingRequest
            )
            Log.d(TAG, "Enqueued daily IntelligenceBriefingWorker with WorkManager")
        }
    }
}
