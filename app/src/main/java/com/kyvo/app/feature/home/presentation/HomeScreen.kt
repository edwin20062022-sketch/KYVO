package com.kyvo.app.feature.home.presentation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyvo.app.R
import com.kyvo.app.core.designsystem.KyvoBrushes
import com.kyvo.app.core.designsystem.KyvoColors
import com.kyvo.app.core.designsystem.component.KyvoCard
import com.kyvo.app.core.designsystem.component.KyvoPrimaryButton
import com.kyvo.app.feature.home.domain.model.DailyNutrition
import com.kyvo.app.feature.home.domain.model.Meal
import com.kyvo.app.feature.home.domain.model.progressOf
import com.kyvo.app.feature.home.domain.repository.MealRepository
import com.kyvo.app.feature.onboarding.domain.repository.OnboardingRepository
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

internal val MACRO_RING_DIAMETER = 84.dp
internal val MACRO_RING_SPACING = 8.dp
internal val MACRO_ICON_SIZE = 22.dp
internal val MACRO_LABEL_FONT_SIZE = 10.sp
internal val DASHBOARD_MACRO_LABELS = listOf("Proteína", "Carbohidratos", "Grasas")
internal const val MACRO_CIRCLE_SHOWS_PERCENTAGE = false

@Composable
fun HomeRoute(onboardingRepository: OnboardingRepository, mealRepository: MealRepository, onAddFood: () -> Unit = {}, onMealShare: () -> Unit = {}, viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(onboardingRepository, mealRepository))) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val destination by viewModel.destination.collectAsStateWithLifecycle()
    when (val screen = destination) {
        HomeDestination.Dashboard -> HomeScreen(state, viewModel::onEvent, onAddFood, onMealShare)
        HomeDestination.NutritionDetail -> when (val value = state) { is HomeUiState.Content -> NutritionDetailScreen(value.daily, onBack = { viewModel.onEvent(HomeEvent.BackToDashboard) }); else -> HomeScreen(value, viewModel::onEvent, onAddFood, onMealShare) }
        is HomeDestination.MealDetail -> MealDetailRoute(mealRepository, screen.mealId, onBack = { viewModel.onEvent(HomeEvent.BackToDashboard) }, onEdit = { viewModel.onEvent(HomeEvent.OpenMealEditor(it)) }, onDelete = { viewModel.onEvent(HomeEvent.DeleteMeal(it)) })
        is HomeDestination.EditMeal -> { val meal by viewModel.mealForEditing(screen.mealId).collectAsStateWithLifecycle(null); MealEditorScreen(meal, onBack = { viewModel.onEvent(HomeEvent.BackToDashboard) }, onSave = { viewModel.onEvent(HomeEvent.UpdateMeal(it)) }, onDelete = { viewModel.onEvent(HomeEvent.DeleteMeal(it)) }) }
    }
}

@Composable
fun HomeScreen(state: HomeUiState, onEvent: (HomeEvent) -> Unit, onAddFood: () -> Unit = {}, onMealShare: () -> Unit = {}) = Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    when (state) {
        HomeUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        is HomeUiState.Error -> ErrorHomeState(state.message, onRetry = { onEvent(HomeEvent.Retry) })
        is HomeUiState.Content -> LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { DashboardHeader() }
            item { DailySummary(state.daily) }
            item { MacroSummary(state.daily, onDetail = { onEvent(HomeEvent.OpenNutritionDetail) }) }
            item { MealsSection(state.daily.meals, onMeal = { onEvent(HomeEvent.OpenMeal(it.id)) }, onAddFood = onAddFood, onMealShare = onMealShare) }
            item { FocusCard() }
            item { BottomNavigationShell(onMealShare) }
        }
    }
}

@Composable
fun HomeScreen(state: HomeUiState, onAddFood: () -> Unit) = HomeScreen(state, onEvent = {}, onAddFood = onAddFood)

