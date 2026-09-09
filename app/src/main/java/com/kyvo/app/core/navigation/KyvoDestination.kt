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
    FoodFrequent("food/frequent"),
    FoodFavorites("food/favorites"),
    FoodResults("food/results/{query}"),
    FoodDetail("food/detail/{id}/{type}"),
    FoodPortion("food/portion/{id}/{type}"),
    FoodPlaceholder("food/placeholder/{name}"),
    SavedDishes("dishes"),
    SavedDishEditor("dishes/editor/{id}"),
    SavedDishDetail("dishes/detail/{id}"),
    SavedDishAddToDay("dishes/add/{id}"),
    MealShareCamera("meal_share/camera"),
    MealSharePhotoPicker("meal_share/photo_picker"),
    MealSharePhotoPreview("meal_share/photo_preview"),
    MealShareMealBuilder("meal_share/meal_builder"),
    MealShareFoodSearchPlaceholder("meal_share/add_food"),
    MealShareContinuePlaceholder("meal_share/continue"),
}

val mainDestinations = listOf(
    KyvoDestination.Home,
    KyvoDestination.Meals,
    KyvoDestination.MealShare,
    KyvoDestination.Progress,
    KyvoDestination.Profile,
)
