package com.kyvo.app.feature.profile.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyvo.app.core.designsystem.KyvoColors
import com.kyvo.app.core.designsystem.component.KyvoBrandLockup
import com.kyvo.app.core.designsystem.component.KyvoCard
import com.kyvo.app.domain.auth.AuthSession
import com.kyvo.app.feature.onboarding.domain.model.ExperienceLevel
import com.kyvo.app.feature.onboarding.domain.model.FitnessGoal
import com.kyvo.app.feature.onboarding.domain.model.FoodPreference
import com.kyvo.app.feature.onboarding.domain.model.TrainingType
import com.kyvo.app.feature.onboarding.domain.repository.OnboardingRepository
import com.kyvo.app.feature.profile.domain.AthleteProfile

const val PROFILE_SCREEN_TAG = "athlete_profile_screen"
const val PROFILE_RETRY_TAG = "athlete_profile_retry"

@Composable
fun AthleteProfileRoute(
    session: AuthSession,
    repository: OnboardingRepository,
    onEditProfile: () -> Unit,
    onNutritionPlan: () -> Unit,
    onUpdateInfo: () -> Unit,
    onPersonalPreferences: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: AthleteProfileViewModel = viewModel(factory = AthleteProfileViewModel.factory(session, repository)),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AthleteProfileScreen(state, session.avatarUrl, session.avatarVersion, viewModel::retry, onEditProfile, onNutritionPlan, onUpdateInfo, onPersonalPreferences, modifier)
}

@Composable
fun AthleteProfileScreen(
    state: AthleteProfileUiState,
    avatarUrl: String? = null,
    avatarVersion: String? = null,
    onRetry: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onNutritionPlan: () -> Unit = {},
    onUpdateInfo: () -> Unit = {},
    onPersonalPreferences: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Surface(modifier.fillMaxSize().semantics { contentDescription = "Perfil de atleta" }, color = MaterialTheme.colorScheme.background) {
        when (state) {
            AthleteProfileUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is AthleteProfileUiState.Error -> ProfileError(state.message, onRetry)
            is AthleteProfileUiState.Content -> ProfileContent(state.profile, avatarUrl, avatarVersion, false, onEditProfile, onNutritionPlan, onUpdateInfo, onPersonalPreferences)
            is AthleteProfileUiState.Incomplete -> ProfileContent(state.profile, avatarUrl, avatarVersion, true, onEditProfile, onNutritionPlan, onUpdateInfo, onPersonalPreferences)
        }
    }
}

@Composable
private fun ProfileError(message: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        OutlinedButton(onClick = onRetry, modifier = Modifier.padding(top = 16.dp).semantics { contentDescription = "Reintentar carga del perfil" }) { Text("Reintentar") }
    }
}

@Composable
private fun ProfileContent(
    profile: AthleteProfile,
    avatarUrl: String? = null,
    avatarVersion: String? = null,
    incomplete: Boolean,
    onEditProfile: () -> Unit,
    onNutritionPlan: () -> Unit,
    onUpdateInfo: () -> Unit,
    onPersonalPreferences: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().semantics { contentDescription = PROFILE_SCREEN_TAG },
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                KyvoBrandLockup(horizontal = true, markSize = 32.dp)
                IconButton(onClick = onPersonalPreferences, Modifier.size(48.dp).semantics { contentDescription = "Ajustes" }) { Icon(Icons.Outlined.Info, null) }
            }
        }
        item { Identity(profile, avatarUrl, avatarVersion, onEditProfile) }
        if (incomplete) item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = MaterialTheme.shapes.large) {
                Text("Completa tu onboarding para ver todos tus datos.", Modifier.padding(18.dp), style = MaterialTheme.typography.bodyMedium)
            }
        }
        item { InfoGrid(profile) }
        item { NutritionCard(profile, onNutritionPlan) }
        item { PreferenceCards(profile) }
        item { ActionCard(onNutritionPlan, onUpdateInfo) }
        item { EditCard(onEditProfile) }
    }
}

@Composable
private fun Identity(profile: AthleteProfile, avatarUrl: String? = null, avatarVersion: String? = null, onEditProfile: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(112.dp), contentAlignment = Alignment.BottomEnd) {
            com.kyvo.app.core.designsystem.component.KyvoUserAvatar(
                name = profile.displayName,
                avatarUrl = avatarUrl,
                avatarVersion = avatarVersion,
                size = 104.dp,
            )
            Surface(Modifier.size(42.dp), CircleShape, color = KyvoColors.Ink, contentColor = Color.White, border = BorderStroke(3.dp, MaterialTheme.colorScheme.background)) {
                IconButton(onClick = onEditProfile, Modifier.fillMaxSize().semantics { contentDescription = "Editar foto de perfil" }) { Icon(Icons.Outlined.Edit, null, Modifier.size(20.dp)) }
            }
        }
        Column(Modifier.padding(start = 14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(profile.displayName ?: "Tu perfil", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Disciplina hoy, resultados mañana.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private data class Info(val title: String, val value: String, val icon: ImageVector)

@Composable
private fun InfoGrid(profile: AthleteProfile) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        InfoRow(Info("Objetivo", profile.goal?.label() ?: "Sin definir", Icons.Outlined.CheckCircle), Info("Experiencia", profile.experience?.label() ?: "Sin definir", Icons.Outlined.CheckCircle))
        InfoRow(Info("Días de entrenamiento", profile.trainingDaysPerWeek?.let { "$it días por semana" } ?: "Sin definir", Icons.Outlined.Info), Info("Tipo de entrenamiento", profile.trainingType?.label() ?: "Sin definir", Icons.Outlined.CheckCircle))
    }
}

@Composable
private fun InfoRow(left: Info, right: Info) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { InfoCard(left, Modifier.weight(1f)); InfoCard(right, Modifier.weight(1f)) }
}

