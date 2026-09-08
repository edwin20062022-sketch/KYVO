package com.kyvo.app.core.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.kyvo.app.feature.food.presentation.FoodHubScreen
import com.kyvo.app.feature.food.presentation.FoodPortionRoute
import com.kyvo.app.feature.food.presentation.FoodResultsRoute
import com.kyvo.app.feature.food.presentation.FoodSearchRoute
import com.kyvo.app.feature.food.presentation.FoodStatePlaceholder
import com.kyvo.app.feature.onboarding.presentation.OnboardingRoute
import kotlinx.coroutines.launch

@Composable
fun KyvoNavHost(
    navController: NavHostController,
    authRepository: AuthRepository,
    onboardingRepository: OnboardingRepository?,
    mealRepository: MealRepository?,
    foodRepository: FoodRepository?,
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
                onFuture = { title -> navController.navigate("food/placeholder/${Uri.encode(title)}") },
            )
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
