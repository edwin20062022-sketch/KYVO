package com.kyvo.app.feature.settings.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.kyvo.app.core.designsystem.KyvoTheme
import com.kyvo.app.domain.auth.AuthProvider
import com.kyvo.app.domain.auth.AuthSession
import com.kyvo.app.feature.settings.domain.AppSettings
import com.kyvo.app.feature.settings.domain.AppearanceMode
import com.kyvo.app.feature.settings.domain.FoodUnit
import com.kyvo.app.feature.settings.domain.HeightUnit
import com.kyvo.app.feature.settings.domain.TemperatureUnit
import com.kyvo.app.feature.settings.domain.UnitPreferences
import com.kyvo.app.feature.settings.domain.WeightUnit
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

    @Test
    fun unitsShowsAllPersistedSelections() {
        composeRule.setContent {
            KyvoTheme(darkTheme = false) {
                UnitsScreen(AppSettings(UnitPreferences(WeightUnit.POUNDS, HeightUnit.FEET_INCHES, FoodUnit.OUNCES, TemperatureUnit.FAHRENHEIT)))
            }
        }

        composeRule.onNodeWithContentDescription("units_screen").assertIsDisplayed()
        composeRule.onNodeWithText("lb").assertIsDisplayed()
        composeRule.onNodeWithText("ft / in").assertIsDisplayed()
        composeRule.onNodeWithText("onzas").assertIsDisplayed()
        composeRule.onNodeWithText("°F").assertIsDisplayed()
    }

    @Test
    fun appearanceShowsAllSupportedModesAndSelection() {
        composeRule.setContent {
            KyvoTheme(darkTheme = true) {
                AppearanceScreen(AppSettings(appearance = AppearanceMode.DARK))
            }
        }

        composeRule.onNodeWithContentDescription("appearance_screen").assertIsDisplayed()
        composeRule.onNodeWithText("Claro").assertIsDisplayed()
        composeRule.onNodeWithText("Oscuro").assertIsDisplayed()
        composeRule.onNodeWithText("Automático").assertIsDisplayed()
    }

    @Test
    fun securityShowsProviderAwarePasswordState() {
        composeRule.setContent {
            KyvoTheme(darkTheme = false) {
                SecurityScreen(SecurityUiState.Content(session.copy(provider = AuthProvider.Google)))
            }
        }

        composeRule.onNodeWithText("Administrada por Google").assertIsDisplayed()
        composeRule.onNodeWithText("No disponible todavía").assertIsDisplayed()
    }
}
