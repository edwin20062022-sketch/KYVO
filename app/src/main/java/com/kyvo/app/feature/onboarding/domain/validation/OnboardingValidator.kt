package com.kyvo.app.feature.onboarding.domain.validation

object OnboardingLimits {
    const val MinimumAge = 16
    const val MaximumAge = 100
    const val MinimumHeightCm = 120.0
    const val MaximumHeightCm = 230.0
    const val MinimumWeightKg = 35.0
    const val MaximumWeightKg = 300.0
    const val MinimumTrainingDays = 1
    const val MaximumTrainingDays = 7
    const val MinimumMeals = 1
    const val MaximumMeals = 8
}

sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val error: OnboardingValidationError) : ValidationResult
}

enum class OnboardingValidationError {
    SelectionRequired, InvalidNumber, AgeOutOfRange, HeightOutOfRange, WeightOutOfRange,
    TrainingDaysOutOfRange, MealsOutOfRange,
}

object OnboardingValidator {
    fun age(value: String): ValidationResult = validateInt(
        value, OnboardingLimits.MinimumAge..OnboardingLimits.MaximumAge,
        OnboardingValidationError.AgeOutOfRange,
    )

    fun heightCm(value: String): ValidationResult = validateDouble(
        value, OnboardingLimits.MinimumHeightCm..OnboardingLimits.MaximumHeightCm,
        OnboardingValidationError.HeightOutOfRange,
    )

    fun weightKg(value: String): ValidationResult = validateDouble(
        value, OnboardingLimits.MinimumWeightKg..OnboardingLimits.MaximumWeightKg,
        OnboardingValidationError.WeightOutOfRange,
    )

    fun trainingDays(value: Int?): ValidationResult = if (value in OnboardingLimits.MinimumTrainingDays..OnboardingLimits.MaximumTrainingDays) {
        ValidationResult.Valid
    } else ValidationResult.Invalid(OnboardingValidationError.TrainingDaysOutOfRange)

    fun meals(value: Int?): ValidationResult = if (value in OnboardingLimits.MinimumMeals..OnboardingLimits.MaximumMeals) {
        ValidationResult.Valid
    } else ValidationResult.Invalid(OnboardingValidationError.MealsOutOfRange)

    private fun validateInt(value: String, range: IntRange, rangeError: OnboardingValidationError): ValidationResult {
        val parsed = value.toIntOrNull() ?: return ValidationResult.Invalid(OnboardingValidationError.InvalidNumber)
        return if (parsed in range) ValidationResult.Valid else ValidationResult.Invalid(rangeError)
    }

    private fun validateDouble(value: String, range: ClosedFloatingPointRange<Double>, rangeError: OnboardingValidationError): ValidationResult {
        val parsed = value.toDoubleOrNull() ?: return ValidationResult.Invalid(OnboardingValidationError.InvalidNumber)
        return if (parsed in range) ValidationResult.Valid else ValidationResult.Invalid(rangeError)
    }
}
