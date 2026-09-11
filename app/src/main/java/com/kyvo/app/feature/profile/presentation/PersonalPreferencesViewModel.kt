package com.kyvo.app.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kyvo.app.feature.onboarding.domain.model.GenderOption
import com.kyvo.app.feature.onboarding.domain.model.ExperienceLevel
import com.kyvo.app.feature.onboarding.domain.model.FoodPreference
import com.kyvo.app.feature.onboarding.domain.model.SavedOnboarding
import com.kyvo.app.feature.onboarding.domain.model.TrainingType
import com.kyvo.app.feature.onboarding.domain.model.WorkActivity
import com.kyvo.app.feature.onboarding.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed interface PersonalPreferencesUiState {
    data object Loading : PersonalPreferencesUiState
    data class Editing(val current: SavedOnboarding, val draft: SavedOnboarding) : PersonalPreferencesUiState
    data class Error(val message: String) : PersonalPreferencesUiState
}

class PersonalPreferencesViewModel(private val repository: OnboardingRepository) : ViewModel() {
    private val _state = MutableStateFlow<PersonalPreferencesUiState>(PersonalPreferencesUiState.Loading)
    val state: StateFlow<PersonalPreferencesUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observe().catch { _state.value = PersonalPreferencesUiState.Error("No pudimos cargar tus datos.") }.collect { saved ->
                val previous = _state.value as? PersonalPreferencesUiState.Editing
                _state.value = if (previous == null) PersonalPreferencesUiState.Editing(saved, saved) else previous.copy(current = saved)
            }
        }
    }

    fun updateGender(value: GenderOption) = updateDraft { copy(gender = value) }
    fun updateAge(value: Int?) = updateDraft { copy(ageYears = value) }
    fun updateHeightCm(value: Double?) = updateDraft { copy(heightCm = value) }
    fun updateWeightKg(value: Double?) = updateDraft { copy(weightKg = value) }
    fun updateTrainingDays(value: Int) = updateDraft { copy(trainingDaysPerWeek = value.coerceIn(1, 7)) }
    fun updateTrainingType(value: TrainingType) = updateDraft { copy(trainingType = value) }
    fun updateWorkActivity(value: WorkActivity) = updateDraft { copy(workActivity = value) }
    fun updateExperience(value: ExperienceLevel) = updateDraft { copy(experience = value) }
    fun updateFoodPreference(value: FoodPreference) = updateDraft { copy(foodPreference = value) }
    fun updateMealsPerDay(value: Int?) = updateDraft { copy(mealsPerDay = value?.coerceIn(1, 8)) }

    fun retry() {
        _state.value = PersonalPreferencesUiState.Loading
        viewModelScope.launch {
            repository.observe().catch { _state.value = PersonalPreferencesUiState.Error("No pudimos cargar tus datos.") }.collect { saved -> _state.value = PersonalPreferencesUiState.Editing(saved, saved) }
        }
    }

    private fun updateDraft(transform: SavedOnboarding.() -> SavedOnboarding) {
        val editing = _state.value as? PersonalPreferencesUiState.Editing ?: return
        _state.value = editing.copy(draft = editing.draft.transform())
    }

    companion object {
        fun factory(repository: OnboardingRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = PersonalPreferencesViewModel(repository) as T
        }
    }
}
