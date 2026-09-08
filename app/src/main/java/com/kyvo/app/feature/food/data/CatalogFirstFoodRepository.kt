package com.kyvo.app.feature.food.data

import com.kyvo.app.feature.food.domain.model.FoodDetail
import com.kyvo.app.feature.food.domain.model.FoodSearchResult
import com.kyvo.app.feature.food.domain.model.FoodType
import com.kyvo.app.feature.food.domain.repository.FoodRepository

/**
 * Keeps direct Android access limited to Supabase. The gateway calls KYVO's
 * authenticated Edge Function; it never calls USDA or Open Food Facts itself.
 */
interface FoodSearchEdgeGateway {
    suspend fun searchFoods(query: String): List<FoodSearchResult>
    suspend fun getFood(id: String, type: String): FoodDetail?
}

class CatalogFirstFoodRepository(
    private val catalog: FoodRepository,
    private val edgeGateway: FoodSearchEdgeGateway,
    private val minimumCatalogResults: Int = 5,
) : FoodRepository {
    override suspend fun searchFoods(query: String): List<FoodSearchResult> {
        val cached = catalog.searchFoods(query)
        if (cached.size >= minimumCatalogResults) return cached

        val providerResults = edgeGateway.searchFoods(query)
        return rankCatalogResults(
            query = query,
            candidates = (cached + providerResults)
                .distinctBy { "${it.type}:${it.id}" }
                .map { it to emptyList() },
        )
    }

    override suspend fun getFood(id: String): FoodDetail? =
        catalog.getFood(id) ?: edgeGateway.getFood(id, "generic") ?: edgeGateway.getFood(id, "commercial")

    override suspend fun getFavoriteFoods(): List<FoodSearchResult> = catalog.getFavoriteFoods()
    override suspend fun isFavorite(foodId: String, type: FoodType): Boolean = catalog.isFavorite(foodId, type)
    override suspend fun setFavorite(foodId: String, type: FoodType, favorite: Boolean) = catalog.setFavorite(foodId, type, favorite)
    override suspend fun getFrequentFoods(): List<FoodSearchResult> = catalog.getFrequentFoods()
}
