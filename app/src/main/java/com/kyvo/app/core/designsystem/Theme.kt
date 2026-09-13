package com.kyvo.app.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = KyvoColors.PurplePrimary,
    onPrimary = KyvoColors.Surface,
    primaryContainer = KyvoColors.PurpleSoft,
    onPrimaryContainer = KyvoColors.PurpleDeep,
    secondary = KyvoColors.PurpleAccent,
    background = KyvoColors.Canvas,
    onBackground = KyvoColors.Ink,
    surface = KyvoColors.Surface,
    onSurface = KyvoColors.Ink,
    surfaceVariant = KyvoColors.PurpleSoft,
    onSurfaceVariant = KyvoColors.Slate,
    outline = KyvoColors.Outline,
    error = KyvoColors.Error,
)

private val DarkColors = darkColorScheme(
    primary = KyvoColors.PurpleAccent,
    onPrimary = KyvoColors.DarkCanvas,
    primaryContainer = KyvoColors.PurpleDeep,
    onPrimaryContainer = KyvoColors.DarkOnSurface,
    background = KyvoColors.DarkCanvas,
    onBackground = KyvoColors.DarkOnSurface,
    surface = KyvoColors.DarkSurface,
    onSurface = KyvoColors.DarkOnSurface,
    surfaceVariant = KyvoColors.PurpleDeep,
    onSurfaceVariant = KyvoColors.Outline,
    outline = KyvoColors.Slate,
    error = KyvoColors.Error,
)

object KyvoTheme {
    val spacing: KyvoSpacing
        @Composable get() = LocalKyvoSpacing.current
}

@Composable
fun KyvoTheme(
    darkTheme: Boolean = false,
    reduceBrightnessInDarkMode: Boolean = true,
    highContrast: Boolean = false,
    content: @Composable () -> Unit,
) {
    val baseColors = if (darkTheme) DarkColors else LightColors
    val colors = baseColors.copy(
        onSurface = if (highContrast) baseColors.onBackground else baseColors.onSurface,
        onSurfaceVariant = if (highContrast) baseColors.onBackground else baseColors.onSurfaceVariant,
        primary = if (darkTheme && reduceBrightnessInDarkMode) baseColors.primary.copy(alpha = 0.88f) else baseColors.primary,
    )
    CompositionLocalProvider(LocalKyvoSpacing provides KyvoSpacing()) {
        val view = LocalView.current
        if (!view.isInEditMode) {
            SideEffect {
                val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
        MaterialTheme(
            colorScheme = colors,
            typography = KyvoTypography,
            shapes = KyvoShapes,
            content = content,
        )
    }
}
