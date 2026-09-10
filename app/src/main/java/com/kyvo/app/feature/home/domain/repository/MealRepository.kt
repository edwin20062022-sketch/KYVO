package com.kyvo.app.feature.home.domain.repository

import com.kyvo.app.feature.home.domain.model.Meal
import com.kyvo.app.feature.home.domain.model.MealType
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

interface MealRepository {
    fun observeMealsForDate(date: LocalDate): Flow<List<Meal>>
    fun observeMeals(startDate: LocalDate, endDate: LocalDate): Flow<Map<LocalDate, List<Meal>>> = flow {
        require(!endDate.isBefore(startDate))
        val dates = generateSequence(startDate) { current -> current.plusDays(1).takeIf { it <= endDate } }.toList()
        emitAll(combine(dates.map(::observeMealsForDate)) { meals -> dates.zip(meals).toMap() })
    }
    fun observeMeal(id: String): Flow<Meal?>
    suspend fun addMeal(date: LocalDate, meal: Meal)
    suspend fun updateMeal(meal: Meal)
    suspend fun deleteMeal(id: String)
    suspend fun addSavedDishToDay(dishId: String, date: LocalDate, type: MealType, portions: Double): Meal =
        throw UnsupportedOperationException("Saved dishes are not available in this repository.")
}
