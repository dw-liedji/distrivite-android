package com.datavite.distrivite.data.notification

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.UUID
import java.util.concurrent.TimeUnit

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val notificationOrchestrator: NotificationOrchestrator
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_ORGANIZATION = "organization"
        private const val TAG = "NotificationWorker"
    }

    object MultiSyncScheduler {

        const val UNIQUE_WORK_NAME = "NotificationWorker"

        private fun buildConstraints(): Constraints {
            return Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // Options: CONNECTED, UNMETERED, NOT_ROAMING, etc.
                .setRequiresBatteryNotLow(true)
                .setRequiresCharging(false)
                .setRequiresStorageNotLow(true)
                // Uncomment this if you want idle-only execution (Doze Mode APIs, Android 6.0+)
                //.setRequiresDeviceIdle(true)
                .build()
        }

        /**
         * Enqueue One-Time Immediate Sync.
         *
         * @param context The application context
         * @param organization Organization name
         * @param existingWorkPolicy Policy: KEEP, REPLACE, APPEND
         */
        fun enqueueNow(
            context: Context,
            organization: String,
            existingWorkPolicy: ExistingWorkPolicy = ExistingWorkPolicy.KEEP
        ): UUID {
            val inputData = Data.Builder()
                .putString(KEY_ORGANIZATION, organization)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInputData(inputData)
                .setConstraints(buildConstraints())
                .build()

            WorkManager.Companion.getInstance(context).enqueueUniqueWork(
                "$UNIQUE_WORK_NAME$organization",
                existingWorkPolicy,
                workRequest
            )



            return workRequest.id
        }

        /**
         * Schedule a periodic sync (e.g., every 12 hours).
         *
         * @param context The application context
         * @param organization Organization name
         * @param repeatIntervalMinutes Interval in hours (min 15 mins for periodic work)
         * @param existingPeriodicPolicy Policy: KEEP, UPDATE
         */
        fun schedulePeriodicSync(
            context: Context,
            organization: String,
            repeatIntervalMinutes: Long = 20,
            existingPeriodicPolicy: ExistingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.KEEP
        ) {
            val inputData = Data.Builder()
                .putString(KEY_ORGANIZATION, organization)
                .build()

            val periodicWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
                repeatIntervalMinutes, TimeUnit.MINUTES
            )
                .setInputData(inputData)
                .setConstraints(buildConstraints())
                .build()

            WorkManager.Companion.getInstance(context).enqueueUniquePeriodicWork(
                "$UNIQUE_WORK_NAME$organization",
                existingPeriodicPolicy,
                periodicWorkRequest
            )
        }

        /**
         * Cancel the scheduled work (OneTime or Periodic)
         */
        fun cancelScheduledSync(context: Context, organization: String) {
            WorkManager.Companion.getInstance(context).cancelUniqueWork("$UNIQUE_WORK_NAME$organization")
        }
    }

    override suspend fun doWork(): Result {
        val organization = inputData.getString(KEY_ORGANIZATION) ?: run {
            Log.e(TAG, "Missing organization parameter")
            return Result.failure()
        }

        return try {
            Log.i(TAG, "Starting pushing changes...")
            notificationOrchestrator.notify(organization)
            Log.i(TAG, "Pushing changes completed.")
            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "Pushing + pulling failed", e)
            Result.retry()
        }
    }

}