@Composable private fun DashboardHeader() = Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 28.dp), verticalAlignment = Alignment.Top) {
    Column(Modifier.weight(1f)) { Text("Hola", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold); Text("Listo para alimentar tu mejor versión.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
    Box(Modifier.size(52.dp).clip(CircleShape).background(KyvoColors.PurpleSoft), contentAlignment = Alignment.Center) { Text("K", color = KyvoColors.PurplePrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
}

@Composable private fun DailySummary(daily: DailyNutrition) = KyvoCard(Modifier.padding(horizontal = 20.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(28.dp)) {
    Column(Modifier.fillMaxWidth().background(KyvoColors.PurpleDeep, MaterialTheme.shapes.large).padding(20.dp)) {
        Text("Resumen de hoy", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(daily.date.format(DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("es", "MX"))), color = Color.White.copy(alpha = .78f), modifier = Modifier.padding(top = 4.dp))
        Row(Modifier.fillMaxWidth().padding(top = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text("Calorías", color = Color.White); Text("${daily.caloriesConsumed} / ${daily.calorieTarget} kcal", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); Text("${daily.caloriesRemaining} kcal restantes", color = Color.White.copy(alpha = .8f)) }
            ProgressRing(progressOf(daily.caloriesConsumed, daily.calorieTarget), "${percent(progressOf(daily.caloriesConsumed, daily.calorieTarget))}% de tu meta", 112.dp, Color.White)
        }
        LinearProgress(progressOf(daily.caloriesConsumed, daily.calorieTarget), Color.White, Color.White.copy(.18f), Modifier.padding(top = 16.dp))
    }
}

@Composable private fun MacroSummary(daily: DailyNutrition, onDetail: () -> Unit) = KyvoCard(Modifier.padding(horizontal = 20.dp)) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("Macros", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Text("Ver detalle ›", color = KyvoColors.PurplePrimary, modifier = Modifier.heightIn(min = 48.dp).clickable(onClick = onDetail).padding(top = 12.dp)) }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(MACRO_RING_SPACING)) {
        Box(Modifier.weight(1f)) { MacroRing(DASHBOARD_MACRO_LABELS[0], daily.proteinConsumed, daily.proteinTarget, R.drawable.ic_home_protein) }
        Box(Modifier.weight(1f)) { MacroRing(DASHBOARD_MACRO_LABELS[1], daily.carbohydrateConsumed, daily.carbohydrateTarget, R.drawable.ic_home_carbohydrates) }
        Box(Modifier.weight(1f)) { MacroRing(DASHBOARD_MACRO_LABELS[2], daily.fatConsumed, daily.fatTarget, R.drawable.ic_home_fat) }
    }
}

@Composable private fun MacroRing(name: String, consumed: Int, target: Int, @DrawableRes icon: Int) = Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val labelFontSize = macroLabelFontSizeFor(maxWidth)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                ProgressRing(progressOf(consumed, target), "$name: $consumed de $target gramos", MACRO_RING_DIAMETER, KyvoColors.PurplePrimary, showPercentage = MACRO_CIRCLE_SHOWS_PERCENTAGE)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.size(MACRO_RING_DIAMETER)
                ) {
                    androidx.compose.foundation.Image(painterResource(icon), null, Modifier.size(MACRO_ICON_SIZE), contentScale = ContentScale.Fit)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${percent(progressOf(consumed, target))}%",
                        color = KyvoColors.PurplePrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier
                    )
                }
            }
            Text(name, color = KyvoColors.PurplePrimary, textAlign = TextAlign.Center, maxLines = 1, softWrap = false, overflow = TextOverflow.Clip, fontSize = labelFontSize, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
            Text("$consumed / $target g", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, maxLines = 1)
        }
    }
}

internal fun macroLabelFontSizeFor(availableWidth: androidx.compose.ui.unit.Dp) = if (availableWidth < 80.dp) 9.sp else MACRO_LABEL_FONT_SIZE

