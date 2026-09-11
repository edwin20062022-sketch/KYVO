package com.kyvo.app.feature.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyvo.app.core.designsystem.KyvoColors
import com.kyvo.app.feature.onboarding.domain.model.FitnessGoal
import com.kyvo.app.feature.onboarding.domain.model.GenderOption
import com.kyvo.app.feature.onboarding.domain.model.SavedOnboarding
import com.kyvo.app.feature.onboarding.domain.model.TrainingType
import com.kyvo.app.feature.onboarding.domain.model.WorkActivity
import com.kyvo.app.feature.onboarding.domain.repository.OnboardingRepository
import kotlin.math.roundToInt

const val NUTRITION_PLAN_SCREEN_TAG = "nutrition_plan_screen"
const val NUTRITION_CALCULATION_SCREEN_TAG = "nutrition_calculation_screen"

@Composable
fun NutritionPlanRoute(
    repository: OnboardingRepository,
    onBack: () -> Unit,
    onHowCalculated: () -> Unit,
    onRecalculate: () -> Unit,
    viewModel: NutritionPlanViewModel = viewModel(factory = NutritionPlanViewModel.factory(repository)),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    NutritionPlanScreen(state, viewModel::retry, onBack, onHowCalculated, onRecalculate)
}

@Composable
fun NutritionPlanScreen(
    state: NutritionPlanUiState,
    onRetry: () -> Unit = {},
    onBack: () -> Unit = {},
    onHowCalculated: () -> Unit = {},
    onRecalculate: () -> Unit = {},
) {
    when (state) {
        NutritionPlanUiState.Loading -> PlanLoading(NUTRITION_PLAN_SCREEN_TAG)
        is NutritionPlanUiState.Error -> PlanError(NUTRITION_PLAN_SCREEN_TAG, state.message, onRetry, onBack)
        is NutritionPlanUiState.Incomplete -> PlanIncomplete(NUTRITION_PLAN_SCREEN_TAG, state.onboarding, onBack)
        is NutritionPlanUiState.Content -> NutritionPlanContent(state.onboarding, onBack, onHowCalculated, onRecalculate)
    }
}

