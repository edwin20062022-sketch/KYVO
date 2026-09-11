package com.kyvo.app.feature.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyvo.app.core.designsystem.KyvoColors
import com.kyvo.app.feature.settings.data.AppSettingsRepository
import com.kyvo.app.feature.settings.domain.AppearanceMode
import com.kyvo.app.feature.settings.domain.AppSettings
import com.kyvo.app.feature.settings.domain.FoodUnit
import com.kyvo.app.feature.settings.domain.HeightUnit
import com.kyvo.app.feature.settings.domain.TemperatureUnit
import com.kyvo.app.feature.settings.domain.WeightUnit

@Composable
fun UnitsRoute(repository: AppSettingsRepository, onBack: () -> Unit, viewModel: AppSettingsViewModel = viewModel(factory = AppSettingsViewModel.factory(repository))) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    UnitsScreen(settings, onBack, viewModel::setWeightUnit, viewModel::setHeightUnit, viewModel::setFoodUnit, viewModel::setTemperatureUnit)
}

@Composable
fun UnitsScreen(settings: AppSettings, onBack: () -> Unit = {}, onWeight: (WeightUnit) -> Unit = {}, onHeight: (HeightUnit) -> Unit = {}, onFood: (FoodUnit) -> Unit = {}, onTemperature: (TemperatureUnit) -> Unit = {}) {
    SettingsScaffold("units_screen", "Unidades", "Personaliza las unidades de medida según tus preferencias.", onBack) {
        item { PreferenceHint(Icons.Outlined.Info, "Estas unidades se aplicarán en toda la app.") }
        item { UnitCard(Icons.Outlined.Lock, "Peso", "Unidad de medida para tu peso corporal y alimentos.", WeightUnit.entries.toList(), settings.units.weight, onWeight) }
        item { UnitCard(Icons.Outlined.Info, "Altura", "Unidad de medida para tu estatura.", HeightUnit.entries.toList(), settings.units.height, onHeight) }
        item { UnitCard(Icons.Outlined.CheckCircle, "Alimentos", "Unidad de medida principal para porciones de alimentos.", FoodUnit.entries.toList(), settings.units.food, onFood) }
        item { UnitCard(Icons.Outlined.Info, "Temperatura", "Unidad de medida para recetas y preparaciones.", TemperatureUnit.entries.toList(), settings.units.temperature, onTemperature) }
        item { PreferenceHint(Icons.Outlined.CheckCircle, "Te recomendamos mantener las unidades en las que te sientas más cómodo, especialmente para un registro de alimentos más preciso.") }
    }
}

@Composable
private fun <T : Enum<T>> UnitCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, options: List<T>, selected: T, onSelected: (T) -> Unit) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(42.dp), tint = KyvoColors.PurplePrimary)
            Column(Modifier.weight(1f).padding(start = 16.dp)) { Text(title, style = MaterialTheme.typography.titleLarge); Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Column { options.forEach { option -> OptionChip(option.name, option.displayLabel(), option == selected) { onSelected(option) } } }
        }
    }
}

@Composable
private fun <T : Enum<T>> OptionChip(key: String, label: String, selected: Boolean, onClick: () -> Unit) {
    Row(Modifier.padding(vertical = 3.dp).clickable(onClick = onClick).semantics { role = Role.RadioButton; this.selected = selected; contentDescription = label }.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) { Text(label, color = if (selected) KyvoColors.PurplePrimary else MaterialTheme.colorScheme.onSurfaceVariant) }
}

@Composable
private fun Enum<*>.displayLabel(): String = when (this) {
    is WeightUnit -> label
    is HeightUnit -> label
    is FoodUnit -> label
    is TemperatureUnit -> label
    else -> name
}

@Composable
private fun PreferenceHint(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) { Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, Modifier.size(32.dp), tint = KyvoColors.PurplePrimary); Text(text, Modifier.padding(start = 14.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
}

@Composable
fun AppearanceRoute(repository: AppSettingsRepository, onBack: () -> Unit, viewModel: AppSettingsViewModel = viewModel(factory = AppSettingsViewModel.factory(repository))) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    AppearanceScreen(settings, onBack, viewModel::setAppearance, viewModel::setReduceBrightnessInDarkMode, viewModel::setHighContrast)
}

@Composable
fun AppearanceScreen(settings: AppSettings, onBack: () -> Unit = {}, onMode: (AppearanceMode) -> Unit = {}, onReduceBrightness: (Boolean) -> Unit = {}, onHighContrast: (Boolean) -> Unit = {}) {
    SettingsScaffold("appearance_screen", "Apariencia", "Elige el estilo visual que mejor se adapte a ti. La experiencia, siempre con la misma esencia.", onBack) {
        item { Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) { AppearanceMode.entries.forEach { mode -> AppearanceCard(mode, settings.appearance == mode, Modifier.weight(1f)) { onMode(mode) } } } }
        item { PreferenceHint(Icons.Outlined.Info, "Los colores, la información y tu progreso se mantienen siempre, sin importar el tema que elijas.") }
        item { Text("OTRAS OPCIONES", Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge) }
        item { AppearanceToggle("Reducir brillo en modo oscuro", "Disminuye la intensidad de algunos elementos.", settings.reduceBrightnessInDarkMode, true, onReduceBrightness) }
        item { AppearanceToggle("Alto contraste", "Mejora la visibilidad de textos y elementos.", settings.highContrast, true, onHighContrast) }
    }
}

@Composable
private fun AppearanceCard(mode: AppearanceMode, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier.clickable(onClick = onClick).semantics { role = Role.RadioButton; this.selected = selected; contentDescription = mode.label }, colors = CardDefaults.cardColors(containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)) { Column(Modifier.padding(vertical = 20.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(if (mode == AppearanceMode.LIGHT) "☼" else if (mode == AppearanceMode.DARK) "◐" else "◑", style = MaterialTheme.typography.headlineLarge, color = KyvoColors.PurplePrimary); Text(mode.label, style = MaterialTheme.typography.titleMedium); Text(when (mode) { AppearanceMode.LIGHT -> "Siempre luminoso"; AppearanceMode.DARK -> "Ideal para bajos niveles de luz"; AppearanceMode.SYSTEM -> "Se adapta a tu dispositivo" }, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
}

@Composable
private fun AppearanceToggle(title: String, subtitle: String, checked: Boolean, enabled: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Info, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant); Column(Modifier.weight(1f).padding(start = 14.dp)) { Text(title, style = MaterialTheme.typography.titleMedium); Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Switch(checked, onCheckedChange = onCheckedChange, enabled = enabled, modifier = Modifier.semantics { role = Role.Switch; contentDescription = title }) } }
}
