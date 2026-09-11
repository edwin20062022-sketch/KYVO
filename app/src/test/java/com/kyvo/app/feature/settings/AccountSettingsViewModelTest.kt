package com.kyvo.app.feature.settings

import com.kyvo.app.domain.auth.AuthError
import com.kyvo.app.domain.auth.AuthProvider
import com.kyvo.app.domain.auth.AuthRepository
import com.kyvo.app.domain.auth.AuthResult
import com.kyvo.app.domain.auth.AuthSession
import com.kyvo.app.domain.auth.AuthState
import com.kyvo.app.feature.settings.presentation.AccountSettingsUiState
import com.kyvo.app.feature.settings.presentation.AccountSettingsViewModel
import com.kyvo.app.test.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccountSettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun successfulSignOutReachesSignedOut() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = AccountSettingsViewModel(session(), repository)

        viewModel.signOut()
        advanceUntilIdle()

        assertEquals(AccountSettingsUiState.SignedOut, viewModel.state.value)
        assertEquals(1, repository.signOutAttempts)
    }

    @Test
    fun signOutFailureKeepsAccountAndExposesRetryableError() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeAuthRepository(signOutFailure = IllegalStateException("offline"))
        val viewModel = AccountSettingsViewModel(session(), repository)

        viewModel.signOut()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is AccountSettingsUiState.Error)
        assertEquals("No pudimos cerrar tu sesión. Inténtalo de nuevo.", (state as AccountSettingsUiState.Error).message)
        assertEquals(session(), state.session)
    }

    @Test
    fun duplicateSignOutIsIgnoredWhileRequestIsPending() = runTest(mainDispatcherRule.testDispatcher) {
        val pending = CompletableDeferred<Unit>()
        val repository = FakeAuthRepository(signOutResult = { pending.await() })
        val viewModel = AccountSettingsViewModel(session(), repository)

        viewModel.signOut()
        runCurrent()
        viewModel.signOut()

        assertEquals(1, repository.signOutAttempts)
        pending.complete(Unit)
        advanceUntilIdle()
        assertEquals(AccountSettingsUiState.SignedOut, viewModel.state.value)
    }

    private fun session() = AuthSession("user-1", "athlete@kyvo.app", AuthProvider.Google, "Edwin", "edwin")
}

private class FakeAuthRepository(
    private val signOutFailure: Throwable? = null,
    private val signOutResult: (suspend () -> Unit)? = null,
) : AuthRepository {
    var signOutAttempts = 0
        private set

    override fun observeSession(): Flow<AuthState> = emptyFlow()
    override fun currentSession(): AuthSession? = null
    override suspend fun signUpWithEmail(email: String, password: CharArray): AuthResult = AuthResult.Failure(AuthError.Unknown)
    override suspend fun signInWithEmail(email: String, password: CharArray): AuthResult = AuthResult.Failure(AuthError.Unknown)
    override suspend fun signInWithGoogle(): AuthResult = AuthResult.Failure(AuthError.Unknown)
    override suspend fun signOut() {
        signOutAttempts++
        signOutFailure?.let { throw it }
        signOutResult?.invoke()
    }
}
