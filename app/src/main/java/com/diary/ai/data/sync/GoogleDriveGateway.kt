package com.diary.ai.data.sync

// ─────────────────────────────────────────────────────────────────────────────
// GoogleDriveGateway — the contract between SyncWorker and the Google Drive API
//
// This interface is intentionally thin. It exposes exactly the 4 operations
// the SyncWorker algorithm requires, modelled directly from Step 4 of the
// tech implementation plan (tech_imp_g_drive.md).
//
// By depending on this interface (not a concrete Drive SDK class), SyncWorker:
//   • remains unit-testable with a fake/mock implementation
//   • is decoupled from the Drive SDK version / auth token management
//   • can be swapped for any cloud backend without changing sync logic
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Lightweight metadata about a file living in the Drive appDataFolder.
 *
 * @property fileId       The Drive-assigned file ID (opaque string).
 * @property fileName     The human-readable file name, e.g. "2026-06-28.json".
 * @property modifiedTime The Drive server-side modification timestamp in Unix
 *                        epoch milliseconds (UTC). This is compared against the
 *                        local Room DB's [NoteDao.getMaxLastModifiedForDate] to
 *                        decide whether to download the file.
 */
data class DriveFileMetadata(
    val fileId: String,
    val fileName: String,
    val modifiedTime: Long  // Unix epoch millis, UTC
)

/**
 * Contract for all Google Drive file operations used by the sync engine.
 *
 * Implementations should:
 *   - Target the hidden **appDataFolder** scope (drive.appdata) so diary files
 *     are invisible to the user in their Drive UI.
 *   - Handle OAuth token refresh transparently (access tokens expire hourly).
 *   - Throw [IOException] or a domain-specific exception on network/auth failure
 *     so SyncWorker can catch it and return [Result.retry()].
 */
interface GoogleDriveGateway {

    /**
     * Returns metadata for the Drive file named [fileName] if it exists,
     * or null if the file has never been uploaded for this date.
     *
     * [fileName] format: "YYYY-MM-DD.json" (e.g. "2026-06-28.json")
     *
     * Used in Phase A (upload check): if null is returned, we always upload.
     * Used in Phase B (pull check):   [DriveFileMetadata.modifiedTime] is
     *                                 compared against the local DB max timestamp.
     */
    suspend fun getFileMetadata(fileName: String): DriveFileMetadata?

    /**
     * Creates or fully overwrites the Drive file named [fileName] with the
     * serialized [jsonContent] string.
     *
     * The file-per-date strategy means this is always a complete snapshot —
     * it replaces the entire previous file, not a partial patch.
     *
     * [fileName]    format: "YYYY-MM-DD.json"
     * [jsonContent] format: serialized [DailyFilePayload] JSON
     */
    suspend fun uploadDayFile(fileName: String, jsonContent: String)

    /**
     * Downloads the raw JSON content of the Drive file named [fileName].
     *
     * [fileName] format: "YYYY-MM-DD.json"
     *
     * @return The raw JSON string, ready to be deserialized into [DailyFilePayload].
     * @throws IOException if the file does not exist or the download fails.
     */
    suspend fun downloadDayFile(fileName: String): String

    /**
     * Lists metadata for all YYYY-MM-DD.json files in the appDataFolder.
     *
     * Used in two scenarios:
     *  1. Phase B of every periodic sync: discover which cloud dates are newer.
     *  2. Initial sync on a new device:   discover all historical dates to pull.
     *
     * @return An unsorted list of metadata. The caller is responsible for
     *         ordering/filtering as needed.
     */
    suspend fun listAllDayFiles(): List<DriveFileMetadata>
}
