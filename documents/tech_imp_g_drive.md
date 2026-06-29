📑 Technical Implementation Plan: Offline-First Diary Sync with Google Drive
1. System Architecture Overview
This app utilizes an Offline-First Architectural Pattern using a Single Source of Truth (SSOT).

Local Storage: Room DB handles all reads/writes instantly.

Sync Engine: WorkManager manages intermittent, constrained background updates.

Cloud Storage: Google Drive API (appdata space) saves diary entries structured in a File-per-Date (YYYY-MM-DD.json) strategy.

2. Step-by-Step Implementation Steps
Step 1: Dependencies & Authorization Configuration
Gradle Dependencies: Add Room, WorkManager, Firebase Auth, Jetpack Credential Manager, and the Google Drive REST API Android client libraries.

OAuth Scopes: In the Google Cloud Console, add the https://www.googleapis.com/auth/drive.appdata scope to your OAuth 2.0 Client ID.

Firebase Configuration: Enable Google Sign-In within the Firebase Authentication console.

Step 2: Database & Local Schema Design (Room)
Entity (DiaryEntry): Represents an individual block text.

Fields: id (UUID String), dateString (String, format YYYY-MM-DD), textContent (String), timestamp (Long), lastUpdated (Long), syncStatus (Enum: SYNCED, NOT_SYNCED).

DAO (DiaryDao): * Provide normal CRUD methods.

Provide a method to query entries by date: getEntriesByDate(date: String): Flow<List<DiaryEntry>>.

Provide a sync query: getUnsyncedEntries(): List<DiaryEntry>.

Provide an upsert/replace transaction method to handle incoming updates from the cloud.

Step 3: Authentication & Token Management
Credential Manager Integration: Implement the Sign-In flow requesting both the Firebase ID Token and the Google Drive Access Token/Auth Code.

Token Lifecycle: Securely cache the Google OAuth Access Token. Since access tokens expire hourly, implement a mechanism (or use Firebase/Google account refresh flows) to retrieve a valid token inside the background WorkManager.

Step 4: Google Drive API Gateway/Service
Create a helper class (e.g., GoogleDriveService) using the Google Drive REST client to handle isolated file tasks inside the hidden application folder (appDataFolder):

getFileMetadata(fileName: String): DriveFileMetadata? (Checks if a date's file exists and fetches its cloud modification timestamp).

uploadDayFile(fileName: String, jsonContent: String) (Creates or overwrites the specific date file).

downloadDayFile(fileName: String): String (Downloads the file contents as raw JSON text).

listAllDayFiles(): List<DriveFileMetadata> (Used during initial sync on a new device to discover what history exists).

Step 5: Background Synchronization Engine (WorkManager)
Create SyncWorker (Extends CoroutineWorker):

Phase A: Upload local modifications. Fetch all entries with NOT_SYNCED. Group them by dateString. For each date, check the cloud's modified timestamp. If the local version is newer (or cloud file doesn't exist), merge and upload the JSON, then update the local Room rows to SYNCED.

Phase B: Pull cloud changes (Two-Way Sync). For the current week/month (or based on altered metadata discovered from listAllDayFiles), check if the cloud's file modification timestamp is newer than the local database's max lastUpdated timestamp for that date. If the cloud is newer, download the JSON, parse it, and upsert it into Room.

Constraints: Configure the PeriodicWorkRequest to execute only when NetworkType.CONNECTED is satisfied.

Step 6: Initial Data Sync (New Device Login)
On a successful authentication event, if Room is detected to be empty, enqueue a one-time high-priority InitialSyncWorker.

This worker calls listAllDayFiles() from Drive, loops through all historical files, downloads them sequentially, parses the payload, and performs a bulk insert into Room DB.