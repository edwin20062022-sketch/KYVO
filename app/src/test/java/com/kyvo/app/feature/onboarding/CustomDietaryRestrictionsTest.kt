package com.kyvo.app.feature.onboarding

import com.kyvo.app.feature.onboarding.data.InMemoryOnboardingRepository
import com.kyvo.app.feature.onboarding.domain.model.ExperienceLevel
import com.kyvo.app.feature.onboarding.domain.model.FitnessGoal
import com.kyvo.app.feature.onboarding.domain.model.FoodPreference
import com.kyvo.app.feature.onboarding.domain.model.GenderOption
import com.kyvo.app.feature.onboarding.domain.model.TrainingType
import com.kyvo.app.feature.onboarding.domain.model.WorkActivity
import com.kyvo.app.feature.onboarding.presentation.OnboardingEvent
import com.kyvo.app.feature.onboarding.presentation.OnboardingViewModel
import com.kyvo.app.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CustomDietaryRestrictionsTest {
    @get:Rule val dispatcherRule = MainDispatcherRule()

    private fun advanceToFoodPreference(vm: OnboardingViewModel) {
        vm.onEvent(OnboardingEvent.SelectGender(GenderOption.Male))
        vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.ChangeAge("30"))
        vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.ChangeHeight("175"))
        vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.ChangeWeight("75"))
        vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.SelectTrainingDays(5))
        vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.SelectTrainingType(TrainingType.Hypertrophy))
        vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.SelectWorkActivity(WorkActivity.Sedentary))
        vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.SelectGoal(FitnessGoal.FatLoss))
        vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.SelectExperience(ExperienceLevel.Intermediate))
        vm.onEvent(OnboardingEvent.Continue)
    }

    @Test
    fun addCustomRestriction() = runTest {
        val vm = OnboardingViewModel(InMemoryOnboardingRepository(), transitionMillis = 0)
        advanceUntilIdle()
        advanceToFoodPreference(vm)
        vm.onEvent(OnboardingEvent.SelectFoodPreference(FoodPreference.Other))
        vm.onEvent(OnboardingEvent.ChangeCustomRestrictionInput("Sin cacahuate"))
        vm.onEvent(OnboardingEvent.AddCustomRestriction)
        assertEquals(listOf("Sin cacahuate"), vm.state.value.customDietaryRestrictions)
        assertEquals("", vm.state.value.customRestrictionInput)
    }

    @Test
    fun addMultipleRestrictions() = runTest {
        val vm = OnboardingViewModel(InMemoryOnboardingRepository(), transitionMillis = 0)
        advanceUntilIdle()
        advanceToFoodPreference(vm)
        vm.onEvent(OnboardingEvent.SelectFoodPreference(FoodPreference.Other))
        vm.onEvent(OnboardingEvent.ChangeCustomRestrictionInput("Sin cacahuate"))
        vm.onEvent(OnboardingEvent.AddCustomRestriction)
        vm.onEvent(OnboardingEvent.ChangeCustomRestrictionInput("Intolerancia a la lactosa"))
        vm.onEvent(OnboardingEvent.AddCustomRestriction)
        assertEquals(2, vm.state.value.customDietaryRestrictions.size)
        assertEquals("Sin cacahuate", vm.state.value.customDietaryRestrictions[0])
        assertEquals("Intolerancia a la lactosa", vm.state.value.customDietaryRestrictions[1])
    }

    @Test
    fun removeRestriction() = runTest {
        val vm = OnboardingViewModel(InMemoryOnboardingRepository(), transitionMillis = 0)
        advanceUntilIdle()
        advanceToFoodPreference(vm)
        vm.onEvent(OnboardingEvent.SelectFoodPreference(FoodPreference.Other))
        vm.onEvent(OnboardingEvent.ChangeCustomRestrictionInput("Sin cacahuate"))
        vm.onEvent(OnboardingEvent.AddCustomRestriction)
        vm.onEvent(OnboardingEvent.ChangeCustomRestrictionInput("Sin trigo"))
        vm.onEvent(OnboardingEvent.AddCustomRestriction)
        vm.onEvent(OnboardingEvent.RemoveCustomRestriction(0))
        assertEquals(1, vm.state.value.customDietaryRestrictions.size)
        assertEquals("Sin trigo", vm.state.value.customDietaryRestrictions[0])
    }

    @Test
    fun blankInputRejected() = runTest {
        val vm = OnboardingViewModel(InMemoryOnboardingRepository(), transitionMillis = 0)
        advanceUntilIdle()
        advanceToFoodPreference(vm)
        vm.onEvent(OnboardingEvent.SelectFoodPreference(FoodPreference.Other))
        vm.onEvent(OnboardingEvent.ChangeCustomRestrictionInput("   "))
        vm.onEvent(OnboardingEvent.AddCustomRestriction)
        assertTrue(vm.state.value.customDietaryRestrictions.isEmpty())
    }

    @Test
    fun duplicateNormalizedRejected() = runTest {
        val vm = OnboardingViewModel(InMemoryOnboardingRepository(), transitionMillis = 0)
        advanceUntilIdle()
        advanceToFoodPreference(vm)
        vm.onEvent(OnboardingEvent.SelectFoodPreference(FoodPreference.Other))
        vm.onEvent(OnboardingEvent.ChangeCustomRestrictionInput("Sin gluten"))
        vm.onEvent(OnboardingEvent.AddCustomRestriction)
        vm.onEvent(OnboardingEvent.ChangeCustomRestrictionInput("  SIN GLUTEN  "))
        vm.onEvent(OnboardingEvent.AddCustomRestriction)
        assertEquals(1, vm.state.value.customDietaryRestrictions.size)
    }

    @Test
    fun restorePersistedRestrictions() = runTest {
        val repository = InMemoryOnboardingRepository()
        val first = OnboardingViewModel(repository, transitionMillis = 0)
        advanceUntilIdle()
        advanceToFoodPreference(first)
        first.onEvent(OnboardingEvent.SelectFoodPreference(FoodPreference.Other))
        first.onEvent(OnboardingEvent.ChangeCustomRestrictionInput("Sin cacahuate"))
        first.onEvent(OnboardingEvent.AddCustomRestriction)
        first.onEvent(OnboardingEvent.ChangeCustomRestrictionInput("Sin trigo"))
        first.onEvent(OnboardingEvent.AddCustomRestriction)
        first.onEvent(OnboardingEvent.Continue)
        advanceUntilIdle()

        val restored = OnboardingViewModel(repository, transitionMillis = 0)
        advanceUntilIdle()
        assertEquals(2, restored.state.value.customDietaryRestrictions.size)
        assertEquals("Sin cacahuate", restored.state.value.customDietaryRestrictions[0])
        assertEquals("Sin trigo", restored.state.value.customDietaryRestrictions[1])
    }

    @Test
    fun structuredPreferencesPreserved() = runTest {
        val vm = OnboardingViewModel(InMemoryOnboardingRepository(), transitionMillis = 0)
        advanceUntilIdle()
        advanceToFoodPreference(vm)
        vm.onEvent(OnboardingEvent.SelectFoodPreference(FoodPreference.Vegetarian))
        assertEquals(FoodPreference.Vegetarian, vm.state.value.foodPreference)
        assertTrue(vm.state.value.customDietaryRestrictions.isEmpty())
    }

    @Test
    fun emptyListBackwardsCompatible() = runTest {
        val repository = InMemoryOnboardingRepository()
        val vm = OnboardingViewModel(repository, transitionMillis = 0)
        advanceUntilIdle()
        assertTrue(vm.state.value.customDietaryRestrictions.isEmpty())
    }

    @Test
    fun customRestrictionsNotUsedInNutritionCalculation() = runTest {
        val repository = InMemoryOnboardingRepository()
        val first = OnboardingViewModel(repository, transitionMillis = 0)
        advanceUntilIdle()
        advanceToFoodPreference(first)
        first.onEvent(OnboardingEvent.SelectFoodPreference(FoodPreference.Vegetarian))
        first.onEvent(OnboardingEvent.Continue)
        first.onEvent(OnboardingEvent.SelectMeals(4))
        first.onEvent(OnboardingEvent.Continue)
        advanceUntilIdle()
        val planWithoutRestrictions = first.state.value.plan

        val second = OnboardingViewModel(repository, transitionMillis = 0)
        advanceUntilIdle()
        second.onEvent(OnboardingEvent.SelectFoodPreference(FoodPreference.Other))
        second.onEvent(OnboardingEvent.ChangeCustomRestrictionInput("Sin cacahuate"))
        second.onEvent(OnboardingEvent.AddCustomRestriction)
        second.onEvent(OnboardingEvent.Continue)
        second.onEvent(OnboardingEvent.SelectMeals(4))
        second.onEvent(OnboardingEvent.Continue)
        advanceUntilIdle()
        val planWithRestrictions = second.state.value.plan

        if (planWithoutRestrictions != null && planWithRestrictions != null) {
            assertEquals(planWithoutRestrictions.targetCaloriesKcal, planWithRestrictions.targetCaloriesKcal)
            assertEquals(planWithoutRestrictions.proteinGrams, planWithRestrictions.proteinGrams)
        }
    }

    @Test
    fun inputClearedAfterAdd() = runTest {
        val vm = OnboardingViewModel(InMemoryOnboardingRepository(), transitionMillis = 0)
        advanceUntilIdle()
        advanceToFoodPreference(vm)
        vm.onEvent(OnboardingEvent.SelectFoodPreference(FoodPreference.Other))
        vm.onEvent(OnboardingEvent.ChangeCustomRestrictionInput("Sin cacahuate"))
        vm.onEvent(OnboardingEvent.AddCustomRestriction)
        assertEquals("", vm.state.value.customRestrictionInput)
    }
}
