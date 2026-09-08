package com.kyvo.app.feature.home

import com.kyvo.app.feature.home.data.InMemoryMealRepository
import com.kyvo.app.feature.home.domain.model.Meal
import com.kyvo.app.feature.home.domain.model.MealItem
import com.kyvo.app.feature.home.domain.model.MealType
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InMemoryMealRepositoryTest {
    private val date = LocalDate.of(2026, 9, 8)
    private fun meal(title: String = "Comida") = Meal("meal-1", MealType.Lunch, title, items = listOf(MealItem("item-1", "Arroz", 100.0, "g", 130, 2, 28, 0)), totalCalories = 130, protein = 2, carbohydrates = 28, fat = 0)

    @Test fun `observes meals for selected date`() = runTest { assertEquals(1, InMemoryMealRepository(mapOf(date to listOf(meal()))).observeMealsForDate(date).first().size) }
    @Test fun `updates an existing meal`() = runTest { val repository = InMemoryMealRepository(mapOf(date to listOf(meal()))); repository.updateMeal(meal("Cena")); assertEquals("Cena", repository.observeMeal("meal-1").first()?.title) }
    @Test fun `deletes an existing meal`() = runTest { val repository = InMemoryMealRepository(mapOf(date to listOf(meal()))); repository.deleteMeal("meal-1"); assertNull(repository.observeMeal("meal-1").first()) }
}
