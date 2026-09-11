package com.kyvo.app.feature.onboarding.domain.repository

import com.kyvo.app.feature.onboarding.domain.model.SavedOnboarding
import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {
    fun observe(): Flow<SavedOnboarding>
    suspend fun save(progress: SavedOnboarding)
    suspend fun clearUserData() = Unit
}
