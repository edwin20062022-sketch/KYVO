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
import com.kyvo.app.feature.mealshare.domain.model.MealShareTemplate
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
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setBreakfastNotifications(enabled: Boolean)
    suspend fun setLunchNotifications(enabled: Boolean)
    suspend fun setSnackNotifications(enabled: Boolean)
    suspend fun setDinnerNotifications(enabled: Boolean)
    suspend fun setDayCloseNotifications(enabled: Boolean)
    suspend fun setNewFeaturesNotifications(enabled: Boolean)
    suspend fun setTipsAndContentNotifications(enabled: Boolean)
    suspend fun setAccountNoticesNotifications(enabled: Boolean)
    suspend fun setMealShareTemplate(template: MealShareTemplate)
    suspend fun setMealShareShowCalories(enabled: Boolean)
    suspend fun setMealShareShowProtein(enabled: Boolean)
    suspend fun setMealShareShowCarbohydrates(enabled: Boolean)
    suspend fun setMealShareShowFat(enabled: Boolean)
    suspend fun setMealShareAutoSave(enabled: Boolean)
    suspend fun setMealShareSuggestInstagram(enabled: Boolean)
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
    override suspend fun setNotificationsEnabled(enabled: Boolean) = edit { it[Keys.NotificationsEnabled] = enabled }
    override suspend fun setBreakfastNotifications(enabled: Boolean) = edit { it[Keys.Breakfast] = enabled }
    override suspend fun setLunchNotifications(enabled: Boolean) = edit { it[Keys.Lunch] = enabled }
    override suspend fun setSnackNotifications(enabled: Boolean) = edit { it[Keys.Snack] = enabled }
    override suspend fun setDinnerNotifications(enabled: Boolean) = edit { it[Keys.Dinner] = enabled }
    override suspend fun setDayCloseNotifications(enabled: Boolean) = edit { it[Keys.DayClose] = enabled }
    override suspend fun setNewFeaturesNotifications(enabled: Boolean) = edit { it[Keys.NewFeatures] = enabled }
    override suspend fun setTipsAndContentNotifications(enabled: Boolean) = edit { it[Keys.TipsAndContent] = enabled }
    override suspend fun setAccountNoticesNotifications(enabled: Boolean) = edit { it[Keys.AccountNotices] = enabled }
    override suspend fun setMealShareTemplate(template: MealShareTemplate) = edit { it[Keys.MealShareTemplate] = template.name }
    override suspend fun setMealShareShowCalories(enabled: Boolean) = edit { it[Keys.MealShareCalories] = enabled }
    override suspend fun setMealShareShowProtein(enabled: Boolean) = edit { it[Keys.MealShareProtein] = enabled }
    override suspend fun setMealShareShowCarbohydrates(enabled: Boolean) = edit { it[Keys.MealShareCarbs] = enabled }
    override suspend fun setMealShareShowFat(enabled: Boolean) = edit { it[Keys.MealShareFat] = enabled }
    override suspend fun setMealShareAutoSave(enabled: Boolean) = edit { it[Keys.MealShareAutoSave] = enabled }
    override suspend fun setMealShareSuggestInstagram(enabled: Boolean) = edit { it[Keys.MealShareInstagram] = enabled }

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
        appearance = preferences[Keys.Appearance].enumOr(AppearanceMode.LIGHT),
        reduceBrightnessInDarkMode = preferences[Keys.ReduceBrightness] ?: true,
        highContrast = preferences[Keys.HighContrast] ?: false,
        notifications = com.kyvo.app.feature.settings.domain.NotificationPreferences(
            enabled = preferences[Keys.NotificationsEnabled] ?: true,
            breakfast = preferences[Keys.Breakfast] ?: true,
            lunch = preferences[Keys.Lunch] ?: true,
            snack = preferences[Keys.Snack] ?: true,
            dinner = preferences[Keys.Dinner] ?: true,
            dayClose = preferences[Keys.DayClose] ?: true,
            newFeatures = preferences[Keys.NewFeatures] ?: true,
            tipsAndContent = preferences[Keys.TipsAndContent] ?: true,
            accountNotices = preferences[Keys.AccountNotices] ?: true,
        ),
        mealShare = com.kyvo.app.feature.settings.domain.MealSharePreferences(
            defaultTemplate = preferences[Keys.MealShareTemplate].enumOr(MealShareTemplate.MINIMAL),
            showCalories = preferences[Keys.MealShareCalories] ?: true,
            showProtein = preferences[Keys.MealShareProtein] ?: true,
            showCarbohydrates = preferences[Keys.MealShareCarbs] ?: true,
            showFat = preferences[Keys.MealShareFat] ?: true,
            autoSave = preferences[Keys.MealShareAutoSave] ?: true,
            suggestInstagram = preferences[Keys.MealShareInstagram] ?: true,
        ),
    )

    private object Keys {
        val Weight = stringPreferencesKey("weight_unit")
        val Height = stringPreferencesKey("height_unit")
        val Food = stringPreferencesKey("food_unit")
        val Temperature = stringPreferencesKey("temperature_unit")
        val Appearance = stringPreferencesKey("appearance_mode")
        val ReduceBrightness = booleanPreferencesKey("reduce_brightness_dark_mode")
        val HighContrast = booleanPreferencesKey("high_contrast")
        val NotificationsEnabled = booleanPreferencesKey("notifications_enabled")
        val Breakfast = booleanPreferencesKey("notification_breakfast")
        val Lunch = booleanPreferencesKey("notification_lunch")
        val Snack = booleanPreferencesKey("notification_snack")
        val Dinner = booleanPreferencesKey("notification_dinner")
        val DayClose = booleanPreferencesKey("notification_day_close")
        val NewFeatures = booleanPreferencesKey("notification_new_features")
        val TipsAndContent = booleanPreferencesKey("notification_tips_content")
        val AccountNotices = booleanPreferencesKey("notification_account_notices")
        val MealShareTemplate = stringPreferencesKey("meal_share_template")
        val MealShareCalories = booleanPreferencesKey("meal_share_show_calories")
        val MealShareProtein = booleanPreferencesKey("meal_share_show_protein")
        val MealShareCarbs = booleanPreferencesKey("meal_share_show_carbohydrates")
        val MealShareFat = booleanPreferencesKey("meal_share_show_fat")
        val MealShareAutoSave = booleanPreferencesKey("meal_share_auto_save")
        val MealShareInstagram = booleanPreferencesKey("meal_share_suggest_instagram")
    }
}

private inline fun <reified T : Enum<T>> String?.enumOr(default: T): T =
    this?.let { value -> enumValues<T>().firstOrNull { it.name == value } } ?: default
