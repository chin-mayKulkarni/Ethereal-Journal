package com.diary.ai

import android.app.Application
import android.util.Log
import com.diary.ai.data.sync.GoogleDriveGatewayHolder
import com.diary.ai.di.AppContainer
import com.diary.ai.di.AppContainerImpl
import com.google.firebase.auth.FirebaseAuth

class DiaryApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()

        // Initialize the manual DI container.
        container = AppContainerImpl(this)

        // Wire the concrete GoogleDriveGateway implementation into the static
        // holder so that SyncWorker can resolve it when WorkManager dispatches it.
        // SyncWorker runs in a separate process context and cannot access the
        // AppContainer directly — the static holder bridges this gap cleanly.
        GoogleDriveGatewayHolder.instance = container.googleDriveService
        Log.i("DiaryApplication", "GoogleDriveGateway wired to GoogleDriveService")

        // Start the periodic background sync.
        // KEEP policy means this is idempotent — safe to call on every app launch.
        // The sync will only fire when NetworkType.CONNECTED is satisfied.
        // If the user is not yet signed in, SyncWorker.doWork() handles this
        // gracefully by returning Result.success() immediately (no-op).
        container.syncScheduler.schedulePeriodicSync()
        Log.i("DiaryApplication", "Periodic sync scheduled (15-min interval, network-constrained)")

        // Observe auth state changes to clear Drive token cache on sign-out.
        // This prevents a signed-out user's token from being used in a residual
        // WorkManager task that fires after sign-out.
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            if (auth.currentUser == null) {
                container.driveAuthProvider.clearToken()
                Log.i("DiaryApplication", "User signed out — Drive token cache cleared")
            }
        }
    }
}
