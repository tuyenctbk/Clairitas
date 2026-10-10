package com.example.service

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages Firebase Authentication and Google Sign-In via Credential Manager.
 */
class AuthManager private constructor(private val context: Context) {

    private val auth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        Log.w(TAG, "FirebaseAuth initialization failed or unconfigured: ${e.message}")
        null
    }

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth?.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _authStatusMessage = MutableStateFlow<String?>(null)
    val authStatusMessage: StateFlow<String?> = _authStatusMessage.asStateFlow()

    init {
        try {
            auth?.addAuthStateListener { firebaseAuth ->
                _currentUser.value = firebaseAuth.currentUser
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to register auth state listener: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "SiftAuthManager"

        @Volatile
        private var INSTANCE: AuthManager? = null

        fun getInstance(context: Context): AuthManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    /**
     * Initiates Google Sign-In flow using CredentialManager.
     */
    suspend fun signInWithGoogle(
        context: Context,
        webClientId: String = "",
        onComplete: (Boolean, String?) -> Unit
    ) {
        val currentAuth = auth
        if (currentAuth == null) {
            onComplete(false, "Firebase Auth is unconfigured. Please connect Firebase in project settings.")
            return
        }

        if (webClientId.isBlank()) {
            onComplete(false, "Please configure your Google Web Client ID in the app settings")
            return
        }
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val result = credentialManager.getCredential(context = context, request = request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                currentAuth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = currentAuth.currentUser
                            _currentUser.value = user
                            _authStatusMessage.value = "Signed in as ${user?.displayName ?: user?.email}"
                            onComplete(true, "Successfully signed in as ${user?.displayName ?: user?.email}")
                        } else {
                            val err = task.exception?.localizedMessage ?: "Firebase auth failed"
                            _authStatusMessage.value = err
                            onComplete(false, err)
                        }
                    }
            } else {
                onComplete(false, "Unrecognized credential type received")
            }
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Credential Manager error", e)
            // Fallback for demo/emulator environment when Google Play Services credentials API is unconfigured
            val errorMsg = e.message ?: "Sign-In cancelled or Google Play Services unconfigured"
            _authStatusMessage.value = errorMsg
            onComplete(false, errorMsg)
        } catch (e: Exception) {
            Log.e(TAG, "Sign-in exception", e)
            val errorMsg = e.message ?: "An unexpected error occurred during Google Sign-In"
            _authStatusMessage.value = errorMsg
            onComplete(false, errorMsg)
        }
    }

    /**
     * Sign out current Firebase user.
     */
    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.w(TAG, "Sign out error: ${e.message}")
        }
        _currentUser.value = null
        _authStatusMessage.value = "Signed out"
    }

    fun clearStatusMessage() {
        _authStatusMessage.value = null
    }
}
