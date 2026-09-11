package com.kyvo.app.feature.settings.domain

enum class WeightUnit(val label: String) { KILOGRAMS("kg"), POUNDS("lb") }
enum class HeightUnit(val label: String) { CENTIMETERS("cm"), FEET_INCHES("ft / in") }
enum class FoodUnit(val label: String) { GRAMS("gramos"), OUNCES("onzas") }
enum class TemperatureUnit(val label: String) { CELSIUS("°C"), FAHRENHEIT("°F") }
enum class AppearanceMode(val label: String) { LIGHT("Claro"), DARK("Oscuro"), SYSTEM("Automático") }

data class NotificationPreferences(
    val enabled: Boolean = true,
    val breakfast: Boolean = true,
    val lunch: Boolean = true,
    val snack: Boolean = true,
    val dinner: Boolean = true,
    val dayClose: Boolean = true,
    val newFeatures: Boolean = true,
    val tipsAndContent: Boolean = true,
    val accountNotices: Boolean = true,
)

data class UnitPreferences(
    val weight: WeightUnit = WeightUnit.KILOGRAMS,
    val height: HeightUnit = HeightUnit.CENTIMETERS,
    val food: FoodUnit = FoodUnit.GRAMS,
    val temperature: TemperatureUnit = TemperatureUnit.CELSIUS,
)

data class AppSettings(
    val units: UnitPreferences = UnitPreferences(),
    val appearance: AppearanceMode = AppearanceMode.SYSTEM,
    val reduceBrightnessInDarkMode: Boolean = true,
    val highContrast: Boolean = false,
    val notifications: NotificationPreferences = NotificationPreferences(),
)

fun AppearanceMode.resolveDarkTheme(systemDark: Boolean): Boolean = when (this) {
    AppearanceMode.LIGHT -> false
    AppearanceMode.DARK -> true
    AppearanceMode.SYSTEM -> systemDark
}

fun kilogramsToPounds(value: Double): Double = value * 2.2046226218
fun poundsToKilograms(value: Double): Double = value / 2.2046226218
fun centimetersToFeetInches(value: Double): Pair<Int, Int> {
    val totalInches = value / 2.54
    val feet = totalInches.toInt() / 12
    val inches = kotlin.math.round(totalInches - feet * 12).toInt()
    return if (inches == 12) feet + 1 to 0 else feet to inches
}
