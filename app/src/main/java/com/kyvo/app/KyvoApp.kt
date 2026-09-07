package com.kyvo.app

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.kyvo.app.core.designsystem.KyvoTheme
import com.kyvo.app.core.navigation.KyvoNavHost
import com.kyvo.app.data.auth.ConfigurationRequiredAuthRepository
import com.kyvo.app.data.auth.CredentialManagerGoogleGateway
import com.kyvo.app.data.auth.SupabaseAuthRepository
import com.kyvo.app.data.auth.SupabaseClientFactory
import com.kyvo.app.data.auth.SupabaseSdkAuthDataSource
import com.kyvo.app.domain.auth.AuthRepository
import com.kyvo.app.domain.auth.AuthState
import com.kyvo.app.feature.onboarding.data.DataStoreOnboardingRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks

private data class AuthDependencies(
    val repository: AuthRepository,
    val client: SupabaseClient? = null,
)

@Composable
fun KyvoApp(authIntent: Intent? = null) {
    val context = LocalContext.current
    val authDependencies = remember(context) {
        if (BuildConfig.SUPABASE_URL.isBlank() || BuildConfig.SUPABASE_PUBLISHABLE_KEY.isBlank()) {
            AuthDependencies(ConfigurationRequiredAuthRepository())
        } else {
            val client = SupabaseClientFactory.create(
                url = BuildConfig.SUPABASE_URL,
                publishableKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY,
                authScheme = BuildConfig.AUTH_SCHEME,
            )
            AuthDependencies(
                repository = SupabaseAuthRepository(
                    dataSource = SupabaseSdkAuthDataSource(client),
                    googleCredentials = CredentialManagerGoogleGateway(
                        context = context,
                        serverClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID,
                    ),
                ),
                client = client,
            )
        }
    }
    val authRepository = authDependencies.repository
    val authState by authRepository.observeSession().collectAsStateWithLifecycle(AuthState.Initializing)
    LaunchedEffect(authIntent, authDependencies.client) {
        if (authIntent != null) authDependencies.client?.handleDeeplinks(authIntent)
    }
    KyvoTheme {
        val navController = rememberNavController()
        when (val state = authState) {
            AuthState.Initializing -> LoadingScreen()
            AuthState.SignedOut -> KyvoNavHost(
                navController = navController,
                authRepository = authRepository,
                onboardingRepository = null,
                authState = state,
                onboardingCompleted = false,
            )
            is AuthState.SignedIn -> {
                val onboardingRepository = remember(context, state.session.userId) {
                    DataStoreOnboardingRepository(context, state.session.userId)
                }
                val onboarding by onboardingRepository.observe()
                    .collectAsStateWithLifecycle(initialValue = null)
                if (onboarding == null) {
                    LoadingScreen()
                } else {
                    KyvoNavHost(
                        navController = navController,
                        authRepository = authRepository,
                        onboardingRepository = onboardingRepository,
                        authState = state,
                        onboardingCompleted = onboarding?.isCompleted == true,
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
