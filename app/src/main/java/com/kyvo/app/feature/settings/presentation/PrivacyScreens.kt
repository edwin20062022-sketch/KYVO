package com.kyvo.app.feature.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kyvo.app.core.designsystem.KyvoColors

@Composable
fun PrivacyScreen(onBack: () -> Unit = {}, onPolicy: () -> Unit = {}, onDeleteData: () -> Unit = {}) {
    SettingsScaffold("privacy_screen", "Privacidad", "Tu información, en tus manos.", onBack) {
        item { PrivacyHero() }
        item { Text("Qué información utilizamos", Modifier.padding(horizontal = 20.dp), style = MaterialTheme.typography.headlineMedium) }
        item { Text("Utilizamos únicamente la información necesaria para brindarte una experiencia personalizada y segura.", Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { PrivacyCard(listOf(
            PrivacyRow("Datos personales", "Nombre, correo, edad, género, altura y actividad para calcular tus objetivos nutricionales.", Icons.Outlined.Person),
            PrivacyRow("Datos de nutrición", "Alimentos, comidas y preferencias alimenticias que registras en la app.", Icons.Outlined.Info),
            PrivacyRow("Fotografías de Meal Share", "Las fotos seleccionadas se utilizan para crear tu card nutricional y compartirla.", Icons.Outlined.Info),
            PrivacyRow("Información de uso", "Información técnica y de uso necesaria para que la app funcione.", Icons.Outlined.CheckCircle),
        )) }
        item { Text("Tus derechos", Modifier.padding(horizontal = 20.dp), style = MaterialTheme.typography.headlineMedium) }
        item { Text("Puedes gestionar tu información en cualquier momento.", Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { PrivacyCard(listOf(
            PrivacyRow("Descargar mis datos", "Solicita una copia de tu información personal y nutricional.", Icons.Outlined.Info, onClick = {}),
            PrivacyRow("Eliminar mis datos", "Puedes solicitar la eliminación de tu información.", Icons.Outlined.Delete, onClick = onDeleteData, danger = true),
            PrivacyRow("Política de privacidad", "Conoce más sobre cómo protegemos tu información.", Icons.Outlined.Info, onClick = onPolicy),
        )) }
        item { PrivacyFooter() }
    }
}

private data class PrivacyRow(val title: String, val subtitle: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val onClick: () -> Unit = {}, val danger: Boolean = false)

@Composable
private fun PrivacyHero() {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) { Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Lock, null, Modifier.size(52.dp), tint = KyvoColors.PurplePrimary); Column(Modifier.padding(start = 18.dp)) { Text("Tu confianza nos impulsa", style = MaterialTheme.typography.headlineMedium); Text("En KYVO nos comprometemos a proteger tu información y utilizarla solo para mejorar tu experiencia.", Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
}

@Composable
private fun PrivacyCard(rows: List<PrivacyRow>) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column { rows.forEach { row -> Row(Modifier.fillMaxWidth().clickable(onClick = row.onClick).semantics { role = Role.Button; contentDescription = row.title }.padding(18.dp), verticalAlignment = Alignment.CenterVertically) { Icon(row.icon, null, Modifier.size(32.dp), tint = if (row.danger) MaterialTheme.colorScheme.error else KyvoColors.PurplePrimary); Column(Modifier.padding(start = 16.dp)) { Text(row.title, style = MaterialTheme.typography.titleMedium, color = if (row.danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface); Text(row.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) } } } } }
}

@Composable
private fun PrivacyFooter() {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) { Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Lock, null, Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant); Column(Modifier.padding(start = 14.dp)) { Text("Tu privacidad es una prioridad", style = MaterialTheme.typography.titleMedium); Text("No compartimos tu información personal con terceros sin tu consentimiento.", color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
}
