package com.kyvo.app.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyvo.app.R

private const val KyvoLogoAspectRatio = 457f / 427f

@Composable
fun KyvoBrandLockup(
    modifier: Modifier = Modifier,
    horizontal: Boolean = false,
    markSize: Dp = 84.dp,
) {
    val logoHeight = markSize * if (horizontal) 1.15f else 1.45f
    Image(
        painter = painterResource(R.drawable.kyvo_logo),
        contentDescription = stringResource(R.string.kyvo_logo_description),
        contentScale = ContentScale.Fit,
        modifier = modifier
            .height(logoHeight)
            .aspectRatio(KyvoLogoAspectRatio),
    )
}
