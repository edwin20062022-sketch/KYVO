package com.kyvo.app.feature.profile.presentation

import com.kyvo.app.feature.onboarding.domain.model.FitnessGoal
import com.kyvo.app.feature.onboarding.domain.model.GenderOption
import com.kyvo.app.feature.onboarding.domain.model.NutritionPlan
import com.kyvo.app.feature.onboarding.domain.model.SavedOnboarding
import com.kyvo.app.feature.onboarding.domain.model.TrainingType
import com.kyvo.app.feature.onboarding.domain.model.WorkActivity
import com.kyvo.app.feature.onboarding.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import com.kyvo.app.test.MainDispatcherRule

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class NutritionPlanViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun completedOnboardingMapsThePersistedPlanWithoutRecalculation() = runTest(mainDispatcherRule.testDispatcher) {
        val saved = completedOnboarding()
        val viewModel = NutritionPlanViewModel(FakeOnboardingRepository(saved))
        advanceUntilIdle()

        val state = viewModel.state.value as NutritionPlanUiState.Content
        assertEquals(2300, state.onboarding.plan?.targetCaloriesKcal)
        assertEquals(160, state.onboarding.plan?.proteinGrams)
        assertEquals(225, state.onboarding.plan?.carbohydrateGrams)
        assertEquals(70, state.onboarding.plan?.fatGrams)
    }

    @Test
    fun missingPlanIsIncompleteAndNeverFakeZeroes() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = NutritionPlanViewModel(FakeOnboardingRepository(SavedOnboarding(isCompleted = true)))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is NutritionPlanUiState.Incomplete)
        assertTrue((state as NutritionPlanUiState.Incomplete).onboarding.plan == null)
    }

    @Test
    fun observationDoesNotWriteToRepository() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeOnboardingRepository(completedOnboarding())
        NutritionPlanViewModel(repository)
        advanceUntilIdle()
        assertEquals(0, repository.writeCount)
    }

    private fun completedOnboarding() = SavedOnboarding(
        gender = GenderOption.Male,
        ageYears = 28,
        heightCm = 180.0,
        weightKg = 75.0,
        trainingDaysPerWeek = 5,
        trainingType = TrainingType.Hypertrophy,
        workActivity = WorkActivity.Sedentary,
        goal = FitnessGoal.Recomposition,
        mealsPerDay = 4,
        plan = NutritionPlan(1637.0, 2537.0, 1.55, -0.05, 2300, 160, 225, 70),
        isCompleted = true,
    )

    private class FakeOnboardingRepository(private val saved: SavedOnboarding) : OnboardingRepository {
        var writeCount = 0
        override fun observe(): Flow<SavedOnboarding> = flowOf(saved)
        override suspend fun save(progress: SavedOnboarding) { writeCount++ }
    }
}
