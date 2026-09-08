package com.kyvo.app.feature.dishes

import com.kyvo.app.feature.dishes.domain.model.SavedDish
import com.kyvo.app.feature.dishes.domain.model.SavedDishItem
import com.kyvo.app.feature.dishes.domain.model.validateSavedDishName
import com.kyvo.app.feature.food.domain.model.FoodType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SavedDishTest {
    @Test fun `rejects blank and accepts trimmed names`() {
        assertNotNull(validateSavedDishName("   ").error)
        assertEquals("Bowl de pollo", validateSavedDishName(" Bowl de pollo ").normalized)
        assertNull(validateSavedDishName("Bowl").error)
    }

    @Test fun `derives macro totals from immutable item snapshots`() {
        val chicken = item("pollo", 248, 47, 0, 5)
        val rice = item("arroz", 156, 3, 34, 0)
        val dish = SavedDish("dish", name = "Bowl", portions = 1, items = listOf(chicken, rice))
        assertEquals(404, dish.totalCalories)
        assertEquals(50, dish.totalProtein)
        assertEquals(34, dish.totalCarbohydrates)
        assertEquals(5, dish.totalFat)
        val editedDish = dish.copy(items = listOf(chicken.copy(calories = 330), rice))
        assertEquals(404, dish.totalCalories)
        assertEquals(486, editedDish.totalCalories)
    }

    private fun item(name: String, calories: Int, protein: Int, carbs: Int, fat: Int) = SavedDishItem(
        id = name, foodReference = "$name-id", foodType = FoodType.Generic, nameSnapshot = name,
        quantity = 100.0, unit = "g", grams = 100.0, calories = calories, protein = protein,
        carbohydrates = carbs, fat = fat,
    )
}
