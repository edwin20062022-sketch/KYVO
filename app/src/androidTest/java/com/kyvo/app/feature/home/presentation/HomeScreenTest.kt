package com.kyvo.app.feature.home.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.kyvo.app.core.designsystem.KyvoTheme
import com.kyvo.app.feature.home.domain.model.DailyNutrition
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {
    @get:Rule val compose = createComposeRule()
    private val emptyDaily = DailyNutrition(LocalDate.of(2026, 9, 8), 2300, 0, 2300, 160, 0, 225, 0, 70, 0, emptyList())

    @Test fun emptyDashboardShowsMealCallToAction() {
        compose.setContent { KyvoTheme { HomeScreen(HomeUiState.Content(emptyDaily)) {} } }
        compose.onNodeWithText("Aún no registras comidas").assertIsDisplayed()
        compose.onNodeWithText("Añadir comida").assertIsDisplayed()
    }
}
