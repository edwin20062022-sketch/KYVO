package com.kyvo.app.feature.mealshare.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kyvo.app.feature.mealshare.domain.model.MealShareDraft
import com.kyvo.app.feature.mealshare.domain.model.renderFingerprint
import com.kyvo.app.feature.mealshare.domain.model.toOverlayData
import com.kyvo.app.feature.mealshare.domain.render.MealShareImageRenderer
import com.kyvo.app.feature.mealshare.domain.render.MealShareRenderRequest
import com.kyvo.app.feature.mealshare.domain.render.MealShareRenderResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MealShareRenderState {
    data object Idle : MealShareRenderState
    data object Rendering : MealShareRenderState
    data class Success(val result: MealShareRenderResult) : MealShareRenderState
    data class Error(val message: String) : MealShareRenderState
}

class MealShareRenderViewModel(private val renderer: MealShareImageRenderer) : ViewModel() {
    private val _state = MutableStateFlow<MealShareRenderState>(MealShareRenderState.Idle)
    val state: StateFlow<MealShareRenderState> = _state.asStateFlow()
    private var latestDraftFingerprint: String? = null

    fun render(draft: MealShareDraft) {
        if (_state.value is MealShareRenderState.Rendering) return
        val photoUri = draft.photoUri
        if (photoUri.isNullOrBlank()) {
            _state.value = MealShareRenderState.Error("Agrega una fotografía antes de generar la vista final.")
            return
        }
        val fingerprint = draft.renderFingerprint()
        latestDraftFingerprint = fingerprint
        viewModelScope.launch {
            _state.value = MealShareRenderState.Rendering
            renderer.render(
                MealShareRenderRequest(
                    photoUri = photoUri,
                    template = draft.template,
                    overlayData = draft.toOverlayData(),
                    fingerprint = fingerprint,
                ),
            ).fold(
                onSuccess = { result ->
                    _state.value = if (latestDraftFingerprint == fingerprint) {
                        MealShareRenderState.Success(result)
                    } else {
                        result.deleteOwnedRender()
                        MealShareRenderState.Idle
                    }
                },
                onFailure = { error ->
                    _state.value = if (latestDraftFingerprint == fingerprint) {
                        MealShareRenderState.Error(error.message ?: "No se pudo generar la imagen Meal Share.")
                    } else {
                        MealShareRenderState.Idle
                    }
                },
            )
        }
    }

    fun invalidateIfStale(fingerprint: String) {
        latestDraftFingerprint = fingerprint
        val current = _state.value
        if (current is MealShareRenderState.Success && current.result.fingerprint != fingerprint) {
            current.result.deleteOwnedRender()
            _state.value = MealShareRenderState.Idle
        }
    }

    fun clearSession() {
        _state.value = MealShareRenderState.Idle
        latestDraftFingerprint = null
    }

    companion object {
        fun factory(renderer: MealShareImageRenderer): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = MealShareRenderViewModel(renderer) as T
        }
    }
}

private fun MealShareRenderResult.deleteOwnedRender() {
    if (file.name.startsWith("meal_share_render_") && file.extension.equals("jpg", ignoreCase = true)) file.delete()
}
