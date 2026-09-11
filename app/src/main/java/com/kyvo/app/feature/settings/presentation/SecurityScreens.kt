package com.kyvo.app.feature.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import com.kyvo.app.core.designsystem.KyvoColors
import com.kyvo.app.core.designsystem.component.KyvoPrimaryButton
import com.kyvo.app.core.designsystem.component.KyvoTextField
import com.kyvo.app.domain.auth.AuthProvider
import com.kyvo.app.domain.auth.AuthRepository
import com.kyvo.app.domain.auth.AuthResult
import com.kyvo.app.domain.auth.AuthSession
import kotlinx.coroutines.launch

sealed interface SecurityUiState {
    data class Content(
        val session: AuthSession,
        val newPassword: String = "",
        val confirmation: String = "",
        val passwordVisible: Boolean = false,
        val isSaving: Boolean = false,
        val message: String? = null,
        val success: Boolean = false,
    ) : SecurityUiState
}

class SecurityViewModel(
    private val session: AuthSession,
    private val authRepository: AuthRepository,
) : androidx.lifecycle.ViewModel() {
    private val _state = kotlinx.coroutines.flow.MutableStateFlow<SecurityUiState>(SecurityUiState.Content(session))
    val state: kotlinx.coroutines.flow.StateFlow<SecurityUiState> = _state

    fun setNewPassword(value: String) = update { copy(newPassword = value, message = null, success = false) }
    fun setConfirmation(value: String) = update { copy(confirmation = value, message = null, success = false) }
    fun togglePasswordVisibility() = update { copy(passwordVisible = !passwordVisible) }

    fun updatePassword() {
        val current = _state.value as SecurityUiState.Content
        if (current.isSaving || session.provider == AuthProvider.Google) return
        val message = when {
            current.newPassword.isEmpty() -> "Ingresa una nueva contraseña."
            current.newPassword.length < 8 -> "La contraseña debe tener al menos 8 caracteres."
            current.newPassword != current.confirmation -> "Las contraseñas no coinciden."
            else -> null
        }
        if (message != null) {
            update { copy(message = message) }
            return
        }
        update { copy(isSaving = true, message = null, success = false) }
        viewModelScope.launch {
            val result = authRepository.updatePassword(current.newPassword.toCharArray())
            when (result) {
                is AuthResult.Success -> _state.value = SecurityUiState.Content(session, message = "Contraseña actualizada.", success = true)
                else -> _state.value = current.copy(isSaving = false, message = "No pudimos actualizar tu contraseña. Inténtalo de nuevo.", success = false)
            }
        }
    }

    private fun update(transform: SecurityUiState.Content.() -> SecurityUiState.Content) {
        _state.value = (_state.value as SecurityUiState.Content).transform()
    }
}

@Composable
fun SecurityRoute(session: AuthSession, authRepository: AuthRepository, onBack: () -> Unit, viewModel: SecurityViewModel = viewModel(factory = securityFactory(session, authRepository))) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SecurityScreen(state as SecurityUiState.Content, onBack, viewModel::setNewPassword, viewModel::setConfirmation, viewModel::togglePasswordVisibility, viewModel::updatePassword)
}

private fun securityFactory(session: AuthSession, repository: AuthRepository) = object : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = SecurityViewModel(session, repository) as T
}

@Composable
fun SecurityScreen(
    state: SecurityUiState.Content,
    onBack: () -> Unit = {},
    onNewPassword: (String) -> Unit = {},
    onConfirmation: (String) -> Unit = {},
    onToggleVisibility: () -> Unit = {},
    onSavePassword: () -> Unit = {},
) {
    SettingsScaffold("security_screen", "Seguridad", "Administra el acceso a tu cuenta y mantén tus datos seguros.", onBack) {
        item { SecurityAccessCard(state.session) }
        item { SecurityPasswordCard(state, onNewPassword, onConfirmation, onToggleVisibility, onSavePassword) }
        item { SecurityInfoCard() }
    }
}

@Composable
private fun SecurityAccessCard(session: AuthSession) {
    SecurityCard("Métodos de acceso", "Gestiona cómo inicias sesión en KYVO.") {
        SecurityRow(Icons.Outlined.Email, "Correo electrónico", session.email, if (session.provider == AuthProvider.Email) "Método principal" else null)
        SecurityRow(Icons.Outlined.CheckCircle, "Google", if (session.provider == AuthProvider.Google) "Vinculado a tu cuenta" else "No vinculado", if (session.provider == AuthProvider.Google) "Conectado" else null)
    }
}

@Composable
private fun SecurityPasswordCard(state: SecurityUiState.Content, onNewPassword: (String) -> Unit, onConfirmation: (String) -> Unit, onToggleVisibility: () -> Unit, onSave: () -> Unit) {
    SecurityCard("Contraseña", if (state.session.provider == AuthProvider.Google) "Tu acceso se administra mediante Google." else "Mantén tu cuenta protegida con una contraseña segura.") {
        if (state.session.provider == AuthProvider.Google) {
            SecurityRow(Icons.Outlined.Lock, "Cambiar contraseña", "Administrada por Google", null)
        } else {
            KyvoTextField(state.newPassword, onNewPassword, "Nueva contraseña", Modifier.fillMaxWidth(), visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = onToggleVisibility, Modifier.semantics { contentDescription = if (state.passwordVisible) "Ocultar contraseña" else "Mostrar contraseña" }) { Icon(Icons.Outlined.Info, null) } })
            KyvoTextField(state.confirmation, onConfirmation, "Confirmar contraseña", Modifier.fillMaxWidth().padding(top = 12.dp), visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation())
            state.message?.let { Text(it, color = if (state.success) KyvoColors.Success else MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 10.dp)) }
            KyvoPrimaryButton("Guardar contraseña", onSave, Modifier.padding(top = 14.dp), isLoading = state.isSaving)
        }
    }
}

@Composable
private fun SecurityInfoCard() {
    SecurityCard("Sesiones activas", "Revisa los dispositivos donde has iniciado sesión.") {
        SecurityRow(Icons.Outlined.Info, "Administrar sesiones", "1 sesión activa", null)
    }
    SecurityCard("Verificación en dos pasos", "Añade una capa extra de seguridad a tu cuenta.") {
        Row(Modifier.fillMaxWidth().heightIn(min = 64.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Info, null, Modifier.size(28.dp), tint = KyvoColors.PurplePrimary)
            Column(Modifier.weight(1f).padding(start = 16.dp)) { Text("Verificación en dos pasos", style = MaterialTheme.typography.titleMedium); Text("No disponible todavía", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Switch(checked = false, onCheckedChange = null, modifier = Modifier.semantics { role = Role.Switch; contentDescription = "Verificación en dos pasos no disponible" })
        }
    }
}

@Composable
private fun SecurityCard(title: String, subtitle: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Text(title, style = MaterialTheme.typography.titleLarge); Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant); content() }
    }
}

@Composable
private fun SecurityRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, badge: String?) {
    Row(Modifier.fillMaxWidth().heightIn(min = 64.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(28.dp), tint = KyvoColors.PurplePrimary)
        Column(Modifier.weight(1f).padding(start = 16.dp)) { Text(title, style = MaterialTheme.typography.titleMedium); Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        badge?.let { Text(it, color = KyvoColors.PurplePrimary, style = MaterialTheme.typography.labelLarge) }
    }
}
