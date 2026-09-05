package com.kyvo.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kyvo.app.domain.auth.AuthRepository
import com.kyvo.app.feature.auth.presentation.LoginRoute
import com.kyvo.app.feature.auth.presentation.OnboardingPendingScreen

@Composable
fun KyvoNavHost(
    navController: NavHostController,
    authRepository: AuthRepository,
) {
    NavHost(
        navController = navController,
        startDestination = KyvoDestination.Login.route,
    ) {
        composable(KyvoDestination.Login.route) {
            LoginRoute(
                authRepository = authRepository,
                onAuthenticated = {
                    navController.navigate(KyvoDestination.Onboarding.route) {
                        popUpTo(KyvoDestination.Login.route) { inclusive = true }
                    }
                },
            )
        }
        composable(KyvoDestination.Onboarding.route) {
            OnboardingPendingScreen()
        }
        // The real onboarding is intentionally deferred until Phase 2 approval.
    }
}

