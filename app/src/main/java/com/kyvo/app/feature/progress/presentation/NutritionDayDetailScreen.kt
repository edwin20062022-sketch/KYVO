package com.kyvo.app.feature.progress.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kyvo.app.core.designsystem.KyvoColors
import com.kyvo.app.core.designsystem.component.KyvoCard
import com.kyvo.app.core.designsystem.component.KyvoPrimaryButton
import com.kyvo.app.feature.home.domain.model.Meal
import com.kyvo.app.feature.home.domain.model.MealItem
import com.kyvo.app.feature.progress.domain.DailyNutritionSummary
import com.kyvo.app.feature.progress.domain.NutritionConsistencyStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun NutritionDayDetailRoute(viewModel: NutritionDayDetailViewModel, onBack: () -> Unit) {
    when (val state = viewModel.state.collectAsStateWithLifecycle().value) {
        NutritionDayDetailUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        NutritionDayDetailUiState.Empty -> DayEmpty(viewModel.date, onBack)
        is NutritionDayDetailUiState.Error -> DayError(state.message, viewModel::retry, onBack)
        is NutritionDayDetailUiState.Content -> NutritionDayDetailScreen(viewModel.date, state.summary, state.meals, onBack)
    }
}

@Composable
private fun NutritionDayDetailScreen(date: LocalDate, summary: DailyNutritionSummary, meals: List<Meal>, onBack: () -> Unit) {
    val percentage = summary.caloriesProgress.ratio?.times(100)?.roundToInt()
    val consistency = com.kyvo.app.feature.progress.domain.calculateConsistency(listOf(summary)).dailyStatuses.single()
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(top = 18.dp, bottom = 32.dp)) {
        item {
            Row(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("‹", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.size(48.dp).clickable(onClick = onBack).semantics { contentDescription = "Volver a Historial" })
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(dateTitle(date), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text("Tu resumen nutricional del día.", color = KyvoColors.Slate)
                }
                Spacer(Modifier.size(48.dp))
            }
        }
        item { DaySummaryCard(summary, percentage, consistency.status) }
        item {
            KyvoCard(Modifier.padding(horizontal = 20.dp), PaddingValues(16.dp)) {
                Text("Macros", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    MacroValue("${summary.proteinConsumed} g", "de ${summary.proteinTarget} g", "Proteína")
                    MacroValue("${summary.carbohydratesConsumed} g", "de ${summary.carbohydratesTarget} g", "Carbohidratos")
                    MacroValue("${summary.fatConsumed} g", "de ${summary.fatTarget} g", "Grasas")
                }
            }
        }
        item { Text("Comidas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 20.dp)) }
        item {
            KyvoCard(Modifier.padding(horizontal = 20.dp), PaddingValues(0.dp)) {
                meals.forEachIndexed { index, meal ->
                    DayMealRow(meal)
                    if (index < meals.lastIndex) androidx.compose.material3.HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun DaySummaryCard(summary: DailyNutritionSummary, percentage: Int?, status: NutritionConsistencyStatus) = KyvoCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), PaddingValues(18.dp)) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(150.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(progress = { (summary.caloriesProgress.ratio ?: 0f).coerceAtMost(1f) }, modifier = Modifier.fillMaxSize(), color = KyvoColors.PurplePrimary, trackColor = KyvoColors.PurpleSoft, strokeWidth = 11.dp)
            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("${summary.caloriesConsumed}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); Text("de ${summary.caloriesTarget} kcal", color = KyvoColors.Slate) }
        }
        Column(Modifier.padding(start = 20.dp).weight(1f)) {
            Text(percentage?.let { "$it%" } ?: "No disponible", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("de tu meta diaria", color = KyvoColors.Slate)
            Text(
                when (status) {
                    NutritionConsistencyStatus.CONSISTENT -> "Día dentro del rango"
                    NutritionConsistencyStatus.INCONSISTENT -> "Día fuera del rango"
                    NutritionConsistencyStatus.NOT_EVALUABLE -> "Sin datos suficientes"
                },
                color = when (status) {
                    NutritionConsistencyStatus.CONSISTENT -> KyvoColors.Success
                    NutritionConsistencyStatus.INCONSISTENT -> KyvoColors.Warning
                    NutritionConsistencyStatus.NOT_EVALUABLE -> KyvoColors.Slate
                },
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable private fun MacroValue(value: String, target: String, label: String) = Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(96.dp)) { Text(value, fontWeight = FontWeight.Bold); Text(target, color = KyvoColors.Slate, style = MaterialTheme.typography.bodySmall); Text(label, color = KyvoColors.PurplePrimary, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp)) }

@Composable
private fun DayMealRow(meal: Meal) {
    Column(Modifier.fillMaxWidth().padding(16.dp).semantics { contentDescription = "${meal.type.label}, ${meal.title}, ${meal.totalCalories} calorías" }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(KyvoColors.PurpleSoft), contentAlignment = Alignment.Center) { Text(meal.type.label.take(1), color = KyvoColors.PurplePrimary, fontWeight = FontWeight.Bold) }
            Column(Modifier.weight(1f).padding(start = 12.dp)) { Text(meal.type.label, fontWeight = FontWeight.Bold); Text(meal.title, color = KyvoColors.Slate, maxLines = 1, overflow = TextOverflow.Ellipsis); meal.time?.let { Text(it, color = KyvoColors.PurplePrimary, style = MaterialTheme.typography.bodySmall) } }
            Text("${meal.totalCalories} kcal", fontWeight = FontWeight.Bold)
        }
        meal.items.forEach { DayMealItem(it) }
    }
}

@Composable private fun DayMealItem(item: MealItem) = Row(Modifier.fillMaxWidth().padding(start = 60.dp, top = 10.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(item.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis); Text("${item.quantity.cleanAmount()} ${item.unit} · ${item.protein} g proteína · ${item.carbohydrates} g carbs", color = KyvoColors.Slate, style = MaterialTheme.typography.bodySmall) }; Text("${item.calories} kcal", style = MaterialTheme.typography.bodySmall) }

private fun Double.cleanAmount(): String = if (this % 1.0 == 0.0) toInt().toString() else "%.1f".format(Locale.US, this)
private fun dateTitle(date: LocalDate) = "${date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es", "MX")).replaceFirstChar { it.uppercase() }}, ${date.dayOfMonth} de ${date.month.getDisplayName(TextStyle.FULL, Locale("es", "MX"))}"

@Composable private fun DayEmpty(date: LocalDate, onBack: () -> Unit) = Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Text(dateTitle(date), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center); Text("No hay comidas registradas para este día.", color = KyvoColors.Slate, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 10.dp)); KyvoPrimaryButton("Volver a Historial", onBack, Modifier.padding(top = 20.dp)) }

@Composable private fun DayError(message: String, retry: () -> Unit, onBack: () -> Unit) = Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Text(message, color = KyvoColors.Slate, textAlign = TextAlign.Center); KyvoPrimaryButton("Reintentar", retry, Modifier.padding(top = 20.dp)); Text("Volver", color = KyvoColors.PurplePrimary, modifier = Modifier.padding(top = 18.dp).clickable(onClick = onBack)) }
