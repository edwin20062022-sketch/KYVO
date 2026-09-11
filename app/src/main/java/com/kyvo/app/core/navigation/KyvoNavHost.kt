package com.kyvo.app.core.navigation

import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.kyvo.app.domain.auth.AuthRepository
import com.kyvo.app.domain.auth.AuthState
import com.kyvo.app.feature.auth.presentation.LoginRoute
import com.kyvo.app.feature.onboarding.domain.repository.OnboardingRepository
import com.kyvo.app.feature.home.domain.repository.MealRepository
import com.kyvo.app.feature.home.presentation.HomeRoute
import com.kyvo.app.feature.progress.presentation.ProgressPlaceholder
import com.kyvo.app.feature.progress.presentation.ProgressRoute
import com.kyvo.app.feature.progress.presentation.ProgressViewModel
import com.kyvo.app.feature.progress.domain.NutritionMetric
import com.kyvo.app.feature.progress.presentation.NutritionMetricDetailRoute
import com.kyvo.app.feature.progress.presentation.NutritionMetricDetailViewModel
import com.kyvo.app.feature.progress.presentation.NutritionConsistencyRoute
import com.kyvo.app.feature.progress.presentation.NutritionConsistencyViewModel
import com.kyvo.app.feature.progress.presentation.NutritionHistoryRoute
import com.kyvo.app.feature.progress.presentation.NutritionHistoryViewModel
import com.kyvo.app.feature.progress.presentation.NutritionDayDetailRoute
import com.kyvo.app.feature.progress.presentation.NutritionDayDetailViewModel
import com.kyvo.app.feature.food.domain.repository.FoodRepository
import com.kyvo.app.feature.food.presentation.FoodDetailRoute
import com.kyvo.app.feature.food.presentation.FoodFavoritesRoute
import com.kyvo.app.feature.food.presentation.FoodFrequentRoute
import com.kyvo.app.feature.food.presentation.FoodHubScreen
import com.kyvo.app.feature.food.presentation.FoodPortionRoute
import com.kyvo.app.feature.food.presentation.FoodResultsRoute
import com.kyvo.app.feature.food.presentation.FoodSearchRoute
import com.kyvo.app.feature.food.presentation.FoodStatePlaceholder
import com.kyvo.app.feature.food.domain.model.FoodType
import com.kyvo.app.feature.dishes.domain.repository.SavedDishRepository
import com.kyvo.app.feature.dishes.presentation.AddSavedDishToDayRoute
import com.kyvo.app.feature.dishes.presentation.SavedDishDetailRoute
import com.kyvo.app.feature.dishes.presentation.SavedDishEditorRoute
import com.kyvo.app.feature.dishes.presentation.SavedDishesRoute
import com.kyvo.app.feature.mealshare.presentation.CameraPermissionGate
import com.kyvo.app.feature.mealshare.presentation.CameraScreen
import com.kyvo.app.feature.mealshare.presentation.MealShareDraftViewModel
import com.kyvo.app.feature.mealshare.presentation.MealSharePhotoPickerRoute
import com.kyvo.app.feature.mealshare.presentation.MealSharePhotoPreviewScreen
import com.kyvo.app.feature.mealshare.presentation.MealShareSourceScreen
import com.kyvo.app.feature.mealshare.presentation.MealShareMealBuilderScreen
import com.kyvo.app.feature.mealshare.presentation.MealShareEditorScreen
import com.kyvo.app.feature.mealshare.presentation.MealShareRenderViewModel
import com.kyvo.app.feature.mealshare.presentation.MealShareShareState
import com.kyvo.app.feature.mealshare.presentation.buildMealShareIntent
import com.kyvo.app.feature.mealshare.data.render.AndroidMealShareImageRenderer
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.content.FileProvider
import java.io.File
import java.time.LocalDate
import com.kyvo.app.feature.onboarding.presentation.OnboardingRoute
import com.kyvo.app.feature.profile.presentation.AthleteProfileRoute
import com.kyvo.app.feature.profile.presentation.EditAthleteProfileRoute
import com.kyvo.app.feature.profile.presentation.NutritionCalculationRoute
import com.kyvo.app.feature.profile.presentation.NutritionPlanRoute
import com.kyvo.app.feature.profile.presentation.RecalculateNutritionPlanViewModel
import com.kyvo.app.feature.profile.presentation.UpdateGoalRoute
import com.kyvo.app.feature.profile.presentation.RecalculateGoalsRoute
import com.kyvo.app.feature.profile.presentation.NewGoalsRoute
import com.kyvo.app.feature.profile.presentation.PersonalPreferencesViewModel
import com.kyvo.app.feature.profile.presentation.PersonalPreferencesHubRoute
import com.kyvo.app.feature.profile.presentation.PersonalDataRoute
import com.kyvo.app.feature.profile.presentation.ActivityTrainingRoute
import com.kyvo.app.feature.profile.presentation.ExperienceRoute
import com.kyvo.app.feature.profile.presentation.FoodPreferencesRoute
import com.kyvo.app.feature.profile.presentation.MealOrganizationRoute
import kotlinx.coroutines.launch

