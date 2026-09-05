package com.kyvo.app.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyvo.app.core.designsystem.KyvoBrushes
import com.kyvo.app.core.designsystem.KyvoColors

@Composable
fun KyvoBrandLockup(
    modifier: Modifier = Modifier,
    horizontal: Boolean = false,
    markSize: Dp = 84.dp,
) {
    if (horizontal) {
        Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
            KyvoMark(size = markSize)
            Spacer(Modifier.width(12.dp))
            KyvoWordmark()
        }
    } else {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            KyvoMark(size = markSize)
            KyvoWordmark()
        }
    }
}

@Composable
private fun KyvoMark(size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val path = Path().apply {
            moveTo(this@Canvas.size.width * 0.23f, this@Canvas.size.height * 0.12f)
            lineTo(this@Canvas.size.width * 0.40f, this@Canvas.size.height * 0.12f)
            lineTo(this@Canvas.size.width * 0.40f, this@Canvas.size.height * 0.48f)
            lineTo(this@Canvas.size.width * 0.76f, this@Canvas.size.height * 0.14f)
            lineTo(this@Canvas.size.width * 0.93f, this@Canvas.size.height * 0.08f)
            lineTo(this@Canvas.size.width * 0.58f, this@Canvas.size.height * 0.52f)
            lineTo(this@Canvas.size.width * 0.88f, this@Canvas.size.height * 0.86f)
            lineTo(this@Canvas.size.width * 0.66f, this@Canvas.size.height * 0.92f)
            lineTo(this@Canvas.size.width * 0.46f, this@Canvas.size.height * 0.65f)
            lineTo(this@Canvas.size.width * 0.34f, this@Canvas.size.height * 0.82f)
            lineTo(this@Canvas.size.width * 0.12f, this@Canvas.size.height * 0.94f)
            lineTo(this@Canvas.size.width * 0.28f, this@Canvas.size.height * 0.58f)
            close()
        }
        drawPath(path = path, brush = KyvoBrushes.PrimaryAction)
        rotate(degrees = -10f, pivot = Offset(this@Canvas.size.width * .3f, this@Canvas.size.height * .18f)) {
            drawRect(
                color = KyvoColors.PurpleAccent,
                topLeft = Offset(this@Canvas.size.width * .13f, this@Canvas.size.height * .05f),
                size = Size(this@Canvas.size.width * .10f, this@Canvas.size.height * .28f),
            )
        }
    }
}

@Composable
private fun KyvoWordmark() {
    Text(
        text = "KYVO",
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 29.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 8.sp,
    )
}
