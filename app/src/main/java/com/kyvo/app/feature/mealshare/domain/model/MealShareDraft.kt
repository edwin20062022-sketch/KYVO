package com.kyvo.app.feature.mealshare.domain.model

import com.kyvo.app.feature.home.domain.model.MealItem
import com.kyvo.app.feature.home.domain.model.MealType

enum class MealShareTemplate { MINIMAL, PERFORMANCE, EDITORIAL }

enum class MealSharePhotoSource { Camera, Gallery }

/** Lightweight, local-only state. A photo is represented only by its content URI. */
data class MealShareDraft(
    val photoUri: String? = null,
    val photoSource: MealSharePhotoSource? = null,
    val mealType: MealType = MealType.Lunch,
    val title: String = "Mi comida",
    val items: List<MealItem> = emptyList(),
    val template: MealShareTemplate = MealShareTemplate.MINIMAL,
) {
    val calories: Int get() = items.sumOf(MealItem::calories)
    val protein: Int get() = items.sumOf(MealItem::protein)
    val carbohydrates: Int get() = items.sumOf(MealItem::carbohydrates)
    val fat: Int get() = items.sumOf(MealItem::fat)
    val isReadyToRender: Boolean get() = !photoUri.isNullOrBlank() && items.isNotEmpty()
}

/** Renderer-ready data derived from the local draft without reimplementing nutrition math. */
data class MealShareOverlayData(
    val title: String,
    val mealTypeLabel: String,
    val calories: Int,
    val protein: Int,
    val carbohydrates: Int,
    val fat: Int,
) {
    val caloriesLabel: String get() = "$calories kcal"
    val proteinLabel: String get() = "P $protein g"
    val carbohydratesLabel: String get() = "C $carbohydrates g"
    val fatLabel: String get() = "G $fat g"
}

fun MealShareDraft.toOverlayData() = MealShareOverlayData(
    title = title,
    mealTypeLabel = mealType.label,
    calories = calories,
    protein = protein,
    carbohydrates = carbohydrates,
    fat = fat,
)
