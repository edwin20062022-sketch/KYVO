package com.kyvo.app.feature.profile.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kyvo.app.core.designsystem.KyvoColors
import com.kyvo.app.core.designsystem.component.KyvoPrimaryButton
import com.kyvo.app.core.designsystem.component.KyvoTextField
import com.kyvo.app.feature.onboarding.domain.model.GenderOption
import com.kyvo.app.feature.onboarding.domain.model.SavedOnboarding
import com.kyvo.app.feature.onboarding.domain.model.TrainingType
import com.kyvo.app.feature.onboarding.domain.model.WorkActivity
import kotlin.math.roundToInt

const val PERSONAL_PREFERENCES_SCREEN_TAG = "personal_preferences_screen"
const val PERSONAL_DATA_SCREEN_TAG = "personal_data_screen"
const val ACTIVITY_TRAINING_SCREEN_TAG = "activity_training_screen"

@Composable
fun PersonalPreferencesHubRoute(viewModel: PersonalPreferencesViewModel, onBack: () -> Unit, onPersonalData: () -> Unit, onActivity: () -> Unit, onFuture: (String) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PersonalPreferencesHubScreen(state, onBack, onPersonalData, onActivity, onFuture, viewModel::retry)
}

@Composable
fun PersonalPreferencesHubScreen(state: PersonalPreferencesUiState, onBack: () -> Unit = {}, onPersonalData: () -> Unit = {}, onActivity: () -> Unit = {}, onFuture: (String) -> Unit = {}, onRetry: () -> Unit = {}) {
    when (state) {
        PersonalPreferencesUiState.Loading -> PreferencesLoading(PERSONAL_PREFERENCES_SCREEN_TAG)
        is PersonalPreferencesUiState.Error -> PreferencesError(PERSONAL_PREFERENCES_SCREEN_TAG, state.message, onRetry, onBack)
        is PersonalPreferencesUiState.Editing -> PreferencesHubContent(state.draft, onBack, onPersonalData, onActivity, onFuture)
    }
}

