package com.kyvo.app.feature.profile.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.kyvo.app.core.designsystem.KyvoTheme
import com.kyvo.app.feature.onboarding.domain.model.ExperienceLevel
import com.kyvo.app.feature.onboarding.domain.model.FitnessGoal
import com.kyvo.app.feature.onboarding.domain.model.FoodPreference
import com.kyvo.app.feature.onboarding.domain.model.GenderOption
import com.kyvo.app.feature.onboarding.domain.model.NutritionPlan
import com.kyvo.app.feature.onboarding.domain.model.SavedOnboarding
import com.kyvo.app.feature.onboarding.domain.model.TrainingType
import com.kyvo.app.feature.onboarding.domain.model.WorkActivity
import org.junit.Rule
import org.junit.Test

class PersonalPreferencesScreensTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun personalDataShowsRealInitialValues() {
        composeRule.setContent { KyvoTheme { PersonalDataScreen(PersonalPreferencesUiState.Editing(sample(), sample())) } }
        composeRule.onNodeWithText("Datos personales").assertIsDisplayed()
        composeRule.onNodeWithText("Masculino").assertIsDisplayed()
        composeRule.onNodeWithText("28").assertIsDisplayed()
        composeRule.onNodeWithText("180").assertIsDisplayed()
        composeRule.onNodeWithText("75").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Guardar datos personales").assertIsDisplayed()
    }

    @Test
    fun activityScreenShowsRealSelections() {
        composeRule.setContent { KyvoTheme { ActivityTrainingScreen(PersonalPreferencesUiState.Editing(sample(), sample())) } }
        composeRule.onNodeWithText("Actividad y entrenamiento").assertIsDisplayed()
        composeRule.onNodeWithText("5").assertIsDisplayed()
        composeRule.onNodeWithText("Hipertrofia").assertIsDisplayed()
        composeRule.onNodeWithText("Oficina").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Guardar actividad y entrenamiento").assertIsDisplayed()
    }

    private fun sample() = SavedOnboarding(
        gender = GenderOption.Male, ageYears = 28, heightCm = 180.0, weightKg = 75.0,
        trainingDaysPerWeek = 5, trainingType = TrainingType.Hypertrophy, workActivity = WorkActivity.Sedentary,
        goal = FitnessGoal.Recomposition, experience = ExperienceLevel.Intermediate, foodPreference = FoodPreference.None,
        mealsPerDay = 4, plan = NutritionPlan(1637.0, 2537.0, 1.55, -0.05, 2300, 160, 225, 70), isCompleted = true,
    )
}
