package com.kyvo.app.data.auth

import com.kyvo.app.domain.auth.AuthProvider
import com.kyvo.app.domain.auth.AuthSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthSessionAvatarTest {
    @Test
    fun `avatarUrl defaults to null`() {
        val session = AuthSession("u1", "a@b.com", AuthProvider.Email)
        assertNull(session.avatarUrl)
        assertNull(session.avatarVersion)
    }

    @Test
    fun `avatarUrl preserves old username metadata`() {
        val session = AuthSession("u1", "a@b.com", AuthProvider.Email, "Name", username = "old_user", avatarUrl = "https://example.com/avatar.jpg")
        assertEquals("old_user", session.username)
        assertEquals("https://example.com/avatar.jpg", session.avatarUrl)
    }

    @Test
    fun `blank avatarUrl treated as null`() {
        val url = "".takeIf(String::isNotBlank)
        assertNull(url)
    }

    @Test
    fun `avatarVersion defaults to null`() {
        val session = AuthSession("u1", "a@b.com", AuthProvider.Email)
        assertNull(session.avatarVersion)
    }

    @Test
    fun `avatarVersion persists after upload`() {
        val session = AuthSession("u1", "a@b.com", AuthProvider.Email, "Test", avatarUrl = "https://example.com/avatar.jpg", avatarVersion = "1726166400000")
        assertEquals("1726166400000", session.avatarVersion)
    }

    @Test
    fun `legacy session without avatarVersion works`() {
        val session = AuthSession("u1", "a@b.com", AuthProvider.Email, "Test", avatarUrl = "https://example.com/avatar.jpg")
        assertNull(session.avatarVersion)
    }
}
