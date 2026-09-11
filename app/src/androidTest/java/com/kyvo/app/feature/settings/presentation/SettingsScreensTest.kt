package com.kyvo.app.feature.settings.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.kyvo.app.core.designsystem.KyvoTheme
import com.kyvo.app.domain.auth.AuthProvider
import com.kyvo.app.domain.auth.AuthSession
import org.junit.Rule
import org.junit.Test

class SettingsScreensTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val session = AuthSession("user-1", "athlete@kyvo.app", AuthProvider.Google, "Edwin", "edwin")

    @Test
    fun settingsShowsApprovedSectionsAndAccountEntry() {
        composeRule.setContent {
            KyvoTheme(darkTheme = false) {
                SettingsScreen(session = session)
            }
        }

        composeRule.onNodeWithContentDescription(SETTINGS_SCREEN_TAG).assertIsDisplayed()
        composeRule.onNodeWithText("Ajustes").assertIsDisplayed()
        composeRule.onNodeWithText("Objetivos nutricionales").assertIsDisplayed()
        composeRule.onNodeWithText("Preferencias de Meal Share").assertIsDisplayed()
        composeRule.onNodeWithText("Cerrar sesión").assertIsDisplayed()
    }

    @Test
    fun accountUsesSessionIdentityAndProviderWithoutInventingData() {
        composeRule.setContent {
            KyvoTheme(darkTheme = false) {
                AccountScreen(AccountSettingsUiState.Content(session))
            }
        }

        composeRule.onNodeWithContentDescription(ACCOUNT_SCREEN_TAG).assertIsDisplayed()
        composeRule.onNodeWithText("Edwin").assertIsDisplayed()
        composeRule.onNodeWithText("athlete@kyvo.app").assertIsDisplayed()
        composeRule.onNodeWithText("Vinculado a tu cuenta").assertIsDisplayed()
        composeRule.onNodeWithText("Editar perfil").assertIsDisplayed()
    }
}
