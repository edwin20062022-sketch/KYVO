package com.kyvo.app.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kyvo.app.domain.auth.AuthSession
import com.kyvo.app.feature.onboarding.domain.model.SavedOnboarding
import com.kyvo.app.feature.onboarding.domain.repository.OnboardingRepository
import com.kyvo.app.feature.profile.domain.AthleteProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed interface AthleteProfileUiState {
    data object Loading : AthleteProfileUiState
    data class Content(val profile: AthleteProfile) : AthleteProfileUiState
    data class Incomplete(val profile: AthleteProfile) : AthleteProfileUiState
    data class Error(val message: String) : AthleteProfileUiState
}

class AthleteProfileViewModel(
    private val session: AuthSession,
    private val repository: OnboardingRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<AthleteProfileUiState>(AthleteProfileUiState.Loading)
    val state: StateFlow<AthleteProfileUiState> = _state.asStateFlow()

    init { observeProfile() }

    fun retry() {
        _state.value = AthleteProfileUiState.Loading
        observeProfile()
    }

    private fun observeProfile() {
        viewModelScope.launch {
            repository.observe()
                .catch { _state.value = AthleteProfileUiState.Error("No pudimos cargar tu perfil.") }
                .collect { saved ->
                    val profile = saved.toAthleteProfile(session)
                    _state.value = if (saved.isCompleted) AthleteProfileUiState.Content(profile) else AthleteProfileUiState.Incomplete(profile)
                }
        }
    }

    companion object {
        fun factory(session: AuthSession, repository: OnboardingRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T = AthleteProfileViewModel(session, repository) as T
            }
    }
}

internal fun SavedOnboarding.toAthleteProfile(session: AuthSession): AthleteProfile = AthleteProfile(
    displayName = session.displayName,
    ageYears = ageYears,
    heightCm = heightCm,
    weightKg = weightKg,
    trainingDaysPerWeek = trainingDaysPerWeek,
    trainingType = trainingType,
    goal = goal,
    experience = experience,
    foodPreference = foodPreference,
    customDietaryRestrictions = customDietaryRestrictions,
    mealsPerDay = mealsPerDay,
    nutritionPlan = plan,
)
