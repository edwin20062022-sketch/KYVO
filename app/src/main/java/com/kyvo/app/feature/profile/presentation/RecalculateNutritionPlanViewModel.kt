package com.kyvo.app.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kyvo.app.feature.onboarding.domain.calculator.NutritionPlanCalculator
import com.kyvo.app.feature.onboarding.domain.model.FitnessGoal
import com.kyvo.app.feature.onboarding.domain.model.NutritionPlan
import com.kyvo.app.feature.onboarding.domain.model.OnboardingAnswers
import com.kyvo.app.feature.onboarding.domain.model.SavedOnboarding
import com.kyvo.app.feature.onboarding.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed interface RecalculateNutritionPlanUiState {
    data object LoadingCurrentData : RecalculateNutritionPlanUiState
    data class Editing(val current: SavedOnboarding, val selectedGoal: FitnessGoal) : RecalculateNutritionPlanUiState
    data class Calculating(val selectedGoal: FitnessGoal) : RecalculateNutritionPlanUiState
    data class Preview(val current: SavedOnboarding, val selectedGoal: FitnessGoal, val oldPlan: NutritionPlan, val previewPlan: NutritionPlan) : RecalculateNutritionPlanUiState
    data class Saving(val preview: Preview) : RecalculateNutritionPlanUiState
    data object Saved : RecalculateNutritionPlanUiState
    data class Error(val message: String, val current: SavedOnboarding? = null, val selectedGoal: FitnessGoal? = null, val preview: Preview? = null) : RecalculateNutritionPlanUiState
    data object Incomplete : RecalculateNutritionPlanUiState
}

class RecalculateNutritionPlanViewModel(
    private val repository: OnboardingRepository,
    private val calculator: NutritionPlanCalculator = NutritionPlanCalculator(),
) : ViewModel() {
    private val _state = MutableStateFlow<RecalculateNutritionPlanUiState>(RecalculateNutritionPlanUiState.LoadingCurrentData)
    val state: StateFlow<RecalculateNutritionPlanUiState> = _state.asStateFlow()
    private var latest: SavedOnboarding? = null
    private var initialized = false
    private var previewNavigationRequested = false

    init {
        viewModelScope.launch {
            repository.observe().catch {
                if (!initialized) _state.value = RecalculateNutritionPlanUiState.Error("No pudimos cargar tus datos.")
            }.collect { saved ->
                latest = saved
                if (!initialized) {
                    initialized = true
                    _state.value = if (saved.isCompleted && saved.plan != null && saved.goal != null) {
                        RecalculateNutritionPlanUiState.Editing(saved, saved.goal)
                    } else {
                        RecalculateNutritionPlanUiState.Incomplete
                    }
                }
            }
        }
    }

    fun selectGoal(goal: FitnessGoal) {
        val current = latest ?: return
        if (current.plan == null || current.goal == null) return
        _state.value = RecalculateNutritionPlanUiState.Editing(current, goal)
    }

    fun startEditing() {
        val current = latest ?: return
        if (current.isCompleted && current.plan != null && current.goal != null) {
            _state.value = RecalculateNutritionPlanUiState.Editing(current, current.goal)
        } else {
            _state.value = RecalculateNutritionPlanUiState.Incomplete
        }
    }

    fun calculatePreview() {
        val editing = _state.value as? RecalculateNutritionPlanUiState.Editing ?: return
        _state.value = RecalculateNutritionPlanUiState.Calculating(editing.selectedGoal)
        viewModelScope.launch {
            val answers = editing.current.toAnswers(editing.selectedGoal)
            if (answers == null) {
                _state.value = RecalculateNutritionPlanUiState.Error("Faltan datos para recalcular tus metas.", editing.current, editing.selectedGoal)
                return@launch
            }
            runCatching { calculator.calculate(answers) }
                .onSuccess { plan ->
                    previewNavigationRequested = true
                    _state.value = RecalculateNutritionPlanUiState.Preview(editing.current, editing.selectedGoal, requireNotNull(editing.current.plan), plan)
                }
                .onFailure { _state.value = RecalculateNutritionPlanUiState.Error("No pudimos recalcular tus metas.", editing.current, editing.selectedGoal) }
        }
    }

    fun confirm() {
        val preview = (_state.value as? RecalculateNutritionPlanUiState.Preview) ?: return
        _state.value = RecalculateNutritionPlanUiState.Saving(preview)
        viewModelScope.launch {
            runCatching { repository.save(preview.current.copy(goal = preview.selectedGoal, plan = preview.previewPlan)) }
                .onSuccess { _state.value = RecalculateNutritionPlanUiState.Saved }
                .onFailure { _state.value = RecalculateNutritionPlanUiState.Error("No pudimos guardar tus nuevas metas. Inténtalo de nuevo.", preview.current, preview.selectedGoal, preview) }
        }
    }

    fun retry() {
        when (val current = _state.value) {
            is RecalculateNutritionPlanUiState.Error -> current.preview?.let { _state.value = it } ?: current.selectedGoal?.let { goal -> latest?.let { _state.value = RecalculateNutritionPlanUiState.Editing(it, goal) } }
            else -> Unit
        }
    }

    fun consumePreviewNavigation(): Boolean = previewNavigationRequested.also { previewNavigationRequested = false }

    companion object {
        fun factory(repository: OnboardingRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = RecalculateNutritionPlanViewModel(repository) as T
        }
    }
}

private fun SavedOnboarding.toAnswers(goal: FitnessGoal): OnboardingAnswers? {
    val gender = gender ?: return null
    val age = ageYears ?: return null
    val height = heightCm ?: return null
    val weight = weightKg ?: return null
    val trainingDays = trainingDaysPerWeek ?: return null
    val trainingType = trainingType ?: return null
    val workActivity = workActivity ?: return null
    val experience = experience ?: return null
    val foodPreference = foodPreference ?: return null
    val meals = mealsPerDay ?: return null
    return OnboardingAnswers(gender, age, height, weight, trainingDays, trainingType, workActivity, goal, experience, foodPreference, meals)
}
