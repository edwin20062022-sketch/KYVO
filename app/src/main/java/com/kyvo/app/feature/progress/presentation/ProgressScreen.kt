package com.kyvo.app.feature.progress.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kyvo.app.core.designsystem.KyvoColors
import com.kyvo.app.core.designsystem.component.KyvoCard
import com.kyvo.app.core.designsystem.component.KyvoPrimaryButton
import com.kyvo.app.core.designsystem.component.KyvoStateView
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import com.kyvo.app.feature.progress.domain.DailyNutritionSummary
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ProgressRoute(
    viewModel: ProgressViewModel,
    onHome: () -> Unit = {},
    onMeals: () -> Unit = {},
    onMealShare: () -> Unit = {},
    onCaloriesDetail: () -> Unit = {},
    onProteinDetail: () -> Unit = {},
    onConsistency: () -> Unit = {},
    onHistory: () -> Unit = {},
) {
    when (val state = viewModel.state.collectAsStateWithLifecycle().value) {
        ProgressUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        ProgressUiState.InsufficientData -> ProgressEmptyState(onMeals)
        is ProgressUiState.Error -> ProgressErrorState(state.message, viewModel::retry)
        is ProgressUiState.Content -> ProgressScreen(state.days, onHome, onMeals, onMealShare, onCaloriesDetail, onProteinDetail, onConsistency, onHistory)
    }
}

@Composable
fun ProgressScreen(
    days: List<DailyNutritionSummary>,
    onHome: () -> Unit = {},
    onMeals: () -> Unit = {},
    onMealShare: () -> Unit = {},
    onCaloriesDetail: () -> Unit = {},
    onProteinDetail: () -> Unit = {},
    onConsistency: () -> Unit = {},
    onHistory: () -> Unit = {},
) {
    val recentDays = days.takeLast(7)
    val latest = recentDays.lastOrNull() ?: return
    val averageCalories = recentDays.map { it.caloriesConsumed }.average().roundToInt()
    val completion = listOf(latest.caloriesProgress, latest.proteinProgress, latest.carbohydratesProgress, latest.fatProgress)
        .mapNotNull { it.ratio }.average().takeIf { !it.isNaN() } ?: 0.0

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 24.dp, bottom = 28.dp),
    ) {
        item {
            Text("Progreso", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Tu esfuerzo se refleja en tus hábitos.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleMedium)
            PeriodSelector()
        }
        item { CompletionCard(completion, recentDays) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NutrientCard("Calorías", "${latest.caloriesConsumed}", "de ${latest.caloriesTarget} kcal", latest.caloriesProgress.percentage, Modifier.weight(1f), onCaloriesDetail)
                NutrientCard("Proteína", "${latest.proteinConsumed} g", "de ${latest.proteinTarget} g", latest.proteinProgress.percentage, Modifier.weight(1f), onProteinDetail)
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NutrientCard("Carbohidratos", "${latest.carbohydratesConsumed} g", "de ${latest.carbohydratesTarget} g", latest.carbohydratesProgress.percentage, Modifier.weight(1f), null)
                NutrientCard("Grasas", "${latest.fatConsumed} g", "de ${latest.fatTarget} g", latest.fatProgress.percentage, Modifier.weight(1f), null)
            }
        }
        item { TrendCard("Tendencia de calorías", "Promedio diario: $averageCalories kcal") { BarTrend(recentDays) } }
        item { TrendCard("Macros en el tiempo", "Proteína, carbohidratos y grasas") { MacroTrend(recentDays) } }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FutureCard("Historial de días", "Revisa tu progreso día por día.", Modifier.weight(1f), onHistory)
                FutureCard("Consistencia nutricional", "Analiza tu cumplimiento en el tiempo.", Modifier.weight(1f), onConsistency)
            }
        }
    }
}

