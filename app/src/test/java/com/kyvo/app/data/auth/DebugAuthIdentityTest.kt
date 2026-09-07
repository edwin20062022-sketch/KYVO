package com.kyvo.app.data.auth

import com.kyvo.app.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class DebugAuthIdentityTest {
    @Test
    fun `debug application id and callback scheme remain isolated from production`() {
        assertEquals("com.kyvo.app.debug", BuildConfig.APPLICATION_ID)
        assertEquals("com.kyvo.app.debug", BuildConfig.AUTH_SCHEME)
    }
}
