package com.kyvo.app.feature.onboarding.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kyvo.app.core.designsystem.KyvoTheme
import com.kyvo.app.feature.onboarding.data.InMemoryOnboardingRepository
import com.kyvo.app.feature.onboarding.domain.calculator.NutritionPlanCalculator
import com.kyvo.app.feature.onboarding.domain.model.ExperienceLevel
import com.kyvo.app.feature.onboarding.domain.model.FitnessGoal
import com.kyvo.app.feature.onboarding.domain.model.FoodPreference
import com.kyvo.app.feature.onboarding.domain.model.GenderOption
import com.kyvo.app.feature.onboarding.domain.model.OnboardingAnswers
import com.kyvo.app.feature.onboarding.domain.model.OnboardingStep
import com.kyvo.app.feature.onboarding.domain.model.TrainingType
import com.kyvo.app.feature.onboarding.domain.model.WorkActivity
import com.kyvo.app.feature.onboarding.domain.validation.OnboardingValidationError
import com.kyvo.app.feature.onboarding.presentation.components.BACK_TAG
import com.kyvo.app.feature.onboarding.presentation.components.CONTINUE_TAG
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class OnboardingScreenTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun navigatesFromGenderToAgeAndBack() {
        val viewModel = OnboardingViewModel(InMemoryOnboardingRepository(), transitionMillis = 0)
        composeRule.setContent {
            KyvoTheme {
                OnboardingRoute(repository = InMemoryOnboardingRepository(), onExit = {}, onCompleted = {}, viewModel = viewModel)
            }
        }
        composeRule.onNodeWithTag(OPTION_TAG_PREFIX + "Masculino").performClick()
        composeRule.onNodeWithTag(CONTINUE_TAG).performClick()
        composeRule.onNodeWithText("¿Cuál es tu edad?").assertIsDisplayed()
        composeRule.onNodeWithTag(BACK_TAG).performClick()
        composeRule.onNodeWithText("¿Cuál es tu género?").assertIsDisplayed()
        composeRule.onNodeWithTag(OPTION_TAG_PREFIX + "Masculino").assertIsDisplayed()
    }

    @Test fun displaysContextualValidationError() {
        composeRule.setContent {
            KyvoTheme {
                OnboardingScreen(
                    OnboardingUiState(isRestoring = false, validationError = OnboardingValidationError.SelectionRequired),
                    onEvent = {},
                )
            }
        }
        composeRule.onNodeWithTag(ERROR_TAG).assertIsDisplayed()
    }

    @Test fun selectingCardEmitsTypedEvent() {
        var event: OnboardingEvent? = null
        composeRule.setContent {
            KyvoTheme { OnboardingScreen(OnboardingUiState(isRestoring = false), onEvent = { event = it }) }
        }
        composeRule.onNodeWithTag(OPTION_TAG_PREFIX + "Femenino").performClick()
        assertEquals(OnboardingEvent.SelectGender(GenderOption.Female), event)
    }

    @Test fun calorieRevealDisplaysCalculatedValue() {
        val plan = NutritionPlanCalculator().calculate(sampleAnswers())
        composeRule.setContent {
            KyvoTheme {
                OnboardingScreen(
                    OnboardingUiState(currentStep = OnboardingStep.CalorieReveal, plan = plan, isRestoring = false),
                    onEvent = {},
                )
            }
        }
        composeRule.onNodeWithText("Tu plan de calorías está listo").assertIsDisplayed()
        composeRule.onNodeWithText("Ver mis macros").assertIsDisplayed()
    }

    @Test fun summaryCompletionEmitsContinue() {
        var event: OnboardingEvent? = null
        val answers = sampleAnswers()
        val plan = NutritionPlanCalculator().calculate(answers)
        composeRule.setContent {
            KyvoTheme {
                OnboardingScreen(
                    OnboardingUiState(
                        currentStep = OnboardingStep.Summary,
                        gender = answers.gender,
                        ageInput = answers.ageYears.toString(),
                        heightInput = answers.heightCm.toInt().toString(),
                        weightInput = answers.weightKg.toInt().toString(),
                        trainingDaysPerWeek = answers.trainingDaysPerWeek,
                        trainingType = answers.trainingType,
                        workActivity = answers.workActivity,
                        goal = answers.goal,
                        experience = answers.experience,
                        foodPreference = answers.foodPreference,
                        mealsPerDay = answers.mealsPerDay,
                        plan = plan,
                        isRestoring = false,
                    ),
                    onEvent = { event = it },
                )
            }
        }
        composeRule.onNodeWithTag(CONTINUE_TAG).performClick()
        assertEquals(OnboardingEvent.Continue, event)
    }

    private fun sampleAnswers() = OnboardingAnswers(
        GenderOption.Male, 30, 175.0, 75.0, 5, TrainingType.Strength,
        WorkActivity.Sedentary, FitnessGoal.Maintenance, ExperienceLevel.Intermediate,
        FoodPreference.None, 4,
    )
}
