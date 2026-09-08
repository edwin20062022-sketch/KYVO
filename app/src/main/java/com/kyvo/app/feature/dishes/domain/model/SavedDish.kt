package com.kyvo.app.feature.dishes.domain.model

import com.kyvo.app.feature.food.domain.model.FoodDetail
import com.kyvo.app.feature.food.domain.model.FoodType
import com.kyvo.app.feature.food.domain.model.forGrams
import java.time.Instant
import java.util.UUID
import kotlin.math.roundToInt

/** A reusable nutrition template. It deliberately has no historical meal identity. */
data class SavedDish(
    val id: String,
    val userId: String? = null,
    val name: String,
    val portions: Int = 1,
    val items: List<SavedDishItem>,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    val totalCalories: Int get() = items.sumOf(SavedDishItem::calories)
    val totalProtein: Int get() = items.sumOf(SavedDishItem::protein)
    val totalCarbohydrates: Int get() = items.sumOf(SavedDishItem::carbohydrates)
    val totalFat: Int get() = items.sumOf(SavedDishItem::fat)
    val coverImage: String? get() = items.firstNotNullOfOrNull { it.imageReference }
}

data class SavedDishItem(
    val id: String,
    val savedDishId: String? = null,
    val foodReference: String,
    val foodType: FoodType,
    val nameSnapshot: String,
    val quantity: Double,
    val unit: String,
    val grams: Double,
    val calories: Int,
    val protein: Int,
    val carbohydrates: Int,
    val fat: Int,
    val fiber: Double? = null,
    val imageReference: String? = null,
)

data class SavedDishNameValidation(val normalized: String?, val error: String? = null)

fun validateSavedDishName(raw: String, maxLength: Int = 80): SavedDishNameValidation {
    val normalized = raw.trim()
    return when {
        normalized.isEmpty() -> SavedDishNameValidation(null, "Escribe un nombre para el platillo.")
        normalized.length > maxLength -> SavedDishNameValidation(null, "El nombre puede tener hasta $maxLength caracteres.")
        else -> SavedDishNameValidation(normalized)
    }
}

object SavedDishItemFactory {
    fun fromFood(food: FoodDetail, grams: Double, existingId: String = UUID.randomUUID().toString()): SavedDishItem {
        require(grams > 0.0) { "La cantidad debe ser mayor que cero." }
        val nutrients = food.nutrients.forGrams(grams)
        return SavedDishItem(
            id = existingId,
            foodReference = food.id,
            foodType = food.type,
            nameSnapshot = food.name,
            quantity = grams,
            unit = "g",
            grams = grams,
            calories = requireNotNull(nutrients.calories) { "El alimento no tiene calorías disponibles." },
            protein = requireNotNull(nutrients.protein) { "El alimento no tiene proteína disponible." }.roundToInt(),
            carbohydrates = requireNotNull(nutrients.carbohydrates) { "El alimento no tiene carbohidratos disponibles." }.roundToInt(),
            fat = requireNotNull(nutrients.fat) { "El alimento no tiene grasas disponibles." }.roundToInt(),
            fiber = nutrients.fiber,
            imageReference = food.imageUrl ?: food.imageKey,
        )
    }
}
