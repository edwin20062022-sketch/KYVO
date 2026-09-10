package com.kyvo.app.core.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KyvoBottomNavigationTest {
    @Test fun topLevelRoutesShowTheBottomNavigation() {
        listOf(
            KyvoDestination.Home.route,
            KyvoDestination.Meals.route,
            KyvoDestination.MealShare.route,
            KyvoDestination.Progress.route,
            KyvoDestination.Profile.route,
        ).forEach { route -> assertTrue("$route should show bottom navigation", shouldShowKyvoBottomNavigation(route)) }
    }

    @Test fun focusedFlowsHideTheBottomNavigation() {
        listOf(
            KyvoDestination.ProgressCalories.route,
            KyvoDestination.ProgressHistory.route,
            KyvoDestination.ProgressDay.route,
            KyvoDestination.FoodDetail.route,
            KyvoDestination.MealShareCamera.route,
        ).forEach { route -> assertFalse("$route should hide bottom navigation", shouldShowKyvoBottomNavigation(route)) }
    }

    @Test fun activeDestinationUsesRouteGroupsForChildren() {
        assertEquals(KyvoBottomDestination.Home, kyvoBottomDestinationForRoute(KyvoDestination.Home.route))
        assertEquals(KyvoBottomDestination.Meals, kyvoBottomDestinationForRoute(KyvoDestination.FoodDetail.route))
        assertEquals(KyvoBottomDestination.MealShare, kyvoBottomDestinationForRoute(KyvoDestination.MealShareEditor.route))
        assertEquals(KyvoBottomDestination.Progress, kyvoBottomDestinationForRoute(KyvoDestination.ProgressCalories.route))
        assertEquals(KyvoBottomDestination.Progress, kyvoBottomDestinationForRoute(KyvoDestination.ProgressDay.route))
    }

    @Test fun centralPlanAssetRepresentsTheMealShareAction() {
        assertEquals("Plan", KyvoBottomDestination.MealShare.label)
        assertEquals("Meal Share", KyvoBottomDestination.MealShare.accessibilityLabel)
        assertEquals(KyvoDestination.MealShare.route, KyvoBottomDestination.MealShare.route)
    }
}
