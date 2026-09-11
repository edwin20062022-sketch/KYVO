package com.kyvo.app.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kyvo.app.domain.auth.AuthRepository
import com.kyvo.app.domain.auth.AuthSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AccountSettingsUiState {
    data class Content(val session: AuthSession) : AccountSettingsUiState
    data class SigningOut(val session: AuthSession) : AccountSettingsUiState
    data class Error(val session: AuthSession, val message: String) : AccountSettingsUiState
    data object SignedOut : AccountSettingsUiState
}

class AccountSettingsViewModel(
    private val session: AuthSession,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<AccountSettingsUiState>(AccountSettingsUiState.Content(session))
    val state: StateFlow<AccountSettingsUiState> = _state.asStateFlow()

    fun signOut() {
        val current = when (val state = _state.value) {
            is AccountSettingsUiState.Content -> state.session
            is AccountSettingsUiState.Error -> state.session
            is AccountSettingsUiState.SigningOut, AccountSettingsUiState.SignedOut -> return
        }
        _state.value = AccountSettingsUiState.SigningOut(current)
        viewModelScope.launch {
            runCatching { authRepository.signOut() }
                .onSuccess { _state.value = AccountSettingsUiState.SignedOut }
                .onFailure { _state.value = AccountSettingsUiState.Error(current, "No pudimos cerrar tu sesión. Inténtalo de nuevo.") }
        }
    }

    fun retrySignOut() = signOut()

    companion object {
        fun factory(session: AuthSession, authRepository: AuthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T = AccountSettingsViewModel(session, authRepository) as T
            }
    }
}
