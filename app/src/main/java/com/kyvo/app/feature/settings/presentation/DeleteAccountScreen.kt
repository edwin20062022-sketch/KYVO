package com.kyvo.app.feature.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyvo.app.core.designsystem.KyvoColors
import com.kyvo.app.domain.auth.AuthRepository
import com.kyvo.app.feature.onboarding.domain.repository.OnboardingRepository

@Composable
fun DeleteAccountRoute(auth: AuthRepository, onboarding: OnboardingRepository, cacheDir: java.io.File, onDeleted: () -> Unit, onBack: () -> Unit, viewModel: DeleteAccountViewModel = viewModel(factory = DeleteAccountViewModel.factory(auth, onboarding) { com.kyvo.app.feature.settings.data.clearOwnedMealShareCache(cacheDir) })) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state) { if (state == DeleteAccountUiState.Deleted) onDeleted() }
    DeleteAccountScreen(state, viewModel::deleteAccount, viewModel::retry, onBack)
}

@Composable
fun DeleteAccountScreen(state: DeleteAccountUiState, onDelete: () -> Unit = {}, onRetry: () -> Unit = {}, onBack: () -> Unit = {}) {
    SettingsScaffold("delete_account_screen", "Eliminar cuenta", "", onBack) {
        item { Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) { Icon(Icons.Outlined.Delete, null, Modifier.size(76.dp), tint = MaterialTheme.colorScheme.error); Text("¿Estás seguro de que quieres eliminar tu cuenta?", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, textAlign = TextAlign.Center); Text("Esta acción es permanente y no se puede deshacer.", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center) } }
        item { Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) { Text("Se eliminará toda tu información, incluyendo:", fontWeight = FontWeight.Bold); DeleteDataRow(Icons.Outlined.Person, "Datos personales", "Tu perfil y configuración de la cuenta."); DeleteDataRow(Icons.Outlined.Info, "Registro de alimentos", "Todas las comidas que has registrado."); DeleteDataRow(Icons.Outlined.Info, "Historial nutricional", "Tus estadísticas y progreso."); DeleteDataRow(Icons.Outlined.Info, "Meal Shares", "Las fotos y contenido que has creado."); DeleteDataRow(Icons.Outlined.Info, "Platillos guardados y favoritos", "Tus recetas, alimentos y listas personalizadas.") } } }
        item { Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = KyvoColors.PurpleSoft)) { Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Info, null, tint = KyvoColors.PurplePrimary); Column(Modifier.padding(start = 14.dp)) { Text("Antes de continuar", fontWeight = FontWeight.Bold); Text("Si solo necesitas hacer algún cambio en tu cuenta o tienes algún problema, te invitamos a contactar a nuestro equipo de soporte. Estamos aquí para ayudarte.", color = MaterialTheme.colorScheme.onSurfaceVariant) } } } }
        item { when (state) { DeleteAccountUiState.Deleting -> Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) { CircularProgressIndicator(Modifier.size(24.dp)); Text("Eliminando cuenta…", Modifier.padding(start = 10.dp)) }; is DeleteAccountUiState.Error -> Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(state.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center); OutlinedButton(onClick = onRetry) { Text("Reintentar") } }; else -> Button(onClick = onDelete, enabled = state != DeleteAccountUiState.Deleting, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).heightIn(min = 52.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),) { Text("Eliminar mi cuenta", fontWeight = FontWeight.Bold) } } }
        if (state != DeleteAccountUiState.Deleting) item { OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).semantics { contentDescription = "Cancelar" }) { Text("Cancelar", color = KyvoColors.PurplePrimary, fontWeight = FontWeight.Bold) } }
    }
}

@Composable private fun DeleteDataRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, Modifier.size(36.dp), tint = KyvoColors.PurplePrimary); Column(Modifier.padding(start = 14.dp)) { Text(title, fontWeight = FontWeight.Bold); Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
