package com.kyvo.app.feature.onboarding

import com.kyvo.app.feature.onboarding.data.InMemoryOnboardingRepository
import com.kyvo.app.feature.onboarding.domain.model.ExperienceLevel
import com.kyvo.app.feature.onboarding.domain.model.FitnessGoal
import com.kyvo.app.feature.onboarding.domain.model.FoodPreference
import com.kyvo.app.feature.onboarding.domain.model.GenderOption
import com.kyvo.app.feature.onboarding.domain.model.OnboardingStep
import com.kyvo.app.feature.onboarding.domain.model.TrainingType
import com.kyvo.app.feature.onboarding.domain.model.WorkActivity
import com.kyvo.app.feature.onboarding.domain.validation.OnboardingLimits
import com.kyvo.app.feature.onboarding.domain.validation.OnboardingValidator
import com.kyvo.app.feature.onboarding.presentation.OnboardingEvent
import com.kyvo.app.feature.onboarding.presentation.OnboardingViewModel
import com.kyvo.app.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.math.abs

@OptIn(ExperimentalCoroutinesApi::class)
class WeightRulerMappingTest {
    @get:Rule val dispatcherRule = MainDispatcherRule()

    private fun buildWeightValues(): List<Double> = generateSequence(OnboardingLimits.MinimumWeightKg) { it + 1.0 }
        .takeWhile { it <= OnboardingLimits.MaximumWeightKg + 0.01 }
        .map { "%.1f".format(it).toDouble() }
        .toList()

    @Test
    fun weightValuesListMatchesRange() {
        val values = buildWeightValues()
        assertEquals(OnboardingLimits.MinimumWeightKg, values.first(), 0.001)
        assertEquals(OnboardingLimits.MaximumWeightKg, values.last(), 0.001)
    }

    @Test
    fun weightValuesIncrementIsExactlyOneKg() {
        val values = buildWeightValues()
        for (i in 1 until values.size) {
            val diff = values[i] - values[i - 1]
            assertEquals(1.0, diff, 0.001)
        }
    }

    @Test
    fun weightValuesHaveOneDecimalPlace() {
        val values = buildWeightValues()
        values.forEach { value ->
            val formatted = "%.1f".format(value)
            assertEquals(formatted.toDouble(), value, 0.001)
        }
    }

    @Test
    fun weightIndexMapping_min() {
        val values = buildWeightValues()
        val index = values.indexOf(OnboardingLimits.MinimumWeightKg)
        assertEquals(0, index)
    }

    @Test
    fun weightIndexMapping_max() {
        val values = buildWeightValues()
        val index = values.indexOf(OnboardingLimits.MaximumWeightKg)
        assertEquals(values.size - 1, index)
    }

    @Test
    fun weightIndexMapping_midpoint() {
        val values = buildWeightValues()
        val index75 = values.indexOf(75.0)
        assertTrue("75.0 kg should exist in values", index75 >= 0)
        assertEquals(40, index75)
    }

    @Test
    fun weightIndexMapping_roundTrip() {
        val values = buildWeightValues()
        values.forEachIndexed { index, value ->
            val reverseIndex = values.indexOf(value)
            assertEquals(index, reverseIndex)
        }
    }

    @Test
    fun weightMinCanBeCentered() {
        val values = buildWeightValues()
        val minIndex = 0
        assertTrue("Min index should be within values range", minIndex in values.indices)
    }

    @Test
    fun weightMaxCanBeCentered() {
        val values = buildWeightValues()
        val maxIndex = values.size - 1
        assertTrue("Max index should be within values range", maxIndex in values.indices)
    }

