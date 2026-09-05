package com.kyvo.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.kyvo.app.core.designsystem.KyvoTheme
import com.kyvo.app.core.navigation.KyvoNavHost
import com.kyvo.app.data.auth.PendingAuthRepository
import com.kyvo.app.feature.onboarding.data.DataStoreOnboardingRepository

@Composable
fun KyvoApp() {
    val context = LocalContext.current
    val authRepository = remember { PendingAuthRepository() }
    val onboardingRepository = remember(context) { DataStoreOnboardingRepository(context) }
    KyvoTheme {
        KyvoNavHost(
            navController = rememberNavController(),
            authRepository = authRepository,
            onboardingRepository = onboardingRepository,
        )
    }
}
