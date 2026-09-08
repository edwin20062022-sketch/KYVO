package com.kyvo.app.feature.food

import com.kyvo.app.feature.food.domain.FrequentFoodCandidate
import com.kyvo.app.feature.food.domain.rankFrequentFoods
import com.kyvo.app.feature.food.domain.model.FoodNutrients
import com.kyvo.app.feature.food.domain.model.FoodSearchResult
import com.kyvo.app.feature.food.domain.model.FoodType
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class FrequentFoodRankerTest {
    private fun food(id: String) = FoodSearchResult(id, FoodType.Generic, id, null, null, null, FoodNutrients(100.0, 10.0, 10.0, 3.0), "100 g")

    @Test fun `empty history is valid`() { assertEquals(emptyList<FoodSearchResult>(), rankFrequentFoods(emptyList(), LocalDate.of(2026, 9, 8))) }

    @Test fun `frequency wins while recency breaks comparable usage`() {
        val today = LocalDate.of(2026, 9, 8)
        val ranked = rankFrequentFoods(listOf(FrequentFoodCandidate(food("old"), 2, today.minusDays(2)), FrequentFoodCandidate(food("new"), 1, today)), today)
        assertEquals(listOf("old", "new"), ranked.map(FoodSearchResult::id))
    }

    @Test fun `same score is deterministic by name and id`() {
        val today = LocalDate.of(2026, 9, 8)
        val ranked = rankFrequentFoods(listOf(FrequentFoodCandidate(food("b"), 1, today), FrequentFoodCandidate(food("a"), 1, today)), today)
        assertEquals(listOf("a", "b"), ranked.map(FoodSearchResult::id))
    }
}
