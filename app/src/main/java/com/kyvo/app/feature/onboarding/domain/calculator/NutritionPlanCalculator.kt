package com.kyvo.app.feature.onboarding.domain.calculator

import com.kyvo.app.feature.onboarding.domain.model.FitnessGoal
import com.kyvo.app.feature.onboarding.domain.model.GenderOption
import com.kyvo.app.feature.onboarding.domain.model.NutritionPlan
import com.kyvo.app.feature.onboarding.domain.model.OnboardingAnswers
import com.kyvo.app.feature.onboarding.domain.model.TrainingType
import com.kyvo.app.feature.onboarding.domain.model.WorkActivity
import kotlin.math.roundToInt

/** Deterministic starting estimate, not a medical prescription. */
class NutritionPlanCalculator {
    fun calculate(input: OnboardingAnswers): NutritionPlan {
        require(input.ageYears in 16..100)
        require(input.heightCm in 120.0..230.0)
        require(input.weightKg in 35.0..300.0)
        require(input.trainingDaysPerWeek in 1..7)
        require(input.mealsPerDay in 1..8)

        val sexConstant = when (input.gender) {
            GenderOption.Male -> MALE_CONSTANT
            GenderOption.Female -> FEMALE_CONSTANT
            GenderOption.PreferNotToSay -> NEUTRAL_ESTIMATE_CONSTANT
        }
        val bmr = WEIGHT_COEFFICIENT * input.weightKg +
            HEIGHT_COEFFICIENT * input.heightCm -
            AGE_COEFFICIENT * input.ageYears + sexConstant
        val activityFactor = activityFactor(input.workActivity, input.trainingType, input.trainingDaysPerWeek)
        val tdee = bmr * activityFactor
        val adjustment = goalAdjustment(input.goal)
        val targetCalories = ((tdee * (1.0 + adjustment)) / 10.0).roundToInt() * 10

        val protein = (input.weightKg * proteinPerKg(input.goal)).roundToInt()
        val fat = (input.weightKg * fatPerKg(input.goal)).roundToInt()
        val carbohydrate = ((targetCalories - protein * KCAL_PER_GRAM_PROTEIN - fat * KCAL_PER_GRAM_FAT) /
            KCAL_PER_GRAM_CARBOHYDRATE.toDouble()).roundToInt().coerceAtLeast(0)

        return NutritionPlan(
            bmrKcal = bmr,
            tdeeKcal = tdee,
            activityFactor = activityFactor,
            goalAdjustmentFraction = adjustment,
            targetCaloriesKcal = targetCalories,
            proteinGrams = protein,
            carbohydrateGrams = carbohydrate,
            fatGrams = fat,
            calculationNote = if (input.gender == GenderOption.PreferNotToSay) NEUTRAL_ESTIMATE_NOTE else null,
        )
    }

    fun activityFactor(work: WorkActivity, training: TrainingType, days: Int): Double {
        require(days in 1..7)
        val baseline = when (work) {
            WorkActivity.Sedentary -> 1.20
            WorkActivity.Active -> 1.35
            WorkActivity.Physical -> 1.50
        }
        val perSession = when (training) {
            TrainingType.Strength -> 0.045
            TrainingType.Hypertrophy -> 0.050
            TrainingType.Functional -> 0.055
            TrainingType.Cardio -> 0.040
        }
        return (baseline + days * perSession).coerceAtMost(MAX_ACTIVITY_FACTOR)
    }

    fun goalAdjustment(goal: FitnessGoal): Double = when (goal) {
        FitnessGoal.FatLoss -> -0.15
        FitnessGoal.MuscleGain -> 0.10
        FitnessGoal.Recomposition -> -0.05
        FitnessGoal.Maintenance -> 0.0
        FitnessGoal.Performance -> 0.05
    }

    private fun proteinPerKg(goal: FitnessGoal): Double = when (goal) {
        FitnessGoal.FatLoss, FitnessGoal.Recomposition -> 2.0
        FitnessGoal.MuscleGain, FitnessGoal.Maintenance, FitnessGoal.Performance -> 1.8
    }

    private fun fatPerKg(goal: FitnessGoal): Double = if (goal == FitnessGoal.Performance) 0.9 else 0.8

    companion object {
        const val WEIGHT_COEFFICIENT = 10.0
        const val HEIGHT_COEFFICIENT = 6.25
        const val AGE_COEFFICIENT = 5.0
        const val MALE_CONSTANT = 5.0
        const val FEMALE_CONSTANT = -161.0
        const val NEUTRAL_ESTIMATE_CONSTANT = -78.0
        const val MAX_ACTIVITY_FACTOR = 1.90
        const val KCAL_PER_GRAM_PROTEIN = 4
        const val KCAL_PER_GRAM_CARBOHYDRATE = 4
        const val KCAL_PER_GRAM_FAT = 9
        const val NEUTRAL_ESTIMATE_NOTE = "Se usó el punto medio de las constantes de Mifflin-St Jeor porque elegiste no indicar sexo para el cálculo."
    }
}
