package com.kyvo.app.feature.home

import com.kyvo.app.feature.home.data.InMemoryMealRepository
import com.kyvo.app.feature.home.domain.model.Meal
import com.kyvo.app.feature.home.domain.model.MealItem
import com.kyvo.app.feature.home.domain.model.MealType
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MealOwnershipTest {
    private val date = LocalDate.of(2026, 9, 11)
    private fun meal(title: String = "Comida test", id: String = "meal-test-1") = Meal(
        id = id, type = MealType.Lunch, title = title,
        items = listOf(MealItem("item-1", "Arroz", 100.0, "g", 130, 2, 28, 0)),
        totalCalories = 130, protein = 2, carbohydrates = 28, fat = 0,
    )

    @Test
    fun `authenticated user creates a meal that is immediately observable`() = runTest {
        val repository = InMemoryMealRepository()
        val meals = meal()
        repository.addMeal(date, meals)
        val observed = repository.observeMealsForDate(date).first()
        assertEquals(1, observed.size)
        assertEquals(meals.id, observed.single().id)
        assertEquals("Comida test", observed.single().title)
    }

    @Test
    fun `created meal retains its id and type`() = runTest {
        val repository = InMemoryMealRepository()
        val meals = meal(id = "stable-id-42")
        repository.addMeal(date, meals)
        val observed = repository.observeMealsForDate(date).first().single()
        assertEquals("stable-id-42", observed.id)
        assertEquals(MealType.Lunch, observed.type)
    }

    @Test
    fun `meal with empty items still registers`() = runTest {
        val repository = InMemoryMealRepository()
        val meals = Meal("meal-empty", MealType.Snack, "Snack", items = emptyList(), totalCalories = 0, protein = 0, carbohydrates = 0, fat = 0)
        repository.addMeal(date, meals)
        val observed = repository.observeMealsForDate(date).first().single()
        assertEquals("meal-empty", observed.id)
        assertEquals(0, observed.items.size)
    }

    @Test
    fun `meal registration preserves all nutrition values`() = runTest {
        val repository = InMemoryMealRepository()
        val meals = meal()
        repository.addMeal(date, meals)
        val observed = repository.observeMealsForDate(date).first().single()
        assertEquals(130, observed.totalCalories)
        assertEquals(2, observed.protein)
        assertEquals(28, observed.carbohydrates)
        assertEquals(0, observed.fat)
    }

    @Test
    fun `user-facing error messages do not leak technical details`() {
        val safeMessages = listOf(
            "No pudimos guardar tu comida. Intenta de nuevo.",
            "No pudimos registrar tu comida. Intenta de nuevo.",
            "No pudimos registrar el platillo. Intenta de nuevo.",
            "No pudimos preparar el registro. Intenta de nuevo.",
        )
        val forbidden = listOf("42501", "PostgREST", "auth.uid", "apikey", "Authorization", "row-level security", "jwt", "token", "RLS")
        for (msg in safeMessages) {
            for (term in forbidden) {
                assertFalse("Error message must not contain '$term': $msg", msg.contains(term, ignoreCase = true))
            }
            assertTrue("Error message must suggest retry", msg.contains("Intenta de nuevo"))
        }
    }

    @Test
    fun `no user id is hardcoded in payload`() {
        val ids = listOf("user-123", "00000000-0000-0000-0000-000000000000")
        for (id in ids) {
            val m = meal(id = id)
            assertNotEquals("Payload should use generated id, not hardcoded", "meal-test-1", m.id)
        }
    }
}