@Composable
private fun PeriodSelector() = Row(
    Modifier.fillMaxWidth().padding(top = 18.dp).clip(RoundedCornerShape(28.dp)).background(KyvoColors.PurpleSoft).padding(4.dp),
    horizontalArrangement = Arrangement.SpaceEvenly,
) {
    listOf("7 días", "30 días", "3 meses").forEachIndexed { index, label ->
        Box(Modifier.weight(1f).clip(RoundedCornerShape(24.dp)).background(if (index == 0) KyvoColors.PurplePrimary else Color.Transparent).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
            Text(label, color = if (index == 0) Color.White else KyvoColors.Slate, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun CompletionCard(completion: Double, days: List<DailyNutritionSummary>) = KyvoCard {
    Text("CUMPLIMIENTO GENERAL", color = KyvoColors.Slate, style = MaterialTheme.typography.labelLarge)
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("${(completion * 100).roundToInt()}%", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
            Text("Tus hábitos nutricionales\nvan por buen camino.", color = KyvoColors.Slate)
        }
        BarTrend(days, Modifier.weight(1.3f))
    }
}

@Composable
private fun NutrientCard(name: String, value: String, target: String, percentage: Int?, modifier: Modifier, onClick: (() -> Unit)?) = KyvoCard(if (onClick == null) modifier else modifier.clickable(onClick = onClick), androidx.compose.foundation.layout.PaddingValues(16.dp)) {
    Text(name, color = KyvoColors.PurpleDeep, fontWeight = FontWeight.SemiBold)
    Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp))
    Text(target, color = KyvoColors.Slate, style = MaterialTheme.typography.bodySmall)
    Text(percentage?.let { "${it}% de tu meta" } ?: "Meta no disponible", color = KyvoColors.PurplePrimary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 12.dp))
}

@Composable
private fun TrendCard(title: String, subtitle: String, content: @Composable () -> Unit) = KyvoCard {
    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text(subtitle, color = KyvoColors.Slate)
    Spacer(Modifier.height(12.dp))
    content()
}

@Composable
private fun BarTrend(days: List<DailyNutritionSummary>, modifier: Modifier = Modifier) {
    val max = (days.maxOfOrNull { it.caloriesConsumed } ?: 1).coerceAtLeast(1)
    Row(modifier.fillMaxWidth().height(116.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
        days.forEach { day ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                Box(Modifier.width(24.dp).height((80f * day.caloriesConsumed / max).dp.coerceAtLeast(4.dp)).clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)).background(KyvoColors.PurpleAccent))
                Text(day.date.format(DateTimeFormatter.ofPattern("E", Locale("es", "MX"))).take(1).uppercase(), color = KyvoColors.Slate, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}

@Composable
private fun MacroTrend(days: List<DailyNutritionSummary>) {
    Canvas(Modifier.fillMaxWidth().height(130.dp)) {
        if (days.isEmpty()) return@Canvas
        fun line(values: List<Int>, color: Color) {
            val max = values.maxOrNull()?.coerceAtLeast(1) ?: 1
            val path = Path()
            values.forEachIndexed { index, value ->
                val x = if (values.size == 1) size.width / 2 else size.width * index / (values.lastIndex).toFloat()
                val y = size.height - size.height * value / max
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, color, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f))
        }
        line(days.map { it.proteinConsumed }, KyvoColors.PurplePrimary)
        line(days.map { it.carbohydratesConsumed }, KyvoColors.PurpleAccent)
        line(days.map { it.fatConsumed }, KyvoColors.Slate)
    }
}

@Composable
private fun FutureCard(title: String, subtitle: String, modifier: Modifier, onClick: () -> Unit) = KyvoCard(modifier.clickable(onClick = onClick), androidx.compose.foundation.layout.PaddingValues(16.dp)) {
    Text(title, fontWeight = FontWeight.Bold)
    Text(subtitle, color = KyvoColors.Slate, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
    Text("›", color = KyvoColors.PurplePrimary, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.align(Alignment.End))
}

@Composable
private fun ProgressEmptyState(onMeals: () -> Unit) = Column(Modifier.fillMaxSize()) {
    Column(Modifier.weight(1f).fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Progreso", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text("Tu esfuerzo se refleja en tus hábitos.", color = KyvoColors.Slate, style = MaterialTheme.typography.titleMedium)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { KyvoStateView(Icons.Outlined.Info, "Estamos construyendo tu progreso", "Registra tus comidas durante los próximos días para empezar a ver tus tendencias y conocer mejor tu nutrición.", actionLabel = "Registrar mi primera comida", onAction = onMeals) }
    }
}

@Composable private fun ProgressErrorState(message: String, retry: () -> Unit) = Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
    Text(message, color = KyvoColors.Slate, textAlign = TextAlign.Center)
    KyvoPrimaryButton("Reintentar", retry, Modifier.padding(top = 20.dp))
}

@Composable
fun ProgressPlaceholder(title: String, onBack: () -> Unit) = Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
    Text("‹", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.size(48.dp).clickable(onClick = onBack).padding(8.dp))
    Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text("Este detalle forma parte del siguiente checkpoint de Progreso.", color = KyvoColors.Slate)
}
