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

    @Test
    fun nineBChangesUpdateOnlyDraftAndPreserveCurrentAndPlan() = runTest(mainDispatcherRule.testDispatcher) {
        val saved = sample()
        val repository = FakeRepository(saved)
        val viewModel = PersonalPreferencesViewModel(repository)
        advanceUntilIdle()

        viewModel.updateExperience(ExperienceLevel.Advanced)
        viewModel.updateFoodPreference(FoodPreference.Vegan)
        viewModel.updateMealsPerDay(6)
        val editing = viewModel.state.value as PersonalPreferencesUiState.Editing

        assertEquals(ExperienceLevel.Advanced, editing.draft.experience)
        assertEquals(FoodPreference.Vegan, editing.draft.foodPreference)
        assertEquals(6, editing.draft.mealsPerDay)
        assertEquals(saved, editing.current)
        assertEquals(saved.plan, editing.draft.plan)
        assertEquals(0, repository.writeCount)
    }

    @Test
    fun nineBChangesPreservePersonalAndActivityDraftFields() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeRepository(sample())
        val viewModel = PersonalPreferencesViewModel(repository)
        advanceUntilIdle()
        viewModel.updateAge(31)
        viewModel.updateTrainingDays(6)
        viewModel.updateExperience(ExperienceLevel.Beginner)
        viewModel.updateFoodPreference(FoodPreference.GlutenFree)
        viewModel.updateMealsPerDay(3)

        val draft = (viewModel.state.value as PersonalPreferencesUiState.Editing).draft
        assertEquals(31, draft.ageYears)
        assertEquals(6, draft.trainingDaysPerWeek)
        assertEquals(ExperienceLevel.Beginner, draft.experience)
        assertEquals(FoodPreference.GlutenFree, draft.foodPreference)
        assertEquals(3, draft.mealsPerDay)
        assertEquals(0, repository.writeCount)
    }

    @Test
    fun noChangesDoNotWriteAndNonPlanChangesPreservePlan() = runTest(mainDispatcherRule.testDispatcher) {
        val saved = sample()
        val repository = FakeRepository(saved)
        val viewModel = PersonalPreferencesViewModel(repository)
        advanceUntilIdle()
        assertTrue(viewModel.prepareReview())
        assertTrue((viewModel.state.value as PersonalPreferencesUiState.Review).hasChanges.not())
        viewModel.confirmUpdate()
        advanceUntilIdle()
        assertEquals(0, repository.writeCount)

        val secondRepository = FakeRepository(saved)
        val secondViewModel = PersonalPreferencesViewModel(secondRepository)
        advanceUntilIdle()
        secondViewModel.updateFoodPreference(FoodPreference.Vegan)
        assertTrue(secondViewModel.prepareReview())
        val review = secondViewModel.state.value as PersonalPreferencesUiState.Review
        assertTrue(review.hasChanges)
        assertTrue(!review.planImpact)
        assertEquals(saved.plan, review.previewPlan)
        secondViewModel.confirmUpdate()
        advanceUntilIdle()
        assertEquals(saved.plan, secondRepository.current.value.plan)
        assertEquals(1, secondRepository.writeCount)
    }

    @Test
    fun planInputsProduceCalculatorPreviewAndPersistItAtomically() = runTest(mainDispatcherRule.testDispatcher) {
        val saved = sample()
        val repository = FakeRepository(saved)
        val viewModel = PersonalPreferencesViewModel(repository)
        advanceUntilIdle()
        viewModel.updateWeightKg(68.0)
        viewModel.updateTrainingDays(4)
        assertTrue(viewModel.prepareReview())
        val review = viewModel.state.value as PersonalPreferencesUiState.Review
        assertTrue(review.planImpact)
        assertEquals(68.0, review.draft.weightKg ?: -1.0, 0.0)
        assertTrue(review.previewPlan != review.oldPlan)
        viewModel.confirmUpdate()
        viewModel.confirmUpdate()
        advanceUntilIdle()
        assertEquals(1, repository.writeCount)
        assertEquals(review.previewPlan, repository.current.value.plan)
        assertEquals(review.draft.copy(plan = review.previewPlan, isCompleted = true, currentStep = saved.currentStep), repository.current.value)
    }

    @Test
    fun failedWritePreservesReviewAndRetryPersistsDraft() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeRepository(sample()).apply { failSaves = true }
        val viewModel = PersonalPreferencesViewModel(repository)
        advanceUntilIdle()
        viewModel.updateAge(31)
        assertTrue(viewModel.prepareReview())
        val review = viewModel.state.value as PersonalPreferencesUiState.Review
        viewModel.confirmUpdate()
        advanceUntilIdle()
        val error = viewModel.state.value as PersonalPreferencesUiState.SaveError
        assertEquals(review.draft, error.review.draft)
        assertEquals(28, repository.current.value.ageYears)
        repository.failSaves = false
        viewModel.retrySave()
        advanceUntilIdle()
        assertTrue(viewModel.state.value is PersonalPreferencesUiState.Saved)
        assertEquals(31, repository.current.value.ageYears)
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
        var failSaves = false
        override fun observe(): Flow<SavedOnboarding> = current
        override suspend fun save(progress: SavedOnboarding) { if (failSaves) throw IllegalStateException("test failure"); writeCount++; current.value = progress }
    }
}
