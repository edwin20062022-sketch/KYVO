package com.kyvo.app.feature.progress.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kyvo.app.feature.home.domain.repository.MealRepository
import com.kyvo.app.feature.onboarding.domain.repository.OnboardingRepository
import com.kyvo.app.feature.progress.domain.NutritionConsistencySummary
import com.kyvo.app.feature.progress.domain.aggregateDailyNutrition
import com.kyvo.app.feature.progress.domain.calculateConsistency
import java.time.LocalDate
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

sealed interface NutritionConsistencyUiState {
    data object Loading : NutritionConsistencyUiState
    data object Empty : NutritionConsistencyUiState
    data class Content(val summary: NutritionConsistencySummary) : NutritionConsistencyUiState
    data class Error(val message: String) : NutritionConsistencyUiState
}

class NutritionConsistencyViewModel(
    private val onboardingRepository: OnboardingRepository,
    private val mealRepository: MealRepository,
    private val today: LocalDate = LocalDate.now(),
) : ViewModel() {
    private val _state = MutableStateFlow<NutritionConsistencyUiState>(NutritionConsistencyUiState.Loading)
    val state: StateFlow<NutritionConsistencyUiState> = _state.asStateFlow()
    private var loadJob: Job? = null

    init { load() }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val startDate = today.minusDays(29)
            combine(onboardingRepository.observe(), mealRepository.observeMeals(startDate, today)) { onboarding, meals ->
                onboarding.plan?.let { plan ->
                    val completeRange = generateSequence(startDate) { date -> date.plusDays(1).takeIf { it <= today } }
                        .associateWith { meals[it].orEmpty() }
                    calculateConsistency(aggregateDailyNutrition(completeRange, plan))
                }
            }.catch { _state.value = NutritionConsistencyUiState.Error("No pudimos cargar tu consistencia. Inténtalo de nuevo.") }
                .collect { summary ->
                    _state.value = when {
                        summary == null -> NutritionConsistencyUiState.Error("Tu plan nutricional aún no está disponible.")
                        summary.evaluableDays == 0 -> NutritionConsistencyUiState.Empty
                        else -> NutritionConsistencyUiState.Content(summary)
                    }
                }
        }
    }

    fun retry() {
        _state.value = NutritionConsistencyUiState.Loading
        load()
    }

    companion object {
        fun factory(onboardingRepository: OnboardingRepository, mealRepository: MealRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T = NutritionConsistencyViewModel(onboardingRepository, mealRepository) as T
            }
    }
}
