package com.kyvo.app.feature.progress.presentation

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kyvo.app.core.designsystem.KyvoColors
import com.kyvo.app.core.designsystem.component.KyvoCard
import com.kyvo.app.core.designsystem.component.KyvoPrimaryButton
import com.kyvo.app.feature.progress.domain.NutritionMetric
import com.kyvo.app.feature.progress.domain.NutritionMetricDetail
import com.kyvo.app.feature.progress.domain.NutritionProgressPoint
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun NutritionMetricDetailRoute(
    viewModel: NutritionMetricDetailViewModel,
    onBack: () -> Unit,
) {
    when (val state = viewModel.state.collectAsStateWithLifecycle().value) {
        NutritionMetricDetailUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        NutritionMetricDetailUiState.Empty -> MetricDetailEmptyState(viewModel.metric.title, onBack)
        is NutritionMetricDetailUiState.Error -> MetricDetailErrorState(state.message, viewModel::retry)
        is NutritionMetricDetailUiState.Content -> NutritionMetricDetailScreen(state.detail, onBack)
    }
}

@Composable
fun NutritionMetricDetailScreen(detail: NutritionMetricDetail, onBack: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 28.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("‹", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.size(48.dp).clickable(onClick = onBack).padding(8.dp))
                Text(if (detail.metric == NutritionMetric.PROTEIN) "Progreso" else "", color = KyvoColors.Slate, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                PeriodPill(detail.metric)
            }
            Text(detail.metric.title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp))
            Text(detail.metric.subtitle, color = KyvoColors.Slate, style = MaterialTheme.typography.titleMedium)
        }
        item { MetricSummary(detail) }
        item { MetricChart(detail) }
        if (detail.metric == NutritionMetric.CALORIES) {
            item { CaloriesSummary(detail) }
            item { InsightCard(detail) }
            item { CaloriesDayList(detail) }
        } else {
            item { ProteinTip() }
            item { ProteinPeriodSummary(detail) }
            item { ProteinSources(detail) }
        }
    }
}

@Composable
private fun PeriodPill(metric: NutritionMetric) = Box(
    Modifier.clip(RoundedCornerShape(24.dp)).background(KyvoColors.PurpleSoft).padding(horizontal = 18.dp, vertical = 12.dp),
    contentAlignment = Alignment.Center,
) { Text(if (metric == NutritionMetric.PROTEIN) "7 días" else "Últimos 7 días", fontWeight = FontWeight.SemiBold) }

@Composable
private fun MetricSummary(detail: NutritionMetricDetail) = KyvoCard {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(82.dp).clip(CircleShape).background(KyvoColors.PurpleSoft), contentAlignment = Alignment.Center) {
            Text(if (detail.metric == NutritionMetric.PROTEIN) "P" else "✦", color = KyvoColors.PurplePrimary, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.weight(1f).padding(start = 18.dp)) {
            Text("Promedio diario", color = KyvoColors.Slate, style = MaterialTheme.typography.titleMedium)
            Text("${detail.averageConsumed} ${detail.metric.unit}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Text("de ${detail.target} ${detail.metric.unit} meta", color = KyvoColors.Slate, style = MaterialTheme.typography.titleMedium)
        }
        MetricRing(detail.averagePercentage)
    }
}

