package com.kyvo.app.feature.food.domain

import com.kyvo.app.feature.food.domain.model.FoodSearchResult
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class FrequentFoodCandidate(val food: FoodSearchResult, val uses: Int, val lastUsed: LocalDate)

/** Deterministic ranking: each use is worth 10 points; recency contributes up to one point. */
fun rankFrequentFoods(candidates: List<FrequentFoodCandidate>, today: LocalDate): List<FoodSearchResult> = candidates
    .sortedWith(
        compareByDescending<FrequentFoodCandidate> { it.uses * 10.0 + recencyScore(it.lastUsed, today) }
            .thenBy { it.food.name.lowercase() }
            .thenBy { it.food.id },
    )
    .map(FrequentFoodCandidate::food)

private fun recencyScore(lastUsed: LocalDate, today: LocalDate): Double {
    val days = ChronoUnit.DAYS.between(lastUsed, today).coerceAtLeast(0)
    return (1.0 - days.toDouble() / 30.0).coerceIn(0.0, 1.0)
}
