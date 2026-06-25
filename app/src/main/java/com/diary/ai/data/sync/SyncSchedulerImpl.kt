package com.diary.ai.data.sync

import android.content.Context
import androidx.work.*
import com.diary.ai.domain.repository.SyncScheduler

class SyncSchedulerImpl(private val context: Context) : SyncScheduler {
    override fun scheduleImmediateSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "diary_sync_work",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }
}
