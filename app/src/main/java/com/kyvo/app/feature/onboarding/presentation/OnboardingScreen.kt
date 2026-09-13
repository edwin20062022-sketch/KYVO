package com.kyvo.app.feature.onboarding.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyvo.app.R
import com.kyvo.app.core.designsystem.KyvoColors
import com.kyvo.app.core.designsystem.component.KyvoBrandLockup
import com.kyvo.app.feature.onboarding.domain.model.ExperienceLevel
import com.kyvo.app.feature.onboarding.domain.model.FitnessGoal
import com.kyvo.app.feature.onboarding.domain.model.FoodPreference
import com.kyvo.app.feature.onboarding.domain.model.GenderOption
import com.kyvo.app.feature.onboarding.domain.model.NutritionPlan
import com.kyvo.app.feature.onboarding.domain.model.OnboardingMode
import com.kyvo.app.feature.onboarding.domain.model.OnboardingStep
import com.kyvo.app.feature.onboarding.domain.model.TrainingType
import com.kyvo.app.feature.onboarding.domain.model.WorkActivity
import com.kyvo.app.feature.onboarding.domain.validation.OnboardingLimits
import com.kyvo.app.feature.onboarding.domain.repository.OnboardingRepository
import com.kyvo.app.feature.onboarding.domain.validation.OnboardingValidationError
import com.kyvo.app.feature.onboarding.presentation.components.KyvoNumericInput
import com.kyvo.app.feature.onboarding.presentation.components.KyvoOptionCard
import com.kyvo.app.feature.onboarding.presentation.components.KyvoStepProgress
import com.kyvo.app.feature.onboarding.presentation.components.KyvoHorizontalWeightRuler
import com.kyvo.app.feature.onboarding.presentation.components.KyvoVerticalWheelPicker
import com.kyvo.app.feature.onboarding.presentation.components.WizardActions
import java.text.NumberFormat

@Composable
fun OnboardingRoute(
    repository: OnboardingRepository,
    onExit: () -> Unit,
    onCompleted: () -> Unit,
    mode: OnboardingMode = OnboardingMode.Initial,
    onCancel: () -> Unit = {},
    viewModel: OnboardingViewModel = viewModel(factory = OnboardingViewModel.factory(repository, mode = mode, onComplete = onCompleted, onCancel = onCancel)),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.shouldExit, state.shouldNavigateHome, state.shouldNavigateBack) {
        when {
            state.shouldNavigateBack -> {
                viewModel.onEvent(OnboardingEvent.NavigationHandled)
                onCancel()
            }
            state.shouldNavigateHome -> {
                viewModel.onEvent(OnboardingEvent.NavigationHandled)
                onCompleted()
            }
            state.shouldExit -> {
                viewModel.onEvent(OnboardingEvent.NavigationHandled)
                onExit()
            }
        }
    }
    OnboardingScreen(state = state, onEvent = viewModel::onEvent)
}

@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onEvent: (OnboardingEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        if (state.isRestoring) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Surface
        }
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val compact = maxWidth < 360.dp || maxHeight < 700.dp
            val horizontalPadding = if (maxWidth < 360.dp) 16.dp else 24.dp
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .imePadding()
                    .padding(horizontal = horizontalPadding),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = if (compact) 8.dp else 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    KyvoBrandLockup(horizontal = true, markSize = if (compact) 34.dp else 40.dp)
                    KyvoStepProgress(state.currentStep.number, OnboardingStep.Total)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                AnimatedContent(
                    targetState = state.currentStep,
                    label = "onboarding-step",
                    modifier = Modifier.weight(1f),
                ) { step ->
                    val usesWheelPicker = step in setOf(OnboardingStep.Age, OnboardingStep.Height, OnboardingStep.CurrentWeight)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (!usesWheelPicker) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                            .padding(top = if (compact) 18.dp else 28.dp, bottom = 16.dp),
                    ) {
                        StepContent(step, state, onEvent, compact)
                    }
                }
                if (state.currentStep != OnboardingStep.Calculating) {
                    WizardActions(
                        continueLabel = continueLabel(state.currentStep, state.mode),
                        onContinue = { onEvent(OnboardingEvent.Continue) },
                        onBack = { onEvent(OnboardingEvent.Back) },
                    )
                } else {
                    androidx.compose.material3.TextButton(
                        onClick = { onEvent(OnboardingEvent.Back) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                    ) { Text("Volver") }
                }
            }
        }
    }
}

