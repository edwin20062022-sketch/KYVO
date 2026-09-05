package com.kyvo.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kyvo.app.domain.auth.AuthRepository
import com.kyvo.app.feature.auth.presentation.LoginRoute
import com.kyvo.app.feature.onboarding.domain.repository.OnboardingRepository
import com.kyvo.app.feature.onboarding.presentation.HomePendingScreen
import com.kyvo.app.feature.onboarding.presentation.OnboardingRoute

@Composable
fun KyvoNavHost(
    navController: NavHostController,
    authRepository: AuthRepository,
    onboardingRepository: OnboardingRepository,
) {
    NavHost(navController = navController, startDestination = KyvoDestination.Login.route) {
        composable(KyvoDestination.Login.route) {
            LoginRoute(
                authRepository = authRepository,
                onAuthenticated = { navController.navigate(KyvoDestination.Onboarding.route) },
            )
        }
        composable(KyvoDestination.Onboarding.route) {
            OnboardingRoute(
                repository = onboardingRepository,
                onExit = { navController.popBackStack() },
                onCompleted = {
                    navController.navigate(KyvoDestination.Home.route) {
                        popUpTo(KyvoDestination.Onboarding.route) { inclusive = true }
                    }
                },
            )
        }
        composable(KyvoDestination.Home.route) { HomePendingScreen() }
    }
}
