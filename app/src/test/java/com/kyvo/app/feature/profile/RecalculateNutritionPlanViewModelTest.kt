package com.kyvo.app.feature.profile.presentation

import com.kyvo.app.feature.onboarding.domain.model.FitnessGoal
import com.kyvo.app.feature.onboarding.domain.model.ExperienceLevel
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
class RecalculateNutritionPlanViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun currentGoalIsSelectedAndSelectionIsOnlyDraft() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeRepository(saved())
        val viewModel = RecalculateNutritionPlanViewModel(repository)
        advanceUntilIdle()

        assertEquals(FitnessGoal.Recomposition, (viewModel.state.value as RecalculateNutritionPlanUiState.Editing).selectedGoal)
        viewModel.selectGoal(FitnessGoal.MuscleGain)

        assertEquals(FitnessGoal.MuscleGain, (viewModel.state.value as RecalculateNutritionPlanUiState.Editing).selectedGoal)
        assertEquals(FitnessGoal.Recomposition, repository.current.value.goal)
        assertEquals(0, repository.writeCount)
    }

    @Test
    fun previewUsesRealCalculatorAndDoesNotChangeOldPlan() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeRepository(saved())
        val viewModel = RecalculateNutritionPlanViewModel(repository)
        advanceUntilIdle()
        viewModel.selectGoal(FitnessGoal.MuscleGain)
        viewModel.calculatePreview()
        advanceUntilIdle()

        val preview = viewModel.state.value as RecalculateNutritionPlanUiState.Preview
        assertEquals(FitnessGoal.MuscleGain, preview.selectedGoal)
        assertEquals(2300, preview.oldPlan.targetCaloriesKcal)
        assertEquals(2780, preview.previewPlan.targetCaloriesKcal)
        assertEquals(2300, repository.current.value.plan?.targetCaloriesKcal)
        assertEquals(0, repository.writeCount)
    }

    @Test
    fun confirmPersistsGoalAndPlanTogetherAndPreservesOtherFields() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeRepository(saved())
        val viewModel = RecalculateNutritionPlanViewModel(repository)
        advanceUntilIdle()
        viewModel.selectGoal(FitnessGoal.MuscleGain)
        viewModel.calculatePreview()
        advanceUntilIdle()
        viewModel.confirm()
        advanceUntilIdle()

        assertTrue(viewModel.state.value is RecalculateNutritionPlanUiState.Saved)
        assertEquals(1, repository.writeCount)
        assertEquals(FitnessGoal.MuscleGain, repository.current.value.goal)
        assertEquals(2780, repository.current.value.plan?.targetCaloriesKcal)
        assertEquals(28, repository.current.value.ageYears)
        assertEquals(180.0, repository.current.value.heightCm ?: -1.0, 0.0)
        assertEquals(75.0, repository.current.value.weightKg ?: -1.0, 0.0)
        assertEquals(5, repository.current.value.trainingDaysPerWeek)
        assertEquals(TrainingType.Hypertrophy, repository.current.value.trainingType)
        assertEquals(WorkActivity.Sedentary, repository.current.value.workActivity)
        assertTrue(repository.current.value.isCompleted)
    }

    @Test
    fun saveFailureKeepsPreviewAndDoesNotReplaceStoredPlan() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeRepository(saved(), failOnSave = true)
        val viewModel = RecalculateNutritionPlanViewModel(repository)
        advanceUntilIdle()
        viewModel.selectGoal(FitnessGoal.MuscleGain)
        viewModel.calculatePreview()
        advanceUntilIdle()
        viewModel.confirm()
        advanceUntilIdle()

        val error = viewModel.state.value as RecalculateNutritionPlanUiState.Error
        assertEquals(2780, error.preview?.previewPlan?.targetCaloriesKcal)
        assertEquals(FitnessGoal.Recomposition, repository.current.value.goal)
        assertEquals(2300, repository.current.value.plan?.targetCaloriesKcal)
    }

    private fun saved() = SavedOnboarding(
        gender = GenderOption.Male, ageYears = 28, heightCm = 180.0, weightKg = 75.0,
        trainingDaysPerWeek = 5, trainingType = TrainingType.Hypertrophy, workActivity = WorkActivity.Sedentary,
        goal = FitnessGoal.Recomposition, experience = ExperienceLevel.Intermediate, foodPreference = FoodPreference.None, mealsPerDay = 4,
        plan = NutritionPlan(1637.0, 2537.0, 1.55, -0.05, 2300, 160, 225, 70), isCompleted = true,
    )

    private class FakeRepository(initial: SavedOnboarding, private val failOnSave: Boolean = false) : OnboardingRepository {
        val current = MutableStateFlow(initial)
        var writeCount = 0
        override fun observe(): Flow<SavedOnboarding> = current
        override suspend fun save(progress: SavedOnboarding) {
            writeCount++
            if (failOnSave) error("write failed")
            current.value = progress
        }
    }
}
