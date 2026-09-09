package com.kyvo.app.feature.mealshare.domain.render

import kotlin.math.roundToInt

data class MealShareCropRect(val left: Int, val top: Int, val width: Int, val height: Int)

/** Equivalent to Compose ContentScale.Crop with a centered source crop. */
fun centeredCropRect(sourceWidth: Int, sourceHeight: Int, targetAspectRatio: Float): MealShareCropRect {
    require(sourceWidth > 0 && sourceHeight > 0) { "Source dimensions must be positive" }
    require(targetAspectRatio > 0f) { "Target aspect ratio must be positive" }
    val sourceAspectRatio = sourceWidth.toFloat() / sourceHeight
    return if (sourceAspectRatio > targetAspectRatio) {
        val width = (sourceHeight * targetAspectRatio).roundToInt().coerceAtMost(sourceWidth)
        MealShareCropRect((sourceWidth - width) / 2, 0, width, sourceHeight)
    } else {
        val height = (sourceWidth / targetAspectRatio).roundToInt().coerceAtMost(sourceHeight)
        MealShareCropRect(0, (sourceHeight - height) / 2, sourceWidth, height)
    }
}
