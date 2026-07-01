package com.diary.ai.data.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
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
     * NOTE: This uses the Identity Authorization API directly, which works
     * with both Credential Manager and legacy GoogleSignIn flows. It does NOT
     * depend on GoogleSignIn.getLastSignedInAccount().
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

        // Use the Identity Authorization API to obtain/refresh the token.
        // This works regardless of whether the user signed in via Credential
        // Manager or the legacy GoogleSignIn API, as long as the drive.appdata
        // scope was granted during sign-in.
        return@withContext try {
            val driveScope = Scope(DRIVE_APPDATA_SCOPE)

            val authorizationRequest = AuthorizationRequest
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
            } else {
                Log.w(TAG, "Identity Authorization API returned null access token — Drive scope may not be granted")
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
