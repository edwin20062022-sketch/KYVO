package com.kyvo.app.feature.progress.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kyvo.app.core.designsystem.KyvoColors
import com.kyvo.app.core.designsystem.component.KyvoCard
import com.kyvo.app.core.designsystem.component.KyvoPrimaryButton
import com.kyvo.app.feature.progress.domain.NutritionConsistencyDay
import com.kyvo.app.feature.progress.domain.NutritionConsistencyStatus
import com.kyvo.app.feature.progress.domain.NutritionConsistencySummary
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun NutritionConsistencyRoute(viewModel: NutritionConsistencyViewModel, onBack: () -> Unit) {
    when (val state = viewModel.state.collectAsStateWithLifecycle().value) {
        NutritionConsistencyUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        NutritionConsistencyUiState.Empty -> ConsistencyEmpty(onBack)
        is NutritionConsistencyUiState.Error -> ConsistencyError(state.message, viewModel::retry, onBack)
        is NutritionConsistencyUiState.Content -> NutritionConsistencyScreen(state.summary, onBack)
    }
}

@Composable
fun NutritionConsistencyScreen(summary: NutritionConsistencySummary, onBack: () -> Unit) {
    val percentage = ((summary.consistencyPercentage ?: 0.0) * 100).roundToInt()
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("‹", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.size(48.dp).clickable(onClick = onBack).semantics { contentDescription = "Volver a Progreso" })
                Column(Modifier.weight(1f)) {
                    Text("Consistencia nutricional", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Tu constancia se refleja en tus resultados.", color = KyvoColors.Slate, style = MaterialTheme.typography.titleMedium)
                }
                Text("Últimos 30 días⌄", modifier = Modifier.clip(RoundedCornerShape(24.dp)).background(KyvoColors.PurpleSoft).padding(horizontal = 14.dp, vertical = 12.dp), fontWeight = FontWeight.SemiBold)
            }
        }
        item { ConsistencyHero(percentage, summary) }
        item {
            Text("Consistencia por nutriente", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ConsistencyMetricCard("Calorías", summary, true, Modifier.weight(1f))
                ConsistencyMetricCard("Proteína", summary, false, Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoMetricCard("Carbohidratos", Modifier.weight(1f))
                InfoMetricCard("Grasas", Modifier.weight(1f))
            }
        }
        item { DailyOverview(summary.dailyStatuses) }
        item {
            KyvoCard(modifier = Modifier.fillMaxWidth()) {
                Text("La consistencia", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("construye resultados.", color = KyvoColors.PurplePrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Seguir tus objetivos te acerca a la mejor versión de ti.", color = KyvoColors.Slate, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
private fun ConsistencyHero(percentage: Int, summary: NutritionConsistencySummary) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(176.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(progress = { percentage / 100f }, modifier = Modifier.fillMaxSize(), color = KyvoColors.PurplePrimary, trackColor = KyvoColors.PurpleSoft, strokeWidth = 12.dp)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$percentage%", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                Text("de tus días\ndentro del rango", textAlign = TextAlign.Center, color = KyvoColors.Slate)
            }
        }
        Column(Modifier.padding(start = 20.dp).weight(1f)) {
            Text(if (percentage >= 70) "Vas por buen camino" else "Cada día cuenta", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Has mantenido tus macros dentro de los rangos en ${summary.consistentDays} de tus últimos ${summary.evaluableDays} días.", color = KyvoColors.Slate, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun ConsistencyMetricCard(name: String, summary: NutritionConsistencySummary, calories: Boolean, modifier: Modifier) {
    val qualifying = summary.dailyStatuses.count { day ->
        if (calories) day.caloriesRatio?.let { it in 0.90..1.10 } == true else day.proteinRatio?.let { it >= 0.90 } == true
    }
    val percentage = if (summary.evaluableDays == 0) 0 else (qualifying * 100.0 / summary.evaluableDays).roundToInt()
    KyvoCard(modifier, PaddingValues(14.dp)) {
        Text(name, fontWeight = FontWeight.SemiBold)
        Text("$percentage%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
        Text("$qualifying de ${summary.evaluableDays} días", color = KyvoColors.Slate, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(KyvoColors.Outline)) {
            Box(Modifier.fillMaxWidth(percentage / 100f).height(8.dp).clip(CircleShape).background(KyvoColors.PurpleAccent))
        }
    }
}

@Composable
private fun InfoMetricCard(name: String, modifier: Modifier) = KyvoCard(modifier, PaddingValues(14.dp)) {
    Text(name, fontWeight = FontWeight.SemiBold)
    Text("Información del periodo", color = KyvoColors.Slate, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun DailyOverview(days: List<NutritionConsistencyDay>) {
    Column {
        Text("Vista general del periodo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            LegendDot(KyvoColors.Success, "Dentro del rango")
            LegendDot(KyvoColors.PurpleAccent, "Fuera del rango")
            LegendDot(KyvoColors.Outline, "Sin datos")
        }
        val weeks = days.chunked(7)
        Row(Modifier.horizontalScroll(androidx.compose.foundation.rememberScrollState()).padding(top = 14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom").forEach { Text(it, color = KyvoColors.Slate, modifier = Modifier.width(28.dp)) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.padding(start = 8.dp)) {
                weeks.forEachIndexed { weekIndex, week ->
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        (0 until 7).forEach { dayIndex ->
                            val day = week.getOrNull(dayIndex)
                            val status = day?.status ?: NutritionConsistencyStatus.NOT_EVALUABLE
                            Box(Modifier.size(16.dp).clip(CircleShape).background(statusColor(status)).semantics { contentDescription = "${day?.date?.format(DateTimeFormatter.ofPattern("EEEE", Locale("es", "MX"))) ?: "Día sin datos"}, ${statusLabel(status)}" })
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun LegendDot(color: Color, label: String) = Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(10.dp).clip(CircleShape).background(color)); Text(label, color = KyvoColors.Slate, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(start = 4.dp)) }

private fun statusColor(status: NutritionConsistencyStatus) = when (status) {
    NutritionConsistencyStatus.CONSISTENT -> KyvoColors.Success
    NutritionConsistencyStatus.INCONSISTENT -> KyvoColors.PurpleAccent
    NutritionConsistencyStatus.NOT_EVALUABLE -> KyvoColors.Outline
}

private fun statusLabel(status: NutritionConsistencyStatus) = when (status) {
    NutritionConsistencyStatus.CONSISTENT -> "dentro del rango"
    NutritionConsistencyStatus.INCONSISTENT -> "fuera del rango"
    NutritionConsistencyStatus.NOT_EVALUABLE -> "sin datos suficientes"
}

@Composable private fun ConsistencyEmpty(onBack: () -> Unit) = Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Text("Consistencia nutricional", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text("Aún no hay datos suficientes para calcular tu consistencia.", color = KyvoColors.Slate, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 10.dp)); KyvoPrimaryButton("Volver a Progreso", onBack, Modifier.padding(top = 20.dp)) }

@Composable private fun ConsistencyError(message: String, retry: () -> Unit, onBack: () -> Unit) = Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Text(message, color = KyvoColors.Slate, textAlign = TextAlign.Center); KyvoPrimaryButton("Reintentar", retry, Modifier.padding(top = 20.dp)); Text("Volver", color = KyvoColors.PurplePrimary, modifier = Modifier.padding(top = 18.dp).semantics { contentDescription = "Volver a Progreso" }) }
