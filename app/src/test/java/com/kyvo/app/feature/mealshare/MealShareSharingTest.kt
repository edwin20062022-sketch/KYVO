package com.kyvo.app.feature.mealshare

import com.kyvo.app.feature.mealshare.domain.model.MealShareDraft
import com.kyvo.app.feature.mealshare.domain.model.renderFingerprint
import com.kyvo.app.feature.mealshare.presentation.MEAL_SHARE_RENDER_RETENTION_MILLIS
import com.kyvo.app.feature.mealshare.presentation.cleanupExpiredMealShareRenders
import com.kyvo.app.feature.mealshare.presentation.shareableRender
import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MealShareSharingTest {
    @Test
    fun validCurrentRenderInsideOwnedDirectoryIsShareable() {
        val directory = Files.createTempDirectory("meal-share-rendered").toFile()
        val draft = MealShareDraft(photoUri = "content://picker/photo")
        val file = File(directory, "meal_share_render_valid.jpg").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        val rendered = draft.copy(renderedImagePath = file.absolutePath, renderedFingerprint = draft.renderFingerprint())

        assertEquals(file.canonicalFile, rendered.shareableRender(directory).getOrThrow())
    }

    @Test
    fun staleAndOutsideRendersAreRejected() {
        val directory = Files.createTempDirectory("meal-share-rendered").toFile()
        val outside = File.createTempFile("meal_share_render_", ".jpg").apply { writeBytes(byteArrayOf(1)) }
        val draft = MealShareDraft(photoUri = "content://picker/photo")

        assertTrue(draft.copy(renderedImagePath = outside.absolutePath, renderedFingerprint = draft.renderFingerprint()).shareableRender(directory).isFailure)
        assertTrue(draft.copy(renderedImagePath = outside.absolutePath, renderedFingerprint = "stale").shareableRender(directory).isFailure)
    }

    @Test
    fun retentionKeepsRecentRenderAndRemovesExpiredOwnedRenderOnly() {
        val directory = Files.createTempDirectory("meal-share-rendered").toFile()
        val now = 1_000_000_000L
        val recent = File(directory, "meal_share_render_recent.jpg").apply { writeBytes(byteArrayOf(1)); setLastModified(now - MEAL_SHARE_RENDER_RETENTION_MILLIS) }
        val expired = File(directory, "meal_share_render_expired.jpg").apply { writeBytes(byteArrayOf(1)); setLastModified(now - MEAL_SHARE_RENDER_RETENTION_MILLIS - 1) }
        val unrelated = File(directory, "other.jpg").apply { writeBytes(byteArrayOf(1)); setLastModified(0L) }

        cleanupExpiredMealShareRenders(directory, now)

        assertTrue(recent.exists())
        assertFalse(expired.exists())
        assertTrue(unrelated.exists())
    }
}
