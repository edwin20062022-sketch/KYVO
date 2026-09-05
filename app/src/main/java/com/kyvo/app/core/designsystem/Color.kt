package com.kyvo.app.core.designsystem

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object KyvoColors {
    val PurpleDeep = Color(0xFF1B0A3D)
    val PurplePrimary = Color(0xFF6D32F5)
    val PurpleAccent = Color(0xFF8B5CF6)
    val PurpleSoft = Color(0xFFF1EBFF)

    val Ink = Color(0xFF101323)
    val Slate = Color(0xFF667085)
    val Outline = Color(0xFFE4E2EA)
    val Canvas = Color(0xFFFBFAFE)
    val Surface = Color(0xFFFFFFFF)

    val Protein = Color(0xFF6D32F5)
    val Carbohydrate = Color(0xFF2CB5A8)
    val Fat = Color(0xFFFFB000)
    val Success = Color(0xFF17B26A)
    val Warning = Color(0xFFF79009)
    val Error = Color(0xFFF04438)

    val DarkCanvas = Color(0xFF0F0B18)
    val DarkSurface = Color(0xFF191326)
    val DarkOnSurface = Color(0xFFF5F2FA)
}

object KyvoBrushes {
    val PrimaryAction = Brush.horizontalGradient(
        listOf(KyvoColors.PurplePrimary, KyvoColors.PurpleAccent, KyvoColors.PurplePrimary),
    )
}

