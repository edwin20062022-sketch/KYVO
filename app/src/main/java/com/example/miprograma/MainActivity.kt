package com.example.miprograma

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

/** Punto de entrada: la interfaz completa se construye con Jetpack Compose. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EventRegistryTheme { EventRegistryApp() }
        }
    }
}

/** Prioridades disponibles en el formulario. */
enum class EventPriority(val label: String) {
    LOW("Baja"), MEDIUM("Media"), HIGH("Alta")
}

/** Modelo inmutable de un evento registrado. */
data class Event(
    val id: Int,
    val title: String,
    val date: LocalDate,
    val priority: EventPriority,
    val description: String
)

/**
 * Fuente única de verdad. El ViewModel conserva la lista durante recreaciones de la
 * actividad, como cambios de orientación o de tema del dispositivo.
 */
class EventViewModel : ViewModel() {
    private var nextId by mutableIntStateOf(3)

    val events = mutableStateListOf(
        Event(
            1,
            "Presentar prototipo",
            LocalDate.now().plusDays(2),
            EventPriority.HIGH,
            "Mostrar el flujo principal y recopilar comentarios del equipo."
        ),
        Event(
            2,
            "Revisión de avances",
            LocalDate.now().plusDays(5),
            EventPriority.MEDIUM,
            "Validar animaciones, accesibilidad y comportamiento del formulario."
        )
    )

    fun addEvent(
        title: String,
        date: LocalDate,
        priority: EventPriority,
        description: String
    ) {
        events.add(
            0,
            Event(nextId++, title.trim(), date, priority, description.trim())
        )
    }

    fun deleteEvent(event: Event): Int {
        val previousIndex = events.indexOf(event)
        if (previousIndex >= 0) events.removeAt(previousIndex)
        return previousIndex
    }

    fun restoreEvent(event: Event, index: Int) {
        if (events.none { it.id == event.id }) {
            events.add(index.coerceIn(0, events.size), event)
        }
    }
}

private object Routes {
    const val LIST = "event_list"
    const val FORM = "event_form"
}

/** NavHost y ViewModel compartido por las dos pantallas. */
@Composable
fun EventRegistryApp(
    navController: NavHostController = rememberNavController(),
    eventViewModel: EventViewModel = viewModel()
) {
    NavHost(navController = navController, startDestination = Routes.LIST) {
        composable(Routes.LIST) {
            EventListScreen(
                events = eventViewModel.events,
                onAddClick = { navController.navigate(Routes.FORM) },
                onDelete = eventViewModel::deleteEvent,
                onUndoDelete = eventViewModel::restoreEvent
            )
        }
        composable(Routes.FORM) {
            EventFormScreen(
                onBack = { navController.popBackStack() },
                onSave = { title, date, priority, description ->
                    eventViewModel.addEvent(title, date, priority, description)
                    navController.popBackStack()
                }
            )
        }
    }
}