@Composable
fun KyvoNavHost(
    navController: NavHostController,
    authRepository: AuthRepository,
    onboardingRepository: OnboardingRepository?,
    mealRepository: MealRepository?,
    foodRepository: FoodRepository?,
    savedDishRepository: SavedDishRepository?,
    authState: AuthState,
    onboardingCompleted: Boolean,
) {
    val target = resolveStartDestination(authState, onboardingCompleted)
    val scope = rememberCoroutineScope()
    val recalculateViewModel = onboardingRepository?.let { repository -> viewModel<RecalculateNutritionPlanViewModel>(factory = RecalculateNutritionPlanViewModel.factory(repository)) }
    val personalPreferencesViewModel = onboardingRepository?.let { repository -> viewModel<PersonalPreferencesViewModel>(factory = PersonalPreferencesViewModel.factory(repository)) }
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentEntry?.destination
    val showBottomNavigation = shouldShowKyvoBottomNavigation(currentDestination?.route)
    LaunchedEffect(target) {
        if (navController.currentDestination?.route != target.route) {
            navController.navigate(target.route) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
    val normalContentModifier = if (target == KyvoDestination.Login || target == KyvoDestination.Onboarding) {
        Modifier.fillMaxSize()
    } else {
        Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)
    }
    Box(normalContentModifier) {
    NavHost(
        navController = navController,
        startDestination = target.route,
        modifier = Modifier.fillMaxSize().padding(bottom = if (showBottomNavigation) 132.dp else 0.dp),
    ) {
        composable(KyvoDestination.Login.route) {
            LoginRoute(authRepository = authRepository)
        }
        composable(KyvoDestination.Onboarding.route) {
            onboardingRepository?.let { repository ->
                OnboardingRoute(
                    repository = repository,
                    onExit = { scope.launch { authRepository.signOut() } },
                    onCompleted = {},
                )
            }
        }
        composable(KyvoDestination.Home.route) {
            if (onboardingRepository != null && mealRepository != null) {
                HomeRoute(
                    onboardingRepository,
                    mealRepository,
                    onAddFood = { navController.navigate(KyvoDestination.FoodHub.route) },
                    onMealShare = { navController.navigate(KyvoDestination.MealShare.route) { launchSingleTop = true } },
                    onProgress = { navController.navigate(KyvoDestination.Progress.route) { launchSingleTop = true } },
                )
            }
        }
        composable(KyvoDestination.Meals.route) {
            FoodHubScreen(
                onSearch = { navController.navigate(foodSearchRoute(FoodSelectionContext.NORMAL_MEAL_LOGGING)) },
                onFrequent = { navController.navigate(KyvoDestination.FoodFrequent.route) },
                onFavorites = { navController.navigate(KyvoDestination.FoodFavorites.route) },
                onFuture = { title -> navController.navigate("food/placeholder/${Uri.encode(title)}") },
                onDishes = { navController.navigate(KyvoDestination.SavedDishes.route) },
            )
        }
        composable(KyvoDestination.Progress.route) {
            if (onboardingRepository != null && mealRepository != null) {
                val progressViewModel: ProgressViewModel = viewModel(factory = ProgressViewModel.factory(onboardingRepository, mealRepository))
                ProgressRoute(
                    progressViewModel,
                    onHome = { navController.navigate(KyvoDestination.Home.route) { popUpTo(KyvoDestination.Home.route) } },
                    onMealShare = { navController.navigate(KyvoDestination.MealShare.route) { launchSingleTop = true } },
                    onCaloriesDetail = { navController.navigate(KyvoDestination.ProgressCalories.route) },
                    onProteinDetail = { navController.navigate(KyvoDestination.ProgressProtein.route) },
                    onConsistency = { navController.navigate(KyvoDestination.ProgressConsistency.route) },
                    onHistory = { navController.navigate(KyvoDestination.ProgressHistory.route) { launchSingleTop = true } },
                )
            }
        }
        composable(KyvoDestination.ProgressCalories.route) {
            if (onboardingRepository != null && mealRepository != null) {
                val detailViewModel: NutritionMetricDetailViewModel = viewModel(factory = NutritionMetricDetailViewModel.factory(onboardingRepository, mealRepository, NutritionMetric.CALORIES))
                NutritionMetricDetailRoute(detailViewModel, onBack = { navController.popBackStack(KyvoDestination.Progress.route, inclusive = false) })
            }
        }
        composable(KyvoDestination.ProgressProtein.route) {
            if (onboardingRepository != null && mealRepository != null) {
                val detailViewModel: NutritionMetricDetailViewModel = viewModel(factory = NutritionMetricDetailViewModel.factory(onboardingRepository, mealRepository, NutritionMetric.PROTEIN))
                NutritionMetricDetailRoute(detailViewModel, onBack = { navController.popBackStack(KyvoDestination.Progress.route, inclusive = false) })
            }
        }
        composable(KyvoDestination.ProgressConsistency.route) {
            if (onboardingRepository != null && mealRepository != null) {
                val consistencyViewModel: NutritionConsistencyViewModel = viewModel(factory = NutritionConsistencyViewModel.factory(onboardingRepository, mealRepository))
                NutritionConsistencyRoute(consistencyViewModel, onBack = { navController.popBackStack(KyvoDestination.Progress.route, inclusive = false) })
            }
        }
        composable(KyvoDestination.ProgressHistory.route) {
            if (onboardingRepository != null && mealRepository != null) {
                val historyViewModel: NutritionHistoryViewModel = viewModel(factory = NutritionHistoryViewModel.factory(onboardingRepository, mealRepository))
                NutritionHistoryRoute(historyViewModel, onBack = { navController.popBackStack(KyvoDestination.Progress.route, inclusive = false) }, onDayDetail = { date -> navController.navigate(dayDetailRoute(date)) { launchSingleTop = true } })
            }
        }
        composable(KyvoDestination.ProgressDay.route) { entry ->
            if (onboardingRepository != null && mealRepository != null) {
                val date = runCatching { LocalDate.parse(entry.arguments?.getString("date").orEmpty()) }.getOrNull()
                if (date == null) {
                    ProgressPlaceholder("Fecha no válida", onBack = { navController.popBackStack(KyvoDestination.ProgressHistory.route, inclusive = false) })
                } else {
                    val dayViewModel: NutritionDayDetailViewModel = viewModel(factory = NutritionDayDetailViewModel.factory(onboardingRepository, mealRepository, date))
                    NutritionDayDetailRoute(dayViewModel, onBack = { navController.popBackStack(KyvoDestination.ProgressHistory.route, inclusive = false) })
                }
            }
        }
        composable(KyvoDestination.Profile.route) {
            if (authState is AuthState.SignedIn && onboardingRepository != null) {
                AthleteProfileRoute(
                    session = authState.session,
                    repository = onboardingRepository,
                    onEditProfile = { navController.navigate(KyvoDestination.ProfileEdit.route) },
                    onNutritionPlan = { navController.navigate(KyvoDestination.ProfileNutritionPlan.route) },
                    onUpdateGoal = { navController.navigate(KyvoDestination.ProfileUpdateGoal.route) },
                    onRecalculateGoals = { navController.navigate(KyvoDestination.ProfileUpdateGoal.route) },
                    onPersonalPreferences = { navController.navigate(KyvoDestination.PersonalPreferences.route) },
                )
            } else {
                TopLevelPlaceholder("Perfil", "Completa tu sesión para ver tu perfil de atleta.")
            }
        }
        composable(KyvoDestination.ProfileEdit.route) {
            if (authState is AuthState.SignedIn) {
                EditAthleteProfileRoute(
                    session = authState.session,
                    authRepository = authRepository,
                    onBack = { navController.popBackStack() },
                )
            } else {
                TopLevelPlaceholder("Editar perfil", "Completa tu sesión para editar tu perfil.") { navController.popBackStack() }
            }
        }
        composable(KyvoDestination.ProfileNutritionPlan.route) {
            onboardingRepository?.let { repository ->
                NutritionPlanRoute(
                    repository = repository,
                    onBack = { navController.popBackStack() },
                    onHowCalculated = { navController.navigate(KyvoDestination.ProfileCalculation.route) },
                    onRecalculate = { navController.navigate(KyvoDestination.ProfileUpdateGoal.route) },
                )
            }
        }
        composable(KyvoDestination.ProfileCalculation.route) {
            onboardingRepository?.let { repository ->
                NutritionCalculationRoute(repository = repository, onBack = { navController.popBackStack() })
            }
        }
        composable(KyvoDestination.ProfileUpdateGoal.route) {
            recalculateViewModel?.let { flowViewModel ->
                UpdateGoalRoute(
                    viewModel = flowViewModel,
                    onBack = { navController.popBackStack() },
                    onContinue = { navController.navigate(KyvoDestination.ProfileRecalculateGoals.route) },
                )
            }
        }
        composable(KyvoDestination.ProfileRecalculateGoals.route) {
            recalculateViewModel?.let { flowViewModel ->
                RecalculateGoalsRoute(
                    viewModel = flowViewModel,
                    onBack = { navController.popBackStack() },
                    onPreview = { navController.navigate(KyvoDestination.ProfileNewGoals.route) { launchSingleTop = true } },
                )
            }
        }
        composable(KyvoDestination.ProfileNewGoals.route) {
            recalculateViewModel?.let { flowViewModel ->
                NewGoalsRoute(
                    viewModel = flowViewModel,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack(KyvoDestination.ProfileNutritionPlan.route, inclusive = false) },
                )
            }
        }
        composable(KyvoDestination.PersonalPreferences.route) {
            personalPreferencesViewModel?.let { flowViewModel ->
                PersonalPreferencesHubRoute(
                    viewModel = flowViewModel,
                    onBack = { navController.popBackStack() },
                    onPersonalData = { navController.navigate(KyvoDestination.PersonalData.route) },
                    onActivity = { navController.navigate(KyvoDestination.ActivityTraining.route) },
                    onFuture = { title ->
                        val destination = when (title) {
                            "Nivel de experiencia" -> KyvoDestination.Experience
                            "Preferencias alimenticias" -> KyvoDestination.FoodPreferences
                            "Organización de comidas" -> KyvoDestination.MealOrganization
                            else -> KyvoDestination.ReviewGoalUpdates
                        }
                        navController.navigate(destination.route)
                    },
                )
            }
        }
        composable(KyvoDestination.PersonalData.route) {
            personalPreferencesViewModel?.let { flowViewModel ->
                PersonalDataRoute(flowViewModel, onBack = { navController.popBackStack() }, onSavedStep = { navController.popBackStack() })
            }
        }
        composable(KyvoDestination.ActivityTraining.route) {
            personalPreferencesViewModel?.let { flowViewModel ->
                ActivityTrainingRoute(flowViewModel, onBack = { navController.popBackStack() }, onSavedStep = { navController.navigate(KyvoDestination.Experience.route) })
            }
        }
        composable(KyvoDestination.Experience.route) {
            personalPreferencesViewModel?.let { flowViewModel ->
                ExperienceRoute(flowViewModel, onBack = { navController.popBackStack() }, onContinue = { navController.navigate(KyvoDestination.FoodPreferences.route) })
            }
        }
        composable(KyvoDestination.FoodPreferences.route) {
            personalPreferencesViewModel?.let { flowViewModel ->
                FoodPreferencesRoute(flowViewModel, onBack = { navController.popBackStack() }, onContinue = { navController.navigate(KyvoDestination.MealOrganization.route) })
            }
        }
        composable(KyvoDestination.MealOrganization.route) {
            personalPreferencesViewModel?.let { flowViewModel ->
                MealOrganizationRoute(flowViewModel, onBack = { navController.popBackStack() }, onContinue = { navController.navigate(KyvoDestination.ReviewGoalUpdates.route) })
            }
        }
        composable(KyvoDestination.ReviewGoalUpdates.route) { TopLevelPlaceholder("Revisar actualización de metas", "Esta pantalla estará disponible en el siguiente checkpoint.") { navController.popBackStack() } }
        composable(KyvoDestination.FoodHub.route) {
            FoodHubScreen(
                onSearch = { navController.navigate(foodSearchRoute(FoodSelectionContext.NORMAL_MEAL_LOGGING)) },
                onFrequent = { navController.navigate(KyvoDestination.FoodFrequent.route) },
                onFavorites = { navController.navigate(KyvoDestination.FoodFavorites.route) },
                onFuture = { title -> navController.navigate("food/placeholder/${Uri.encode(title)}") },
                onDishes = { navController.navigate(KyvoDestination.SavedDishes.route) },
            )
        }
        composable(KyvoDestination.FoodFrequent.route) {
            foodRepository?.let { repository ->
                FoodFrequentRoute(repository, onBack = { navController.popBackStack() }, onSelect = { result -> navController.navigate(foodDetailRoute(result.id, result.type.name, FoodSelectionContext.NORMAL_MEAL_LOGGING)) }, onPortion = { result -> navController.navigate(foodPortionRoute(result.id, result.type.name, FoodSelectionContext.NORMAL_MEAL_LOGGING)) })
            }
        }
        composable(KyvoDestination.FoodFavorites.route) {
            foodRepository?.let { repository ->
                FoodFavoritesRoute(repository, onBack = { navController.popBackStack() }, onSelect = { result -> navController.navigate(foodDetailRoute(result.id, result.type.name, FoodSelectionContext.NORMAL_MEAL_LOGGING)) }, onPortion = { result -> navController.navigate(foodPortionRoute(result.id, result.type.name, FoodSelectionContext.NORMAL_MEAL_LOGGING)) })
            }
        }
        composable(KyvoDestination.FoodSearch.route) { entry ->
            foodRepository?.let { repository ->
                val context = foodSelectionContext(entry)
                FoodSearchRoute(repository, onBack = { navController.popBackStack() }, onResults = { query -> navController.navigate(foodResultsRoute(query, context)) })
            }
        }
        composable(KyvoDestination.FoodResults.route) { entry ->
            foodRepository?.let { repository ->
                val query = Uri.decode(entry.arguments?.getString("query").orEmpty())
                val context = foodSelectionContext(entry)
                FoodResultsRoute(query, repository, onBack = { navController.popBackStack() }, onSelect = { result -> navController.navigate(foodDetailRoute(result.id, result.type.name, context)) }, onFuture = {})
            }
        }
        composable(KyvoDestination.FoodDetail.route) { entry ->
            if (foodRepository != null) {
                val id = Uri.decode(entry.arguments?.getString("id").orEmpty())
                val type = entry.arguments?.getString("type").orEmpty()
                val context = foodSelectionContext(entry)
                FoodDetailRoute(id, type, foodRepository, onBack = { navController.popBackStack() }, onPortion = { navController.navigate(foodPortionRoute(it.id, it.type.name, context)) })
            }
        }
        composable(KyvoDestination.FoodPortion.route) { entry ->
            if (foodRepository != null && mealRepository != null) {
                val id = Uri.decode(entry.arguments?.getString("id").orEmpty())
                val type = entry.arguments?.getString("type").orEmpty()
                val context = foodSelectionContext(entry)
                FoodPortionRoute(
                    id,
                    type,
                    foodRepository,
                    mealRepository,
                    onBack = { navController.popBackStack() },
                    onRegistered = { navController.popBackStack(KyvoDestination.Home.route, inclusive = false) },
                    selectionContext = context,
                    onMealSharePortionConfirmed = { item ->
                        val builderEntry = navController.getBackStackEntry(KyvoDestination.MealShareMealBuilder.route)
                        builderEntry.savedStateHandle[MEAL_SHARE_PENDING_ITEM] = Bundle().apply {
                            putString("id", item.id)
                            putString("name", item.name)
                            putDouble("quantity", item.quantity)
                            putString("unit", item.unit)
                            putInt("calories", item.calories)
                            putInt("protein", item.protein)
                            putInt("carbohydrates", item.carbohydrates)
                            putInt("fat", item.fat)
                            item.image?.let { putString("image", it) }
                            item.fiber?.let { putDouble("fiber", it) }
                            item.sugar?.let { putDouble("sugar", it) }
                            item.sodiumMg?.let { putDouble("sodiumMg", it) }
                            putDouble("grams", item.grams ?: item.quantity)
                            putString("foodId", item.foodId)
                            putString("foodType", item.foodType?.name)
                        }
                        navController.popBackStack(KyvoDestination.MealShareMealBuilder.route, inclusive = false)
                    },
                )
            }
        }
        composable(KyvoDestination.FoodPlaceholder.route) { entry ->
            FoodStatePlaceholder(Uri.decode(entry.arguments?.getString("name").orEmpty()), onBack = { navController.popBackStack() })
        }
        composable(KyvoDestination.MealShare.route) {
            MealShareSourceScreen(
                onCamera = { navController.navigate(KyvoDestination.MealShareCamera.route) },
                onGallery = { navController.navigate(KyvoDestination.MealSharePhotoPicker.route) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(KyvoDestination.MealShareCamera.route) { entry ->
            val mealShareEntry = remember(navController, entry) { navController.getBackStackEntry(KyvoDestination.MealShare.route) }
            val draft: MealShareDraftViewModel = viewModel(mealShareEntry)
            var cameraGranted by remember { mutableStateOf(false) }
            if (cameraGranted) {
                CameraScreen(onBack = { navController.popBackStack() }, onPhotoCaptured = { draft.setCapturedPhoto(it); navController.navigate(KyvoDestination.MealSharePhotoPreview.route) })
            } else {
                CameraPermissionGate(onGranted = { cameraGranted = true }, onBack = { navController.popBackStack() })
            }
        }
        composable(KyvoDestination.MealSharePhotoPicker.route) { entry ->
            val mealShareEntry = remember(navController, entry) { navController.getBackStackEntry(KyvoDestination.MealShare.route) }
            val draft: MealShareDraftViewModel = viewModel(mealShareEntry)
            MealSharePhotoPickerRoute(onSelected = { draft.setGalleryPhoto(it); navController.navigate(KyvoDestination.MealSharePhotoPreview.route) }, onBack = { navController.popBackStack() })
        }
        composable(KyvoDestination.MealSharePhotoPreview.route) { entry ->
            val mealShareEntry = remember(navController, entry) { navController.getBackStackEntry(KyvoDestination.MealShare.route) }
            val draft: MealShareDraftViewModel = viewModel(mealShareEntry)
            MealSharePhotoPreviewScreen(
                photoUri = draft.draft.collectAsState().value.photoUri,
                onConfirm = { navController.navigate(KyvoDestination.MealShareMealBuilder.route) },
                onRepeat = { navController.navigate(KyvoDestination.MealShareCamera.route) },
                onChange = { navController.navigate(KyvoDestination.MealSharePhotoPicker.route) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(KyvoDestination.MealShareMealBuilder.route) { entry ->
            val mealShareEntry = remember(navController, entry) { navController.getBackStackEntry(KyvoDestination.MealShare.route) }
            val draft: MealShareDraftViewModel = viewModel(mealShareEntry)
            val pendingItem by entry.savedStateHandle
                .getStateFlow<Bundle?>(MEAL_SHARE_PENDING_ITEM, null)
                .collectAsState()
            LaunchedEffect(pendingItem) {
                pendingItem?.toMealItem()?.let(draft::addMealItemIfAbsent)
                if (pendingItem != null) {
                    entry.savedStateHandle[MEAL_SHARE_PENDING_ITEM] = null
                }
            }
            MealShareMealBuilderScreen(
                draft = draft.draft.collectAsState().value,
                onMealTypeSelected = draft::setMealType,
                onAddFood = { navController.navigate(foodSearchRoute(FoodSelectionContext.MEAL_SHARE)) },
                onAddManualFood = draft::addMealItem,
                onUpdateManualFood = draft::updateMealItem,
                onUpdatePortion = draft::updateMealItemPortion,
                onRemoveFood = draft::removeMealItem,
                onContinue = { navController.navigate(KyvoDestination.MealShareEditor.route) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(KyvoDestination.MealShareFoodSearchPlaceholder.route) {
            FoodStatePlaceholder("Búsqueda de alimentos · siguiente checkpoint", onBack = { navController.popBackStack() })
        }
        composable(KyvoDestination.MealShareEditor.route) { entry ->
            val mealShareEntry = remember(navController, entry) { navController.getBackStackEntry(KyvoDestination.MealShare.route) }
            val draft: MealShareDraftViewModel = viewModel(mealShareEntry)
            val context = LocalContext.current
            val renderer = remember(context.applicationContext) { AndroidMealShareImageRenderer(context.applicationContext) }
            val render: MealShareRenderViewModel = viewModel(mealShareEntry, factory = MealShareRenderViewModel.factory(renderer))
            val currentDraft = draft.draft.collectAsState().value
            val finalization = draft.finalization.collectAsState().value
            val shareLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { draft.onShareReturned() }
            MealShareEditorScreen(
                draft = currentDraft,
                onTemplateSelected = draft::setTemplate,
                renderState = render.state.collectAsState().value,
                onRender = render::render,
                onDraftChanged = render::invalidateIfStale,
                onRendered = draft::setRenderedResult,
                finalizationState = finalization,
                onConfirmFinalization = { mealRepository?.let(draft::confirmFinalMeal) },
                canFinalize = mealRepository != null,
                shareState = draft.share.collectAsState().value,
                onShare = {
                    draft.prepareShare(File(context.cacheDir, "meal_share/rendered"))?.let { file ->
                        runCatching {
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                            shareLauncher.launch(android.content.Intent.createChooser(buildMealShareIntent(uri), "Compartir Meal Share"))
                        }.onFailure { error -> draft.onShareLaunchError(error.message ?: "No pudimos abrir el selector para compartir.") }
                    }
                },
                onFinish = {
                    draft.clearMealShareSession(context.cacheDir)
                    render.clearSession()
                    navController.popBackStack(KyvoDestination.Home.route, inclusive = false)
                },
                onBack = { if (finalization !is com.kyvo.app.feature.mealshare.presentation.MealShareFinalizationState.Persisted) navController.popBackStack() },
            )
        }
        composable(KyvoDestination.SavedDishes.route) {
            savedDishRepository?.let { repository ->
                SavedDishesRoute(repository, onBack = { navController.popBackStack() }, onCreate = { navController.navigate("dishes/editor/new") }, onDetail = { id -> navController.navigate("dishes/detail/${Uri.encode(id)}") })
            }
        }
        composable(KyvoDestination.SavedDishEditor.route) { entry ->
            if (savedDishRepository != null && foodRepository != null) {
                val id = Uri.decode(entry.arguments?.getString("id").orEmpty()).takeUnless { it == "new" }
                SavedDishEditorRoute(id, savedDishRepository, foodRepository, onBack = { navController.popBackStack() }, onSaved = { savedId -> navController.navigate("dishes/detail/${Uri.encode(savedId)}") { popUpTo(KyvoDestination.SavedDishes.route) { inclusive = false } } })
            }
        }
        composable(KyvoDestination.SavedDishDetail.route) { entry ->
            savedDishRepository?.let { repository ->
                val id = Uri.decode(entry.arguments?.getString("id").orEmpty())
                SavedDishDetailRoute(id, repository, onBack = { navController.popBackStack() }, onEdit = { dishId -> navController.navigate("dishes/editor/${Uri.encode(dishId)}") }, onAddToDay = { dishId -> navController.navigate("dishes/add/${Uri.encode(dishId)}") })
            }
        }
        composable(KyvoDestination.SavedDishAddToDay.route) { entry ->
            if (savedDishRepository != null && mealRepository != null) {
                val id = Uri.decode(entry.arguments?.getString("id").orEmpty())
                AddSavedDishToDayRoute(id, savedDishRepository, mealRepository, onBack = { navController.popBackStack() }, onAdded = { navController.navigate(KyvoDestination.Home.route) { popUpTo(KyvoDestination.Home.route) { inclusive = false } } })
            }
        }
    }
    if (showBottomNavigation) {
        Box(Modifier.align(Alignment.BottomCenter)) {
            KyvoBottomNavigation(
                selected = kyvoBottomDestinationFor(currentDestination),
                onDestinationSelected = { destination ->
                    navController.navigate(destination.route) {
                        if (destination != KyvoBottomDestination.MealShare) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            restoreState = true
                        }
                        launchSingleTop = true
                    }
                },
            )
        }
    }
    }
}

@Composable
private fun TopLevelPlaceholder(title: String, message: String, onBack: (() -> Unit)? = null) = Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
        androidx.compose.material3.Text(title, style = androidx.compose.material3.MaterialTheme.typography.headlineSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        androidx.compose.material3.Text(message, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        onBack?.let { callback ->
            androidx.compose.material3.TextButton(onClick = callback, modifier = Modifier.padding(top = 12.dp)) { androidx.compose.material3.Text("Volver") }
        }
    }
}

private fun foodSelectionContext(entry: androidx.navigation.NavBackStackEntry): FoodSelectionContext =
    entry.arguments?.getString("selectionContext")?.let { value -> runCatching { FoodSelectionContext.valueOf(value) }.getOrNull() }
        ?: FoodSelectionContext.NORMAL_MEAL_LOGGING

private const val MEAL_SHARE_PENDING_ITEM = "meal_share_pending_item"

private fun Bundle.toMealItem(): com.kyvo.app.feature.home.domain.model.MealItem? {
    val id = getString("id") ?: return null
    val name = getString("name") ?: return null
    val unit = getString("unit") ?: return null
    val quantity = getDouble("quantity", 0.0)
    if (quantity <= 0.0) return null
    return com.kyvo.app.feature.home.domain.model.MealItem(
        id = id,
        name = name,
        quantity = quantity,
        unit = unit,
        calories = getInt("calories", 0),
        protein = getInt("protein", 0),
        carbohydrates = getInt("carbohydrates", 0),
        fat = getInt("fat", 0),
        image = getString("image"),
        fiber = getNullableDouble("fiber"),
        sugar = getNullableDouble("sugar"),
        sodiumMg = getNullableDouble("sodiumMg"),
        grams = getDouble("grams", quantity),
        foodId = getString("foodId"),
        foodType = getString("foodType")?.let { value -> runCatching { FoodType.valueOf(value) }.getOrNull() },
    )
}

private fun Bundle.getNullableDouble(key: String): Double? = if (containsKey(key)) getDouble(key) else null

internal fun foodSearchRoute(context: FoodSelectionContext) = "food/search?selectionContext=${context.name}"
internal fun dayDetailRoute(date: LocalDate) = "progress/day/$date"
internal fun foodResultsRoute(query: String, context: FoodSelectionContext) = "food/results/${Uri.encode(query)}?selectionContext=${context.name}"
internal fun foodDetailRoute(id: String, type: String, context: FoodSelectionContext) = "food/detail/${Uri.encode(id)}/${type}?selectionContext=${context.name}"
internal fun foodPortionRoute(id: String, type: String, context: FoodSelectionContext) = "food/portion/${Uri.encode(id)}/${type}?selectionContext=${context.name}"

internal fun resolveStartDestination(
    authState: AuthState,
    onboardingCompleted: Boolean,
): KyvoDestination = when {
    authState is AuthState.SignedOut -> KyvoDestination.Login
    authState is AuthState.SignedIn && onboardingCompleted -> KyvoDestination.Home
    else -> KyvoDestination.Onboarding
}
