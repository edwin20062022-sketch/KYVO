package com.kyvo.app.feature.foundation.presentation

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kyvo.app.core.designsystem.KyvoTheme
import com.kyvo.app.core.designsystem.component.KyvoCard

@Composable
fun FoundationRoute() {
    FoundationScreen()
}

@Composable
internal fun FoundationScreen(modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        BoxWithConstraints {
            val horizontalPadding = if (maxWidth < 360.dp) 16.dp else 24.dp
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = horizontalPadding, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(KyvoTheme.spacing.xl),
            ) {
                Text(
                    text = "KYVO",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = "Foundation lista",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = "Sistema de diseño, arquitectura y navegación base configurados. Login se implementará en la siguiente fase aprobada.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                KyvoCard(modifier = Modifier.fillMaxWidth()) {
                    Text("FUEL BALANCE", style = MaterialTheme.typography.labelLarge)
                    Text(
                        "Nutrición enfocada en atletas de gimnasio.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Preview(name = "Compact 320x640", widthDp = 320, heightDp = 640, showBackground = true)
@Composable
private fun CompactPreview() {
    KyvoTheme(darkTheme = false) { FoundationScreen() }
}

@Preview(name = "Standard 411x891", widthDp = 411, heightDp = 891, showBackground = true)
@Composable
private fun StandardPreview() {
    KyvoTheme(darkTheme = false) { FoundationScreen() }
}

@Preview(name = "Large 480x960", widthDp = 480, heightDp = 960, showBackground = true)
@Composable
private fun LargePreview() {
    KyvoTheme(darkTheme = false) { FoundationScreen() }
}

@Preview(
    name = "Dark",
    widthDp = 411,
    heightDp = 891,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun DarkPreview() {
    KyvoTheme(darkTheme = true) { FoundationScreen() }
}
