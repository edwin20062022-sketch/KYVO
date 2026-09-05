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

class DataStoreOnboardingRepository(context: Context) : OnboardingRepository {
    private val dataStore = context.applicationContext.onboardingDataStore

    override fun observe(): Flow<SavedOnboarding> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map(::decode)

    override suspend fun save(progress: SavedOnboarding) {
        dataStore.edit { preferences ->
            preferences.clear()
            preferences[Keys.Step] = progress.currentStep.name
            preferences[Keys.Completed] = progress.isCompleted
            progress.gender?.let { preferences[Keys.Gender] = it.name }
            progress.ageYears?.let { preferences[Keys.Age] = it }
            progress.heightCm?.let { preferences[Keys.Height] = it }
            progress.weightKg?.let { preferences[Keys.Weight] = it }
            progress.trainingDaysPerWeek?.let { preferences[Keys.TrainingDays] = it }
            progress.trainingType?.let { preferences[Keys.TrainingTypeKey] = it.name }
            progress.workActivity?.let { preferences[Keys.WorkActivityKey] = it.name }
            progress.goal?.let { preferences[Keys.Goal] = it.name }
            progress.experience?.let { preferences[Keys.Experience] = it.name }
            progress.foodPreference?.let { preferences[Keys.FoodPreferenceKey] = it.name }
            progress.mealsPerDay?.let { preferences[Keys.Meals] = it }
            progress.plan?.let { plan ->
                preferences[Keys.Bmr] = plan.bmrKcal
                preferences[Keys.Tdee] = plan.tdeeKcal
                preferences[Keys.ActivityFactor] = plan.activityFactor
                preferences[Keys.GoalAdjustment] = plan.goalAdjustmentFraction
                preferences[Keys.TargetCalories] = plan.targetCaloriesKcal
                preferences[Keys.Protein] = plan.proteinGrams
                preferences[Keys.Carbohydrates] = plan.carbohydrateGrams
                preferences[Keys.Fat] = plan.fatGrams
                plan.calculationNote?.let { preferences[Keys.CalculationNote] = it }
            }
        }
    }

    private fun decode(preferences: Preferences): SavedOnboarding = SavedOnboarding(
        currentStep = preferences[Keys.Step].enumValueOr(OnboardingStep.Gender),
        gender = preferences[Keys.Gender].enumValueOrNull(),
        ageYears = preferences[Keys.Age],
        heightCm = preferences[Keys.Height],
        weightKg = preferences[Keys.Weight],
        trainingDaysPerWeek = preferences[Keys.TrainingDays],
        trainingType = preferences[Keys.TrainingTypeKey].enumValueOrNull(),
        workActivity = preferences[Keys.WorkActivityKey].enumValueOrNull(),
        goal = preferences[Keys.Goal].enumValueOrNull(),
        experience = preferences[Keys.Experience].enumValueOrNull(),
        foodPreference = preferences[Keys.FoodPreferenceKey].enumValueOrNull(),
        mealsPerDay = preferences[Keys.Meals],
        plan = decodePlan(preferences),
        isCompleted = preferences[Keys.Completed] ?: false,
    )

    private fun decodePlan(preferences: Preferences): NutritionPlan? {
        val calories = preferences[Keys.TargetCalories] ?: return null
        return NutritionPlan(
            bmrKcal = preferences[Keys.Bmr] ?: return null,
            tdeeKcal = preferences[Keys.Tdee] ?: return null,
            activityFactor = preferences[Keys.ActivityFactor] ?: return null,
            goalAdjustmentFraction = preferences[Keys.GoalAdjustment] ?: return null,
            targetCaloriesKcal = calories,
            proteinGrams = preferences[Keys.Protein] ?: return null,
            carbohydrateGrams = preferences[Keys.Carbohydrates] ?: return null,
            fatGrams = preferences[Keys.Fat] ?: return null,
            calculationNote = preferences[Keys.CalculationNote],
        )
    }

    private object Keys {
        val Step = stringPreferencesKey("step")
        val Completed = booleanPreferencesKey("completed")
        val Gender = stringPreferencesKey("gender")
        val Age = intPreferencesKey("age_years")
        val Height = doublePreferencesKey("height_cm")
        val Weight = doublePreferencesKey("weight_kg")
        val TrainingDays = intPreferencesKey("training_days")
        val TrainingTypeKey = stringPreferencesKey("training_type")
        val WorkActivityKey = stringPreferencesKey("work_activity")
        val Goal = stringPreferencesKey("goal")
        val Experience = stringPreferencesKey("experience")
        val FoodPreferenceKey = stringPreferencesKey("food_preference")
        val Meals = intPreferencesKey("meals_per_day")
        val Bmr = doublePreferencesKey("bmr_kcal")
        val Tdee = doublePreferencesKey("tdee_kcal")
        val ActivityFactor = doublePreferencesKey("activity_factor")
        val GoalAdjustment = doublePreferencesKey("goal_adjustment")
        val TargetCalories = intPreferencesKey("target_calories")
        val Protein = intPreferencesKey("protein_grams")
        val Carbohydrates = intPreferencesKey("carbohydrate_grams")
        val Fat = intPreferencesKey("fat_grams")
        val CalculationNote = stringPreferencesKey("calculation_note")
    }
}

private inline fun <reified T : Enum<T>> String?.enumValueOrNull(): T? =
    this?.let { stored -> enumValues<T>().firstOrNull { it.name == stored } }

private inline fun <reified T : Enum<T>> String?.enumValueOr(default: T): T =
    enumValueOrNull<T>() ?: default
