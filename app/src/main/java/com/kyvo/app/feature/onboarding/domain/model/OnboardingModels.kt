package com.kyvo.app.feature.onboarding.domain.model

enum class OnboardingStep(val number: Int) {
    Gender(1), Age(2), Height(3), CurrentWeight(4), TrainingDays(5), TrainingType(6),
    WorkActivity(7), Goal(8), Experience(9), FoodPreference(10), MealsPerDay(11),
    Calculating(12), CalorieReveal(13), MacroReveal(14), Summary(15);

    fun previous(): OnboardingStep? = entries.getOrNull(ordinal - 1)
    fun next(): OnboardingStep? = entries.getOrNull(ordinal + 1)

    companion object { const val Total = 15 }
}

enum class GenderOption { Male, Female, PreferNotToSay }
enum class HeightUnit { Centimeters, FeetInches }
enum class WeightUnit { Kilograms, Pounds }
enum class TrainingType { Strength, Hypertrophy, Functional, Cardio }
enum class WorkActivity { Sedentary, Active, Physical }
enum class FitnessGoal { FatLoss, MuscleGain, Recomposition, Maintenance, Performance }
enum class ExperienceLevel { Beginner, Intermediate, Advanced }
enum class FoodPreference { None, Vegetarian, Vegan, GlutenFree, DairyFree, Other }

data class OnboardingAnswers(
    val gender: GenderOption,
    val ageYears: Int,
    val heightCm: Double,
    val weightKg: Double,
    val trainingDaysPerWeek: Int,
    val trainingType: TrainingType,
    val workActivity: WorkActivity,
    val goal: FitnessGoal,
    val experience: ExperienceLevel,
    val foodPreference: FoodPreference,
    val mealsPerDay: Int,
    val customDietaryRestrictions: List<String> = emptyList(),
)

data class NutritionPlan(
    val bmrKcal: Double,
    val tdeeKcal: Double,
    val activityFactor: Double,
    val goalAdjustmentFraction: Double,
    val targetCaloriesKcal: Int,
    val proteinGrams: Int,
    val carbohydrateGrams: Int,
    val fatGrams: Int,
    val calculationNote: String? = null,
) {
    val macroCalories: Int get() = proteinGrams * 4 + carbohydrateGrams * 4 + fatGrams * 9
}

data class SavedOnboarding(
    val currentStep: OnboardingStep = OnboardingStep.Gender,
    val gender: GenderOption? = null,
    val ageYears: Int? = null,
    val heightCm: Double? = null,
    val weightKg: Double? = null,
    val trainingDaysPerWeek: Int? = null,
    val trainingType: TrainingType? = null,
    val workActivity: WorkActivity? = null,
    val goal: FitnessGoal? = null,
    val experience: ExperienceLevel? = null,
    val foodPreference: FoodPreference? = null,
    val customDietaryRestrictions: List<String> = emptyList(),
    val mealsPerDay: Int? = null,
    val plan: NutritionPlan? = null,
    val isCompleted: Boolean = false,
)
