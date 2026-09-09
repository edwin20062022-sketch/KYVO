package com.kyvo.app.feature.mealshare

import androidx.lifecycle.SavedStateHandle
import com.kyvo.app.feature.mealshare.domain.model.MealSharePhotoSource
import com.kyvo.app.feature.mealshare.presentation.MealShareDraftViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MealShareDraftViewModelTest {
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
