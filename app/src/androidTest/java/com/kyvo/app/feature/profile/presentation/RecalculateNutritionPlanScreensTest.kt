package com.kyvo.app.feature.profile.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.kyvo.app.core.designsystem.KyvoTheme
import com.kyvo.app.feature.onboarding.domain.model.FitnessGoal
import com.kyvo.app.feature.onboarding.domain.model.GenderOption
import com.kyvo.app.feature.onboarding.domain.model.NutritionPlan
import com.kyvo.app.feature.onboarding.domain.model.SavedOnboarding
import com.kyvo.app.feature.onboarding.domain.model.TrainingType
import com.kyvo.app.feature.onboarding.domain.model.WorkActivity
import org.junit.Rule
import org.junit.Test

class RecalculateNutritionPlanScreensTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun updateGoalShowsCurrentSelectionAndAllExistingGoals() {
        composeRule.setContent {
            KyvoTheme {
                UpdateGoalScreen(
                    RecalculateNutritionPlanUiState.Editing(sample(), FitnessGoal.Recomposition),
                )
            }
        }
        composeRule.onNodeWithText("Actualizar objetivo").assertIsDisplayed()
        composeRule.onNodeWithText("Recomposición corporal").assertIsDisplayed()
        composeRule.onNodeWithText("Ganancia muscular").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Continuar con el nuevo objetivo").assertIsDisplayed()
    }

    @Test
    fun newGoalsShowsPreviewAndNotifiesNewPlan() {
        val preview = RecalculateNutritionPlanUiState.Preview(sample(), FitnessGoal.MuscleGain, sample().plan!!, NutritionPlan(1740.0, 2523.0, 1.45, .10, 2780, 135, 348, 60))
        composeRule.setContent { KyvoTheme { NewGoalsScreen(preview) } }
        composeRule.onNodeWithText("Tus nuevas metas").assertIsDisplayed()
        composeRule.onNodeWithText("2780").assertIsDisplayed()
        composeRule.onNodeWithText("¡Comenzar!").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Aplicar nuevas metas").assertIsDisplayed()
    }

    private fun sample() = SavedOnboarding(
        gender = GenderOption.Male, ageYears = 28, heightCm = 180.0, weightKg = 75.0,
        trainingDaysPerWeek = 5, trainingType = TrainingType.Hypertrophy, workActivity = WorkActivity.Sedentary,
        goal = FitnessGoal.Recomposition, mealsPerDay = 4,
        plan = NutritionPlan(1637.0, 2537.0, 1.55, -0.05, 2300, 160, 225, 70), isCompleted = true,
    )
}
