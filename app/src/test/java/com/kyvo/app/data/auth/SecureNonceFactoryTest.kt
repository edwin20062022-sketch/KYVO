package com.kyvo.app.data.auth

import java.security.MessageDigest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SecureNonceFactoryTest {
    @Test
    fun `nonce is random url safe and hashed with SHA-256 hex`() {
        val factory = SecureNonceFactory()
        val first = factory.create()
        val second = factory.create()
        assertNotEquals(first.raw, second.raw)
        assertTrue(first.raw.matches(Regex("[A-Za-z0-9_-]+")))
        assertEquals(64, first.sha256.length)
        assertEquals(sha256(first.raw), first.sha256)
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
}
