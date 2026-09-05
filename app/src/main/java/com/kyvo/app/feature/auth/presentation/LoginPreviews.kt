package com.kyvo.app.feature.auth.presentation

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.kyvo.app.core.designsystem.KyvoTheme

@Preview(name = "Login compact 320x640", widthDp = 320, heightDp = 640, showBackground = true)
@Composable
private fun CompactLoginPreview() {
    KyvoTheme(darkTheme = false) { LoginScreen(LoginUiState(), onEvent = {}) }
}

@Preview(name = "Login 360x800", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun MediumLoginPreview() {
    KyvoTheme(darkTheme = false) { LoginScreen(LoginUiState(), onEvent = {}) }
}

@Preview(name = "Login 411x891", widthDp = 411, heightDp = 891, showBackground = true)
@Composable
private fun StandardLoginPreview() {
    KyvoTheme(darkTheme = false) { LoginScreen(LoginUiState(), onEvent = {}) }
}

@Preview(name = "Login large 480x960", widthDp = 480, heightDp = 960, showBackground = true)
@Composable
private fun LargeLoginPreview() {
    KyvoTheme(darkTheme = false) { LoginScreen(LoginUiState(), onEvent = {}) }
}

@Preview(name = "Email form with errors", widthDp = 320, heightDp = 640, showBackground = true)
@Composable
private fun EmailErrorPreview() {
    KyvoTheme(darkTheme = false) {
        LoginScreen(
            state = LoginUiState(
                mode = LoginMode.Email,
                email = "correo-invalido",
                emailError = EmailValidationError.Invalid,
                passwordError = PasswordValidationError.Empty,
                message = LoginMessage.InvalidCredentials,
            ),
            onEvent = {},
        )
    }
}

@Preview(
    name = "Login dark",
    widthDp = 411,
    heightDp = 891,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun DarkLoginPreview() {
    KyvoTheme(darkTheme = true) { LoginScreen(LoginUiState(), onEvent = {}) }
}

