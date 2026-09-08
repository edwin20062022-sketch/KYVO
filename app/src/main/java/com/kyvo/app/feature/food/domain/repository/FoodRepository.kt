package com.kyvo.app.feature.food.domain.repository

import com.kyvo.app.feature.food.domain.model.FoodDetail
import com.kyvo.app.feature.food.domain.model.FoodSearchResult
interface FoodRepository {
    suspend fun searchFoods(query: String): List<FoodSearchResult>
    suspend fun getFood(id: String): FoodDetail?
}
