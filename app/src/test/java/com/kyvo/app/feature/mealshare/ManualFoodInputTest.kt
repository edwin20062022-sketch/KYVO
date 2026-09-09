package com.kyvo.app.feature.mealshare

import com.kyvo.app.feature.mealshare.domain.model.ManualFoodInput
import com.kyvo.app.feature.mealshare.domain.model.ManualFoodValidation
import com.kyvo.app.feature.mealshare.domain.model.caloriesFromMacros
import com.kyvo.app.feature.mealshare.domain.model.toManualMacroGramsOrNull
import com.kyvo.app.feature.mealshare.domain.model.toMealItem
import com.kyvo.app.feature.mealshare.domain.model.validate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ManualFoodInputTest {
    @Test
    fun caloriesUseTheFourFourNineFormulaIncludingDecimals() {
        assertEquals(165, caloriesFromMacros(10.0, 20.0, 5.0))
        assertEquals(0, caloriesFromMacros(0.0, 0.0, 0.0))
        assertEquals(82, caloriesFromMacros(12.5, 3.5, 2.0))
        assertEquals(51_000, caloriesFromMacros(3_000.0, 3_000.0, 3_000.0))
    }

    @Test
    fun validationRejectsEmptyNameNegativeAndEmptyNutrition() {
        assertTrue(ManualFoodInput("", 1.0, 2.0, 3.0).validate() is ManualFoodValidation.Invalid)
        assertTrue(ManualFoodInput("Prueba", -1.0, 2.0, 3.0).validate() is ManualFoodValidation.Invalid)
        assertTrue(ManualFoodInput("Prueba", 1.0, -2.0, 3.0).validate() is ManualFoodValidation.Invalid)
        assertTrue(ManualFoodInput("Prueba", 1.0, 2.0, -3.0).validate() is ManualFoodValidation.Invalid)
        assertTrue(ManualFoodInput("Prueba", 0.0, 0.0, 0.0).validate() is ManualFoodValidation.Invalid)
    }

    @Test
    fun parserAcceptsLocalizedDecimalsAndRejectsInvalidText() {
        assertEquals(12.5, "12,5".toManualMacroGramsOrNull()!!, 0.0)
        assertNull("doce".toManualMacroGramsOrNull())
        assertNull("NaN".toManualMacroGramsOrNull())
        assertNull("Infinity".toManualMacroGramsOrNull())
    }

    @Test
    fun validManualFoodBuildsARealMealItemWithUnknownMicronutrients() {
        val item = ManualFoodInput("Bowl de pollo casero", 10.0, 20.0, 5.0).toMealItem("manual-id")!!

        assertEquals("manual-id", item.id)
        assertEquals("Bowl de pollo casero", item.name)
        assertEquals(1.0, item.quantity, 0.0)
        assertEquals("porción", item.unit)
        assertEquals(165, item.calories)
        assertEquals(10, item.protein)
        assertEquals(20, item.carbohydrates)
        assertEquals(5, item.fat)
        assertNull(item.foodId)
        assertNull(item.foodType)
        assertNull(item.fiber)
        assertNull(item.sugar)
        assertNull(item.sodiumMg)
    }

    @Test
    fun decimalMacrosNormalizeToTheExistingWholeGramMealItemContract() {
        val item = ManualFoodInput("Avena", 12.5, 3.5, 2.0).toMealItem("manual-decimal")!!

        assertEquals(13, item.protein)
        assertEquals(4, item.carbohydrates)
        assertEquals(2, item.fat)
        assertEquals(86, item.calories)
    }

    @Test
    fun invalidInputCannotBuildAnItem() {
        assertNull(ManualFoodInput("", 10.0, 20.0, 5.0).toMealItem("manual-id"))
        assertFalse(ManualFoodInput("Prueba", 10.0, 20.0, 5.0).validate() is ManualFoodValidation.Invalid)
    }
}
