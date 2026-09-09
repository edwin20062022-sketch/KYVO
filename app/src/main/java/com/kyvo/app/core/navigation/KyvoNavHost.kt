package com.kyvo.app.core.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kyvo.app.domain.auth.AuthRepository
import com.kyvo.app.domain.auth.AuthState
import com.kyvo.app.feature.auth.presentation.LoginRoute
import com.kyvo.app.feature.onboarding.domain.repository.OnboardingRepository
import com.kyvo.app.feature.home.domain.repository.MealRepository
import com.kyvo.app.feature.home.presentation.HomeRoute
import com.kyvo.app.feature.food.domain.repository.FoodRepository
import com.kyvo.app.feature.food.presentation.FoodDetailRoute
import com.kyvo.app.feature.food.presentation.FoodFavoritesRoute
import com.kyvo.app.feature.food.presentation.FoodFrequentRoute
import com.kyvo.app.feature.food.presentation.FoodHubScreen
import com.kyvo.app.feature.food.presentation.FoodPortionRoute
import com.kyvo.app.feature.food.presentation.FoodResultsRoute
import com.kyvo.app.feature.food.presentation.FoodSearchRoute
import com.kyvo.app.feature.food.presentation.FoodStatePlaceholder
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyvo.app.feature.onboarding.presentation.OnboardingRoute
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
    LaunchedEffect(target) {
        if (navController.currentDestination?.route != target.route) {
            navController.navigate(target.route) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
    NavHost(navController = navController, startDestination = target.route) {
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
                HomeRoute(onboardingRepository, mealRepository, onAddFood = { navController.navigate(KyvoDestination.FoodHub.route) })
            }
        }
        composable(KyvoDestination.FoodHub.route) {
            FoodHubScreen(
                onSearch = { navController.navigate(KyvoDestination.FoodSearch.route) },
                onFrequent = { navController.navigate(KyvoDestination.FoodFrequent.route) },
                onFavorites = { navController.navigate(KyvoDestination.FoodFavorites.route) },
                onFuture = { title -> navController.navigate("food/placeholder/${Uri.encode(title)}") },
                onDishes = { navController.navigate(KyvoDestination.SavedDishes.route) },
            )
        }
        composable(KyvoDestination.FoodFrequent.route) {
            foodRepository?.let { repository ->
                FoodFrequentRoute(repository, onBack = { navController.popBackStack() }, onSelect = { result -> navController.navigate("food/detail/${Uri.encode(result.id)}/${result.type.name}") }, onPortion = { result -> navController.navigate("food/portion/${Uri.encode(result.id)}/${result.type.name}") })
            }
        }
        composable(KyvoDestination.FoodFavorites.route) {
            foodRepository?.let { repository ->
                FoodFavoritesRoute(repository, onBack = { navController.popBackStack() }, onSelect = { result -> navController.navigate("food/detail/${Uri.encode(result.id)}/${result.type.name}") }, onPortion = { result -> navController.navigate("food/portion/${Uri.encode(result.id)}/${result.type.name}") })
            }
        }
        composable(KyvoDestination.FoodSearch.route) {
            foodRepository?.let { repository ->
                FoodSearchRoute(repository, onBack = { navController.popBackStack() }, onResults = { query -> navController.navigate("food/results/${Uri.encode(query)}") })
            }
        }
        composable(KyvoDestination.FoodResults.route) { entry ->
            foodRepository?.let { repository ->
                val query = Uri.decode(entry.arguments?.getString("query").orEmpty())
                FoodResultsRoute(query, repository, onBack = { navController.popBackStack() }, onSelect = { result -> navController.navigate("food/detail/${Uri.encode(result.id)}/${result.type.name}") }, onFuture = {})
            }
        }
        composable(KyvoDestination.FoodDetail.route) { entry ->
            if (foodRepository != null) {
                val id = Uri.decode(entry.arguments?.getString("id").orEmpty())
                val type = entry.arguments?.getString("type").orEmpty()
                FoodDetailRoute(id, type, foodRepository, onBack = { navController.popBackStack() }, onPortion = { navController.navigate("food/portion/${Uri.encode(it.id)}/${it.type.name}") })
            }
        }
        composable(KyvoDestination.FoodPortion.route) { entry ->
            if (foodRepository != null && mealRepository != null) {
                val id = Uri.decode(entry.arguments?.getString("id").orEmpty())
                val type = entry.arguments?.getString("type").orEmpty()
                FoodPortionRoute(id, type, foodRepository, mealRepository, onBack = { navController.popBackStack() }, onRegistered = { navController.popBackStack(KyvoDestination.Home.route, inclusive = false) })
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
                onConfirm = { navController.popBackStack(KyvoDestination.MealShare.route, inclusive = false) },
                onRepeat = { navController.navigate(KyvoDestination.MealShareCamera.route) },
                onChange = { navController.navigate(KyvoDestination.MealSharePhotoPicker.route) },
                onBack = { navController.popBackStack() },
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
}

internal fun resolveStartDestination(
    authState: AuthState,
    onboardingCompleted: Boolean,
): KyvoDestination = when {
    authState is AuthState.SignedOut -> KyvoDestination.Login
    authState is AuthState.SignedIn && onboardingCompleted -> KyvoDestination.Home
    else -> KyvoDestination.Onboarding
}
