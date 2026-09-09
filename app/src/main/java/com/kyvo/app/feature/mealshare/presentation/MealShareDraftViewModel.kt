package com.kyvo.app.feature.mealshare.presentation

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.kyvo.app.feature.mealshare.domain.model.MealShareDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MealShareDraftViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val _draft = MutableStateFlow(MealShareDraft(photoUri = savedStateHandle[PHOTO_URI]))
    val draft: StateFlow<MealShareDraft> = _draft.asStateFlow()
    fun setCapturedPhoto(file: java.io.File) { setPhoto(Uri.fromFile(file).toString()) }
    private fun setPhoto(value: String) { savedStateHandle[PHOTO_URI] = value; _draft.value = _draft.value.copy(photoUri = value) }
    companion object { private const val PHOTO_URI = "meal_share_photo_uri" }
}
