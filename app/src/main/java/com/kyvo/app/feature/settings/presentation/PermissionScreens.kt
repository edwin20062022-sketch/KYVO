package com.kyvo.app.feature.settings.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kyvo.app.core.designsystem.KyvoColors

enum class CameraPermissionStatus { GRANTED, DENIED }

data class PermissionScreenState(
    val camera: CameraPermissionStatus,
    val notifications: NotificationPermissionStatus,
)

fun cameraPermissionStatus(context: Context): CameraPermissionStatus = if (
    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
) CameraPermissionStatus.GRANTED else CameraPermissionStatus.DENIED

@Composable
fun PermissionsRoute(onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var state by remember { mutableStateOf(readPermissionState(context)) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { state = readPermissionState(context) }
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { state = readPermissionState(context) }
    fun refresh() { state = readPermissionState(context) }
    LaunchedEffect(Unit) { refresh() }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) refresh() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    PermissionsScreen(
        state = state,
        onBack = onBack,
        onCamera = { cameraLauncher.launch(Manifest.permission.CAMERA) },
        onNotifications = { if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
        onOpenSettings = { openKyvoAppSettings(context) },
    )
}

private fun readPermissionState(context: Context) = PermissionScreenState(cameraPermissionStatus(context), notificationPermissionStatus(context))

@Composable
fun PermissionsScreen(
    state: PermissionScreenState,
    onBack: () -> Unit = {},
    onCamera: () -> Unit = {},
    onNotifications: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
) {
    SettingsScaffold("permissions_screen", "Permisos", "KYVO utiliza ciertos permisos para ofrecerte una mejor experiencia. Puedes gestionarlos en cualquier momento.", onBack) {
        item { PermissionRow("Cámara", "Permite tomar fotos de tus comidas para crear y compartir tu Meal Share.", statusLabel(state.camera), state.camera == CameraPermissionStatus.GRANTED, onCamera) }
        item { PermissionRow("Fotos", "Las imágenes se seleccionan mediante el selector seguro de Android.", "Selector seguro", true, {}) }
        item { PermissionRow("Notificaciones", "Recibe recordatorios y otras actualizaciones importantes.", notificationLabel(state.notifications), state.notifications != NotificationPermissionStatus.DENIED, onNotifications) }
        item { PermissionHint() }
        item { PermissionRow("Abrir configuración del dispositivo", "Gestiona los permisos desde la configuración de tu teléfono.", "Abrir", true, onOpenSettings) }
    }
}

private fun statusLabel(status: CameraPermissionStatus) = if (status == CameraPermissionStatus.GRANTED) "Permitido" else "No permitido"
private fun notificationLabel(status: NotificationPermissionStatus) = when (status) {
    NotificationPermissionStatus.GRANTED -> "Permitido"
    NotificationPermissionStatus.NOT_REQUIRED -> "No requerido en esta versión"
    NotificationPermissionStatus.DENIED -> "No permitido"
}

@Composable
private fun PermissionRow(title: String, subtitle: String, status: String, granted: Boolean, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable(onClick = onClick).semantics { role = Role.Button; contentDescription = title }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Info, null, Modifier.size(36.dp), tint = KyvoColors.PurplePrimary); Column(Modifier.weight(1f).padding(start = 16.dp)) { Text(title, style = MaterialTheme.typography.titleLarge); Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(status, color = if (granted) KyvoColors.Success else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge) }; Text("›", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
}

@Composable
private fun PermissionHint() {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) { Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Lock, null, Modifier.size(30.dp), tint = KyvoColors.PurplePrimary); Text("Solo solicitamos permisos necesarios para que KYVO funcione correctamente.", Modifier.padding(start = 14.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
}
