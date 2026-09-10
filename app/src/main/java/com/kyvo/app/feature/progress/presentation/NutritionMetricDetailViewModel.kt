package com.kyvo.app.feature.progress.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kyvo.app.feature.home.domain.repository.MealRepository
import com.kyvo.app.feature.onboarding.domain.repository.OnboardingRepository
import com.kyvo.app.feature.progress.domain.NutritionMetric
import com.kyvo.app.feature.progress.domain.NutritionMetricDetail
import com.kyvo.app.feature.progress.domain.aggregateDailyNutrition
import com.kyvo.app.feature.progress.domain.buildMetricDetail
import java.time.LocalDate
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

sealed interface NutritionMetricDetailUiState {
    data object Loading : NutritionMetricDetailUiState
    data object Empty : NutritionMetricDetailUiState
    data class Content(val detail: NutritionMetricDetail) : NutritionMetricDetailUiState
    data class Error(val message: String) : NutritionMetricDetailUiState
}

class NutritionMetricDetailViewModel(
    private val onboardingRepository: OnboardingRepository,
    private val mealRepository: MealRepository,
    val metric: NutritionMetric,
    private val today: LocalDate = LocalDate.now(),
) : ViewModel() {
    private val _state = MutableStateFlow<NutritionMetricDetailUiState>(NutritionMetricDetailUiState.Loading)
    val state: StateFlow<NutritionMetricDetailUiState> = _state.asStateFlow()
    private var loadJob: Job? = null

    init { load() }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val startDate = today.minusDays(6)
            combine(onboardingRepository.observe(), mealRepository.observeMeals(startDate, today)) { onboarding, meals ->
                onboarding.plan?.let { plan ->
                    val summaries = aggregateDailyNutrition(meals, plan)
                    plan to (meals to summaries)
                }
            }.catch { _state.value = NutritionMetricDetailUiState.Error("No pudimos cargar este detalle. Inténtalo de nuevo.") }
                .collect { result ->
                    _state.value = when {
                        result == null -> NutritionMetricDetailUiState.Error("Tu plan nutricional aún no está disponible.")
                        result.second.second.none { it.mealCount > 0 } -> NutritionMetricDetailUiState.Empty
                        else -> NutritionMetricDetailUiState.Content(buildMetricDetail(metric, result.second.second, result.second.first))
                    }
                }
        }
    }

    fun retry() {
        _state.value = NutritionMetricDetailUiState.Loading
        load()
    }

    companion object {
        fun factory(onboardingRepository: OnboardingRepository, mealRepository: MealRepository, metric: NutritionMetric): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T = NutritionMetricDetailViewModel(onboardingRepository, mealRepository, metric) as T
            }
    }
}