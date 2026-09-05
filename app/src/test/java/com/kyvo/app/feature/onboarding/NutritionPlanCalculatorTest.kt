package com.kyvo.app.feature.onboarding.domain.calculator

import com.kyvo.app.feature.onboarding.domain.model.ExperienceLevel
import com.kyvo.app.feature.onboarding.domain.model.FitnessGoal
import com.kyvo.app.feature.onboarding.domain.model.FoodPreference
import com.kyvo.app.feature.onboarding.domain.model.GenderOption
import com.kyvo.app.feature.onboarding.domain.model.OnboardingAnswers
import com.kyvo.app.feature.onboarding.domain.model.TrainingType
import com.kyvo.app.feature.onboarding.domain.model.WorkActivity
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NutritionPlanCalculatorTest {
    private val calculator = NutritionPlanCalculator()

    @Test
    fun mifflinStJeorUsesExplicitMaleConstant() {
        val plan = calculator.calculate(profile(gender = GenderOption.Male))
        assertEquals(1698.75, plan.bmrKcal, 0.001)
    }

    @Test
    fun mifflinStJeorUsesExplicitFemaleConstant() {
        val plan = calculator.calculate(profile(gender = GenderOption.Female))
        assertEquals(1532.75, plan.bmrKcal, 0.001)
    }

    @Test
    fun undisclosedGenderUsesDocumentedNeutralEstimate() {
        val plan = calculator.calculate(profile(gender = GenderOption.PreferNotToSay))
        assertEquals(1615.75, plan.bmrKcal, 0.001)
        assertNotNull(plan.calculationNote)
    }

    @Test
    fun activityCombinesWorkTrainingAndDaysDeterministically() {
        assertEquals(1.425, calculator.activityFactor(WorkActivity.Sedentary, TrainingType.Strength, 5), 0.0001)
        assertEquals(1.775, calculator.activityFactor(WorkActivity.Physical, TrainingType.Functional, 5), 0.0001)
        assertEquals(1.63, calculator.activityFactor(WorkActivity.Active, TrainingType.Cardio, 7), 0.0001)
    }

    @Test
    fun activityFactorIsCapped() {
        assertTrue(calculator.activityFactor(WorkActivity.Physical, TrainingType.Functional, 7) <= 1.90)
    }

    @Test
    fun goalAdjustmentsCoverAllStrategies() {
        assertEquals(-0.15, calculator.goalAdjustment(FitnessGoal.FatLoss), 0.0)
        assertEquals(0.10, calculator.goalAdjustment(FitnessGoal.MuscleGain), 0.0)
        assertEquals(-0.05, calculator.goalAdjustment(FitnessGoal.Recomposition), 0.0)
        assertEquals(0.0, calculator.goalAdjustment(FitnessGoal.Maintenance), 0.0)
        assertEquals(0.05, calculator.goalAdjustment(FitnessGoal.Performance), 0.0)
    }

    @Test
    fun fatLossProducesDeficitAndHigherProtein() {
        val plan = calculator.calculate(profile(goal = FitnessGoal.FatLoss))
        assertTrue(plan.targetCaloriesKcal < plan.tdeeKcal)
        assertEquals(150, plan.proteinGrams)
        assertEquals(60, plan.fatGrams)
    }

    @Test
    fun muscleGainProducesSurplus() {
        val plan = calculator.calculate(profile(goal = FitnessGoal.MuscleGain))
        assertTrue(plan.targetCaloriesKcal > plan.tdeeKcal)
    }

    @Test
    fun maintenanceMatchesRoundedTdee() {
        val plan = calculator.calculate(profile(goal = FitnessGoal.Maintenance))
        assertTrue(abs(plan.targetCaloriesKcal - plan.tdeeKcal) <= 5.0)
    }

    @Test
    fun performanceUsesSmallSurplus() {
        val plan = calculator.calculate(profile(goal = FitnessGoal.Performance))
        assertEquals(0.05, plan.goalAdjustmentFraction, 0.0)
        assertEquals(68, plan.fatGrams)
    }

    @Test
    fun macroEnergyMatchesTargetWithinRoundingTolerance() {
        FitnessGoal.entries.forEach { goal ->
            val plan = calculator.calculate(profile(goal = goal))
            assertTrue("Energy mismatch for $goal", abs(plan.macroCalories - plan.targetCaloriesKcal) <= 2)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidAge() {
        calculator.calculate(profile(age = 15))
    }

    @Test
    fun acceptsBoundaryValues() {
        val plan = calculator.calculate(profile(age = 100, height = 120.0, weight = 35.0, days = 1))
        assertTrue(plan.targetCaloriesKcal > 0)
        assertTrue(plan.carbohydrateGrams >= 0)
    }

    private fun profile(
        gender: GenderOption = GenderOption.Male,
        age: Int = 30,
        height: Double = 175.0,
        weight: Double = 75.0,
        days: Int = 5,
        goal: FitnessGoal = FitnessGoal.Maintenance,
    ) = OnboardingAnswers(
        gender = gender,
        ageYears = age,
        heightCm = height,
        weightKg = weight,
        trainingDaysPerWeek = days,
        trainingType = TrainingType.Strength,
        workActivity = WorkActivity.Sedentary,
        goal = goal,
        experience = ExperienceLevel.Intermediate,
        foodPreference = FoodPreference.None,
        mealsPerDay = 4,
    )
}
