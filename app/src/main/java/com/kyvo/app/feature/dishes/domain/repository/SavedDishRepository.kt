package com.kyvo.app.feature.dishes.domain.repository

import com.kyvo.app.feature.dishes.domain.model.SavedDish
import kotlinx.coroutines.flow.Flow

interface SavedDishRepository {
    fun observeSavedDishes(): Flow<List<SavedDish>>
    fun observeSavedDish(id: String): Flow<SavedDish?>
    suspend fun saveDish(dish: SavedDish): SavedDish
    suspend fun deleteDish(id: String)
}
