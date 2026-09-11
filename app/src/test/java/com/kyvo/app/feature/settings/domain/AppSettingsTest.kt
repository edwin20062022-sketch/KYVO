package com.kyvo.app.feature.settings.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppSettingsTest {
    @Test fun defaultsUseCurrentKyvoUnitsAndSystemTheme() {
        assertEquals(WeightUnit.KILOGRAMS, AppSettings().units.weight)
        assertEquals(HeightUnit.CENTIMETERS, AppSettings().units.height)
        assertEquals(FoodUnit.GRAMS, AppSettings().units.food)
        assertEquals(TemperatureUnit.CELSIUS, AppSettings().units.temperature)
        assertEquals(AppearanceMode.SYSTEM, AppSettings().appearance)
    }

    @Test fun appearanceResolvesAgainstSystemOnlyForAutomatic() {
        assertTrue(AppearanceMode.SYSTEM.resolveDarkTheme(true))
        assertFalse(AppearanceMode.SYSTEM.resolveDarkTheme(false))
        assertTrue(AppearanceMode.DARK.resolveDarkTheme(false))
        assertFalse(AppearanceMode.LIGHT.resolveDarkTheme(true))
    }

    @Test fun conversionsAreRoundedOnlyForDisplay() {
        assertEquals(176.4, kilogramsToPounds(80.0), 0.1)
        assertEquals(80.0, poundsToKilograms(176.4), 0.1)
        assertEquals(5 to 11, centimetersToFeetInches(180.0))
    }
}
