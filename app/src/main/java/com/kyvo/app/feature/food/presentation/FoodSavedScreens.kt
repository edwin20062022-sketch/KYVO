package com.kyvo.app.feature.food.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyvo.app.core.designsystem.KyvoColors
import com.kyvo.app.core.designsystem.component.KyvoCard
import com.kyvo.app.core.designsystem.component.KyvoPrimaryButton
import com.kyvo.app.feature.food.domain.model.FoodSearchResult
import com.kyvo.app.feature.food.domain.repository.FoodRepository

@Composable
fun FoodFrequentRoute(repository: FoodRepository, onBack: () -> Unit, onSelect: (FoodSearchResult) -> Unit, onPortion: (FoodSearchResult) -> Unit) {
    val vm: FrequentFoodsViewModel = viewModel(factory = FrequentFoodsViewModel.factory(repository))
    SavedFoodsRouteContent("Frecuentes", "Tus alimentos más usados para registrarlos en 1–2 taps.", vm.state.collectAsStateWithLifecycle().value, onBack, vm::retry, onSelect, onPortion, showFavorite = false, onRemoveFavorite = {})
}

@Composable
fun FoodFavoritesRoute(repository: FoodRepository, onBack: () -> Unit, onSelect: (FoodSearchResult) -> Unit, onPortion: (FoodSearchResult) -> Unit) {
    val vm: FavoriteFoodsViewModel = viewModel(factory = FavoriteFoodsViewModel.factory(repository))
    val state by vm.state.collectAsStateWithLifecycle()
    SavedFoodsRouteContent("Favoritos", "Tus alimentos guardados para registrarlos más rápido.", state, onBack, vm::retry, onSelect, onPortion, showFavorite = true, onRemoveFavorite = vm::remove)
}

@Composable
private fun SavedFoodsRouteContent(title: String, subtitle: String, state: SavedFoodsUiState, onBack: () -> Unit, onRetry: () -> Unit, onSelect: (FoodSearchResult) -> Unit, onPortion: (FoodSearchResult) -> Unit, showFavorite: Boolean, onRemoveFavorite: (FoodSearchResult) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        FoodHeader(title, subtitle, onBack)
        when (state) {
            SavedFoodsUiState.Loading -> LoadingFood("Cargando alimentos…")
            SavedFoodsUiState.Empty -> SavedEmptyState(title, onBack)
            is SavedFoodsUiState.Error -> FoodStateMessage("No pudimos cargar $title", state.message, "Reintentar", onRetry)
            is SavedFoodsUiState.Content -> {
                state.message?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) }
                SavedFoodsList(state.items, onSelect, onPortion, showFavorite, onRemoveFavorite)
            }
        }
    }
}

@Composable
private fun SavedFoodsList(items: List<FoodSearchResult>, onSelect: (FoodSearchResult) -> Unit, onPortion: (FoodSearchResult) -> Unit, showFavorite: Boolean, onRemoveFavorite: (FoodSearchResult) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { if (showFavorite) Text("ALIMENTOS", color = KyvoColors.Slate, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 2.dp)) else SavedHintCard() }
        items(items, key = { "${it.type}:${it.id}" }) { food -> SavedFoodRow(food, onSelect = { onSelect(food) }, onPortion = { onPortion(food) }, showFavorite = showFavorite, onRemoveFavorite = { onRemoveFavorite(food) }) }
    }
}

@Composable
private fun SavedFoodRow(food: FoodSearchResult, onSelect: () -> Unit, onPortion: () -> Unit, showFavorite: Boolean, onRemoveFavorite: () -> Unit) {
    KyvoCard(Modifier.fillMaxWidth().clickable(onClick = onSelect), contentPadding = PaddingValues(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FoodImage(food.imageUrl, food.imageKey, Modifier.size(70.dp))
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(food.name, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(food.servingLabel ?: "Por 100 g", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Row(Modifier.padding(top = 7.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) { Text(food.nutrients.caloriesPer100g?.let { "${it.toInt()} kcal" } ?: "—", fontWeight = FontWeight.SemiBold); Text("${food.nutrients.proteinPer100g?.oneDecimal() ?: "—"} g prot.", color = KyvoColors.PurplePrimary, style = MaterialTheme.typography.bodySmall); Text("${food.nutrients.carbohydratesPer100g?.oneDecimal() ?: "—"} g carb.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onPortion) { Icon(Icons.Outlined.Add, "Registrar ${food.name}", tint = KyvoColors.PurplePrimary, modifier = Modifier.clip(CircleShape).background(KyvoColors.PurpleSoft).padding(9.dp)) }
                if (showFavorite) IconButton(onClick = onRemoveFavorite) { Icon(Icons.Outlined.Favorite, "Quitar ${food.name} de favoritos", tint = KyvoColors.PurplePrimary) }
            }
        }
    }
}

@Composable private fun SavedHintCard() { Surface(color = KyvoColors.PurpleSoft, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) { Text("Registra una comida para que tus alimentos más usados aparezcan aquí.", color = KyvoColors.PurpleDeep, modifier = Modifier.padding(16.dp)) } }
@Composable private fun SavedEmptyState(title: String, onBack: () -> Unit) { Column(Modifier.fillMaxSize().padding(28.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) { Icon(if (title == "Favoritos") Icons.Outlined.FavoriteBorder else Icons.Outlined.Search, null, tint = KyvoColors.PurplePrimary, modifier = Modifier.size(54.dp)); Text(if (title == "Favoritos") "Aún no tienes favoritos" else "Aún no tienes frecuentes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp)); Text(if (title == "Favoritos") "Marca alimentos desde su detalle para encontrarlos aquí." else "Tus alimentos aparecerán aquí a partir de tu historial real.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 7.dp)); KyvoPrimaryButton("Explorar alimentos", onBack, Modifier.padding(top = 20.dp)) } }
private fun Double.oneDecimal(): String = String.format(java.util.Locale.US, "%.1f", this)
