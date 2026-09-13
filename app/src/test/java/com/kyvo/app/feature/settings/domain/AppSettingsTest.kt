package com.kyvo.app.feature.settings.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppSettingsTest {
    @Test fun defaultsUseCurrentKyvoUnitsAndLightTheme() {
        assertEquals(WeightUnit.KILOGRAMS, AppSettings().units.weight)
        assertEquals(HeightUnit.CENTIMETERS, AppSettings().units.height)
        assertEquals(FoodUnit.GRAMS, AppSettings().units.food)
        assertEquals(TemperatureUnit.CELSIUS, AppSettings().units.temperature)
        assertEquals(AppearanceMode.LIGHT, AppSettings().appearance)
    }

    @Test fun appearanceResolverUsesSystemOnlyForAutomatic() {
        assertFalse(AppearanceMode.LIGHT.resolveDarkTheme(false))
        assertFalse(AppearanceMode.LIGHT.resolveDarkTheme(true))
        assertTrue(AppearanceMode.DARK.resolveDarkTheme(false))
        assertTrue(AppearanceMode.DARK.resolveDarkTheme(true))
        assertFalse(AppearanceMode.SYSTEM.resolveDarkTheme(false))
        assertTrue(AppearanceMode.SYSTEM.resolveDarkTheme(true))
    }

    @Test fun conversionsAreRoundedOnlyForDisplay() {
        assertEquals(176.4, kilogramsToPounds(80.0), 0.1)
        assertEquals(80.0, poundsToKilograms(176.4), 0.1)
        assertEquals(5 to 11, centimetersToFeetInches(180.0))
    }
}
