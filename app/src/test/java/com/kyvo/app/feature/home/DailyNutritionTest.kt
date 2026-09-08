package com.kyvo.app.feature.home

import com.kyvo.app.feature.home.domain.model.Meal
import com.kyvo.app.feature.home.domain.model.MealItem
import com.kyvo.app.feature.home.domain.model.MealType
import com.kyvo.app.feature.home.domain.model.progressOf
import com.kyvo.app.feature.home.domain.model.toDailyNutrition
import com.kyvo.app.feature.onboarding.domain.model.NutritionPlan
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class DailyNutritionTest {
    private val plan = NutritionPlan(1600.0, 2300.0, 1.5, 0.0, 2300, 160, 225, 70)
    private fun meal(calories: Int = 500, protein: Int = 30) = Meal("meal", MealType.Lunch, "Comida", items = listOf(MealItem("item", "Arroz", 100.0, "g", calories, protein, 40, 8)), totalCalories = calories, protein = protein, carbohydrates = 40, fat = 8)

    @Test fun `empty dashboard preserves onboarding targets`() {
        val daily = plan.toDailyNutrition(LocalDate.of(2026, 9, 8), emptyList())
        assertEquals(0, daily.caloriesConsumed)
        assertEquals(2300, daily.caloriesRemaining)
        assertEquals(160, daily.proteinTarget)
        assertEquals(0, daily.meals.size)
    }

    @Test fun `dashboard aggregates meal nutrients`() {
        val daily = plan.toDailyNutrition(LocalDate.now(), listOf(meal(), meal(260, 20)))
        assertEquals(760, daily.caloriesConsumed)
        assertEquals(1540, daily.caloriesRemaining)
        assertEquals(50, daily.proteinConsumed)
        assertEquals(80, daily.carbohydrateConsumed)
        assertEquals(16, daily.fatConsumed)
    }

    @Test fun `remaining calories never becomes negative and progress keeps overflow`() {
        val daily = plan.toDailyNutrition(LocalDate.now(), listOf(meal(2500, 200)))
        assertEquals(0, daily.caloriesRemaining)
        assertEquals(1.25f, progressOf(daily.proteinConsumed, daily.proteinTarget))
    }

    @Test fun `zero target has safe progress`() = assertEquals(0f, progressOf(10, 0))
}
