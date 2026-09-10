package com.kyvo.app.feature.progress.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kyvo.app.feature.home.domain.model.Meal
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

sealed interface NutritionDayDetailUiState {
    data object Loading : NutritionDayDetailUiState
    data object Empty : NutritionDayDetailUiState
    data class Content(val summary: DailyNutritionSummary, val plan: NutritionPlan, val meals: List<Meal>) : NutritionDayDetailUiState
    data class Error(val message: String) : NutritionDayDetailUiState
}

class NutritionDayDetailViewModel(
    private val onboardingRepository: OnboardingRepository,
    private val mealRepository: MealRepository,
    val date: LocalDate,
) : ViewModel() {
    private val _state = MutableStateFlow<NutritionDayDetailUiState>(NutritionDayDetailUiState.Loading)
    val state: StateFlow<NutritionDayDetailUiState> = _state.asStateFlow()
    private var loadJob: Job? = null

    init { load() }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            combine(onboardingRepository.observe(), mealRepository.observeMealsForDate(date)) { onboarding, meals ->
                onboarding.plan?.let { plan -> plan to (meals to aggregateDailyNutrition(mapOf(date to meals), plan).single()) }
            }.catch { _state.value = NutritionDayDetailUiState.Error("No pudimos cargar el detalle del día. Inténtalo de nuevo.") }
                .collect { result ->
                    _state.value = when {
                        result == null -> NutritionDayDetailUiState.Error("Tu plan nutricional aún no está disponible.")
                        result.second.first.isEmpty() -> NutritionDayDetailUiState.Empty
                        else -> NutritionDayDetailUiState.Content(result.second.second, result.first, result.second.first.sortedWith(compareBy<Meal> { it.time == null }.thenBy { it.time ?: "" }.thenBy { it.type.ordinal }))
                    }
                }
        }
    }

    fun retry() {
        _state.value = NutritionDayDetailUiState.Loading
        load()
    }

    companion object {
        fun factory(onboardingRepository: OnboardingRepository, mealRepository: MealRepository, date: LocalDate): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T = NutritionDayDetailViewModel(onboardingRepository, mealRepository, date) as T
            }
    }
}
