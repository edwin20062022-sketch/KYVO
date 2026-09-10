package com.kyvo.app.feature.progress.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.kyvo.app.feature.progress.domain.NutritionConsistencyStatus
import com.kyvo.app.feature.progress.domain.NutritionHistoryDay
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun NutritionHistoryRoute(viewModel: NutritionHistoryViewModel, onBack: () -> Unit, onDayDetail: (LocalDate) -> Unit) {
    when (val state = viewModel.state.collectAsStateWithLifecycle().value) {
        NutritionHistoryUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        NutritionHistoryUiState.Empty -> HistoryEmpty(onBack)
        is NutritionHistoryUiState.Error -> HistoryError(state.message, viewModel::retry, onBack)
        is NutritionHistoryUiState.Content -> NutritionHistoryScreen(state, viewModel::selectDate, onDayDetail, onBack)
    }
}

@Composable
private fun NutritionHistoryScreen(state: NutritionHistoryUiState.Content, onSelect: (LocalDate) -> Unit, onDayDetail: (LocalDate) -> Unit, onBack: () -> Unit) {
    val byDate = state.days.associateBy { it.summary.date }
    val dates = (0 until state.month.lengthOfMonth()).map { state.month.atDay(it + 1) }
    val selected = byDate[state.selectedDate]
    val recorded = state.days.filter { it.summary.mealCount > 0 }
    val averageCalories = recorded.map { it.summary.caloriesConsumed }.average().roundToInt()
    val consistency = state.days.map { it.consistency }.let { days ->
        val evaluable = days.count { it.status != NutritionConsistencyStatus.NOT_EVALUABLE }
        days.count { it.status == NutritionConsistencyStatus.CONSISTENT }.takeIf { evaluable > 0 }?.let { it.toDouble() / evaluable }
    }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("‹", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.size(48.dp).clickable(onClick = onBack).semantics { contentDescription = "Volver a Progreso" })
                Column(Modifier.weight(1f)) {
                    Text("Historial", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    Text("Consulta tu progreso día por día.", color = KyvoColors.Slate, style = MaterialTheme.typography.titleMedium)
                }
                Text(monthTitle(state.month), modifier = Modifier.clip(RoundedCornerShape(24.dp)).background(KyvoColors.PurpleSoft).padding(horizontal = 12.dp, vertical = 12.dp), fontWeight = FontWeight.SemiBold)
            }
        }
        item { HistorySummaryCard(state.days.size, recorded.size, averageCalories, consistency) }
        item { HistoryCalendar(state.month, dates, byDate, state.selectedDate, onSelect) }
        selected?.let { day -> item { SelectedDayCard(day, onDayDetail) } }
        if (selected == null) item { Text("Selecciona un día con registro para ver su detalle.", color = KyvoColors.Slate, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
    }
}

@Composable
private fun HistorySummaryCard(totalDays: Int, recordedDays: Int, averageCalories: Int, consistency: Double?) = KyvoCard(Modifier.fillMaxWidth(), PaddingValues(16.dp)) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        SummaryColumn("Días con registros", "$recordedDays / $totalDays")
        SummaryColumn("Promedio de calorías", if (recordedDays == 0) "—" else "$averageCalories kcal")
        SummaryColumn("Cumplimiento", consistency?.let { "${(it * 100).roundToInt()}%" } ?: "—")
    }
    Text("Tu constancia se refleja en tus resultados.", color = KyvoColors.Slate, modifier = Modifier.padding(top = 14.dp))
}

@Composable private fun SummaryColumn(label: String, value: String) = Column(Modifier.width(105.dp)) { Text(label, color = KyvoColors.Slate, style = MaterialTheme.typography.labelLarge); Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp)) }

