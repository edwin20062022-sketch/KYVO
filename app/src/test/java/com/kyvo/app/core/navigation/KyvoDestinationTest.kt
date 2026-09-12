package com.kyvo.app.core.navigation

import com.kyvo.app.domain.auth.AuthProvider
import com.kyvo.app.domain.auth.AuthSession
import com.kyvo.app.domain.auth.AuthState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KyvoDestinationTest {
    @Test
    fun `meal share context is carried through search results detail and portion`() {
        val context = FoodSelectionContext.MEAL_SHARE
        assertEquals("MEAL_SHARE", foodSearchRoute(context).substringAfter("selectionContext="))
        assertTrue(KyvoDestination.FoodResults.route.contains("selectionContext={selectionContext}"))
        assertTrue(KyvoDestination.FoodDetail.route.contains("selectionContext={selectionContext}"))
        assertTrue(KyvoDestination.FoodPortion.route.contains("selectionContext={selectionContext}"))
    }

    @Test
    fun `normal meal logging context remains distinct`() {
        assertEquals("NORMAL_MEAL_LOGGING", foodSearchRoute(FoodSelectionContext.NORMAL_MEAL_LOGGING).substringAfter("selectionContext="))
    }
    @Test
    fun `all routes are unique and non blank`() {
        val routes = KyvoDestination.entries.map(KyvoDestination::route)

        assertEquals(routes.size, routes.distinct().size)
        assertTrue(routes.none(String::isBlank))
    }

    @Test
    fun `main navigation keeps approved order`() {
        assertEquals(
            listOf("home", "meals", "plan", "progress", "profile"),
            mainDestinations.map(KyvoDestination::route),
        )
    }

    @Test
    fun `signed out session routes to login`() {
        assertEquals(KyvoDestination.Login, resolveStartDestination(AuthState.SignedOut, false))
    }

    @Test
    fun `existing session with incomplete onboarding routes to onboarding`() {
        assertEquals(KyvoDestination.Onboarding, resolveStartDestination(signedIn(), false))
    }

    @Test
    fun `existing session with complete onboarding routes to home placeholder`() {
        assertEquals(KyvoDestination.Home, resolveStartDestination(signedIn(), true))
    }

    private fun signedIn() = AuthState.SignedIn(
        AuthSession("user-1", "athlete@kyvo.app", AuthProvider.Email),
    )
}