@Composable
private fun StepContent(
    step: OnboardingStep,
    state: OnboardingUiState,
    onEvent: (OnboardingEvent) -> Unit,
    compact: Boolean,
) {
    when (step) {
        OnboardingStep.Gender -> {
            StepHeading("DATOS PERSONALES", "¿Cuál es tu género?", "Esta información nos ayuda a calcular tus necesidades nutricionales.", compact)
            OptionList(
                listOf(
                    Option("Masculino", "M", state.gender == GenderOption.Male, iconRes = R.drawable.ic_onboarding_male) { onEvent(OnboardingEvent.SelectGender(GenderOption.Male)) },
                    Option("Femenino", "F", state.gender == GenderOption.Female, iconRes = R.drawable.ic_onboarding_female) { onEvent(OnboardingEvent.SelectGender(GenderOption.Female)) },
                    Option("Prefiero no decirlo", "—", state.gender == GenderOption.PreferNotToSay, iconRes = R.drawable.ic_onboarding_undisclosed) { onEvent(OnboardingEvent.SelectGender(GenderOption.PreferNotToSay)) },
                ),
            )
            StepError(state.validationError)
        }
        OnboardingStep.Age -> {
            Image(
                painter = painterResource(R.drawable.ic_onboarding_age),
                contentDescription = null,
                modifier = Modifier.size(64.dp).padding(bottom = 8.dp),
            )
            StepHeading("DATOS PERSONALES", "¿Cuál es tu edad?",
                "Tu edad nos ayuda a calcular tu gasto calórico y necesidades nutricionales.", compact)
            KyvoVerticalWheelPicker(
                items = (OnboardingLimits.MinimumVisualAge..OnboardingLimits.MaximumAge).toList(),
                selectedItem = (state.ageInput.toIntOrNull() ?: OnboardingLimits.DefaultAge).coerceIn(OnboardingLimits.MinimumAge, OnboardingLimits.MaximumAge),
                onItemSelected = { onEvent(OnboardingEvent.ChangeAge(it.coerceAtLeast(OnboardingLimits.MinimumAge).toString())) },
                label = "años",
            )
            StepError(state.validationError)
        }
        OnboardingStep.Height -> {
            Image(
                painter = painterResource(R.drawable.ic_onboarding_height),
                contentDescription = null,
                modifier = Modifier.size(64.dp).padding(bottom = 8.dp),
            )
            StepHeading("DATOS PERSONALES", "¿Cuál es tu altura?",
                "Tu altura nos ayuda a estimar tu gasto calórico y calcular tus macros.", compact)
            KyvoVerticalWheelPicker(
                items = (OnboardingLimits.MinimumHeightCm.toInt()..OnboardingLimits.MaximumHeightCm.toInt()).toList(),
                selectedItem = (state.heightInput.toDoubleOrNull()?.toInt() ?: OnboardingLimits.DefaultHeightCm.toInt()).coerceIn(OnboardingLimits.MinimumHeightCm.toInt(), OnboardingLimits.MaximumHeightCm.toInt()),
                onItemSelected = { onEvent(OnboardingEvent.ChangeHeight(it.toString())) },
                label = "cm",
            )
            StepError(state.validationError)
        }
        OnboardingStep.CurrentWeight -> {
            Image(
                painter = painterResource(R.drawable.ic_onboarding_weight),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(64.dp).padding(bottom = 8.dp),
            )
            StepHeading("DATOS PERSONALES", "¿Cuál es tu peso actual?",
                "Tu peso actual nos ayuda a calcular tus necesidades calóricas diarias.", compact)
            val weightValues = remember {
                generateSequence(OnboardingLimits.MinimumWeightKg) { it + 1.0 }
                    .takeWhile { it <= OnboardingLimits.MaximumWeightKg + 0.01 }
                    .map { "%.1f".format(it).toDouble() }
                    .toList()
            }
            KyvoHorizontalWeightRuler(
                values = weightValues,
                selectedValue = weightValues.firstOrNull { it == (state.weightInput.toDoubleOrNull() ?: OnboardingLimits.MinimumWeightKg) }
                    ?: OnboardingLimits.MinimumWeightKg,
                onValueSelected = { onEvent(OnboardingEvent.ChangeWeight("%.1f".format(it))) },
            )
            StepError(state.validationError)
        }
        OnboardingStep.TrainingDays -> {
            StepHeading("ACTIVIDAD", "¿Cuántos días entrenas a la semana?", "Esto nos ayuda a calcular tu nivel de actividad física.", compact)
            OptionList((1..7).map { day ->
                Option(TRAINING_DAYS_LABEL, day.toString(), state.trainingDaysPerWeek == day) {
                    onEvent(OnboardingEvent.SelectTrainingDays(day))
                }
            })
            StepError(state.validationError)
        }
        OnboardingStep.TrainingType -> {
            StepHeading("ACTIVIDAD", "¿Qué tipo de entrenamiento realizas principalmente?", "Selecciona el que mejor representa tu entrenamiento.", compact)
            OptionList(TrainingType.entries.map { value ->
                Option(value.label(), value.label().take(1), state.trainingType == value, value.description(), value.iconRes()) {
                    onEvent(OnboardingEvent.SelectTrainingType(value))
                }
            })
            StepError(state.validationError)
        }
        OnboardingStep.WorkActivity -> {
            StepHeading("ACTIVIDAD LABORAL", "¿Cuál describe mejor tu actividad laboral?", "Consideramos tu actividad diaria fuera del entrenamiento.", compact)
            OptionList(WorkActivity.entries.map { value ->
                Option(value.label(), value.label().take(1), state.workActivity == value, value.description(), value.iconRes()) {
                    onEvent(OnboardingEvent.SelectWorkActivity(value))
                }
            })
            StepError(state.validationError)
        }
        OnboardingStep.Goal -> {
            StepHeading("OBJETIVO", "¿Cuál es tu objetivo principal?", "Esto nos ayuda a crear un plan nutricional personalizado para ti.", compact)
            OptionList(FitnessGoal.entries.map { value ->
                Option(value.label(), value.label().take(1), state.goal == value, value.description(), value.iconRes()) {
                    onEvent(OnboardingEvent.SelectGoal(value))
                }
            })
            StepError(state.validationError)
        }
        OnboardingStep.Experience -> {
            StepHeading("EXPERIENCIA", "¿Cuál es tu nivel de experiencia?", "Conservaremos este dato para adaptar el tono de futuras recomendaciones.", compact)
            OptionList(ExperienceLevel.entries.map { value ->
                Option(value.label(), value.label().take(1), state.experience == value, value.description(), value.iconRes()) {
                    onEvent(OnboardingEvent.SelectExperience(value))
                }
            })
            StepError(state.validationError)
        }
        OnboardingStep.FoodPreference -> {
            StepHeading("PREFERENCIAS ALIMENTICIAS", "¿Tienes alguna preferencia o restricción alimenticia?", "Selecciona la opción que mejor representa tu alimentación.", compact)
            OptionList(FoodPreference.entries.map { value ->
                Option(value.label(), value.label().take(1), state.foodPreference == value, value.description(), value.iconRes()) {
                    onEvent(OnboardingEvent.SelectFoodPreference(value))
                }
            })
            if (state.foodPreference == FoodPreference.Other) {
                Spacer(Modifier.height(16.dp))
                CustomRestrictionsSection(state, onEvent)
            }
            StepError(state.validationError)
        }
        OnboardingStep.MealsPerDay -> MealsStep(state, onEvent, compact)
        OnboardingStep.Calculating -> CalculatingStep()
        OnboardingStep.CalorieReveal -> CalorieReveal(state.plan)
        OnboardingStep.MacroReveal -> MacroReveal(state.plan)
        OnboardingStep.Summary -> SummaryStep(state)
    }
}

