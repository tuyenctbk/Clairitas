package com.example.service

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.repository.NewsRepository
import java.util.concurrent.TimeUnit

class NewsSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Background NewsSyncWorker started execution")
        val prefs = applicationContext.getSharedPreferences("sift_prefs", Context.MODE_PRIVATE)
        val isLowPower = prefs.getBoolean("is_low_power_mode", false)
        if (isLowPower) {
            Log.d(TAG, "Low Power Mode is enabled. Skipping background sync to save battery.")
            return Result.success()
        }

        val retentionDays = prefs.getInt("auto_clear_retention_days", 30)
        return try {
            val repository = NewsRepository(applicationContext)
            if (retentionDays > 0) {
                repository.autoClearOldArticlesAndCache(retentionDays)
            }
            val freshArticles = repository.refreshNewsFeed()
            Log.d(TAG, "Background NewsSyncWorker successfully updated Room database with fresh articles")

            // Send Daily Digest push notification with top priority high-SNR stories
            val topStories = freshArticles
                .sortedByDescending { it.snrScore }
                .take(5)

            if (topStories.isNotEmpty()) {
                val notificationManager = RadarNotificationManager(applicationContext)
                notificationManager.sendDailyDigestNotification(topStories)
                Log.d(TAG, "Sent Morning Daily Digest notification for ${topStories.size} top stories")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error performing background news sync", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "NewsSyncWorker"
        const val PERIODIC_WORK_NAME = "com.example.news_background_sync"
        const val ONE_TIME_WORK_NAME = "com.example.news_online_sync"

        /**
         * Schedules recurring background sync every 15 minutes when connected to network.
         */
        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<NewsSyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
            Log.d(TAG, "Enqueued periodic background sync with WorkManager")
        }

        /**
         * Enqueues an immediate background sync as soon as network is online.
         */
        fun triggerImmediateSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<NewsSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                ONE_TIME_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                syncRequest
            )
            Log.d(TAG, "Enqueued immediate background sync with WorkManager")
        }
    }
}
