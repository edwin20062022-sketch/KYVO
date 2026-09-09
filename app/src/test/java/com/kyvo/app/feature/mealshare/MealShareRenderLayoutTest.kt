package com.kyvo.app.feature.mealshare

import com.kyvo.app.feature.mealshare.domain.model.MealShareTemplate
import com.kyvo.app.feature.mealshare.domain.model.MealShareTemplateSpecs
import com.kyvo.app.feature.mealshare.domain.model.MealShareDraft
import com.kyvo.app.feature.mealshare.domain.model.renderFingerprint
import com.kyvo.app.feature.home.domain.model.MealItem
import com.kyvo.app.feature.home.domain.model.MealType
import com.kyvo.app.feature.mealshare.domain.render.centeredCropRect
import com.kyvo.app.feature.mealshare.domain.render.MealShareRenderResult
import com.kyvo.app.feature.mealshare.presentation.MEAL_SHARE_PREVIEW_ASPECT_RATIO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.roundToInt
import java.io.File

class MealShareRenderLayoutTest {
    @Test
    fun allTemplatesHaveValidSharedExportSpecs() {
        assertEquals(MEAL_SHARE_PREVIEW_ASPECT_RATIO, MealShareTemplateSpecs.aspectRatio)
        MealShareTemplate.entries.forEach { template ->
            val spec = MealShareTemplateSpecs.forTemplate(template)
            assertEquals(MealShareTemplateSpecs.aspectRatio, spec.aspectRatio)
            assertTrue(spec.overlayLeftFraction in 0f..1f)
            assertTrue(spec.overlayTopFraction in 0f..1f)
            assertTrue(spec.overlayWidthFraction in 0f..1f)
            assertTrue(spec.overlayHeightFraction in 0f..1f)
            assertTrue(spec.overlayLeftFraction + spec.overlayWidthFraction <= 1f)
            assertTrue(spec.overlayTopFraction + spec.overlayHeightFraction <= 1f)
        }
        assertEquals((MealShareTemplateSpecs.exportWidth / MealShareTemplateSpecs.aspectRatio).roundToInt(), MealShareTemplateSpecs.exportHeight)
    }

    @Test
    fun centeredCropCoversTargetForPortraitLandscapeSquareAndMatchingRatios() {
        val portrait = centeredCropRect(2000, 4000, MealShareTemplateSpecs.aspectRatio)
        val landscape = centeredCropRect(4000, 2000, MealShareTemplateSpecs.aspectRatio)
        val square = centeredCropRect(2000, 2000, MealShareTemplateSpecs.aspectRatio)
        val matching = centeredCropRect(2320, 2000, MealShareTemplateSpecs.aspectRatio)

        assertEquals(2000, portrait.width)
        assertTrue(portrait.height < 4000)
        assertEquals(2000, landscape.height)
        assertTrue(landscape.width < 4000)
        assertEquals(2000, square.width)
        assertTrue(square.height < 2000)
        assertEquals(0, matching.left)
        assertEquals(0, matching.top)
        assertEquals(2320, matching.width)
        assertEquals(2000, matching.height)
    }

    @Test
    fun fingerprintChangesOnlyWhenRenderRelevantDraftDataChanges() {
        val draft = MealShareDraft(
            photoUri = "file:///cache/photo.jpg",
            mealType = MealType.Lunch,
            items = listOf(MealItem("one", "Pollo", 100.0, "g", 640, 42, 68, 21)),
        )
        val fingerprint = draft.renderFingerprint()
        assertEquals(fingerprint, draft.renderFingerprint())
        assertTrue(fingerprint != draft.copy(template = MealShareTemplate.PERFORMANCE).renderFingerprint())
        assertTrue(fingerprint != draft.copy(photoUri = "content://picker/other").renderFingerprint())
        assertTrue(fingerprint != draft.copy(mealType = MealType.Dinner).renderFingerprint())
        assertTrue(fingerprint != draft.copy(items = draft.items.map { it.copy(quantity = 150.0, calories = 960) }).renderFingerprint())
    }

    @Test
    fun renderResultUsesTheCanonicalOutputContract() {
        val result = MealShareRenderResult(File("/cache/meal_share/rendered/result.jpg"), MealShareTemplateSpecs.exportWidth, MealShareTemplateSpecs.exportHeight, "draft")
        assertEquals(1080, result.width)
        assertEquals(931, result.height)
        assertTrue(result.file.extension.equals("jpg", ignoreCase = true))
    }
}
