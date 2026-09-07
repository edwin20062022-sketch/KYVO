package com.kyvo.app.feature.onboarding.data

import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingPreferencePrefixTest {
    @Test
    fun `onboarding keys are namespaced by authenticated user id`() {
        val firstUser = onboardingPreferencePrefix("user-a")
        val secondUser = onboardingPreferencePrefix("user-b")

        assertNotEquals(firstUser, secondUser)
        assertTrue(firstUser.startsWith("user_user-a_"))
        assertTrue(secondUser.startsWith("user_user-b_"))
    }
}
