package com.kyvo.app.feature.food.domain.repository

import com.kyvo.app.feature.food.domain.model.FoodDetail
import com.kyvo.app.feature.food.domain.model.FoodSearchResult
import com.kyvo.app.feature.food.domain.model.FoodType
interface FoodRepository {
    suspend fun searchFoods(query: String): List<FoodSearchResult>
    suspend fun getFood(id: String): FoodDetail?
    suspend fun getFavoriteFoods(): List<FoodSearchResult> = emptyList()
    suspend fun isFavorite(foodId: String, type: FoodType): Boolean = false
    suspend fun setFavorite(foodId: String, type: FoodType, favorite: Boolean) = Unit
    suspend fun getFrequentFoods(): List<FoodSearchResult> = emptyList()
}
