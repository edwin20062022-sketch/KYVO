package com.kyvo.app.feature.launch.presentation

import com.kyvo.app.domain.auth.AuthState

internal fun isLaunchDestinationReady(
    authState: AuthState,
    onboardingLoaded: Boolean,
): Boolean = when (authState) {
    AuthState.Initializing -> false
    AuthState.SignedOut -> true
    is AuthState.SignedIn -> onboardingLoaded
}

internal fun shouldShowLaunchCover(
    destinationReady: Boolean,
    minimumLaunchDurationElapsed: Boolean,
): Boolean = !destinationReady || !minimumLaunchDurationElapsed
