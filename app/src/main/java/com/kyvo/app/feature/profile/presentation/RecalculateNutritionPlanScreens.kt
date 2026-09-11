package com.kyvo.app.feature.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kyvo.app.core.designsystem.KyvoColors
import com.kyvo.app.feature.onboarding.domain.model.FitnessGoal
import com.kyvo.app.feature.onboarding.domain.repository.OnboardingRepository
import kotlin.math.roundToInt

const val UPDATE_GOAL_SCREEN_TAG = "update_goal_screen"
const val RECALCULATE_GOALS_SCREEN_TAG = "recalculate_goals_screen"
const val NEW_GOALS_SCREEN_TAG = "new_goals_screen"

@Composable
fun UpdateGoalRoute(viewModel: RecalculateNutritionPlanViewModel, onBack: () -> Unit, onContinue: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.startEditing() }
    UpdateGoalScreen(state, viewModel::selectGoal, onContinue, onBack)
}

@Composable
fun UpdateGoalScreen(
    state: RecalculateNutritionPlanUiState,
    onSelect: (FitnessGoal) -> Unit = {},
    onContinue: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    val editing = state as? RecalculateNutritionPlanUiState.Editing
    FlowScaffold(UPDATE_GOAL_SCREEN_TAG, "Actualizar objetivo", onBack) {
        item { Text("Elige el objetivo que mejor representa tu siguiente etapa.", Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleMedium) }
        items(FitnessGoal.entries.size) { index ->
            val goal = FitnessGoal.entries[index]
            GoalOption(goal, editing?.selectedGoal == goal, onSelect)
        }
        item {
            Button(onClick = onContinue, enabled = editing != null, colors = ButtonDefaults.buttonColors(containerColor = KyvoColors.PurplePrimary), modifier = Modifier.padding(16.dp).fillMaxWidth().height(56.dp).semantics { contentDescription = "Continuar con el nuevo objetivo" }) { Text("Continuar") }
        }
    }
}

@Composable
private fun GoalOption(goal: FitnessGoal, selected: Boolean, onSelect: (FitnessGoal) -> Unit) {
    Card(onClick = { onSelect(goal) }, Modifier.padding(horizontal = 16.dp).fillMaxWidth().semantics { role = Role.RadioButton; contentDescription = "${goal.label()}, ${if (selected) "seleccionado" else "no seleccionado"}" }, colors = CardDefaults.cardColors(containerColor = if (selected) KyvoColors.PurpleSoft else MaterialTheme.colorScheme.surface), border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, KyvoColors.PurplePrimary) else null) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(48.dp), RoundedCornerShape(24.dp), color = KyvoColors.PurpleSoft) { Icon(Icons.Outlined.Info, null, Modifier.padding(13.dp), tint = KyvoColors.PurplePrimary) }
            Column(Modifier.weight(1f).padding(start = 14.dp)) { Text(goal.label(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text(goal.description(), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            RadioButton(selected = selected, onClick = { onSelect(goal) })
        }
    }
}

@Composable
fun RecalculateGoalsRoute(viewModel: RecalculateNutritionPlanViewModel, onBack: () -> Unit, onPreview: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state) { if (state is RecalculateNutritionPlanUiState.Preview && viewModel.consumePreviewNavigation()) onPreview() }
    RecalculateGoalsScreen(state, viewModel::calculatePreview, onBack)
}

@Composable
fun RecalculateGoalsScreen(state: RecalculateNutritionPlanUiState, onCalculate: () -> Unit = {}, onBack: () -> Unit = {}) {
    val selected = when (state) {
        is RecalculateNutritionPlanUiState.Editing -> state.selectedGoal
        is RecalculateNutritionPlanUiState.Calculating -> state.selectedGoal
        is RecalculateNutritionPlanUiState.Preview -> state.selectedGoal
        is RecalculateNutritionPlanUiState.Error -> state.selectedGoal
        else -> null
    }
    if (state is RecalculateNutritionPlanUiState.Calculating) {
        FlowScaffold(RECALCULATE_GOALS_SCREEN_TAG, "Recalculando tus metas", onBack) { item { CalculationLoading() } }
    } else {
        FlowScaffold(RECALCULATE_GOALS_SCREEN_TAG, "Recalcular metas", onBack) {
            item { Text("Vamos a usar tus datos actuales y el objetivo seleccionado para preparar una nueva estimación.", Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleMedium) }
            item { Card(Modifier.padding(16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = KyvoColors.PurpleSoft)) { Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.CheckCircle, null, Modifier.size(34.dp), tint = KyvoColors.PurplePrimary); Column(Modifier.padding(start = 14.dp)) { Text("Nuevo objetivo", color = MaterialTheme.colorScheme.onSurfaceVariant); Text(selected?.label() ?: "Sin definir", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) } } } }
            item { Button(onClick = onCalculate, enabled = selected != null, colors = ButtonDefaults.buttonColors(containerColor = KyvoColors.PurplePrimary), modifier = Modifier.padding(16.dp).fillMaxWidth().height(56.dp).semantics { contentDescription = "Recalcular metas" }) { Text("Recalcular metas") } }
        }
    }
}

@Composable
private fun CalculationLoading() { Column(Modifier.fillMaxWidth().padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) { CircularProgressIndicator(color = KyvoColors.PurplePrimary); Text("Estamos preparando tus nuevas metas.", textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium) } }

@Composable
fun NewGoalsRoute(viewModel: RecalculateNutritionPlanViewModel, onBack: () -> Unit, onSaved: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state) { if (state is RecalculateNutritionPlanUiState.Saved) onSaved() }
    NewGoalsScreen(state, viewModel::confirm, viewModel::retry, onBack)
}

