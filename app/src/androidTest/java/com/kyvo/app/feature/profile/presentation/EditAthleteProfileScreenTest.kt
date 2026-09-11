package com.kyvo.app.feature.profile.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.kyvo.app.core.designsystem.KyvoTheme
import org.junit.Rule
import org.junit.Test

class EditAthleteProfileScreenTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun editingShowsRealAuthFieldsAndSaveAction() {
        composeRule.setContent {
            KyvoTheme {
                EditAthleteProfileScreen(
                    state = EditAthleteProfileUiState.Editing(
                        EditAthleteProfileDraft("Alex Martínez", "alexmrtz", "alex@kyvo.app"),
                    ),
                )
            }
        }
        composeRule.onNodeWithText("Editar perfil").assertIsDisplayed()
        composeRule.onNodeWithText("Alex Martínez").assertIsDisplayed()
        composeRule.onNodeWithText("alexmrtz").assertIsDisplayed()
        composeRule.onNodeWithText("alex@kyvo.app").assertIsDisplayed()
        composeRule.onNodeWithTag(EDIT_PROFILE_SAVE_TAG).assertIsDisplayed()
    }

    @Test
    fun errorKeepsDraftVisible() {
        composeRule.setContent {
            KyvoTheme {
                EditAthleteProfileScreen(
                    state = EditAthleteProfileUiState.Error(
                        EditAthleteProfileDraft("Alex Nuevo", "alexnuevo", "alex@kyvo.app"),
                        "No pudimos guardar los cambios.",
                    ),
                )
            }
        }
        composeRule.onNodeWithText("Alex Nuevo").assertIsDisplayed()
        composeRule.onNodeWithText("No pudimos guardar los cambios.").assertIsDisplayed()
    }
}