@Composable
private fun InfoCard(info: Info, modifier: Modifier) {
    KyvoCard(modifier, PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(info.icon, null, Modifier.size(28.dp), tint = KyvoColors.PurplePrimary)
            Column(Modifier.padding(start = 10.dp)) {
                Text(info.title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                Text(info.value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2)
            }
        }
    }
}

@Composable
private fun NutritionCard(profile: AthleteProfile, onClick: () -> Unit) {
    Card(onClick = onClick, Modifier.fillMaxWidth().semantics { role = Role.Button; contentDescription = "Ver detalles del plan nutricional" }, shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = KyvoColors.PurpleSoft)) {
        Column(Modifier.padding(20.dp)) {
            Text("Tu plan nutricional", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Ver detalles", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = KyvoColors.PurplePrimary, modifier = Modifier.padding(top = 4.dp))
            val plan = profile.nutritionPlan
            if (plan == null) {
                Text("Tu plan aún no está disponible.", Modifier.padding(top = 18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Column(Modifier.fillMaxWidth().padding(top = 18.dp)) {
                    Text("Meta diaria", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    Text("${plan.targetCaloriesKcal} kcal", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Black, maxLines = 1)
                    Text("Calculado para tu objetivo y nivel de actividad.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
                    Row(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Macro("Proteína", plan.proteinGrams, KyvoColors.Protein, Icons.Outlined.CheckCircle)
                        Macro("Carbohidratos", plan.carbohydrateGrams, KyvoColors.Carbohydrate, Icons.Outlined.CheckCircle)
                        Macro("Grasas", plan.fatGrams, KyvoColors.Fat, Icons.Outlined.CheckCircle)
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.Macro(label: String, value: Int, color: Color, icon: ImageVector) {
    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(Modifier.size(62.dp), CircleShape, color = MaterialTheme.colorScheme.surface, border = BorderStroke(5.dp, color.copy(alpha = .65f))) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Icon(icon, null, Modifier.size(16.dp), tint = color); Text("${value}g", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1) }
        }
        Text(label, color = KyvoColors.PurplePrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center, maxLines = 2)
    }
}

@Composable
private fun PreferenceCards(profile: AthleteProfile) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SmallCard("Comidas al día", profile.mealsPerDay?.let { "$it comidas" } ?: "Sin definir", "Distribuidas según tu estilo de vida.", Modifier.fillMaxWidth())
        SmallCard("Preferencia alimenticia", profile.foodPreference?.label() ?: "Sin definir", "Preferencia registrada.", Modifier.fillMaxWidth())
    }
}

@Composable
private fun SmallCard(title: String, value: String, caption: String, modifier: Modifier) {
    KyvoCard(modifier, PaddingValues(14.dp)) {
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2)
            Text(caption, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
        }
    }
}

@Composable
private fun ActionCard(onPlan: () -> Unit, onUpdateInfo: () -> Unit) {
    KyvoCard(Modifier.fillMaxWidth(), PaddingValues(horizontal = 18.dp, vertical = 4.dp)) {
        Action("Mi plan nutricional", "Revisa tus calorías, macros y parámetros.", Icons.Outlined.CheckCircle, onPlan)
        HorizontalDivider()
        Action("Actualizar información", "Actualiza tus datos, actividad y preferencias.", Icons.Outlined.Edit, onUpdateInfo)
    }
}

@Composable
private fun Action(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Card(onClick = onClick, Modifier.fillMaxWidth().height(76.dp).semantics { role = Role.Button; contentDescription = title }, colors = CardDefaults.cardColors(containerColor = Color.Transparent), elevation = CardDefaults.cardElevation(0.dp)) {
        Row(Modifier.fillMaxSize().padding(horizontal = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(Modifier.weight(1f).padding(start = 16.dp)) { Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2) }
        }
    }
}

@Composable
private fun EditCard(onClick: () -> Unit) {
    Card(onClick = onClick, Modifier.fillMaxWidth().semantics { role = Role.Button; contentDescription = "Editar perfil" }, shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.fillMaxWidth().height(86.dp).padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(44.dp), RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Edit, null) } }
            Column(Modifier.weight(1f).padding(start = 16.dp)) { Text("Editar perfil", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text("Foto, nombre y datos de tu cuenta.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

private fun FitnessGoal.label() = when (this) {
    FitnessGoal.FatLoss -> "Bajar grasa"
    FitnessGoal.MuscleGain -> "Ganancia muscular"
    FitnessGoal.Recomposition -> "Recomposición"
    FitnessGoal.Maintenance -> "Mantenimiento"
    FitnessGoal.Performance -> "Performance / fuerza"
}
private fun ExperienceLevel.label() = when (this) {
    ExperienceLevel.Beginner -> "Principiante"
    ExperienceLevel.Intermediate -> "Intermedio"
    ExperienceLevel.Advanced -> "Avanzado"
}
private fun TrainingType.label() = when (this) {
    TrainingType.Strength -> "Fuerza"
    TrainingType.Hypertrophy -> "Hipertrofia"
    TrainingType.Functional -> "Funcional"
    TrainingType.Cardio -> "Cardio"
}
private fun FoodPreference.label() = when (this) {
    FoodPreference.None -> "Sin restricciones"
    FoodPreference.Vegetarian -> "Vegetariano"
    FoodPreference.Vegan -> "Vegano"
    FoodPreference.GlutenFree -> "Sin gluten"
    FoodPreference.DairyFree -> "Sin lácteos"
    FoodPreference.Other -> "Otra preferencia"
}
