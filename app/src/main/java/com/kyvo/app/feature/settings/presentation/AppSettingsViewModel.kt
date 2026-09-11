package com.kyvo.app.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kyvo.app.feature.settings.data.AppSettingsRepository
import com.kyvo.app.feature.settings.domain.AppearanceMode
import com.kyvo.app.feature.settings.domain.AppSettings
import com.kyvo.app.feature.settings.domain.FoodUnit
import com.kyvo.app.feature.settings.domain.HeightUnit
import com.kyvo.app.feature.settings.domain.TemperatureUnit
import com.kyvo.app.feature.settings.domain.WeightUnit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppSettingsViewModel(private val repository: AppSettingsRepository) : ViewModel() {
    val settings: StateFlow<AppSettings> = repository.observe().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setWeightUnit(unit: WeightUnit) = viewModelScope.launch { repository.setWeightUnit(unit) }
    fun setHeightUnit(unit: HeightUnit) = viewModelScope.launch { repository.setHeightUnit(unit) }
    fun setFoodUnit(unit: FoodUnit) = viewModelScope.launch { repository.setFoodUnit(unit) }
    fun setTemperatureUnit(unit: TemperatureUnit) = viewModelScope.launch { repository.setTemperatureUnit(unit) }
    fun setAppearance(mode: AppearanceMode) = viewModelScope.launch { repository.setAppearance(mode) }
    fun setReduceBrightnessInDarkMode(enabled: Boolean) = viewModelScope.launch { repository.setReduceBrightnessInDarkMode(enabled) }
    fun setHighContrast(enabled: Boolean) = viewModelScope.launch { repository.setHighContrast(enabled) }
    fun setNotificationsEnabled(enabled: Boolean) = viewModelScope.launch { repository.setNotificationsEnabled(enabled) }
    fun setBreakfastNotifications(enabled: Boolean) = viewModelScope.launch { repository.setBreakfastNotifications(enabled) }
    fun setLunchNotifications(enabled: Boolean) = viewModelScope.launch { repository.setLunchNotifications(enabled) }
    fun setSnackNotifications(enabled: Boolean) = viewModelScope.launch { repository.setSnackNotifications(enabled) }
    fun setDinnerNotifications(enabled: Boolean) = viewModelScope.launch { repository.setDinnerNotifications(enabled) }
    fun setDayCloseNotifications(enabled: Boolean) = viewModelScope.launch { repository.setDayCloseNotifications(enabled) }
    fun setNewFeaturesNotifications(enabled: Boolean) = viewModelScope.launch { repository.setNewFeaturesNotifications(enabled) }
    fun setTipsAndContentNotifications(enabled: Boolean) = viewModelScope.launch { repository.setTipsAndContentNotifications(enabled) }
    fun setAccountNoticesNotifications(enabled: Boolean) = viewModelScope.launch { repository.setAccountNoticesNotifications(enabled) }

    companion object {
        fun factory(repository: AppSettingsRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = AppSettingsViewModel(repository) as T
        }
    }
}