@Composable private fun MealsSection(meals: List<Meal>, onMeal: (Meal) -> Unit, onAddFood: () -> Unit, onMealShare: () -> Unit) = KyvoCard(Modifier.padding(horizontal = 20.dp)) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("Comidas de hoy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Text("＋ Añadir comida", color = KyvoColors.PurplePrimary, modifier = Modifier.heightIn(min = 48.dp).clickable(onClick = onAddFood).padding(top = 12.dp)) }
    if (meals.isEmpty()) EmptyMealsState(onAddFood, onMealShare) else meals.forEachIndexed { index, meal -> MealRow(meal, onClick = { onMeal(meal) }); if (index < meals.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(.55f)) }
}

@Composable private fun EmptyMealsState(onAddFood: () -> Unit, onMealShare: () -> Unit) = Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
    androidx.compose.foundation.Image(painterResource(R.drawable.ic_home_empty_meals), null, Modifier.size(150.dp), contentScale = ContentScale.Fit)
    Text("Aún no registras comidas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text("Registra tu primera comida y empieza a avanzar hacia tus objetivos.", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp))
    Row(Modifier.fillMaxWidth().padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) { HomeCta("Meal Share", Modifier.weight(1f), false, onMealShare); HomeCta("Añadir comida", Modifier.weight(1f), true, onAddFood) }
}

@Composable private fun HomeCta(text: String, modifier: Modifier, filled: Boolean, onClick: () -> Unit = {}) = Box(modifier.heightIn(min = 56.dp).clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick).then(if (filled) Modifier.background(KyvoBrushes.PrimaryAction) else Modifier.background(KyvoColors.PurpleSoft)).padding(12.dp), contentAlignment = Alignment.Center) { Text(text, color = if (filled) Color.White else KyvoColors.PurplePrimary, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center) }

@Composable private fun MealRow(meal: Meal, onClick: () -> Unit) = Row(Modifier.fillMaxWidth().heightIn(min = 78.dp).clickable(onClick = onClick).padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(50.dp).clip(CircleShape).background(KyvoColors.PurpleSoft), contentAlignment = Alignment.Center) { Text(meal.type.label.take(1), color = KyvoColors.PurplePrimary, fontWeight = FontWeight.Bold) }; Column(Modifier.weight(1f).padding(start = 12.dp)) { Text(meal.type.label, fontWeight = FontWeight.Bold); Text(meal.title, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis); meal.time?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = KyvoColors.PurplePrimary) } }; Text("${meal.totalCalories} kcal  ›", fontWeight = FontWeight.SemiBold) }

