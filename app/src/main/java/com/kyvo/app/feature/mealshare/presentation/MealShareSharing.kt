package com.kyvo.app.feature.mealshare.presentation

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import com.kyvo.app.feature.mealshare.domain.model.MealShareDraft
import com.kyvo.app.feature.mealshare.domain.model.renderFingerprint
import java.io.File

internal const val MEAL_SHARE_RENDER_RETENTION_MILLIS = 24L * 60L * 60L * 1000L

sealed interface MealShareShareState {
    data object ReadyToShare : MealShareShareState
    data object LaunchingShare : MealShareShareState
    data object ReturnedFromShareSheet : MealShareShareState
    data class Error(val message: String) : MealShareShareState
}

internal fun MealShareDraft.shareableRender(renderedDirectory: File): Result<File> = runCatching {
    require(!renderedImagePath.isNullOrBlank()) { "Genera una imagen final antes de compartir." }
    require(renderedFingerprint == renderFingerprint()) { "La imagen final ya no corresponde a esta comida." }
    val root = renderedDirectory.canonicalFile
    val file = File(renderedImagePath).canonicalFile
    require(file.parentFile == root) { "La imagen final no pertenece al directorio seguro de Meal Share." }
    require(file.name.startsWith("meal_share_render_") && file.extension.equals("jpg", ignoreCase = true)) { "La imagen final no es un render válido de Meal Share." }
    require(file.isFile && file.length() > 0L) { "La imagen final ya no está disponible." }
    file
}

internal fun buildMealShareIntent(contentUri: Uri): Intent = Intent(Intent.ACTION_SEND).apply {
    type = "image/jpeg"
    putExtra(Intent.EXTRA_STREAM, contentUri)
    clipData = ClipData.newRawUri("Meal Share", contentUri)
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
}

internal fun cleanupExpiredMealShareRenders(renderedDirectory: File, nowMillis: Long) {
    val root = renderedDirectory.canonicalFile
    root.listFiles()?.forEach { candidate ->
        val file = candidate.canonicalFile
        val ownedRender = file.parentFile == root && file.name.startsWith("meal_share_render_") && file.extension.equals("jpg", ignoreCase = true)
        if (ownedRender && nowMillis - file.lastModified() > MEAL_SHARE_RENDER_RETENTION_MILLIS) file.delete()
    }
}
