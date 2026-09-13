package com.kyvo.app.core.designsystem.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KyvoUserAvatarInitialTest {
    @Test
    fun `Edwin yields E`() = assertEquals("E", extractInitial("Edwin"))

    @Test
    fun `Edwin Beta yields E`() = assertEquals("E", extractInitial("Edwin Beta"))

    @Test
    fun `maria yields M with uppercase`() = assertEquals("M", extractInitial("maría"))

    @Test
    fun `leading spaces trimmed then G`() = assertEquals("G", extractInitial("  Giovanni"))

    @Test
    fun `empty name yields null`() = assertNull(extractInitial(""))

    @Test
    fun `blank name yields null`() = assertNull(extractInitial("   "))

    @Test
    fun `single char yields itself uppercase`() = assertEquals("A", extractInitial("a"))

    @Test
    fun `null name yields null`() = assertNull(extractInitial(null))

    private fun extractInitial(name: String?): String? = name?.trim()?.firstOrNull()?.uppercase().takeIf { !it.isNullOrBlank() }
}
