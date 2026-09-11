package com.kyvo.app.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kyvo.app.feature.onboarding.domain.model.GenderOption
import com.kyvo.app.feature.onboarding.domain.model.ExperienceLevel
import com.kyvo.app.feature.onboarding.domain.model.FoodPreference
import com.kyvo.app.feature.onboarding.domain.calculator.NutritionPlanCalculator
import com.kyvo.app.feature.onboarding.domain.model.FitnessGoal
import com.kyvo.app.feature.onboarding.domain.model.NutritionPlan
import com.kyvo.app.feature.onboarding.domain.model.OnboardingAnswers
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
    data class Review(val current: SavedOnboarding, val draft: SavedOnboarding, val hasChanges: Boolean, val planImpact: Boolean, val oldPlan: NutritionPlan, val previewPlan: NutritionPlan) : PersonalPreferencesUiState
    data class Saving(val review: Review, val keepCurrentPlan: Boolean) : PersonalPreferencesUiState
    data class Saved(val onboarding: SavedOnboarding) : PersonalPreferencesUiState
    data class Error(val message: String) : PersonalPreferencesUiState
    data class SaveError(val message: String, val review: Review, val keepCurrentPlan: Boolean) : PersonalPreferencesUiState
}

class PersonalPreferencesViewModel(
    private val repository: OnboardingRepository,
    private val calculator: NutritionPlanCalculator = NutritionPlanCalculator(),
) : ViewModel() {
    private val _state = MutableStateFlow<PersonalPreferencesUiState>(PersonalPreferencesUiState.Loading)
    val state: StateFlow<PersonalPreferencesUiState> = _state.asStateFlow()
    private var latestSaved: SavedOnboarding? = null

    init {
        viewModelScope.launch {
            repository.observe().catch { _state.value = PersonalPreferencesUiState.Error("No pudimos cargar tus datos.") }.collect { saved ->
                latestSaved = saved
                when (val previous = _state.value) {
                    PersonalPreferencesUiState.Loading -> _state.value = PersonalPreferencesUiState.Editing(saved, saved)
                    is PersonalPreferencesUiState.Editing -> _state.value = previous.copy(current = saved)
                    else -> Unit
                }
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

    fun prepareReview(): Boolean {
        val editing = _state.value as? PersonalPreferencesUiState.Editing ?: return false
        val answers = editing.draft.toAnswers() ?: run {
            _state.value = PersonalPreferencesUiState.Error("Faltan datos obligatorios para revisar tus metas.")
            return false
        }
        val oldPlan = editing.current.plan ?: run {
            _state.value = PersonalPreferencesUiState.Error("No encontramos tu plan nutricional actual.")
            return false
        }
        val planImpact = hasNutritionPlanImpact(editing.current, editing.draft)
        val preview = if (planImpact) runCatching { calculator.calculate(answers) }.getOrNull() else oldPlan
        if (preview == null) {
            _state.value = PersonalPreferencesUiState.Error("No pudimos calcular la vista previa de tus metas.")
            return false
        }
        _state.value = PersonalPreferencesUiState.Review(editing.current, editing.draft, editing.current != editing.draft, planImpact, oldPlan, preview)
        return true
    }

    fun confirmUpdate() = persistReview(keepCurrentPlan = false)
    fun confirmKeepingCurrentPlan() = persistReview(keepCurrentPlan = true)

    fun retry() {
        latestSaved?.let { _state.value = PersonalPreferencesUiState.Editing(it, it) }
    }

    fun retrySave() {
        val error = _state.value as? PersonalPreferencesUiState.SaveError ?: return
        persistReview(error.review, error.keepCurrentPlan)
    }

    private fun updateDraft(transform: SavedOnboarding.() -> SavedOnboarding) {
        val editing = _state.value as? PersonalPreferencesUiState.Editing ?: return
        _state.value = editing.copy(draft = editing.draft.transform())
    }

    private fun persistReview(review: PersonalPreferencesUiState.Review? = _state.value as? PersonalPreferencesUiState.Review, keepCurrentPlan: Boolean) {
        if (review == null || _state.value is PersonalPreferencesUiState.Saving) return
        if (!review.hasChanges) {
            _state.value = PersonalPreferencesUiState.Saved(review.current)
            return
        }
        _state.value = PersonalPreferencesUiState.Saving(review, keepCurrentPlan)
        val finalPlan = if (keepCurrentPlan) review.oldPlan else review.previewPlan
        val finalDraft = review.draft.copy(plan = finalPlan, isCompleted = review.current.isCompleted, currentStep = review.current.currentStep)
        viewModelScope.launch {
            runCatching { repository.save(finalDraft) }
                .onSuccess { _state.value = PersonalPreferencesUiState.Saved(finalDraft) }
                .onFailure { _state.value = PersonalPreferencesUiState.SaveError("No pudimos guardar tus cambios. Inténtalo de nuevo.", review, keepCurrentPlan) }
        }
    }

    companion object {
        fun factory(repository: OnboardingRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = PersonalPreferencesViewModel(repository) as T
        }
    }
}

private fun SavedOnboarding.toAnswers(): OnboardingAnswers? {
    val gender = gender ?: return null
    val age = ageYears ?: return null
    val height = heightCm ?: return null
    val weight = weightKg ?: return null
    val days = trainingDaysPerWeek ?: return null
    val training = trainingType ?: return null
    val activity = workActivity ?: return null
    val goal = goal ?: return null
    val experience = experience ?: return null
    val preference = foodPreference ?: return null
    val meals = mealsPerDay ?: return null
    return OnboardingAnswers(gender, age, height, weight, days, training, activity, goal, experience, preference, meals)
}

internal fun hasNutritionPlanImpact(current: SavedOnboarding, draft: SavedOnboarding): Boolean =
    current.gender != draft.gender ||
        current.ageYears != draft.ageYears ||
        current.heightCm != draft.heightCm ||
        current.weightKg != draft.weightKg ||
        current.trainingDaysPerWeek != draft.trainingDaysPerWeek ||
        current.trainingType != draft.trainingType ||
        current.workActivity != draft.workActivity ||
        current.goal != draft.goal
