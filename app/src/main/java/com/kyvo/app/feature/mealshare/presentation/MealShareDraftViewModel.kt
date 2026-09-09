package com.kyvo.app.feature.mealshare.presentation

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.kyvo.app.feature.mealshare.domain.model.MealShareDraft
import com.kyvo.app.feature.mealshare.domain.model.MealSharePhotoSource
import com.kyvo.app.feature.food.domain.model.FoodType
import com.kyvo.app.feature.home.domain.model.MealItem
import com.kyvo.app.feature.home.domain.model.MealType
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

    fun setMealType(type: MealType) {
        savedStateHandle[MEAL_TYPE] = type.name
        _draft.value = _draft.value.copy(mealType = type)
    }

    fun addMealItem(item: MealItem) {
        updateItems(_draft.value.items + item)
    }

    fun updateMealItem(item: MealItem) {
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
        updateItems(_draft.value.items.filterNot { it.id == id })
    }

    private fun setPhoto(value: String, source: MealSharePhotoSource) {
        savedStateHandle[PHOTO_URI] = value
        savedStateHandle[PHOTO_SOURCE] = source.name
        _draft.value = _draft.value.copy(photoUri = value, photoSource = source)
    }

    private fun updateItems(items: List<MealItem>) {
        savedStateHandle[MEAL_ITEMS] = Json.encodeToString(items.map(MealItemSnapshot::from))
        _draft.value = _draft.value.copy(items = items)
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
        private const val MEAL_TYPE = "meal_share_meal_type"
        private const val MEAL_ITEMS = "meal_share_meal_items"

        private fun decodeItems(value: String?): List<MealItem> = value?.let {
            runCatching { Json.decodeFromString<List<MealItemSnapshot>>(it).map(MealItemSnapshot::toDomain) }.getOrDefault(emptyList())
        } ?: emptyList()
    }
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
