package com.kyvo.app.feature.progress

import com.kyvo.app.feature.progress.domain.DailyNutritionSummary
import com.kyvo.app.feature.progress.domain.NutritionConsistencyStatus
import com.kyvo.app.feature.progress.domain.calculateConsistency
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NutritionConsistencyTest {
    private val day = LocalDate.of(2026, 9, 9)

    @Test fun consistentDayUsesInclusiveCalorieAndProteinLimits() {
        val result = calculateConsistency(listOf(summary(1800, 144)))
        assertEquals(NutritionConsistencyStatus.CONSISTENT, result.dailyStatuses.single().status)
        assertEquals(1.0, result.consistencyPercentage!!, 0.0001)
    }

    @Test fun caloriesLowHighAndProteinLowAreInconsistent() {
        val result = calculateConsistency(listOf(summary(1500, 160), summary(2300, 170), summary(2000, 100)))
        assertEquals(List(3) { NutritionConsistencyStatus.INCONSISTENT }, result.dailyStatuses.map { it.status })
        assertEquals(0.0, result.consistencyPercentage!!, 0.0001)
    }

    @Test fun proteinMayExceedTargetAndCaloriesUpperBoundaryIsValid() {
        val result = calculateConsistency(listOf(summary(2200, 200)))
        assertEquals(NutritionConsistencyStatus.CONSISTENT, result.dailyStatuses.single().status)
    }

    @Test fun noMealsWithValidTargetsIsRealZeroNotEmpty() {
        val result = calculateConsistency(listOf(summary(0, 0)))
        assertEquals(NutritionConsistencyStatus.INCONSISTENT, result.dailyStatuses.single().status)
        assertEquals(0.0, result.consistencyPercentage!!, 0.0001)
    }

    @Test fun zeroTargetIsNotEvaluableAndExcludedFromDenominator() {
        val result = calculateConsistency(listOf(summary(2000, 160), summary(1800, 144, caloriesTarget = 0), summary(1800, 144, proteinTarget = 0)))
        assertEquals(1, result.evaluableDays)
        assertEquals(1, result.consistentDays)
        assertEquals(1.0, result.consistencyPercentage!!, 0.0001)
        assertEquals(NutritionConsistencyStatus.NOT_EVALUABLE, result.dailyStatuses[1].status)
        assertEquals(NutritionConsistencyStatus.NOT_EVALUABLE, result.dailyStatuses[2].status)
    }

    @Test fun allInvalidTargetsProduceNoPercentage() {
        val result = calculateConsistency(listOf(summary(0, 0, caloriesTarget = 0), summary(0, 0, proteinTarget = 0)))
        assertEquals(0, result.evaluableDays)
        assertNull(result.consistencyPercentage)
    }

    @Test fun summaryUsesEvaluableDenominatorAndPreservesDateOrder() {
        val result = calculateConsistency(listOf(summary(2000, 160, date = day.plusDays(2)), summary(1900, 150, date = day)))
        assertEquals(2, result.totalDays)
        assertEquals(2, result.evaluableDays)
        assertEquals(2, result.consistentDays)
        assertEquals(day, result.startDate)
        assertEquals(day.plusDays(2), result.endDate)
    }

    @Test fun negativeConsumedValuesAreDefensivelyClamped() {
        val result = calculateConsistency(listOf(summary(-100, -10)))
        assertEquals(0, result.dailyStatuses.single().caloriesConsumed)
        assertEquals(0, result.dailyStatuses.single().proteinConsumed)
        assertEquals(NutritionConsistencyStatus.INCONSISTENT, result.dailyStatuses.single().status)
    }

    private fun summary(
        calories: Int,
        protein: Int,
        date: LocalDate = day,
        caloriesTarget: Int = 2000,
        proteinTarget: Int = 160,
    ) = DailyNutritionSummary(date, calories, caloriesTarget, protein, proteinTarget, 0, 0, 0, 0, if (calories > 0) 1 else 0)
}
