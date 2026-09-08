package com.kyvo.app.feature.home

import com.kyvo.app.feature.home.data.InMemoryMealRepository
import com.kyvo.app.feature.home.domain.model.Meal
import com.kyvo.app.feature.home.domain.model.MealItem
import com.kyvo.app.feature.home.domain.model.MealType
import com.kyvo.app.feature.home.presentation.MealDetailUiState
import com.kyvo.app.feature.home.presentation.MealDetailViewModel
import com.kyvo.app.test.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MealDetailViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    private val date = LocalDate.of(2026, 9, 8)
    private val first = MealItem("item-1", "Pollo", 150.0, "g", 248, 31, 0, 5, "https://example.com/pollo.jpg")
    private val second = MealItem("item-2", "Arroz", 200.0, "g", 260, 5, 56, 1)
    private val meal = Meal("meal-1", MealType.Lunch, "Comida", "13:30", listOf(first, second), 0, 0, 0, 0)

    @Test fun `detail derives totals and keeps historical snapshots`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = InMemoryMealRepository(mapOf(date to listOf(meal)))
        val vm = MealDetailViewModel(repository, meal.id)
        advanceUntilIdle()
        val content = vm.state.value as MealDetailUiState.Content
        assertEquals(508, content.meal.totalCalories)
        assertEquals("Pollo", content.meal.items.first().name)
        assertEquals(first.image, content.meal.items.first().image)
    }

    @Test fun `detail reflects edit and deletion without manual refresh`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = InMemoryMealRepository(mapOf(date to listOf(meal)))
        val vm = MealDetailViewModel(repository, meal.id)
        advanceUntilIdle()
        repository.updateMeal(meal.copy(items = listOf(first.copy(calories = 300))))
        advanceUntilIdle()
        assertEquals(300, (vm.state.value as MealDetailUiState.Content).meal.totalCalories)
        repository.deleteMeal(meal.id)
        advanceUntilIdle()
        assertEquals(MealDetailUiState.NotFound, vm.state.value)
    }
}
