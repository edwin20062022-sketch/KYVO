package com.kyvo.app.feature.progress.domain

import java.time.LocalDate
import java.time.YearMonth

data class NutritionHistoryDay(
    val summary: DailyNutritionSummary,
    val consistency: NutritionConsistencyDay,
)

fun buildNutritionHistoryDays(summaries: List<DailyNutritionSummary>): List<NutritionHistoryDay> = summaries
    .sortedByDescending { it.date }
    .map { summary -> NutritionHistoryDay(summary, calculateConsistency(listOf(summary)).dailyStatuses.single()) }

fun monthDateRange(month: YearMonth): List<LocalDate> = generateSequence(month.atDay(1)) { date ->
    date.plusDays(1).takeIf { it <= month.atEndOfMonth() }
}.toList()