@Composable private fun FocusCard() = KyvoCard(Modifier.padding(horizontal = 20.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) { Row(Modifier.fillMaxWidth().background(KyvoColors.PurpleSoft).padding(16.dp), verticalAlignment = Alignment.CenterVertically) { androidx.compose.foundation.Image(painterResource(R.drawable.ic_home_focus), null, Modifier.size(56.dp), contentScale = ContentScale.Fit); Column(Modifier.padding(start = 12.dp)) { Text("Tu enfoque de hoy", fontWeight = FontWeight.Bold); Text("Cada comida cuenta. Alimenta tu rendimiento.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) } } }
@Composable private fun BottomNavigationShell(onMealShare: () -> Unit) = Row(Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceAround) {
    listOf("Inicio", "Comidas", "Progreso", "Perfil").forEach { Text(it, color = if (it == "Inicio") KyvoColors.PurplePrimary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (it == "Inicio") FontWeight.Bold else FontWeight.Normal) }
    Text("＋", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, modifier = Modifier.heightIn(min = 48.dp).clickable(onClick = onMealShare).semantics { contentDescription = "Meal Share" })
}

@Composable fun NutritionDetailScreen(daily: DailyNutrition, onBack: () -> Unit) = LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { item { Text("‹", Modifier.size(48.dp).clickable(onClick = onBack).padding(10.dp), style = MaterialTheme.typography.headlineMedium); Text("Detalle nutricional", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold); Text(daily.date.format(DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("es", "MX"))), color = MaterialTheme.colorScheme.onSurfaceVariant) }; item { DetailCalories(daily) }; item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { DetailMacro("Proteína", daily.proteinConsumed, daily.proteinTarget, Modifier.weight(1f)); DetailMacro("Carbohidratos", daily.carbohydrateConsumed, daily.carbohydrateTarget, Modifier.weight(1f)); DetailMacro("Grasas", daily.fatConsumed, daily.fatTarget, Modifier.weight(1f)) } }; item { MacroBreakdown(daily) } }
@Composable private fun DetailCalories(d: DailyNutrition) = KyvoCard { Text("Calorías", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("${d.caloriesConsumed} kcal", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold); Text("de ${d.calorieTarget} kcal", color = MaterialTheme.colorScheme.onSurfaceVariant); LinearProgress(progressOf(d.caloriesConsumed, d.calorieTarget), KyvoColors.PurplePrimary, KyvoColors.Outline, Modifier.padding(top = 14.dp)); Text("${d.caloriesRemaining} kcal restantes", color = KyvoColors.PurplePrimary, modifier = Modifier.padding(top = 8.dp)) }; ProgressRing(progressOf(d.caloriesConsumed, d.calorieTarget), "${percent(progressOf(d.caloriesConsumed,d.calorieTarget))}% de tu meta", 104.dp, KyvoColors.PurplePrimary) } }
@Composable private fun DetailMacro(name: String, consumed: Int, target: Int, modifier: Modifier) = KyvoCard(modifier, androidx.compose.foundation.layout.PaddingValues(12.dp)) { Text(name, color = KyvoColors.PurplePrimary, maxLines = 1, overflow = TextOverflow.Ellipsis); Text("$consumed g", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text("de $target g", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall); LinearProgress(progressOf(consumed, target), KyvoColors.PurplePrimary, KyvoColors.Outline, Modifier.padding(top = 8.dp)); Text("${percent(progressOf(consumed, target))}%", color = KyvoColors.PurplePrimary, modifier = Modifier.align(Alignment.End)) }
@Composable private fun MacroBreakdown(d: DailyNutrition) = KyvoCard { Text("Desglose de macronutrientes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); BreakdownRow("Proteína", d.proteinConsumed, d.proteinTarget); BreakdownRow("Carbohidratos", d.carbohydrateConsumed, d.carbohydrateTarget); BreakdownRow("Grasas", d.fatConsumed, d.fatTarget) }
@Composable private fun BreakdownRow(name: String, consumed: Int, target: Int) = Column(Modifier.padding(top = 16.dp)) { Row(Modifier.fillMaxWidth()) { Text(name, Modifier.weight(1f), fontWeight = FontWeight.SemiBold); Text("$consumed g / $target g") }; LinearProgress(progressOf(consumed, target), KyvoColors.PurplePrimary, KyvoColors.Outline, Modifier.padding(top = 6.dp)) }

@Composable fun MealEditorScreen(meal: Meal?, onBack: () -> Unit, onSave: (Meal) -> Unit, onDelete: (String) -> Unit) {
    if (meal == null) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("La comida ya no está disponible") }; return }
    var portionFactor by remember(meal.id) { mutableFloatStateOf(1f) }
    val editedMeal = meal.scaled(portionFactor)
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("‹", Modifier.size(48.dp).clickable(onClick = onBack).padding(10.dp), style = MaterialTheme.typography.headlineMedium); Text("Editar comida", Modifier.weight(1f), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold); Text("Guardar", Modifier.heightIn(min = 48.dp).clickable { onSave(editedMeal) }.padding(12.dp), color = KyvoColors.PurplePrimary, fontWeight = FontWeight.Bold) }; Text(meal.title, style = MaterialTheme.typography.titleLarge); Text("${meal.type.label} · ${meal.time.orEmpty()}", color = KyvoColors.PurplePrimary) }
        item { MealTotals(editedMeal) }
        item { KyvoCard { Text("Alimentos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); editedMeal.items.forEachIndexed { i, item -> FoodItemRow(item); if (i < editedMeal.items.lastIndex) HorizontalDivider() } } }
        item { KyvoCard { Text("Ajustar porción total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text("Multiplica o divide las cantidades de todos los alimentos.", color = MaterialTheme.colorScheme.onSurfaceVariant); Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) { PortionAction("−") { portionFactor = (portionFactor - .25f).coerceAtLeast(.25f) }; Text(String.format(Locale.US, "%.2fx", portionFactor), modifier = Modifier.padding(horizontal = 18.dp), fontWeight = FontWeight.Bold); PortionAction("+") { portionFactor += .25f } } } }
        item { Box(Modifier.fillMaxWidth().heightIn(min = 56.dp).clip(RoundedCornerShape(16.dp)).background(KyvoColors.Error.copy(.1f)).clickable { onDelete(meal.id) }, contentAlignment = Alignment.Center) { Text("Eliminar comida", color = KyvoColors.Error, fontWeight = FontWeight.Bold) } }
    }
}
@Composable private fun PortionAction(symbol: String, onClick: () -> Unit) = Box(Modifier.size(48.dp).clip(CircleShape).background(KyvoColors.PurpleSoft).clickable(onClick = onClick), contentAlignment = Alignment.Center) { Text(symbol, color = KyvoColors.PurplePrimary, style = MaterialTheme.typography.titleLarge) }
private fun Meal.scaled(factor: Float) = copy(items = items.map { it.copy(quantity = it.quantity * factor, grams = it.grams?.times(factor), calories = (it.calories * factor).roundToInt(), protein = (it.protein * factor).roundToInt(), carbohydrates = (it.carbohydrates * factor).roundToInt(), fat = (it.fat * factor).roundToInt()) }, totalCalories = (totalCalories * factor).roundToInt(), protein = (protein * factor).roundToInt(), carbohydrates = (carbohydrates * factor).roundToInt(), fat = (fat * factor).roundToInt())
@Composable private fun MealTotals(m: Meal) = KyvoCard { Text("Totales de la comida", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text("${m.totalCalories} kcal", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { listOf("Proteína" to m.protein, "Carbohidratos" to m.carbohydrates, "Grasas" to m.fat).forEach { (n,v) -> Column { Text(n, color = KyvoColors.PurplePrimary); Text("$v g", fontWeight = FontWeight.Bold) } } } }
@Composable private fun FoodItemRow(item: com.kyvo.app.feature.home.domain.model.MealItem) = Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(44.dp).clip(CircleShape).background(KyvoColors.PurpleSoft), contentAlignment = Alignment.Center) { Text(item.name.take(1), color = KyvoColors.PurplePrimary) }; Column(Modifier.weight(1f).padding(start = 12.dp)) { Text(item.name, fontWeight = FontWeight.SemiBold); Text("${item.quantity} ${item.unit} · ${item.protein} g proteína · ${item.carbohydrates} g carbs", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }; Text("${item.calories} kcal") }
@Composable private fun ProgressRing(progress: Float, description: String, size: androidx.compose.ui.unit.Dp, color: Color, showPercentage: Boolean = true) = Box(Modifier.size(size).semantics { contentDescription = description }, contentAlignment = Alignment.Center) { Canvas(Modifier.fillMaxSize()) { drawArc(KyvoColors.Outline.copy(.65f), -90f, 360f, false, style = Stroke(10.dp.toPx(), cap = StrokeCap.Round)); drawArc(color, -90f, 360f * progress.coerceIn(0f,1f), false, style = Stroke(10.dp.toPx(), cap = StrokeCap.Round)) }; if (showPercentage) Text("${percent(progress)}%", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge) }
@Composable private fun LinearProgress(progress: Float, color: Color, track: Color, modifier: Modifier = Modifier) = Box(modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(track)) { Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(8.dp).background(color)) }
private fun percent(progress: Float) = (progress * 100).roundToInt()
@Composable private fun ErrorHomeState(message: String, onRetry: () -> Unit) = Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) { Text(message, textAlign = TextAlign.Center); Spacer(Modifier.height(16.dp)); KyvoPrimaryButton("Reintentar", onRetry) }
