package com.kyvo.app.feature.progress

import com.kyvo.app.feature.home.domain.model.Meal
import com.kyvo.app.feature.home.domain.model.MealItem
import com.kyvo.app.feature.home.domain.model.MealType
import com.kyvo.app.feature.onboarding.domain.model.NutritionPlan
import com.kyvo.app.feature.progress.domain.NutritionConsistencyStatus
import com.kyvo.app.feature.progress.domain.NutritionProgress
import com.kyvo.app.feature.progress.domain.aggregateDailyNutrition
import com.kyvo.app.feature.progress.domain.buildNutritionHistoryDays
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NutritionDayDetailTest {
    private val date = LocalDate.of(2026, 9, 9)
    private val plan = NutritionPlan(0.0, 0.0, 1.0, 0.0, 2000, 160, 220, 70)

    @Test fun detailAggregatesTwoMealsForTheRequestedDateOnly() {
        val result = aggregateDailyNutrition(
            mapOf(date to listOf(meal("breakfast", 800, 70), meal("dinner", 1200, 90))),
            plan,
        ).single()
        assertEquals(2, result.mealCount)
        assertEquals(2000, result.caloriesConsumed)
        assertEquals(160, result.proteinConsumed)
    }

    @Test fun wrongDateDoesNotAppearInRequestedDaySummary() {
        val result = aggregateDailyNutrition(mapOf(date.minusDays(1) to listOf(meal("other", 1900, 150))), plan)
        assertEquals(date.minusDays(1), result.single().date)
        assertEquals(0, result.count { it.date == date })
    }

    @Test fun overTargetAndZeroTargetRemainSafeForDetail() {
        val over = aggregateDailyNutrition(mapOf(date to listOf(meal("over", 2500, 200))), plan).single()
        assertEquals(125, over.caloriesProgress.percentage)
        assertEquals(NutritionConsistencyStatus.INCONSISTENT, buildNutritionHistoryDays(listOf(over)).single().consistency.status)
        assertNull(NutritionProgress(100, 0).ratio)
        assertNull(NutritionProgress(100, 0).percentage)
    }

    private fun meal(id: String, calories: Int, protein: Int) = Meal(id, MealType.Lunch, id, items = listOf(MealItem(id, "Snapshot $id", 1.0, "porción", calories, protein, 0, 0, foodId = null, foodType = null)), totalCalories = calories, protein = protein, carbohydrates = 0, fat = 0)
}