@Composable
private fun NutritionPlanContent(
    onboarding: SavedOnboarding,
    onBack: () -> Unit,
    onHowCalculated: () -> Unit,
    onRecalculate: () -> Unit,
) {
    val plan = onboarding.plan ?: return
    PlanScaffold(NUTRITION_PLAN_SCREEN_TAG, onBack, "Mi plan nutricional") {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(onClick = onRecalculate, shape = RoundedCornerShape(24.dp)) { Text("Recalcular metas") }
            }
        }
        item {
            Text("Tu guía diaria para alcanzar tus objetivos.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleMedium)
        }
        item { GoalCard(onboarding.goal) }
        item {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("TU CONSUMO DIARIO", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                Text("${plan.targetCaloriesKcal} kcal", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
                Text("Calculadas especialmente para ti", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item { Text("TUS MACROS DIARIOS", Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        item { MacroRow(plan) }
        item { Text("INFORMACIÓN UTILIZADA", Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        item { InformationCard(onboarding) }
        item {
            Card(onClick = onHowCalculated, Modifier.fillMaxWidth().semantics { role = Role.Button; contentDescription = "Cómo calculamos tus metas" }, colors = CardDefaults.cardColors(containerColor = KyvoColors.PurpleSoft), shape = MaterialTheme.shapes.large) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Info, null, Modifier.size(36.dp), tint = KyvoColors.PurplePrimary)
                    Column(Modifier.weight(1f).padding(start = 14.dp)) {
                        Text("¿Cómo calculamos tus metas?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Usamos tus datos, tu actividad y tu objetivo para crear un plan personalizado.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("Conocer más", color = KyvoColors.PurplePrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun GoalCard(goal: FitnessGoal?) {
    Box(Modifier.padding(horizontal = 16.dp).fillMaxWidth().clip(MaterialTheme.shapes.large).background(Brush.horizontalGradient(listOf(Color(0xFF24183F), Color(0xFF4B2E72)))) .padding(22.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("TU OBJETIVO", color = Color.White.copy(alpha = .8f), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(goal?.label() ?: "Sin definir", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text(goal?.description() ?: "Completa tus datos para definir tu plan.", color = Color.White.copy(alpha = .8f), style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun MacroRow(plan: com.kyvo.app.feature.onboarding.domain.model.NutritionPlan) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MacroCard("Proteína", plan.proteinGrams, KyvoColors.Protein, Icons.Outlined.CheckCircle, plan.targetCaloriesKcal, Modifier.weight(1f))
        MacroCard("Carbohidratos", plan.carbohydrateGrams, KyvoColors.Carbohydrate, Icons.Outlined.Info, plan.targetCaloriesKcal, Modifier.weight(1f))
        MacroCard("Grasas", plan.fatGrams, KyvoColors.Fat, Icons.Outlined.Info, plan.targetCaloriesKcal, Modifier.weight(1f))
    }
}

@Composable
private fun MacroCard(label: String, grams: Int, color: Color, icon: ImageVector, calories: Int, modifier: Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Surface(Modifier.size(42.dp), RoundedCornerShape(21.dp), color = KyvoColors.PurpleSoft) { Icon(icon, null, Modifier.padding(10.dp), tint = color) }
            Text(label, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center, maxLines = 2)
            Text("$grams g", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Text("${(grams * if (label == "Grasas") 9 else 4) * 100 / calories}%", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun InformationCard(onboarding: SavedOnboarding) {
    val values = listOf(
        InfoLine("Género", onboarding.gender?.label() ?: "Sin definir", Icons.Outlined.Person),
        InfoLine("Edad", onboarding.ageYears?.let { "$it años" } ?: "Sin definir", Icons.Outlined.Info),
        InfoLine("Altura", onboarding.heightCm?.let { "${it.roundToInt()} cm" } ?: "Sin definir", Icons.Outlined.Info),
        InfoLine("Peso actual", onboarding.weightKg?.let { "${it.roundToInt()} kg" } ?: "Sin definir", Icons.Outlined.CheckCircle),
        InfoLine("Días de entrenamiento", onboarding.trainingDaysPerWeek?.let { "$it días por semana" } ?: "Sin definir", Icons.Outlined.Info),
        InfoLine("Tipo de entrenamiento", onboarding.trainingType?.label() ?: "Sin definir", Icons.Outlined.CheckCircle),
        InfoLine("Actividad laboral", onboarding.workActivity?.label() ?: "Sin definir", Icons.Outlined.Person),
        InfoLine("Comidas al día", onboarding.mealsPerDay?.let { "$it comidas" } ?: "Sin definir", Icons.Outlined.Info),
        InfoLine("Preferencias alimenticias", onboarding.foodPreference?.label() ?: "Sin definir", Icons.Outlined.Info),
    )
    Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { values.forEach { line -> InfoLineView(line) } }
    }
}

private data class InfoLine(val label: String, val value: String, val icon: ImageVector)

@Composable
private fun InfoLineView(line: InfoLine) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(line.icon, null, Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(line.label, Modifier.weight(1f).padding(start = 10.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(line.value, fontWeight = FontWeight.Medium, textAlign = TextAlign.End)
    }
}

@Composable
fun NutritionCalculationRoute(
    repository: OnboardingRepository,
    onBack: () -> Unit,
    viewModel: NutritionPlanViewModel = viewModel(factory = NutritionPlanViewModel.factory(repository)),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when (state) {
        NutritionPlanUiState.Loading -> PlanLoading(NUTRITION_CALCULATION_SCREEN_TAG)
        is NutritionPlanUiState.Error -> PlanError(NUTRITION_CALCULATION_SCREEN_TAG, (state as NutritionPlanUiState.Error).message, viewModel::retry, onBack)
        is NutritionPlanUiState.Incomplete -> PlanIncomplete(NUTRITION_CALCULATION_SCREEN_TAG, (state as NutritionPlanUiState.Incomplete).onboarding, onBack)
        is NutritionPlanUiState.Content -> NutritionCalculationScreen((state as NutritionPlanUiState.Content).onboarding, onBack)
    }
}

@Composable
fun NutritionCalculationScreen(onboarding: SavedOnboarding, onBack: () -> Unit) {
    val plan = onboarding.plan
    PlanScaffold(NUTRITION_CALCULATION_SCREEN_TAG, onBack, "Cómo calculamos tus metas") {
        item {
            Text("Utilizamos un enfoque científico y personalizado para estimar tus necesidades calóricas y macros, basado en tu información y objetivo.", Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleMedium)
        }
        item { CalculationStep(1, "Metabolismo basal (BMR)", "Calculamos tu gasto energético en reposo utilizando la fórmula de Mifflin-St Jeor.", "${plan?.bmrKcal?.roundToInt() ?: "Sin definir"} kcal/día", Icons.Outlined.Person) }
        item { CalculationStep(2, "Nivel de actividad", "Ajustamos tu BMR según tus días de entrenamiento, el tipo de entrenamiento y tu actividad laboral.", "Factor ${plan?.activityFactor?.let { "%.2f".format(it) } ?: "Sin definir"}", Icons.Outlined.Info) }
        item { CalculationStep(3, "Gasto energético total (TDEE)", "Multiplicamos tu BMR por el factor de actividad para estimar las calorías de tu día.", "${plan?.tdeeKcal?.roundToInt() ?: "Sin definir"} kcal/día", Icons.Outlined.Info) }
        item { CalculationStep(4, "Ajuste por objetivo", "Aplicamos un ajuste calórico según tu objetivo: déficit, superávit o mantenimiento.", "${onboarding.goal?.label() ?: "Sin definir"} ${plan?.goalAdjustmentFraction?.let { "(${(it * 100).roundToInt()}%)" } ?: ""}", Icons.Outlined.CheckCircle) }
        item { CalculationStep(5, "Distribución de macros", "Distribuimos tus calorías entre proteína, carbohidratos y grasas según tu objetivo y tus datos.", plan?.let { "${it.proteinGrams} g · ${it.carbohydrateGrams} g · ${it.fatGrams} g" } ?: "Sin definir", Icons.Outlined.Info) }
        item {
            Card(Modifier.padding(16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = KyvoColors.PurpleSoft), shape = MaterialTheme.shapes.large) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Outlined.Info, null, Modifier.size(38.dp), tint = KyvoColors.PurplePrimary)
                    Column(Modifier.padding(start = 14.dp)) {
                        Text("Importante", color = KyvoColors.PurplePrimary, fontWeight = FontWeight.Bold)
                        Text("Estos cálculos son estimaciones basadas en evidencia científica y pueden ajustarse con el tiempo según tu progreso y necesidades individuales. La nutrición es un proceso dinámico.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun CalculationStep(number: Int, title: String, description: String, value: String, icon: ImageVector) {
    Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Surface(Modifier.size(42.dp), RoundedCornerShape(14.dp), color = KyvoColors.PurpleSoft) { Icon(icon, null, Modifier.padding(10.dp), tint = KyvoColors.PurplePrimary) }
            Column(Modifier.weight(1f).padding(start = 14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("$number. $title", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, color = KyvoColors.PurplePrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PlanScaffold(tag: String, onBack: () -> Unit, title: String, content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
    Surface(Modifier.fillMaxSize().semantics { contentDescription = tag }, color = MaterialTheme.colorScheme.background) {
        LazyColumn(contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { IconButton(onClick = onBack, Modifier.size(48.dp).padding(start = 4.dp).semantics { contentDescription = "Volver" }) { Icon(Icons.Outlined.ArrowBack, "Volver") } }
            item { Text(title, Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black) }
            content()
        }
    }
}

@Composable
private fun PlanLoading(tag: String) { Surface(Modifier.fillMaxSize().semantics { contentDescription = tag }, color = MaterialTheme.colorScheme.background) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } } }

@Composable
private fun PlanIncomplete(tag: String, onboarding: SavedOnboarding, onBack: () -> Unit) { PlanScaffold(tag, onBack, "Mi plan nutricional") { item { Text("Tu plan aún no está disponible.", Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text("Completa tu información para generar una estimación personalizada.", Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) } } }

@Composable
private fun PlanError(tag: String, message: String, onRetry: () -> Unit, onBack: () -> Unit) { PlanScaffold(tag, onBack, "Mi plan nutricional") { item { Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(message, textAlign = TextAlign.Center); Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = KyvoColors.PurplePrimary), modifier = Modifier.padding(top = 16.dp)) { Text("Reintentar") } } } } }

private fun FitnessGoal.label() = when (this) {
    FitnessGoal.FatLoss -> "Bajar grasa"
    FitnessGoal.MuscleGain -> "Ganancia muscular"
    FitnessGoal.Recomposition -> "Recomposición corporal"
    FitnessGoal.Maintenance -> "Mantenimiento"
    FitnessGoal.Performance -> "Performance / fuerza"
}

private fun FitnessGoal.description() = when (this) {
    FitnessGoal.FatLoss -> "Déficit calórico para reducir grasa corporal."
    FitnessGoal.MuscleGain -> "Superávit controlado para ganar músculo."
    FitnessGoal.Recomposition -> "Gana músculo, pierde grasa y mejora tu composición corporal."
    FitnessGoal.Maintenance -> "Mantén tu composición corporal actual."
    FitnessGoal.Performance -> "Combustible para rendir y recuperar mejor."
}

private fun GenderOption.label() = when (this) {
    GenderOption.Male -> "Masculino"
    GenderOption.Female -> "Femenino"
    GenderOption.PreferNotToSay -> "No indicado"
}

private fun TrainingType.label() = when (this) {
    TrainingType.Strength -> "Fuerza"
    TrainingType.Hypertrophy -> "Hipertrofia"
    TrainingType.Functional -> "Funcional"
    TrainingType.Cardio -> "Cardio"
}

private fun WorkActivity.label() = when (this) {
    WorkActivity.Sedentary -> "Oficina (sedentario)"
    WorkActivity.Active -> "Activo"
    WorkActivity.Physical -> "Trabajo físico"
}

private fun com.kyvo.app.feature.onboarding.domain.model.FoodPreference.label() = when (this) {
    com.kyvo.app.feature.onboarding.domain.model.FoodPreference.None -> "Sin restricciones"
    com.kyvo.app.feature.onboarding.domain.model.FoodPreference.Vegetarian -> "Vegetariano"
    com.kyvo.app.feature.onboarding.domain.model.FoodPreference.Vegan -> "Vegano"
    com.kyvo.app.feature.onboarding.domain.model.FoodPreference.GlutenFree -> "Sin gluten"
    com.kyvo.app.feature.onboarding.domain.model.FoodPreference.DairyFree -> "Sin lácteos"
    com.kyvo.app.feature.onboarding.domain.model.FoodPreference.Other -> "Otra preferencia"
}
