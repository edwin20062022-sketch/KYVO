package com.kyvo.app.feature.launch.presentation

import com.kyvo.app.domain.auth.AuthProvider
import com.kyvo.app.domain.auth.AuthSession
import com.kyvo.app.domain.auth.AuthState
import com.kyvo.app.R
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LaunchStartupTest {
    @Test
    fun `initializing session keeps launch cover visible`() {
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

    @Test
    fun `ready launch cover transition is stable and only happens once`() {
        assertTrue(shouldShowLaunchCover(destinationReady = true, minimumLaunchDurationElapsed = false))
        assertFalse(shouldShowLaunchCover(destinationReady = true, minimumLaunchDurationElapsed = true))
        assertFalse(shouldShowLaunchCover(destinationReady = true, minimumLaunchDurationElapsed = true))
    }

    @Test
    fun `system splash uses a dedicated resource instead of the launcher icon`() {
        assertNotEquals(R.mipmap.ic_launcher, R.drawable.kyvo_splash_icon)
    }

    private fun signedIn() = AuthState.SignedIn(
        AuthSession("user-1", "athlete@kyvo.app", AuthProvider.Email),
    )
}
