package com.kyvo.app.feature.mealshare.domain.model

import kotlin.math.roundToInt

/** Shared visual contract for the Compose preview and the independent image renderer. */
data class MealShareTemplateSpec(
    val aspectRatio: Float,
    val overlayWidthFraction: Float,
    val overlayHeightFraction: Float,
    val outerMarginFraction: Float,
)

object MealShareTemplateSpecs {
    const val exportWidth = 1080
    const val aspectRatio = 1.05f
    val exportHeight: Int = (exportWidth / aspectRatio).roundToInt()

    fun forTemplate(template: MealShareTemplate): MealShareTemplateSpec = when (template) {
        MealShareTemplate.MINIMAL -> MealShareTemplateSpec(aspectRatio, .62f, .34f, .04f)
        MealShareTemplate.PERFORMANCE -> MealShareTemplateSpec(aspectRatio, 1f, .29f, 0f)
        MealShareTemplate.EDITORIAL -> MealShareTemplateSpec(aspectRatio, .58f, .60f, .04f)
    }
}

fun MealShareDraft.renderFingerprint(): String = listOf(
    photoUri.orEmpty(),
    template.name,
    mealType.name,
    title,
    calories,
    protein,
    carbohydrates,
    fat,
    items.joinToString(separator = ",") { item -> "${item.id}:${item.quantity}:${item.calories}:${item.protein}:${item.carbohydrates}:${item.fat}" },
).joinToString(separator = "|")
