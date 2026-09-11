package com.kyvo.app.feature.profile.presentation

import com.kyvo.app.feature.onboarding.domain.model.ExperienceLevel
import com.kyvo.app.feature.onboarding.domain.model.FitnessGoal
import com.kyvo.app.feature.onboarding.domain.model.FoodPreference
import com.kyvo.app.feature.onboarding.domain.model.GenderOption
import com.kyvo.app.feature.onboarding.domain.model.NutritionPlan
import com.kyvo.app.feature.onboarding.domain.model.SavedOnboarding
import com.kyvo.app.feature.onboarding.domain.model.TrainingType
import com.kyvo.app.feature.onboarding.domain.model.WorkActivity
import com.kyvo.app.feature.onboarding.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import com.kyvo.app.test.MainDispatcherRule

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PersonalPreferencesViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialDraftMapsSavedOnboarding() = runTest(mainDispatcherRule.testDispatcher) {
        val saved = sample()
        val viewModel = PersonalPreferencesViewModel(FakeRepository(saved))
        advanceUntilIdle()
        val state = viewModel.state.value as PersonalPreferencesUiState.Editing
        assertEquals(saved, state.draft)
        assertEquals(GenderOption.Male, state.draft.gender)
        assertEquals(75.0, state.draft.weightKg ?: -1.0, 0.0)
    }

    @Test
    fun personalAndActivityChangesStayInDraftAndDoNotWrite() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeRepository(sample())
        val viewModel = PersonalPreferencesViewModel(repository)
        advanceUntilIdle()
        viewModel.updateAge(30)
        viewModel.updateWeightKg(72.0)
        viewModel.updateTrainingDays(4)
        viewModel.updateWorkActivity(WorkActivity.Active)

        val state = viewModel.state.value as PersonalPreferencesUiState.Editing
        assertEquals(30, state.draft.ageYears)
        assertEquals(72.0, state.draft.weightKg ?: -1.0, 0.0)
        assertEquals(4, state.draft.trainingDaysPerWeek)
        assertEquals(WorkActivity.Active, state.draft.workActivity)
        assertEquals(sample(), state.current)
        assertEquals(0, repository.writeCount)
    }

    @Test
    fun draftPreservesGoalPreferencesMealsAndPlan() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = PersonalPreferencesViewModel(FakeRepository(sample()))
        advanceUntilIdle()
        viewModel.updateHeightCm(182.0)
        val draft = (viewModel.state.value as PersonalPreferencesUiState.Editing).draft
        assertEquals(FitnessGoal.Recomposition, draft.goal)
        assertEquals(ExperienceLevel.Intermediate, draft.experience)
        assertEquals(FoodPreference.None, draft.foodPreference)
        assertEquals(4, draft.mealsPerDay)
        assertEquals(2300, draft.plan?.targetCaloriesKcal)
        assertTrue(draft.isCompleted)
    }

    private fun sample() = SavedOnboarding(
        gender = GenderOption.Male, ageYears = 28, heightCm = 180.0, weightKg = 75.0,
        trainingDaysPerWeek = 5, trainingType = TrainingType.Hypertrophy, workActivity = WorkActivity.Sedentary,
        goal = FitnessGoal.Recomposition, experience = ExperienceLevel.Intermediate, foodPreference = FoodPreference.None,
        mealsPerDay = 4, plan = NutritionPlan(1637.0, 2537.0, 1.55, -0.05, 2300, 160, 225, 70), isCompleted = true,
    )

    private class FakeRepository(initial: SavedOnboarding) : OnboardingRepository {
        val current = MutableStateFlow(initial)
        var writeCount = 0
        override fun observe(): Flow<SavedOnboarding> = current
        override suspend fun save(progress: SavedOnboarding) { writeCount++; current.value = progress }
    }
}
