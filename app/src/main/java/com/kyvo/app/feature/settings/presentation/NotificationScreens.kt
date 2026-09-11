package com.kyvo.app.feature.settings.presentation

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyvo.app.core.designsystem.KyvoColors
import com.kyvo.app.feature.settings.data.AppSettingsRepository
import com.kyvo.app.feature.settings.domain.AppSettings
import com.kyvo.app.feature.settings.domain.NotificationPreferences

@Composable
fun NotificationsRoute(repository: AppSettingsRepository, onBack: () -> Unit, onOpenSettings: () -> Unit, viewModel: AppSettingsViewModel = viewModel(factory = AppSettingsViewModel.factory(repository))) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var permission by remember { mutableStateOf(notificationPermissionStatus(context)) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permission = notificationPermissionStatus(context) }
    fun refresh() { permission = notificationPermissionStatus(context) }
    LaunchedEffect(Unit) { refresh() }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) refresh() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    NotificationsScreen(
        settings = settings,
        permission = permission,
        onBack = onBack,
        onMasterChanged = { enabled ->
            viewModel.setNotificationsEnabled(enabled)
            if (enabled && permission == NotificationPermissionStatus.DENIED && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        },
        onBreakfast = viewModel::setBreakfastNotifications,
        onLunch = viewModel::setLunchNotifications,
        onSnack = viewModel::setSnackNotifications,
        onDinner = viewModel::setDinnerNotifications,
        onDayClose = viewModel::setDayCloseNotifications,
        onNewFeatures = viewModel::setNewFeaturesNotifications,
        onTips = viewModel::setTipsAndContentNotifications,
        onAccount = viewModel::setAccountNoticesNotifications,
        onOpenSettings = onOpenSettings,
    )
}

@Composable
fun NotificationsScreen(
    settings: AppSettings,
    permission: NotificationPermissionStatus = NotificationPermissionStatus.NOT_REQUIRED,
    onBack: () -> Unit = {},
    onMasterChanged: (Boolean) -> Unit = {},
    onBreakfast: (Boolean) -> Unit = {},
    onLunch: (Boolean) -> Unit = {},
    onSnack: (Boolean) -> Unit = {},
    onDinner: (Boolean) -> Unit = {},
    onDayClose: (Boolean) -> Unit = {},
    onNewFeatures: (Boolean) -> Unit = {},
    onTips: (Boolean) -> Unit = {},
    onAccount: (Boolean) -> Unit = {},
    onOpenSettings: () -> Unit = {},
) {
    val prefs = settings.notifications
    SettingsScaffold("notifications_screen", "Notificaciones", "Configura cuándo y cómo quieres recibir notificaciones.", onBack) {
        item { NotificationMasterCard(prefs.enabled, permission, onMasterChanged, onOpenSettings) }
        item { NotificationGroup("Recordatorios de comidas", "Te ayudan a mantener el enfoque en tus objetivos.", listOf(
            NotificationRow("Desayuno", "Preferencia para el recordatorio de desayuno.", "8:00 AM", prefs.breakfast, onBreakfast),
            NotificationRow("Comida", "Preferencia para el recordatorio de comida.", "1:00 PM", prefs.lunch, onLunch),
            NotificationRow("Snack", "Preferencia para el recordatorio de snack.", "5:00 PM", prefs.snack, onSnack),
            NotificationRow("Cena", "Preferencia para el recordatorio de cena.", "8:00 PM", prefs.dinner, onDinner),
        )) }
        item { NotificationGroup("Cierre del día", "Te recuerda completar tu registro diario.", listOf(NotificationRow("Recordatorio de cierre", "Preferencia para revisar tu progreso del día.", "10:00 PM", prefs.dayClose, onDayClose))) }
        item { NotificationGroup("Actualizaciones importantes", "Preferencias sobre información relevante de KYVO.", listOf(
            NotificationRow("Nuevas funcionalidades", "Novedades de KYVO.", null, prefs.newFeatures, onNewFeatures),
            NotificationRow("Consejos y contenido", "Recomendaciones para mejorar tu nutrición.", null, prefs.tipsAndContent, onTips),
            NotificationRow("Avisos de la cuenta", "Información importante sobre tu cuenta y servicio.", null, prefs.accountNotices, onAccount),
        )) }
        item { NotificationHint() }
    }
}

private data class NotificationRow(val title: String, val subtitle: String, val time: String?, val checked: Boolean, val onChecked: (Boolean) -> Unit)

@Composable
private fun NotificationMasterCard(enabled: Boolean, permission: NotificationPermissionStatus, onChange: (Boolean) -> Unit, onOpenSettings: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.Icon(Icons.Outlined.Info, null, Modifier.size(32.dp), tint = KyvoColors.PurplePrimary)
            Column(Modifier.weight(1f).padding(start = 14.dp)) { Text("Notificaciones activadas", style = MaterialTheme.typography.titleLarge); Text("Preferencias para recordatorios y actualizaciones.", color = MaterialTheme.colorScheme.onSurfaceVariant); Text(permissionLabel(permission), color = if (permission == NotificationPermissionStatus.GRANTED || permission == NotificationPermissionStatus.NOT_REQUIRED) KyvoColors.Success else MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall); if (permission == NotificationPermissionStatus.DENIED) TextButton(onClick = onOpenSettings) { Text("Abrir Ajustes") } }
            Switch(enabled, onCheckedChange = onChange, modifier = Modifier.semantics { role = Role.Switch; contentDescription = "Notificaciones activadas" })
        }
    }
}

private fun permissionLabel(status: NotificationPermissionStatus) = when (status) {
    NotificationPermissionStatus.GRANTED -> "Permiso del sistema activado"
    NotificationPermissionStatus.NOT_REQUIRED -> "No requiere permiso en esta versión de Android"
    NotificationPermissionStatus.DENIED -> "Permiso del sistema desactivado"
}

@Composable
private fun NotificationGroup(title: String, subtitle: String, rows: List<NotificationRow>) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column(Modifier.padding(18.dp)) { Text(title, style = MaterialTheme.typography.titleLarge); Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp)); rows.forEach { row -> NotificationRowItem(row) } } }
}

@Composable
private fun NotificationRowItem(row: NotificationRow) {
    Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        androidx.compose.material3.Icon(Icons.Outlined.CheckCircle, null, Modifier.size(28.dp), tint = KyvoColors.PurplePrimary)
        Column(Modifier.weight(1f).padding(start = 14.dp)) { Text(row.title, style = MaterialTheme.typography.titleMedium); Text(row.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        row.time?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 10.dp)) }
        Switch(row.checked, row.onChecked, modifier = Modifier.semantics { role = Role.Switch; contentDescription = row.title })
    }
}

@Composable
private fun NotificationHint() {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) { Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) { androidx.compose.material3.Icon(Icons.Outlined.Lock, null, Modifier.size(28.dp), tint = KyvoColors.PurplePrimary); Text("Estas preferencias quedarán listas para el sistema de recordatorios de KYVO.", Modifier.padding(start = 12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
}
