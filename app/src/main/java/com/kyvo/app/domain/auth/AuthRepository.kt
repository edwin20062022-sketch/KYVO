package com.kyvo.app.domain.auth

import kotlinx.coroutines.flow.Flow

enum class AuthProvider { Email, Google }

data class AuthSession(
    val userId: String,
    val email: String,
    val provider: AuthProvider,
    val displayName: String? = null,
    val username: String? = null,
    val avatarUrl: String? = null,
    val avatarVersion: String? = null,
)

sealed interface AuthError {
    data object InvalidCredentials : AuthError
    data object Network : AuthError
    data object Cancelled : AuthError
    data object ConfigurationRequired : AuthError
    data object Unknown : AuthError
}

sealed interface AuthState {
    data object Initializing : AuthState
    data object SignedOut : AuthState
    data class SignedIn(val session: AuthSession) : AuthState
}

sealed interface AuthResult {
    data class Success(val session: AuthSession) : AuthResult
    data class ConfirmationRequired(val email: String) : AuthResult
    data class Failure(val error: AuthError) : AuthResult
}

interface AuthRepository {
    fun observeSession(): Flow<AuthState>
    fun currentSession(): AuthSession?
    suspend fun signUpWithEmail(email: String, password: CharArray): AuthResult
    suspend fun signInWithEmail(email: String, password: CharArray): AuthResult
    suspend fun signInWithGoogle(): AuthResult
    suspend fun updateProfileMetadata(displayName: String, username: String?): AuthResult =
        AuthResult.Failure(AuthError.Unknown)
    suspend fun updateAvatarBytes(bytes: ByteArray): AuthResult =
        AuthResult.Failure(AuthError.Unknown)
    suspend fun updatePassword(newPassword: CharArray): AuthResult {
        newPassword.fill('\u0000')
        return AuthResult.Failure(AuthError.Unknown)
    }
    suspend fun signOut()
    suspend fun deleteAccount(): AuthResult = AuthResult.Failure(AuthError.Unknown)
}
