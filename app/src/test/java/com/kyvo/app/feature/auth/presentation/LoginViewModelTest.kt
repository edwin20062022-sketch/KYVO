package com.kyvo.app.feature.auth.presentation

import com.kyvo.app.domain.auth.AuthError
import com.kyvo.app.domain.auth.AuthProvider
import com.kyvo.app.domain.auth.AuthRepository
import com.kyvo.app.domain.auth.AuthResult
import com.kyvo.app.domain.auth.AuthSession
import com.kyvo.app.test.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `submit with empty fields shows contextual validation`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = LoginViewModel(FakeAuthRepository())

        viewModel.onEvent(LoginEvent.SubmitEmail)

        assertEquals(EmailValidationError.Empty, viewModel.state.value.emailError)
        assertEquals(PasswordValidationError.Empty, viewModel.state.value.passwordError)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `loading starts immediately and prevents duplicate submissions`() = runTest(mainDispatcherRule.testDispatcher) {
        val deferredResult = CompletableDeferred<AuthResult>()
        val repository = FakeAuthRepository(emailResult = { deferredResult.await() })
        val viewModel = configuredViewModel(repository)

        viewModel.onEvent(LoginEvent.SubmitEmail)
        viewModel.onEvent(LoginEvent.SubmitEmail)
        runCurrent()

        assertTrue(viewModel.state.value.isLoading)
        assertEquals(1, repository.emailAttempts)

        deferredResult.complete(successResult())
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `invalid credentials are mapped to visible auth error`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeAuthRepository(
            emailResult = { AuthResult.Failure(AuthError.InvalidCredentials) },
        )
        val viewModel = configuredViewModel(repository)

        viewModel.onEvent(LoginEvent.SubmitEmail)
        advanceUntilIdle()

        assertEquals(LoginMessage.InvalidCredentials, viewModel.state.value.message)
        assertFalse(viewModel.state.value.isAuthenticated)
    }

    @Test
    fun `network failure is represented independently`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeAuthRepository(
            emailResult = { AuthResult.Failure(AuthError.Network) },
        )
        val viewModel = configuredViewModel(repository)

        viewModel.onEvent(LoginEvent.SubmitEmail)
        advanceUntilIdle()

        assertEquals(LoginMessage.Network, viewModel.state.value.message)
    }

    @Test
    fun `successful login clears password and exposes navigation state`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = configuredViewModel(FakeAuthRepository(emailResult = { successResult() }))

        viewModel.onEvent(LoginEvent.SubmitEmail)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isAuthenticated)
        assertTrue(viewModel.state.value.password.isEmpty())

        viewModel.onEvent(LoginEvent.NavigationHandled)
        assertFalse(viewModel.state.value.isAuthenticated)
    }

    @Test
    fun `google failure uses same repository boundary`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeAuthRepository(
            googleResult = { AuthResult.Failure(AuthError.ConfigurationRequired) },
        )
        val viewModel = LoginViewModel(repository)

        viewModel.onEvent(LoginEvent.SubmitGoogle)
        advanceUntilIdle()

        assertEquals(1, repository.googleAttempts)
        assertEquals(LoginMessage.ConfigurationRequired, viewModel.state.value.message)
    }

    private fun configuredViewModel(repository: AuthRepository): LoginViewModel =
        LoginViewModel(repository).also { viewModel ->
            viewModel.onEvent(LoginEvent.EmailChanged("alex@kyvo.app"))
            viewModel.onEvent(LoginEvent.PasswordChanged("Password1"))
        }

    private fun successResult() = AuthResult.Success(
        AuthSession(
            userId = "user-1",
            email = "alex@kyvo.app",
            provider = AuthProvider.Email,
        ),
    )
}

private class FakeAuthRepository(
    private val emailResult: suspend () -> AuthResult = { AuthResult.Failure(AuthError.Unknown) },
    private val googleResult: suspend () -> AuthResult = { AuthResult.Failure(AuthError.Unknown) },
) : AuthRepository {
    private val session = MutableStateFlow<AuthSession?>(null)
    var emailAttempts = 0
        private set
    var googleAttempts = 0
        private set

    override fun observeSession(): Flow<AuthSession?> = session

    override suspend fun signInWithEmail(email: String, password: CharArray): AuthResult {
        emailAttempts += 1
        return emailResult()
    }

    override suspend fun signInWithGoogle(): AuthResult {
        googleAttempts += 1
        return googleResult()
    }

    override suspend fun signOut() {
        session.value = null
    }
}

