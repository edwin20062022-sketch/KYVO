package com.kyvo.app.feature.auth.domain

import com.kyvo.app.feature.auth.presentation.EmailValidationError
import com.kyvo.app.feature.auth.presentation.PasswordValidationError

data class LoginValidationResult(
    val emailError: EmailValidationError?,
    val passwordError: PasswordValidationError?,
) {
    val isValid: Boolean = emailError == null && passwordError == null
}

class LoginValidator {
    fun validateEmail(email: String): EmailValidationError? = when {
        email.isBlank() -> EmailValidationError.Empty
        !EMAIL_PATTERN.matches(email.trim()) -> EmailValidationError.Invalid
        else -> null
    }

    fun validatePassword(password: String): PasswordValidationError? = when {
        password.isEmpty() -> PasswordValidationError.Empty
        password.length < MINIMUM_PASSWORD_LENGTH -> PasswordValidationError.TooShort
        else -> null
    }

    fun validate(email: String, password: String) = LoginValidationResult(
        emailError = validateEmail(email),
        passwordError = validatePassword(password),
    )

    private companion object {
        const val MINIMUM_PASSWORD_LENGTH = 8
        val EMAIL_PATTERN = Regex(
            pattern = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            option = RegexOption.IGNORE_CASE,
        )
    }
}

