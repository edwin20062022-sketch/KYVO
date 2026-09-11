package com.kyvo.app.feature.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyvo.app.core.designsystem.KyvoColors
import com.kyvo.app.core.designsystem.component.KyvoBrandLockup
import com.kyvo.app.domain.auth.AuthProvider
import com.kyvo.app.domain.auth.AuthRepository
import com.kyvo.app.domain.auth.AuthSession

const val SETTINGS_SCREEN_TAG = "settings_screen"
const val ACCOUNT_SCREEN_TAG = "account_screen"

@Composable
fun SettingsRoute(session: AuthSession, onBack: () -> Unit, onProfile: () -> Unit, onAccount: () -> Unit, onFuture: (String) -> Unit, onSignOut: () -> Unit) {
    SettingsScreen(session, onBack, onProfile, onAccount, onFuture, onSignOut)
}

@Composable
fun SettingsScreen(session: AuthSession, onBack: () -> Unit = {}, onProfile: () -> Unit = {}, onAccount: () -> Unit = {}, onFuture: (String) -> Unit = {}, onSignOut: () -> Unit = {}) {
    SettingsScaffold(SETTINGS_SCREEN_TAG, "Ajustes", "Personaliza tu experiencia en KYVO", onBack, showBrand = true) {
        item { SettingsProfileCard(session, onProfile) }
        item { SettingsSection("CUENTA", listOf(SettingRowData("Cuenta", "Correo, método de acceso y más", Icons.Outlined.Person) { onAccount() }, SettingRowData("Seguridad", "Contraseña y acceso a tu cuenta", Icons.Outlined.Lock) { onFuture("Seguridad") })) }
        item { SettingsSection("NUTRICIÓN", listOf(SettingRowData("Objetivos nutricionales", "Revisa tus metas y recálcula si lo necesitas", Icons.Outlined.CheckCircle) { onFuture("Objetivos nutricionales") }, SettingRowData("Preferencias alimenticias", "Restricciones y número de comidas", Icons.Outlined.Info) { onFuture("Preferencias alimenticias") })) }
        item { SettingsSection("PREFERENCIAS", listOf(SettingRowData("Unidades", "Sistemas de medida", Icons.Outlined.Info) { onFuture("Unidades") }, SettingRowData("Notificaciones", "Recordatorios y avisos", Icons.Outlined.Info) { onFuture("Notificaciones") }, SettingRowData("Apariencia", "Modo claro u oscuro", Icons.Outlined.Info) { onFuture("Apariencia") }, SettingRowData("Meal Share", "Configuración de plantillas y compartir", Icons.Outlined.Info) { onFuture("Preferencias de Meal Share") })) }
        item { SettingsSection("PRIVACIDAD", listOf(SettingRowData("Privacidad", "Gestiona tu información", Icons.Outlined.Lock) { onFuture("Privacidad") }, SettingRowData("Permisos", "Cámara, fotos, notificaciones y más", Icons.Outlined.Info) { onFuture("Permisos") })) }
        item { SettingsSection("SOPORTE", listOf(SettingRowData("Ayuda y soporte", "Preguntas frecuentes y contacto", Icons.Outlined.Info) { onFuture("Ayuda y soporte") }, SettingRowData("Acerca de KYVO", "Versión de la app y más", Icons.Outlined.Info) { onFuture("Acerca de KYVO") }, SettingRowData("Legal", "Términos, privacidad y avisos", Icons.Outlined.Info) { onFuture("Legal") })) }
        item { SettingsSection("", listOf(SettingRowData("Cerrar sesión", "Salir de tu cuenta en este dispositivo", Icons.AutoMirrored.Outlined.ArrowBack, isDanger = true) { onSignOut() }, SettingRowData("Eliminar cuenta", "Esta acción es permanente", Icons.Outlined.Delete, isDanger = true) { onFuture("Eliminar cuenta") })) }
    }
}

