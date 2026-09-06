package com.kyvo.app.data.auth

import android.content.Context
import android.content.MutableContextWrapper
import androidx.credentials.CustomCredential
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.credentials.CredentialManager
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

data class GoogleIdTokenResult(val idToken: String, val rawNonce: String)

sealed interface GoogleCredentialResult {
    data class Success(val value: GoogleIdTokenResult) : GoogleCredentialResult
    data object Cancelled : GoogleCredentialResult
    data object Unavailable : GoogleCredentialResult
    data class Failure(val cause: Throwable) : GoogleCredentialResult
}

interface GoogleCredentialGateway {
    suspend fun requestIdToken(): GoogleCredentialResult
    suspend fun clearCredentialState()
}

class CredentialManagerGoogleGateway(
    context: Context,
    private val serverClientId: String,
    private val nonceFactory: SecureNonceFactory = SecureNonceFactory(),
) : GoogleCredentialGateway {
    private val credentialContext = MutableContextWrapper(context)
    private val credentialManager = CredentialManager.create(credentialContext)

    override suspend fun requestIdToken(): GoogleCredentialResult {
        if (serverClientId.isBlank()) return GoogleCredentialResult.Unavailable
        val nonce = nonceFactory.create()
        val option = GetSignInWithGoogleOption.Builder(serverClientId)
            .setNonce(nonce.sha256)
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
        return try {
            val response = credentialManager.getCredential(credentialContext, request)
            val credential = response.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                GoogleCredentialResult.Success(GoogleIdTokenResult(googleCredential.idToken, nonce.raw))
            } else {
                GoogleCredentialResult.Failure(IllegalStateException("Unsupported credential type"))
            }
        } catch (_: GetCredentialCancellationException) {
            GoogleCredentialResult.Cancelled
        } catch (_: NoCredentialException) {
            GoogleCredentialResult.Unavailable
        } catch (error: Exception) {
            GoogleCredentialResult.Failure(error)
        }
    }

    override suspend fun clearCredentialState() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
}

data class SecureNonce(val raw: String, val sha256: String)

class SecureNonceFactory(private val secureRandom: SecureRandom = SecureRandom()) {
    fun create(): SecureNonce {
        val bytes = ByteArray(32).also(secureRandom::nextBytes)
        val raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        val hashed = MessageDigest.getInstance("SHA-256")
            .digest(raw.toByteArray(Charsets.UTF_8))
            .joinToString(separator = "") { byte -> "%02x".format(byte.toInt() and 0xff) }
        return SecureNonce(raw = raw, sha256 = hashed)
    }
}
