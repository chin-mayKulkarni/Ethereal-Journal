package com.diary.ai.data.sync

import android.content.Context
import androidx.work.*
import com.diary.ai.domain.repository.SyncScheduler
import java.util.concurrent.TimeUnit

class SyncSchedulerImpl(private val context: Context) : SyncScheduler {

    // Shared network constraint: both immediate and periodic syncs require WiFi
    // or mobile data to be present before WorkManager dispatches the worker.
    private val networkConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /**
     * Enqueues a one-time sync with [ExistingWorkPolicy.REPLACE].
     *
     * Called immediately after the user saves a note so that the new entry
     * is pushed to Drive as soon as a network becomes available.
     * REPLACE policy ensures only one upload is queued even if the user
     * saves several notes in quick succession.
     */
    override fun scheduleImmediateSync() {
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(networkConstraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }

    /**
     * Enqueues a periodic background sync using [PeriodicWorkRequest].
     *
     * Interval: 15 minutes — WorkManager's minimum ([PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS]).
     * Policy:   [ExistingPeriodicWorkPolicy.KEEP] — if a periodic job is already
     *           running, do NOT cancel it. This avoids resetting the interval
     *           timer when the app re-registers on every launch.
     *
     * Should be called once from [DiaryApplication.onCreate] after the user
     * has authenticated so that the sync runs silently in the background even
     * when the app is not in the foreground.
     */
    override fun schedulePeriodicSync() {
        val periodicRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(networkConstraints)
            // Back-off policy: if the worker returns Result.retry(), WorkManager
            // will wait at least 30 seconds before trying again, doubling each
            // time (exponential) up to a 5-hour maximum.
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )
    }

    companion object {
        private const val UNIQUE_IMMEDIATE_WORK_NAME = "diary_sync_immediate"
        private const val UNIQUE_PERIODIC_WORK_NAME  = "diary_sync_periodic"
    }
}
