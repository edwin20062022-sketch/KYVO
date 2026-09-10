package com.kyvo.app.feature.progress

import com.kyvo.app.feature.home.domain.model.Meal
import com.kyvo.app.feature.home.domain.model.MealItem
import com.kyvo.app.feature.home.domain.model.MealType
import com.kyvo.app.feature.onboarding.domain.model.NutritionPlan
import com.kyvo.app.feature.progress.domain.NutritionConsistencyStatus
import com.kyvo.app.feature.progress.domain.aggregateDailyNutrition
import com.kyvo.app.feature.progress.domain.buildNutritionHistoryDays
import com.kyvo.app.feature.progress.domain.monthDateRange
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NutritionHistoryTest {
    private val plan = NutritionPlan(0.0, 0.0, 1.0, 0.0, 2000, 160, 220, 70)

    @Test fun historySortsRepositoryDatesNewestFirst() {
        val first = LocalDate.of(2026, 9, 1)
        val last = LocalDate.of(2026, 9, 5)
        val summaries = aggregateDailyNutrition(mapOf(last to listOf(meal("last", item(1000, 80))), first to listOf(meal("first", item(1900, 150)))), plan)
        assertEquals(listOf(last, first), buildNutritionHistoryDays(summaries).map { it.summary.date })
    }

    @Test fun monthRangeIncludesBothBoundsAcrossMonthAndYear() {
        val september = monthDateRange(YearMonth.of(2026, 9))
        assertEquals(LocalDate.of(2026, 9, 1), september.first())
        assertEquals(LocalDate.of(2026, 9, 30), september.last())
        assertEquals(31, monthDateRange(YearMonth.of(2026, 12)).size)
        assertEquals(28, monthDateRange(YearMonth.of(2027, 2)).size)
    }

    @Test fun manualAndCatalogSnapshotsBothContributeToHistoryTotals() {
        val date = LocalDate.of(2026, 9, 30)
        val manual = item(300, 30, "Prueba manual")
        val catalog = item(1700, 130, "Alimento catalogado")
        val summary = aggregateDailyNutrition(mapOf(date to listOf(meal("mixed", catalog, manual))), plan).single()
        assertEquals(2000, summary.caloriesConsumed)
        assertEquals(160, summary.proteinConsumed)
        assertEquals(1, summary.mealCount)
        assertEquals(NutritionConsistencyStatus.CONSISTENT, buildNutritionHistoryDays(listOf(summary)).single().consistency.status)
    }

    @Test fun rangeBoundariesCanBeRepresentedWithoutDroppingDates() {
        val start = LocalDate.of(2026, 12, 31)
        val end = LocalDate.of(2027, 1, 1)
        val summaries = aggregateDailyNutrition(mapOf(start to listOf(meal("start", item(1000, 80))), end to listOf(meal("end", item(1000, 80)))), plan)
        assertEquals(listOf(start, end), summaries.map { it.date })
        assertTrue(summaries.all { it.caloriesTarget == 2000 })
    }

    private fun meal(id: String, vararg items: MealItem) = Meal(id, MealType.Lunch, id, items = items.toList(), totalCalories = 0, protein = 0, carbohydrates = 0, fat = 0)
    private fun item(calories: Int, protein: Int, name: String = "snapshot") = MealItem(name, name, 1.0, "porción", calories, protein, 0, 0, foodId = null, foodType = null)
}
