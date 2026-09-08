package com.kyvo.app.feature.home.presentation

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyvo.app.core.designsystem.KyvoColors
import com.kyvo.app.core.designsystem.component.KyvoCard
import com.kyvo.app.core.designsystem.component.KyvoPrimaryButton
import com.kyvo.app.feature.food.presentation.FoodImage
import com.kyvo.app.feature.home.domain.model.Meal
import com.kyvo.app.feature.home.domain.model.MealItem
import com.kyvo.app.feature.home.domain.model.MealType
import com.kyvo.app.feature.home.domain.model.withCalculatedTotals
import com.kyvo.app.feature.home.domain.repository.MealRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed interface MealDetailUiState {
    data object Loading : MealDetailUiState
    data class Content(val meal: Meal) : MealDetailUiState
    data object NotFound : MealDetailUiState
    data class Error(val message: String) : MealDetailUiState
}

class MealDetailViewModel(private val repository: MealRepository, private val mealId: String) : ViewModel() {
    private val _state = MutableStateFlow<MealDetailUiState>(MealDetailUiState.Loading)
    val state: StateFlow<MealDetailUiState> = _state.asStateFlow()
    private var loadJob: Job? = null
    init { load() }
    fun retry() = load()
    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            repository.observeMeal(mealId).catch { _state.value = MealDetailUiState.Error(it.message ?: "No pudimos cargar la comida.") }.collect { meal ->
                _state.value = meal?.let { MealDetailUiState.Content(it.withCalculatedTotals()) } ?: MealDetailUiState.NotFound
            }
        }
    }
    companion object {
        fun factory(repository: MealRepository, mealId: String) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = MealDetailViewModel(repository, mealId) as T
        }
    }
}

@Composable
fun MealDetailRoute(repository: MealRepository, mealId: String, onBack: () -> Unit, onEdit: (String) -> Unit, onDelete: (String) -> Unit) {
    val vm: MealDetailViewModel = viewModel(factory = MealDetailViewModel.factory(repository, mealId))
    val state by vm.state.collectAsStateWithLifecycle()
    when (val current = state) {
        MealDetailUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        MealDetailUiState.NotFound -> MealDetailMessage("Comida no disponible", "Esta comida pudo haber sido eliminada.", "Volver", onBack)
        is MealDetailUiState.Error -> MealDetailMessage("No pudimos cargar la comida", current.message, "Reintentar", vm::retry)
        is MealDetailUiState.Content -> MealDetailScreen(current.meal, onBack, { onEdit(current.meal.id) }, { onDelete(current.meal.id) })
    }
}

@Composable
private fun MealDetailScreen(meal: Meal, onBack: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Volver") }
                Column(Modifier.weight(1f)) { Text(meal.type.label, color = KyvoColors.PurplePrimary, fontWeight = FontWeight.Bold); Text(meal.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis); Text("Hoy${meal.time?.let { ", $it" }.orEmpty()}", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                IconButton(onClick = onEdit) { Icon(Icons.Outlined.Info, "Editar comida") }
            }
        }
        item { Row(Modifier.padding(horizontal = 20.dp).fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(KyvoColors.PurpleSoft).padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Info, null, tint = KyvoColors.PurplePrimary); Text(meal.items.joinToString(", ") { it.name }, color = KyvoColors.PurpleDeep, modifier = Modifier.padding(start = 12.dp), maxLines = 2, overflow = TextOverflow.Ellipsis) } }
        item { MealSummaryCard(meal) }
        item { Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) { Text("Alimentos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); TextButton(onClick = {}) { Icon(Icons.Outlined.Add, null); Text("Agregar alimento") } } }
        item { KyvoCard(Modifier.padding(horizontal = 20.dp), contentPadding = PaddingValues(0.dp)) { meal.items.forEachIndexed { index, item -> MealSnapshotRow(item); if (index < meal.items.lastIndex) HorizontalDivider() } } }
        item { Row(Modifier.padding(horizontal = 20.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(KyvoColors.PurpleSoft).padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceAround) { TextButton(onClick = {}) { Icon(Icons.Outlined.Share, null, tint = KyvoColors.PurplePrimary); Text("Duplicar comida", color = KyvoColors.PurplePrimary) }; TextButton(onClick = onDelete) { Icon(Icons.Outlined.Close, null, tint = KyvoColors.Error); Text("Eliminar comida", color = KyvoColors.Error) } } }
    }
}

@Composable private fun MealSummaryCard(meal: Meal) = KyvoCard(Modifier.padding(horizontal = 20.dp)) { Text("Resumen de la comida", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) { SummaryValue("${meal.totalCalories}", "kcal"); SummaryValue("${meal.protein} g", "Proteína"); SummaryValue("${meal.carbohydrates} g", "Carbohidratos"); SummaryValue("${meal.fat} g", "Grasas") } }
@Composable private fun SummaryValue(value: String, label: String) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text(label, color = KyvoColors.PurplePrimary, style = MaterialTheme.typography.bodySmall) } }
@Composable private fun MealSnapshotRow(item: MealItem) { Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) { FoodImage(item.image?.takeIf { it.startsWith("http") }, item.image?.takeUnless { it.startsWith("http") }, Modifier.size(64.dp)); Column(Modifier.weight(1f).padding(start = 12.dp)) { Text(item.name, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis); Text("${item.quantity.cleanAmount()} ${item.unit}", color = MaterialTheme.colorScheme.onSurfaceVariant); Row(Modifier.padding(top = 7.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) { Text("${item.protein} g proteína", color = KyvoColors.PurplePrimary, style = MaterialTheme.typography.bodySmall); Text("${item.carbohydrates} g carbs", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) } }; Text("${item.calories} kcal", fontWeight = FontWeight.Bold) } }
@Composable private fun MealDetailMessage(title: String, message: String, action: String, onAction: () -> Unit) { Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp)); KyvoPrimaryButton(action, onAction, Modifier.padding(top = 20.dp)) } }
private fun Double.cleanAmount(): String = if (this % 1.0 == 0.0) toInt().toString() else String.format(java.util.Locale.US, "%.1f", this)
