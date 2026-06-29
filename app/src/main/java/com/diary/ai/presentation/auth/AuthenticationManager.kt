package com.diary.ai.presentation.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "AuthenticationManager"
private const val DRIVE_APPDATA_SCOPE = "https://www.googleapis.com/auth/drive.appdata"

class AuthenticationManager(private val context: Context) {
    private val credentialManager = CredentialManager.create(context)
    private val firebaseAuth = FirebaseAuth.getInstance()

    // OAuth Web Client ID from google-services.json
    private val webClientId = "802091427032-sa7bi7og9797mou130hp9gqh5vluau7c.apps.googleusercontent.com"

    // ─────────────────────────────────────────────────────────────────────────
    // Step 1 — Google Sign-In + Firebase Auth
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Performs Google Sign-In via Credential Manager and authenticates with Firebase.
     *
     * After Firebase auth succeeds, immediately calls [requestDriveAuthorization]
     * to request the drive.appdata scope. If the scope was previously granted,
     * this is a silent no-op. If not, the user sees a one-time consent dialog.
     *
     * @param coroutineScope Scope tied to the calling screen's lifecycle.
     * @param activity       Required for the Drive authorization UI (if needed).
     * @param onResult       Callback: (success: Boolean, errorMessage: String?)
     */
    fun performGoogleSignIn(
        coroutineScope: CoroutineScope,
        activity: Activity,
        onResult: (Boolean, String?) -> Unit
    ) {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        coroutineScope.launch {
            try {
                // Request credentials via Credential Manager bottom sheet
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken

                        // Authenticate with Firebase using the Google ID Token
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        firebaseAuth.signInWithCredential(firebaseCredential).await()

                        Log.i(TAG, "Firebase sign-in successful — requesting Drive authorization")

                        // Request Drive scope after Firebase auth.
                        // On first run: shows a one-time consent dialog.
                        // On subsequent runs: silently resolves if already granted.
                        requestDriveAuthorization(activity)

                        onResult(true, null)

                    } catch (e: GoogleIdTokenParsingException) {
                        onResult(false, "Failed to parse Google ID Token: ${e.localizedMessage}")
                    }
                } else {
                    onResult(false, "Unexpected credential type returned: ${credential.type}")
                }
            } catch (e: GetCredentialCancellationException) {
                onResult(false, "Sign-In cancelled by user")
            } catch (e: NoCredentialException) {
                onResult(false, "No Google accounts found. Please add a Google account in Settings.")
            } catch (e: Exception) {
                e.printStackTrace()
                val msg = e.localizedMessage ?: "Unknown error"
                val descriptiveError = when {
                    msg.contains("developer", ignoreCase = true) || msg.contains("10") ->
                        "Developer configuration mismatch (API code 10). Verify your debug SHA-1 in Firebase Console."
                    else -> "Google Sign-In failed: $msg"
                }
                onResult(false, descriptiveError)
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Step 2 — Drive Scope Authorization
    //
    // This is separate from Firebase Sign-In because:
    //   • Firebase Auth uses a Google ID Token (for identity verification)
    //   • Drive API calls require an OAuth2 Access Token with drive.appdata scope
    //   These are two distinct credentials serving different purposes.
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Requests the drive.appdata OAuth2 scope via the Identity Authorization API.
     *
     * Behavior:
     *   • If the scope is already granted: resolves silently with no UI shown.
     *   • If not yet granted: triggers a consent dialog via the Activity's
     *     result contract (no startActivityForResult — uses pending intent).
     *
     * This method is safe to call on every sign-in — it is idempotent once
     * the scope is granted.
     *
     * @param activity Required to launch the authorization UI if needed.
     */
    suspend fun requestDriveAuthorization(activity: Activity) {
        val driveScope = Scope(DRIVE_APPDATA_SCOPE)
        val currentAccount = GoogleSignIn.getLastSignedInAccount(context)

        // If the scope is already granted for the current account, nothing to do.
        if (currentAccount != null && GoogleSignIn.hasPermissions(currentAccount, driveScope)) {
            Log.i(TAG, "Drive appdata scope already authorized — no dialog needed")
            return
        }

        try {
            val authorizationRequest = AuthorizationRequest.builder()
                .setRequestedScopes(listOf(driveScope))
                .build()

            val result = Identity.getAuthorizationClient(activity)
                .authorize(authorizationRequest)
                .await()

            if (result.hasResolution()) {
                // User needs to approve the scope in a dialog.
                // Launch the pending intent from the Activity.
                Log.i(TAG, "Drive authorization requires user consent — launching dialog")
                result.pendingIntent?.let { pendingIntent ->
                    activity.startIntentSenderForResult(
                        pendingIntent.intentSender,
                        DRIVE_AUTH_REQUEST_CODE,
                        null, 0, 0, 0
                    )
                }
            } else {
                // Scope already granted — token is available silently.
                Log.i(TAG, "Drive appdata scope granted silently")
            }
        } catch (e: ApiException) {
            // Non-fatal — the app works offline; sync will retry when scope is granted.
            Log.w(TAG, "Drive authorization failed (API ${e.statusCode}): ${e.message}")
        } catch (e: Exception) {
            Log.w(TAG, "Drive authorization failed: ${e.message}")
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sign Out
    // ─────────────────────────────────────────────────────────────────────────

    fun signOutUser(
        coroutineScope: CoroutineScope,
        onComplete: () -> Unit = {}
    ) {
        // Sign out from Firebase
        firebaseAuth.signOut()

        // Clear Credential Manager session state
        coroutineScope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            onComplete()
        }
    }

    companion object {
        /** Request code for the Drive authorization pending intent. */
        const val DRIVE_AUTH_REQUEST_CODE = 9001
    }
}
