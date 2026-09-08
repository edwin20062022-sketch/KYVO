package com.kyvo.app.feature.food.domain.repository

import com.kyvo.app.feature.food.domain.model.FoodDetail
import com.kyvo.app.feature.food.domain.model.FoodSearchResult
import kotlinx.coroutines.flow.Flow

interface FoodRepository { fun searchFoods(query: String): Flow<List<FoodSearchResult>>; suspend fun getFood(id: String): FoodDetail?; fun observeFavorites(): Flow<List<FoodSearchResult>>; fun observeFrequentFoods(): Flow<List<FoodSearchResult>>; suspend fun toggleFavorite(id: String) }
