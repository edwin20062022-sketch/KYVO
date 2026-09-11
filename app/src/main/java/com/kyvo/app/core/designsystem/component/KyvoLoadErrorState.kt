package com.kyvo.app.core.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kyvo.app.core.designsystem.LoadFailureKind

@Composable
fun KyvoLoadErrorState(kind: LoadFailureKind, modifier: Modifier = Modifier, onRetry: () -> Unit) {
    val offline = kind == LoadFailureKind.Offline
    KyvoStateView(
        icon = if (offline) Icons.Outlined.Search else Icons.Outlined.Info,
        title = if (offline) "Sin conexión" else "Error de carga",
        message = if (offline) "No pudimos conectarnos. Revisa tu conexión a internet e inténtalo nuevamente." else "No pudimos cargar la información. Inténtalo nuevamente.",
        modifier = modifier,
        actionLabel = "Reintentar",
        onAction = onRetry,
    )
}
