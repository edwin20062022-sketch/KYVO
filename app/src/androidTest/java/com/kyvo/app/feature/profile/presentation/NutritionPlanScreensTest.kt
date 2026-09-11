package com.kyvo.app.feature.profile.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
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

class NutritionPlanScreensTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun nutritionPlanShowsPersistedValues() {
        composeRule.setContent { KyvoTheme { NutritionPlanScreen(NutritionPlanUiState.Content(sample())) } }
        composeRule.onNodeWithText("Mi plan nutricional").assertIsDisplayed()
        composeRule.onNodeWithText("Recomposición corporal").assertIsDisplayed()
        composeRule.onNodeWithText("2300 kcal").assertIsDisplayed()
        composeRule.onNodeWithText("160 g").assertIsDisplayed()
        composeRule.onNodeWithText("225 g").assertIsDisplayed()
        composeRule.onNodeWithText("70 g").assertIsDisplayed()
    }

    @Test
    fun calculationScreenShowsRealSteps() {
        composeRule.setContent { KyvoTheme { NutritionCalculationScreen(sample(), onBack = {}) } }
        composeRule.onNodeWithText("Cómo calculamos tus metas").assertIsDisplayed()
        composeRule.onNodeWithText("Metabolismo basal (BMR)").assertIsDisplayed()
        composeRule.onNodeWithText("1637 kcal/día").assertIsDisplayed()
        composeRule.onNodeWithText("Nivel de actividad").assertIsDisplayed()
        composeRule.onNodeWithText("Ajuste por objetivo").assertIsDisplayed()
    }

    @Test
    fun missingPlanShowsIncompleteState() {
        composeRule.setContent { KyvoTheme { NutritionPlanScreen(NutritionPlanUiState.Incomplete(SavedOnboarding())) } }
        composeRule.onNodeWithText("Tu plan aún no está disponible.").assertIsDisplayed()
    }

    private fun sample() = SavedOnboarding(
        gender = GenderOption.Male, ageYears = 28, heightCm = 180.0, weightKg = 75.0,
        trainingDaysPerWeek = 5, trainingType = TrainingType.Hypertrophy, workActivity = WorkActivity.Sedentary,
        goal = FitnessGoal.Recomposition, mealsPerDay = 4,
        plan = NutritionPlan(1637.0, 2537.0, 1.55, -0.05, 2300, 160, 225, 70), isCompleted = true,
    )
}