@Composable
private fun StepHeading(eyebrow: String, title: String, description: String, compact: Boolean) {
    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primaryContainer) {
        Text(
            eyebrow,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
        )
    }
    Spacer(Modifier.height(if (compact) 14.dp else 22.dp))
    Text(
        title,
        style = if (compact) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Black,
        modifier = Modifier.semantics { heading() },
    )
    Spacer(Modifier.height(10.dp))
    Text(description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(if (compact) 18.dp else 28.dp))
}

private data class Option(
    val title: String,
    val badge: String,
    val selected: Boolean,
    val description: String? = null,
    val iconRes: Int? = null,
    val onClick: () -> Unit,
)

@Composable
private fun OptionList(options: List<Option>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        options.forEach {
            KyvoOptionCard(
                title = it.title,
                description = it.description,
                badge = it.badge,
                iconRes = it.iconRes,
                selected = it.selected,
                onClick = it.onClick,
                testTag = OPTION_TAG_PREFIX + it.title,
            )
        }
    }
}

@Composable
private fun CustomRestrictionsSection(state: OnboardingUiState, onEvent: (OnboardingEvent) -> Unit) {
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    Column {
        OutlinedTextField(
            value = state.customRestrictionInput,
            onValueChange = { onEvent(OnboardingEvent.ChangeCustomRestrictionInput(it)) },
            label = { Text("Escribe tu restricción") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    onEvent(OnboardingEvent.AddCustomRestriction)
                    focusManager.clearFocus()
                    keyboard?.hide()
                },
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        androidx.compose.material3.TextButton(
            onClick = {
                onEvent(OnboardingEvent.AddCustomRestriction)
                focusManager.clearFocus()
                keyboard?.hide()
            },
            enabled = state.customRestrictionInput.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Añadir")
        }
        if (state.customDietaryRestrictions.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            state.customDietaryRestrictions.forEachIndexed { index, restriction ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = restriction,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(
                            onClick = { onEvent(OnboardingEvent.RemoveCustomRestriction(index)) },
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Eliminar restricción $restriction",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NumericStep(
    eyebrow: String,
    title: String,
    description: String,
    inputLabel: String,
    value: String,
    suffix: String,
    keyboardType: KeyboardType,
    error: OnboardingValidationError?,
    compact: Boolean,
    onChange: (String) -> Unit,
    onDone: () -> Unit,
    testTag: String,
    iconRes: Int? = null,
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    StepHeading(eyebrow, title, description, compact)
    KyvoNumericInput(
        value = value,
        onValueChange = onChange,
        label = inputLabel,
        suffix = suffix,
        errorText = error?.message(),
        keyboardType = keyboardType,
        focusRequester = focusRequester,
        onDone = {
            focusManager.clearFocus()
            keyboard?.hide()
            onDone()
        },
        testTag = testTag,
        iconRes = iconRes,
    )
}

@Composable
private fun MealsStep(state: OnboardingUiState, onEvent: (OnboardingEvent) -> Unit, compact: Boolean) {
    StepHeading("ALIMENTACIÓN", "¿Cuántas comidas sueles hacer al día?", "Este dato organiza tu plan; no modifica tus calorías.", compact)
    val fixed = listOf(2, 3, 4, 5)
    OptionList(fixed.map { count ->
        Option(count.toString() + " comidas", count.toString(), !state.isCustomMeals && state.mealsPerDay == count, mealDescription(count), count.mealIconRes()) {
            onEvent(OnboardingEvent.SelectMeals(count))
        }
    } + Option("Personalizado", "±", state.isCustomMeals, "Define entre 1 y 8 comidas.", R.drawable.ic_meals_custom) {
        onEvent(OnboardingEvent.SelectMeals(state.mealsPerDay, custom = true))
    })
    if (state.isCustomMeals) {
        Spacer(Modifier.height(12.dp))
        val requester = remember { FocusRequester() }
        KyvoNumericInput(
            value = state.mealsPerDay?.toString().orEmpty(),
            onValueChange = { onEvent(OnboardingEvent.SelectMeals(it.toIntOrNull(), custom = true)) },
            label = "Número de comidas",
            suffix = "al día",
            errorText = state.validationError?.message(),
            keyboardType = KeyboardType.Number,
            focusRequester = requester,
            onDone = { onEvent(OnboardingEvent.Continue) },
            testTag = MEALS_INPUT_TAG,
        )
    } else StepError(state.validationError)
}

@Composable
private fun CalculatingStep() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Calculando\ntu plan personalizado", textAlign = TextAlign.Center, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(16.dp))
        Text("Estamos analizando tu información para crear un punto de partida para ti.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(48.dp))
        Image(painterResource(R.drawable.ic_reveal_calculating), contentDescription = null, modifier = Modifier.size(120.dp))
        Spacer(Modifier.height(20.dp))
        CircularProgressIndicator(modifier = Modifier.size(156.dp), strokeWidth = 10.dp)
        Spacer(Modifier.height(32.dp))
        Text("Calculando calorías y macros", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(36.dp))
        InfoCard("Tu información se guarda únicamente en el almacenamiento privado de la aplicación.")
    }
}

@Composable
private fun CalorieReveal(plan: NutritionPlan?) {
    StepHeading("¡LO TENEMOS!", "Tu plan de calorías está listo", "Calculamos tu gasto energético y el ajuste correspondiente a tu objetivo.", false)
    if (plan == null) return
    ResultCard("Tu gasto energético diario (TDEE)", format(plan.tdeeKcal) + " kcal", "Estimación para mantener tu peso actual.", R.drawable.ic_reveal_expenditure)
    Spacer(Modifier.height(16.dp))
    Text("Ajuste por objetivo: " + signedPercent(plan.goalAdjustmentFraction), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(16.dp))
    ResultCard("Tu objetivo calórico diario", format(plan.targetCaloriesKcal) + " kcal", "Punto de partida moderado y ajustable.", R.drawable.ic_reveal_intake)
    plan.calculationNote?.let {
        Spacer(Modifier.height(16.dp))
        InfoCard(it)
    }
}

@Composable
private fun MacroReveal(plan: NutritionPlan?) {
    StepHeading("TU PLAN KYVO", "Tus macronutrientes diarios", "La distribución mantiene consistencia energética con tu objetivo calórico.", false)
    if (plan == null) return
    MacroRow("Proteína", plan.proteinGrams, KyvoColors.Protein, plan.targetCaloriesKcal, iconRes = R.drawable.ic_macro_protein)
    Spacer(Modifier.height(12.dp))
    MacroRow("Carbohidratos", plan.carbohydrateGrams, KyvoColors.Carbohydrate, plan.targetCaloriesKcal, iconRes = R.drawable.ic_macro_carbohydrates)
    Spacer(Modifier.height(12.dp))
    MacroRow("Grasas", plan.fatGrams, KyvoColors.Fat, plan.targetCaloriesKcal, caloriesPerGram = 9, iconRes = R.drawable.ic_macro_fat)
    Spacer(Modifier.height(20.dp))
    InfoCard("La diferencia máxima frente al objetivo es producto del redondeo de gramos.")
}

@Composable
private fun MacroRow(name: String, grams: Int, color: androidx.compose.ui.graphics.Color, calories: Int, caloriesPerGram: Int = 4, iconRes: Int? = null) {
    val macroCalories = grams * caloriesPerGram
    val fraction = (macroCalories.toFloat() / calories).coerceIn(0f, 1f)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = color.copy(alpha = .08f),
        border = BorderStroke(1.dp, color.copy(alpha = .35f)),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    iconRes?.let { Image(painterResource(it), contentDescription = null, modifier = Modifier.size(28.dp)) }
                    Spacer(Modifier.width(8.dp))
                    Text(name, color = color, style = MaterialTheme.typography.titleMedium)
                }
                Text(grams.toString() + " g", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth(), color = color)
            Spacer(Modifier.height(6.dp))
            Text(macroCalories.toString() + " kcal · " + (fraction * 100).toInt() + "%", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SummaryStep(state: OnboardingUiState) {
    val isEdit = state.mode == OnboardingMode.Edit
    StepHeading(
        if (isEdit) "REVISAR CAMBIOS" else "RESUMEN",
        if (isEdit) "Revisa tu información antes de guardar" else "¡Listo! Aquí está tu plan personalizado",
        if (isEdit) "Confirma que tus datos sean correctos." else "Tus datos, objetivo y preferencias quedan reunidos en un único plan.",
        false,
    )
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Tu información", style = MaterialTheme.typography.titleLarge)
            SummaryLine("Género", state.gender?.label().orEmpty())
            SummaryLine("Edad", state.ageInput + " años")
            SummaryLine("Altura", state.heightInput + " cm")
            SummaryLine("Peso actual", state.weightInput + " kg")
            SummaryLine("Entrenamiento", state.trainingDaysPerWeek.toString() + " días · " + state.trainingType?.label().orEmpty())
            SummaryLine("Actividad laboral", state.workActivity?.label().orEmpty())
            SummaryLine("Objetivo", state.goal?.label().orEmpty())
            SummaryLine("Experiencia", state.experience?.label().orEmpty())
            SummaryLine("Alimentación", state.foodPreference?.label().orEmpty())
            if (state.customDietaryRestrictions.isNotEmpty()) {
                SummaryLine("Restricciones", state.customDietaryRestrictions.joinToString(", "))
            }
            SummaryLine("Comidas", state.mealsPerDay.toString() + " al día")
        }
    }
    Spacer(Modifier.height(16.dp))
    state.plan?.let { plan ->
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .35f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = .3f)),
        ) {
            Column(Modifier.fillMaxWidth().padding(20.dp)) {
                Text("Tu plan nutricional", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                Text(format(plan.targetCaloriesKcal) + " kcal", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
                Text(plan.proteinGrams.toString() + " g proteína · " + plan.carbohydrateGrams + " g carbohidratos · " + plan.fatGrams + " g grasas")
            }
        }
    }
    Spacer(Modifier.height(16.dp))
    InfoCard("Este cálculo es un punto de partida informativo, no una prescripción médica.")
}

@Composable
private fun ResultCard(title: String, value: String, description: String, iconRes: Int? = null) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .28f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = .3f)),
    ) {
        Column(Modifier.padding(22.dp)) {
            iconRes?.let { Image(painterResource(it), contentDescription = null, modifier = Modifier.size(52.dp)) }
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun InfoCard(text: String) {
    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .35f)) {
        Text(text, modifier = Modifier.fillMaxWidth().padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SummaryLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(16.dp))
        Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End)
    }
}

