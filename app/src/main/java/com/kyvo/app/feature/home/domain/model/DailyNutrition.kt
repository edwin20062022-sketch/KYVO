package com.kyvo.app.feature.home.domain.model

import com.kyvo.app.feature.onboarding.domain.model.NutritionPlan
import java.time.LocalDate
import kotlin.math.max

data class DailyNutrition(val date: LocalDate, val calorieTarget: Int, val caloriesConsumed: Int, val caloriesRemaining: Int, val proteinTarget: Int, val proteinConsumed: Int, val carbohydrateTarget: Int, val carbohydrateConsumed: Int, val fatTarget: Int, val fatConsumed: Int, val meals: List<Meal>)
data class Meal(val id: String, val type: MealType, val title: String, val time: String? = null, val items: List<MealItem>, val totalCalories: Int, val protein: Int, val carbohydrates: Int, val fat: Int)
data class MealItem(val id: String, val name: String, val quantity: Double, val unit: String, val calories: Int, val protein: Int, val carbohydrates: Int, val fat: Int, val image: String? = null, val fiber: Double? = null, val sugar: Double? = null, val sodiumMg: Double? = null, val grams: Double? = null)
enum class MealType(val label: String) { Breakfast("Desayuno"), Lunch("Comida"), Dinner("Cena"), Snack("Snack") }

fun NutritionPlan.toDailyNutrition(date: LocalDate, meals: List<Meal>): DailyNutrition {
    val calories = meals.sumOf(Meal::totalCalories)
    return DailyNutrition(date, targetCaloriesKcal, calories, max(targetCaloriesKcal - calories, 0), proteinGrams, meals.sumOf(Meal::protein), carbohydrateGrams, meals.sumOf(Meal::carbohydrates), fatGrams, meals.sumOf(Meal::fat), meals)
}

fun progressOf(consumed: Int, target: Int): Float = if (target <= 0) 0f else consumed.toFloat() / target
