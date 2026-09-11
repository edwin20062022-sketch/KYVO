package com.kyvo.app.feature.settings.presentation

import com.kyvo.app.domain.auth.AuthError
import com.kyvo.app.domain.auth.AuthProvider
import com.kyvo.app.domain.auth.AuthRepository
import com.kyvo.app.domain.auth.AuthResult
import com.kyvo.app.domain.auth.AuthSession
import com.kyvo.app.domain.auth.AuthState
import com.kyvo.app.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SecurityViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test fun googleUserCannotBePresentedWithFakePasswordManagement() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeSecurityAuthRepository()
        val viewModel = SecurityViewModel(session(AuthProvider.Google), repository)
        viewModel.setNewPassword("Password1")
        viewModel.setConfirmation("Password1")
        viewModel.updatePassword()
        advanceUntilIdle()
        assertEquals(0, repository.passwordAttempts)
    }

    @Test fun invalidPasswordDoesNotCallAuth() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeSecurityAuthRepository()
        val viewModel = SecurityViewModel(session(AuthProvider.Email), repository)
        viewModel.setNewPassword("short")
        viewModel.setConfirmation("short")
        viewModel.updatePassword()
        assertEquals(0, repository.passwordAttempts)
        assertTrue((viewModel.state.value as SecurityUiState.Content).message!!.contains("8 caracteres"))
    }

    @Test fun validPasswordUpdatesOnceAndClearsSensitiveFields() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeSecurityAuthRepository()
        val viewModel = SecurityViewModel(session(AuthProvider.Email), repository)
        viewModel.setNewPassword("Password1")
        viewModel.setConfirmation("Password1")
        viewModel.updatePassword()
        advanceUntilIdle()
        val state = viewModel.state.value as SecurityUiState.Content
        assertEquals(1, repository.passwordAttempts)
        assertEquals("", state.newPassword)
        assertEquals("", state.confirmation)
        assertTrue(state.success)
    }

    private fun session(provider: AuthProvider) = AuthSession("user-1", "athlete@kyvo.app", provider)
}

private class FakeSecurityAuthRepository : AuthRepository {
    var passwordAttempts = 0
    override fun observeSession(): Flow<AuthState> = emptyFlow()
    override fun currentSession(): AuthSession? = null
    override suspend fun signUpWithEmail(email: String, password: CharArray) = AuthResult.Failure(AuthError.Unknown)
    override suspend fun signInWithEmail(email: String, password: CharArray) = AuthResult.Failure(AuthError.Unknown)
    override suspend fun signInWithGoogle() = AuthResult.Failure(AuthError.Unknown)
    override suspend fun updatePassword(newPassword: CharArray): AuthResult { passwordAttempts++; newPassword.fill('\u0000'); return AuthResult.Success(AuthSession("user-1", "athlete@kyvo.app", AuthProvider.Email)) }
    override suspend fun signOut() = Unit
}
