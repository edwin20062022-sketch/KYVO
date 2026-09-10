package com.kyvo.app.feature.progress.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kyvo.app.feature.home.domain.repository.MealRepository
import com.kyvo.app.feature.onboarding.domain.model.NutritionPlan
import com.kyvo.app.feature.onboarding.domain.repository.OnboardingRepository
import com.kyvo.app.feature.progress.domain.DailyNutritionSummary
import com.kyvo.app.feature.progress.domain.aggregateDailyNutrition
import java.time.LocalDate
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

sealed interface ProgressUiState {
    data object Loading : ProgressUiState
    data object Empty : ProgressUiState
    data class Content(val days: List<DailyNutritionSummary>, val plan: NutritionPlan) : ProgressUiState
    data class Error(val message: String) : ProgressUiState
}

class ProgressViewModel(
    private val onboardingRepository: OnboardingRepository,
    private val mealRepository: MealRepository,
    private val today: LocalDate = LocalDate.now(),
) : ViewModel() {
    private val _state = MutableStateFlow<ProgressUiState>(ProgressUiState.Loading)
    val state: StateFlow<ProgressUiState> = _state.asStateFlow()
    private var loadJob: Job? = null

    init { load() }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val startDate = today.minusDays(6)
            combine(onboardingRepository.observe(), mealRepository.observeMeals(startDate, today)) { onboarding, meals ->
                onboarding.plan?.let { plan -> plan to aggregateDailyNutrition(meals, plan) }
            }.catch { _state.value = ProgressUiState.Error("No pudimos cargar tu progreso. Inténtalo de nuevo.") }
                .collect { result ->
                    _state.value = when {
                        result == null -> ProgressUiState.Error("Tu plan nutricional aún no está disponible.")
                        result.second.none { it.mealCount > 0 } -> ProgressUiState.Empty
                        else -> ProgressUiState.Content(result.second, result.first)
                    }
                }
        }
    }

    fun retry() {
        _state.value = ProgressUiState.Loading
        load()
    }

    companion object {
        fun factory(onboardingRepository: OnboardingRepository, mealRepository: MealRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T = ProgressViewModel(onboardingRepository, mealRepository) as T
            }
    }
}