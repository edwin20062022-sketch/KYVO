package com.kyvo.app.core.navigation

enum class KyvoDestination(val route: String) {
    Foundation("foundation"),
    Login("login"),
    Onboarding("onboarding"),
    Home("home"),
    Meals("meals"),
    MealShare("meal_share"),
    Progress("progress"),
    Profile("profile"),
}

val mainDestinations = listOf(
    KyvoDestination.Home,
    KyvoDestination.Meals,
    KyvoDestination.MealShare,
    KyvoDestination.Progress,
    KyvoDestination.Profile,
)