@Composable
private fun StepError(validationError: OnboardingValidationError?) {
    validationError ?: return
    val message = validationError.message()
    Spacer(Modifier.height(12.dp))
    Text(
        message,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.semantics { error(message) }.testTag(ERROR_TAG),
    )
}

private fun continueLabel(step: OnboardingStep, mode: OnboardingMode): String = when {
    step == OnboardingStep.Summary && mode == OnboardingMode.Edit -> "Guardar cambios"
    step == OnboardingStep.CalorieReveal -> "Ver mis macros"
    step == OnboardingStep.MacroReveal -> "Ver resumen de mi plan"
    step == OnboardingStep.Summary -> "Comenzar mi plan"
    else -> "Continuar"
}

private fun OnboardingValidationError.message(): String = when (this) {
    OnboardingValidationError.SelectionRequired -> "Selecciona una opción para continuar."
    OnboardingValidationError.InvalidNumber -> "Ingresa un número válido."
    OnboardingValidationError.AgeOutOfRange -> "Ingresa una edad entre 1 y 150 años."
    OnboardingValidationError.HeightOutOfRange -> "Ingresa una altura entre 100 y 300 cm."
    OnboardingValidationError.WeightOutOfRange -> "Ingresa un peso entre 35 y 300 kg."
    OnboardingValidationError.TrainingDaysOutOfRange -> "Selecciona entre 1 y 7 días."
    OnboardingValidationError.MealsOutOfRange -> "Selecciona entre 1 y 8 comidas."
}

