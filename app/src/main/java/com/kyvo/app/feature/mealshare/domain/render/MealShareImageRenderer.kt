package com.kyvo.app.feature.mealshare.domain.render

import com.kyvo.app.feature.mealshare.domain.model.MealShareOverlayData
import com.kyvo.app.feature.mealshare.domain.model.MealShareTemplate
import java.io.File

data class MealShareRenderRequest(
    val photoUri: String,
    val template: MealShareTemplate,
    val overlayData: MealShareOverlayData,
    val fingerprint: String,
)

data class MealShareRenderResult(
    val file: File,
    val width: Int,
    val height: Int,
    val fingerprint: String,
)

interface MealShareImageRenderer {
    suspend fun render(request: MealShareRenderRequest): Result<MealShareRenderResult>
}