/** Pantalla principal con lista animada y gesto horizontal para borrar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    events: List<Event>,
    onAddClick: () -> Unit,
    onDelete: (Event) -> Int,
    onUndoDelete: (Event, Int) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mis eventos", fontWeight = FontWeight.Bold)
                        Text(
                            if (events.size == 1) "1 evento próximo" else "${events.size} eventos próximos",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAddClick()
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nuevo evento") },
                elevation = FloatingActionButtonDefaults.elevation(6.dp)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedVisibility(
                visible = events.isEmpty(),
                enter = fadeIn() + slideInVertically { it / 3 },
                exit = fadeOut() + slideOutVertically { it / 3 }
            ) {
                EmptyEventsState(onAddClick)
            }

            if (events.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 18.dp, 16.dp, 104.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "Desliza una tarjeta hacia la izquierda para eliminarla.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                    items(events, key = { it.id }) { event ->
                        SwipeableEventCard(
                            event = event,
                            onDelete = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val previousIndex = onDelete(event)
                                coroutineScope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Evento eliminado",
                                        actionLabel = "Deshacer"
                                    )
                                    if (result == SnackbarResult.ActionPerformed && previousIndex >= 0) {
                                        onUndoDelete(event, previousIndex)
                                    }
                                }
                            },
                            modifier = Modifier.animateContentSize()
                        )
                    }
                }
            }
        }
    }
}

/** Contenedor que revela el fondo rojo mientras el usuario desliza. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableEventCard(
    event: Event,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState()
    LaunchedEffect(Unit) { isVisible = true }
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) onDelete()
    }

    AnimatedVisibility(
        visible = isVisible,
        modifier = modifier,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 }
    ) {
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = false,
            enableDismissFromEndToStart = true,
            backgroundContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.errorContainer,
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 22.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar evento",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        ) {
            EventCard(event)
        }
    }
}

/** Tarjeta plegable con tamaño, elevación y color animados. */
@Composable
fun EventCard(event: Event, modifier: Modifier = Modifier) {
    var isExpanded by rememberSaveable(event.id) { mutableStateOf(false) }
    val containerColor by animateColorAsState(
        if (isExpanded) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.surfaceContainer,
        label = "eventCardColor"
    )
    val elevation by animateDpAsState(
        if (isExpanded) 8.dp else 2.dp,
        label = "eventCardElevation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .semantics {
                stateDescription = if (isExpanded) "Tarjeta expandida" else "Tarjeta contraída"
            }
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                PriorityIndicator(event.priority)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(5.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            formatDisplayDate(event.date),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.semantics {
                        contentDescription = if (isExpanded) {
                            "Contraer detalles de ${event.title}"
                        } else "Expandir detalles de ${event.title}"
                    }
                ) {
                    Icon(
                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

            AnimatedVisibility(isExpanded) {
                Column {
                    HorizontalDivider(Modifier.padding(vertical = 14.dp))
                    Text(
                        "Descripción",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        event.description.ifBlank { "Sin descripción adicional." },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun PriorityIndicator(priority: EventPriority) {
    val color = priorityColor(priority)
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(54.dp)) {
        Box(Modifier.size(12.dp).background(color, RoundedCornerShape(50)))
        Spacer(Modifier.height(5.dp))
        Text(
            priority.label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Formulario con validaciones que se actualizan mientras el usuario escribe. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventFormScreen(
    onBack: () -> Unit,
    onSave: (String, LocalDate, EventPriority, String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var dateText by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var priorityName by rememberSaveable { mutableStateOf(EventPriority.MEDIUM.name) }
    var titleTouched by rememberSaveable { mutableStateOf(false) }
    var dateTouched by rememberSaveable { mutableStateOf(false) }
    var descriptionTouched by rememberSaveable { mutableStateOf(false) }
    var titleError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val priority = EventPriority.valueOf(priorityName)

    // Los efectos mantienen los mensajes sincronizados con cada cambio de entrada.
    LaunchedEffect(title, titleTouched) {
        titleError = if (titleTouched) validateTitle(title) else null
    }
    LaunchedEffect(dateText, dateTouched) {
        dateError = if (dateTouched) validateDate(dateText) else null
    }
    LaunchedEffect(description, descriptionTouched) {
        descriptionError = if (descriptionTouched) validateDescription(description) else null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar evento", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver a eventos")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                "Datos del evento",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Los campos marcados con * son obligatorios.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleTouched = true
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Título *") },
                placeholder = { Text("Ej. Entrega del proyecto") },
                singleLine = true,
                isError = titleError != null,
                supportingText = { Text(titleError ?: "Máximo 60 caracteres") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = dateText,
                onValueChange = {
                    if (it.length <= 10) dateText = it
                    dateTouched = true
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Fecha *") },
                placeholder = { Text("AAAA-MM-DD") },
                singleLine = true,
                isError = dateError != null,
                supportingText = { Text(dateError ?: "Debe ser una fecha posterior a hoy") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Elegir fecha")
                    }
                },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Prioridad", style = MaterialTheme.typography.labelLarge)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EventPriority.entries.forEach { option ->
                        FilterChip(
                            selected = priority == option,
                            onClick = { priorityName = option.name },
                            label = { Text(option.label) },
                            leadingIcon = {
                                Box(
                                    Modifier
                                        .size(9.dp)
                                        .background(priorityColor(option), RoundedCornerShape(50))
                                )
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                    descriptionTouched = true
                },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                label = { Text("Descripción") },
                placeholder = { Text("Agrega contexto, ubicación o indicaciones...") },
                minLines = 4,
                isError = descriptionError != null,
                supportingText = {
                    Text(descriptionError ?: "${description.length}/300 caracteres")
                },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = ImeAction.Done
                )
            )

            Button(
                onClick = {
                    titleTouched = true
                    dateTouched = true
                    descriptionTouched = true
                    val currentTitleError = validateTitle(title)
                    val currentDateError = validateDate(dateText)
                    val currentDescriptionError = validateDescription(description)
                    titleError = currentTitleError
                    dateError = currentDateError
                    descriptionError = currentDescriptionError

                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (currentTitleError == null &&
                        currentDateError == null &&
                        currentDescriptionError == null
                    ) {
                        onSave(title, LocalDate.parse(dateText), priority, description)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Guardar evento")
            }
            Spacer(Modifier.height(12.dp))
        }
    }

    if (showDatePicker) {
        EventDatePickerDialog(
            onDismiss = { showDatePicker = false },
            onDateSelected = {
                dateText = it.toString()
                dateTouched = true
                showDatePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventDatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val tomorrowUtc = LocalDate.now().plusDays(1)
        .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = tomorrowUtc)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(
                            Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        )
                    }
                }
            ) { Text("Aceptar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    ) {
        DatePicker(state = pickerState)
    }
}

@Composable
private fun EmptyEventsState(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 36.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(28.dp)
        ) {
            Icon(
                Icons.Default.DateRange,
                contentDescription = null,
                modifier = Modifier.padding(20.dp).size(44.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(
            "Aún no hay eventos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(7.dp))
        Text(
            "Registra tu primer recordatorio para mantener todo bajo control.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Crear evento")
        }
    }
}

private fun validateTitle(value: String): String? = when {
    value.isBlank() -> "El título es obligatorio."
    value.trim().length < 3 -> "Escribe al menos 3 caracteres."
    value.trim().length > 60 -> "El título no puede superar 60 caracteres."
    else -> null
}

private fun validateDate(value: String): String? {
    if (value.isBlank()) return "La fecha es obligatoria."
    val parsedDate = try {
        LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (_: DateTimeParseException) {
        return "Usa una fecha válida con formato AAAA-MM-DD."
    }
    return if (!parsedDate.isAfter(LocalDate.now())) {
        "Selecciona una fecha futura."
    } else null
}

private fun validateDescription(value: String): String? =
    if (value.length > 300) "La descripción no puede superar 300 caracteres." else null

private fun formatDisplayDate(date: LocalDate): String = date.format(
    DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-MX"))
)

@Composable
private fun priorityColor(priority: EventPriority): Color = when (priority) {
    EventPriority.LOW -> MaterialTheme.colorScheme.tertiary
    EventPriority.MEDIUM -> Color(0xFFE07A1F)
    EventPriority.HIGH -> MaterialTheme.colorScheme.error
}

private val LightEventColors = lightColorScheme(
    primary = Color(0xFF315DA8), onPrimary = Color.White,
    primaryContainer = Color(0xFFD9E2FF), onPrimaryContainer = Color(0xFF0B285A),
    secondary = Color(0xFF596070), onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDE2F3), onSecondaryContainer = Color(0xFF161C28),
    tertiary = Color(0xFF287A62), background = Color(0xFFF9F9FF),
    onBackground = Color(0xFF191B20), surface = Color(0xFFF9F9FF),
    onSurface = Color(0xFF191B20), surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44464F), error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6)
)

private val DarkEventColors = darkColorScheme(
    primary = Color(0xFFAFC6FF), onPrimary = Color(0xFF002E69),
    primaryContainer = Color(0xFF164582), onPrimaryContainer = Color(0xFFD9E2FF),
    secondary = Color(0xFFC1C6D6), onSecondary = Color(0xFF2B303B),
    secondaryContainer = Color(0xFF414752), onSecondaryContainer = Color(0xFFDDE2F3),
    tertiary = Color(0xFF8BD8BA), background = Color(0xFF111318),
    onBackground = Color(0xFFE2E2E9), surface = Color(0xFF111318),
    onSurface = Color(0xFFE2E2E9), surfaceVariant = Color(0xFF44464F),
    onSurfaceVariant = Color(0xFFC4C6D0), error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A)
)

private val EventTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 21.sp,
        lineHeight = 27.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 23.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
)

@Composable
fun EventRegistryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkEventColors else LightEventColors,
        typography = EventTypography,
        content = content
    )
}

@Preview(name = "Lista de eventos - Claro", showBackground = true)
@Composable
private fun EventListLightPreview() {
    EventRegistryTheme(darkTheme = false) {
        EventListScreen(
            events = previewEvents,
            onAddClick = {},
            onDelete = { -1 },
            onUndoDelete = { _, _ -> }
        )
    }
}

@Preview(
    name = "Lista de eventos - Oscuro",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun EventListDarkPreview() {
    EventRegistryTheme(darkTheme = true) {
        EventListScreen(
            events = previewEvents,
            onAddClick = {},
            onDelete = { -1 },
            onUndoDelete = { _, _ -> }
        )
    }
}

private val previewEvents = listOf(
    Event(
        id = 1,
        title = "Presentar prototipo",
        date = LocalDate.now().plusDays(2),
        priority = EventPriority.HIGH,
        description = "Mostrar el flujo principal al equipo."
    )
)
