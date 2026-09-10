package com.kyvo.app.feature.progress

import com.kyvo.app.feature.home.domain.model.Meal
import com.kyvo.app.feature.home.domain.model.MealItem
import com.kyvo.app.feature.home.domain.model.MealType
import com.kyvo.app.feature.onboarding.domain.model.NutritionPlan
import com.kyvo.app.feature.progress.domain.NutritionMetric
import com.kyvo.app.feature.progress.domain.buildMetricDetail
import com.kyvo.app.feature.progress.domain.aggregateDailyNutrition
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NutritionMetricDetailTest {
    private val plan = NutritionPlan(0.0, 0.0, 1.0, 0.0, 2000, 160, 220, 70)

    @Test fun sevenDaySeriesKeepsMissingCalendarDays() {
        val start = LocalDate.of(2026, 9, 8)
        val summaries = (0L..6L).map { offset ->
            val date = start.plusDays(offset)
            val meal = if (offset == 0L || offset == 2L || offset == 6L) listOf(meal("$offset", 1000, 80)) else emptyList()
            aggregateDailyNutrition(mapOf(date to meal), plan).single()
        }

        val detail = buildMetricDetail(NutritionMetric.CALORIES, summaries)

        assertEquals(7, detail.points.size)
        assertEquals(listOf(1000, 0, 1000, 0, 0, 0, 1000), detail.points.map { it.consumed })
        assertEquals(listOf(start, start.plusDays(6)), listOf(detail.points.first().date, detail.points.last().date))
    }

    @Test fun caloriesAndProteinKeepOverTargetPercentages() {
        val date = LocalDate.of(2026, 9, 9)
        val summary = aggregateDailyNutrition(mapOf(date to listOf(meal("over", 2200, 200))), plan).single()

        assertEquals(110, buildMetricDetail(NutritionMetric.CALORIES, listOf(summary)).averagePercentage)
        assertEquals(125, buildMetricDetail(NutritionMetric.PROTEIN, listOf(summary)).averagePercentage)
    }

    @Test fun zeroTargetIsUnavailableForBothMetrics() {
        val date = LocalDate.of(2026, 9, 9)
        val zeroPlan = plan.copy(proteinGrams = 0, targetCaloriesKcal = 0)
        val summary = aggregateDailyNutrition(mapOf(date to listOf(meal("zero", 100, 20))), zeroPlan).single()

        assertNull(buildMetricDetail(NutritionMetric.CALORIES, listOf(summary)).averageRatio)
        assertNull(buildMetricDetail(NutritionMetric.PROTEIN, listOf(summary)).averageRatio)
    }

    private fun meal(id: String, calories: Int, protein: Int) = Meal(
        id = id,
        type = MealType.Lunch,
        title = id,
        items = listOf(MealItem(id, id, 1.0, "g", calories, protein, 0, 0, foodId = null, foodType = null)),
        totalCalories = 0,
        protein = 0,
        carbohydrates = 0,
        fat = 0,
    )
}