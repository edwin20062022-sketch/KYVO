package com.kyvo.app.feature.auth.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kyvo.app.core.designsystem.KyvoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun welcomeShowsThreeAuthenticationActions() {
        composeRule.setContent {
            KyvoTheme(darkTheme = false) { LoginScreen(LoginUiState(), onEvent = {}) }
        }

        composeRule.onNodeWithTag(CREATE_ACCOUNT_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(OPEN_EMAIL_LOGIN_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(GOOGLE_LOGIN_TAG).assertIsDisplayed()
        composeRule.onNodeWithText("TRACKING SIMPLE Y PRECISO").assertIsDisplayed()
    }

    @Test
    fun emailModeShowsFieldsAndSubmitCta() {
        composeRule.setContent {
            KyvoTheme(darkTheme = false) {
                LoginScreen(LoginUiState(mode = LoginMode.Email), onEvent = {})
            }
        }

        composeRule.onNodeWithTag(EMAIL_FIELD_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(PASSWORD_FIELD_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(SUBMIT_LOGIN_TAG).assertIsDisplayed()
    }

    @Test
    fun validationErrorsAreVisibleInContext() {
        composeRule.setContent {
            KyvoTheme(darkTheme = false) {
                LoginScreen(
                    state = LoginUiState(
                        mode = LoginMode.Email,
                        emailError = EmailValidationError.Invalid,
                        passwordError = PasswordValidationError.Empty,
                    ),
                    onEvent = {},
                )
            }
        }

        composeRule.onNodeWithText("Ingresa un correo electrónico válido.").assertIsDisplayed()
        composeRule.onNodeWithText("Ingresa tu contraseña.").assertIsDisplayed()
    }

    @Test
    fun loadingDisablesSubmitAction() {
        composeRule.setContent {
            KyvoTheme(darkTheme = false) {
                LoginScreen(
                    LoginUiState(mode = LoginMode.Email, isLoading = true),
                    onEvent = {},
                )
            }
        }

        composeRule.onNodeWithTag(SUBMIT_LOGIN_TAG).assertIsNotEnabled()
    }

    @Test
    fun tappingSignInRequestsEmailMode() {
        var received: LoginEvent? = null
        composeRule.setContent {
            KyvoTheme(darkTheme = false) {
                LoginScreen(LoginUiState(), onEvent = { received = it })
            }
        }

        composeRule.onNodeWithTag(OPEN_EMAIL_LOGIN_TAG).performClick()
        assertEquals(LoginEvent.OpenEmailLogin, received)
    }
}
