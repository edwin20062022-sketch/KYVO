package com.kyvo.app.feature.food

import com.kyvo.app.feature.food.domain.model.FoodDetail
import com.kyvo.app.feature.food.domain.model.FoodNutrients
import com.kyvo.app.feature.food.domain.model.FoodSearchResult
import com.kyvo.app.feature.food.domain.model.FoodType
import com.kyvo.app.feature.food.domain.repository.FoodRepository
import com.kyvo.app.feature.food.presentation.FavoriteFoodsViewModel
import com.kyvo.app.feature.food.presentation.FoodFavoriteViewModel
import com.kyvo.app.feature.food.presentation.SavedFoodsUiState
import com.kyvo.app.test.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FoodFavoriteViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    private val food = FoodSearchResult("food-1", FoodType.Generic, "Pollo", null, null, null, FoodNutrients(165.0, 31.0, 0.0, 3.6), "100 g")

    @Test fun `empty favorites is a valid state`() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = FavoriteFoodsViewModel(FakeFavoriteRepository(emptyList()), mainDispatcherRule.testDispatcher)
        advanceUntilIdle()
        assertEquals(SavedFoodsUiState.Empty, vm.state.value)
    }

    @Test fun `favorite toggle is optimistic and rolls back on persistence error`() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = FoodFavoriteViewModel(FakeFavoriteRepository(emptyList(), failWrites = true), "food-1", FoodType.Generic, mainDispatcherRule.testDispatcher)
        advanceUntilIdle()
        vm.toggle()
        assertTrue(vm.state.value.isFavorite)
        advanceUntilIdle()
        assertFalse(vm.state.value.isFavorite)
        assertTrue(vm.state.value.error != null)
    }

    @Test fun `favorite list removes item through the repository boundary`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeFavoriteRepository(listOf(food))
        val vm = FavoriteFoodsViewModel(repository, mainDispatcherRule.testDispatcher)
        advanceUntilIdle()
        vm.remove(food)
        advanceUntilIdle()
        assertTrue(vm.state.value is SavedFoodsUiState.Empty)
        assertEquals(listOf("food-1"), repository.removed)
    }
}

private class FakeFavoriteRepository(private val favorites: List<FoodSearchResult>, private val failWrites: Boolean = false) : FoodRepository {
    val removed = mutableListOf<String>()
    override suspend fun searchFoods(query: String) = emptyList<FoodSearchResult>()
    override suspend fun getFood(id: String): FoodDetail? = null
    override suspend fun getFavoriteFoods() = favorites
    override suspend fun isFavorite(foodId: String, type: FoodType) = favorites.any { it.id == foodId && it.type == type }
    override suspend fun setFavorite(foodId: String, type: FoodType, favorite: Boolean) { if (failWrites) error("write failed"); if (!favorite) removed += foodId }
}
