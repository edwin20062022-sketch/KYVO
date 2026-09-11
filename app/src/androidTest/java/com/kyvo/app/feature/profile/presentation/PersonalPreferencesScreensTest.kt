package com.kyvo.app.feature.profile.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
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

    @Test
    fun experienceScreenShowsCurrentSelectionAndContinue() {
        composeRule.setContent { KyvoTheme { ExperienceScreen(PersonalPreferencesUiState.Editing(sample(), sample())) } }
        composeRule.onNodeWithText("Nivel de experiencia").assertIsDisplayed()
        composeRule.onNodeWithText("Intermedio").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Guardar experiencia y continuar").assertIsDisplayed()
    }

    @Test
    fun dietaryScreenShowsCurrentSelectionAndOptions() {
        composeRule.setContent { KyvoTheme { FoodPreferencesScreen(PersonalPreferencesUiState.Editing(sample(), sample())) } }
        composeRule.onNodeWithText("Preferencias alimenticias").assertIsDisplayed()
        composeRule.onNodeWithText("Sin restricciones").assertIsDisplayed()
        composeRule.onNodeWithText("Vegano").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Guardar preferencias alimenticias").assertIsDisplayed()
    }

    @Test
    fun dietaryScreenBlocksContinueWithoutPreference() {
        composeRule.setContent { KyvoTheme { FoodPreferencesScreen(PersonalPreferencesUiState.Editing(sample(), sample().copy(foodPreference = null))) } }
        composeRule.onNodeWithContentDescription("Guardar preferencias alimenticias").assertIsNotEnabled()
    }

    @Test
    fun mealOrganizationScreenShowsCurrentSelectionAndPreview() {
        composeRule.setContent { KyvoTheme { MealOrganizationScreen(PersonalPreferencesUiState.Editing(sample(), sample())) } }
        composeRule.onNodeWithText("Organización de comidas").assertIsDisplayed()
        composeRule.onNodeWithText("4").assertIsDisplayed()
        composeRule.onNodeWithText("Desayuno").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Guardar organización de comidas").assertIsDisplayed()
    }

    @Test
    fun reviewScreenShowsChangedFieldsAndPlanPreview() {
        val current = sample()
        val draft = current.copy(weightKg = 68.0, trainingDaysPerWeek = 4)
        val preview = current.plan!!.copy(targetCaloriesKcal = 2420, proteinGrams = 165, carbohydrateGrams = 248, fatGrams = 72)
        composeRule.setContent {
            KyvoTheme {
                ReviewGoalUpdatesScreen(
                    PersonalPreferencesUiState.Review(current, draft, true, true, requireNotNull(current.plan), preview),
                )
            }
        }
        composeRule.onNodeWithText("Tus datos han cambiado").assertIsDisplayed()
        composeRule.onNodeWithText("Cambios realizados").assertIsDisplayed()
        composeRule.onNodeWithText("2,300 kcal  |  2,420 kcal").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Confirmar actualización").assertIsDisplayed()
    }

    @Test
    fun confirmationScreenShowsFinalAction() {
        composeRule.setContent { KyvoTheme { PersonalPreferencesConfirmationScreen() } }
        composeRule.onNodeWithText("Cambios guardados").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Volver al perfil").assertIsDisplayed()
    }

    private fun sample() = SavedOnboarding(
        gender = GenderOption.Male, ageYears = 28, heightCm = 180.0, weightKg = 75.0,
        trainingDaysPerWeek = 5, trainingType = TrainingType.Hypertrophy, workActivity = WorkActivity.Sedentary,
        goal = FitnessGoal.Recomposition, experience = ExperienceLevel.Intermediate, foodPreference = FoodPreference.None,
        mealsPerDay = 4, plan = NutritionPlan(1637.0, 2537.0, 1.55, -0.05, 2300, 160, 225, 70), isCompleted = true,
    )
}
