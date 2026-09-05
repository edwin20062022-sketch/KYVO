package com.kyvo.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kyvo.app.feature.foundation.presentation.FoundationRoute

@Composable
fun KyvoNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = KyvoDestination.Foundation.route,
    ) {
        composable(KyvoDestination.Foundation.route) {
            FoundationRoute()
        }
        // Feature destinations are registered only when their phase is approved.
    }
}

