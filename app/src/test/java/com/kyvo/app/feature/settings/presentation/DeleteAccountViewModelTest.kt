package com.kyvo.app.feature.settings.presentation

import com.kyvo.app.domain.auth.AuthError
import com.kyvo.app.domain.auth.AuthProvider
import com.kyvo.app.domain.auth.AuthRepository
import com.kyvo.app.domain.auth.AuthResult
import com.kyvo.app.domain.auth.AuthSession
import com.kyvo.app.domain.auth.AuthState
import com.kyvo.app.feature.onboarding.domain.model.SavedOnboarding
import com.kyvo.app.feature.onboarding.domain.repository.OnboardingRepository
import com.kyvo.app.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeleteAccountViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun successfulDeletionCleansLocalUserDataAfterRemoteSuccess() = runTest(mainDispatcherRule.testDispatcher) {
        val auth = FakeDeleteAuthRepository(AuthResult.Success(session()))
        val local = FakeOnboardingRepository()
        var cacheClears = 0
        val viewModel = DeleteAccountViewModel(auth, local) { cacheClears++ }

        viewModel.deleteAccount()
        advanceUntilIdle()

        assertEquals(1, auth.deleteCalls)
        assertEquals(1, local.clearCalls)
        assertEquals(1, cacheClears)
        assertEquals(DeleteAccountUiState.Deleted, viewModel.state.value)
    }

    @Test
    fun backendFailurePreservesLocalDataAndAllowsRetry() = runTest(mainDispatcherRule.testDispatcher) {
        val auth = FakeDeleteAuthRepository(AuthResult.Failure(AuthError.Network))
        val local = FakeOnboardingRepository()
        val viewModel = DeleteAccountViewModel(auth, local) {}

        viewModel.deleteAccount()
        advanceUntilIdle()

        assertTrue(viewModel.state.value is DeleteAccountUiState.Error)
        assertEquals(0, local.clearCalls)
    }

    @Test
    fun doubleSubmitMakesOnlyOneDeleteRequestWhileDeleting() = runTest(mainDispatcherRule.testDispatcher) {
        val auth = FakeDeleteAuthRepository(AuthResult.Success(session()))
        val viewModel = DeleteAccountViewModel(auth, FakeOnboardingRepository()) {}

        viewModel.deleteAccount()
        viewModel.deleteAccount()
        advanceUntilIdle()

        assertEquals(1, auth.deleteCalls)
    }
}

private fun session() = AuthSession("user-a", "a@example.com", AuthProvider.Email)

private class FakeDeleteAuthRepository(private val result: AuthResult) : AuthRepository {
    var deleteCalls = 0
    override fun observeSession(): Flow<AuthState> = flowOf(AuthState.SignedIn(session()))
    override fun currentSession(): AuthSession = session()
    override suspend fun signUpWithEmail(email: String, password: CharArray) = result
    override suspend fun signInWithEmail(email: String, password: CharArray) = result
    override suspend fun signInWithGoogle() = result
    override suspend fun signOut() = Unit
    override suspend fun deleteAccount(): AuthResult { deleteCalls++; return result }
}

private class FakeOnboardingRepository : OnboardingRepository {
    var clearCalls = 0
    override fun observe(): Flow<SavedOnboarding> = flowOf(SavedOnboarding())
    override suspend fun save(progress: SavedOnboarding) = Unit
    override suspend fun clearUserData() { clearCalls++ }
}
