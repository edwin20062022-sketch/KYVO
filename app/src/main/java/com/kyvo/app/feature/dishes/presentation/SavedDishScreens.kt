package com.kyvo.app.feature.dishes.presentation

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyvo.app.core.designsystem.KyvoColors
import com.kyvo.app.core.designsystem.component.KyvoCard
import com.kyvo.app.core.designsystem.component.KyvoPrimaryButton
import com.kyvo.app.feature.dishes.domain.model.SavedDish
import com.kyvo.app.feature.dishes.domain.model.SavedDishItem
import com.kyvo.app.feature.dishes.domain.model.SavedDishItemFactory
import com.kyvo.app.feature.dishes.domain.model.validateSavedDishName
import com.kyvo.app.feature.dishes.domain.repository.SavedDishRepository
import com.kyvo.app.feature.food.domain.model.FoodDetail
import com.kyvo.app.feature.food.domain.model.FoodSearchResult
import com.kyvo.app.feature.food.domain.repository.FoodRepository
import com.kyvo.app.feature.food.presentation.FoodImage
import com.kyvo.app.feature.food.presentation.FoodSearchUiState
import com.kyvo.app.feature.food.presentation.FoodSearchViewModel
import com.kyvo.app.feature.home.domain.model.MealType
import com.kyvo.app.feature.home.domain.repository.MealRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SavedDishesRoute(repository: SavedDishRepository, onBack: () -> Unit, onCreate: () -> Unit, onDetail: (String) -> Unit) {
    val dishes by repository.observeSavedDishes().collectAsStateWithLifecycle(emptyList())
    var loading by remember { mutableStateOf(true) }
    LaunchedEffect(dishes) { loading = false }
    Scaffold(bottomBar = { KyvoPrimaryButton("Crear platillo", onCreate, Modifier.padding(20.dp)) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { DishHeader("Mis platillos", "Tus combinaciones listas para registrar.", onBack) }
            if (loading) item { LoadingDishes() }
            else if (dishes.isEmpty()) item { EmptyDishes(onCreate) }
            else {
                item { Text("${dishes.size} platillo${if (dishes.size == 1) "" else "s"}", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                items(dishes, key = SavedDish::id) { dish -> SavedDishCard(dish, onClick = { onDetail(dish.id) }) }
            }
        }
    }
}

@Composable
fun SavedDishEditorRoute(dishId: String?, dishes: SavedDishRepository, foods: FoodRepository, onBack: () -> Unit, onSaved: (String) -> Unit) {
    var loadedDish by remember(dishId) { mutableStateOf<SavedDish?>(null) }
    var loading by remember(dishId) { mutableStateOf(dishId != null) }
    LaunchedEffect(dishId) {
        if (dishId != null) dishes.observeSavedDish(dishId).collectLatest { dish -> loadedDish = dish; loading = false }
    }
    if (loading) return LoadingDishes()
    var name by remember(loadedDish?.id) { mutableStateOf(loadedDish?.name.orEmpty()) }
    var portions by remember(loadedDish?.id) { mutableStateOf(loadedDish?.portions ?: 1) }
    var items by remember(loadedDish?.id) { mutableStateOf(loadedDish?.items.orEmpty()) }
    var stage by remember { mutableStateOf(EditorStage.Form) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var editing by remember { mutableStateOf<SavedDishItem?>(null) }
    val scope = rememberCoroutineScope()
    if (stage == EditorStage.Search) {
        DishIngredientSearch(
            foods = foods,
            onBack = { stage = EditorStage.Form },
            onAdd = { food, grams -> items = items + SavedDishItemFactory.fromFood(food, grams); stage = EditorStage.Form },
        )
        return
    }
    if (editing != null) AmountEditor(editing!!, foods, onDismiss = { editing = null }) { updated ->
        items = items.map { if (it.id == updated.id) updated else it }; editing = null
    }
    val validation = validateSavedDishName(name)
    Scaffold(bottomBar = {
        Column(Modifier.padding(20.dp)) {
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp)) }
            KyvoPrimaryButton("Guardar platillo", {
                if (!saving && validation.error == null && items.isNotEmpty()) {
                    saving = true; error = null
                    scope.launch {
                        runCatching { dishes.saveDish(SavedDish(loadedDish?.id.orEmpty(), name = validation.normalized!!, portions = portions, items = items)) }
                            .fold(onSuccess = { onSaved(it.id) }, onFailure = { error = it.message ?: "No pudimos guardar el platillo." })
                        saving = false
                    }
                }
            }, enabled = validation.error == null && items.isNotEmpty(), isLoading = saving)
        }
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { DishHeader(if (dishId == null) "Crear platillo" else "Editor del platillo", "Arma tu combinación y guarda sus macros.", onBack) }
            item {
                OutlinedTextField(name, { name = it; error = null }, Modifier.fillMaxWidth(), label = { Text("Nombre del platillo") }, singleLine = true, isError = validation.error != null, supportingText = { validation.error?.let { message -> Text(message) } })
            }
            item { PortionSelector(portions, onChange = { portions = it }) }
            item { DishTotals(items, portions) }
            item { Text("Ingredientes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            if (items.isEmpty()) item { EmptyIngredients { stage = EditorStage.Search } }
            items(items, key = SavedDishItem::id) { item ->
                DishItemRow(item, onEdit = { editing = item }, onDelete = { items = items.filterNot { it.id == item.id } })
            }
            item { TextButton(onClick = { stage = EditorStage.Search }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Add, null); Text(" Agregar ingrediente") } }
        }
    }
}

@Composable
fun SavedDishDetailRoute(id: String, repository: SavedDishRepository, onBack: () -> Unit, onEdit: (String) -> Unit, onAddToDay: (String) -> Unit) {
    val dish by repository.observeSavedDish(id).collectAsStateWithLifecycle(null)
    var confirmDelete by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    if (dish == null) return LoadingDishes()
    if (confirmDelete) AlertDialog(onDismissRequest = { confirmDelete = false }, title = { Text("¿Eliminar platillo?") }, text = { Text("Se eliminará esta plantilla. Tus comidas registradas no cambiarán.") }, confirmButton = { TextButton(onClick = { scope.launch { deleting = true; runCatching { repository.deleteDish(id) }.fold(onSuccess = { onBack() }, onFailure = { error = it.message }); deleting = false } }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) } }, dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") } })
    Scaffold(bottomBar = { KyvoPrimaryButton("Agregar al día", { onAddToDay(id) }, Modifier.padding(20.dp)) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { DishHeader("Detalle del platillo", "Plantilla reutilizable", onBack, actions = { IconButton(onClick = { onEdit(id) }) { Icon(Icons.Outlined.Edit, "Editar") }; IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Outlined.Delete, "Eliminar") } }) }
            item { DishHero(dish!!) }
            item { DishTotals(dish!!.items, dish!!.portions) }
            item { Text("Ingredientes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            items(dish!!.items, key = SavedDishItem::id) { DishItemRow(it) }
            error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
            if (deleting) item { CircularProgressIndicator() }
        }
    }
}

@Composable
fun AddSavedDishToDayRoute(id: String, dishes: SavedDishRepository, meals: MealRepository, onBack: () -> Unit, onAdded: () -> Unit) {
    val dish by dishes.observeSavedDish(id).collectAsStateWithLifecycle(null)
    if (dish == null) return LoadingDishes()
    var portions by remember { mutableStateOf(dish!!.portions.toDouble()) }
    var type by remember { mutableStateOf(MealType.Lunch) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    Scaffold(bottomBar = {
        Column(Modifier.padding(20.dp)) {
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp)) }
            KyvoPrimaryButton("Agregar al día", {
                if (!saving) { saving = true; scope.launch { runCatching { meals.addSavedDishToDay(id, LocalDate.now(), type, portions) }.fold(onSuccess = { onAdded() }, onFailure = { error = it.message ?: "No pudimos registrar el platillo." }); saving = false } }
            }, isLoading = saving)
        }
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { DishHeader("Agregar al día", "Elige la comida y las porciones.", onBack) }
            item { DishHero(dish!!) }
            item { PortionMultiplier(portions, dish!!.portions.toDouble(), { portions = it }) }
            item { Text("Agregar a", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            item { MealTypeSelector(type) { type = it } }
            item { Text("Hoy · ${LocalDate.now()}", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable private fun DishIngredientSearch(foods: FoodRepository, onBack: () -> Unit, onAdd: (FoodDetail, Double) -> Unit) {
    val vm: FoodSearchViewModel = viewModel(factory = FoodSearchViewModel.factory(foods))
    val query by vm.query.collectAsStateWithLifecycle()
    val state by vm.state.collectAsStateWithLifecycle()
    var selected by remember { mutableStateOf<FoodDetail?>(null) }
    var grams by remember { mutableStateOf("100") }
    val scope = rememberCoroutineScope()
    if (selected != null) AlertDialog(onDismissRequest = { selected = null }, title = { Text(selected!!.name) }, text = { OutlinedTextField(grams, { grams = it }, label = { Text("Cantidad") }, suffix = { Text("g") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)) }, confirmButton = { TextButton(onClick = { grams.replace(',', '.').toDoubleOrNull()?.takeIf { it > 0 }?.let { onAdd(selected!!, it) } }) { Text("Agregar") } }, dismissButton = { TextButton(onClick = { selected = null }) { Text("Cancelar") } })
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        DishHeader("Buscar ingredientes", "Busca en el catálogo KYVO.", onBack)
        OutlinedTextField(query, vm::onQueryChanged, Modifier.fillMaxWidth().padding(top = 12.dp), label = { Text("Buscar alimento") }, leadingIcon = { Icon(Icons.Outlined.Search, null) }, singleLine = true)
        Spacer(Modifier.height(14.dp))
        when (val s = state) {
            FoodSearchUiState.Idle -> Text("Escribe al menos 2 caracteres.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            FoodSearchUiState.Loading -> LoadingDishes()
            is FoodSearchUiState.Empty -> Text("No encontramos alimentos.")
            is FoodSearchUiState.Error -> Text(s.message, color = MaterialTheme.colorScheme.error)
            is FoodSearchUiState.Results -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) { items(s.items, key = { it.id }) { result -> IngredientResult(result) { scope.launch { selected = foods.getFood(result.id) } } } }
        }
    }
}

@Composable private fun IngredientResult(result: FoodSearchResult, onClick: () -> Unit) { KyvoCard(Modifier.fillMaxWidth().clickable(onClick = onClick), contentPadding = PaddingValues(12.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { FoodImage(result.imageUrl, result.imageKey, Modifier.size(58.dp)); Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { Text(result.name, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis); Text(result.subtitle ?: "Por 100 g", color = MaterialTheme.colorScheme.onSurfaceVariant) }; Icon(Icons.Outlined.Add, "Agregar", tint = KyvoColors.PurplePrimary) } } }
@Composable private fun DishHeader(title: String, subtitle: String, onBack: () -> Unit, actions: @Composable () -> Unit = {}) { Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Volver") }; Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) }; actions() } }
@Composable private fun SavedDishCard(dish: SavedDish, onClick: () -> Unit) { KyvoCard(Modifier.fillMaxWidth().clickable(onClick = onClick), contentPadding = PaddingValues(14.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { FoodImage(dish.coverImage, null, Modifier.size(72.dp)); Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { Text(dish.name, fontWeight = FontWeight.Bold); Text("${dish.items.size} ingredientes · ${dish.portions} porción${if (dish.portions == 1) "" else "es"}", color = MaterialTheme.colorScheme.onSurfaceVariant); MacroLine(dish) }; Text("${dish.totalCalories} kcal", color = KyvoColors.PurplePrimary, fontWeight = FontWeight.Bold) } } }
@Composable private fun DishHero(dish: SavedDish) { KyvoCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { FoodImage(dish.coverImage, null, Modifier.size(92.dp)); Column(Modifier.padding(start = 14.dp)) { Text(dish.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text("${dish.items.size} ingredientes · ${dish.portions} porción${if (dish.portions == 1) "" else "es"}", color = MaterialTheme.colorScheme.onSurfaceVariant); Text("${dish.totalCalories} kcal", color = KyvoColors.PurplePrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 5.dp)) } } } }
@Composable private fun MacroLine(dish: SavedDish) { Text("P ${dish.totalProtein}g · C ${dish.totalCarbohydrates}g · G ${dish.totalFat}g", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
@Composable private fun DishTotals(items: List<SavedDishItem>, portions: Int) { val calories = items.sumOf { it.calories }; val protein = items.sumOf { it.protein }; val carbs = items.sumOf { it.carbohydrates }; val fat = items.sumOf { it.fat }; KyvoCard(Modifier.fillMaxWidth()) { Text("Totales · $portions porción${if (portions == 1) "" else "es"}", color = KyvoColors.PurplePrimary, fontWeight = FontWeight.SemiBold); Text("$calories kcal", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text("Proteína $protein g · Carbos $carbs g · Grasas $fat g", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
@Composable private fun DishItemRow(item: SavedDishItem, onEdit: (() -> Unit)? = null, onDelete: (() -> Unit)? = null) { KyvoCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(12.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { FoodImage(item.imageReference, null, Modifier.size(54.dp)); Column(Modifier.weight(1f).padding(horizontal = 10.dp)) { Text(item.nameSnapshot, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis); Text("${item.grams.clean()} g · ${item.calories} kcal · P ${item.protein}g", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }; onEdit?.let { IconButton(onClick = it) { Icon(Icons.Outlined.Edit, "Editar cantidad") } }; onDelete?.let { IconButton(onClick = it) { Icon(Icons.Outlined.Delete, "Eliminar ingrediente") } } } } }
@Composable private fun PortionSelector(portions: Int, onChange: (Int) -> Unit) { Row(verticalAlignment = Alignment.CenterVertically) { Text("Porciones", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); IconButton(onClick = { onChange((portions - 1).coerceAtLeast(1)) }) { Icon(Icons.Outlined.Close, "Reducir") }; Text(portions.toString(), style = MaterialTheme.typography.titleMedium); IconButton(onClick = { onChange((portions + 1).coerceAtMost(99)) }) { Icon(Icons.Outlined.Add, "Aumentar") } } }
@Composable private fun PortionMultiplier(value: Double, base: Double, onChange: (Double) -> Unit) { Column { Text("Porciones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Row(Modifier.horizontalScroll(rememberScrollState()).padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf(base * .5, base, base * 2).distinct().forEach { candidate -> Surface(color = if (candidate == value) KyvoColors.PurplePrimary else MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(14.dp), modifier = Modifier.clickable { onChange(candidate) }) { Text("${candidate.clean()} porciones", color = if (candidate == value) Color.White else MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) } } } } }
@Composable private fun MealTypeSelector(selected: MealType, onSelect: (MealType) -> Unit) { Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) { MealType.entries.forEach { type -> Surface(color = if (selected == type) KyvoColors.PurplePrimary else MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(14.dp), modifier = Modifier.clickable { onSelect(type) }) { Text(type.label, color = if (selected == type) Color.White else MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) } } } }
@Composable private fun EmptyDishes(onCreate: () -> Unit) { KyvoCard(Modifier.fillMaxWidth()) { Text("Todavía no tienes platillos guardados.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text("Crea una combinación reutilizable de tus alimentos.", color = MaterialTheme.colorScheme.onSurfaceVariant); TextButton(onClick = onCreate) { Text("Crear mi primer platillo") } } }
@Composable private fun EmptyIngredients(onAdd: () -> Unit) { KyvoCard(Modifier.fillMaxWidth()) { Text("Agrega tus ingredientes", fontWeight = FontWeight.Bold); Text("Busca alimentos del catálogo y define su cantidad.", color = MaterialTheme.colorScheme.onSurfaceVariant); TextButton(onClick = onAdd) { Icon(Icons.Outlined.Add, null); Text(" Agregar ingrediente") } } }
@Composable private fun LoadingDishes() { Box(Modifier.fillMaxWidth().padding(36.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
@Composable private fun AmountEditor(item: SavedDishItem, foods: FoodRepository, onDismiss: () -> Unit, onUpdated: (SavedDishItem) -> Unit) { var value by remember { mutableStateOf(item.grams.clean()) }; var error by remember { mutableStateOf<String?>(null) }; val scope = rememberCoroutineScope(); AlertDialog(onDismissRequest = onDismiss, title = { Text("Editar cantidad") }, text = { Column { Text(item.nameSnapshot); OutlinedTextField(value, { value = it; error = null }, label = { Text("Gramos") }, suffix = { Text("g") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)); error?.let { Text(it, color = MaterialTheme.colorScheme.error) } } }, confirmButton = { TextButton(onClick = { val grams = value.replace(',', '.').toDoubleOrNull(); if (grams == null || grams <= 0) error = "Ingresa una cantidad válida." else scope.launch { runCatching { foods.getFood(item.foodReference)?.let { SavedDishItemFactory.fromFood(it, grams, item.id) } ?: error("No encontramos el alimento.") }.fold(onSuccess = onUpdated, onFailure = { error = it.message ?: "No pudimos actualizar la cantidad." }) } }) { Text("Actualizar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }) }
private fun Double.clean(): String = if (this % 1.0 == 0.0) roundToInt().toString() else String.format(java.util.Locale.US, "%.1f", this)
private enum class EditorStage { Form, Search }
