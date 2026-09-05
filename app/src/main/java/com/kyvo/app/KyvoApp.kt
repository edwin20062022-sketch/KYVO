package com.kyvo.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.kyvo.app.core.designsystem.KyvoTheme
import com.kyvo.app.core.navigation.KyvoNavHost

@Composable
fun KyvoApp() {
    KyvoTheme {
        KyvoNavHost(navController = rememberNavController())
    }
}

