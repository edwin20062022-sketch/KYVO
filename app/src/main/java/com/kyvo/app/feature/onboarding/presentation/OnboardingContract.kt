package com.kyvo.app.feature.onboarding.presentation

import androidx.compose.runtime.Immutable
import com.kyvo.app.feature.onboarding.domain.model.ExperienceLevel
import com.kyvo.app.feature.onboarding.domain.model.FitnessGoal
import com.kyvo.app.feature.onboarding.domain.model.FoodPreference
import com.kyvo.app.feature.onboarding.domain.model.GenderOption
import com.kyvo.app.feature.onboarding.domain.model.HeightUnit
import com.kyvo.app.feature.onboarding.domain.model.NutritionPlan
import com.kyvo.app.feature.onboarding.domain.model.OnboardingStep
import com.kyvo.app.feature.onboarding.domain.model.TrainingType
import com.kyvo.app.feature.onboarding.domain.model.WeightUnit
import com.kyvo.app.feature.onboarding.domain.model.WorkActivity
import com.kyvo.app.feature.onboarding.domain.validation.OnboardingValidationError

@Immutable
data class OnboardingUiState(
    val currentStep: OnboardingStep = OnboardingStep.Gender,
    val gender: GenderOption? = null,
    val ageInput: String = "",
    val heightInput: String = "",
    val heightUnit: HeightUnit = HeightUnit.Centimeters,
    val weightInput: String = "",
    val weightUnit: WeightUnit = WeightUnit.Kilograms,
    val trainingDaysPerWeek: Int? = null,
    val trainingType: TrainingType? = null,
    val workActivity: WorkActivity? = null,
    val goal: FitnessGoal? = null,
    val experience: ExperienceLevel? = null,
    val foodPreference: FoodPreference? = null,
    val mealsPerDay: Int? = null,
    val isCustomMeals: Boolean = false,
    val plan: NutritionPlan? = null,
    val validationError: OnboardingValidationError? = null,
    val isRestoring: Boolean = true,
    val shouldExit: Boolean = false,
    val shouldNavigateHome: Boolean = false,
)

sealed interface OnboardingEvent {
    data class SelectGender(val value: GenderOption) : OnboardingEvent
    data class ChangeAge(val value: String) : OnboardingEvent
    data class ChangeHeight(val value: String) : OnboardingEvent
    data class ChangeWeight(val value: String) : OnboardingEvent
    data class SelectTrainingDays(val value: Int) : OnboardingEvent
    data class SelectTrainingType(val value: TrainingType) : OnboardingEvent
    data class SelectWorkActivity(val value: WorkActivity) : OnboardingEvent
    data class SelectGoal(val value: FitnessGoal) : OnboardingEvent
    data class SelectExperience(val value: ExperienceLevel) : OnboardingEvent
    data class SelectFoodPreference(val value: FoodPreference) : OnboardingEvent
    data class SelectMeals(val value: Int?, val custom: Boolean = false) : OnboardingEvent
    data object Continue : OnboardingEvent
    data object Back : OnboardingEvent
    data object NavigationHandled : OnboardingEvent
}
