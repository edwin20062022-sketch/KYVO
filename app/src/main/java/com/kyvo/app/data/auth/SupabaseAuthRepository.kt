package com.kyvo.app.data.auth

import com.kyvo.app.domain.auth.AuthError
import com.kyvo.app.domain.auth.AuthRepository
import com.kyvo.app.domain.auth.AuthResult
import com.kyvo.app.domain.auth.AuthSession
import com.kyvo.app.domain.auth.AuthState
import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

class SupabaseAuthRepository(
    private val dataSource: SupabaseAuthDataSource,
    private val googleCredentials: GoogleCredentialGateway,
) : AuthRepository {
    override fun observeSession(): Flow<AuthState> = dataSource.observeSession()
    override fun currentSession(): AuthSession? = dataSource.currentSession()

    override suspend fun signUpWithEmail(email: String, password: CharArray): AuthResult =
        withPassword(password) { passwordValue ->
            val session = dataSource.signUpWithEmail(email, passwordValue)
            if (session == null) AuthResult.ConfirmationRequired(email) else AuthResult.Success(session)
        }

    override suspend fun signInWithEmail(email: String, password: CharArray): AuthResult =
        withPassword(password) { passwordValue ->
            AuthResult.Success(dataSource.signInWithEmail(email, passwordValue))
        }

    override suspend fun signInWithGoogle(): AuthResult = when (val credential = googleCredentials.requestIdToken()) {
        GoogleCredentialResult.Cancelled -> AuthResult.Failure(AuthError.Cancelled)
        GoogleCredentialResult.Unavailable -> AuthResult.Failure(AuthError.ConfigurationRequired)
        is GoogleCredentialResult.Failure -> AuthResult.Failure(credential.cause.toAuthError())
        is GoogleCredentialResult.Success -> runAuthRequest {
            AuthResult.Success(
                dataSource.signInWithGoogle(
                    idToken = credential.value.idToken,
                    rawNonce = credential.value.rawNonce,
                ),
            )
        }
    }

    override suspend fun updateProfileMetadata(displayName: String, username: String?): AuthResult =
        runAuthRequest { AuthResult.Success(dataSource.updateProfileMetadata(displayName, username)) }

    override suspend fun updateAvatarBytes(bytes: ByteArray): AuthResult =
        runAuthRequest { AuthResult.Success(dataSource.updateAvatar(bytes)) }

    override suspend fun deleteAvatar(): AuthResult =
        runAuthRequest { AuthResult.Success(dataSource.deleteAvatar()) }

    override suspend fun updatePassword(newPassword: CharArray): AuthResult =
        withPassword(newPassword) { passwordValue ->
            runAuthRequest { AuthResult.Success(dataSource.updatePassword(passwordValue)) }
        }

    override suspend fun signOut() {
        dataSource.signOut()
        runCatching { googleCredentials.clearCredentialState() }
    }

    override suspend fun deleteAccount(): AuthResult = runAuthRequest {
        val session = requireNotNull(currentSession()) { "No authenticated session" }
        dataSource.deleteAccount()
        runCatching { dataSource.signOut() }
        runCatching { googleCredentials.clearCredentialState() }
        AuthResult.Success(session)
    }

    private suspend fun withPassword(
        password: CharArray,
        operation: suspend (String) -> AuthResult,
    ): AuthResult = try {
        runAuthRequest { operation(String(password)) }
    } finally {
        password.fill('\u0000')
    }
}

private suspend fun runAuthRequest(operation: suspend () -> AuthResult): AuthResult = try {
    operation()
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (error: Throwable) {
    AuthResult.Failure(error.toAuthError())
}

private fun Throwable.toAuthError(): AuthError = when (this) {
    is HttpRequestTimeoutException, is IOException -> AuthError.Network
    is RestException -> if (statusCode == 400 || statusCode == 401) {
        AuthError.InvalidCredentials
    } else {
        AuthError.Unknown
    }
    else -> AuthError.Unknown
}
