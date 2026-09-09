package com.kyvo.app.feature.food.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.kyvo.app.core.designsystem.KyvoBrushes
import com.kyvo.app.core.designsystem.KyvoColors
import com.kyvo.app.core.designsystem.component.KyvoCard
import com.kyvo.app.core.designsystem.component.KyvoPrimaryButton
import com.kyvo.app.feature.food.domain.FoodRegistrationFactory
import com.kyvo.app.feature.food.domain.validateFoodAmount
import com.kyvo.app.feature.food.domain.model.FoodDetail
import com.kyvo.app.feature.food.domain.model.FoodSearchResult
import com.kyvo.app.feature.food.domain.model.FoodServing
import com.kyvo.app.feature.food.domain.model.FoodType
import com.kyvo.app.feature.food.domain.model.NutritionAmount
import com.kyvo.app.feature.food.domain.model.forGrams
import com.kyvo.app.feature.food.domain.repository.FoodRepository
import com.kyvo.app.feature.home.domain.model.MealType
import com.kyvo.app.feature.home.domain.model.MealItem
import com.kyvo.app.core.navigation.FoodSelectionContext
import com.kyvo.app.feature.home.domain.repository.MealRepository
import java.time.LocalDate
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import kotlin.math.roundToInt

@Composable
fun FoodHubScreen(onSearch: () -> Unit, onFrequent: () -> Unit, onFavorites: () -> Unit, onFuture: (String) -> Unit, onDishes: () -> Unit = { onFuture("Platillos guardados") }, onBack: () -> Unit = {}) {
    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { FoodHeader("Registro de alimentos", "Encuentra un alimento y agrégalo a tu día.", onBack, showBack = false) }
            item {
                KyvoCard(Modifier.fillMaxWidth().clickable(onClick = onSearch), contentPadding = PaddingValues(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Search, null, tint = KyvoColors.PurplePrimary, modifier = Modifier.size(28.dp))
                        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                            Text("Buscar alimento", fontWeight = FontWeight.Bold)
                            Text("Por nombre, marca o platillo", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, tint = KyvoColors.PurplePrimary)
                    }
                }
            }
            item { SectionLabel("ACCESOS RÁPIDOS") }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickFoodAction("Frecuentes", Icons.Outlined.Search, Modifier.weight(1f), onFrequent)
                    QuickFoodAction("Favoritos", Icons.Outlined.FavoriteBorder, Modifier.weight(1f), onFavorites)
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickFoodAction("Platillos", Icons.Outlined.Info, Modifier.weight(1f), onDishes)
                    QuickFoodAction("Meal Share", Icons.Outlined.Share, Modifier.weight(1f)) { onFuture("Meal Share") }
                }
            }
            item {
                KyvoCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(18.dp)) {
                    SectionLabel("RECIENTES")
                    Spacer(Modifier.height(10.dp))
                    Text("Aún no hay alimentos registrados hoy.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item {
                Surface(color = KyvoColors.PurpleDeep, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Info, null, tint = KyvoColors.PurpleAccent, modifier = Modifier.size(26.dp))
                        Column(Modifier.padding(start = 12.dp)) {
                            Text("Tip rápido", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Registra tus porciones mientras están frescas en tu memoria.", color = Color.White.copy(alpha = .8f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FoodSearchRoute(repository: FoodRepository, onBack: () -> Unit, onResults: (String) -> Unit) {
    val vm: FoodSearchViewModel = viewModel(factory = FoodSearchViewModel.factory(repository))
    val query by vm.query.collectAsStateWithLifecycle()
    var focused by rememberSaveable { mutableStateOf(false) }
    FoodSearchContent(query, focused, { focused = true }, vm::onQueryChanged, vm::searchNow, onBack, onResults)
}

@Composable
private fun FoodSearchContent(
    query: String, focused: Boolean, onFocus: () -> Unit, onQuery: (String) -> Unit,
    onSearch: () -> Unit, onBack: () -> Unit, onResults: (String) -> Unit,
) {
    LaunchedEffect(focused) { if (focused) { /* Compose keeps the field ready for the keyboard without forcing it on every recomposition. */ } }
    val normalized = query.trim()
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        FoodHeader("Buscar alimentos", "Busca por nombre, marca o platillo", onBack)
        OutlinedTextField(
            value = query, onValueChange = onQuery, modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            singleLine = true, label = { Text("¿Qué comiste?") }, placeholder = { Text("Ej. pollo, arroz o avena") },
            leadingIcon = { Icon(Icons.Outlined.Search, null) },
            trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { onQuery("") }) { Icon(Icons.Outlined.Close, "Limpiar") } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            keyboardActions = KeyboardActions(onSearch = { if (normalized.length >= FoodSearchViewModel.MIN_QUERY_LENGTH) { onSearch(); onResults(normalized) } }),
        )
        Spacer(Modifier.height(14.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Todos", "Proteínas", "Carbohidratos", "Grasas", "Otros").forEach { label -> AssistChip(onClick = {}, label = { Text(label) }) }
        }
        Spacer(Modifier.height(22.dp))
        if (normalized.length < FoodSearchViewModel.MIN_QUERY_LENGTH) {
            SearchGuidance()
        } else {
            KyvoCard(Modifier.fillMaxWidth()) {
                Text("Listo para buscar", fontWeight = FontWeight.Bold)
                Text("Presiona buscar para consultar el catálogo disponible.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
                KyvoPrimaryButton("Ver resultados", { onResults(normalized) }, Modifier.padding(top = 14.dp))
            }
        }
    }
}

@Composable
private fun SearchGuidance() {
    Column(Modifier.fillMaxWidth().padding(top = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(72.dp).clip(CircleShape).background(KyvoColors.PurpleSoft), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Search, null, tint = KyvoColors.PurplePrimary, modifier = Modifier.size(34.dp)) }
        Text("Busca un alimento", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
        Text("Escribe al menos 2 caracteres para consultar resultados reales.", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
fun FoodResultsRoute(query: String, repository: FoodRepository, onBack: () -> Unit, onSelect: (FoodSearchResult) -> Unit, onFuture: () -> Unit = {}) {
    val vm: FoodSearchViewModel = viewModel(factory = FoodSearchViewModel.factory(repository, query))
    val state by vm.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize()) {
        FoodHeader("Resultados", "Mostrando resultados para ‘${query.trim()}’", onBack)
        when (val current = state) {
            FoodSearchUiState.Idle -> LoadingFood("Preparando búsqueda…")
            FoodSearchUiState.Loading -> LoadingFood("Buscando alimentos…")
            is FoodSearchUiState.Empty -> FoodStateMessage("No encontramos alimentos", "Prueba con otro nombre o marca.", "Volver a buscar", onBack)
            is FoodSearchUiState.Error -> FoodStateMessage("No pudimos cargar resultados", current.message, "Reintentar", vm::retry)
            is FoodSearchUiState.Results -> {
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("${current.items.size} resultados", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                    TextButton(onClick = onFuture) { Icon(Icons.Outlined.Info, null); Text(" Filtros") }
                }
                LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(current.items, key = { "${it.type}:${it.id}" }) { result -> FoodResultRow(result, onClick = { onSelect(result) }) }
                    item { OutlinedButton(onClick = onFuture, modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp)) { Text("¿No encuentras tu alimento? Crear alimento personalizado") } }
                }
            }
        }
    }
}

@Composable
fun FoodDetailRoute(foodId: String, type: String, repository: FoodRepository, onBack: () -> Unit, onPortion: (FoodDetail) -> Unit) {
    val vm: FoodDetailViewModel = viewModel(factory = FoodDetailViewModel.factory(repository, foodId, type))
    val favoriteVm: FoodFavoriteViewModel = viewModel(factory = FoodFavoriteViewModel.factory(repository, foodId, type.toFoodType()))
    val state by vm.state.collectAsStateWithLifecycle()
    val favoriteState by favoriteVm.state.collectAsStateWithLifecycle()
    when (val current = state) {
        FoodDetailUiState.Loading -> LoadingFood("Cargando alimento…")
        FoodDetailUiState.NotFound -> FoodStateMessage("Alimento no disponible", "Este alimento ya no está disponible en el catálogo.", "Volver", onBack)
        is FoodDetailUiState.Error -> FoodStateMessage("No pudimos cargar el alimento", current.message, "Reintentar", vm::retry)
        is FoodDetailUiState.Content -> FoodDetailContent(current.food, favoriteState, onBack, { onPortion(current.food) }, favoriteVm::toggle)
    }
}

@Composable
private fun FoodDetailContent(food: FoodDetail, favoriteState: FoodFavoriteUiState, onBack: () -> Unit, onPortion: () -> Unit, onToggleFavorite: () -> Unit) {
    Scaffold(bottomBar = { KyvoPrimaryButton("Elegir porción", onPortion, Modifier.padding(20.dp)) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 16.dp)) {
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Volver") }
                    Text("Detalle del alimento", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    IconButton(onClick = onToggleFavorite) { Icon(if (favoriteState.isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder, if (favoriteState.isFavorite) "Quitar de favoritos" else "Agregar a favoritos", tint = KyvoColors.PurplePrimary) }
                }
            }
            item { FoodImage(food.imageUrl, food.imageKey, Modifier.fillMaxWidth().height(210.dp)) }
            item {
                Column(Modifier.padding(20.dp)) {
                    Text(food.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    food.variant?.takeIf(String::isNotBlank)?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp)) }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 10.dp)) {
                        Icon(Icons.Outlined.CheckCircle, null, tint = KyvoColors.Success, modifier = Modifier.size(18.dp))
                        Text(" Información del catálogo", color = KyvoColors.Success, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            item { NutrientCard("Por 100 g", food.nutrients.forGrams(100.0)) }
            favoriteState.error?.let { error -> item { Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 20.dp)) } }
            item { ServingList(food.servings) }
        }
    }
}

