package com.kyvo.app.feature.mealshare

import androidx.lifecycle.SavedStateHandle
import com.kyvo.app.feature.mealshare.domain.model.MealSharePhotoSource
import com.kyvo.app.feature.mealshare.presentation.MealShareDraftViewModel
import com.kyvo.app.feature.home.domain.model.MealItem
import com.kyvo.app.feature.home.domain.model.MealType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MealShareDraftViewModelTest {
    private fun item(id: String, quantity: Double = 100.0, calories: Int = 200) = MealItem(id, "Arroz", quantity, "g", calories, 5, 40, 2)

    @Test
    fun setMealTypeUpdatesDraft() {
        val viewModel = MealShareDraftViewModel(SavedStateHandle())
        viewModel.setMealType(MealType.Dinner)
        assertEquals(MealType.Dinner, viewModel.draft.value.mealType)
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
}
