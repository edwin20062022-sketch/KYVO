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
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
import com.kyvo.app.R
import com.kyvo.app.feature.onboarding.domain.model.ExperienceLevel
import com.kyvo.app.feature.onboarding.domain.model.FoodPreference
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
fun ExperienceRoute(viewModel: PersonalPreferencesViewModel, onBack: () -> Unit, onContinue: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ExperienceScreen(state, viewModel::updateExperience, onContinue, onBack)
}

@Composable
fun ExperienceScreen(state: PersonalPreferencesUiState, onExperience: (ExperienceLevel) -> Unit = {}, onContinue: () -> Unit = {}, onBack: () -> Unit = {}) {
    val editing = state as? PersonalPreferencesUiState.Editing
    if (editing == null) { PreferencesLoading("experience_screen"); return }
    PreferencesScaffold("experience_screen", "Nivel de experiencia", "Nos ayuda a personalizar tu experiencia, recomendaciones y contenido según tu trayectoria en el gimnasio.", onBack) {
        ExperienceLevel.entries.forEach { level ->
            item {
                ExperienceCard(level, editing.draft.experience == level, { onExperience(level) })
            }
        }
        item { InfoNotice("Tu experiencia importa. Adaptaremos el lenguaje, las recomendaciones y la información para que sean más útiles en tu etapa actual.") }
        item { KyvoPrimaryButton("Guardar y continuar", onContinue, Modifier.padding(horizontal = 16.dp).semantics { contentDescription = "Guardar experiencia y continuar" }) }
    }
}

