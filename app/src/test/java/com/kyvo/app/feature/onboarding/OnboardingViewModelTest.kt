package com.kyvo.app.feature.onboarding.presentation

import com.kyvo.app.feature.onboarding.data.InMemoryOnboardingRepository
import com.kyvo.app.feature.onboarding.domain.model.ExperienceLevel
import com.kyvo.app.feature.onboarding.domain.model.FitnessGoal
import com.kyvo.app.feature.onboarding.domain.model.FoodPreference
import com.kyvo.app.feature.onboarding.domain.model.GenderOption
import com.kyvo.app.feature.onboarding.domain.model.NutritionPlan
import com.kyvo.app.feature.onboarding.domain.model.OnboardingMode
import com.kyvo.app.feature.onboarding.domain.model.OnboardingStep
import com.kyvo.app.feature.onboarding.domain.model.SavedOnboarding
import com.kyvo.app.feature.onboarding.domain.model.TrainingType
import com.kyvo.app.feature.onboarding.domain.model.WorkActivity
import com.kyvo.app.feature.onboarding.domain.validation.OnboardingValidationError
import com.kyvo.app.feature.onboarding.domain.validation.OnboardingLimits
import com.kyvo.app.test.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    @get:Rule val dispatcherRule = MainDispatcherRule()

    @Test fun startsAtGender() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        assertEquals(OnboardingStep.Gender, vm.state.value.currentStep)
        assertFalse(vm.state.value.isRestoring)
    }

    @Test fun missingPersonalDataUsesRequestedDefaults() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        assertEquals(OnboardingLimits.DefaultAge.toString(), vm.state.value.ageInput)
        assertEquals(OnboardingLimits.DefaultHeightCm.toInt().toString(), vm.state.value.heightInput)
    }

    @Test fun decorativeZeroAgeIsNotPersisted() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onEvent(OnboardingEvent.ChangeAge("0"))
        advanceUntilIdle()
        assertEquals("", vm.state.value.ageInput)
    }

    @Test fun cannotAdvanceWithoutRequiredSelection() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onEvent(OnboardingEvent.Continue)
        assertEquals(OnboardingStep.Gender, vm.state.value.currentStep)
        assertEquals(OnboardingValidationError.SelectionRequired, vm.state.value.validationError)
    }

    @Test fun advancesAndBackPreservesAnswer() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onEvent(OnboardingEvent.SelectGender(GenderOption.Female))
        vm.onEvent(OnboardingEvent.Continue)
        assertEquals(OnboardingStep.Age, vm.state.value.currentStep)
        vm.onEvent(OnboardingEvent.Back)
        assertEquals(OnboardingStep.Gender, vm.state.value.currentStep)
        assertEquals(GenderOption.Female, vm.state.value.gender)
    }

    @Test fun rejectsInvalidNumericInput() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onEvent(OnboardingEvent.SelectGender(GenderOption.Male))
        vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.ChangeAge("151"))
        vm.onEvent(OnboardingEvent.Continue)
        assertEquals(OnboardingStep.Age, vm.state.value.currentStep)
        assertEquals(OnboardingValidationError.AgeOutOfRange, vm.state.value.validationError)
    }

    @Test fun completesPreferencesCalculatesAndFinishes() = runTest {
        val repository = InMemoryOnboardingRepository()
        val vm = OnboardingViewModel(repository, transitionMillis = 0)
        advanceUntilIdle()
        completeQuestions(vm)
        advanceUntilIdle()
        assertEquals(OnboardingStep.CalorieReveal, vm.state.value.currentStep)
        assertNotNull(vm.state.value.plan)
        vm.onEvent(OnboardingEvent.Continue)
        assertEquals(OnboardingStep.MacroReveal, vm.state.value.currentStep)
        vm.onEvent(OnboardingEvent.Continue)
        assertEquals(OnboardingStep.Summary, vm.state.value.currentStep)
        vm.onEvent(OnboardingEvent.Continue)
        advanceUntilIdle()
        assertTrue(vm.state.value.shouldNavigateHome)
        assertTrue(repository.observe().first().isCompleted)
    }

    @Test fun restoredDraftResumesWithoutLosingData() = runTest {
        val repository = InMemoryOnboardingRepository()
        val first = OnboardingViewModel(repository, transitionMillis = 0)
        advanceUntilIdle()
        first.onEvent(OnboardingEvent.SelectGender(GenderOption.Male))
        first.onEvent(OnboardingEvent.Continue)
        first.onEvent(OnboardingEvent.ChangeAge("31"))
        first.onEvent(OnboardingEvent.Continue)
        advanceUntilIdle()
        val restored = OnboardingViewModel(repository, transitionMillis = 0)
        advanceUntilIdle()
        assertEquals(OnboardingStep.Height, restored.state.value.currentStep)
        assertEquals("31", restored.state.value.ageInput)
    }

    @Test fun backFromCalorieRevealSkipsTransientCalculationStep() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        completeQuestions(vm)
        advanceUntilIdle()
        vm.onEvent(OnboardingEvent.Back)
        assertEquals(OnboardingStep.MealsPerDay, vm.state.value.currentStep)
        assertNotNull(vm.state.value.plan)
    }

    private fun viewModel() = OnboardingViewModel(InMemoryOnboardingRepository(), transitionMillis = 0)

    private fun completeQuestions(vm: OnboardingViewModel) {
        vm.onEvent(OnboardingEvent.SelectGender(GenderOption.Male)); vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.ChangeAge("30")); vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.ChangeHeight("175")); vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.ChangeWeight("75")); vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.SelectTrainingDays(5)); vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.SelectTrainingType(TrainingType.Hypertrophy)); vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.SelectWorkActivity(WorkActivity.Sedentary)); vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.SelectGoal(FitnessGoal.FatLoss)); vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.SelectExperience(ExperienceLevel.Intermediate)); vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.SelectFoodPreference(FoodPreference.None)); vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.SelectMeals(4)); vm.onEvent(OnboardingEvent.Continue)
    }

    private fun completedOnboarding() = SavedOnboarding(
        gender = GenderOption.Male, ageYears = 30, heightCm = 175.0, weightKg = 75.0,
        trainingDaysPerWeek = 5, trainingType = TrainingType.Hypertrophy, workActivity = WorkActivity.Sedentary,
        goal = FitnessGoal.FatLoss, experience = ExperienceLevel.Intermediate, foodPreference = FoodPreference.None,
        mealsPerDay = 4, plan = NutritionPlan(1637.0, 2537.0, 1.55, -0.15, 2160, 150, 210, 65),
        isCompleted = true,
    )

    @Test fun editModePreloadsCurrentValues() = runTest {
        val repository = InMemoryOnboardingRepository()
        repository.save(completedOnboarding())
        val vm = OnboardingViewModel(repository, transitionMillis = 0, mode = OnboardingMode.Edit)
        advanceUntilIdle()
        assertEquals(OnboardingStep.Gender, vm.state.value.currentStep)
        assertEquals(GenderOption.Male, vm.state.value.gender)
        assertEquals("30", vm.state.value.ageInput)
        assertEquals("175", vm.state.value.heightInput)
        assertEquals("75", vm.state.value.weightInput)
        assertEquals(5, vm.state.value.trainingDaysPerWeek)
        assertEquals(TrainingType.Hypertrophy, vm.state.value.trainingType)
        assertEquals(WorkActivity.Sedentary, vm.state.value.workActivity)
        assertEquals(FitnessGoal.FatLoss, vm.state.value.goal)
        assertEquals(ExperienceLevel.Intermediate, vm.state.value.experience)
        assertEquals(FoodPreference.None, vm.state.value.foodPreference)
        assertEquals(4, vm.state.value.mealsPerDay)
        assertEquals(OnboardingMode.Edit, vm.state.value.mode)
    }

    @Test fun editModeDoesNotPersistIntermediateChanges() = runTest {
        val repository = InMemoryOnboardingRepository()
        repository.save(completedOnboarding())
        val vm = OnboardingViewModel(repository, transitionMillis = 0, mode = OnboardingMode.Edit)
        advanceUntilIdle()
        vm.onEvent(OnboardingEvent.SelectGender(GenderOption.Female))
        vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.ChangeAge("32"))
        vm.onEvent(OnboardingEvent.Continue)
        advanceUntilIdle()
        val saved = repository.observe().first()
        assertEquals(GenderOption.Male, saved.gender)
        assertEquals(30, saved.ageYears)
    }

    @Test fun editModeNonImpactChangePreservesPlan() = runTest {
        val repository = InMemoryOnboardingRepository()
        val original = completedOnboarding()
        repository.save(original)
        val vm = OnboardingViewModel(repository, transitionMillis = 0, mode = OnboardingMode.Edit)
        advanceUntilIdle()
        completeEditQuestions(vm)
        advanceUntilIdle()
        val saved = repository.observe().first()
        assertEquals(ExperienceLevel.Advanced, saved.experience)
        assertEquals(original.plan, saved.plan)
        assertTrue(saved.isCompleted)
    }

    @Test fun editModeImpactChangeRecalculatesPlan() = runTest {
        val repository = InMemoryOnboardingRepository()
        val original = completedOnboarding()
        repository.save(original)
        val vm = OnboardingViewModel(repository, transitionMillis = 0, mode = OnboardingMode.Edit)
        advanceUntilIdle()
        vm.onEvent(OnboardingEvent.SelectGender(GenderOption.Male)); vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.ChangeAge("30")); vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.ChangeHeight("175")); vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.ChangeWeight("70")); vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.SelectTrainingDays(4)); vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.SelectTrainingType(TrainingType.Hypertrophy)); vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.SelectWorkActivity(WorkActivity.Sedentary)); vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.SelectGoal(FitnessGoal.Maintenance)); vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.SelectExperience(ExperienceLevel.Intermediate)); vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.SelectFoodPreference(FoodPreference.None)); vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.SelectMeals(4)); vm.onEvent(OnboardingEvent.Continue)
        advanceUntilIdle()
        val saved = repository.observe().first()
        assertEquals(70.0, saved.weightKg ?: -1.0, 0.0)
        assertEquals(4, saved.trainingDaysPerWeek)
        assertEquals(FitnessGoal.Maintenance, saved.goal)
        assertNotNull(saved.plan)
        assertTrue(saved.plan != original.plan)
    }

    @Test fun editModeCancelDoesNotPersist() = runTest {
        val repository = InMemoryOnboardingRepository()
        val original = completedOnboarding()
        repository.save(original)
        val vm = OnboardingViewModel(repository, transitionMillis = 0, mode = OnboardingMode.Edit)
        advanceUntilIdle()
        vm.onEvent(OnboardingEvent.SelectGender(GenderOption.Female))
        vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.Back)
        assertEquals(OnboardingStep.Gender, vm.state.value.currentStep)
        assertEquals(GenderOption.Female, vm.state.value.gender)
        val saved = repository.observe().first()
        assertEquals(GenderOption.Male, saved.gender)
    }

    @Test fun editModeBackFromGenderCancels() = runTest {
        val repository = InMemoryOnboardingRepository()
        repository.save(completedOnboarding())
        val vm = OnboardingViewModel(repository, transitionMillis = 0, mode = OnboardingMode.Edit)
        advanceUntilIdle()
        vm.onEvent(OnboardingEvent.Back)
        assertTrue(vm.state.value.shouldNavigateBack)
    }

    @Test fun initialModeRegression() = runTest {
        val repository = InMemoryOnboardingRepository()
        val vm = OnboardingViewModel(repository, transitionMillis = 0, mode = OnboardingMode.Initial)
        advanceUntilIdle()
        assertEquals(OnboardingMode.Initial, vm.state.value.mode)
        assertNull(vm.state.value.originalOnboarding)
        assertEquals(OnboardingStep.Gender, vm.state.value.currentStep)
    }

    private fun completeEditQuestions(vm: OnboardingViewModel) {
        vm.onEvent(OnboardingEvent.SelectGender(GenderOption.Male)); vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.ChangeAge("30")); vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.ChangeHeight("175")); vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.ChangeWeight("75")); vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.SelectTrainingDays(5)); vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.SelectTrainingType(TrainingType.Hypertrophy)); vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.SelectWorkActivity(WorkActivity.Sedentary)); vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.SelectGoal(FitnessGoal.FatLoss)); vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.SelectExperience(ExperienceLevel.Advanced)); vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.SelectFoodPreference(FoodPreference.None)); vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.SelectMeals(4)); vm.onEvent(OnboardingEvent.Continue)
    }
}
