package com.kyvo.app.feature.progress

import com.kyvo.app.feature.home.domain.model.Meal
import com.kyvo.app.feature.home.domain.model.MealItem
import com.kyvo.app.feature.home.domain.model.MealType
import com.kyvo.app.feature.onboarding.domain.model.NutritionPlan
import com.kyvo.app.feature.progress.domain.NutritionProgress
import com.kyvo.app.feature.progress.domain.aggregateDailyNutrition
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NutritionProgressTest {
    private val plan = NutritionPlan(0.0, 0.0, 1.0, 0.0, 2000, 160, 220, 70)

    @Test fun aggregatesCatalogAndManualItemsByDate() {
        val day = LocalDate.of(2026, 9, 9)
        val meal = meal("day", item("catalog", 700, 50, 80, 20), item("manual", 300, 30, 30, 15))

        val result = aggregateDailyNutrition(mapOf(day to listOf(meal)), plan).single()

        assertEquals(1000, result.caloriesConsumed)
        assertEquals(80, result.proteinConsumed)
        assertEquals(110, result.carbohydratesConsumed)
        assertEquals(35, result.fatConsumed)
        assertEquals(50, result.caloriesProgress.percentage)
        assertEquals(50, result.proteinProgress.percentage)
        assertEquals(50, result.carbohydratesProgress.percentage)
        assertEquals(50, result.fatProgress.percentage)
    }

    @Test fun separatesDatesAndPreservesOverTarget() {
        val first = LocalDate.of(2026, 9, 8)
        val second = LocalDate.of(2026, 9, 9)
        val result = aggregateDailyNutrition(
            mapOf(first to listOf(meal("first", item("a", 2200, 0, 0, 0))), second to listOf(meal("second", item("b", 100, 0, 0, 0)))),
            plan,
        )

        assertEquals(listOf(first, second), result.map { it.date })
        assertEquals(2200, result.first().caloriesConsumed)
        assertEquals(110, result.first().caloriesProgress.percentage)
        assertEquals(100, result[1].caloriesConsumed)
    }

    @Test fun zeroTargetIsUnavailableInsteadOfInfinite() {
        assertNull(NutritionProgress(10, 0).ratio)
        assertNull(NutritionProgress(10, 0).percentage)
    }

    private fun meal(id: String, vararg items: MealItem) = Meal(id, MealType.Lunch, id, items = items.toList(), totalCalories = 0, protein = 0, carbohydrates = 0, fat = 0)
    private fun item(id: String, calories: Int, protein: Int, carbohydrates: Int, fat: Int) = MealItem(id, id, 1.0, "g", calories, protein, carbohydrates, fat)
}