@Composable
private fun PreferencesHubContent(draft: SavedOnboarding, onBack: () -> Unit, onPersonalData: () -> Unit, onActivity: () -> Unit, onFuture: (String) -> Unit) {
    PreferencesScaffold(PERSONAL_PREFERENCES_SCREEN_TAG, "Datos personales y preferencias", "Mantén tu información actualizada para una experiencia más precisa y personalizada.", onBack) {
        item { PreferenceEntry("Datos personales", "Tu información básica para calcular tus requerimientos.", "${draft.gender.label()}, ${draft.ageYears ?: "—"} años\n${draft.heightCm?.roundToInt() ?: "—"} cm, ${draft.weightKg?.roundToInt() ?: "—"} kg", onPersonalData) }
        item { PreferenceEntry("Actividad y entrenamiento", "Cuéntanos sobre tu rutina y tu actividad diaria.", "${draft.trainingDaysPerWeek ?: "—"} días/semana\n${draft.trainingType.label()}, ${draft.workActivity.label()}", onActivity) }
        item { PreferenceEntry("Nivel de experiencia", "Nos ayuda a personalizar recomendaciones y contenido.", draft.experience?.label() ?: "Sin definir", { onFuture("Nivel de experiencia") }) }
        item { PreferenceEntry("Preferencias alimenticias", "Selecciona tus restricciones y preferencias de alimentación.", draft.foodPreference?.label() ?: "Sin definir", { onFuture("Preferencias alimenticias") }) }
        item { PreferenceEntry("Organización de comidas", "Define cuántas comidas realizas al día.", draft.mealsPerDay?.let { "$it comidas al día" } ?: "Sin definir", { onFuture("Organización de comidas") }) }
        item { Card(onClick = { onFuture("Revisar actualización de metas") }, Modifier.fillMaxWidth().semantics { role = Role.Button; contentDescription = "Revisar actualización de metas" }, colors = CardDefaults.cardColors(containerColor = KyvoColors.PurpleSoft)) { Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Info, null, Modifier.size(34.dp), tint = KyvoColors.PurplePrimary); Column(Modifier.weight(1f).padding(start = 14.dp)) { Text("¿Cambiaste algún dato importante?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text("Más adelante podrás revisar cómo estos cambios afectan tus metas.", color = MaterialTheme.colorScheme.onSurfaceVariant) } } } }
    }
}

@Composable
private fun PreferenceEntry(title: String, subtitle: String, value: String, onClick: () -> Unit) {
    Card(onClick = onClick, Modifier.fillMaxWidth().semantics { role = Role.Button; contentDescription = title }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(48.dp), RoundedCornerShape(24.dp), color = KyvoColors.PurpleSoft) { Icon(Icons.Outlined.Person, null, Modifier.padding(13.dp), tint = KyvoColors.PurplePrimary) }
            Column(Modifier.weight(1f).padding(start = 14.dp)) { Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End) }
            Text("›", color = KyvoColors.PurplePrimary, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
fun PersonalDataRoute(viewModel: PersonalPreferencesViewModel, onBack: () -> Unit, onSavedStep: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PersonalDataScreen(state, viewModel::updateGender, viewModel::updateAge, viewModel::updateHeightCm, viewModel::updateWeightKg, onSavedStep, onBack)
}

@Composable
fun PersonalDataScreen(state: PersonalPreferencesUiState, onGender: (GenderOption) -> Unit = {}, onAge: (Int?) -> Unit = {}, onHeightCm: (Double?) -> Unit = {}, onWeightKg: (Double?) -> Unit = {}, onContinue: () -> Unit = {}, onBack: () -> Unit = {}) {
    val editing = state as? PersonalPreferencesUiState.Editing
    var heightUnit by remember { mutableStateOf("cm") }
    var weightUnit by remember { mutableStateOf("kg") }
    var ageText by remember(editing?.draft?.ageYears) { mutableStateOf(editing?.draft?.ageYears?.toString().orEmpty()) }
    var heightText by remember(editing?.draft?.heightCm, heightUnit) { mutableStateOf(displayHeight(editing?.draft?.heightCm, heightUnit)) }
    var weightText by remember(editing?.draft?.weightKg, weightUnit) { mutableStateOf(displayWeight(editing?.draft?.weightKg, weightUnit)) }
    if (editing == null) { PreferencesLoading(PERSONAL_DATA_SCREEN_TAG); return }
    PreferencesScaffold(PERSONAL_DATA_SCREEN_TAG, "Datos personales", "Esta información se utiliza para calcular tus requerimientos nutricionales.", onBack) {
        item { Text("GÉNERO", Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold) }
        item { Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { GenderOption.entries.forEach { gender -> SelectChip(gender.label(), editing.draft.gender == gender, { onGender(gender) }, Modifier.weight(1f)) } } }
        item { KyvoTextField(ageText, { value -> ageText = value; onAge(value.toIntOrNull()) }, "Edad", Modifier.padding(horizontal = 16.dp).fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)) }
        item { UnitInput("Altura", heightText, { value -> heightText = value; onHeightCm(parseHeight(value, heightUnit)) }, heightUnit, { unit -> heightUnit = unit; heightText = displayHeight(editing.draft.heightCm, unit) }) }
        item { UnitInput("Peso actual", weightText, { value -> weightText = value; onWeightKg(parseWeight(value, weightUnit)) }, weightUnit, { unit -> weightUnit = unit; weightText = displayWeight(editing.draft.weightKg, unit) }) }
        item { InfoNotice("Si modificas alguno de estos datos, es posible que tus metas nutricionales se actualicen. Te avisaremos antes de realizar cualquier cambio.") }
        item { KyvoPrimaryButton("Guardar cambios", onContinue, Modifier.padding(horizontal = 16.dp).semantics { contentDescription = "Guardar datos personales" }) }
    }
}

@Composable
private fun UnitInput(label: String, value: String, onValue: (String) -> Unit, unit: String, onUnit: (String) -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf(if (label == "Altura") "cm" else "kg", if (label == "Altura") "ft" else "lb").forEach { option -> SelectChip(option, unit == option, { onUnit(option) }, Modifier.weight(1f)) } }; KyvoTextField(value, onValue, label, Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)) }
}

