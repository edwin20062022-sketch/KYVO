package com.kyvo.app.feature.settings.presentation

import com.kyvo.app.feature.settings.data.AppSettingsRepository
import com.kyvo.app.feature.settings.domain.AppearanceMode
import com.kyvo.app.feature.settings.domain.AppSettings
import com.kyvo.app.feature.settings.domain.FoodUnit
import com.kyvo.app.feature.settings.domain.HeightUnit
import com.kyvo.app.feature.settings.domain.TemperatureUnit
import com.kyvo.app.feature.settings.domain.WeightUnit
import com.kyvo.app.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppSettingsViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun unitAndAppearanceChangesArePersistedThroughRepository() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeAppSettingsRepository()
        val viewModel = AppSettingsViewModel(repository)

        viewModel.setWeightUnit(WeightUnit.POUNDS)
        viewModel.setHeightUnit(HeightUnit.FEET_INCHES)
        viewModel.setFoodUnit(FoodUnit.OUNCES)
        viewModel.setTemperatureUnit(TemperatureUnit.FAHRENHEIT)
        viewModel.setAppearance(AppearanceMode.DARK)
        advanceUntilIdle()

        assertEquals(WeightUnit.POUNDS, repository.settings.value.units.weight)
        assertEquals(HeightUnit.FEET_INCHES, repository.settings.value.units.height)
        assertEquals(FoodUnit.OUNCES, repository.settings.value.units.food)
        assertEquals(TemperatureUnit.FAHRENHEIT, repository.settings.value.units.temperature)
        assertEquals(AppearanceMode.DARK, repository.settings.value.appearance)
        assertEquals(repository.settings.value, repository.observe().first())
    }
}

private class FakeAppSettingsRepository : AppSettingsRepository {
    val settings = MutableStateFlow(AppSettings())
    override fun observe(): Flow<AppSettings> = settings.asStateFlow()
    override suspend fun setWeightUnit(unit: WeightUnit) { settings.value = settings.value.copy(units = settings.value.units.copy(weight = unit)) }
    override suspend fun setHeightUnit(unit: HeightUnit) { settings.value = settings.value.copy(units = settings.value.units.copy(height = unit)) }
    override suspend fun setFoodUnit(unit: FoodUnit) { settings.value = settings.value.copy(units = settings.value.units.copy(food = unit)) }
    override suspend fun setTemperatureUnit(unit: TemperatureUnit) { settings.value = settings.value.copy(units = settings.value.units.copy(temperature = unit)) }
    override suspend fun setAppearance(mode: AppearanceMode) { settings.value = settings.value.copy(appearance = mode) }
    override suspend fun setReduceBrightnessInDarkMode(enabled: Boolean) { settings.value = settings.value.copy(reduceBrightnessInDarkMode = enabled) }
    override suspend fun setHighContrast(enabled: Boolean) { settings.value = settings.value.copy(highContrast = enabled) }
}
