package com.kyvo.app.data.auth

import com.kyvo.app.domain.auth.AuthRepository
import com.kyvo.app.domain.auth.AuthResult
import com.kyvo.app.domain.auth.AuthSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Safe adapter used until a production identity provider is configured.
 * It never persists credentials and never treats local values as authenticated.
 */
class PendingAuthRepository : AuthRepository {
    private val session = MutableStateFlow<AuthSession?>(null)

    override fun observeSession(): Flow<AuthSession?> = session.asStateFlow()

    override suspend fun signInWithEmail(email: String, password: CharArray): AuthResult {
        password.fill('\u0000')
        return AuthResult.Failure(com.kyvo.app.domain.auth.AuthError.ConfigurationRequired)
    }

    override suspend fun signInWithGoogle(): AuthResult =
        AuthResult.Failure(com.kyvo.app.domain.auth.AuthError.ConfigurationRequired)

    override suspend fun signOut() {
        session.value = null
    }
}

