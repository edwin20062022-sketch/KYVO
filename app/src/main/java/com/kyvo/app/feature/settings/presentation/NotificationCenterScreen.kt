package com.kyvo.app.feature.settings.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kyvo.app.core.designsystem.component.KyvoStateView

@Composable
fun NotificationCenterScreen(onBack: () -> Unit = {}, onNotificationSettings: () -> Unit = {}, onPermissions: () -> Unit = {}) {
    SettingsScaffold("notification_center_screen", "Centro de notificaciones", "Mantente al día con KYVO.", onBack) {
        item { KyvoStateView(Icons.Outlined.Info, "No tienes notificaciones", "Aquí aparecerán las novedades y avisos importantes de KYVO.") }
        item { Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Text("Las notificaciones estarán disponibles cuando KYVO tenga un historial real de avisos.", Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        item { SettingsInlineAction("Ajustes de notificaciones", "Gestiona tus preferencias", onNotificationSettings) }
        item { SettingsInlineAction("Permisos del sistema", "Revisa los permisos de KYVO", onPermissions) }
    }
}

@Composable private fun SettingsInlineAction(title: String, subtitle: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Column(Modifier.fillMaxWidth()) { Text(title, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium); Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium) } }
    }
}
