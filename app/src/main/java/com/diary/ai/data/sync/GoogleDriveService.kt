package com.diary.ai.data.sync

import android.content.Context
import android.util.Log
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

// ─────────────────────────────────────────────────────────────────────────────
// GoogleDriveService — concrete implementation of GoogleDriveGateway
//
// Uses the Google Drive REST API v3 via the google-api-services-drive Android
// client library. All files are stored in the hidden "appDataFolder" space
// (drive.appdata scope), which is:
//   • Invisible to the user in their Google Drive UI
//   • Automatically deleted when the user uninstalls the app
//   • Sandboxed — other apps cannot access it
//
// File naming convention: "YYYY-MM-DD.json" (one file per calendar day)
// ─────────────────────────────────────────────────────────────────────────────

private const val TAG = "GoogleDriveService"
private const val APP_DATA_FOLDER = "appDataFolder"
private const val MIME_JSON = "application/json"

// Fields requested in list operations — only what we need for sync decisions.
// Fetching minimal fields reduces network payload and quota usage.
private const val LIST_FIELDS = "files(id,name,modifiedTime)"

class GoogleDriveService(
    private val context: Context,
    private val driveAuthProvider: DriveAuthProvider
) : GoogleDriveGateway {

    // ── Drive client builder ──────────────────────────────────────────────────

    /**
     * Builds a Drive client authenticated with the access token obtained from
     * [DriveAuthProvider]. Called fresh before each API call to ensure the token
     * is always current (DriveAuthProvider handles caching/refresh).
     *
     * NOTE: This intentionally does NOT use the legacy GoogleSignIn API.
     * The app authenticates via Credential Manager, which does not populate
     * GoogleSignIn.getLastSignedInAccount(). Instead, DriveAuthProvider obtains
     * an OAuth2 access token via the Identity Authorization API and we inject
     * it into HTTP request headers directly.
     *
     * @return Authenticated Drive v3 client.
     * @throws IllegalStateException if the user is not signed in or the token
     *         cannot be obtained.
     */
    private suspend fun buildDriveClient(): Drive {
        val accessToken = driveAuthProvider.getValidAccessToken()
            ?: throw IllegalStateException(
                "Cannot obtain Drive access token — user not signed in or Drive scope not granted"
            )

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance()
        ) { request ->
            request.headers.authorization = "Bearer $accessToken"
        }
            .setApplicationName("EtherealJournal")
            .build()
    }

    // ── GoogleDriveGateway implementation ────────────────────────────────────

    /**
     * Returns metadata for the Drive file named [fileName] if it exists,
     * or null if no such file has been uploaded yet.
     *
     * Searches by exact name in the appDataFolder using the Drive Files.list API.
     * Returns the first match (there should only ever be one per date).
     */
    override suspend fun getFileMetadata(fileName: String): DriveFileMetadata? =
        withContext(Dispatchers.IO) {
            val drive = buildDriveClient()

            try {
                val fileList: FileList = drive.files().list()
                    .setSpaces(APP_DATA_FOLDER)
                    // Filter to exact file name — prevents false matches on similar names
                    .setQ("name = '$fileName' and trashed = false")
                    .setFields(LIST_FIELDS)
                    .execute()

                val file = fileList.files.firstOrNull() ?: run {
                    Log.d(TAG, "getFileMetadata: '$fileName' not found in Drive")
                    return@withContext null
                }

                DriveFileMetadata(
                    fileId = file.id,
                    fileName = file.name,
                    // Drive returns ISO-8601; convert to Unix epoch millis for LWW comparison
                    modifiedTime = file.modifiedTime?.value ?: 0L
                ).also {
                    Log.d(TAG, "getFileMetadata: found '$fileName' modifiedTime=${it.modifiedTime}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "getFileMetadata failed for '$fileName': ${e.message}", e)
                throw e
            }
        }

    /**
     * Creates a new Drive file or overwrites an existing one with [jsonContent].
     *
     * Uses a two-step approach:
     *   1. Check if the file already exists (getFileMetadata).
     *   2a. If it exists → Files.update() (PATCH) with the new content.
     *   2b. If it doesn't exist → Files.create() (POST) into appDataFolder.
     *
     * This avoids duplicate files accumulating for the same date.
     */
    override suspend fun uploadDayFile(fileName: String, jsonContent: String) {
        withContext(Dispatchers.IO) {
            val drive = buildDriveClient()

            val contentBytes = jsonContent.toByteArray(Charsets.UTF_8)
            val mediaContent = ByteArrayContent(MIME_JSON, contentBytes)

            try {
                val existing = getFileMetadata(fileName)

                if (existing != null) {
                    // File exists — update content in place (keeps same fileId)
                    drive.files().update(existing.fileId, null, mediaContent)
                        .execute()
                    Log.i(TAG, "uploadDayFile: updated '$fileName' (${contentBytes.size} bytes)")
                } else {
                    // File doesn't exist yet — create it in appDataFolder
                    val fileMetadata = File().apply {
                        name = fileName
                        parents = listOf(APP_DATA_FOLDER)
                        mimeType = MIME_JSON
                    }
                    drive.files().create(fileMetadata, mediaContent)
                        .setFields("id,name")
                        .execute()
                    Log.i(TAG, "uploadDayFile: created '$fileName' (${contentBytes.size} bytes)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "uploadDayFile failed for '$fileName': ${e.message}", e)
                throw e
            }
        }
    }

    /**
     * Downloads and returns the raw JSON content of the Drive file named [fileName].
     *
     * Uses Files.get() with alt=media to stream the file content directly.
     * The content is read into a ByteArrayOutputStream and decoded as UTF-8.
     *
     * @throws Exception if the file is not found or the download fails.
     */
    override suspend fun downloadDayFile(fileName: String): String =
        withContext(Dispatchers.IO) {
            val drive = buildDriveClient()
                ?: throw IllegalStateException("Cannot build Drive client — user not signed in")

            try {
                // First resolve the fileId from the fileName
                val meta = getFileMetadata(fileName)
                    ?: throw NoSuchFileException(java.io.File(fileName), reason = "File not found in Drive appDataFolder")

                val outputStream = ByteArrayOutputStream()
                drive.files().get(meta.fileId)
                    .executeMediaAndDownloadTo(outputStream)

                val content = outputStream.toString(Charsets.UTF_8.name())
                Log.i(TAG, "downloadDayFile: downloaded '$fileName' (${content.length} chars)")
                content
            } catch (e: Exception) {
                Log.e(TAG, "downloadDayFile failed for '$fileName': ${e.message}", e)
                throw e
            }
        }

    /**
     * Lists metadata for all YYYY-MM-DD.json files in the appDataFolder.
     *
     * Handles Drive API pagination automatically — keeps fetching pages
     * until nextPageToken is null, ensuring all historical dates are returned.
     *
     * @return All files found, unsorted. Returns an empty list if none exist.
     */
    override suspend fun listAllDayFiles(): List<DriveFileMetadata> =
        withContext(Dispatchers.IO) {
            val drive = buildDriveClient()

            val results = mutableListOf<DriveFileMetadata>()
            var pageToken: String? = null

            try {
                do {
                    val listRequest = drive.files().list()
                        .setSpaces(APP_DATA_FOLDER)
                        // Match only our date-named JSON files — avoids unrelated app data
                        .setQ("mimeType = '$MIME_JSON' and trashed = false")
                        .setFields("nextPageToken,$LIST_FIELDS")
                        .setPageSize(100) // Max allowed by Drive API

                    if (pageToken != null) {
                        listRequest.pageToken = pageToken
                    }

                    val page: FileList = listRequest.execute()

                    page.files?.forEach { file ->
                        results += DriveFileMetadata(
                            fileId = file.id,
                            fileName = file.name,
                            modifiedTime = file.modifiedTime?.value ?: 0L
                        )
                    }

                    pageToken = page.nextPageToken
                } while (pageToken != null)

                Log.i(TAG, "listAllDayFiles: found ${results.size} file(s) in appDataFolder")
                results
            } catch (e: Exception) {
                Log.e(TAG, "listAllDayFiles failed: ${e.message}", e)
                throw e
            }
        }
}
