package com.kyvo.app

import android.os.Bundle
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    private var authIntent by mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            splashScreenView.iconView.animate()
                .alpha(0f)
                .scaleX(1.04f)
                .scaleY(1.04f)
                .setDuration(SPLASH_EXIT_DURATION_MS)
                .start()
            splashScreenView.view.animate()
                .alpha(0f)
                .setDuration(SPLASH_EXIT_DURATION_MS)
                .withEndAction { splashScreenView.remove() }
                .start()
        }
        authIntent = intent
        enableEdgeToEdge()
        setContent { KyvoApp(authIntent = authIntent) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        authIntent = intent
    }

    private companion object {
        const val SPLASH_EXIT_DURATION_MS = 240L
    }
}
