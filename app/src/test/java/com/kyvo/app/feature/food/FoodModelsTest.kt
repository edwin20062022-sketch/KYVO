package com.kyvo.app.feature.food

import com.kyvo.app.feature.food.domain.normalizeFoodQuery
import com.kyvo.app.feature.food.domain.model.FoodNutrients
import com.kyvo.app.feature.food.domain.model.forGrams
import org.junit.Assert.assertEquals
import org.junit.Test

class FoodModelsTest {
    private val nutrients = FoodNutrients(18.0, 1.0, 4.0, 0.2, fiberPer100g = null)
    @Test fun `normalizes aliases and accents`() { assertEquals("tomate", normalizeFoodQuery("  Tomátes  ")) }
    @Test fun `derives nutrition from 100 grams`() { assertEquals(18, nutrients.forGrams(100.0).calories) }
    @Test fun `derives nutrition from 50 grams`() { assertEquals(9, nutrients.forGrams(50.0).calories) }
    @Test fun `derives nutrition from 150 grams`() { val amount = nutrients.forGrams(150.0); assertEquals(27, amount.calories); assertEquals(1.5, amount.protein) }
    @Test fun `keeps unknown nutrients unknown`() { assertEquals(null, nutrients.forGrams(150.0).fiber) }
}
