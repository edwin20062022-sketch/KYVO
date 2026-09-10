package com.kyvo.app.feature.progress.domain

import com.kyvo.app.feature.home.domain.model.Meal
import com.kyvo.app.feature.onboarding.domain.model.NutritionPlan
import java.time.LocalDate

data class DailyNutritionSummary(
    val date: LocalDate,
    val caloriesConsumed: Int,
    val caloriesTarget: Int,
    val proteinConsumed: Int,
    val proteinTarget: Int,
    val carbohydratesConsumed: Int,
    val carbohydratesTarget: Int,
    val fatConsumed: Int,
    val fatTarget: Int,
    val mealCount: Int,
) {
    val caloriesProgress: NutritionProgress get() = NutritionProgress(caloriesConsumed, caloriesTarget)
    val proteinProgress: NutritionProgress get() = NutritionProgress(proteinConsumed, proteinTarget)
    val carbohydratesProgress: NutritionProgress get() = NutritionProgress(carbohydratesConsumed, carbohydratesTarget)
    val fatProgress: NutritionProgress get() = NutritionProgress(fatConsumed, fatTarget)
}

data class NutritionProgress(val consumed: Int, val target: Int) {
    val ratio: Float? get() = target.takeIf { it > 0 }?.let { consumed.toFloat() / it }
    val percentage: Int? get() = ratio?.times(100)?.toInt()
}

fun aggregateDailyNutrition(
    mealsByDate: Map<LocalDate, List<Meal>>,
    plan: NutritionPlan,
): List<DailyNutritionSummary> = mealsByDate
    .toSortedMap()
    .map { (date, meals) ->
        DailyNutritionSummary(
            date = date,
            caloriesConsumed = meals.sumOf { meal -> meal.items.sumOf { it.calories } },
            caloriesTarget = plan.targetCaloriesKcal,
            proteinConsumed = meals.sumOf { meal -> meal.items.sumOf { it.protein } },
            proteinTarget = plan.proteinGrams,
            carbohydratesConsumed = meals.sumOf { meal -> meal.items.sumOf { it.carbohydrates } },
            carbohydratesTarget = plan.carbohydrateGrams,
            fatConsumed = meals.sumOf { meal -> meal.items.sumOf { it.fat } },
            fatTarget = plan.fatGrams,
            mealCount = meals.size,
        )
    }