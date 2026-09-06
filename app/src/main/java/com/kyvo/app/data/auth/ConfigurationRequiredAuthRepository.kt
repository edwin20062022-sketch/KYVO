package com.kyvo.app.data.auth

import com.kyvo.app.domain.auth.AuthError
import com.kyvo.app.domain.auth.AuthRepository
import com.kyvo.app.domain.auth.AuthResult
import com.kyvo.app.domain.auth.AuthSession
import com.kyvo.app.domain.auth.AuthState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Used only when required public client configuration is absent from local.properties. */
class ConfigurationRequiredAuthRepository : AuthRepository {
    override fun observeSession(): Flow<AuthState> = flowOf(AuthState.SignedOut)
    override fun currentSession(): AuthSession? = null

    override suspend fun signUpWithEmail(email: String, password: CharArray): AuthResult =
        configurationRequired(password)

    override suspend fun signInWithEmail(email: String, password: CharArray): AuthResult =
        configurationRequired(password)

    override suspend fun signInWithGoogle(): AuthResult =
        AuthResult.Failure(AuthError.ConfigurationRequired)

    override suspend fun signOut() = Unit

    private fun configurationRequired(password: CharArray): AuthResult {
        password.fill('\u0000')
        return AuthResult.Failure(AuthError.ConfigurationRequired)
    }
}
