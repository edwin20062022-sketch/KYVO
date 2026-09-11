package com.kyvo.app.feature.settings.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyvo.app.core.designsystem.KyvoColors
import com.kyvo.app.core.designsystem.component.KyvoBrandLockup
import com.kyvo.app.feature.mealshare.domain.model.MealShareTemplate
import com.kyvo.app.feature.settings.data.AppSettingsRepository

@Composable
fun MealSharePreferencesRoute(repository: AppSettingsRepository, onBack: () -> Unit, viewModel: AppSettingsViewModel = viewModel(factory = AppSettingsViewModel.factory(repository))) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    SettingsScaffold("meal_share_preferences_screen", "Preferencias de Meal Share", "Personaliza cómo se verán tus comidas al compartir. Tu estilo, tu progreso, siempre con KYVO.", onBack) {
        item { Text("ESTILO PREDETERMINADO", Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold) }
        item { Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { MealShareTemplate.entries.forEachIndexed { index, template ->
            Card(onClick = { viewModel.setMealShareTemplate(template) }, modifier = Modifier.fillMaxWidth().semantics { role = Role.RadioButton }, colors = CardDefaults.cardColors(containerColor = if (settings.mealShare.defaultTemplate == template) KyvoColors.PurpleSoft else MaterialTheme.colorScheme.surface)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Estilo ${"%02d".format(index + 1)}", fontWeight = FontWeight.Bold); Text(template.label(), color = MaterialTheme.colorScheme.onSurfaceVariant) }; Text(if (settings.mealShare.defaultTemplate == template) "Seleccionado" else "Elegir", color = KyvoColors.PurplePrimary, fontWeight = FontWeight.Bold) }
            }
        } } }
        item { Text("INFORMACIÓN VISIBLE", Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold) }
        item { Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column { PreferenceSwitch("Calorías", settings.mealShare.showCalories, viewModel::setMealShareShowCalories); PreferenceSwitch("Proteína", settings.mealShare.showProtein, viewModel::setMealShareShowProtein); PreferenceSwitch("Carbohidratos", settings.mealShare.showCarbohydrates, viewModel::setMealShareShowCarbohydrates); PreferenceSwitch("Grasas", settings.mealShare.showFat, viewModel::setMealShareShowFat) } } }
        item { Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column { PreferenceSwitch("Guardar automáticamente", settings.mealShare.autoSave, viewModel::setMealShareAutoSave); PreferenceSwitch("Sugerir compartir en Instagram", settings.mealShare.suggestInstagram, viewModel::setMealShareSuggestInstagram) } } }
        item { Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = KyvoColors.PurpleSoft)) { Column(Modifier.padding(16.dp)) { Text("Siempre con KYVO", fontWeight = FontWeight.Bold); Text("El branding de KYVO forma parte del diseño de tus Meal Shares y no puede desactivarse.", color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
    }
}

@Composable private fun PreferenceSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) { Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { Text(label, Modifier.weight(1f), fontWeight = FontWeight.Medium); Switch(checked, onCheckedChange) } }
private fun MealShareTemplate.label() = when (this) { MealShareTemplate.MINIMAL -> "Minimal"; MealShareTemplate.PERFORMANCE -> "Performance"; MealShareTemplate.EDITORIAL -> "Editorial" }

private data class HelpTopic(val title: String, val answer: String)
private val helpTopics = listOf(
    HelpTopic("¿Cómo registro un alimento?", "Busca el alimento por nombre, elige la porción y confirma la comida."),
    HelpTopic("¿Puedo editar o eliminar una comida?", "Abre la comida desde el resumen del día para editarla o eliminarla."),
    HelpTopic("¿Cómo funciona Meal Share?", "Elige una foto 9:16, añade los alimentos y selecciona el estilo de tu card."),
    HelpTopic("¿Cómo se calculan mis objetivos nutricionales?", "KYVO usa tus datos, actividad y objetivo para calcular calorías y macros."),
    HelpTopic("¿Mis datos están seguros?", "Tus datos se gestionan con la cuenta autenticada y los controles de privacidad disponibles en Ajustes."),
)

