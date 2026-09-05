package com.kyvo.app.domain.model

data class NutritionTargets(
    val caloriesKcal: Int,
    val proteinGrams: Int,
    val carbohydrateGrams: Int,
    val fatGrams: Int,
) {
    init {
        require(caloriesKcal > 0) { "Calories must be positive" }
        require(proteinGrams >= 0) { "Protein cannot be negative" }
        require(carbohydrateGrams >= 0) { "Carbohydrates cannot be negative" }
        require(fatGrams >= 0) { "Fat cannot be negative" }
    }
}

