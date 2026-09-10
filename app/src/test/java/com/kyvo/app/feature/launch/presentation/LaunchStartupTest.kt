package com.kyvo.app.feature.launch.presentation

import com.kyvo.app.domain.auth.AuthProvider
import com.kyvo.app.domain.auth.AuthSession
import com.kyvo.app.domain.auth.AuthState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LaunchStartupTest {
    @Test
    fun `initializing session is not ready for routing`() {
        assertFalse(isLaunchDestinationReady(AuthState.Initializing, onboardingLoaded = false))
    }

    @Test
    fun `signed out session is ready for login`() {
        assertTrue(isLaunchDestinationReady(AuthState.SignedOut, onboardingLoaded = false))
    }

    @Test
    fun `signed in session waits for onboarding before routing`() {
        assertFalse(isLaunchDestinationReady(signedIn(), onboardingLoaded = false))
        assertTrue(isLaunchDestinationReady(signedIn(), onboardingLoaded = true))
    }

    private fun signedIn() = AuthState.SignedIn(
        AuthSession("user-1", "athlete@kyvo.app", AuthProvider.Email),
    )
}
