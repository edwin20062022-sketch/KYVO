package com.kyvo.app.feature.mealshare.presentation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

sealed interface CameraPermissionState {
    data object Granted : CameraPermissionState
    data object NotRequested : CameraPermissionState
    data object Denied : CameraPermissionState
    data object PermanentlyDenied : CameraPermissionState
}

fun cameraPermissionState(context: Context, requested: Boolean): CameraPermissionState = when {
    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> CameraPermissionState.Granted
    !requested -> CameraPermissionState.NotRequested
    else -> CameraPermissionState.Denied
}

fun openCameraSettings(context: Context) {
    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")))
}

@Composable
fun CameraPermissionGate(onGranted: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var requested by remember { mutableStateOf(false) }
    var state by remember { mutableStateOf(cameraPermissionState(context, requested)) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        requested = true
        state = if (granted) CameraPermissionState.Granted else if (context is Activity && !ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.CAMERA)) CameraPermissionState.PermanentlyDenied else CameraPermissionState.Denied
    }
    LaunchedEffect(state) { if (state is CameraPermissionState.Granted) onGranted() }
    DisposableEffect(lifecycleOwner, requested) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) state = cameraPermissionState(context, requested)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(if (state is CameraPermissionState.PermanentlyDenied) "Activa el permiso de cámara en Ajustes." else "KYVO necesita acceso a la cámara para tomar tu foto.")
        Button(onClick = { if (state is CameraPermissionState.PermanentlyDenied) openCameraSettings(context) else launcher.launch(Manifest.permission.CAMERA) }, modifier = Modifier.padding(top = 16.dp)) { Text(if (state is CameraPermissionState.PermanentlyDenied) "Abrir Ajustes" else "Continuar") }
        Button(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) { Text("Cancelar") }
    }
}
