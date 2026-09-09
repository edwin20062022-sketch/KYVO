package com.kyvo.app.feature.mealshare

import com.kyvo.app.feature.mealshare.domain.model.MealShareTemplate
import com.kyvo.app.feature.mealshare.domain.model.MealShareTemplateSpecs
import com.kyvo.app.feature.mealshare.domain.render.centeredCropRect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.roundToInt

class MealShareRenderLayoutTest {
    @Test
    fun allTemplatesHaveValidSharedExportSpecs() {
        MealShareTemplate.entries.forEach { template ->
            val spec = MealShareTemplateSpecs.forTemplate(template)
            assertEquals(MealShareTemplateSpecs.aspectRatio, spec.aspectRatio)
            assertTrue(spec.overlayWidthFraction in 0f..1f)
            assertTrue(spec.overlayHeightFraction in 0f..1f)
            assertTrue(spec.outerMarginFraction in 0f..1f)
        }
        assertEquals((MealShareTemplateSpecs.exportWidth / MealShareTemplateSpecs.aspectRatio).roundToInt(), MealShareTemplateSpecs.exportHeight)
    }

    @Test
    fun centeredCropCoversTargetForPortraitLandscapeSquareAndMatchingRatios() {
        val portrait = centeredCropRect(2000, 4000, MealShareTemplateSpecs.aspectRatio)
        val landscape = centeredCropRect(4000, 2000, MealShareTemplateSpecs.aspectRatio)
        val square = centeredCropRect(2000, 2000, MealShareTemplateSpecs.aspectRatio)
        val matching = centeredCropRect(2100, 2000, MealShareTemplateSpecs.aspectRatio)

        assertEquals(2000, portrait.width)
        assertTrue(portrait.height < 4000)
        assertEquals(2000, landscape.height)
        assertTrue(landscape.width < 4000)
        assertEquals(2000, square.width)
        assertTrue(square.height < 2000)
        assertEquals(0, matching.left)
        assertEquals(0, matching.top)
        assertEquals(2100, matching.width)
        assertEquals(2000, matching.height)
    }
}