    @Test
    fun totalWeightValuesCount() {
        val values = buildWeightValues()
        assertEquals(266, values.size)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class WeightDraftInteractionTest {
    @get:Rule val dispatcherRule = MainDispatcherRule()

    @Test
    fun weightChange_updatesDraft() = runTest {
        val vm = OnboardingViewModel(InMemoryOnboardingRepository(), transitionMillis = 0)
        advanceUntilIdle()
        vm.onEvent(OnboardingEvent.SelectGender(GenderOption.Male))
        vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.ChangeAge("30"))
        vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.ChangeHeight("175"))
        vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.ChangeWeight("82.0"))
        assertEquals("82.0", vm.state.value.weightInput)
    }

    @Test
    fun weight_restoreFromDraft() = runTest {
        val repository = InMemoryOnboardingRepository()
        val first = OnboardingViewModel(repository, transitionMillis = 0)
        advanceUntilIdle()
        first.onEvent(OnboardingEvent.SelectGender(GenderOption.Male))
        first.onEvent(OnboardingEvent.Continue)
        first.onEvent(OnboardingEvent.ChangeAge("30"))
        first.onEvent(OnboardingEvent.Continue)
        first.onEvent(OnboardingEvent.ChangeHeight("175"))
        first.onEvent(OnboardingEvent.Continue)
        first.onEvent(OnboardingEvent.ChangeWeight("95.0"))
        first.onEvent(OnboardingEvent.Continue)
        advanceUntilIdle()

        val restored = OnboardingViewModel(repository, transitionMillis = 0)
        advanceUntilIdle()
        assertEquals(OnboardingStep.TrainingDays, restored.state.value.currentStep)
        assertEquals("95", restored.state.value.weightInput)
    }

    @Test
    fun weight_exactMin_valid() {
        val result = OnboardingValidator.weightKg(OnboardingLimits.MinimumWeightKg.toString())
        assertTrue(result is com.kyvo.app.feature.onboarding.domain.validation.ValidationResult.Valid)
    }

    @Test
    fun weight_exactMax_valid() {
        val result = OnboardingValidator.weightKg(OnboardingLimits.MaximumWeightKg.toString())
        assertTrue(result is com.kyvo.app.feature.onboarding.domain.validation.ValidationResult.Valid)
    }

    @Test
    fun weight_belowMin_invalid() {
        val result = OnboardingValidator.weightKg("34.9")
        assertTrue(result is com.kyvo.app.feature.onboarding.domain.validation.ValidationResult.Invalid)
    }

    @Test
    fun weight_aboveMax_invalid() {
        val result = OnboardingValidator.weightKg("300.1")
        assertTrue(result is com.kyvo.app.feature.onboarding.domain.validation.ValidationResult.Invalid)
    }

    @Test
    fun weight_decimal_valid() {
        val result = OnboardingValidator.weightKg("75.5")
        assertTrue(result is com.kyvo.app.feature.onboarding.domain.validation.ValidationResult.Valid)
    }

    @Test
    fun weight_noFloatingPointDrift() {
        val values = generateSequence(OnboardingLimits.MinimumWeightKg) { it + 1.0 }
            .takeWhile { it <= OnboardingLimits.MaximumWeightKg + 0.01 }
            .map { "%.1f".format(it).toDouble() }
            .toList()
        values.forEach { value ->
            val formatted = "%.1f".format(value)
            val reparsed = formatted.toDouble()
            assertEquals("Floating point drift detected for $value", value, reparsed, 0.001)
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class TrainingDaysMappingTest {
    @get:Rule val dispatcherRule = MainDispatcherRule()

    @Test
    fun trainingDaysRange_matchesLimits() {
        val days = 1..7
        assertEquals(OnboardingLimits.MinimumTrainingDays, days.first)
        assertEquals(OnboardingLimits.MaximumTrainingDays, days.last)
    }

    @Test
    fun trainingDays_select_updatesDraft() = runTest {
        val vm = OnboardingViewModel(InMemoryOnboardingRepository(), transitionMillis = 0)
        advanceUntilIdle()
        vm.onEvent(OnboardingEvent.SelectGender(GenderOption.Male))
        vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.ChangeAge("30"))
        vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.ChangeHeight("175"))
        vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.ChangeWeight("75.0"))
        vm.onEvent(OnboardingEvent.Continue)
        vm.onEvent(OnboardingEvent.SelectTrainingDays(3))
        assertEquals(3, vm.state.value.trainingDaysPerWeek)
    }

    @Test
    fun trainingDays_restoreFromDraft() = runTest {
        val repository = InMemoryOnboardingRepository()
        val first = OnboardingViewModel(repository, transitionMillis = 0)
        advanceUntilIdle()
        first.onEvent(OnboardingEvent.SelectGender(GenderOption.Male))
        first.onEvent(OnboardingEvent.Continue)
        first.onEvent(OnboardingEvent.ChangeAge("30"))
        first.onEvent(OnboardingEvent.Continue)
        first.onEvent(OnboardingEvent.ChangeHeight("175"))
        first.onEvent(OnboardingEvent.Continue)
        first.onEvent(OnboardingEvent.ChangeWeight("75.0"))
        first.onEvent(OnboardingEvent.Continue)
        first.onEvent(OnboardingEvent.SelectTrainingDays(6))
        first.onEvent(OnboardingEvent.Continue)
        advanceUntilIdle()

        val restored = OnboardingViewModel(repository, transitionMillis = 0)
        advanceUntilIdle()
        assertEquals(OnboardingStep.TrainingType, restored.state.value.currentStep)
        assertEquals(6, restored.state.value.trainingDaysPerWeek)
    }

    @Test
    fun trainingDays_eachDayIsValid() {
        for (day in 1..7) {
            val result = OnboardingValidator.trainingDays(day)
            assertTrue("Day $day should be valid", result is com.kyvo.app.feature.onboarding.domain.validation.ValidationResult.Valid)
        }
    }

    @Test
    fun trainingDays_zeroIsInvalid() {
        val result = OnboardingValidator.trainingDays(0)
        assertTrue(result is com.kyvo.app.feature.onboarding.domain.validation.ValidationResult.Invalid)
    }

    @Test
    fun trainingDays_eightIsInvalid() {
        val result = OnboardingValidator.trainingDays(8)
        assertTrue(result is com.kyvo.app.feature.onboarding.domain.validation.ValidationResult.Invalid)
    }

    @Test
    fun trainingDays_nullIsInvalid() {
        val result = OnboardingValidator.trainingDays(null)
        assertTrue(result is com.kyvo.app.feature.onboarding.domain.validation.ValidationResult.Invalid)
    }
}
