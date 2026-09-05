package com.kyvo.app.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

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
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalKyvoSpacing provides KyvoSpacing()) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = KyvoTypography,
            shapes = KyvoShapes,
            content = content,
        )
    }
}

