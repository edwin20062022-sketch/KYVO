package com.kyvo.app.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kyvo.app.domain.auth.AuthError
import com.kyvo.app.domain.auth.AuthRepository
import com.kyvo.app.domain.auth.AuthResult
import com.kyvo.app.feature.auth.domain.LoginValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val validator: LoginValidator = LoginValidator(),
) : ViewModel() {
    private val mutableState = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = mutableState.asStateFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            LoginEvent.OpenEmailLogin -> updateState { copy(mode = LoginMode.Email, message = null) }
            LoginEvent.BackToWelcome -> updateState {
                copy(
                    mode = LoginMode.Welcome,
                    password = "",
                    emailError = null,
                    passwordError = null,
                    message = null,
                )
            }
            is LoginEvent.EmailChanged -> updateState {
                copy(email = event.value, emailError = null, message = null)
            }
            is LoginEvent.PasswordChanged -> updateState {
                copy(password = event.value, passwordError = null, message = null)
            }
            LoginEvent.TogglePasswordVisibility -> updateState {
                copy(isPasswordVisible = !isPasswordVisible)
            }
            LoginEvent.SubmitEmail -> submitEmail()
            LoginEvent.SubmitGoogle -> authenticate { authRepository.signInWithGoogle() }
            LoginEvent.CreateAccount -> updateState {
                copy(message = LoginMessage.AccountCreationUnavailable)
            }
            LoginEvent.DismissMessage -> updateState { copy(message = null) }
            LoginEvent.NavigationHandled -> updateState { copy(isAuthenticated = false) }
        }
    }

    private fun submitEmail() {
        val current = mutableState.value
        if (current.isLoading) return

        val validation = validator.validate(current.email, current.password)
        if (!validation.isValid) {
            updateState {
                copy(
                    emailError = validation.emailError,
                    passwordError = validation.passwordError,
                    message = null,
                )
            }
            return
        }

        val email = current.email.trim()
        val password = current.password.toCharArray()
        authenticate {
            try {
                authRepository.signInWithEmail(email, password)
            } finally {
                password.fill('\u0000')
            }
        }
    }

    private fun authenticate(request: suspend () -> AuthResult) {
        if (mutableState.value.isLoading) return
        updateState { copy(isLoading = true, message = null) }
        viewModelScope.launch {
            val result = try {
                request()
            } catch (_: Exception) {
                AuthResult.Failure(AuthError.Unknown)
            }
            when (result) {
                is AuthResult.Success -> updateState {
                    copy(password = "", isLoading = false, isAuthenticated = true)
                }
                is AuthResult.Failure -> updateState {
                    copy(isLoading = false, message = result.error.toLoginMessage())
                }
            }
        }
    }

    private inline fun updateState(transform: LoginUiState.() -> LoginUiState) {
        mutableState.update(transform)
    }

    companion object {
        fun factory(authRepository: AuthRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { LoginViewModel(authRepository) }
        }
    }
}

private fun AuthError.toLoginMessage(): LoginMessage = when (this) {
    AuthError.InvalidCredentials -> LoginMessage.InvalidCredentials
    AuthError.Network -> LoginMessage.Network
    AuthError.ConfigurationRequired -> LoginMessage.ConfigurationRequired
    AuthError.Unknown -> LoginMessage.Unknown
}
