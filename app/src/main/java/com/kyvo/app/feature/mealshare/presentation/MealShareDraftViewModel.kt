package com.kyvo.app.feature.mealshare.presentation

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.kyvo.app.feature.mealshare.domain.model.MealShareDraft
import com.kyvo.app.feature.mealshare.domain.model.MealSharePhotoSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MealShareDraftViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val _draft = MutableStateFlow(
        MealShareDraft(
            photoUri = savedStateHandle[PHOTO_URI],
            photoSource = (savedStateHandle[PHOTO_SOURCE] as String?)?.let { MealSharePhotoSource.valueOf(it) },
        ),
    )
    val draft: StateFlow<MealShareDraft> = _draft.asStateFlow()
    fun setCapturedPhoto(file: java.io.File) {
        replaceOwnedCameraFileIfNeeded()
        setPhoto(Uri.fromFile(file).toString(), MealSharePhotoSource.Camera)
    }

    fun setGalleryPhoto(uri: Uri) {
        replaceOwnedCameraFileIfNeeded()
        setGalleryPhotoUri(uri.toString())
    }

    fun setGalleryPhotoUri(value: String) {
        replaceOwnedCameraFileIfNeeded()
        setPhoto(value, MealSharePhotoSource.Gallery)
    }

    private fun setPhoto(value: String, source: MealSharePhotoSource) {
        savedStateHandle[PHOTO_URI] = value
        savedStateHandle[PHOTO_SOURCE] = source.name
        _draft.value = _draft.value.copy(photoUri = value, photoSource = source)
    }

    private fun replaceOwnedCameraFileIfNeeded() {
        val previous = _draft.value
        if (previous.photoSource == MealSharePhotoSource.Camera) {
            previous.photoUri?.let { uri ->
                Uri.parse(uri).path?.let { path ->
                    val file = java.io.File(path)
                    if (file.parentFile?.name == "meal_share" && file.parentFile?.parentFile?.name == "cache") {
                        file.delete()
                    }
                }
            }
        }
    }

    companion object {
        private const val PHOTO_URI = "meal_share_photo_uri"
        private const val PHOTO_SOURCE = "meal_share_photo_source"
    }
}
