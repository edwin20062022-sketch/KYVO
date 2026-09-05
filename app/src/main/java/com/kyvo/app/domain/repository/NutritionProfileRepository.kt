package com.kyvo.app.domain.repository

import com.kyvo.app.domain.model.NutritionTargets
import kotlinx.coroutines.flow.Flow

interface NutritionProfileRepository {
    fun observeTargets(): Flow<NutritionTargets?>
    suspend fun saveTargets(targets: NutritionTargets)
}

