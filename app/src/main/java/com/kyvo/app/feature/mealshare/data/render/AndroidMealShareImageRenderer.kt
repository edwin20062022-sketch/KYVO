package com.kyvo.app.feature.mealshare.data.render

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import com.kyvo.app.R
import com.kyvo.app.feature.mealshare.domain.model.MealShareOverlayData
import com.kyvo.app.feature.mealshare.domain.model.MealShareTemplate
import com.kyvo.app.feature.mealshare.domain.model.MealShareTemplateSpecs
import com.kyvo.app.feature.mealshare.domain.render.MealShareImageRenderer
import com.kyvo.app.feature.mealshare.domain.render.MealShareRenderRequest
import com.kyvo.app.feature.mealshare.domain.render.MealShareRenderResult
import com.kyvo.app.feature.mealshare.domain.render.centeredCropRect
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

/** Android-only Canvas renderer. Bitmaps are temporary and never escape this class. */
class AndroidMealShareImageRenderer(private val context: Context) : MealShareImageRenderer {
    override suspend fun render(request: MealShareRenderRequest): Result<MealShareRenderResult> = try {
        val source = withContext(Dispatchers.IO) { decodeSource(Uri.parse(request.photoUri)) }
        val rendered = try {
            withContext(Dispatchers.Default) { renderBitmap(source, request) }
        } finally {
            source.recycle()
        }
        try {
            withContext(Dispatchers.IO) { writeOutput(rendered, request.fingerprint) }
        } finally {
            rendered.recycle()
        }
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        Result.failure(error)
    }

