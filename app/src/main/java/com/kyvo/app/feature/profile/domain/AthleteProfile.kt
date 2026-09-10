package com.kyvo.app.feature.profile.domain

import com.kyvo.app.feature.onboarding.domain.model.ExperienceLevel
import com.kyvo.app.feature.onboarding.domain.model.FitnessGoal
import com.kyvo.app.feature.onboarding.domain.model.FoodPreference
import com.kyvo.app.feature.onboarding.domain.model.NutritionPlan
import com.kyvo.app.feature.onboarding.domain.model.TrainingType

data class AthleteProfile(
    val displayName: String?,
    val ageYears: Int?,
    val heightCm: Double?,
    val weightKg: Double?,
    val trainingDaysPerWeek: Int?,
    val trainingType: TrainingType?,
    val goal: FitnessGoal?,
    val experience: ExperienceLevel?,
    val foodPreference: FoodPreference?,
    val mealsPerDay: Int?,
    val nutritionPlan: NutritionPlan?,
)
