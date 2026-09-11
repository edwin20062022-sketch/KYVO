package com.kyvo.app.feature.settings.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kyvo.app.feature.settings.domain.AppearanceMode
import com.kyvo.app.feature.settings.domain.AppSettings
import com.kyvo.app.feature.settings.domain.FoodUnit
import com.kyvo.app.feature.settings.domain.HeightUnit
import com.kyvo.app.feature.settings.domain.TemperatureUnit
import com.kyvo.app.feature.settings.domain.UnitPreferences
import com.kyvo.app.feature.settings.domain.WeightUnit
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.appSettingsDataStore by preferencesDataStore(name = "kyvo_app_settings")

interface AppSettingsRepository {
    fun observe(): Flow<AppSettings>
    suspend fun setWeightUnit(unit: WeightUnit)
    suspend fun setHeightUnit(unit: HeightUnit)
    suspend fun setFoodUnit(unit: FoodUnit)
    suspend fun setTemperatureUnit(unit: TemperatureUnit)
    suspend fun setAppearance(mode: AppearanceMode)
    suspend fun setReduceBrightnessInDarkMode(enabled: Boolean)
    suspend fun setHighContrast(enabled: Boolean)
}

class DataStoreAppSettingsRepository(context: Context) : AppSettingsRepository {
    private val dataStore = context.applicationContext.appSettingsDataStore

    override fun observe(): Flow<AppSettings> = dataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map(::decode)

    override suspend fun setWeightUnit(unit: WeightUnit) = edit { it[Keys.Weight] = unit.name }
    override suspend fun setHeightUnit(unit: HeightUnit) = edit { it[Keys.Height] = unit.name }
    override suspend fun setFoodUnit(unit: FoodUnit) = edit { it[Keys.Food] = unit.name }
    override suspend fun setTemperatureUnit(unit: TemperatureUnit) = edit { it[Keys.Temperature] = unit.name }
    override suspend fun setAppearance(mode: AppearanceMode) = edit { it[Keys.Appearance] = mode.name }
    override suspend fun setReduceBrightnessInDarkMode(enabled: Boolean) = edit { it[Keys.ReduceBrightness] = enabled }
    override suspend fun setHighContrast(enabled: Boolean) = edit { it[Keys.HighContrast] = enabled }

    private suspend fun edit(block: (MutableMap<Preferences.Key<*>, Any>) -> Unit) {
        dataStore.edit { preferences ->
            val values = LinkedHashMap<Preferences.Key<*>, Any>()
            block(values)
            values.forEach { (key, value) ->
                @Suppress("UNCHECKED_CAST")
                preferences[key as Preferences.Key<Any>] = value
            }
        }
    }

    private fun decode(preferences: Preferences) = AppSettings(
        units = UnitPreferences(
            weight = preferences[Keys.Weight].enumOr(WeightUnit.KILOGRAMS),
            height = preferences[Keys.Height].enumOr(HeightUnit.CENTIMETERS),
            food = preferences[Keys.Food].enumOr(FoodUnit.GRAMS),
            temperature = preferences[Keys.Temperature].enumOr(TemperatureUnit.CELSIUS),
        ),
        appearance = preferences[Keys.Appearance].enumOr(AppearanceMode.SYSTEM),
        reduceBrightnessInDarkMode = preferences[Keys.ReduceBrightness] ?: true,
        highContrast = preferences[Keys.HighContrast] ?: false,
    )

    private object Keys {
        val Weight = stringPreferencesKey("weight_unit")
        val Height = stringPreferencesKey("height_unit")
        val Food = stringPreferencesKey("food_unit")
        val Temperature = stringPreferencesKey("temperature_unit")
        val Appearance = stringPreferencesKey("appearance_mode")
        val ReduceBrightness = booleanPreferencesKey("reduce_brightness_dark_mode")
        val HighContrast = booleanPreferencesKey("high_contrast")
    }
}

private inline fun <reified T : Enum<T>> String?.enumOr(default: T): T =
    this?.let { value -> enumValues<T>().firstOrNull { it.name == value } } ?: default