@Composable
fun ActivityTrainingRoute(viewModel: PersonalPreferencesViewModel, onBack: () -> Unit, onSavedStep: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ActivityTrainingScreen(state, viewModel::updateTrainingDays, viewModel::updateTrainingType, viewModel::updateWorkActivity, onSavedStep, onBack)
}

@Composable
fun ActivityTrainingScreen(state: PersonalPreferencesUiState, onDays: (Int) -> Unit = {}, onTraining: (TrainingType) -> Unit = {}, onWork: (WorkActivity) -> Unit = {}, onContinue: () -> Unit = {}, onBack: () -> Unit = {}) {
    val editing = state as? PersonalPreferencesUiState.Editing
    if (editing == null) { PreferencesLoading(ACTIVITY_TRAINING_SCREEN_TAG); return }
    PreferencesScaffold(ACTIVITY_TRAINING_SCREEN_TAG, "Actividad y entrenamiento", "Esta información nos ayuda a calcular tus requerimientos calóricos y personalizar tu experiencia.", onBack) {
        item { SectionTitle("Días de entrenamiento por semana", "¿Cuántos días sueles entrenar en el gimnasio?") }
        item { Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) { (1..7).forEach { day -> SelectChip(day.toString(), editing.draft.trainingDaysPerWeek == day, { onDays(day) }, Modifier.weight(1f)) } } }
        item { SectionTitle("Tipo de entrenamiento", "¿Qué tipo de entrenamiento realizas principalmente?") }
        item { Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { TrainingType.entries.forEach { training -> SelectChip(training.label(), editing.draft.trainingType == training, { onTraining(training) }, Modifier.weight(1f)) } } }
        item { SectionTitle("Actividad laboral", "¿Cómo describirías tu nivel de actividad en tu trabajo del día a día?") }
        item { Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { WorkActivity.entries.forEach { work -> SelectCard(work.label(), editing.draft.workActivity == work, { onWork(work) }) } } }
        item { KyvoPrimaryButton("Guardar cambios", onContinue, Modifier.padding(horizontal = 16.dp).semantics { contentDescription = "Guardar actividad y entrenamiento" }) }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) { Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) } }

@Composable
private fun SelectChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) { OutlinedButton(onClick = onClick, modifier = modifier.height(52.dp).semantics { role = Role.RadioButton; this.selected = selected; contentDescription = "$label, ${if (selected) "seleccionado" else "no seleccionado"}" }, border = BorderStroke(1.dp, if (selected) KyvoColors.PurplePrimary else MaterialTheme.colorScheme.outline), colors = ButtonDefaults.outlinedButtonColors(containerColor = if (selected) KyvoColors.PurpleSoft else MaterialTheme.colorScheme.surface, contentColor = if (selected) KyvoColors.PurplePrimary else MaterialTheme.colorScheme.onSurface)) { Text(label, textAlign = TextAlign.Center) } }

@Composable
private fun SelectCard(label: String, selected: Boolean, onClick: () -> Unit) { Card(onClick = onClick, Modifier.fillMaxWidth().semantics { role = Role.RadioButton; this.selected = selected; contentDescription = "$label, ${if (selected) "seleccionado" else "no seleccionado"}" }, colors = CardDefaults.cardColors(containerColor = if (selected) KyvoColors.PurpleSoft else MaterialTheme.colorScheme.surface), border = if (selected) BorderStroke(1.dp, KyvoColors.PurplePrimary) else null) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.CheckCircle, null, Modifier.size(28.dp), tint = KyvoColors.PurplePrimary); Text(label, Modifier.padding(start = 12.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) } } }

