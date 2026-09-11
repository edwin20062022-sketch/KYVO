package com.kyvo.app.feature.profile.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyvo.app.core.designsystem.KyvoColors
import com.kyvo.app.core.designsystem.component.KyvoPrimaryButton
import com.kyvo.app.core.designsystem.component.KyvoTextField
import com.kyvo.app.domain.auth.AuthRepository
import com.kyvo.app.domain.auth.AuthSession

const val EDIT_PROFILE_SCREEN_TAG = "edit_profile_screen"
const val EDIT_PROFILE_SAVE_TAG = "edit_profile_save"

@Composable
fun EditAthleteProfileRoute(
    session: AuthSession,
    authRepository: AuthRepository,
    onBack: () -> Unit,
    viewModel: EditAthleteProfileViewModel = viewModel(factory = EditAthleteProfileViewModel.factory(session, authRepository)),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state) {
        if (state is EditAthleteProfileUiState.Saved) onBack()
    }
    EditAthleteProfileScreen(
        state = state,
        onDisplayNameChanged = viewModel::onDisplayNameChanged,
        onUsernameChanged = viewModel::onUsernameChanged,
        onSave = viewModel::save,
        onBack = onBack,
    )
}

@Composable
fun EditAthleteProfileScreen(
    state: EditAthleteProfileUiState,
    onDisplayNameChanged: (String) -> Unit = {},
    onUsernameChanged: (String) -> Unit = {},
    onSave: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    val draft = when (state) {
        is EditAthleteProfileUiState.Editing -> state.draft
        is EditAthleteProfileUiState.Saving -> state.draft
        is EditAthleteProfileUiState.Error -> state.draft
        EditAthleteProfileUiState.Loading, EditAthleteProfileUiState.Saved -> null
    }
    Surface(
        modifier = Modifier.fillMaxSize().semantics { contentDescription = EDIT_PROFILE_SCREEN_TAG },
        color = MaterialTheme.colorScheme.background,
    ) {
        if (draft == null) {
            CircularProgressIndicator(Modifier.padding(32.dp))
        } else {
            val saving = state is EditAthleteProfileUiState.Saving
            val error = (state as? EditAthleteProfileUiState.Editing)?.validationMessage
                ?: (state as? EditAthleteProfileUiState.Error)?.message
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, enabled = !saving, modifier = Modifier.size(48.dp).semantics { contentDescription = "Volver al perfil" }) {
                        Icon(Icons.Outlined.ArrowBack, null)
                    }
                    TextButton(onClick = onBack, enabled = !saving, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                }
                Text("Editar perfil", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text("Actualiza tu información personal.\nTu perfil es privado.", color = KyvoColors.Slate, style = MaterialTheme.typography.bodyLarge)
                AvatarPlaceholder()
                KyvoTextField(
                    value = draft.displayName,
                    onValueChange = onDisplayNameChanged,
                    label = "Nombre",
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !saving,
                    isError = error != null && draft.displayName.isBlank(),
                    supportingText = error,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )
                KyvoTextField(
                    value = draft.username,
                    onValueChange = onUsernameChanged,
                    label = "Nombre de usuario (opcional)",
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !saving,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                )
                Text("Esto te ayudará a ser identificado dentro de la app.", color = KyvoColors.Slate, style = MaterialTheme.typography.bodyMedium)
                KyvoTextField(
                    value = draft.email,
                    onValueChange = {},
                    label = "Correo electrónico",
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    trailingIcon = { Icon(Icons.Outlined.Lock, "Correo protegido") },
                )
                Text("Para cambiar tu correo, ve a Ajustes de cuenta.", color = KyvoColors.Slate, style = MaterialTheme.typography.bodyMedium)
                KyvoPrimaryButton(
                    text = "Guardar cambios",
                    onClick = onSave,
                    enabled = !saving,
                    isLoading = saving,
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = EDIT_PROFILE_SAVE_TAG },
                )
            }
        }
    }
}

@Composable
private fun AvatarPlaceholder() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Surface(Modifier.size(144.dp), shape = androidx.compose.foundation.shape.CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
            Icon(Icons.Outlined.Person, null, Modifier.padding(34.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        TextButton(onClick = {}, enabled = false, modifier = Modifier.padding(top = 4.dp)) { Text("Cambiar foto") }
        Text("JPG, PNG o HEIC. Máx. 5 MB.", color = KyvoColors.Slate, style = MaterialTheme.typography.bodySmall)
    }
}
