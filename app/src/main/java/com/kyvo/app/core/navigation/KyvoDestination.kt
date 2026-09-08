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
    FoodHub("food/hub"),
    FoodSearch("food/search"),
    FoodResults("food/results/{query}"),
    FoodDetail("food/detail/{id}/{type}"),
    FoodPortion("food/portion/{id}/{type}"),
    FoodPlaceholder("food/placeholder/{name}"),
}

val mainDestinations = listOf(
    KyvoDestination.Home,
    KyvoDestination.Meals,
    KyvoDestination.MealShare,
    KyvoDestination.Progress,
    KyvoDestination.Profile,
)
