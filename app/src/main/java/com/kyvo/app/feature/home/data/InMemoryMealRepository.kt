package com.kyvo.app.feature.home.data

import com.kyvo.app.feature.home.domain.model.Meal
import com.kyvo.app.feature.home.domain.repository.MealRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** Local adapter for Phase 3. Food catalog and durable storage are intentionally deferred. */
class InMemoryMealRepository(initialMeals: Map<LocalDate, List<Meal>> = emptyMap()) : MealRepository {
    private val meals = MutableStateFlow(initialMeals)
    override fun observeMealsForDate(date: LocalDate): Flow<List<Meal>> = meals.map { it[date].orEmpty() }
    override fun observeMeal(id: String): Flow<Meal?> = meals.map { days -> days.values.flatten().firstOrNull { it.id == id } }
    override suspend fun updateMeal(meal: Meal) { meals.value = meals.value.mapValues { (_, values) -> values.map { if (it.id == meal.id) meal else it } } }
    override suspend fun deleteMeal(id: String) { meals.value = meals.value.mapValues { (_, values) -> values.filterNot { it.id == id } } }
}
