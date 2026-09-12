package com.kyvo.app.feature.auth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ConfirmationEmailTemplateTest {
    private val templateFile: File by lazy {
        val candidates = listOf(
            File("../../supabase/templates/confirmation.html"),
            File("../supabase/templates/confirmation.html"),
            File("supabase/templates/confirmation.html"),
        )
        candidates.firstOrNull { it.exists() }
            ?: File("supabase/templates/confirmation.html")
    }

    @Test
    fun templateFileExists() {
        assertTrue(
            "confirmation.html must exist at supabase/templates/",
            templateFile.exists(),
        )
    }

    @Test
    fun templateContainsConfirmationUrlVariable() {
        val html = templateFile.readText()
        assertTrue(
            "Template must use Supabase official {{ .ConfirmationURL }} variable",
            html.contains("{{ .ConfirmationURL }}"),
        )
    }

    @Test
    fun templateContainsKYVOBranding() {
        val html = templateFile.readText()
        assertTrue("Template must contain KYVO branding", html.contains("KYVO"))
    }

    @Test
    fun templateContainsSpanishCopy() {
        val html = templateFile.readText()
        assertTrue(
            "Template must contain Spanish CTA 'Confirmar correo'",
            html.contains("Confirmar correo"),
        )
        assertTrue(
            "Template must contain Spanish subject reference",
            html.contains("Confirma tu correo"),
        )
    }

    @Test
    fun templateDoesNotMentionSupabase() {
        val html = templateFile.readText()
        val lower = html.lowercase()
        assertFalse(
            "Template must not mention Supabase to the user",
            lower.contains("supabase"),
        )
    }

    @Test
    fun templateDoesNotContainLocalhost() {
        val html = templateFile.readText()
        val lower = html.lowercase()
        assertFalse(
            "Template must not contain localhost URLs",
            lower.contains("localhost"),
        )
    }

    @Test
    fun templateDoesNotContainWindowsPaths() {
        val html = templateFile.readText()
        assertFalse(
            "Template must not contain Windows file paths",
            html.contains("C:\\"),
        )
        assertFalse(
            "Template must not contain Windows forward-slash paths",
            html.contains("C:/"),
        )
    }

    @Test
    fun templateDoesNotContainHardcodedTokens() {
        val html = templateFile.readText()
        assertFalse(
            "Template must not contain hardcoded JWT tokens",
            html.contains("eyJ"),
        )
    }

    @Test
    fun templateHasFallbackLink() {
        val html = templateFile.readText()
        assertTrue(
            "Template must include a fallback text link for the confirmation URL",
            html.contains("{{ .ConfirmationURL }}") && html.contains("enlace"),
        )
    }

    @Test
    fun templateHasDoctype() {
        val html = templateFile.readText()
        assertTrue("Template must have DOCTYPE declaration", html.contains("<!DOCTYPE html>"))
    }

    @Test
    fun templateHasMaxWidthConstraint() {
        val html = templateFile.readText()
        assertTrue(
            "Template must have max-width for email client compatibility",
            html.contains("max-width"),
        )
    }
}
