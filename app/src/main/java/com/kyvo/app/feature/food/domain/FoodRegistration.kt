package com.kyvo.app.feature.food.domain

import com.kyvo.app.feature.food.domain.model.FoodDetail
import com.kyvo.app.feature.food.domain.model.forGrams
import com.kyvo.app.feature.home.domain.model.Meal
import com.kyvo.app.feature.home.domain.model.MealItem
import com.kyvo.app.feature.home.domain.model.MealType
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.math.roundToInt

data class FoodAmountValidation(val grams: Double?, val error: String? = null)

fun validateFoodAmount(raw: String, maxGrams: Double = 10_000.0): FoodAmountValidation {
    val normalized = raw.trim().replace(',', '.')
    val amount = normalized.toDoubleOrNull()
    return when {
        amount == null -> FoodAmountValidation(null, "Escribe una cantidad válida.")
        amount <= 0.0 -> FoodAmountValidation(null, "La cantidad debe ser mayor que cero.")
        amount > maxGrams -> FoodAmountValidation(null, "La cantidad máxima es ${maxGrams.toInt()} g.")
        else -> FoodAmountValidation(amount)
    }
}

object FoodRegistrationFactory {
    fun createMeal(food: FoodDetail, grams: Double, mealType: MealType, now: LocalTime = LocalTime.now()): Meal {
        val nutrients = food.nutrients.forGrams(grams)
        val calories = requireNotNull(nutrients.calories) { "El alimento no tiene calorías disponibles." }
        val protein = requireNotNull(nutrients.protein) { "El alimento no tiene proteína disponible." }
        val carbohydrates = requireNotNull(nutrients.carbohydrates) { "El alimento no tiene carbohidratos disponibles." }
        val fat = requireNotNull(nutrients.fat) { "El alimento no tiene grasas disponibles." }
        val item = MealItem(
            id = UUID.randomUUID().toString(), name = food.name, quantity = grams, unit = "g",
            calories = calories, protein = protein.roundToInt(), carbohydrates = carbohydrates.roundToInt(), fat = fat.roundToInt(),
            image = food.imageUrl ?: food.imageKey, fiber = nutrients.fiber, sugar = nutrients.sugar,
            sodiumMg = nutrients.sodiumMg, grams = grams,
        )
        return Meal(
            id = UUID.randomUUID().toString(), type = mealType, title = food.name,
            time = now.format(DateTimeFormatter.ofPattern("HH:mm")), items = listOf(item),
            totalCalories = calories, protein = protein.roundToInt(), carbohydrates = carbohydrates.roundToInt(), fat = fat.roundToInt(),
        )
    }
}