@Composable
fun NewGoalsScreen(state: RecalculateNutritionPlanUiState, onConfirm: () -> Unit = {}, onRetry: () -> Unit = {}, onBack: () -> Unit = {}) {
    val preview = when (state) {
        is RecalculateNutritionPlanUiState.Preview -> state
        is RecalculateNutritionPlanUiState.Saving -> state.preview
        is RecalculateNutritionPlanUiState.Error -> state.preview
        else -> null
    }
    FlowScaffold(NEW_GOALS_SCREEN_TAG, "Tus nuevas metas", onBack, closeLabel = "Cerrar") {
        if (preview == null) {
            item { Text("No hay una vista previa disponible.", Modifier.padding(16.dp)) }
        } else {
            item { Text("Con base en los cambios que realizaste, hemos recalculado tu plan nutricional.", Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center) }
            item { Card(Modifier.padding(16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = KyvoColors.PurpleSoft)) { Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.CheckCircle, null, Modifier.size(36.dp), tint = KyvoColors.PurplePrimary); Column(Modifier.padding(start = 14.dp)) { Text("Nuevo objetivo", color = MaterialTheme.colorScheme.onSurfaceVariant); Text(preview.selectedGoal.label(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) } } } }
            item { PlanComparison(preview) }
            item { Card(Modifier.padding(16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Row(Modifier.padding(18.dp), verticalAlignment = Alignment.Top) { Icon(Icons.Outlined.Info, null, Modifier.size(34.dp), tint = KyvoColors.PurplePrimary); Column(Modifier.padding(start = 14.dp)) { Text("Un impulso para tu progreso", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text("Ajustamos tus calorías y macros para alinearlos con tu nuevo objetivo.", color = MaterialTheme.colorScheme.onSurfaceVariant) } } } }
            item { Button(onClick = onConfirm, enabled = state !is RecalculateNutritionPlanUiState.Saving, colors = ButtonDefaults.buttonColors(containerColor = KyvoColors.PurplePrimary), modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(56.dp).semantics { contentDescription = "Aplicar nuevas metas" }) { Text(if (state is RecalculateNutritionPlanUiState.Saving) "Guardando…" else "¡Comenzar!") } }
            if (state is RecalculateNutritionPlanUiState.Error) item { Column(Modifier.padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(state.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center); OutlinedButton(onClick = onRetry) { Text("Reintentar") } } }
        }
    }
}

@Composable
private fun PlanComparison(preview: RecalculateNutritionPlanUiState.Preview) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ComparisonCard("PLAN ANTERIOR", preview.oldPlan.targetCaloriesKcal, preview.oldPlan.proteinGrams, preview.oldPlan.carbohydrateGrams, preview.oldPlan.fatGrams, Modifier.weight(1f), false)
        ComparisonCard("NUEVO PLAN", preview.previewPlan.targetCaloriesKcal, preview.previewPlan.proteinGrams, preview.previewPlan.carbohydrateGrams, preview.previewPlan.fatGrams, Modifier.weight(1f), true)
    }
}

@Composable
private fun ComparisonCard(label: String, calories: Int, protein: Int, carbs: Int, fat: Int, modifier: Modifier, highlighted: Boolean) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = if (highlighted) KyvoColors.PurpleSoft else MaterialTheme.colorScheme.surface)) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) { Text(label, color = if (highlighted) KyvoColors.PurplePrimary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold); Text("$calories", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = if (highlighted) KyvoColors.PurplePrimary else MaterialTheme.colorScheme.onSurface); Text("kcal por día", color = MaterialTheme.colorScheme.onSurfaceVariant); Text("Proteína       $protein g"); Text("Carbohidratos  $carbs g"); Text("Grasas          $fat g") } }
}

@Composable
private fun FlowScaffold(tag: String, title: String, onBack: () -> Unit, closeLabel: String? = null, content: LazyListScope.() -> Unit) {
    Surface(Modifier.fillMaxSize().semantics { contentDescription = tag }, color = MaterialTheme.colorScheme.background) { LazyColumn(contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { item { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack, Modifier.size(48.dp).semantics { contentDescription = "Volver" }) { Icon(Icons.Outlined.ArrowBack, "Volver") }; if (closeLabel != null) Text(closeLabel, Modifier.weight(1f).padding(end = 18.dp), color = KyvoColors.PurplePrimary, fontWeight = FontWeight.Bold, textAlign = TextAlign.End) } }; item { Text(title, Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black) }; content() } }
}

private fun FitnessGoal.label() = when (this) {
    FitnessGoal.FatLoss -> "Bajar grasa"
    FitnessGoal.MuscleGain -> "Ganancia muscular"
    FitnessGoal.Recomposition -> "Recomposición corporal"
    FitnessGoal.Maintenance -> "Mantenimiento"
    FitnessGoal.Performance -> "Performance / fuerza"
}

private fun FitnessGoal.description() = when (this) {
    FitnessGoal.FatLoss -> "Quiero reducir mi porcentaje de grasa corporal."
    FitnessGoal.MuscleGain -> "Quiero aumentar mi masa muscular de forma saludable."
    FitnessGoal.Recomposition -> "Quiero ganar músculo y perder grasa al mismo tiempo."
    FitnessGoal.Maintenance -> "Quiero mantener mi peso actual de forma saludable."
    FitnessGoal.Performance -> "Quiero mejorar mi rendimiento, fuerza y capacidades físicas."
}
