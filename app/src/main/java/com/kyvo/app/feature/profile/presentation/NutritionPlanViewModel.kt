package com.kyvo.app.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kyvo.app.feature.onboarding.domain.model.SavedOnboarding
import com.kyvo.app.feature.onboarding.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed interface NutritionPlanUiState {
    data object Loading : NutritionPlanUiState
    data class Content(val onboarding: SavedOnboarding) : NutritionPlanUiState
    data class Incomplete(val onboarding: SavedOnboarding) : NutritionPlanUiState
    data class Error(val message: String) : NutritionPlanUiState
}

class NutritionPlanViewModel(
    private val repository: OnboardingRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<NutritionPlanUiState>(NutritionPlanUiState.Loading)
    val state: StateFlow<NutritionPlanUiState> = _state.asStateFlow()

    init { observePlan() }

    fun retry() {
        _state.value = NutritionPlanUiState.Loading
        observePlan()
    }

    private fun observePlan() {
        viewModelScope.launch {
            repository.observe()
                .catch { _state.value = NutritionPlanUiState.Error("No pudimos cargar tu plan nutricional.") }
                .collect { onboarding ->
                    _state.value = if (onboarding.isCompleted && onboarding.plan != null) {
                        NutritionPlanUiState.Content(onboarding)
                    } else {
                        NutritionPlanUiState.Incomplete(onboarding)
                    }
                }
        }
    }

    companion object {
        fun factory(repository: OnboardingRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T = NutritionPlanViewModel(repository) as T
            }
    }
}
