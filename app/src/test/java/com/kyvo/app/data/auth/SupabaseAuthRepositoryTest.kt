package com.kyvo.app.data.auth

import com.kyvo.app.domain.auth.AuthError
import com.kyvo.app.domain.auth.AuthProvider
import com.kyvo.app.domain.auth.AuthResult
import com.kyvo.app.domain.auth.AuthSession
import com.kyvo.app.domain.auth.AuthState
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SupabaseAuthRepositoryTest {
    @Test
    fun `session inexistente is exposed as signed out`() = runTest {
        val repository = repository(FakeDataSource())
        assertEquals(AuthState.SignedOut, repository.observeSession().first())
        assertNull(repository.currentSession())
    }

    @Test
    fun `existing restored session is exposed`() = runTest {
        val dataSource = FakeDataSource(initialSession = session())
        assertEquals(AuthState.SignedIn(session()), repository(dataSource).observeSession().first())
    }

    @Test
    fun `email login returns Supabase session and wipes caller password`() = runTest {
        val password = "Password1".toCharArray()
        val result = repository(FakeDataSource()).signInWithEmail("athlete@kyvo.app", password)
        assertEquals(AuthResult.Success(session()), result)
        assertTrue(password.all { it == '\u0000' })
    }

    @Test
    fun `email Supabase error is mapped without throwing`() = runTest {
        val dataSource = FakeDataSource(emailFailure = IOException("offline"))
        assertEquals(
            AuthResult.Failure(AuthError.Network),
            repository(dataSource).signInWithEmail("athlete@kyvo.app", "Password1".toCharArray()),
        )
    }

    @Test
    fun `sign up without immediate session requires email confirmation`() = runTest {
        val dataSource = FakeDataSource(signUpSession = null)
        assertEquals(
            AuthResult.ConfirmationRequired("athlete@kyvo.app"),
            repository(dataSource).signUpWithEmail("athlete@kyvo.app", "Password1".toCharArray()),
        )
    }

    @Test
    fun `Google cancelled is distinct from provider error`() = runTest {
        val gateway = FakeGoogleGateway(GoogleCredentialResult.Cancelled)
        assertEquals(AuthResult.Failure(AuthError.Cancelled), repository(FakeDataSource(), gateway).signInWithGoogle())
    }

    @Test
    fun `Google credential error is mapped`() = runTest {
        val gateway = FakeGoogleGateway(GoogleCredentialResult.Failure(IOException("offline")))
        assertEquals(AuthResult.Failure(AuthError.Network), repository(FakeDataSource(), gateway).signInWithGoogle())
    }

    @Test
    fun `Supabase rejects Google token without leaking it`() = runTest {
        val dataSource = FakeDataSource(googleFailure = IllegalStateException("provider rejected token"))
        val gateway = FakeGoogleGateway(
            GoogleCredentialResult.Success(GoogleIdTokenResult("id-token", "raw-nonce")),
        )
        assertEquals(AuthResult.Failure(AuthError.Unknown), repository(dataSource, gateway).signInWithGoogle())
    }

    @Test
    fun `logout clears Supabase and Credential Manager state`() = runTest {
        val dataSource = FakeDataSource(initialSession = session())
        val gateway = FakeGoogleGateway(GoogleCredentialResult.Unavailable)
        repository(dataSource, gateway).signOut()
        assertEquals(AuthState.SignedOut, dataSource.states.value)
        assertTrue(gateway.wasCleared)
    }

    private fun repository(
        dataSource: FakeDataSource,
        gateway: FakeGoogleGateway = FakeGoogleGateway(GoogleCredentialResult.Unavailable),
    ) = SupabaseAuthRepository(dataSource, gateway)

    private fun session() = AuthSession("user-1", "athlete@kyvo.app", AuthProvider.Email)
}

private class FakeDataSource(
    initialSession: AuthSession? = null,
    private val signUpSession: AuthSession? = AuthSession("user-1", "athlete@kyvo.app", AuthProvider.Email),
    private val emailFailure: Throwable? = null,
    private val googleFailure: Throwable? = null,
) : SupabaseAuthDataSource {
    val states = MutableStateFlow<AuthState>(initialSession?.let(AuthState::SignedIn) ?: AuthState.SignedOut)
    override fun observeSession(): Flow<AuthState> = states
    override fun currentSession(): AuthSession? = (states.value as? AuthState.SignedIn)?.session
    override suspend fun signUpWithEmail(email: String, password: String): AuthSession? = signUpSession

    override suspend fun signInWithEmail(email: String, password: String): AuthSession {
        emailFailure?.let { throw it }
        return AuthSession("user-1", email, AuthProvider.Email).also { states.value = AuthState.SignedIn(it) }
    }

    override suspend fun signInWithGoogle(idToken: String, rawNonce: String): AuthSession {
        googleFailure?.let { throw it }
        return AuthSession("google-1", "google@kyvo.app", AuthProvider.Google).also {
            states.value = AuthState.SignedIn(it)
        }
    }

    override suspend fun updateProfileMetadata(displayName: String, username: String?): AuthSession =
        requireNotNull(currentSession())

    override suspend fun updateAvatar(bytes: ByteArray): AuthSession =
        requireNotNull(currentSession())

    override suspend fun signOut() {
        states.value = AuthState.SignedOut
    }
}

private class FakeGoogleGateway(private val result: GoogleCredentialResult) : GoogleCredentialGateway {
    var wasCleared = false
        private set
    override suspend fun requestIdToken(): GoogleCredentialResult = result
    override suspend fun clearCredentialState() {
        wasCleared = true
    }
}
