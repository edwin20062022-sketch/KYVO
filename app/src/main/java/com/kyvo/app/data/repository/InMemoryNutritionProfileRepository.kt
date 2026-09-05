package com.kyvo.app.data.repository

import com.kyvo.app.domain.model.NutritionTargets
import com.kyvo.app.domain.repository.NutritionProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Temporary development adapter. A persistent implementation will replace it in its feature phase. */
class InMemoryNutritionProfileRepository : NutritionProfileRepository {
    private val targets = MutableStateFlow<NutritionTargets?>(null)

    override fun observeTargets(): Flow<NutritionTargets?> = targets.asStateFlow()

    override suspend fun saveTargets(targets: NutritionTargets) {
        this.targets.value = targets
    }
}

