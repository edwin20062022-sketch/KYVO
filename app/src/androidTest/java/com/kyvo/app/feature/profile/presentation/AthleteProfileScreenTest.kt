package com.kyvo.app.feature.profile.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.kyvo.app.core.designsystem.KyvoTheme
import com.kyvo.app.domain.auth.AuthProvider
import com.kyvo.app.domain.auth.AuthSession
import com.kyvo.app.feature.onboarding.domain.model.ExperienceLevel
import com.kyvo.app.feature.onboarding.domain.model.FitnessGoal
import com.kyvo.app.feature.onboarding.domain.model.FoodPreference
import com.kyvo.app.feature.onboarding.domain.model.NutritionPlan
import com.kyvo.app.feature.onboarding.domain.model.SavedOnboarding
import com.kyvo.app.feature.onboarding.domain.model.TrainingType
import com.kyvo.app.feature.profile.domain.AthleteProfile
import org.junit.Rule
import org.junit.Test

class AthleteProfileScreenTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun contentShowsRealProfileFieldsAndNutritionTargets() {
        composeRule.setContent { KyvoTheme { AthleteProfileScreen(AthleteProfileUiState.Content(sampleProfile())) } }

        composeRule.onNodeWithText("Alex Martínez").assertIsDisplayed()
        composeRule.onNodeWithText("Recomposición").assertIsDisplayed()
        composeRule.onNodeWithText("2300 kcal").assertIsDisplayed()
        composeRule.onNodeWithText("160g").assertIsDisplayed()
    }

    @Test
    fun loadingShowsProgressIndicator() {
        composeRule.setContent { KyvoTheme { AthleteProfileScreen(AthleteProfileUiState.Loading) } }
        composeRule.onNodeWithContentDescription("Perfil de atleta").assertIsDisplayed()
    }

    @Test
    fun incompleteDoesNotShowFakeData() {
        composeRule.setContent { KyvoTheme { AthleteProfileScreen(AthleteProfileUiState.Incomplete(AthleteProfile(null, null, null, null, null, null, null, null, null, null, null))) } }
        composeRule.onNodeWithText("Tu perfil").assertIsDisplayed()
        composeRule.onNodeWithText("Sin definir").assertIsDisplayed()
        composeRule.onNodeWithText("Tu plan aún no está disponible.").assertIsDisplayed()
    }

    @Test
    fun errorShowsRetryAction() {
        var retries = 0
        composeRule.setContent { KyvoTheme { AthleteProfileScreen(AthleteProfileUiState.Error("No pudimos cargar tu perfil."), onRetry = { retries++ }) } }
        composeRule.onNodeWithText("No pudimos cargar tu perfil.").assertIsDisplayed()
        composeRule.onNodeWithText("Reintentar").assertIsDisplayed()
    }

    private fun sampleProfile() = AthleteProfile(
        displayName = "Alex Martínez",
        ageYears = 30,
        heightCm = 175.0,
        weightKg = 75.0,
        trainingDaysPerWeek = 5,
        trainingType = TrainingType.Hypertrophy,
        goal = FitnessGoal.Recomposition,
        experience = ExperienceLevel.Intermediate,
        foodPreference = FoodPreference.None,
        mealsPerDay = 4,
        nutritionPlan = NutritionPlan(1800.0, 2300.0, 1.5, .0, 2300, 160, 225, 70),
    )
}