private fun TrainingType.label() = when (this) {
    TrainingType.Strength -> "Fuerza"
    TrainingType.Hypertrophy -> "Hipertrofia"
    TrainingType.Functional -> "Funcional"
    TrainingType.Cardio -> "Cardio"
}
private fun TrainingType.description() = when (this) {
    TrainingType.Strength -> "Pesas y ejercicios orientados a fuerza."
    TrainingType.Hypertrophy -> "Entrenamiento enfocado en masa muscular."
    TrainingType.Functional -> "Funcional, HIIT, crossfit o calistenia."
    TrainingType.Cardio -> "Carrera, bicicleta, elíptica u otro cardio."
}
private fun TrainingType.iconRes() = when (this) {
    TrainingType.Strength -> R.drawable.ic_onboarding_strength
    TrainingType.Hypertrophy -> R.drawable.ic_onboarding_hypertrophy
    TrainingType.Functional -> R.drawable.ic_onboarding_functional
    TrainingType.Cardio -> R.drawable.ic_onboarding_cardio
}
private fun WorkActivity.label() = when (this) {
    WorkActivity.Sedentary -> "Oficina / Sedentario"
    WorkActivity.Active -> "Activo"
    WorkActivity.Physical -> "Trabajo físico"
}
private fun WorkActivity.description() = when (this) {
    WorkActivity.Sedentary -> "Paso la mayor parte del día sentado."
    WorkActivity.Active -> "Camino, estoy de pie o realizo tareas ligeras."
    WorkActivity.Physical -> "Realizo trabajo físico exigente."
}
private fun WorkActivity.iconRes() = when (this) {
    WorkActivity.Sedentary -> R.drawable.ic_onboarding_office
    WorkActivity.Active -> R.drawable.ic_work_active
    WorkActivity.Physical -> R.drawable.ic_work_physical
}
private fun FitnessGoal.label() = when (this) {
    FitnessGoal.FatLoss -> "Bajar grasa"
    FitnessGoal.MuscleGain -> "Ganancia muscular"
    FitnessGoal.Recomposition -> "Recomposición corporal"
    FitnessGoal.Maintenance -> "Mantenimiento"
    FitnessGoal.Performance -> "Performance / fuerza"
}
private fun FitnessGoal.description() = when (this) {
    FitnessGoal.FatLoss -> "Reducir grasa preservando masa muscular."
    FitnessGoal.MuscleGain -> "Aumentar masa muscular de forma gradual."
    FitnessGoal.Recomposition -> "Ganar músculo y perder grasa."
    FitnessGoal.Maintenance -> "Mantener el peso y rendimiento actuales."
    FitnessGoal.Performance -> "Apoyar fuerza y capacidad física."
}
private fun FitnessGoal.iconRes() = when (this) {
    FitnessGoal.FatLoss -> R.drawable.ic_goal_fat_loss
    FitnessGoal.MuscleGain -> R.drawable.ic_goal_muscle_gain
    FitnessGoal.Recomposition -> R.drawable.ic_goal_recomposition
    FitnessGoal.Maintenance -> R.drawable.ic_goal_maintenance
    FitnessGoal.Performance -> R.drawable.ic_goal_performance
}
private fun ExperienceLevel.label() = when (this) {
    ExperienceLevel.Beginner -> "Principiante"
    ExperienceLevel.Intermediate -> "Intermedio"
    ExperienceLevel.Advanced -> "Avanzado"
}
private fun ExperienceLevel.description() = when (this) {
    ExperienceLevel.Beginner -> "Estoy comenzando mi camino en el gimnasio."
    ExperienceLevel.Intermediate -> "Entreno de manera regular."
    ExperienceLevel.Advanced -> "Tengo una base sólida de entrenamiento."
}
private fun ExperienceLevel.iconRes() = when (this) {
    ExperienceLevel.Beginner -> R.drawable.ic_experience_beginner
    ExperienceLevel.Intermediate -> R.drawable.ic_experience_intermediate
    ExperienceLevel.Advanced -> R.drawable.ic_experience_advanced
}
private fun FoodPreference.label() = when (this) {
    FoodPreference.None -> "Sin restricciones"
    FoodPreference.Vegetarian -> "Vegetariano"
    FoodPreference.Vegan -> "Vegano"
    FoodPreference.GlutenFree -> "Sin gluten"
    FoodPreference.DairyFree -> "Sin lácteos"
    FoodPreference.Other -> "Tengo otra restricción alimenticia"
}
private fun FoodPreference.description() = when (this) {
    FoodPreference.None -> "No tengo restricciones alimenticias."
    FoodPreference.Vegetarian -> "No consumo carne ni pescado."
    FoodPreference.Vegan -> "No consumo productos de origen animal."
    FoodPreference.GlutenFree -> "Evito alimentos que contienen gluten."
    FoodPreference.DairyFree -> "No consumo productos lácteos."
    FoodPreference.Other -> "Agrega restricciones alimentarias personalizadas."
}
private fun FoodPreference.iconRes() = when (this) {
    FoodPreference.None -> R.drawable.ic_food_none
    FoodPreference.Vegetarian -> R.drawable.ic_food_vegetarian
    FoodPreference.Vegan -> R.drawable.ic_food_vegan
    FoodPreference.GlutenFree -> R.drawable.ic_food_gluten_free
    FoodPreference.DairyFree -> R.drawable.ic_food_dairy_free
    FoodPreference.Other -> R.drawable.ic_onboarding_other
}
private fun GenderOption.label() = when (this) {
    GenderOption.Male -> "Masculino"
    GenderOption.Female -> "Femenino"
    GenderOption.PreferNotToSay -> "Prefiero no decirlo"
}
private fun mealDescription(count: Int) = when (count) {
    2 -> "Dos comidas principales al día."
    3 -> "Desayuno, comida y cena."
    4 -> "Tres comidas y un snack."
    else -> "Tres comidas principales y dos snacks."
}
private fun Int.mealIconRes() = when (this) {
    2 -> R.drawable.ic_meals_2
    3 -> R.drawable.ic_meals_3
    4 -> R.drawable.ic_meals_4
    else -> R.drawable.ic_meals_5
}
private fun format(value: Number): String = NumberFormat.getIntegerInstance().format(value.toDouble())
private fun signedPercent(value: Double): String = when {
    value > 0 -> "+" + (value * 100).toInt() + "%"
    value < 0 -> (value * 100).toInt().toString() + "%"
    else -> "0%"
}

const val AGE_INPUT_TAG = "onboarding_age"
const val TRAINING_DAYS_LABEL = "días a la semana"
const val HEIGHT_INPUT_TAG = "onboarding_height"
const val WEIGHT_INPUT_TAG = "onboarding_weight"
const val MEALS_INPUT_TAG = "onboarding_meals"
const val OPTION_TAG_PREFIX = "onboarding_option_"
const val ERROR_TAG = "onboarding_error"
