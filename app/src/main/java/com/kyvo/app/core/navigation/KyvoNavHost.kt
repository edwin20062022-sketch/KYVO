package com.kyvo.app.core.navigation

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
import com.kyvo.app.feature.onboarding.presentation.HomePendingScreen
import com.kyvo.app.feature.onboarding.presentation.OnboardingRoute
import kotlinx.coroutines.launch

@Composable
fun KyvoNavHost(
    navController: NavHostController,
    authRepository: AuthRepository,
    onboardingRepository: OnboardingRepository?,
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
            HomePendingScreen(onSignOut = { scope.launch { authRepository.signOut() } })
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
