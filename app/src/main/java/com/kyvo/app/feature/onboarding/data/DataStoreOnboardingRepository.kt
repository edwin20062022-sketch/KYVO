package com.kyvo.app.feature.onboarding.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kyvo.app.feature.onboarding.domain.model.ExperienceLevel
import com.kyvo.app.feature.onboarding.domain.model.FitnessGoal
import com.kyvo.app.feature.onboarding.domain.model.FoodPreference
import com.kyvo.app.feature.onboarding.domain.model.GenderOption
import com.kyvo.app.feature.onboarding.domain.model.NutritionPlan
import com.kyvo.app.feature.onboarding.domain.model.OnboardingStep
import com.kyvo.app.feature.onboarding.domain.model.SavedOnboarding
import com.kyvo.app.feature.onboarding.domain.model.TrainingType
import com.kyvo.app.feature.onboarding.domain.model.WorkActivity
import com.kyvo.app.feature.onboarding.domain.repository.OnboardingRepository
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.onboardingDataStore by preferencesDataStore(name = "kyvo_onboarding")

class DataStoreOnboardingRepository(context: Context, userId: String) : OnboardingRepository {
    private val dataStore = context.applicationContext.onboardingDataStore
    private val keys = Keys(userId.also { require(it.isNotBlank()) { "userId must not be blank" } })

    override fun observe(): Flow<SavedOnboarding> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map(::decode)

    override suspend fun save(progress: SavedOnboarding) {
        dataStore.edit { preferences ->
            keys.all.forEach { preferences.remove(it) }
            preferences[keys.Step] = progress.currentStep.name
            preferences[keys.Completed] = progress.isCompleted
            progress.gender?.let { preferences[keys.Gender] = it.name }
            progress.ageYears?.let { preferences[keys.Age] = it }
            progress.heightCm?.let { preferences[keys.Height] = it }
            progress.weightKg?.let { preferences[keys.Weight] = it }
            progress.trainingDaysPerWeek?.let { preferences[keys.TrainingDays] = it }
            progress.trainingType?.let { preferences[keys.TrainingTypeKey] = it.name }
            progress.workActivity?.let { preferences[keys.WorkActivityKey] = it.name }
            progress.goal?.let { preferences[keys.Goal] = it.name }
            progress.experience?.let { preferences[keys.Experience] = it.name }
            progress.foodPreference?.let { preferences[keys.FoodPreferenceKey] = it.name }
            if (progress.customDietaryRestrictions.isNotEmpty()) {
                preferences[keys.CustomRestrictions] = progress.customDietaryRestrictions.joinToString(separator = "\u001F")
            }
            progress.mealsPerDay?.let { preferences[keys.Meals] = it }
            progress.plan?.let { plan ->
                preferences[keys.Bmr] = plan.bmrKcal
                preferences[keys.Tdee] = plan.tdeeKcal
                preferences[keys.ActivityFactor] = plan.activityFactor
                preferences[keys.GoalAdjustment] = plan.goalAdjustmentFraction
                preferences[keys.TargetCalories] = plan.targetCaloriesKcal
                preferences[keys.Protein] = plan.proteinGrams
                preferences[keys.Carbohydrates] = plan.carbohydrateGrams
                preferences[keys.Fat] = plan.fatGrams
                plan.calculationNote?.let { preferences[keys.CalculationNote] = it }
            }
        }
    }

    override suspend fun clearUserData() {
        dataStore.edit { preferences -> keys.all.forEach { key -> preferences.remove(key) } }
    }

