package com.bunny.contestpulse.data.sync

import android.content.Context
import android.util.Log
import androidx.work.*
import com.bunny.contestpulse.alarm.AlarmScheduler
import com.bunny.contestpulse.data.repository.ContestRepository
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker that periodically syncs contest data
 * from all platforms and reschedules alarms.
 */
class ContestSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "ContestSyncWorker"
        private const val WORK_NAME = "contest_sync"

        /**
         * Schedule periodic sync every 3 hours.
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<ContestSyncWorker>(
                3, TimeUnit.HOURS,
                30, TimeUnit.MINUTES // Flex interval
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )

            Log.d(TAG, "Periodic sync scheduled")
        }

        /**
         * Trigger an immediate one-time sync.
         */
        fun syncNow(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<ContestSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueue(request)

            Log.d(TAG, "Immediate sync triggered")
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting contest sync...")

        val repository = ContestRepository.getInstance(applicationContext)
        val success = repository.refreshContests()

        if (success) {
            // Reschedule alarms after successful sync
            AlarmScheduler.scheduleAllAlarms(applicationContext)
            Log.d(TAG, "Contest sync completed successfully")
            return Result.success()
        }

        Log.w(TAG, "Contest sync failed")
        return Result.retry()
    }
}
