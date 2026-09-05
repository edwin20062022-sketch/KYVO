package com.kyvo.app.feature.onboarding.data

import com.kyvo.app.feature.onboarding.domain.model.SavedOnboarding
import com.kyvo.app.feature.onboarding.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryOnboardingRepository(initial: SavedOnboarding = SavedOnboarding()) : OnboardingRepository {
    private val state = MutableStateFlow(initial)
    override fun observe(): Flow<SavedOnboarding> = state.asStateFlow()
    override suspend fun save(progress: SavedOnboarding) { state.value = progress }
}