@Composable
private fun SettingsProfileCard(session: AuthSession, onClick: () -> Unit) {
    val displayName = session.displayName?.takeIf { it.isNotBlank() } ?: session.email.substringBefore('@').ifBlank { "Tu perfil" }
    Card(onClick = onClick, Modifier.fillMaxWidth().padding(horizontal = 16.dp).semantics { role = Role.Button; contentDescription = "Ver mi perfil" }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) { InitialAvatar(displayName); Column(Modifier.weight(1f).padding(start = 14.dp)) { Text(displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text(session.email, color = MaterialTheme.colorScheme.onSurfaceVariant); Text("Ver mi perfil", color = KyvoColors.PurplePrimary, fontWeight = FontWeight.Bold) }; Text("›", color = KyvoColors.PurplePrimary, style = MaterialTheme.typography.headlineMedium) }
    }
}

@Composable
private fun SettingsSection(title: String, rows: List<SettingRowData>) {
    Column(Modifier.padding(horizontal = 16.dp)) { if (title.isNotBlank()) Text(title, Modifier.padding(start = 4.dp, bottom = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold); Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column { rows.forEachIndexed { index, row -> SettingsRow(row); if (index < rows.lastIndex) androidx.compose.material3.HorizontalDivider(Modifier.padding(start = 68.dp), color = MaterialTheme.colorScheme.outlineVariant) } } } }
}

private data class SettingRowData(val title: String, val subtitle: String, val icon: ImageVector, val isDanger: Boolean = false, val onClick: () -> Unit)

@Composable
private fun SettingsRow(row: SettingRowData) {
    val color = if (row.isDanger) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurface
    Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).clickable(onClick = row.onClick).semantics { role = Role.Button; contentDescription = row.title }.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(row.icon, null, Modifier.size(28.dp), tint = if (row.isDanger) color else MaterialTheme.colorScheme.onSurfaceVariant); Column(Modifier.weight(1f).padding(start = 16.dp)) { Text(row.title, color = color, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text(row.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium) }; Text("›", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.headlineMedium) }
}

@Composable
fun AccountRoute(session: AuthSession, authRepository: AuthRepository, onBack: () -> Unit, onEditProfile: () -> Unit, onFuture: (String) -> Unit, onSignedOut: () -> Unit, viewModel: AccountSettingsViewModel = viewModel(factory = AccountSettingsViewModel.factory(session, authRepository))) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AccountScreen(state, onBack, onEditProfile, onFuture, viewModel::signOut, onSignedOut, viewModel::retrySignOut)
}

@Composable
fun AccountScreen(state: AccountSettingsUiState, onBack: () -> Unit = {}, onEditProfile: () -> Unit = {}, onFuture: (String) -> Unit = {}, onSignOut: () -> Unit = {}, onSignedOut: () -> Unit = {}, onRetry: () -> Unit = {}) {
    when (state) {
        is AccountSettingsUiState.Content -> AccountContent(state.session, onBack, onEditProfile, onFuture, onSignOut, false, null)
        is AccountSettingsUiState.SigningOut -> AccountContent(state.session, onBack, onEditProfile, onFuture, onSignOut, true, null)
        is AccountSettingsUiState.Error -> AccountContent(state.session, onBack, onEditProfile, onFuture, onSignOut, false, state.message, onRetry)
        AccountSettingsUiState.SignedOut -> onSignedOut()
    }
}

@Composable
private fun AccountContent(session: AuthSession, onBack: () -> Unit, onEditProfile: () -> Unit, onFuture: (String) -> Unit, onSignOut: () -> Unit, signingOut: Boolean, error: String?, onRetry: () -> Unit = {}) {
    SettingsScaffold(ACCOUNT_SCREEN_TAG, "Cuenta", "Gestiona tu información y preferencias de acceso.", onBack, showBrand = true) {
        item { AccountIdentityCard(session, onEditProfile) }
        item { AccountSection("MÉTODOS DE ACCESO", listOf(SettingRowData("Correo electrónico", session.email, Icons.Outlined.Person) {}, SettingRowData("Google", if (session.provider == AuthProvider.Google) "Vinculado a tu cuenta" else "No vinculado", Icons.Outlined.Info) {})) }
        item { AccountSection("SEGURIDAD", listOf(SettingRowData("Cambiar contraseña", "Actualiza tu contraseña en cualquier momento.", Icons.Outlined.Lock) { onFuture("Cambiar contraseña") }, SettingRowData("Autenticación con Google", "Gestiona la conexión con tu cuenta de Google.", Icons.Outlined.Info) { onFuture("Autenticación con Google") })) }
        item { AccountSection("DATOS DE LA CUENTA", listOf(SettingRowData("Información personal", "Nombre, correo y más.", Icons.Outlined.Person) { onEditProfile() }, SettingRowData("Gestionar mis datos", "Descarga o revisa tu información.", Icons.Outlined.Info) { onFuture("Gestionar mis datos") })) }
        item { AccountSection("ACCIONES", listOf(SettingRowData("Cerrar sesión", "Salir de tu cuenta en este dispositivo.", Icons.AutoMirrored.Outlined.ArrowBack) { onSignOut() }, SettingRowData("Eliminar cuenta", "Esta acción es permanente.", Icons.Outlined.Delete, isDanger = true) { onFuture("Eliminar cuenta") })) }
        if (signingOut) item { Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) { CircularProgressIndicator(Modifier.size(22.dp), color = KyvoColors.PurplePrimary); Text("Cerrando sesión…", Modifier.padding(start = 10.dp)) } }
        if (error != null) item { Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(error, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center); OutlinedButton(onClick = onRetry) { Text("Reintentar") } } }
    }
}