@Composable
private fun ExperienceCard(level: ExperienceLevel, selected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).semantics {
            role = Role.RadioButton
            this.selected = selected
            contentDescription = "${level.label()}, ${if (selected) "seleccionado" else "no seleccionado"}"
        },
        colors = CardDefaults.cardColors(containerColor = if (selected) KyvoColors.PurpleSoft else MaterialTheme.colorScheme.surface),
        border = if (selected) BorderStroke(1.dp, KyvoColors.PurplePrimary) else null,
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(level.profileIconRes()), level.label(), Modifier.size(88.dp), contentScale = ContentScale.Fit)
            Column(Modifier.weight(1f).padding(start = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(level.label(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(level.description(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(level.benefit(), color = KyvoColors.PurplePrimary, style = MaterialTheme.typography.bodyMedium)
            }
            Icon(Icons.Outlined.CheckCircle, null, tint = if (selected) KyvoColors.PurplePrimary else MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun FoodPreferencesRoute(viewModel: PersonalPreferencesViewModel, onBack: () -> Unit, onContinue: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    FoodPreferencesScreen(state, viewModel::updateFoodPreference, onContinue, onBack)
}

@Composable
fun FoodPreferencesScreen(state: PersonalPreferencesUiState, onPreference: (FoodPreference) -> Unit = {}, onContinue: () -> Unit = {}, onBack: () -> Unit = {}) {
    val editing = state as? PersonalPreferencesUiState.Editing
    if (editing == null) { PreferencesLoading("food_preferences_screen"); return }
    PreferencesScaffold("food_preferences_screen", "Preferencias alimenticias", "Selecciona la opción que mejor representa tu alimentación.", onBack) {
        item { InfoNotice("Estas preferencias nos ayudan a mostrarte alimentos más relevantes y adaptados a tus necesidades.") }
        item { SectionTitle("Preferencia alimenticia", "Selecciona una opción") }
        FoodPreference.entries.forEach { preference ->
            item {
                FoodPreferenceCard(preference, editing.draft.foodPreference == preference, { onPreference(preference) })
            }
        }
        item {
            KyvoPrimaryButton(
                "Guardar cambios",
                onContinue,
                Modifier.padding(horizontal = 16.dp).semantics { contentDescription = "Guardar preferencias alimenticias" },
                enabled = editing.draft.foodPreference != null,
            )
        }
    }
}

@Composable
private fun FoodPreferenceCard(preference: FoodPreference, selected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).semantics {
            role = Role.RadioButton
            this.selected = selected
            contentDescription = "${preference.label()}, ${if (selected) "seleccionada" else "no seleccionada"}"
        },
        colors = CardDefaults.cardColors(containerColor = if (selected) KyvoColors.PurpleSoft else MaterialTheme.colorScheme.surface),
        border = if (selected) BorderStroke(1.dp, KyvoColors.PurplePrimary) else null,
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(preference.profileIconRes()), preference.label(), Modifier.size(48.dp), contentScale = ContentScale.Fit)
            Column(Modifier.weight(1f).padding(start = 16.dp)) {
                Text(preference.label(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(preference.description(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Outlined.CheckCircle, null, tint = if (selected) KyvoColors.PurplePrimary else MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun MealOrganizationRoute(viewModel: PersonalPreferencesViewModel, onBack: () -> Unit, onContinue: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MealOrganizationScreen(state, viewModel::updateMealsPerDay, onContinue, onBack)
}

@Composable
fun MealOrganizationScreen(state: PersonalPreferencesUiState, onMeals: (Int?) -> Unit = {}, onContinue: () -> Unit = {}, onBack: () -> Unit = {}) {
    val editing = state as? PersonalPreferencesUiState.Editing
    var customSelected by remember(editing?.draft?.mealsPerDay) { mutableStateOf(editing?.draft?.mealsPerDay !in 3..6) }
    var customText by remember(editing?.draft?.mealsPerDay) { mutableStateOf(editing?.draft?.mealsPerDay?.toString().orEmpty()) }
    if (editing == null) { PreferencesLoading("meal_organization_screen"); return }
    PreferencesScaffold("meal_organization_screen", "Organización de comidas", "Elige cuántas comidas sueles hacer al día. Esto ayuda a personalizar la distribución de tus alimentos.", onBack) {
        item { SectionTitle("Número de comidas al día", "Selecciona una opción") }
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (3..6).forEach { count ->
                    SelectChip("$count", !customSelected && editing.draft.mealsPerDay == count, { customSelected = false; onMeals(count) }, Modifier.weight(1f))
                }
            }
        }
        item {
            MealCountCard("Personalizado", customSelected, { customSelected = true }, Modifier.padding(horizontal = 16.dp))
        }
        if (customSelected) {
            item { KyvoTextField(customText, { value -> customText = value; onMeals(value.toIntOrNull()) }, "Número de comidas", Modifier.padding(horizontal = 16.dp).fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)) }
        }
        item { SectionTitle("Vista previa de tu día", "Esta es una sugerencia de distribución. Puedes ajustar los nombres u horarios más adelante.") }
        item { MealPreview(editing.draft.mealsPerDay ?: 0) }
        item { InfoNotice("El número de comidas solo organiza tu día. Tus calorías y macros totales se mantienen igual.") }
        item { KyvoPrimaryButton("Guardar cambios", onContinue, Modifier.padding(horizontal = 16.dp).semantics { contentDescription = "Guardar organización de comidas" }, enabled = editing.draft.mealsPerDay in 1..8) }
    }
}

@Composable
private fun MealCountCard(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier.fillMaxWidth().semantics { role = Role.RadioButton; this.selected = selected; contentDescription = "$label, ${if (selected) "seleccionado" else "no seleccionado"}" }, colors = CardDefaults.cardColors(containerColor = if (selected) KyvoColors.PurpleSoft else MaterialTheme.colorScheme.surface), border = if (selected) BorderStroke(1.dp, KyvoColors.PurplePrimary) else null) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Text(label, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Icon(Icons.Outlined.CheckCircle, null, tint = if (selected) KyvoColors.PurplePrimary else MaterialTheme.colorScheme.outline) }
    }
}

@Composable
private fun MealPreview(meals: Int) {
    val labels = when (meals) { 3 -> listOf("Desayuno", "Comida", "Cena"); 4 -> listOf("Desayuno", "Comida", "Snack", "Cena"); 5 -> listOf("Desayuno", "Snack 1", "Comida", "Snack 2", "Cena"); 6 -> listOf("Desayuno", "Snack 1", "Comida", "Snack 2", "Cena", "Snack 3"); else -> emptyList() }
    Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { labels.forEach { label -> Surface(Modifier.weight(1f), RoundedCornerShape(12.dp), color = KyvoColors.PurpleSoft) { Text(label, Modifier.padding(10.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium) } } } }
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
private fun ExperienceLevel.description() = when (this) { ExperienceLevel.Beginner -> "Estoy comenzando mi camino en el gimnasio."; ExperienceLevel.Intermediate -> "Tengo experiencia y entreno de forma constante."; ExperienceLevel.Advanced -> "Tengo varios años de experiencia y un entrenamiento estructurado." }
private fun ExperienceLevel.benefit() = when (this) { ExperienceLevel.Beginner -> "Aprende lo esencial · Recomendaciones paso a paso"; ExperienceLevel.Intermediate -> "Recomendaciones más avanzadas · Mayor variedad"; ExperienceLevel.Advanced -> "Sugerencias de alto rendimiento · Mayor control" }
private fun ExperienceLevel.profileIconRes() = when (this) { ExperienceLevel.Beginner -> R.drawable.ic_experience_beginner; ExperienceLevel.Intermediate -> R.drawable.ic_experience_intermediate; ExperienceLevel.Advanced -> R.drawable.ic_experience_advanced }
private fun FoodPreference.description() = when (this) { FoodPreference.None -> "Como de todo"; FoodPreference.Vegetarian -> "Sin carne"; FoodPreference.Vegan -> "Sin productos de origen animal"; FoodPreference.GlutenFree -> "Evito alimentos con gluten"; FoodPreference.DairyFree -> "Sin productos lácteos"; FoodPreference.Other -> "Otra preferencia alimenticia" }
private fun FoodPreference.profileIconRes() = when (this) { FoodPreference.None -> R.drawable.ic_food_none; FoodPreference.Vegetarian -> R.drawable.ic_food_vegetarian; FoodPreference.Vegan -> R.drawable.ic_food_vegan; FoodPreference.GlutenFree -> R.drawable.ic_food_gluten_free; FoodPreference.DairyFree -> R.drawable.ic_food_dairy_free; FoodPreference.Other -> R.drawable.ic_onboarding_other }
