package com.kyvo.app.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kyvo.app.feature.home.domain.DateProvider
import com.kyvo.app.feature.home.domain.SystemDateProvider
import com.kyvo.app.feature.home.domain.model.Meal
import com.kyvo.app.feature.home.domain.model.toDailyNutrition
import com.kyvo.app.feature.home.domain.repository.MealRepository
import com.kyvo.app.feature.onboarding.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class HomeViewModel(onboardingRepository: OnboardingRepository, private val mealRepository: MealRepository, dateProvider: DateProvider = SystemDateProvider) : ViewModel() {
    private val selectedDate = dateProvider.today()
    private val _state = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val state: StateFlow<HomeUiState> = _state.asStateFlow()
    private val _destination = MutableStateFlow<HomeDestination>(HomeDestination.Dashboard)
    val destination: StateFlow<HomeDestination> = _destination.asStateFlow()
    init { viewModelScope.launch { combine(onboardingRepository.observe().map { it.plan }, mealRepository.observeMealsForDate(selectedDate)) { plan, meals -> plan?.toDailyNutrition(selectedDate, meals) }.catch { _state.value = HomeUiState.Error("No pudimos cargar tu resumen. Inténtalo de nuevo.") }.collect { daily -> _state.value = if (daily == null) HomeUiState.Error("Tu plan nutricional aún no está disponible.") else HomeUiState.Content(daily) } } }
    fun onEvent(event: HomeEvent) = when (event) { HomeEvent.OpenNutritionDetail -> _destination.value = HomeDestination.NutritionDetail; is HomeEvent.OpenMeal -> _destination.value = HomeDestination.EditMeal(event.mealId); HomeEvent.BackToDashboard -> _destination.value = HomeDestination.Dashboard; is HomeEvent.UpdateMeal -> viewModelScope.launch { mealRepository.updateMeal(event.meal); _destination.value = HomeDestination.Dashboard }; is HomeEvent.DeleteMeal -> viewModelScope.launch { mealRepository.deleteMeal(event.mealId); _destination.value = HomeDestination.Dashboard }; HomeEvent.Retry -> Unit }
    fun mealForEditing(id: String) = mealRepository.observeMeal(id)
    companion object { fun factory(onboardingRepository: OnboardingRepository, mealRepository: MealRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory { @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(onboardingRepository, mealRepository) as T } }
}