    private fun decodeSource(uri: Uri): Bitmap {
        require(uri.scheme == "content" || uri.scheme == "file") { "La fotografía debe usar una URI content:// o file:// válida." }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        open(uri).use { stream -> BitmapFactory.decodeStream(stream, null, bounds) }
        require(bounds.outWidth > 0 && bounds.outHeight > 0) { "No se pudo leer la fotografía seleccionada." }
        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
            inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight)
        }
        val decoded = open(uri).use { stream -> BitmapFactory.decodeStream(stream, null, options) }
            ?: error("No se pudo decodificar la fotografía seleccionada.")
        return rotateIfNeeded(decoded, readOrientation(uri))
    }

    private fun open(uri: Uri) = context.contentResolver.openInputStream(uri)
        ?: error("No se pudo abrir la fotografía seleccionada.")

    private fun readOrientation(uri: Uri): Int = runCatching {
        open(uri).use { ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL) }
    }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

    private fun sampleSize(width: Int, height: Int): Int {
        val targetLongEdge = max(MealShareTemplateSpecs.exportWidth, MealShareTemplateSpecs.exportHeight) * 2
        var sample = 1
        while (max(width / (sample * 2), height / (sample * 2)) >= targetLongEdge) sample *= 2
        return sample
    }

    private fun rotateIfNeeded(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix().apply {
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> postScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    postRotate(90f)
                    postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    postRotate(270f)
                    postScale(-1f, 1f)
                }
            }
        }
        if (matrix.isIdentity) return bitmap
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true).also { bitmap.recycle() }
    }

    private fun renderBitmap(source: Bitmap, request: MealShareRenderRequest): Bitmap {
        val width = MealShareTemplateSpecs.exportWidth
        val height = MealShareTemplateSpecs.exportHeight
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        Canvas(output).apply {
            val crop = centeredCropRect(source.width, source.height, MealShareTemplateSpecs.aspectRatio)
            drawBitmap(source, Rect(crop.left, crop.top, crop.left + crop.width, crop.top + crop.height), Rect(0, 0, width, height), Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
            when (request.template) {
                MealShareTemplate.MINIMAL -> drawMinimal(request.overlayData)
                MealShareTemplate.PERFORMANCE -> drawPerformance(request.overlayData)
                MealShareTemplate.EDITORIAL -> drawEditorial(request.overlayData)
            }
        }
        return output
    }

    private fun Canvas.drawMinimal(data: MealShareOverlayData) {
        val spec = MealShareTemplateSpecs.forTemplate(MealShareTemplate.MINIMAL)
        val cardWidth = (width * spec.overlayWidthFraction).toInt()
        val cardHeight = (height * spec.overlayHeightFraction).toInt()
        val left = (width * spec.overlayLeftFraction).toInt()
        val top = (height * spec.overlayTopFraction).toInt()
        drawRoundRect(RectF(left.toFloat(), top.toFloat(), (left + cardWidth).toFloat(), (top + cardHeight).toFloat()), 32f, 32f, paint(Color.argb(217, 26, 26, 31)))
        val inset = (cardWidth * .08f).toInt()
        drawBranding(left + inset, top + (cardHeight * .08f).toInt(), (cardWidth * .5f).toInt(), (cardHeight * .15f).toInt())
        drawTextFitted(data.title, (left + inset).toFloat(), top + cardHeight * .43f, cardWidth - inset * 2f, cardHeight * .13f, Color.WHITE, Typeface.BOLD)
        drawTextFitted(data.caloriesLabel, (left + inset).toFloat(), top + cardHeight * .66f, cardWidth - inset * 2f, cardHeight * .20f, Color.WHITE, Typeface.BOLD)
        drawMacroRow(data, (left + inset).toFloat(), top + cardHeight * .88f, cardWidth - inset * 2f, Color.WHITE, cardHeight * .09f)
    }

    private fun Canvas.drawPerformance(data: MealShareOverlayData) {
        val spec = MealShareTemplateSpecs.forTemplate(MealShareTemplate.PERFORMANCE)
        val panelWidth = (width * spec.overlayWidthFraction).toInt()
        val panelHeight = (height * spec.overlayHeightFraction).toInt()
        val left = (width * spec.overlayLeftFraction).toInt()
        val top = (height * spec.overlayTopFraction).toInt()
        drawRect(left.toFloat(), top.toFloat(), (left + panelWidth).toFloat(), (top + panelHeight).toFloat(), paint(Color.argb(232, 11, 11, 16)))
        val inset = (panelWidth * .045f).toInt()
        drawTextFitted(data.title, (left + inset).toFloat(), top + panelHeight * .24f, panelWidth * .70f, panelHeight * .14f, Color.WHITE, Typeface.BOLD)
        drawTextFitted(data.calories.toString(), (left + inset).toFloat(), top + panelHeight * .60f, panelWidth * .48f, panelHeight * .32f, PURPLE, Typeface.BOLD)
        drawTextFitted("kcal", left + panelWidth * .48f, top + panelHeight * .60f, panelWidth * .16f, panelHeight * .12f, Color.WHITE, Typeface.NORMAL)
        drawMacroRow(data, (left + inset).toFloat(), top + panelHeight * .87f, panelWidth * .74f, Color.WHITE, panelHeight * .12f)
        drawMark(left + panelWidth - (panelWidth * .14f).toInt(), top + (panelHeight * .20f).toInt(), (panelWidth * .10f).toInt())
    }

    private fun Canvas.drawEditorial(data: MealShareOverlayData) {
        val spec = MealShareTemplateSpecs.forTemplate(MealShareTemplate.EDITORIAL)
        val cardWidth = (width * spec.overlayWidthFraction).toInt()
        val cardHeight = (height * spec.overlayHeightFraction).toInt()
        val left = (width * spec.overlayLeftFraction).toInt()
        val top = (height * spec.overlayTopFraction).toInt()
        drawRoundRect(RectF(left.toFloat(), top.toFloat(), (left + cardWidth).toFloat(), (top + cardHeight).toFloat()), 30f, 30f, paint(Color.argb(247, 255, 255, 255)))
        val inset = (cardWidth * .06f).toInt()
        drawBranding(left + inset, top + (cardHeight * .06f).toInt(), (cardWidth * .55f).toInt(), (cardHeight * .10f).toInt())
        drawTextFitted(data.title, (left + inset).toFloat(), top + cardHeight * .30f, cardWidth - inset * 2f, cardHeight * .08f, INK, Typeface.BOLD)
        drawTextFitted(data.caloriesLabel, (left + inset).toFloat(), top + cardHeight * .43f, cardWidth - inset * 2f, cardHeight * .12f, INK, Typeface.BOLD)
        drawMacroRow(data, (left + inset).toFloat(), top + cardHeight * .57f, cardWidth - inset * 2f, INK, cardHeight * .045f)
        drawTextFitted(data.mealTypeLabel.uppercase(), (left + inset).toFloat(), top + cardHeight * .93f, cardWidth - inset * 2f, cardHeight * .035f, SLATE, Typeface.BOLD)
    }

    private fun Canvas.drawMacroRow(data: MealShareOverlayData, left: Float, baseline: Float, availableWidth: Float, color: Int, textSize: Float) {
        val values = listOf(data.proteinLabel, data.carbohydratesLabel, data.fatLabel)
        val column = availableWidth / values.size
        values.forEachIndexed { index, value ->
            drawTextFitted(value, left + column * index, baseline, column - 8f, textSize, color, Typeface.BOLD)
        }
    }

    private fun Canvas.drawBranding(left: Int, top: Int, targetWidth: Int, targetHeight: Int) {
        ContextCompat.getDrawable(context, R.drawable.kyvo_logo)?.let { drawable ->
            val ratio = min(targetWidth.toFloat() / drawable.intrinsicWidth.coerceAtLeast(1), targetHeight.toFloat() / drawable.intrinsicHeight.coerceAtLeast(1))
            val width = (drawable.intrinsicWidth * ratio).toInt()
            val height = (drawable.intrinsicHeight * ratio).toInt()
            drawable.setBounds(left, top, left + width, top + height)
            drawable.draw(this)
        }
    }

    private fun Canvas.drawMark(left: Int, top: Int, size: Int) {
        ContextCompat.getDrawable(context, R.drawable.kyvo_mark_watermark)?.let { drawable ->
            drawable.setBounds(left, top, left + size, top + size)
            drawable.draw(this)
        }
    }

    private fun Canvas.drawTextFitted(text: String, x: Float, baseline: Float, maxWidth: Float, initialSize: Float, color: Int, style: Int) {
        val textPaint = paint(color).apply {
            typeface = Typeface.create("sans-serif", style)
            textSize = initialSize
        }
        if (textPaint.measureText(text) > maxWidth) textPaint.textSize = initialSize * (maxWidth / textPaint.measureText(text)).coerceAtLeast(.55f)
        drawText(text, x, baseline, textPaint)
    }

    private fun paint(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }

    private fun writeOutput(bitmap: Bitmap, fingerprint: String): Result<MealShareRenderResult> {
        val directory = File(context.cacheDir, "meal_share/rendered").apply { mkdirs() }
        check(directory.isDirectory) { "No se pudo crear el directorio temporal para Meal Share." }
        val name = "meal_share_render_${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg"
        val output = File(directory, name)
        val partial = File(directory, "$name.partial")
        return try {
            FileOutputStream(partial).use { stream ->
                check(bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream)) { "No se pudo codificar la imagen Meal Share." }
            }
            check(partial.renameTo(output)) { "No se pudo finalizar la imagen Meal Share." }
            verifyOutput(output, bitmap.width, bitmap.height)
            cleanupSupersededOutputs(directory, output)
            Result.success(MealShareRenderResult(output, bitmap.width, bitmap.height, fingerprint))
        } catch (error: Exception) {
            partial.delete()
            output.delete()
            Result.failure(error)
        }
    }

    private fun verifyOutput(file: File, expectedWidth: Int, expectedHeight: Int) {
        check(file.extension.equals("jpg", ignoreCase = true) && file.length() > 0L) { "El archivo JPEG generado no es válido." }
        file.inputStream().use { stream ->
            check(stream.read() == 0xFF && stream.read() == 0xD8) { "El archivo generado no tiene cabecera JPEG." }
        }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, bounds)
        check(bounds.outWidth == expectedWidth && bounds.outHeight == expectedHeight) { "La imagen Meal Share generada tiene dimensiones inesperadas." }
    }

    private fun cleanupSupersededOutputs(directory: File, keep: File) {
        directory.listFiles()?.forEach { candidate ->
            val isOwnedRender = candidate.name.startsWith("meal_share_render_") && (candidate.extension == "jpg" || candidate.extension == "partial")
            if (isOwnedRender && candidate != keep) candidate.delete()
        }
    }

    private companion object {
        const val JPEG_QUALITY = 95
        const val PURPLE = 0xFF8B5CF6.toInt()
        const val INK = 0xFF101323.toInt()
        const val SLATE = 0xFF667085.toInt()
    }
}
