package com.kyvo.app.feature.mealshare.domain.model

import com.kyvo.app.feature.home.domain.model.MealItem
import java.util.UUID
import kotlin.math.roundToInt

/** Ephemeral, user-entered nutrition for a single Meal Share item. */
data class ManualFoodInput(
    val name: String,
    val protein: Double,
    val carbohydrates: Double,
    val fat: Double,
)

sealed interface ManualFoodValidation {
    data object Valid : ManualFoodValidation
    data class Invalid(val message: String) : ManualFoodValidation
}

const val MAX_MANUAL_MACRO_GRAMS = 3_000.0

fun ManualFoodInput.validate(): ManualFoodValidation = when {
    name.isBlank() -> ManualFoodValidation.Invalid("Escribe el nombre del alimento.")
    listOf(protein, carbohydrates, fat).any { !it.isFinite() || it < 0.0 } ->
        ManualFoodValidation.Invalid("Los macronutrientes deben ser números iguales o mayores a cero.")
    listOf(protein, carbohydrates, fat).any { it > MAX_MANUAL_MACRO_GRAMS } ->
        ManualFoodValidation.Invalid("Ingresa una cantidad menor o igual a ${MAX_MANUAL_MACRO_GRAMS.toInt()} g por macro.")
    protein == 0.0 && carbohydrates == 0.0 && fat == 0.0 ->
        ManualFoodValidation.Invalid("Agrega al menos un macro mayor a cero.")
    else -> ManualFoodValidation.Valid
}

fun caloriesFromMacros(protein: Double, carbohydrates: Double, fat: Double): Int =
    ((protein * 4.0) + (carbohydrates * 4.0) + (fat * 9.0)).roundToInt()

fun ManualFoodInput.calories(): Int = caloriesFromMacros(protein, carbohydrates, fat)

fun ManualFoodInput.toMealItem(id: String = UUID.randomUUID().toString()): MealItem? {
    if (validate() !is ManualFoodValidation.Valid) return null
    val storedProtein = protein.roundToInt()
    val storedCarbohydrates = carbohydrates.roundToInt()
    val storedFat = fat.roundToInt()
    return MealItem(
        id = id,
        name = name.trim(),
        quantity = 1.0,
        unit = "porción",
        calories = caloriesFromMacros(storedProtein.toDouble(), storedCarbohydrates.toDouble(), storedFat.toDouble()),
        protein = storedProtein,
        carbohydrates = storedCarbohydrates,
        fat = storedFat,
        // The user only supplied macros. Do not invent micronutrients or a catalog source.
        fiber = null,
        sugar = null,
        sodiumMg = null,
        grams = null,
        foodId = null,
        foodType = null,
    )
}

fun String.toManualMacroGramsOrNull(): Double? = trim()
    .replace(',', '.')
    .toDoubleOrNull()
    ?.takeIf(Double::isFinite)