@Composable
private fun MetricRing(percentage: Int?) = Box(Modifier.size(112.dp), contentAlignment = Alignment.Center) {
    Canvas(Modifier.fillMaxSize()) {
        drawArc(KyvoColors.Outline.copy(alpha = .45f), -90f, 360f, false, style = Stroke(10.dp.toPx()))
        percentage?.let { drawArc(KyvoColors.PurplePrimary, -90f, 360f * (it / 100f).coerceIn(0f, 1f), false, style = Stroke(10.dp.toPx())) }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(percentage?.let { "$it%" } ?: "N/D", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("de tu meta", color = KyvoColors.Slate, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun MetricChart(detail: NutritionMetricDetail) = KyvoCard {
    Text(if (detail.metric == NutritionMetric.PROTEIN) "Consumo diario de proteína" else "Consumo diario de calorías", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.End) {
        Text("- - -  Meta ${detail.target} ${detail.metric.unit}", color = KyvoColors.PurplePrimary, style = MaterialTheme.typography.bodySmall)
    }
    DetailBarChart(detail.points, Modifier.padding(top = 12.dp))
}

@Composable
private fun DetailBarChart(points: List<NutritionProgressPoint>, modifier: Modifier = Modifier) {
    val maxValue = maxOf(points.maxOfOrNull { it.consumed } ?: 0, points.maxOfOrNull { it.target } ?: 0, 1)
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().height(170.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
            points.forEach { point ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                    Text("${point.consumed}", style = MaterialTheme.typography.labelSmall, color = KyvoColors.Ink)
                    Box(Modifier.padding(top = 4.dp).width(26.dp).height((120f * point.consumed / maxValue).dp.coerceAtLeast(4.dp)).clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)).background(KyvoColors.PurpleAccent))
                    Text(dayLabel(point), color = KyvoColors.Slate, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
        Canvas(Modifier.fillMaxWidth().height(1.dp)) { }
    }
}

private fun dayLabel(point: NutritionProgressPoint): String = point.date.format(DateTimeFormatter.ofPattern("E", Locale("es", "MX"))).take(1).uppercase()

@Composable
private fun CaloriesSummary(detail: NutritionMetricDetail) = SummaryStrip(
    listOf(
        "+${differencePercentage(detail)}%" to "vs. semana anterior",
        "${detail.daysWithMeals} de ${detail.points.size}" to "días dentro del rango",
        "${detail.averageConsumed - detail.target} ${detail.metric.unit}" to "diferencia promedio vs. tu meta",
    ),
)

@Composable
private fun ProteinPeriodSummary(detail: NutritionMetricDetail) = SummaryStrip(
    listOf(
        "${detail.totalConsumed} g" to "Total en 7 días",
        "${detail.daysWithMeals} de ${detail.points.size}" to "días en rango",
        "${detail.averagePercentage?.let { if (it >= 100) "+${it - 100}" else "${it - 100}" } ?: "N/D"}%" to "vs. tu meta",
    ),
)

@Composable
private fun SummaryStrip(values: List<Pair<String, String>>) = Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
    values.forEach { (value, label) ->
        KyvoCard(Modifier.weight(1f), PaddingValues(12.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, color = KyvoColors.Slate, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun InsightCard(detail: NutritionMetricDetail) = KyvoCard(Modifier.background(KyvoColors.PurpleSoft)) {
    Text("Insight", color = KyvoColors.PurplePrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text(if ((detail.averageRatio ?: 0.0) >= 1.0) "Tu consumo calórico está por encima de tu meta promedio." else "Tu consumo calórico se ha mantenido por debajo de tu meta promedio.", color = KyvoColors.Slate, modifier = Modifier.padding(top = 6.dp))
}

@Composable
private fun CaloriesDayList(detail: NutritionMetricDetail) {
    Text("Detalle por día", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    KyvoCard(contentPadding = PaddingValues(8.dp)) {
        detail.points.forEach { point ->
            Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(if ((point.ratio ?: 0.0) <= 1.0) Color(0xFF20C979) else Color(0xFFFFA31A)))
                Text(point.date.format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es", "MX"))), Modifier.weight(1f).padding(start = 12.dp), maxLines = 1)
                Text("${point.consumed} kcal", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ProteinTip() = KyvoCard(Modifier.background(KyvoColors.PurpleSoft)) {
    Text("Tip KYVO", color = KyvoColors.PurplePrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text("Mantener un consumo de proteína constante te ayuda a preservar masa muscular y mejorar tu recuperación.", color = KyvoColors.Slate, modifier = Modifier.padding(top = 6.dp))
}

@Composable
private fun ProteinSources(detail: NutritionMetricDetail) {
    Text("Fuentes principales", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    detail.sources.forEach { source ->
        val share = if (detail.totalConsumed > 0) source.amount * 100 / detail.totalConsumed else 0
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(KyvoColors.PurpleSoft), contentAlignment = Alignment.Center) { Text(source.name.take(1), color = KyvoColors.PurplePrimary, fontWeight = FontWeight.Bold) }
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(source.name, fontWeight = FontWeight.SemiBold); Text("${source.amount} g  $share%") }
                Box(Modifier.fillMaxWidth().padding(top = 6.dp).height(7.dp).clip(CircleShape).background(KyvoColors.Outline.copy(alpha = .35f))) { Box(Modifier.fillMaxWidth((share / 100f).coerceIn(0f, 1f)).height(7.dp).background(KyvoColors.PurplePrimary)) }
            }
        }
    }
}

@Composable
private fun MetricDetailEmptyState(title: String, onBack: () -> Unit) = Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
    Text("‹", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.align(Alignment.Start).size(48.dp).clickable(onClick = onBack).padding(8.dp))
    Text(title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
    Text("Aún no hay comidas registradas en este periodo.", color = KyvoColors.Slate, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun MetricDetailErrorState(message: String, retry: () -> Unit) = Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
    Text(message, color = KyvoColors.Slate, textAlign = TextAlign.Center)
    KyvoPrimaryButton("Reintentar", retry, Modifier.padding(top = 20.dp))
}

private fun differencePercentage(detail: NutritionMetricDetail): Int = detail.averagePercentage?.minus(100) ?: 0