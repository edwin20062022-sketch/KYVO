package com.kyvo.app.feature.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kyvo.app.feature.onboarding.domain.calculator.NutritionPlanCalculator
import com.kyvo.app.feature.onboarding.domain.model.OnboardingAnswers
import com.kyvo.app.feature.onboarding.domain.model.OnboardingMode
import com.kyvo.app.feature.onboarding.domain.model.OnboardingStep
import com.kyvo.app.feature.onboarding.domain.model.SavedOnboarding
import com.kyvo.app.feature.onboarding.domain.repository.OnboardingRepository
import com.kyvo.app.feature.onboarding.domain.validation.OnboardingValidationError
import com.kyvo.app.feature.onboarding.domain.validation.OnboardingLimits
import com.kyvo.app.feature.onboarding.domain.validation.OnboardingValidator
import com.kyvo.app.feature.onboarding.domain.validation.ValidationResult
import com.kyvo.app.feature.onboarding.domain.restriction.DietaryRestrictionNormalizer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val repository: OnboardingRepository,
    private val calculator: NutritionPlanCalculator = NutritionPlanCalculator(),
    private val transitionMillis: Long = 450L,
    private val mode: OnboardingMode = OnboardingMode.Initial,
    private val onComplete: () -> Unit = {},
    private val onCancel: () -> Unit = {},
) : ViewModel() {
    private val mutableState = MutableStateFlow(OnboardingUiState(mode = mode))
    val state: StateFlow<OnboardingUiState> = mutableState.asStateFlow()
    private var calculationJob: Job? = null
    private var original: SavedOnboarding? = null

    init {
        viewModelScope.launch {
            val saved = repository.observe().first()
            if (mode == OnboardingMode.Edit && saved.isCompleted) {
                original = saved
                mutableState.value = saved.toUiStateForEdit()
            } else {
                mutableState.value = saved.toUiState()
            }
        }
    }

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.SelectGender -> edit { copy(gender = event.value) }
            is OnboardingEvent.ChangeAge -> edit {
                val age = event.value.digitsOnly()
                copy(ageInput = age.takeUnless { it.toIntOrNull() == OnboardingLimits.MinimumVisualAge }.orEmpty())
            }
            is OnboardingEvent.ChangeHeight -> edit { copy(heightInput = event.value.decimalOnly()) }
            is OnboardingEvent.ChangeWeight -> edit { copy(weightInput = event.value.decimalOnly()) }
            is OnboardingEvent.SelectTrainingDays -> edit { copy(trainingDaysPerWeek = event.value) }
            is OnboardingEvent.SelectTrainingType -> edit { copy(trainingType = event.value) }
            is OnboardingEvent.SelectWorkActivity -> edit { copy(workActivity = event.value) }
            is OnboardingEvent.SelectGoal -> edit { copy(goal = event.value) }
            is OnboardingEvent.SelectExperience -> edit { copy(experience = event.value) }
            is OnboardingEvent.SelectFoodPreference -> edit { copy(foodPreference = event.value) }
            is OnboardingEvent.ChangeCustomRestrictionInput -> edit { copy(customRestrictionInput = event.value) }
            is OnboardingEvent.AddCustomRestriction -> addCustomRestriction()
            is OnboardingEvent.RemoveCustomRestriction -> edit {
                copy(customDietaryRestrictions = customDietaryRestrictions.toMutableList().apply { removeAt(event.index) })
            }
            is OnboardingEvent.SelectMeals -> edit { copy(mealsPerDay = event.value, isCustomMeals = event.custom) }
            OnboardingEvent.Continue -> continueWizard()
            OnboardingEvent.Back -> goBack()
            OnboardingEvent.NavigationHandled -> mutableState.update { it.copy(shouldExit = false, shouldNavigateHome = false, shouldNavigateBack = false) }
        }
    }

    private fun edit(transform: OnboardingUiState.() -> OnboardingUiState) {
        mutableState.update { it.transform().copy(validationError = null) }
        if (mutableState.value.mode == OnboardingMode.Initial) persist()
    }

    private fun addCustomRestriction() {
        val input = mutableState.value.customRestrictionInput.trim()
        if (input.isBlank()) return
        val normalized = DietaryRestrictionNormalizer.normalize(input)
        val isDuplicate = mutableState.value.customDietaryRestrictions.any { existing ->
            DietaryRestrictionNormalizer.normalize(existing) == normalized
        }
        if (isDuplicate) {
            mutableState.update { it.copy(customRestrictionInput = "", validationError = null) }
            return
        }
        edit {
            copy(
                customDietaryRestrictions = customDietaryRestrictions + input,
                customRestrictionInput = "",
            )
        }
    }

    private fun continueWizard() {
        val state = mutableState.value
        val validation = validate(state)
        if (validation is ValidationResult.Invalid) {
            mutableState.update { it.copy(validationError = validation.error) }
            return
        }
        when (state.currentStep) {
            OnboardingStep.MealsPerDay -> {
                if (state.mode == OnboardingMode.Edit) {
                    editModeFinish()
                } else {
                    calculatePlan()
                }
            }
            OnboardingStep.Summary -> {
                if (state.mode == OnboardingMode.Edit) {
                    editModeSave()
                } else {
                    completeOnboarding()
                }
            }
            OnboardingStep.Calculating -> Unit
            else -> {
                mutableState.update { it.copy(currentStep = it.currentStep.next() ?: it.currentStep, validationError = null) }
                if (state.mode == OnboardingMode.Initial) persist()
            }
        }
    }

    private fun calculatePlan() {
        calculationJob?.cancel()
        val answers = mutableState.value.toAnswers() ?: return
        mutableState.update { it.copy(currentStep = OnboardingStep.Calculating, validationError = null) }
        persist()
        calculationJob = viewModelScope.launch {
            val plan = calculator.calculate(answers)
            if (transitionMillis > 0) delay(transitionMillis)
            mutableState.update { it.copy(currentStep = OnboardingStep.CalorieReveal, plan = plan) }
            persist()
        }
    }

    private fun completeOnboarding() {
        mutableState.update { it.copy(shouldNavigateHome = true) }
        viewModelScope.launch { repository.save(mutableState.value.toSaved(isCompleted = true)) }
    }

    private fun editModeFinish() {
        val state = mutableState.value
        val orig = original
        if (orig == null) {
            editModeSave()
            return
        }
        val draft = state.toSaved()
        val planImpact = orig.hasNutritionPlanImpact(draft)
        if (planImpact) {
            calculatePlanForEdit()
        } else {
            editModeSave()
        }
    }

    private fun calculatePlanForEdit() {
        calculationJob?.cancel()
        val answers = mutableState.value.toAnswers() ?: return
        mutableState.update { it.copy(currentStep = OnboardingStep.Calculating, validationError = null) }
        calculationJob = viewModelScope.launch {
            val plan = calculator.calculate(answers)
            mutableState.update { it.copy(plan = plan) }
            editModeSave()
        }
    }

    private fun editModeSave() {
        val state = mutableState.value
        val orig = original
        val draft = state.toSaved()
        val finalDraft = if (orig != null && draft.plan == null) {
            draft.copy(plan = orig.plan, isCompleted = true, currentStep = orig.currentStep)
        } else {
            draft.copy(isCompleted = true, currentStep = orig?.currentStep ?: OnboardingStep.Summary)
        }
        viewModelScope.launch {
            repository.save(finalDraft)
            mutableState.update { it.copy(shouldNavigateBack = true) }
            onComplete()
        }
    }

    private fun goBack() {
        val state = mutableState.value
        if (state.currentStep == OnboardingStep.Gender) {
            if (state.mode == OnboardingMode.Edit) {
                mutableState.update { it.copy(shouldNavigateBack = true) }
                onCancel()
            } else {
                mutableState.update { it.copy(shouldExit = true) }
            }
            return
        }
        calculationJob?.cancel()
        val destination = when (state.currentStep) {
            OnboardingStep.Calculating -> OnboardingStep.MealsPerDay
            OnboardingStep.CalorieReveal -> OnboardingStep.MealsPerDay
            else -> state.currentStep.previous() ?: OnboardingStep.Gender
        }
        mutableState.update { it.copy(currentStep = destination, validationError = null) }
        if (state.mode == OnboardingMode.Initial) persist()
    }

    private fun validate(state: OnboardingUiState): ValidationResult = when (state.currentStep) {
        OnboardingStep.Gender -> required(state.gender)
        OnboardingStep.Age -> OnboardingValidator.age(state.ageInput)
        OnboardingStep.Height -> OnboardingValidator.heightCm(state.heightInput)
        OnboardingStep.CurrentWeight -> OnboardingValidator.weightKg(state.weightInput)
        OnboardingStep.TrainingDays -> OnboardingValidator.trainingDays(state.trainingDaysPerWeek)
        OnboardingStep.TrainingType -> required(state.trainingType)
        OnboardingStep.WorkActivity -> required(state.workActivity)
        OnboardingStep.Goal -> required(state.goal)
        OnboardingStep.Experience -> required(state.experience)
        OnboardingStep.FoodPreference -> required(state.foodPreference)
        OnboardingStep.MealsPerDay -> OnboardingValidator.meals(state.mealsPerDay)
        OnboardingStep.Calculating -> ValidationResult.Invalid(OnboardingValidationError.SelectionRequired)
        OnboardingStep.CalorieReveal, OnboardingStep.MacroReveal, OnboardingStep.Summary -> ValidationResult.Valid
    }

    private fun required(value: Any?): ValidationResult = if (value == null) {
        ValidationResult.Invalid(OnboardingValidationError.SelectionRequired)
    } else ValidationResult.Valid

    private fun persist() {
        val snapshot = mutableState.value.toSaved()
        viewModelScope.launch { repository.save(snapshot) }
    }

    private fun OnboardingUiState.toAnswers(): OnboardingAnswers? = OnboardingAnswers(
        gender = gender ?: return null,
        ageYears = ageInput.toIntOrNull() ?: return null,
        heightCm = heightInput.toDoubleOrNull() ?: return null,
        weightKg = weightInput.toDoubleOrNull() ?: return null,
        trainingDaysPerWeek = trainingDaysPerWeek ?: return null,
        trainingType = trainingType ?: return null,
        workActivity = workActivity ?: return null,
        goal = goal ?: return null,
        experience = experience ?: return null,
        foodPreference = foodPreference ?: return null,
        mealsPerDay = mealsPerDay ?: return null,
        customDietaryRestrictions = customDietaryRestrictions,
    )

    private fun OnboardingUiState.toSaved(isCompleted: Boolean = false) = SavedOnboarding(
        currentStep = currentStep, gender = gender, ageYears = ageInput.toIntOrNull(),
        heightCm = heightInput.toDoubleOrNull(), weightKg = weightInput.toDoubleOrNull(),
        trainingDaysPerWeek = trainingDaysPerWeek, trainingType = trainingType,
        workActivity = workActivity, goal = goal, experience = experience,
        foodPreference = foodPreference, customDietaryRestrictions = customDietaryRestrictions,
        mealsPerDay = mealsPerDay, plan = plan,
        isCompleted = isCompleted,
    )

    private fun SavedOnboarding.toUiState(): OnboardingUiState {
        val restoredStep = when {
            isCompleted -> OnboardingStep.Summary
            currentStep == OnboardingStep.Calculating && plan == null -> OnboardingStep.MealsPerDay
            currentStep == OnboardingStep.Calculating -> OnboardingStep.CalorieReveal
            else -> currentStep
        }
        return OnboardingUiState(
            currentStep = restoredStep, gender = gender, ageInput = (ageYears ?: OnboardingLimits.DefaultAge).toString(),
            heightInput = (heightCm ?: OnboardingLimits.DefaultHeightCm).display(), weightInput = weightKg?.display().orEmpty(),
            trainingDaysPerWeek = trainingDaysPerWeek, trainingType = trainingType,
            workActivity = workActivity, goal = goal, experience = experience,
            foodPreference = foodPreference, customDietaryRestrictions = customDietaryRestrictions,
            mealsPerDay = mealsPerDay,
            isCustomMeals = mealsPerDay != null && mealsPerDay !in 2..5,
            plan = plan, isRestoring = false, shouldNavigateHome = isCompleted,
        )
    }

    private fun SavedOnboarding.toUiStateForEdit(): OnboardingUiState = OnboardingUiState(
        currentStep = OnboardingStep.Gender, gender = gender,
        ageInput = (ageYears ?: OnboardingLimits.DefaultAge).toString(),
        heightInput = (heightCm ?: OnboardingLimits.DefaultHeightCm).display(),
        weightInput = weightKg?.display().orEmpty(),
        trainingDaysPerWeek = trainingDaysPerWeek, trainingType = trainingType,
        workActivity = workActivity, goal = goal, experience = experience,
        foodPreference = foodPreference, customDietaryRestrictions = customDietaryRestrictions,
        mealsPerDay = mealsPerDay,
        isCustomMeals = mealsPerDay != null && mealsPerDay !in 2..5,
        plan = plan, isRestoring = false, mode = OnboardingMode.Edit,
        originalOnboarding = this,
    )

    companion object {
        fun factory(
            repository: OnboardingRepository,
            mode: OnboardingMode = OnboardingMode.Initial,
            onComplete: () -> Unit = {},
            onCancel: () -> Unit = {},
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    OnboardingViewModel(repository, mode = mode, onComplete = onComplete, onCancel = onCancel) as T
            }
    }

}

private fun String.digitsOnly(): String = filter(Char::isDigit).take(3)
private fun String.decimalOnly(): String {
    val normalized = replace(',', '.')
    var separatorSeen = false
    return normalized.filter { character ->
        character.isDigit() || (character == '.' && !separatorSeen).also { if (it) separatorSeen = true }
    }.take(6)
}
private fun Double.display(): String = if (this % 1.0 == 0.0) toInt().toString() else toString()