    private fun decode(preferences: Preferences): SavedOnboarding = SavedOnboarding(
        currentStep = preferences[keys.Step].enumValueOr(OnboardingStep.Gender),
        gender = preferences[keys.Gender].enumValueOrNull(),
        ageYears = preferences[keys.Age],
        heightCm = preferences[keys.Height],
        weightKg = preferences[keys.Weight],
        trainingDaysPerWeek = preferences[keys.TrainingDays],
        trainingType = preferences[keys.TrainingTypeKey].enumValueOrNull(),
        workActivity = preferences[keys.WorkActivityKey].enumValueOrNull(),
        goal = preferences[keys.Goal].enumValueOrNull(),
        experience = preferences[keys.Experience].enumValueOrNull(),
        foodPreference = preferences[keys.FoodPreferenceKey].enumValueOrNull(),
        customDietaryRestrictions = preferences[keys.CustomRestrictions]
            ?.split("\u001F")
            ?.filter { it.isNotBlank() }
            ?: emptyList(),
        mealsPerDay = preferences[keys.Meals],
        plan = decodePlan(preferences),
        isCompleted = preferences[keys.Completed] ?: false,
    )

    private fun decodePlan(preferences: Preferences): NutritionPlan? {
        val calories = preferences[keys.TargetCalories] ?: return null
        return NutritionPlan(
            bmrKcal = preferences[keys.Bmr] ?: return null,
            tdeeKcal = preferences[keys.Tdee] ?: return null,
            activityFactor = preferences[keys.ActivityFactor] ?: return null,
            goalAdjustmentFraction = preferences[keys.GoalAdjustment] ?: return null,
            targetCaloriesKcal = calories,
            proteinGrams = preferences[keys.Protein] ?: return null,
            carbohydrateGrams = preferences[keys.Carbohydrates] ?: return null,
            fatGrams = preferences[keys.Fat] ?: return null,
            calculationNote = preferences[keys.CalculationNote],
        )
    }

    private class Keys(userId: String) {
        private val prefix = onboardingPreferencePrefix(userId)
        val Step = stringPreferencesKey(prefix + "step")
        val Completed = booleanPreferencesKey(prefix + "completed")
        val Gender = stringPreferencesKey(prefix + "gender")
        val Age = intPreferencesKey(prefix + "age_years")
        val Height = doublePreferencesKey(prefix + "height_cm")
        val Weight = doublePreferencesKey(prefix + "weight_kg")
        val TrainingDays = intPreferencesKey(prefix + "training_days")
        val TrainingTypeKey = stringPreferencesKey(prefix + "training_type")
        val WorkActivityKey = stringPreferencesKey(prefix + "work_activity")
        val Goal = stringPreferencesKey(prefix + "goal")
        val Experience = stringPreferencesKey(prefix + "experience")
        val FoodPreferenceKey = stringPreferencesKey(prefix + "food_preference")
        val CustomRestrictions = stringPreferencesKey(prefix + "custom_restrictions")
        val Meals = intPreferencesKey(prefix + "meals_per_day")
        val Bmr = doublePreferencesKey(prefix + "bmr_kcal")
        val Tdee = doublePreferencesKey(prefix + "tdee_kcal")
        val ActivityFactor = doublePreferencesKey(prefix + "activity_factor")
        val GoalAdjustment = doublePreferencesKey(prefix + "goal_adjustment")
        val TargetCalories = intPreferencesKey(prefix + "target_calories")
        val Protein = intPreferencesKey(prefix + "protein_grams")
        val Carbohydrates = intPreferencesKey(prefix + "carbohydrate_grams")
        val Fat = intPreferencesKey(prefix + "fat_grams")
        val CalculationNote = stringPreferencesKey(prefix + "calculation_note")
        val all: List<Preferences.Key<*>> = listOf(
            Step, Completed, Gender, Age, Height, Weight, TrainingDays, TrainingTypeKey,
            WorkActivityKey, Goal, Experience, FoodPreferenceKey, CustomRestrictions, Meals, Bmr, Tdee,
            ActivityFactor, GoalAdjustment, TargetCalories, Protein, Carbohydrates, Fat,
            CalculationNote,
        )
    }
}

internal fun onboardingPreferencePrefix(userId: String): String {
    require(userId.isNotBlank()) { "userId must not be blank" }
    return "user_${userId}_"
}

private inline fun <reified T : Enum<T>> String?.enumValueOrNull(): T? =
    this?.let { stored -> enumValues<T>().firstOrNull { it.name == stored } }

private inline fun <reified T : Enum<T>> String?.enumValueOr(default: T): T =
    enumValueOrNull<T>() ?: default
