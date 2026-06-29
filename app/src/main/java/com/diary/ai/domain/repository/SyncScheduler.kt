package com.diary.ai.domain.repository

interface SyncScheduler {
    /** Enqueues a one-time sync immediately (used after saving a note). */
    fun scheduleImmediateSync()

    /**
     * Enqueues a periodic background sync that fires at most every
     * [WorkManager.MIN_PERIODIC_INTERVAL_MILLIS] (15 minutes), subject to
     * a [NetworkType.CONNECTED] constraint.
     *
     * Safe to call multiple times — uses [ExistingPeriodicWorkPolicy.KEEP]
     * so a running periodic job is never cancelled by re-registration.
     */
    fun schedulePeriodicSync()
}