@Composable
private fun AccountIdentityCard(session: AuthSession, onEditProfile: () -> Unit) {
    val displayName = session.displayName?.takeIf { it.isNotBlank() } ?: session.email.substringBefore('@').ifBlank { "Tu cuenta" }
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) { InitialAvatar(displayName); Column(Modifier.weight(1f).padding(start = 16.dp)) { Text(displayName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text(session.email, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(session.username?.takeIf { it.isNotBlank() }?.let { "@$it" } ?: "Sin nombre de usuario", color = MaterialTheme.colorScheme.onSurfaceVariant) }; OutlinedButton(onClick = onEditProfile) { Icon(Icons.Outlined.Edit, null); Text("Editar perfil", Modifier.padding(start = 6.dp)) } } }
}

@Composable
private fun AccountSection(title: String, rows: List<SettingRowData>) {
    Column(Modifier.padding(horizontal = 16.dp)) { Text(title, Modifier.padding(start = 4.dp, bottom = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold); Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column { rows.forEachIndexed { index, row -> AccountRow(row); if (index < rows.lastIndex) androidx.compose.material3.HorizontalDivider(Modifier.padding(start = 68.dp), color = MaterialTheme.colorScheme.outlineVariant) } } } }
}

@Composable
private fun AccountRow(row: SettingRowData) { Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).clickable(onClick = row.onClick).semantics { role = Role.Button; contentDescription = row.title }.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(row.icon, null, Modifier.size(28.dp), tint = if (row.isDanger) Color(0xFFE53935) else KyvoColors.PurplePrimary); Column(Modifier.weight(1f).padding(start = 16.dp)) { Text(row.title, color = if (row.isDanger) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text(row.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Text("›", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.headlineMedium) } }

@Composable
private fun InitialAvatar(value: String) { Surface(Modifier.size(68.dp), shape = androidx.compose.foundation.shape.CircleShape, color = KyvoColors.PurpleSoft) { Text(value.trim().take(1).uppercase().ifBlank { "K" }, Modifier.fillMaxSize().padding(top = 15.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.headlineMedium, color = KyvoColors.PurplePrimary, fontWeight = FontWeight.Bold) } }

@Composable
internal fun SettingsScaffold(tag: String, title: String, subtitle: String, onBack: () -> Unit, showBrand: Boolean = false, content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) { Surface(Modifier.fillMaxSize().semantics { contentDescription = tag }, color = MaterialTheme.colorScheme.background) { LazyColumn(contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { item { Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack, Modifier.size(48.dp).semantics { contentDescription = "Volver" }) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Volver") }; Column(Modifier.weight(1f).padding(start = 4.dp)) { Text(title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black); Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleMedium) }; if (showBrand) KyvoBrandLockup(horizontal = true, markSize = 30.dp) } }; content() } } }

@Composable
fun SettingsPlaceholderScreen(title: String, onBack: () -> Unit) { SettingsScaffold("settings_placeholder_$title", title, "Esta sección estará disponible en una próxima fase.", onBack) { item { Text("Seguimos trabajando para ofrecerte una experiencia completa en KYVO.", Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
