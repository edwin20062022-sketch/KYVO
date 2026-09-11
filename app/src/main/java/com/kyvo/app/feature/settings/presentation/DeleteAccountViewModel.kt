package com.kyvo.app.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kyvo.app.domain.auth.AuthRepository
import com.kyvo.app.domain.auth.AuthResult
import com.kyvo.app.feature.onboarding.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DeleteAccountUiState {
    data object Idle : DeleteAccountUiState
    data object Deleting : DeleteAccountUiState
    data object Deleted : DeleteAccountUiState
    data class Error(val message: String) : DeleteAccountUiState
}

class DeleteAccountViewModel(
    private val authRepository: AuthRepository,
    private val onboardingRepository: OnboardingRepository,
    private val clearCache: () -> Unit,
) : ViewModel() {
    private val _state = MutableStateFlow<DeleteAccountUiState>(DeleteAccountUiState.Idle)
    val state: StateFlow<DeleteAccountUiState> = _state.asStateFlow()

    fun deleteAccount() {
        if (_state.value == DeleteAccountUiState.Deleting) return
        _state.value = DeleteAccountUiState.Deleting
        viewModelScope.launch {
            when (val result = authRepository.deleteAccount()) {
                is AuthResult.Success -> {
                    onboardingRepository.clearUserData()
                    clearCache()
                    _state.value = DeleteAccountUiState.Deleted
                }
                else -> _state.value = DeleteAccountUiState.Error("No pudimos eliminar tu cuenta. Revisa tu conexión e inténtalo de nuevo.")
            }
        }
    }

    fun retry() { if (_state.value is DeleteAccountUiState.Error) deleteAccount() }

    companion object {
        fun factory(auth: AuthRepository, onboarding: OnboardingRepository, clearCache: () -> Unit): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = DeleteAccountViewModel(auth, onboarding, clearCache) as T
        }
    }
}
