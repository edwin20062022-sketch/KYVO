package com.kyvo.app.feature.food.domain.model

import kotlin.math.roundToInt

enum class FoodType { Generic, Commercial }
data class FoodServing(val label: String, val gramEquivalent: Double, val source: String)
data class FoodNutrients(val caloriesPer100g: Double?, val proteinPer100g: Double?, val carbohydratesPer100g: Double?, val fatPer100g: Double?, val fiberPer100g: Double? = null, val sugarPer100g: Double? = null, val sodiumMgPer100g: Double? = null)
data class FoodSearchResult(val id: String, val type: FoodType, val name: String, val subtitle: String?, val brand: String?, val imageKey: String?, val nutrients: FoodNutrients, val servingLabel: String?, val imageUrl: String? = null)
data class FoodDetail(val id: String, val type: FoodType, val name: String, val variant: String?, val imageKey: String?, val nutrients: FoodNutrients, val servings: List<FoodServing>, val imageUrl: String? = null)
data class NutritionAmount(val calories: Int?, val protein: Double?, val carbohydrates: Double?, val fat: Double?, val fiber: Double?, val sugar: Double?, val sodiumMg: Double?)
fun FoodNutrients.forGrams(grams: Double): NutritionAmount {
    val factor = grams / 100.0
    fun value(v: Double?): Double? = v?.times(factor)
    return NutritionAmount(value(caloriesPer100g)?.roundToInt(), value(proteinPer100g), value(carbohydratesPer100g), value(fatPer100g), value(fiberPer100g), value(sugarPer100g), value(sodiumMgPer100g))
}
