package com.kyvo.app.core.navigation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.kyvo.app.R

internal enum class KyvoBottomDestination(
    val route: String,
    val label: String,
    val accessibilityLabel: String = label,
    @DrawableRes val icon: Int,
) {
    Home(KyvoDestination.Home.route, "Inicio", icon = R.drawable.ic_nav_inicio),
    Meals(KyvoDestination.Meals.route, "Alimentos", accessibilityLabel = "Comidas", icon = R.drawable.ic_nav_alimentos),
    Plan(KyvoDestination.Plan.route, "Plan", accessibilityLabel = "Plan nutricional", icon = R.drawable.ic_nav_plan),
    Progress(KyvoDestination.Progress.route, "Progreso", icon = R.drawable.ic_nav_progreso),
    Profile(KyvoDestination.Profile.route, "Perfil", icon = R.drawable.ic_nav_perfil),
}

internal fun shouldShowKyvoBottomNavigation(route: String?): Boolean = route in setOf(
    KyvoDestination.Home.route,
    KyvoDestination.Meals.route,
    KyvoDestination.Plan.route,
    KyvoDestination.Progress.route,
    KyvoDestination.Profile.route,
)

internal fun kyvoBottomDestinationForRoute(route: String?): KyvoBottomDestination? = when {
    route == KyvoDestination.Home.route -> KyvoBottomDestination.Home
    route == KyvoDestination.Meals.route || route?.startsWith("food/") == true || route?.startsWith("dishes") == true -> KyvoBottomDestination.Meals
    route == KyvoDestination.Plan.route || route?.startsWith("plan") == true -> KyvoBottomDestination.Plan
    route?.startsWith("meal_share") == true -> KyvoBottomDestination.Plan
    route == KyvoDestination.Progress.route || route?.startsWith("progress/") == true -> KyvoBottomDestination.Progress
    route == KyvoDestination.Profile.route -> KyvoBottomDestination.Profile
    else -> null
}

internal fun kyvoBottomDestinationFor(destination: NavDestination?): KyvoBottomDestination? = destination
    ?.hierarchy
    ?.mapNotNull(NavDestination::route)
    ?.firstNotNullOfOrNull(::kyvoBottomDestinationForRoute)

@Composable
internal fun KyvoBottomNavigation(selected: KyvoBottomDestination?, onDestinationSelected: (KyvoBottomDestination) -> Unit) {
    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 18.dp, vertical = 10.dp),
        shape = RoundedCornerShape(38.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(82.dp).padding(horizontal = 6.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            KyvoBottomDestination.entries.forEach { destination ->
                val isSelected = selected == destination
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .then(if (isSelected) Modifier.background(MaterialTheme.colorScheme.surfaceVariant) else Modifier)
                        .selectable(selected = isSelected, role = Role.Tab, onClick = { onDestinationSelected(destination) })
                        .semantics(mergeDescendants = true) {
                            contentDescription = destination.accessibilityLabel
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painter = painterResource(destination.icon),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        colorFilter = ColorFilter.tint(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant),
                    )
                    Text(
                        text = destination.label,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
