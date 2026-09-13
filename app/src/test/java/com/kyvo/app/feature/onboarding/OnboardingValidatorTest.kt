package com.kyvo.app.feature.onboarding.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Test

class OnboardingValidatorTest {
    @Test fun ageBoundariesAreValid() {
        assertEquals(ValidationResult.Valid, OnboardingValidator.age("1"))
        assertEquals(ValidationResult.Valid, OnboardingValidator.age("150"))
    }

    @Test fun invalidAgeIsRejected() {
        assertEquals(ValidationResult.Invalid(OnboardingValidationError.InvalidNumber), OnboardingValidator.age("abc"))
        assertEquals(ValidationResult.Invalid(OnboardingValidationError.AgeOutOfRange), OnboardingValidator.age("0"))
        assertEquals(ValidationResult.Invalid(OnboardingValidationError.AgeOutOfRange), OnboardingValidator.age("151"))
    }

    @Test fun heightAndWeightUseCentralRanges() {
        assertEquals(ValidationResult.Valid, OnboardingValidator.heightCm("175.5"))
        assertEquals(ValidationResult.Valid, OnboardingValidator.heightCm("100"))
        assertEquals(ValidationResult.Valid, OnboardingValidator.heightCm("300"))
        assertEquals(ValidationResult.Invalid(OnboardingValidationError.HeightOutOfRange), OnboardingValidator.heightCm("99"))
        assertEquals(ValidationResult.Invalid(OnboardingValidationError.HeightOutOfRange), OnboardingValidator.heightCm("301"))
        assertEquals(ValidationResult.Valid, OnboardingValidator.weightKg("75.2"))
        assertEquals(ValidationResult.Invalid(OnboardingValidationError.WeightOutOfRange), OnboardingValidator.weightKg("20"))
    }

    @Test fun trainingAndMealsRangesAreEnforced() {
        assertEquals(ValidationResult.Valid, OnboardingValidator.trainingDays(7))
        assertEquals(ValidationResult.Invalid(OnboardingValidationError.TrainingDaysOutOfRange), OnboardingValidator.trainingDays(0))
        assertEquals(ValidationResult.Valid, OnboardingValidator.meals(8))
        assertEquals(ValidationResult.Invalid(OnboardingValidationError.MealsOutOfRange), OnboardingValidator.meals(9))
    }
}