@Composable
fun FoodPortionRoute(foodId: String, type: String, repository: FoodRepository, mealRepository: MealRepository, onBack: () -> Unit, onRegistered: () -> Unit, selectionContext: FoodSelectionContext = FoodSelectionContext.NORMAL_MEAL_LOGGING, onMealSharePortionConfirmed: (MealItem) -> Unit = {}) {
    val vm: FoodDetailViewModel = viewModel(factory = FoodDetailViewModel.factory(repository, foodId, type))
    val state by vm.state.collectAsStateWithLifecycle()
    when (val current = state) {
        FoodDetailUiState.Loading -> LoadingFood("Cargando alimento…")
        FoodDetailUiState.NotFound -> FoodStateMessage("Alimento no disponible", "No pudimos recuperar este alimento.", "Volver", onBack)
        is FoodDetailUiState.Error -> FoodStateMessage("No pudimos cargar el alimento", current.message, "Reintentar", vm::retry)
        is FoodDetailUiState.Content -> FoodPortionContent(
            current.food,
            mealRepository,
            onBack,
            onRegistered,
            onMealShareConfirmed = onMealSharePortionConfirmed.takeIf { selectionContext == FoodSelectionContext.MEAL_SHARE },
        )
    }
}

@Composable
private fun FoodPortionContent(food: FoodDetail, mealRepository: MealRepository, onBack: () -> Unit, onRegistered: () -> Unit, onMealShareConfirmed: ((MealItem) -> Unit)? = null) {
    var amountText by rememberSaveable { mutableStateOf("150") }
    var mealTypeName by rememberSaveable { mutableStateOf(MealType.Lunch.name) }
    var saving by rememberSaveable { mutableStateOf(false) }
    var saveError by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val validation = validateFoodAmount(amountText)
    val grams = validation.grams
    val nutrition = grams?.let(food.nutrients::forGrams)
    val complete = nutrition?.let { it.calories != null && it.protein != null && it.carbohydrates != null && it.fat != null } == true
    Scaffold(bottomBar = {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            saveError?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp)) }
            KyvoPrimaryButton("Agregar a mi comida", {
                if (grams != null && complete && !saving) {
                    saving = true; saveError = null
                    scope.launch {
                        runCatching { FoodRegistrationFactory.createMeal(food, grams, MealType.valueOf(mealTypeName)) }
                            .fold(
                                onSuccess = { meal ->
                                    if (onMealShareConfirmed != null) {
                                        onMealShareConfirmed(meal.items.single())
                                    } else {
                                        runCatching { mealRepository.addMeal(LocalDate.now(), meal) }
                                            .fold(onSuccess = { onRegistered() }, onFailure = { saveError = it.message ?: "No pudimos guardar la comida." })
                                    }
                                },
                                onFailure = { saveError = it.message ?: "No pudimos preparar el registro." },
                            )
                        saving = false
                    }
                }
            }, enabled = grams != null && complete, isLoading = saving)
        }
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { FoodHeader("Elegir porción", "Indica cuánto comiste y en qué comida registrarlo.", onBack) }
            item { FoodSummary(food) }
            item {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    Text("Porciones rápidas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(Modifier.horizontalScroll(rememberScrollState()).padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(100.0, 150.0, 200.0).forEach { quick -> AmountChip("${quick.toInt()} g", quick, amountText) { amountText = quick.toInt().toString() } }
                        food.servings.filter { it.gramEquivalent > 0 }.distinctBy { it.gramEquivalent }.forEach { serving -> AmountChip(serving.label, serving.gramEquivalent, amountText) { amountText = serving.gramEquivalent.cleanAmount() } }
                    }
                }
            }
            item {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    Text("Cantidad personalizada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = amountText, onValueChange = { amountText = it; saveError = null }, modifier = Modifier.fillMaxWidth().padding(top = 10.dp), singleLine = true, label = { Text("Gramos") }, suffix = { Text("g") }, isError = validation.error != null, supportingText = { validation.error?.let { Text(it) } }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { amountText = ((grams ?: 0.0) - 10.0).coerceAtLeast(1.0).cleanAmount() }) { Text("− 10 g") }
                        TextButton(onClick = { amountText = ((grams ?: 0.0) + 10.0).coerceAtMost(10_000.0).cleanAmount() }) { Text("+ 10 g") }
                    }
                }
            }
            item { nutrition?.let { NutrientCard("Estimado para ${amountText.replace(',', '.')} g", it) } }
            item {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    Text("Agregar a", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(Modifier.horizontalScroll(rememberScrollState()).padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MealType.entries.forEach { typeOption ->
                            val selected = typeOption.name == mealTypeName
                            Surface(color = if (selected) KyvoColors.PurplePrimary else MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(14.dp), modifier = Modifier.border(1.dp, if (selected) KyvoColors.PurplePrimary else MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)).clickable { mealTypeName = typeOption.name }) {
                                Text(typeOption.label, color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
            if (!complete && grams != null) item { Text("Información incompleta: no se puede registrar hasta tener calorías, proteína, carbohidratos y grasas.", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 20.dp)) }
        }
    }
}

@Composable
private fun FoodSummary(food: FoodDetail) {
    KyvoCard(Modifier.padding(horizontal = 20.dp), contentPadding = PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FoodImage(food.imageUrl, food.imageKey, Modifier.size(70.dp))
            Column(Modifier.weight(1f).padding(start = 12.dp)) { Text(food.name, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis); food.variant?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis) } }
            Text("${food.nutrients.forGrams(150.0).calories?.let { "$it kcal" } ?: "—"}", color = KyvoColors.PurplePrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AmountChip(label: String, grams: Double, selectedText: String, onClick: () -> Unit) {
    val selected = selectedText.replace(',', '.').toDoubleOrNull() == grams
    Surface(color = if (selected) KyvoColors.PurplePrimary else MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp), modifier = Modifier.border(1.dp, if (selected) KyvoColors.PurplePrimary else MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)).clickable(onClick = onClick)) {
        Text(label, color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp))
    }
}

