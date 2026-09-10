package com.kyvo.app.feature.profile

import com.kyvo.app.domain.auth.AuthProvider
import com.kyvo.app.domain.auth.AuthSession
import com.kyvo.app.feature.onboarding.domain.model.ExperienceLevel
import com.kyvo.app.feature.onboarding.domain.model.FitnessGoal
import com.kyvo.app.feature.onboarding.domain.model.FoodPreference
import com.kyvo.app.feature.onboarding.domain.model.NutritionPlan
import com.kyvo.app.feature.onboarding.domain.model.SavedOnboarding
import com.kyvo.app.feature.onboarding.domain.model.TrainingType
import com.kyvo.app.feature.profile.presentation.toAthleteProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AthleteProfileMappingTest {
    @Test
    fun `maps persisted onboarding and nutrition plan without recalculating`() {
        val plan = NutritionPlan(1800.0, 2300.0, 1.5, .0, 2300, 160, 225, 70)
        val saved = SavedOnboarding(
            ageYears = 30,
            heightCm = 175.0,
            weightKg = 75.0,
            trainingDaysPerWeek = 5,
            trainingType = TrainingType.Hypertrophy,
            goal = FitnessGoal.Recomposition,
            experience = ExperienceLevel.Intermediate,
            foodPreference = FoodPreference.None,
            mealsPerDay = 4,
            plan = plan,
            isCompleted = true,
        )

        val result = saved.toAthleteProfile(AuthSession("user-1", "athlete@kyvo.app", AuthProvider.Email, "Alex Martínez"))

        assertEquals("Alex Martínez", result.displayName)
        assertEquals(5, result.trainingDaysPerWeek)
        assertEquals(FitnessGoal.Recomposition, result.goal)
        assertEquals(plan, result.nutritionPlan)
    }

    @Test
    fun `missing profile values remain missing instead of becoming fake zeros`() {
        val result = SavedOnboarding().toAthleteProfile(AuthSession("user-1", "athlete@kyvo.app", AuthProvider.Email))

        assertNull(result.displayName)
        assertNull(result.weightKg)
        assertNull(result.nutritionPlan)
    }
}
