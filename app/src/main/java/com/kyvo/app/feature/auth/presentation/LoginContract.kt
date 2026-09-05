package com.kyvo.app.feature.auth.presentation

enum class LoginMode { Welcome, Email }

enum class EmailValidationError { Empty, Invalid }

enum class PasswordValidationError { Empty, TooShort }

enum class LoginMessage {
    InvalidCredentials,
    Network,
    ConfigurationRequired,
    Unknown,
    AccountCreationUnavailable,
}

data class LoginUiState(
    val mode: LoginMode = LoginMode.Welcome,
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val emailError: EmailValidationError? = null,
    val passwordError: PasswordValidationError? = null,
    val message: LoginMessage? = null,
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
)

sealed interface LoginEvent {
    data object OpenEmailLogin : LoginEvent
    data object BackToWelcome : LoginEvent
    data class EmailChanged(val value: String) : LoginEvent
    data class PasswordChanged(val value: String) : LoginEvent
    data object TogglePasswordVisibility : LoginEvent
    data object SubmitEmail : LoginEvent
    data object SubmitGoogle : LoginEvent
    data object CreateAccount : LoginEvent
    data object DismissMessage : LoginEvent
    data object NavigationHandled : LoginEvent
}

