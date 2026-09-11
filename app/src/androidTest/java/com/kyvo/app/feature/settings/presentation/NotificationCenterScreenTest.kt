package com.kyvo.app.feature.settings.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.kyvo.app.core.designsystem.KyvoTheme
import org.junit.Rule
import org.junit.Test

class NotificationCenterScreenTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun centerShowsHonestEmptyStateAndRealSettingsActions() {
        composeRule.setContent { KyvoTheme(darkTheme = false) { NotificationCenterScreen() } }

        composeRule.onNodeWithContentDescription("notification_center_screen").assertIsDisplayed()
        composeRule.onNodeWithText("No tienes notificaciones").assertIsDisplayed()
        composeRule.onNodeWithText("Ajustes de notificaciones").assertIsDisplayed()
        composeRule.onNodeWithText("Permisos del sistema").assertIsDisplayed()
    }
}
