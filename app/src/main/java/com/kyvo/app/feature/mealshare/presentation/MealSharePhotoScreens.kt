package com.kyvo.app.feature.mealshare.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun MealShareSourceScreen(
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Comparte tu comida")
        Spacer(Modifier.height(16.dp))
        Button(onClick = onCamera, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Tomar foto") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onGallery, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Elegir de galería") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Atrás") }
    }
}

@Composable
fun MealSharePhotoPickerRoute(
    onSelected: (Uri) -> Unit,
    onBack: () -> Unit,
) {
    var launched by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        launched = false
        if (uri != null) onSelected(uri) else onBack()
    }
    LaunchedEffect(Unit) {
        if (!launched) {
            launched = true
            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Selecciona una foto")
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Cancelar") }
    }
}

@Composable
fun MealSharePhotoPreviewScreen(
    photoUri: String?,
    onConfirm: () -> Unit,
    onRepeat: () -> Unit,
    onChange: () -> Unit,
    onBack: () -> Unit,
) {
    if (photoUri.isNullOrBlank()) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text("No se encontró la fotografía")
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onChange, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Elegir otra foto") }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Volver") }
        }
        return
    }

    var loadError by remember(photoUri) { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().background(Color.Black).padding(20.dp)) {
        if (!loadError) {
            AsyncImage(
                model = Uri.parse(photoUri),
                contentDescription = "Vista previa de la comida",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().weight(1f),
                onError = { loadError = true },
            )
        } else {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text("No se pudo cargar la fotografía", color = Color.White)
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onConfirm, enabled = !loadError, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Usar esta foto") }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onRepeat, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Repetir / cambiar") }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Atrás") }
    }
}