@Composable
fun HelpScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf<String?>(null) }
    val topics = helpTopics.filter { query.isBlank() || it.title.contains(query, true) }
    SettingsScaffold("help_screen", "Ayuda y soporte", "Estamos aquí para ayudarte.", onBack) {
        item { OutlinedTextField(query, { query = it }, Modifier.padding(horizontal = 16.dp).fillMaxWidth(), label = { Text("Buscar ayuda") }, singleLine = true) }
        item { Text("TEMAS POPULARES", Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold) }
        items(topics.size) { index -> HelpRow(topics[index], expanded == topics[index].title) { expanded = if (expanded == topics[index].title) null else topics[index].title } }
        item { Text("CONTACTO", Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold) }
        item { HelpAction("Contáctanos por chat", "Disponible próximamente", Icons.Outlined.Info, {}) }
        item { HelpAction("Envíanos un correo", "soporte@kyvoapp.com", Icons.Outlined.Email) { openSupportEmail(context) } }
        item { HelpAction("Revisa nuestras preguntas frecuentes", "Encuentra respuestas rápidas", Icons.Outlined.Info, {}) }
    }
}

@Composable private fun HelpRow(topic: HelpTopic, expanded: Boolean, onClick: () -> Unit) { Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth().clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column(Modifier.padding(16.dp)) { Text(topic.title, fontWeight = FontWeight.Bold); if (expanded) Text(topic.answer, Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
@Composable private fun HelpAction(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) { Row(Modifier.padding(horizontal = 16.dp, vertical = 6.dp).fillMaxWidth().clickable(onClick = onClick), verticalAlignment = Alignment.CenterVertically) { androidx.compose.material3.Icon(icon, null, Modifier.size(28.dp), tint = KyvoColors.PurplePrimary); Column(Modifier.padding(start = 14.dp)) { Text(title, fontWeight = FontWeight.Bold); Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
private fun openSupportEmail(context: Context) { val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:soporte@kyvoapp.com")); if (intent.resolveActivity(context.packageManager) != null) context.startActivity(intent) }

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val info = remember { context.packageManager.getPackageInfo(context.packageName, 0) }
    val buildNumber = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.longVersionCode else info.versionCode.toLong()
    SettingsScaffold("about_screen", "Acerca de KYVO", "", onBack, showBrand = true) {
        item { Column(Modifier.padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) { KyvoBrandLockup(markSize = 84.dp); Text("Una app creada para personas que entrenan, que cuidan lo que comen y que buscan más de su rendimiento, todos los días.", Modifier.padding(top = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        item { Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column(Modifier.padding(16.dp)) { Text("Versión ${info.versionName ?: "desconocida"}"); Text("Compilación $buildNumber", color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
        item { Text("Contenido web y contacto: pendiente de fuente oficial del producto.", Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
fun LegalScreen(onBack: () -> Unit, onPrivacy: () -> Unit, onTerms: () -> Unit, onNutritionNotice: () -> Unit, onLicenses: () -> Unit) {
    SettingsScaffold("legal_screen", "Legal", "Información legal y de uso de KYVO.", onBack) {
        item { LegalRow("Términos y condiciones", onTerms); LegalRow("Política de privacidad", onPrivacy); LegalRow("Aviso sobre información nutricional", onNutritionNotice); LegalRow("Licencias de terceros", onLicenses) }
        item { Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = KyvoColors.PurpleSoft)) { Text("La información proporcionada por KYVO es únicamente con fines informativos y no sustituye el consejo, diagnóstico o tratamiento profesional de un médico, nutriólogo u otro especialista de la salud.", Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        item { Text("KYVO · Nutrición para atletas", Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}
@Composable private fun LegalRow(title: String, onClick: () -> Unit) { Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) { androidx.compose.material3.Icon(Icons.Outlined.Info, null, tint = KyvoColors.PurplePrimary); Text(title, Modifier.weight(1f).padding(start = 14.dp), fontWeight = FontWeight.Bold); Text("›", color = MaterialTheme.colorScheme.onSurfaceVariant) }; HorizontalDivider(Modifier.padding(horizontal = 16.dp)) }

@Composable fun LegalPendingScreen(title: String, onBack: () -> Unit) { SettingsScaffold("legal_pending_$title", title, "Contenido pendiente de aprobación legal.", onBack) { item { Text("Este contenido se incorporará cuando exista la versión oficial aprobada.", Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
