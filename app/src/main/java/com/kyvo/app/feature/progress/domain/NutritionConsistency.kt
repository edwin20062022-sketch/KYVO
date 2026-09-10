package com.kyvo.app.feature.progress.domain

import java.time.LocalDate

enum class NutritionConsistencyStatus {
    CONSISTENT,
    INCONSISTENT,
    NOT_EVALUABLE,
}

data class NutritionConsistencyDay(
    val date: LocalDate,
    val caloriesConsumed: Int,
    val caloriesTarget: Int,
    val caloriesRatio: Double?,
    val proteinConsumed: Int,
    val proteinTarget: Int,
    val proteinRatio: Double?,
    val status: NutritionConsistencyStatus,
)

data class NutritionConsistencySummary(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalDays: Int,
    val evaluableDays: Int,
    val consistentDays: Int,
    val inconsistentDays: Int,
    val consistencyPercentage: Double?,
    val dailyStatuses: List<NutritionConsistencyDay>,
)

fun calculateConsistency(dailySummaries: List<DailyNutritionSummary>): NutritionConsistencySummary {
    val days = dailySummaries.sortedBy { it.date }.map { summary ->
        val calories = summary.caloriesConsumed.coerceAtLeast(0)
        val protein = summary.proteinConsumed.coerceAtLeast(0)
        val caloriesRatio = summary.caloriesTarget.takeIf { it > 0 }?.let { calories.toDouble() / it }
        val proteinRatio = summary.proteinTarget.takeIf { it > 0 }?.let { protein.toDouble() / it }
        val status = when {
            caloriesRatio == null || proteinRatio == null -> NutritionConsistencyStatus.NOT_EVALUABLE
            caloriesRatio in 0.90..1.10 && proteinRatio >= 0.90 -> NutritionConsistencyStatus.CONSISTENT
            else -> NutritionConsistencyStatus.INCONSISTENT
        }
        NutritionConsistencyDay(summary.date, calories, summary.caloriesTarget, caloriesRatio, protein, summary.proteinTarget, proteinRatio, status)
    }
    val evaluable = days.count { it.status != NutritionConsistencyStatus.NOT_EVALUABLE }
    val consistent = days.count { it.status == NutritionConsistencyStatus.CONSISTENT }
    val inconsistent = days.count { it.status == NutritionConsistencyStatus.INCONSISTENT }
    return NutritionConsistencySummary(
        startDate = days.firstOrNull()?.date ?: LocalDate.MIN,
        endDate = days.lastOrNull()?.date ?: LocalDate.MIN,
        totalDays = days.size,
        evaluableDays = evaluable,
        consistentDays = consistent,
        inconsistentDays = inconsistent,
        consistencyPercentage = consistent.takeIf { evaluable > 0 }?.let { it.toDouble() / evaluable },
        dailyStatuses = days,
    )
}
