package com.kyvo.app.feature.mealshare.domain.model

import kotlin.math.roundToInt

/** Shared visual contract for the Compose preview and the independent image renderer. */
data class MealShareTemplateSpec(
    val aspectRatio: Float,
    val overlayLeftFraction: Float,
    val overlayTopFraction: Float,
    val overlayWidthFraction: Float,
    val overlayHeightFraction: Float,
)

object MealShareTemplateSpecs {
    const val exportWidth = 1080
    /** Measured from the Meal Share editor card in the approved KYVO mockup. */
    const val aspectRatio = 1.16f
    val exportHeight: Int = (exportWidth / aspectRatio).roundToInt()

    fun forTemplate(template: MealShareTemplate): MealShareTemplateSpec = when (template) {
        MealShareTemplate.MINIMAL -> MealShareTemplateSpec(aspectRatio, .68f, .03f, .30f, .32f)
        MealShareTemplate.PERFORMANCE -> MealShareTemplateSpec(aspectRatio, 0f, .65f, 1f, .35f)
        MealShareTemplate.EDITORIAL -> MealShareTemplateSpec(aspectRatio, 0f, 0f, .58f, 1f)
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
