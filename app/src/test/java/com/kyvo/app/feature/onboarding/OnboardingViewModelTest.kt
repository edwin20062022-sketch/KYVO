package com.kyvo.app.feature.onboarding.presentation

import com.kyvo.app.feature.onboarding.data.InMemoryOnboardingRepository
import com.kyvo.app.feature.onboarding.domain.model.ExperienceLevel
import com.kyvo.app.feature.onboarding.domain.model.FitnessGoal
import com.kyvo.app.feature.onboarding.domain.model.FoodPreference
import com.kyvo.app.feature.onboarding.domain.model.GenderOption
import com.kyvo.app.feature.onboarding.domain.model.OnboardingStep
import com.kyvo.app.feature.onboarding.domain.model.TrainingType
import com.kyvo.app.feature.onboarding.domain.model.WorkActivity
import com.kyvo.app.feature.onboarding.domain.validation.OnboardingValidationError
import com.kyvo.app.test.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
        vm.onEvent(OnboardingEvent.ChangeAge("12"))
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
}
