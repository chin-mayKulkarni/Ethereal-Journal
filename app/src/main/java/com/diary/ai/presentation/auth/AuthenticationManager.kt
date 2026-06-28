package com.diary.ai.presentation.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthenticationManager(private val context: Context) {
    private val credentialManager = CredentialManager.create(context)
    private val firebaseAuth = FirebaseAuth.getInstance()

    // OAuth Web Client ID from google-services.json
    private val webClientId = "802091427032-sa7bi7og9797mou130hp9gqh5vluau7c.apps.googleusercontent.com"

    fun performGoogleSignIn(
        coroutineScope: CoroutineScope,
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
                onResult(false, "No Google accounts found in device Settings. Please sign in to a Google account in your system Settings.")
            } catch (e: Exception) {
                e.printStackTrace()
                val msg = e.localizedMessage ?: "Unknown error"
                val descriptiveError = when {
                    msg.contains("developer", ignoreCase = true) || msg.contains("10") -> {
                        "Developer configuration mismatch (API code 10). Please verify your debug SHA-1 signature matches your Firebase Console settings."
                    }
                    else -> "Google Sign-In failed: $msg"
                }
                onResult(false, descriptiveError)
            }
        }
    }

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
}
