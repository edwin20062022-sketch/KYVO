package com.kyvo.app.feature.profile.presentation

import com.kyvo.app.domain.auth.AuthError
import com.kyvo.app.domain.auth.AuthProvider
import com.kyvo.app.domain.auth.AuthRepository
import com.kyvo.app.domain.auth.AuthResult
import com.kyvo.app.domain.auth.AuthSession
import com.kyvo.app.domain.auth.AuthState
import com.kyvo.app.test.MainDispatcherRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class EditAthleteProfileViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val session = AuthSession("user-1", "alex@kyvo.app", AuthProvider.Email, "Alex Martínez", "alexmrtz")

    @Test
    fun initialStateMapsAuthValues() {
        val viewModel = EditAthleteProfileViewModel(session, FakeAuthRepository(session))
        val state = viewModel.state.value as EditAthleteProfileUiState.Editing
        assertEquals("Alex Martínez", state.draft.displayName)
        assertEquals("alexmrtz", state.draft.username)
        assertEquals("alex@kyvo.app", state.draft.email)
    }

    @Test
    fun validUpdateTrimsAndPersistsMetadata() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeAuthRepository(session)
        val viewModel = EditAthleteProfileViewModel(session, repository)
        viewModel.onDisplayNameChanged("  Alex Rivera  ")
        viewModel.onUsernameChanged("  alexrv  ")
        viewModel.save()
        advanceUntilIdle()
        assertEquals("Alex Rivera" to "alexrv", repository.updated)
        assertTrue(viewModel.state.value is EditAthleteProfileUiState.Saved)
    }

    @Test
    fun blankNameDoesNotPersistAndShowsValidation() {
        val repository = FakeAuthRepository(session)
        val viewModel = EditAthleteProfileViewModel(session, repository)
        viewModel.onDisplayNameChanged("   ")
        viewModel.save()
        val state = viewModel.state.value as EditAthleteProfileUiState.Editing
        assertEquals("Escribe tu nombre.", state.validationMessage)
        assertEquals(null, repository.updated)
    }

    @Test
    fun failedUpdatePreservesDraft() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeAuthRepository(session, AuthResult.Failure(AuthError.Network))
        val viewModel = EditAthleteProfileViewModel(session, repository)
        viewModel.onDisplayNameChanged("Alex Nuevo")
        viewModel.save()
        advanceUntilIdle()
        val state = viewModel.state.value as EditAthleteProfileUiState.Error
        assertEquals("Alex Nuevo", state.draft.displayName)
        assertEquals("No pudimos guardar los cambios. Revisa tu conexión.", state.message)
    }

    @Test
    fun unchangedProfileDoesNotWrite() {
        val repository = FakeAuthRepository(session)
        EditAthleteProfileViewModel(session, repository).also {
            it.save()
            assertTrue(it.state.value is EditAthleteProfileUiState.Saved)
        }
        assertEquals(null, repository.updated)
    }

    private class FakeAuthRepository(
        private val session: AuthSession,
        private val updateResult: AuthResult = AuthResult.Success(session),
    ) : AuthRepository {
        var updated: Pair<String, String?>? = null
        override fun observeSession(): Flow<AuthState> = flowOf(AuthState.SignedIn(session))
        override fun currentSession(): AuthSession = session
        override suspend fun signUpWithEmail(email: String, password: CharArray): AuthResult = updateResult
        override suspend fun signInWithEmail(email: String, password: CharArray): AuthResult = updateResult
        override suspend fun signInWithGoogle(): AuthResult = updateResult
        override suspend fun updateProfileMetadata(displayName: String, username: String?): AuthResult {
            updated = displayName to username
            return updateResult
        }
        override suspend fun signOut() = Unit
    }
}