@Composable
private fun InfoNotice(text: String) { Surface(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), color = KyvoColors.PurpleSoft, shape = MaterialTheme.shapes.large) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) { Icon(Icons.Outlined.Info, null, Modifier.size(28.dp), tint = KyvoColors.PurplePrimary); Text(text, Modifier.padding(start = 12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) } } }

@Composable
private fun PreferencesScaffold(tag: String, title: String, subtitle: String, onBack: () -> Unit, content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) { Surface(Modifier.fillMaxSize().imePadding().semantics { contentDescription = tag }, color = MaterialTheme.colorScheme.background) { LazyColumn(contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { item { IconButton(onClick = onBack, Modifier.size(48.dp).semantics { contentDescription = "Volver" }) { Icon(Icons.Outlined.ArrowBack, "Volver") } }; item { Text(title, Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black) }; item { Text(subtitle, Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleMedium) }; content() } } }

@Composable
private fun PreferencesLoading(tag: String) { Surface(Modifier.fillMaxSize().semantics { contentDescription = tag }, color = MaterialTheme.colorScheme.background) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } } }

@Composable
private fun PreferencesError(tag: String, message: String, onRetry: () -> Unit, onBack: () -> Unit) { PreferencesScaffold(tag, "Datos personales y preferencias", "", onBack) { item { Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(message, textAlign = TextAlign.Center); Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) { Text("Reintentar") } } } } }

private fun displayHeight(cm: Double?, unit: String): String = cm?.let { if (unit == "ft") "%.2f".format(it / 30.48) else it.roundToInt().toString() }.orEmpty()
private fun parseHeight(value: String, unit: String): Double? = value.replace(',', '.').toDoubleOrNull()?.let { if (unit == "ft") it * 30.48 else it }
private fun displayWeight(kg: Double?, unit: String): String = kg?.let { if (unit == "lb") "%.1f".format(it * 2.20462) else it.roundToInt().toString() }.orEmpty()
private fun parseWeight(value: String, unit: String): Double? = value.replace(',', '.').toDoubleOrNull()?.let { if (unit == "lb") it / 2.20462 else it }

private fun GenderOption?.label() = when (this) { GenderOption.Male -> "Masculino"; GenderOption.Female -> "Femenino"; GenderOption.PreferNotToSay -> "Otro"; null -> "Sin definir" }
private fun TrainingType?.label() = when (this) { TrainingType.Strength -> "Fuerza"; TrainingType.Hypertrophy -> "Hipertrofia"; TrainingType.Functional -> "Funcional"; TrainingType.Cardio -> "Cardio"; null -> "Sin definir" }
private fun WorkActivity?.label() = when (this) { WorkActivity.Sedentary -> "Oficina"; WorkActivity.Active -> "Activo"; WorkActivity.Physical -> "Trabajo físico"; null -> "Sin definir" }
private fun com.kyvo.app.feature.onboarding.domain.model.ExperienceLevel.label() = when (this) { com.kyvo.app.feature.onboarding.domain.model.ExperienceLevel.Beginner -> "Principiante"; com.kyvo.app.feature.onboarding.domain.model.ExperienceLevel.Intermediate -> "Intermedio"; com.kyvo.app.feature.onboarding.domain.model.ExperienceLevel.Advanced -> "Avanzado" }
private fun com.kyvo.app.feature.onboarding.domain.model.FoodPreference.label() = when (this) { com.kyvo.app.feature.onboarding.domain.model.FoodPreference.None -> "Sin restricciones"; com.kyvo.app.feature.onboarding.domain.model.FoodPreference.Vegetarian -> "Vegetariano"; com.kyvo.app.feature.onboarding.domain.model.FoodPreference.Vegan -> "Vegano"; com.kyvo.app.feature.onboarding.domain.model.FoodPreference.GlutenFree -> "Sin gluten"; com.kyvo.app.feature.onboarding.domain.model.FoodPreference.DairyFree -> "Sin lácteos"; com.kyvo.app.feature.onboarding.domain.model.FoodPreference.Other -> "Otra preferencia" }
