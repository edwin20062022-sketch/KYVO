package com.kyvo.app.feature.home.presentation

import com.kyvo.app.feature.home.domain.model.DailyNutrition
import com.kyvo.app.feature.home.domain.model.Meal

sealed interface HomeUiState { data object Loading : HomeUiState; data class Content(val daily: DailyNutrition) : HomeUiState; data class Error(val message: String) : HomeUiState }
sealed interface HomeEvent { data object OpenNutritionDetail : HomeEvent; data class OpenMeal(val mealId: String) : HomeEvent; data class UpdateMeal(val meal: Meal) : HomeEvent; data class DeleteMeal(val mealId: String) : HomeEvent; data object BackToDashboard : HomeEvent; data object Retry : HomeEvent }
sealed interface HomeDestination { data object Dashboard : HomeDestination; data object NutritionDetail : HomeDestination; data class EditMeal(val mealId: String) : HomeDestination }
