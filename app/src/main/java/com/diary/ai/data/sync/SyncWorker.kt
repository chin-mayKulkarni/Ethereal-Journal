package com.diary.ai.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.diary.ai.data.local.NoteDatabase
import com.diary.ai.data.local.NoteEntity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// ─────────────────────────────────────────────────────────────────────────────
// SyncWorker — Two-Phase Offline-First Reconciliation Engine
//
// Architecture context:
//   • Extends CoroutineWorker so all DB and network work runs on coroutines,
//     never blocking the main thread.
//   • Runs under WorkManager with a NetworkType.CONNECTED constraint
//     (enforced in SyncSchedulerImpl) — doWork() is only called online.
//   • Uses LWW (Last Write Wins) conflict resolution keyed on lastModified
//     Unix epoch timestamps.
//   • Depends on GoogleDriveGateway (interface, not a concrete Drive SDK class)
//     keeping this class testable in isolation.
//
// Sync algorithm overview:
//   Phase A — Upload local modifications to Google Drive
//   Phase B — Pull cloud changes that are newer than local data
// ─────────────────────────────────────────────────────────────────────────────

private const val TAG = "SyncWorker"

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    // ── Dependencies ──────────────────────────────────────────────────────────

    private val database: NoteDatabase by lazy {
        NoteDatabase.getDatabase(applicationContext)
    }

    /**
     * The Drive gateway is resolved via a static factory so that:
     *  • Tests can swap in a [FakeGoogleDriveGateway] before the worker runs.
     *  • The worker itself has no coupling to the Drive SDK, credentials, or
     *    token management — that complexity lives in the concrete implementation.
     *
     * NOTE: Replace [GoogleDriveGatewayHolder.instance] assignment with your
     * production [GoogleDriveService] implementation once it is built.
     */
    private val driveGateway: GoogleDriveGateway
        get() = GoogleDriveGatewayHolder.instance
            ?: throw IllegalStateException(
                "GoogleDriveGateway not initialized. " +
                "Set GoogleDriveGatewayHolder.instance before enqueuing SyncWorker."
            )

    // ── Serializer ────────────────────────────────────────────────────────────

    /**
     * Lenient JSON parser: ignores unknown keys so that a Drive file written by
     * a newer app version can still be parsed by an older client.
     */
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false   // compact for smaller Drive files
    }

    // ─────────────────────────────────────────────────────────────────────────
    // doWork()
    // ─────────────────────────────────────────────────────────────────────────

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val noteDao = database.noteDao()

        // Resolve the authenticated user ID from Firebase.
        // If the user is not signed in, there is nothing to sync — we return
        // success (not retry) so WorkManager doesn't keep retrying needlessly.
        val userId = try {
            FirebaseAuth.getInstance().currentUser?.uid
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseAuth unavailable: ${e.message}")
            null
        }

        if (userId == null) {
            Log.i(TAG, "No authenticated user — skipping sync.")
            return@withContext Result.success()
        }

        return@withContext try {
            // IMPORTANT: Phase B (pull) runs BEFORE Phase A (upload).
            //
            // Why this order matters — multi-device conflict scenario:
            //   Device A writes entry X and uploads to Drive.
            //   Device B logs in, starts typing entry Y before sync runs.
            //   Device B triggers SyncWorker.
            //
            //   OLD order (A then B): Device B uploads only entry Y first,
            //   overwriting Drive and permanently deleting entry X. Then
            //   Phase B pulls — but now Drive only has Y, so X is lost forever.
            //
            //   CORRECT order (B then A): Device B first pulls entry X from Drive
            //   into its local Room DB. Then Phase A uploads a merged snapshot
            //   containing BOTH X and Y — no data is lost.
            phaseB_pullCloudChanges(noteDao, userId)
            phaseA_uploadLocalModifications(noteDao, userId)
            Log.i(TAG, "Sync completed successfully for user $userId")
            Result.success()
        } catch (e: Exception) {
            // Any network or Drive API error triggers a retry with exponential
            // back-off (WorkManager's default retry policy).
            Log.e(TAG, "Sync failed — will retry. Error: ${e.message}", e)
            Result.retry()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Phase A — Upload Local Modifications
    //
    // Strategy: group all pending (not-yet-synced) entries by their dateString,
    // then upload a complete daily snapshot for each affected date.
    //
    // Why upload the full day snapshot, not just the pending entries?
    //   The Drive file is a single JSON blob representing ALL entries for that
    //   date. Uploading only the changed entries would delete the previously
    //   synced entries for that day. A full snapshot guarantees correctness.
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun phaseA_uploadLocalModifications(
        noteDao: com.diary.ai.data.local.NoteDao,
        userId: String
    ) {
        // Step A1: Fetch all entries whose sync_status != SYNCHRONIZED.
        val pendingNotes = noteDao.getPendingSyncNotes(userId)

        if (pendingNotes.isEmpty()) {
            Log.d(TAG, "Phase A: no pending local modifications.")
            return
        }

        // Step A2: Group by dateString — each group maps to one Drive file.
        val notesByDate: Map<String, List<NoteEntity>> = pendingNotes.groupBy { it.dateString }
        Log.d(TAG, "Phase A: ${pendingNotes.size} pending notes across ${notesByDate.size} date(s).")

        for ((dateString, _) in notesByDate) {
            val fileName = "$dateString.json"

            // Step A3: Check if a cloud file already exists for this date and
            //          compare the cloud's modification time vs local max timestamp.
            val cloudMeta: DriveFileMetadata? = driveGateway.getFileMetadata(fileName)
            val localMaxTimestamp: Long = noteDao.getMaxLastModifiedForDate(dateString, userId) ?: 0L

            val shouldUpload = when {
                // Cloud file doesn't exist yet — always upload.
                cloudMeta == null -> true
                // Local has changes that are newer than the cloud file — upload.
                // (LWW: if local lastModified > cloud modifiedTime, local wins.)
                localMaxTimestamp > cloudMeta.modifiedTime -> true
                // Cloud is equally fresh or newer — skip upload to avoid
                // overwriting cloud changes. Phase B will reconcile the pull.
                else -> false
            }

            if (!shouldUpload) {
                Log.d(TAG, "Phase A [$dateString]: cloud is up-to-date, skipping upload.")
                continue
            }

            // Step A4: Fetch ALL local entries for this date (not just pending)
            //          to build a complete daily snapshot.
            val allEntriesForDate: List<NoteEntity> = noteDao.getNotesByDateList(dateString, userId)

            // Step A5: Convert to DailyFilePayload and serialize to JSON.
            val payload = DailyFilePayload(
                dateString = dateString,
                // Top-level lastUpdated = max of all entry timestamps for fast comparison.
                lastUpdated = allEntriesForDate.maxOf { it.lastModified },
                entries = allEntriesForDate.map { entity ->
                    NoteEntryItem(
                        id = entity.id,
                        content = entity.content,
                        mediaType = entity.mediaType,
                        mediaPath = entity.mediaPath,
                        lastModified = entity.lastModified,
                        syncStatus = "SYNCHRONIZED"
                    )
                }
            )
            val jsonContent: String = json.encodeToString(payload)

            // Step A6: Upload to Drive — creates the file if absent, overwrites
            //          it if present. Drive handles deduplication via fileId.
            driveGateway.uploadDayFile(fileName, jsonContent)
            Log.i(TAG, "Phase A [$dateString]: uploaded ${payload.entries.size} entries.")

            // Step A7: Mark all local entries for this date as SYNCHRONIZED.
            //          We update every entry (not just pending ones) so the
            //          status accurately reflects what was just uploaded.
            noteDao.upsertFromCloud(
                allEntriesForDate.map { it.copy(syncStatus = "SYNCHRONIZED") }
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Phase B — Pull Cloud Changes (Two-Way Sync)
    //
    // Strategy: list all Drive files, and for each one where the cloud
    // modification timestamp is newer than our local data, download and upsert.
    //
    // This also handles the initial sync case on a new device:
    //   — getMaxLastModifiedForDate() returns null for dates with no local data.
    //   — null is treated as "local has nothing" → always download.
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun phaseB_pullCloudChanges(
        noteDao: com.diary.ai.data.local.NoteDao,
        userId: String
    ) {
        // Step B1: Discover all date-files on Drive for this user's account.
        val cloudFiles: List<DriveFileMetadata> = driveGateway.listAllDayFiles()

        if (cloudFiles.isEmpty()) {
            Log.d(TAG, "Phase B: no cloud files found.")
            return
        }

        Log.d(TAG, "Phase B: checking ${cloudFiles.size} cloud file(s) for newer data.")

        for (cloudMeta in cloudFiles) {
            // Drive files are named "YYYY-MM-DD.json" — strip the extension.
            val dateString = cloudMeta.fileName.removeSuffix(".json")

            // Step B2: Compare cloud modifiedTime vs local max lastModified.
            val localMaxTimestamp: Long? = noteDao.getMaxLastModifiedForDate(dateString, userId)

            val cloudIsNewer = when {
                // No local data for this date → always download.
                localMaxTimestamp == null -> true
                // Cloud file was modified more recently than our most recent local entry.
                cloudMeta.modifiedTime > localMaxTimestamp -> true
                // Local data is equally fresh or newer — Phase A already handled upload.
                else -> false
            }

            if (!cloudIsNewer) {
                Log.d(TAG, "Phase B [$dateString]: local data is current, skipping download.")
                continue
            }

            // Step B3: Download the full JSON file content from Drive.
            val rawJson: String = driveGateway.downloadDayFile(cloudMeta.fileName)

            // Step B4: Deserialize into our DTO.
            val payload: DailyFilePayload = json.decodeFromString(rawJson)

            // Step B5: Convert DTO entries → NoteEntity rows, re-attaching the
            //          userId (which is NOT stored in the Drive file payload).
            //          All incoming entries are stamped SYNCHRONIZED.
            val entitiesToUpsert: List<NoteEntity> = payload.entries.map { item ->
                NoteEntity(
                    id = item.id,
                    userId = userId,                   // re-attach from auth context
                    dateString = payload.dateString,
                    content = item.content,
                    mediaType = item.mediaType,
                    mediaPath = item.mediaPath,
                    syncStatus = "SYNCHRONIZED",       // came from cloud — already synced
                    lastModified = item.lastModified
                )
            }

            // Step B6: Atomically bulk-upsert into Room using the @Transaction method.
            //          If any single insert fails, the whole batch is rolled back,
            //          preventing the local DB from being partially updated.
            noteDao.upsertFromCloud(entitiesToUpsert)
            Log.i(TAG, "Phase B [$dateString]: upserted ${entitiesToUpsert.size} entries from cloud.")
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// GoogleDriveGatewayHolder
//
// A simple static holder that lets the production app (DiaryApplication.onCreate)
// or test setup inject a concrete GoogleDriveGateway before WorkManager runs.
//
// Usage in production (DiaryApplication.kt):
//   GoogleDriveGatewayHolder.instance = GoogleDriveService(context, tokenProvider)
//
// Usage in unit tests:
//   GoogleDriveGatewayHolder.instance = FakeGoogleDriveGateway()
//
// This lightweight pattern avoids full DI framework overhead while keeping
// SyncWorker decoupled from the Drive SDK.
// ─────────────────────────────────────────────────────────────────────────────

object GoogleDriveGatewayHolder {
    @Volatile
    var instance: GoogleDriveGateway? = null
}
