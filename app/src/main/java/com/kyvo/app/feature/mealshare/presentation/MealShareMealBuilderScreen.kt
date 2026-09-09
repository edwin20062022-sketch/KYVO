package com.kyvo.app.feature.mealshare.presentation

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.kyvo.app.core.designsystem.component.KyvoCard
import com.kyvo.app.core.designsystem.component.KyvoPrimaryButton
import com.kyvo.app.core.designsystem.component.KyvoSecondaryButton
import com.kyvo.app.feature.home.domain.model.MealItem
import com.kyvo.app.feature.home.domain.model.MealType
import com.kyvo.app.feature.mealshare.domain.model.MealShareDraft

@Composable
fun MealShareMealBuilderScreen(
    draft: MealShareDraft,
    onMealTypeSelected: (MealType) -> Unit,
    onAddFood: () -> Unit,
    onUpdatePortion: (String, Double) -> Unit,
    onRemoveFood: (String) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver")
                }
                Text("Construye tu comida", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
            }
        }
        item {
            if (!draft.photoUri.isNullOrBlank()) {
                AsyncImage(
                    model = Uri.parse(draft.photoUri),
                    contentDescription = "Fotografía de la comida",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(190.dp).padding(horizontal = 20.dp),
                )
            }
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Text("¿Qué tipo de comida es?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MealType.entries.forEach { type ->
                        val selected = type == draft.mealType
                        Surface(
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).clickable { onMealTypeSelected(type) },
                        ) {
                            Text(type.label, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 8.dp, vertical = 14.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
        item {
            KyvoCard(Modifier.padding(horizontal = 20.dp)) {
                Text("Resumen nutricional", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${draft.calories} kcal", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    SummaryMetric("Proteína", "${draft.protein} g")
                    SummaryMetric("Carbohidratos", "${draft.carbohydrates} g")
                    SummaryMetric("Grasas", "${draft.fat} g")
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Alimentos asociados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                TextButton(onClick = onAddFood, modifier = Modifier.height(48.dp)) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Text("Agregar alimento")
                }
            }
        }
        if (draft.items.isEmpty()) {
            item {
                KyvoCard(Modifier.padding(horizontal = 20.dp)) {
                    Text("Aún no has agregado alimentos", style = MaterialTheme.typography.bodyLarge)
                    Text("Asocia manualmente los alimentos que aparecen en tu fotografía.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
                    KyvoSecondaryButton("Agregar alimento", onAddFood, Modifier.padding(top = 14.dp))
                }
            }
        } else {
            items(draft.items, key = MealItem::id) { item ->
                MealShareDraftItemRow(item, onUpdatePortion, onRemoveFood)
            }
        }
        item {
            KyvoPrimaryButton(
                text = "Continuar",
                onClick = onContinue,
                enabled = draft.items.isNotEmpty(),
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }
    }
}

@Composable
private fun MealShareDraftItemRow(item: MealItem, onUpdatePortion: (String, Double) -> Unit, onRemoveFood: (String) -> Unit) {
    var editing by rememberSaveable(item.id) { mutableStateOf(false) }
    var amount by rememberSaveable(item.id) { mutableStateOf(item.quantity.toString()) }
    KyvoCard(Modifier.padding(horizontal = 20.dp), contentPadding = PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("${item.calories} kcal · ${item.protein} g proteína", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
            }
            IconButton(onClick = { editing = !editing }, modifier = Modifier.size(48.dp)) { Icon(Icons.Outlined.Edit, contentDescription = "Editar porción") }
            IconButton(onClick = { onRemoveFood(item.id) }, modifier = Modifier.size(48.dp)) { Icon(Icons.Outlined.Delete, contentDescription = "Eliminar alimento") }
        }
        if (editing) {
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Cantidad") }, suffix = { Text(item.unit) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
                TextButton(onClick = { amount.replace(',', '.').toDoubleOrNull()?.takeIf { it > 0 }?.let { onUpdatePortion(item.id, it); editing = false } }, modifier = Modifier.height(48.dp)) { Text("Guardar") }
            }
        } else {
            Text("${item.quantity} ${item.unit} · ${item.carbohydrates} g carbos · ${item.fat} g grasas", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@Composable
private fun SummaryMetric(label: String, value: String) {
    Column {
        Text(label, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
        Text(value, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
    }
}
