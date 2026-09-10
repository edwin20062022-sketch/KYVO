package com.kyvo.app

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.kyvo.app.core.designsystem.KyvoTheme
import com.kyvo.app.core.navigation.KyvoNavHost
import com.kyvo.app.feature.launch.presentation.isLaunchDestinationReady
import com.kyvo.app.data.auth.ConfigurationRequiredAuthRepository
import com.kyvo.app.data.auth.CredentialManagerGoogleGateway
import com.kyvo.app.data.auth.SupabaseAuthRepository
import com.kyvo.app.data.auth.SupabaseClientFactory
import com.kyvo.app.data.auth.SupabaseSdkAuthDataSource
import com.kyvo.app.feature.home.data.SupabaseMealRepository
import com.kyvo.app.feature.dishes.data.SupabaseSavedDishRepository
import com.kyvo.app.feature.food.data.CatalogFirstFoodRepository
import com.kyvo.app.feature.food.data.SupabaseFoodRepository
import com.kyvo.app.feature.food.data.SupabaseFoodSearchEdgeGateway
import com.kyvo.app.feature.food.domain.repository.FoodRepository
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
fun KyvoApp(
    authIntent: Intent? = null,
    onStartupReady: () -> Unit = {},
) {
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
    val foodRepository: FoodRepository? = remember(authDependencies.client) {
        authDependencies.client?.let { client ->
            CatalogFirstFoodRepository(SupabaseFoodRepository(client), SupabaseFoodSearchEdgeGateway(client))
        }
    }
    val authState by authRepository.observeSession().collectAsStateWithLifecycle(AuthState.Initializing)
    LaunchedEffect(authIntent, authDependencies.client) {
        if (authIntent != null) authDependencies.client?.handleDeeplinks(authIntent)
    }
    KyvoTheme {
        val navController = rememberNavController()
        when (val state = authState) {
            AuthState.Initializing -> Box(Modifier.fillMaxSize())
            AuthState.SignedOut -> {
                LaunchedEffect(Unit) { onStartupReady() }
                KyvoNavHost(
                    navController = navController,
                    authRepository = authRepository,
                    onboardingRepository = null,
                    mealRepository = null,
                    foodRepository = null,
                    savedDishRepository = null,
                    authState = state,
                    onboardingCompleted = false,
                )
            }
            is AuthState.SignedIn -> {
                val onboardingRepository = remember(context, state.session.userId) {
                    DataStoreOnboardingRepository(context, state.session.userId)
                }
                val mealRepository = remember(state.session.userId, authDependencies.client) { SupabaseMealRepository(requireNotNull(authDependencies.client)) }
                val savedDishRepository = remember(state.session.userId, authDependencies.client) { SupabaseSavedDishRepository(requireNotNull(authDependencies.client)) }
                val onboarding by onboardingRepository.observe()
                    .collectAsStateWithLifecycle(initialValue = null)
                if (isLaunchDestinationReady(state, onboardingLoaded = onboarding != null)) {
                    LaunchedEffect(state.session.userId) { onStartupReady() }
                    KyvoNavHost(
                        navController = navController,
                        authRepository = authRepository,
                        onboardingRepository = onboardingRepository,
                        mealRepository = mealRepository,
                        foodRepository = foodRepository,
                        savedDishRepository = savedDishRepository,
                        authState = state,
                        onboardingCompleted = onboarding?.isCompleted == true,
                    )
                } else Box(Modifier.fillMaxSize())
            }
        }
    }
}
