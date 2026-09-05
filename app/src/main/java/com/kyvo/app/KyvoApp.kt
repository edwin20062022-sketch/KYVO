package com.kyvo.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.kyvo.app.core.designsystem.KyvoTheme
import com.kyvo.app.core.navigation.KyvoNavHost
import com.kyvo.app.data.auth.PendingAuthRepository

@Composable
fun KyvoApp() {
    val authRepository = remember { PendingAuthRepository() }
    KyvoTheme {
        KyvoNavHost(
            navController = rememberNavController(),
            authRepository = authRepository,
        )
    }
}

