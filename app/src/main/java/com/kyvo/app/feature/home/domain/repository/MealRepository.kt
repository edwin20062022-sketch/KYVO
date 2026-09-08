package com.kyvo.app.feature.home.domain.repository

import com.kyvo.app.feature.home.domain.model.Meal
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface MealRepository {
    fun observeMealsForDate(date: LocalDate): Flow<List<Meal>>
    fun observeMeal(id: String): Flow<Meal?>
    suspend fun addMeal(date: LocalDate, meal: Meal)
    suspend fun updateMeal(meal: Meal)
    suspend fun deleteMeal(id: String)
}
