package com.kyvo.app.feature.mealshare.domain.model

import com.kyvo.app.feature.home.domain.model.MealItem
import com.kyvo.app.feature.home.domain.model.MealType

enum class MealShareTemplate { Minimal, Performance, Editorial }

enum class MealSharePhotoSource { Camera, Gallery }

/** Lightweight, local-only state. A photo is represented only by its content URI. */
data class MealShareDraft(
    val photoUri: String? = null,
    val photoSource: MealSharePhotoSource? = null,
    val mealType: MealType = MealType.Lunch,
    val title: String = "Mi comida",
    val items: List<MealItem> = emptyList(),
    val template: MealShareTemplate = MealShareTemplate.Minimal,
) {
    val calories: Int get() = items.sumOf(MealItem::calories)
    val protein: Int get() = items.sumOf(MealItem::protein)
    val carbohydrates: Int get() = items.sumOf(MealItem::carbohydrates)
    val fat: Int get() = items.sumOf(MealItem::fat)
    val isReadyToRender: Boolean get() = !photoUri.isNullOrBlank() && items.isNotEmpty()
}
