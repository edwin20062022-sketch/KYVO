package com.kyvo.app.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kyvo.app.domain.auth.AuthError
import com.kyvo.app.domain.auth.AuthRepository
import com.kyvo.app.domain.auth.AuthResult
import com.kyvo.app.domain.auth.AuthSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditAthleteProfileDraft(
    val displayName: String,
    val username: String,
    val email: String,
)

sealed interface EditAthleteProfileUiState {
    data object Loading : EditAthleteProfileUiState
    data class Editing(val draft: EditAthleteProfileDraft, val validationMessage: String? = null) : EditAthleteProfileUiState
    data class Saving(val draft: EditAthleteProfileDraft) : EditAthleteProfileUiState
    data object Saved : EditAthleteProfileUiState
    data class Error(val draft: EditAthleteProfileDraft, val message: String) : EditAthleteProfileUiState
}

class EditAthleteProfileViewModel(
    private val session: AuthSession,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val initialDraft = EditAthleteProfileDraft(
        displayName = session.displayName.orEmpty(),
        username = session.username.orEmpty(),
        email = session.email,
    )
    private val _state = MutableStateFlow<EditAthleteProfileUiState>(EditAthleteProfileUiState.Editing(initialDraft))
    val state: StateFlow<EditAthleteProfileUiState> = _state.asStateFlow()

    fun onDisplayNameChanged(value: String) = updateDraft { copy(displayName = value) }

    fun onUsernameChanged(value: String) = updateDraft { copy(username = value) }

    fun save() {
        val draft = currentDraft() ?: return
        val normalized = draft.copy(
            displayName = draft.displayName.trim(),
            username = draft.username.trim(),
        )
        when {
            normalized.displayName.isBlank() -> {
                _state.value = EditAthleteProfileUiState.Editing(normalized, "Escribe tu nombre.")
            }
            normalized.displayName.length > 120 -> {
                _state.value = EditAthleteProfileUiState.Editing(normalized, "El nombre es demasiado largo.")
            }
            normalized.username.length > 64 -> {
                _state.value = EditAthleteProfileUiState.Editing(normalized, "El nombre de usuario es demasiado largo.")
            }
            normalized.displayName == initialDraft.displayName && normalized.username == initialDraft.username -> {
                _state.value = EditAthleteProfileUiState.Saved
            }
            else -> persist(normalized)
        }
    }

    private fun persist(draft: EditAthleteProfileDraft) {
        _state.value = EditAthleteProfileUiState.Saving(draft)
        viewModelScope.launch {
            when (val result = authRepository.updateProfileMetadata(draft.displayName, draft.username.ifBlank { null })) {
                is AuthResult.Success -> _state.value = EditAthleteProfileUiState.Saved
                is AuthResult.Failure -> _state.value = EditAthleteProfileUiState.Error(draft, result.error.message())
                is AuthResult.ConfirmationRequired -> _state.value = EditAthleteProfileUiState.Error(draft, "No pudimos guardar tu perfil.")
            }
        }
    }

    private fun updateDraft(transform: EditAthleteProfileDraft.() -> EditAthleteProfileDraft) {
        val draft = currentDraft() ?: return
        _state.value = EditAthleteProfileUiState.Editing(draft.transform())
    }

    private fun currentDraft(): EditAthleteProfileDraft? = when (val current = _state.value) {
        is EditAthleteProfileUiState.Editing -> current.draft
        is EditAthleteProfileUiState.Error -> current.draft
        is EditAthleteProfileUiState.Saving -> current.draft
        EditAthleteProfileUiState.Loading, EditAthleteProfileUiState.Saved -> null
    }

    companion object {
        fun factory(session: AuthSession, authRepository: AuthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    EditAthleteProfileViewModel(session, authRepository) as T
            }
    }
}

private fun AuthError.message(): String = when (this) {
    AuthError.Network -> "No pudimos guardar los cambios. Revisa tu conexión."
    AuthError.ConfigurationRequired -> "La autenticación aún no está configurada."
    AuthError.InvalidCredentials -> "No pudimos validar tu sesión."
    AuthError.Cancelled -> "La operación fue cancelada."
    AuthError.Unknown -> "No pudimos guardar los cambios. Inténtalo de nuevo."
}
