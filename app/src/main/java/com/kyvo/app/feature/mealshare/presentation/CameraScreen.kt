package com.kyvo.app.feature.mealshare.presentation

import android.annotation.SuppressLint
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.compose.foundation.clickable
import java.io.File
import java.util.UUID

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun CameraScreen(onBack: () -> Unit, onPhotoCaptured: (File) -> Unit = {}) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var error by remember { mutableStateOf<String?>(null) }
    var ready by remember { mutableStateOf(false) }
    var capturing by remember { mutableStateOf(false) }
    var captureError by remember { mutableStateOf<String?>(null) }
    val previewView = remember { PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }
    val storyResolutionSelector = remember {
        ResolutionSelector.Builder()
            .setAspectRatioStrategy(AspectRatioStrategy(AspectRatio.RATIO_16_9, AspectRatioStrategy.FALLBACK_RULE_AUTO))
            .build()
    }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setResolutionSelector(storyResolutionSelector)
            .build()
    }
    LaunchedEffect(previewView, lifecycleOwner) {
        runCatching {
            val provider = ProcessCameraProvider.getInstance(context).get()
            provider.unbindAll()
            imageCapture.targetRotation = previewView.display?.rotation ?: android.view.Surface.ROTATION_0
            val preview = Preview.Builder()
                .setResolutionSelector(storyResolutionSelector)
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }
            provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
            ready = true
        }.onFailure { error = it.message ?: "No pudimos iniciar la cámara." }
    }
    DisposableEffect(lifecycleOwner) { onDispose { runCatching { ProcessCameraProvider.getInstance(context).get().unbindAll() } } }
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxWidth().aspectRatio(9f / 16f).align(Alignment.Center),
        )
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart).padding(20.dp)) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Cancelar", tint = Color.White) }
        Text(captureError ?: if (error != null) error!! else if (ready) "Toma una foto de tu comida" else "Iniciando cámara…", color = Color.White, modifier = Modifier.align(Alignment.TopCenter).padding(top = 28.dp))
        Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 38.dp).size(76.dp).background(if (capturing || !ready) Color.Gray else Color.White.copy(alpha = .95f), androidx.compose.foundation.shape.CircleShape).clickable(enabled = ready && !capturing) {
            capturing = true
            val directory = File(context.cacheDir, "meal_share").apply { mkdirs() }
            val file = File(directory, "meal_share_${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg")
            val options = ImageCapture.OutputFileOptions.Builder(file).build()
            imageCapture.takePicture(options, ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) { capturing = false; onPhotoCaptured(file) }
                override fun onError(exception: ImageCaptureException) { capturing = false; file.delete(); captureError = exception.message ?: "No pudimos guardar la fotografía." }
            })
        })
    }
}