@Composable
private fun HistoryCalendar(month: YearMonth, dates: List<LocalDate>, byDate: Map<LocalDate, NutritionHistoryDay>, selected: LocalDate, onSelect: (LocalDate) -> Unit) {
    KyvoCard(Modifier.fillMaxWidth(), PaddingValues(14.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) { listOf("L", "M", "M", "J", "V", "S", "D").forEach { Text(it, color = KyvoColors.Slate, fontWeight = FontWeight.Bold, modifier = Modifier.width(34.dp), textAlign = TextAlign.Center) } }
        val cells: List<LocalDate?> = List(dates.first().dayOfWeek.value - 1) { null } + dates
        Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            cells.chunked(7).forEach { week ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    week.forEach { date ->
                        if (date == null) Spacer(Modifier.size(42.dp)) else DayCell(date, byDate[date], date == selected, onSelect)
                    }
                    repeat(7 - week.size) { Spacer(Modifier.size(42.dp)) }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Legend(KyvoColors.Success, "Dentro del rango")
            Legend(KyvoColors.Warning, "Fuera del rango")
            Legend(KyvoColors.Outline, "Sin registros")
        }
    }
}

@Composable
private fun DayCell(date: LocalDate, day: NutritionHistoryDay?, selected: Boolean, onSelect: (LocalDate) -> Unit) {
    val color = when { selected -> KyvoColors.PurplePrimary; day == null || day.summary.mealCount == 0 -> KyvoColors.Outline; day.consistency.status == NutritionConsistencyStatus.CONSISTENT -> KyvoColors.Success; else -> KyvoColors.Warning }
    Box(Modifier.size(42.dp).clip(CircleShape).then(if (selected) Modifier.background(KyvoColors.PurplePrimary) else Modifier.border(2.dp, color, CircleShape)).clickable { onSelect(date) }.semantics { contentDescription = "${date.dayOfMonth} de ${date.month.getDisplayName(TextStyle.FULL, Locale("es", "MX"))}, ${day?.summary?.caloriesConsumed ?: 0} calorías" }, contentAlignment = Alignment.Center) { Text(date.dayOfMonth.toString(), color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold) }
}

@Composable private fun Legend(color: Color, label: String) = Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(10.dp).clip(CircleShape).background(color)); Text(label, color = KyvoColors.Slate, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(start = 4.dp)) }

@Composable
private fun SelectedDayCard(day: NutritionHistoryDay, onDayDetail: (LocalDate) -> Unit) = KyvoCard(Modifier.fillMaxWidth().clickable { onDayDetail(day.summary.date) }, PaddingValues(16.dp)) {
    Row(verticalAlignment = Alignment.CenterVertically) { Text(day.summary.date.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es", "MX"))).replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Text("›", color = KyvoColors.PurplePrimary, style = MaterialTheme.typography.headlineMedium) }
    Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        DayValue("${day.summary.caloriesConsumed}", "/ ${day.summary.caloriesTarget} kcal", "Calorías")
        DayValue("${day.summary.proteinConsumed} g", "/ ${day.summary.proteinTarget} g", "Proteína")
        DayValue("${day.summary.carbohydratesConsumed} g", "/ ${day.summary.carbohydratesTarget} g", "Carbohidratos")
        DayValue("${day.summary.fatConsumed} g", "/ ${day.summary.fatTarget} g", "Grasas")
    }
    KyvoPrimaryButton("Ver detalle del día", { onDayDetail(day.summary.date) }, Modifier.fillMaxWidth().padding(top = 16.dp))
}

@Composable private fun DayValue(value: String, target: String, label: String) = Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(72.dp)) { Text(value, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center); Text(target, color = KyvoColors.Slate, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center); Text(label, color = KyvoColors.Slate, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp)) }

private fun monthTitle(month: YearMonth) = "${month.month.getDisplayName(TextStyle.FULL, Locale("es", "MX")).replaceFirstChar { it.uppercase() }} ${month.year}"

@Composable private fun HistoryEmpty(onBack: () -> Unit) = Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Text("Historial", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text("Aún no hay datos suficientes de progreso.", color = KyvoColors.Slate, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 10.dp)); KyvoPrimaryButton("Volver a Progreso", onBack, Modifier.padding(top = 20.dp)) }

@Composable private fun HistoryError(message: String, retry: () -> Unit, onBack: () -> Unit) = Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Text(message, color = KyvoColors.Slate, textAlign = TextAlign.Center); KyvoPrimaryButton("Reintentar", retry, Modifier.padding(top = 20.dp)); Text("Volver", color = KyvoColors.PurplePrimary, modifier = Modifier.padding(top = 18.dp).clickable(onClick = onBack)) }
