package com.kyvo.app.core.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KyvoDestinationTest {
    @Test
    fun `all routes are unique and non blank`() {
        val routes = KyvoDestination.entries.map(KyvoDestination::route)

        assertEquals(routes.size, routes.distinct().size)
        assertTrue(routes.none(String::isBlank))
    }

    @Test
    fun `main navigation keeps approved order`() {
        assertEquals(
            listOf("home", "meals", "meal_share", "progress", "profile"),
            mainDestinations.map(KyvoDestination::route),
        )
    }
}

