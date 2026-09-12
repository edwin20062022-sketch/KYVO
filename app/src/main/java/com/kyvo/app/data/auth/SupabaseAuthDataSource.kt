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
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.UploadData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

interface SupabaseAuthDataSource {
    fun observeSession(): Flow<AuthState>
    fun currentSession(): AuthSession?
    suspend fun signUpWithEmail(email: String, password: String): AuthSession?
    suspend fun signInWithEmail(email: String, password: String): AuthSession
    suspend fun signInWithGoogle(idToken: String, rawNonce: String): AuthSession
    suspend fun updateProfileMetadata(displayName: String, username: String?): AuthSession
    suspend fun updateAvatar(bytes: ByteArray): AuthSession
    suspend fun updatePassword(newPassword: String): AuthSession = requireNotNull(currentSession())
    suspend fun signOut()
    suspend fun deleteAccount() = Unit
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

    override suspend fun updateProfileMetadata(displayName: String, username: String?): AuthSession {
        client.auth.updateUser {
            data = buildJsonObject {
                put("full_name", JsonPrimitive(displayName))
                put("username", JsonPrimitive(username.orEmpty()))
            }
        }
        return requireNotNull(currentSession()) { "Supabase returned no session after profile update" }
    }

    override suspend fun updateAvatar(bytes: ByteArray): AuthSession {
        val userId = requireNotNull(client.auth.currentUserOrNull()?.id) { "No authenticated user" }
        val bucket = client.storage.from("avatars")
        val path = "$userId/avatar.jpg"
        bucket.upload(path, bytes) { upsert = true }
        val publicUrl = bucket.publicUrl(path)
        val version = System.currentTimeMillis().toString()
        client.auth.updateUser {
            data = buildJsonObject {
                put("avatar_url", JsonPrimitive(publicUrl))
                put("avatar_version", JsonPrimitive(version))
            }
        }
        return requireNotNull(currentSession()) { "Supabase returned no session after avatar update" }
    }

    override suspend fun updatePassword(newPassword: String): AuthSession {
        client.auth.updateUser { password = newPassword }
        return requireNotNull(currentSession()) { "Supabase returned no session after password update" }
    }

    override suspend fun signOut() {
        client.auth.signOut()
    }

    override suspend fun deleteAccount() {
        client.functions.invoke(function = "delete-account")
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
        username = user?.userMetadata?.get("username")?.jsonPrimitive?.contentOrNull?.takeIf(String::isNotBlank),
        avatarUrl = user?.userMetadata?.get("avatar_url")?.jsonPrimitive?.contentOrNull?.takeIf(String::isNotBlank),
        avatarVersion = user?.userMetadata?.get("avatar_version")?.jsonPrimitive?.contentOrNull?.takeIf(String::isNotBlank),
    )
}
