package com.diary.ai.data.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

// ─────────────────────────────────────────────────────────────────────────────
// DriveAuthProvider
//
// Manages the OAuth2 access token scoped to https://www.googleapis.com/auth/drive.appdata.
//
// KEY CONCEPT — Firebase ID token ≠ Drive access token:
//   • Firebase Sign-In gives you a Firebase ID token (for Firestore, auth, etc.)
//   • To call Google Drive REST APIs, you need a separate OAuth2 ACCESS token
//     with the drive.appdata scope — that is what this class provides.
//
// Token strategy:
//   1. On first call, request authorization via the Identity Authorization API.
//   2. Cache the access token + expiry time in memory.
//   3. On subsequent calls, return the cached token unless it's within
//      TOKEN_EXPIRY_BUFFER_MS of expiry (proactive refresh).
//   4. SyncWorker calls getValidAccessToken() before every Drive API call.
// ─────────────────────────────────────────────────────────────────────────────

private const val TAG = "DriveAuthProvider"

/** Millis before expiry at which we proactively refresh the token (5 minutes). */
private val TOKEN_EXPIRY_BUFFER_MS = TimeUnit.MINUTES.toMillis(5)

/** The Drive appdata OAuth2 scope — gives access only to app-created files. */
private const val DRIVE_APPDATA_SCOPE = "https://www.googleapis.com/auth/drive.appdata"

class DriveAuthProvider(private val context: Context) {

    // In-memory token cache — cleared when the process is killed.
    @Volatile private var cachedToken: String? = null
    @Volatile private var tokenExpiryMs: Long = 0L

    /**
     * Returns a valid Drive API access token, refreshing it if necessary.
     *
     * This method is safe to call from a coroutine on any dispatcher —
     * the actual authorization task is dispatched to [Dispatchers.IO].
     *
     * @return A valid OAuth2 access token string, or null if the user is not
     *         signed in with a Google account that has granted Drive access.
     * @throws Exception if the token refresh fails (caller should handle/retry).
     */
    suspend fun getValidAccessToken(): String? = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()

        // Return cached token if it won't expire within the buffer window.
        val cached = cachedToken
        if (cached != null && tokenExpiryMs - now > TOKEN_EXPIRY_BUFFER_MS) {
            Log.d(TAG, "Returning cached Drive access token (expires in ${(tokenExpiryMs - now) / 1000}s)")
            return@withContext cached
        }

        // Try to obtain a fresh token from the last signed-in Google account.
        return@withContext try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null) {
                Log.w(TAG, "No signed-in Google account found — cannot get Drive token")
                return@withContext null
            }

            // Check if the Drive scope is already authorized for this account.
            val driveScope = Scope(DRIVE_APPDATA_SCOPE)
            if (!GoogleSignIn.hasPermissions(account, driveScope)) {
                // The user hasn't granted Drive access yet.
                // AuthenticationManager.requestDriveAuthorization() must be
                // called from the UI layer first (requires Activity context).
                Log.w(TAG, "Drive appdata scope not yet authorized for this account")
                return@withContext null
            }

            // Use Identity Authorization API to silently refresh the token.
            // This works because the user already granted the scope interactively.
            val authorizationRequest = com.google.android.gms.auth.api.identity.AuthorizationRequest
                .builder()
                .setRequestedScopes(listOf(driveScope))
                .build()

            val result = Identity.getAuthorizationClient(context)
                .authorize(authorizationRequest)
                .await()

            val token = result.accessToken
            if (token != null) {
                // Cache token — Drive access tokens are valid for ~3600 seconds.
                cachedToken = token
                tokenExpiryMs = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)
                Log.i(TAG, "Drive access token refreshed successfully")
            }
            token
        } catch (e: Exception) {
            Log.e(TAG, "Failed to refresh Drive access token: ${e.message}", e)
            // Clear stale cache on failure so the next call tries again.
            cachedToken = null
            tokenExpiryMs = 0L
            throw e
        }
    }

    /**
     * Clears the cached token.
     * Call this on user sign-out so the next sign-in starts fresh.
     */
    fun clearToken() {
        cachedToken = null
        tokenExpiryMs = 0L
        Log.d(TAG, "Drive access token cache cleared")
    }
}
