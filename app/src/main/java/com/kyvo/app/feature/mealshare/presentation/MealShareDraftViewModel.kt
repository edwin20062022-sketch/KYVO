package com.kyvo.app.feature.mealshare.presentation

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kyvo.app.feature.mealshare.domain.model.MealShareDraft
import com.kyvo.app.feature.mealshare.domain.model.MealSharePhotoSource
import com.kyvo.app.feature.mealshare.domain.model.MealShareTemplate
import com.kyvo.app.feature.food.domain.model.FoodType
import com.kyvo.app.feature.home.domain.model.MealItem
import com.kyvo.app.feature.home.domain.model.MealType
import com.kyvo.app.feature.home.domain.model.Meal
import com.kyvo.app.feature.home.domain.repository.MealRepository
import com.kyvo.app.feature.mealshare.domain.model.renderFingerprint
import com.kyvo.app.feature.mealshare.domain.render.MealShareRenderResult
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt

class MealShareDraftViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val _draft = MutableStateFlow(
        MealShareDraft(
            photoUri = savedStateHandle[PHOTO_URI],
            photoSource = (savedStateHandle[PHOTO_SOURCE] as String?)?.let { MealSharePhotoSource.valueOf(it) },
            mealType = (savedStateHandle[MEAL_TYPE] as String?)?.let { MealType.valueOf(it) } ?: MealType.Lunch,
            items = decodeItems(savedStateHandle[MEAL_ITEMS] as String?),
            template = (savedStateHandle[TEMPLATE] as String?)?.let { value ->
                runCatching { MealShareTemplate.valueOf(value) }.getOrNull()
            } ?: MealShareTemplate.MINIMAL,
            renderedImagePath = savedStateHandle[RENDERED_IMAGE_PATH],
            renderedFingerprint = savedStateHandle[RENDERED_FINGERPRINT],
        ),
    )
    val draft: StateFlow<MealShareDraft> = _draft.asStateFlow()
    private val _finalization = MutableStateFlow<MealShareFinalizationState>(
        (savedStateHandle[PERSISTED_MEAL_ID] as String?)?.let(MealShareFinalizationState::Persisted) ?: MealShareFinalizationState.Ready,
    )
    val finalization: StateFlow<MealShareFinalizationState> = _finalization.asStateFlow()
    fun setCapturedPhoto(file: java.io.File) {
        if (isPersisted()) return
        replaceOwnedCameraFileIfNeeded()
        setPhoto(Uri.fromFile(file).toString(), MealSharePhotoSource.Camera)
    }

    fun setGalleryPhoto(uri: Uri) {
        if (isPersisted()) return
        replaceOwnedCameraFileIfNeeded()
        setGalleryPhotoUri(uri.toString())
    }

    fun setGalleryPhotoUri(value: String) {
        if (isPersisted()) return
        replaceOwnedCameraFileIfNeeded()
        setPhoto(value, MealSharePhotoSource.Gallery)
    }

    fun setMealType(type: MealType) {
        if (isPersisted()) return
        savedStateHandle[MEAL_TYPE] = type.name
        _draft.value = _draft.value.copy(mealType = type)
    }

    fun setTemplate(template: MealShareTemplate) {
        if (isPersisted()) return
        savedStateHandle[TEMPLATE] = template.name
        _draft.value = _draft.value.copy(template = template)
    }

    fun addMealItem(item: MealItem) {
        if (isPersisted()) return
        updateItems(_draft.value.items + item)
    }

    fun addMealItemIfAbsent(item: MealItem) {
        if (_draft.value.items.none { it.id == item.id }) {
            addMealItem(item)
        }
    }

    fun updateMealItem(item: MealItem) {
        if (isPersisted()) return
        updateItems(_draft.value.items.map { current -> if (current.id == item.id) item else current })
    }

    fun updateMealItemPortion(id: String, quantity: Double) {
        if (quantity <= 0.0) return
        val current = _draft.value.items.firstOrNull { it.id == id } ?: return
        if (current.quantity <= 0.0) return
        val factor = quantity / current.quantity
        updateMealItem(
            current.copy(
                quantity = quantity,
                grams = current.grams?.times(factor),
                calories = (current.calories * factor).roundToInt(),
                protein = (current.protein * factor).roundToInt(),
                carbohydrates = (current.carbohydrates * factor).roundToInt(),
                fat = (current.fat * factor).roundToInt(),
            ),
        )
    }

    fun removeMealItem(id: String) {
        if (isPersisted()) return
        updateItems(_draft.value.items.filterNot { it.id == id })
    }

    private fun setPhoto(value: String, source: MealSharePhotoSource) {
        invalidateRenderedReference()
        savedStateHandle[PHOTO_URI] = value
        savedStateHandle[PHOTO_SOURCE] = source.name
        _draft.value = _draft.value.copy(photoUri = value, photoSource = source)
    }

    private fun updateItems(items: List<MealItem>) {
        invalidateRenderedReference()
        savedStateHandle[MEAL_ITEMS] = Json.encodeToString(items.map(MealItemSnapshot::from))
        _draft.value = _draft.value.copy(items = items)
    }

    fun setRenderedResult(result: MealShareRenderResult) {
        if (result.fingerprint != _draft.value.renderFingerprint()) return
        savedStateHandle[RENDERED_IMAGE_PATH] = result.file.absolutePath
        savedStateHandle[RENDERED_FINGERPRINT] = result.fingerprint
        _draft.value = _draft.value.copy(renderedImagePath = result.file.absolutePath, renderedFingerprint = result.fingerprint)
    }

    fun confirmFinalMeal(repository: MealRepository) {
        if (_finalization.value is MealShareFinalizationState.Persisting || _finalization.value is MealShareFinalizationState.Persisted) return
        val current = _draft.value
        val validationError = current.finalizationError()
        if (validationError != null) {
            _finalization.value = MealShareFinalizationState.Error(validationError)
            return
        }
        val mealId = (savedStateHandle[PENDING_MEAL_ID] as String?) ?: UUID.randomUUID().toString().also { savedStateHandle[PENDING_MEAL_ID] = it }
        val meal = Meal(
            id = mealId,
            type = current.mealType,
            title = current.title,
            time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
            items = current.items,
            totalCalories = current.calories,
            protein = current.protein,
            carbohydrates = current.carbohydrates,
            fat = current.fat,
        )
        viewModelScope.launch {
            _finalization.value = MealShareFinalizationState.Persisting
            try {
                repository.addMeal(LocalDate.now(), meal)
                savedStateHandle[PERSISTED_MEAL_ID] = mealId
                _finalization.value = MealShareFinalizationState.Persisted(mealId)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                _finalization.value = MealShareFinalizationState.Error(failure.message ?: "No pudimos registrar tu comida.")
            }
        }
    }

    private fun MealShareDraft.finalizationError(): String? = when {
        photoUri.isNullOrBlank() -> "Agrega una fotografía antes de registrar la comida."
        items.isEmpty() || items.any { it.quantity <= 0.0 || it.calories < 0 || it.protein < 0 || it.carbohydrates < 0 || it.fat < 0 } -> "Agrega alimentos con información nutricional válida."
        renderedImagePath.isNullOrBlank() || renderedFingerprint != renderFingerprint() || !java.io.File(renderedImagePath).isFile || java.io.File(renderedImagePath).length() <= 0L -> "Genera una imagen final actual antes de registrar la comida."
        else -> null
    }

    private fun invalidateRenderedReference() {
        if (_draft.value.renderedImagePath == null) return
        savedStateHandle[RENDERED_IMAGE_PATH] = null
        savedStateHandle[RENDERED_FINGERPRINT] = null
        _draft.value = _draft.value.copy(renderedImagePath = null, renderedFingerprint = null)
    }

    private fun isPersisted() = _finalization.value is MealShareFinalizationState.Persisted

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
        private const val MEAL_TYPE = "meal_share_meal_type"
        private const val MEAL_ITEMS = "meal_share_meal_items"
        private const val TEMPLATE = "meal_share_template"
        private const val RENDERED_IMAGE_PATH = "meal_share_rendered_image_path"
        private const val RENDERED_FINGERPRINT = "meal_share_rendered_fingerprint"
        private const val PENDING_MEAL_ID = "meal_share_pending_meal_id"
        private const val PERSISTED_MEAL_ID = "meal_share_persisted_meal_id"

        private fun decodeItems(value: String?): List<MealItem> = value?.let {
            runCatching { Json.decodeFromString<List<MealItemSnapshot>>(it).map(MealItemSnapshot::toDomain) }.getOrDefault(emptyList())
        } ?: emptyList()
    }
}

sealed interface MealShareFinalizationState {
    data object Ready : MealShareFinalizationState
    data object Persisting : MealShareFinalizationState
    data class Persisted(val mealId: String) : MealShareFinalizationState
    data class Error(val message: String) : MealShareFinalizationState
}

@Serializable
private data class MealItemSnapshot(
    val id: String,
    val name: String,
    val quantity: Double,
    val unit: String,
    val calories: Int,
    val protein: Int,
    val carbohydrates: Int,
    val fat: Int,
    val image: String? = null,
    val fiber: Double? = null,
    val sugar: Double? = null,
    val sodiumMg: Double? = null,
    val grams: Double? = null,
    val foodId: String? = null,
    val foodType: String? = null,
) {
    fun toDomain() = MealItem(id, name, quantity, unit, calories, protein, carbohydrates, fat, image, fiber, sugar, sodiumMg, grams, foodId, foodType?.let { runCatching { FoodType.valueOf(it) }.getOrNull() })

    companion object {
        fun from(item: MealItem) = MealItemSnapshot(item.id, item.name, item.quantity, item.unit, item.calories, item.protein, item.carbohydrates, item.fat, item.image, item.fiber, item.sugar, item.sodiumMg, item.grams, item.foodId, item.foodType?.name)
    }
}
