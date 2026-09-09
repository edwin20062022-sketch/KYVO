package com.kyvo.app.feature.mealshare

import androidx.lifecycle.SavedStateHandle
import com.kyvo.app.feature.mealshare.domain.model.MealSharePhotoSource
import com.kyvo.app.feature.mealshare.domain.model.MealShareTemplate
import com.kyvo.app.feature.mealshare.domain.model.renderFingerprint
import com.kyvo.app.feature.mealshare.presentation.MealShareDraftViewModel
import com.kyvo.app.feature.mealshare.presentation.MealShareFinalizationState
import com.kyvo.app.feature.mealshare.domain.render.MealShareRenderResult
import com.kyvo.app.feature.home.domain.model.Meal
import com.kyvo.app.feature.home.domain.model.MealItem
import com.kyvo.app.feature.home.domain.model.MealType
import com.kyvo.app.feature.home.domain.repository.MealRepository
import com.kyvo.app.feature.home.data.InMemoryMealRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import java.io.File
import java.time.LocalDate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MealShareDraftViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = kotlinx.coroutines.Dispatchers.setMain(dispatcher)
    @After fun tearDown() = kotlinx.coroutines.Dispatchers.resetMain()

    private fun item(id: String, quantity: Double = 100.0, calories: Int = 200) = MealItem(id, "Arroz", quantity, "g", calories, 5, 40, 2)

    @Test
    fun setMealTypeUpdatesDraft() {
        val viewModel = MealShareDraftViewModel(SavedStateHandle())
        viewModel.setMealType(MealType.Dinner)
        assertEquals(MealType.Dinner, viewModel.draft.value.mealType)
    }

    @Test
    fun defaultTemplateIsMinimal() {
        assertEquals(MealShareTemplate.MINIMAL, MealShareDraftViewModel(SavedStateHandle()).draft.value.template)
    }

    @Test
    fun eachTemplateCanBeSelected() {
        val viewModel = MealShareDraftViewModel(SavedStateHandle())
        MealShareTemplate.entries.forEach { template ->
            viewModel.setTemplate(template)
            assertEquals(template, viewModel.draft.value.template)
        }
    }

    @Test
    fun selectedTemplateSurvivesRestorationWithoutChangingDraftData() {
        val state = SavedStateHandle()
        val viewModel = MealShareDraftViewModel(state)
        viewModel.setGalleryPhotoUri("content://picker/photo")
        viewModel.setMealType(MealType.Dinner)
        viewModel.addMealItem(item("one"))
        viewModel.setTemplate(MealShareTemplate.PERFORMANCE)
        viewModel.setTemplate(MealShareTemplate.EDITORIAL)

        val restored = MealShareDraftViewModel(state).draft.value
        assertEquals(MealShareTemplate.EDITORIAL, restored.template)
        assertEquals("content://picker/photo", restored.photoUri)
        assertEquals(MealType.Dinner, restored.mealType)
        assertEquals(listOf("one"), restored.items.map(MealItem::id))
        assertEquals(200, restored.calories)
    }

    @Test
    fun addMealItemUpdatesDraftAndTotals() {
        val viewModel = MealShareDraftViewModel(SavedStateHandle())
        viewModel.addMealItem(item("one"))
        assertEquals(listOf("one"), viewModel.draft.value.items.map(MealItem::id))
        assertEquals(200, viewModel.draft.value.calories)
        assertEquals(5, viewModel.draft.value.protein)
    }

    @Test
    fun observingTheSamePendingItemTwiceKeepsOneItem() {
        val viewModel = MealShareDraftViewModel(SavedStateHandle())
        val pending = item("pending")
        viewModel.addMealItemIfAbsent(pending)
        viewModel.addMealItemIfAbsent(pending)
        assertEquals(listOf("pending"), viewModel.draft.value.items.map(MealItem::id))
    }

    @Test
    fun removeMealItemRecalculatesTotals() {
        val viewModel = MealShareDraftViewModel(SavedStateHandle())
        viewModel.addMealItem(item("one"))
        viewModel.addMealItem(item("two", calories = 300))
        viewModel.removeMealItem("one")
        assertEquals(listOf("two"), viewModel.draft.value.items.map(MealItem::id))
        assertEquals(300, viewModel.draft.value.calories)
    }

    @Test
    fun updateMealItemReplacesOnlyMatchingId() {
        val viewModel = MealShareDraftViewModel(SavedStateHandle())
        viewModel.addMealItem(item("one"))
        viewModel.addMealItem(item("two"))
        viewModel.updateMealItem(item("two", calories = 450))
        assertEquals(650, viewModel.draft.value.calories)
    }

    @Test
    fun emptyTotalsAreZero() {
        val draft = MealShareDraftViewModel(SavedStateHandle()).draft.value
        assertEquals(0, draft.calories)
        assertEquals(0, draft.protein)
        assertEquals(0, draft.carbohydrates)
        assertEquals(0, draft.fat)
    }

    @Test
    fun multipleItemsAreSummedWithoutMerging() {
        val viewModel = MealShareDraftViewModel(SavedStateHandle())
        viewModel.addMealItem(item("rice-a"))
        viewModel.addMealItem(item("rice-b"))
        assertEquals(2, viewModel.draft.value.items.size)
        assertEquals(400, viewModel.draft.value.calories)
    }

    @Test
    fun editingPortionRecalculatesNutritionProportionally() {
        val viewModel = MealShareDraftViewModel(SavedStateHandle())
        viewModel.addMealItem(item("one"))
        viewModel.updateMealItemPortion("one", 150.0)
        val updated = viewModel.draft.value.items.single()
        assertEquals(150.0, updated.quantity, 0.0)
        assertEquals(300, updated.calories)
        assertEquals(8, updated.protein)
    }

    @Test
    fun removingAllItemsReturnsEmptyTotals() {
        val viewModel = MealShareDraftViewModel(SavedStateHandle())
        viewModel.addMealItem(item("one"))
        viewModel.removeMealItem("one")
        assertTrue(viewModel.draft.value.items.isEmpty())
        assertEquals(0, viewModel.draft.value.calories)
    }
    @Test
    fun gallerySelectionStoresContentUriAndSource() {
        val viewModel = MealShareDraftViewModel(SavedStateHandle())
        viewModel.setGalleryPhotoUri("content://picker/photo-1")
        assertEquals("content://picker/photo-1", viewModel.draft.value.photoUri)
        assertEquals(MealSharePhotoSource.Gallery, viewModel.draft.value.photoSource)
    }

    @Test
    fun pickerCancellationDoesNotChangeDraft() {
        val viewModel = MealShareDraftViewModel(SavedStateHandle())
        assertNull(viewModel.draft.value.photoUri)
        assertNull(viewModel.draft.value.photoSource)
    }

    @Test
    fun replacingCameraWithGalleryRemovesOnlyOwnedCameraFile() {
        val viewModel = MealShareDraftViewModel(SavedStateHandle())
        viewModel.setGalleryPhotoUri("content://picker/photo-1")
        viewModel.setGalleryPhotoUri("content://picker/photo-2")
        assertEquals("content://picker/photo-2", viewModel.draft.value.photoUri)
        assertEquals(MealSharePhotoSource.Gallery, viewModel.draft.value.photoSource)
    }

    @Test
    fun galleryUriAndSourceRestoreFromSavedState() {
        val state = SavedStateHandle()
        MealShareDraftViewModel(state).setGalleryPhotoUri("content://picker/photo-3")
        val restored = MealShareDraftViewModel(state)
        assertEquals("content://picker/photo-3", restored.draft.value.photoUri)
        assertEquals(MealSharePhotoSource.Gallery, restored.draft.value.photoSource)
    }

    @Test
    fun finalConfirmationPersistsOneStableMealAndRestoresItsState() = runTest(dispatcher) {
        val state = SavedStateHandle()
        val repository = RecordingMealRepository()
        val viewModel = readyViewModel(state)

        viewModel.confirmFinalMeal(repository)
        advanceUntilIdle()

        val persisted = viewModel.finalization.value as MealShareFinalizationState.Persisted
        assertEquals(1, repository.added.size)
        assertEquals(persisted.mealId, repository.added.single().id)
        assertEquals(MealType.Lunch, repository.added.single().type)
        assertEquals(viewModel.draft.value.items, repository.added.single().items)
        assertEquals(viewModel.draft.value.calories, repository.added.single().totalCalories)
        assertTrue(repository.added.single().time?.matches(Regex("\\d{2}:\\d{2}")) == true)

        val restored = MealShareDraftViewModel(state)
        assertEquals(MealShareFinalizationState.Persisted(persisted.mealId), restored.finalization.value)
        assertEquals(viewModel.draft.value.renderedImagePath, restored.draft.value.renderedImagePath)
        restored.confirmFinalMeal(repository)
        advanceUntilIdle()
        assertEquals(1, repository.added.size)
    }

    @Test
    fun finalConfirmationIgnoresDoubleTapWhilePersistenceIsRunning() = runTest(dispatcher) {
        val repository = BlockingMealRepository()
        val viewModel = readyViewModel(SavedStateHandle())

        viewModel.confirmFinalMeal(repository)
        runCurrent()
        viewModel.confirmFinalMeal(repository)
        assertEquals(1, repository.calls)

        repository.result.complete(Unit)
        advanceUntilIdle()
        assertTrue(viewModel.finalization.value is MealShareFinalizationState.Persisted)
    }

    @Test
    fun retryAfterFailureReusesTheSameStableMealId() = runTest(dispatcher) {
        val repository = RecordingMealRepository(mutableListOf(Result.failure(IllegalStateException("offline")), Result.success(Unit)))
        val viewModel = readyViewModel(SavedStateHandle())

        viewModel.confirmFinalMeal(repository)
        advanceUntilIdle()
        assertTrue(viewModel.finalization.value is MealShareFinalizationState.Error)
        viewModel.confirmFinalMeal(repository)
        advanceUntilIdle()

        assertEquals(2, repository.added.size)
        assertEquals(repository.added[0].id, repository.added[1].id)
        assertTrue(viewModel.finalization.value is MealShareFinalizationState.Persisted)
    }

    @Test
    fun finalConfirmationRequiresAValidCurrentRender() = runTest(dispatcher) {
        val repository = RecordingMealRepository()
        val viewModel = MealShareDraftViewModel(SavedStateHandle())
        viewModel.setGalleryPhotoUri("content://picker/photo")
        viewModel.addMealItem(item("one"))

        viewModel.confirmFinalMeal(repository)
        advanceUntilIdle()

        assertEquals(0, repository.added.size)
        assertTrue(viewModel.finalization.value is MealShareFinalizationState.Error)
    }

    @Test
    fun finalConfirmationRequiresPhotoAndMealItemsIndependently() = runTest(dispatcher) {
        val repository = RecordingMealRepository()
        val withoutPhoto = MealShareDraftViewModel(SavedStateHandle()).apply {
            addMealItem(item("one"))
            setRenderedResult(renderResultFor(draft.value))
        }
        val withoutItems = MealShareDraftViewModel(SavedStateHandle()).apply {
            setGalleryPhotoUri("content://picker/photo")
            setRenderedResult(renderResultFor(draft.value))
        }

        withoutPhoto.confirmFinalMeal(repository)
        withoutItems.confirmFinalMeal(repository)
        advanceUntilIdle()

        assertEquals(0, repository.added.size)
        assertTrue(withoutPhoto.finalization.value is MealShareFinalizationState.Error)
        assertTrue(withoutItems.finalization.value is MealShareFinalizationState.Error)
    }

    @Test
    fun staleRenderCannotBePersisted() = runTest(dispatcher) {
        val repository = RecordingMealRepository()
        val viewModel = readyViewModel(SavedStateHandle())
        viewModel.setTemplate(MealShareTemplate.EDITORIAL)

        viewModel.confirmFinalMeal(repository)
        advanceUntilIdle()

        assertEquals(0, repository.added.size)
        assertTrue(viewModel.finalization.value is MealShareFinalizationState.Error)
    }

    @Test
    fun finalizedMealAppearsThroughTheStandardRepositoryFlow() = runTest(dispatcher) {
        val repository = InMemoryMealRepository()
        val viewModel = readyViewModel(SavedStateHandle())

        viewModel.confirmFinalMeal(repository)
        advanceUntilIdle()

        assertEquals((viewModel.finalization.value as MealShareFinalizationState.Persisted).mealId, repository.observeMealsForDate(LocalDate.now()).first().single().id)
    }

    @Test
    fun persistedDraftDoesNotAllowLaterMutationsOrDuplicateRegistration() = runTest(dispatcher) {
        val repository = RecordingMealRepository()
        val viewModel = readyViewModel(SavedStateHandle())
        viewModel.confirmFinalMeal(repository)
        advanceUntilIdle()
        val originalTemplate = viewModel.draft.value.template

        viewModel.setTemplate(MealShareTemplate.EDITORIAL)
        viewModel.addMealItem(item("another"))
        viewModel.confirmFinalMeal(repository)
        advanceUntilIdle()

        assertEquals(originalTemplate, viewModel.draft.value.template)
        assertEquals(listOf("one"), viewModel.draft.value.items.map(MealItem::id))
        assertEquals(1, repository.added.size)
    }

    private fun readyViewModel(state: SavedStateHandle): MealShareDraftViewModel {
        val viewModel = MealShareDraftViewModel(state)
        viewModel.setGalleryPhotoUri("content://picker/photo")
        viewModel.addMealItem(item("one"))
        viewModel.setRenderedResult(renderResultFor(viewModel.draft.value))
        return viewModel
    }

    private fun renderResultFor(draft: com.kyvo.app.feature.mealshare.domain.model.MealShareDraft): MealShareRenderResult {
        val rendered = File.createTempFile("meal-share-render", ".png").apply {
            writeBytes(byteArrayOf(1, 2, 3))
            deleteOnExit()
        }
        return MealShareRenderResult(rendered, 1080, 931, draft.renderFingerprint())
    }

    private class RecordingMealRepository(
        private val results: MutableList<Result<Unit>> = mutableListOf(Result.success(Unit)),
    ) : MealRepository {
        val added = mutableListOf<Meal>()
        override fun observeMealsForDate(date: LocalDate): Flow<List<Meal>> = flowOf(emptyList())
        override fun observeMeal(id: String): Flow<Meal?> = flowOf(null)
        override suspend fun addMeal(date: LocalDate, meal: Meal) {
            added += meal
            (if (results.isEmpty()) Result.success(Unit) else results.removeAt(0)).getOrThrow()
        }
        override suspend fun updateMeal(meal: Meal) = Unit
        override suspend fun deleteMeal(id: String) = Unit
    }

    private class BlockingMealRepository : MealRepository {
        var calls = 0
        val result = CompletableDeferred<Unit>()
        override fun observeMealsForDate(date: LocalDate): Flow<List<Meal>> = flowOf(emptyList())
        override fun observeMeal(id: String): Flow<Meal?> = flowOf(null)
        override suspend fun addMeal(date: LocalDate, meal: Meal) {
            calls++
            result.await()
        }
        override suspend fun updateMeal(meal: Meal) = Unit
        override suspend fun deleteMeal(id: String) = Unit
    }
}