@Composable
private fun ServingList(servings: List<FoodServing>) {
    if (servings.isNotEmpty()) KyvoCard(Modifier.padding(20.dp)) {
        Text("Porciones disponibles", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        servings.forEach { serving -> Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) { Text(serving.label, Modifier.weight(1f)); Text("${serving.gramEquivalent.cleanAmount()} g", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    }
}

@Composable
private fun NutrientCard(title: String, n: NutritionAmount) {
    KyvoCard(Modifier.padding(horizontal = 20.dp)) {
        Text(title, color = KyvoColors.PurplePrimary, fontWeight = FontWeight.SemiBold)
        Text(n.calories?.let { "$it kcal" } ?: "—", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
        Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.SpaceBetween) { NutrientValue("Proteína", n.protein, "g"); NutrientValue("Carbos", n.carbohydrates, "g"); NutrientValue("Grasas", n.fat, "g") }
        HorizontalDivider(Modifier.padding(vertical = 14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { NutrientValue("Fibra", n.fiber, "g"); NutrientValue("Azúcar", n.sugar, "g"); NutrientValue("Sodio", n.sodiumMg, "mg") }
    }
}

@Composable
private fun NutrientValue(label: String, value: Double?, unit: String) { Column { Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall); Text(value?.let { "${it.oneDecimal()} $unit" } ?: "—", fontWeight = FontWeight.Bold) } }

@Composable
private fun FoodResultRow(result: FoodSearchResult, onClick: () -> Unit) {
    KyvoCard(Modifier.fillMaxWidth().clickable(onClick = onClick), contentPadding = PaddingValues(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FoodImage(result.imageUrl, result.imageKey, Modifier.size(62.dp))
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(result.name, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                result.subtitle?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                Text(result.servingLabel ?: "Por 100 g", color = KyvoColors.PurplePrimary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
            }
            Column(horizontalAlignment = Alignment.End) { Text(result.nutrients.caloriesPer100g?.let { "${it.roundToInt()} kcal" } ?: "—", fontWeight = FontWeight.Bold); Text("${result.nutrients.proteinPer100g?.oneDecimal() ?: "—"} g prot.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall); Icon(Icons.Outlined.Add, "Agregar", tint = KyvoColors.PurplePrimary, modifier = Modifier.padding(top = 5.dp)) }
        }
    }
}

@Composable
internal fun FoodImage(imageUrl: String?, imageKey: String?, modifier: Modifier) {
    Box(modifier.clip(RoundedCornerShape(16.dp)).background(KyvoColors.PurpleSoft), contentAlignment = Alignment.Center) {
        if (!imageUrl.isNullOrBlank()) AsyncImage(model = imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        else Icon(Icons.Outlined.Search, null, tint = KyvoColors.PurplePrimary, modifier = Modifier.size(30.dp))
    }
}

@Composable
internal fun FoodHeader(title: String, subtitle: String, onBack: () -> Unit, showBack: Boolean = true) {
    Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (showBack) IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Volver") }
        Column(Modifier.weight(1f).padding(start = if (showBack) 0.dp else 8.dp)) { Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 3.dp)) }
        if (!showBack) Icon(Icons.Outlined.Info, "Ayuda", tint = KyvoColors.PurplePrimary, modifier = Modifier.size(28.dp))
    }
}

@Composable
private fun QuickFoodAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    KyvoCard(modifier.clickable(onClick = onClick), contentPadding = PaddingValues(14.dp)) { Icon(icon, null, tint = KyvoColors.PurplePrimary, modifier = Modifier.size(25.dp)); Text(label, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 10.dp), maxLines = 1, overflow = TextOverflow.Ellipsis) }
}

@Composable private fun SectionLabel(text: String) { Text(text, color = KyvoColors.PurplePrimary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, letterSpacing = MaterialTheme.typography.labelMedium.letterSpacing) }
@Composable internal fun LoadingFood(text: String) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator(); Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 12.dp)) } } }
@Composable internal fun FoodStateMessage(title: String, message: String, action: String, onAction: () -> Unit) { Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center); Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp)); KyvoPrimaryButton(action, onAction, Modifier.padding(top = 20.dp)) } }
@Composable fun FoodStatePlaceholder(title: String, onBack: () -> Unit) { FoodStateMessage(title, "Esta sección pertenece a un checkpoint posterior.", "Volver", onBack) }

private fun Double.oneDecimal(): String = String.format(java.util.Locale.US, "%.1f", this)
private fun Double.cleanAmount(): String = if (this % 1.0 == 0.0) toInt().toString() else oneDecimal()
private fun String.toFoodType() = if (equals("Commercial", ignoreCase = true)) FoodType.Commercial else FoodType.Generic
