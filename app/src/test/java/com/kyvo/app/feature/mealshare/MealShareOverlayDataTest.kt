package com.kyvo.app.feature.mealshare

import com.kyvo.app.feature.home.domain.model.MealItem
import com.kyvo.app.feature.home.domain.model.MealType
import com.kyvo.app.feature.mealshare.domain.model.MealShareDraft
import com.kyvo.app.feature.mealshare.domain.model.toOverlayData
import org.junit.Assert.assertEquals
import org.junit.Test

class MealShareOverlayDataTest {
    @Test
    fun overlayDataUsesDerivedDraftTotalsAndMealType() {
        val draft = MealShareDraft(
            title = "Pollo y arroz",
            mealType = MealType.Dinner,
            items = listOf(
                MealItem("one", "Pollo", 100.0, "g", 320, 42, 20, 12),
                MealItem("two", "Arroz", 120.0, "g", 260, 4, 48, 2),
            ),
        )
        val overlay = draft.toOverlayData()
        assertEquals(580, overlay.calories)
        assertEquals(46, overlay.protein)
        assertEquals(68, overlay.carbohydrates)
        assertEquals(14, overlay.fat)
        assertEquals("Cena", overlay.mealTypeLabel)
        assertEquals("580 kcal", overlay.caloriesLabel)
    }
}
