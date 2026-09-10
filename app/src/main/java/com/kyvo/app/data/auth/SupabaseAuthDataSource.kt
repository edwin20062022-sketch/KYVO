package com.kyvo.app.data.auth

import com.kyvo.app.domain.auth.AuthProvider
import com.kyvo.app.domain.auth.AuthSession
import com.kyvo.app.domain.auth.AuthState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

interface SupabaseAuthDataSource {
    fun observeSession(): Flow<AuthState>
    fun currentSession(): AuthSession?
    suspend fun signUpWithEmail(email: String, password: String): AuthSession?
    suspend fun signInWithEmail(email: String, password: String): AuthSession
    suspend fun signInWithGoogle(idToken: String, rawNonce: String): AuthSession
    suspend fun signOut()
}

class SupabaseSdkAuthDataSource(private val client: SupabaseClient) : SupabaseAuthDataSource {
    override fun observeSession(): Flow<AuthState> = client.auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> AuthState.SignedIn(status.session.toDomain())
            SessionStatus.Initializing -> AuthState.Initializing
            else -> AuthState.SignedOut
        }
    }

    override fun currentSession(): AuthSession? = client.auth.currentSessionOrNull()?.toDomain()

    override suspend fun signUpWithEmail(email: String, password: String): AuthSession? {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        return currentSession()
    }

    override suspend fun signInWithEmail(email: String, password: String): AuthSession {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        return requireNotNull(currentSession()) { "Supabase returned no session after email sign-in" }
    }

    override suspend fun signInWithGoogle(idToken: String, rawNonce: String): AuthSession {
        client.auth.signInWith(IDToken) {
            this.idToken = idToken
            provider = Google
            nonce = rawNonce
        }
        return requireNotNull(currentSession()) { "Supabase returned no session after Google sign-in" }
    }

    override suspend fun signOut() {
        client.auth.signOut()
    }
}

private fun io.github.jan.supabase.auth.user.UserSession.toDomain(): AuthSession {
    val resolvedProvider = if (user?.appMetadata?.get("provider")?.toString()?.contains("google", true) == true) {
        AuthProvider.Google
    } else {
        AuthProvider.Email
    }
    return AuthSession(
        userId = requireNotNull(user?.id) { "Authenticated Supabase session has no user id" },
        email = user?.email.orEmpty(),
        provider = resolvedProvider,
        displayName = user?.userMetadata?.let { metadata ->
            listOf("full_name", "name", "display_name")
                .firstNotNullOfOrNull { key -> metadata[key]?.jsonPrimitive?.contentOrNull?.takeIf(String::isNotBlank) }
        },
    )
}
