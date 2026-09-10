package com.kyvo.app.feature.progress.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kyvo.app.feature.home.domain.repository.MealRepository
import com.kyvo.app.feature.onboarding.domain.repository.OnboardingRepository
import com.kyvo.app.feature.progress.domain.NutritionHistoryDay
import com.kyvo.app.feature.progress.domain.aggregateDailyNutrition
import com.kyvo.app.feature.progress.domain.buildNutritionHistoryDays
import com.kyvo.app.feature.progress.domain.monthDateRange
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

sealed interface NutritionHistoryUiState {
    data object Loading : NutritionHistoryUiState
    data object Empty : NutritionHistoryUiState
    data class Content(val month: YearMonth, val days: List<NutritionHistoryDay>, val selectedDate: LocalDate) : NutritionHistoryUiState
    data class Error(val message: String) : NutritionHistoryUiState
}

class NutritionHistoryViewModel(
    private val onboardingRepository: OnboardingRepository,
    private val mealRepository: MealRepository,
    private val month: YearMonth = YearMonth.now(),
) : ViewModel() {
    private val _state = MutableStateFlow<NutritionHistoryUiState>(NutritionHistoryUiState.Loading)
    val state: StateFlow<NutritionHistoryUiState> = _state.asStateFlow()
    private var loadJob: Job? = null

    init { load() }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val dates = monthDateRange(month)
            combine(onboardingRepository.observe(), mealRepository.observeMeals(dates.first(), dates.last())) { onboarding, meals ->
                onboarding.plan?.let { plan -> buildNutritionHistoryDays(aggregateDailyNutrition(meals, plan)) }
            }.catch { _state.value = NutritionHistoryUiState.Error("No pudimos cargar tu historial. Inténtalo de nuevo.") }
                .collect { days ->
                    _state.value = when {
                        days == null -> NutritionHistoryUiState.Error("Tu plan nutricional aún no está disponible.")
                        days.none { it.summary.mealCount > 0 } -> NutritionHistoryUiState.Empty
                        else -> NutritionHistoryUiState.Content(month, days, days.firstOrNull { it.summary.mealCount > 0 }?.summary?.date ?: days.first().summary.date)
                    }
                }
        }
    }

    fun selectDate(date: LocalDate) {
        val current = _state.value
        if (current is NutritionHistoryUiState.Content && date in month.atDay(1)..month.atEndOfMonth()) {
            _state.value = current.copy(selectedDate = date)
        }
    }

    fun retry() {
        _state.value = NutritionHistoryUiState.Loading
        load()
    }

    companion object {
        fun factory(onboardingRepository: OnboardingRepository, mealRepository: MealRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T = NutritionHistoryViewModel(onboardingRepository, mealRepository) as T
            }
    }
}
