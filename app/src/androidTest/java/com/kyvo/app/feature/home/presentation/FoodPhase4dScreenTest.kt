package com.kyvo.app.feature.food

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kyvo.app.core.designsystem.KyvoTheme
import com.kyvo.app.feature.food.domain.model.FoodDetail
import com.kyvo.app.feature.food.domain.model.FoodNutrients
import com.kyvo.app.feature.food.domain.model.FoodSearchResult
import com.kyvo.app.feature.food.domain.model.FoodType
import com.kyvo.app.feature.food.domain.repository.FoodRepository
import com.kyvo.app.feature.food.presentation.FoodDetailRoute
import com.kyvo.app.feature.food.presentation.FoodFavoritesRoute
import org.junit.Rule
import org.junit.Test

class FoodPhase4dScreenTest {
    @get:Rule val compose = createComposeRule()
    private val food = FoodSearchResult("food-1", FoodType.Generic, "Pollo", "Pechuga", null, null, FoodNutrients(165.0, 31.0, 0.0, 3.6), "100 g")
    private val detail = FoodDetail("food-1", FoodType.Generic, "Pollo", "Pechuga", null, food.nutrients, emptyList())

    @Test fun favoritesEmptyStateIsRendered() {
        compose.setContent { KyvoTheme { FoodFavoritesRoute(FakeFoodRepository(emptyList()), {}, {}, {}) } }
        compose.onNodeWithText("Aún no tienes favoritos").assertIsDisplayed()
    }

    @Test fun foodDetailFavoriteToggleUsesAccessibleAction() {
        compose.setContent { KyvoTheme { FoodDetailRoute("food-1", "Generic", FakeFoodRepository(emptyList(), detail), {}, {}) } }
        compose.onNodeWithContentDescription("Agregar a favoritos").performClick()
        compose.onNodeWithContentDescription("Quitar de favoritos").assertIsDisplayed()
    }
}

private class FakeFoodRepository(private val favorites: List<FoodSearchResult>, private val detail: FoodDetail? = null) : FoodRepository {
    private var favorite = favorites.isNotEmpty()
    override suspend fun searchFoods(query: String) = listOf(FoodSearchResult("food-1", FoodType.Generic, "Pollo", "Pechuga", null, null, FoodNutrients(165.0, 31.0, 0.0, 3.6), "100 g"))
    override suspend fun getFood(id: String) = detail
    override suspend fun getFavoriteFoods() = favorites
    override suspend fun isFavorite(foodId: String, type: FoodType) = favorite
    override suspend fun setFavorite(foodId: String, type: FoodType, favorite: Boolean) { this.favorite = favorite }
}
