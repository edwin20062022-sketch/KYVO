package com.kyvo.app.feature.progress.domain

import java.time.LocalDate

enum class NutritionMetric(
    val title: String,
    val subtitle: String,
    val unit: String,
) {
    CALORIES("Calorías", "Tu progreso calórico en el tiempo.", "kcal"),
    PROTEIN("Proteína", "Tu progreso de proteína en el tiempo.", "g"),
}

data class NutritionProgressPoint(
    val date: LocalDate,
    val consumed: Int,
    val target: Int,
    val mealCount: Int,
) {
    val ratio: Double? get() = target.takeIf { it > 0 }?.let { consumed.toDouble() / it }
    val percentage: Int? get() = ratio?.times(100)?.toInt()
}

data class NutritionSource(val name: String, val amount: Int)

data class NutritionMetricDetail(
    val metric: NutritionMetric,
    val points: List<NutritionProgressPoint>,
    val sources: List<NutritionSource> = emptyList(),
) {
    val target: Int get() = points.lastOrNull()?.target ?: 0
    val totalConsumed: Int get() = points.sumOf { it.consumed }
    val averageConsumed: Int get() = if (points.isEmpty()) 0 else points.sumOf { it.consumed } / points.size
    val daysWithMeals: Int get() = points.count { it.mealCount > 0 }
    val averageRatio: Double? get() = target.takeIf { it > 0 }?.let { averageConsumed.toDouble() / it }
    val averagePercentage: Int? get() = averageRatio?.times(100)?.toInt()
}

fun buildMetricDetail(
    metric: NutritionMetric,
    summaries: List<DailyNutritionSummary>,
    mealsByDate: Map<LocalDate, List<com.kyvo.app.feature.home.domain.model.Meal>> = emptyMap(),
): NutritionMetricDetail =
    NutritionMetricDetail(
        metric = metric,
        points = summaries.sortedBy { it.date }.map { summary ->
            when (metric) {
                NutritionMetric.CALORIES -> NutritionProgressPoint(summary.date, summary.caloriesConsumed, summary.caloriesTarget, summary.mealCount)
                NutritionMetric.PROTEIN -> NutritionProgressPoint(summary.date, summary.proteinConsumed, summary.proteinTarget, summary.mealCount)
            }
        },
        sources = if (metric == NutritionMetric.PROTEIN) {
            mealsByDate.values.flatten().flatMap { it.items }
                .groupBy { it.name }
                .map { (name, items) -> NutritionSource(name, items.sumOf { it.protein }) }
                .sortedByDescending { it.amount }
                .take(4)
        } else emptyList(),
    )