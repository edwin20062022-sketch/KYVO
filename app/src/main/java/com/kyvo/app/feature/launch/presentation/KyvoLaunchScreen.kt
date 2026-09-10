package com.kyvo.app.feature.launch.presentation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import com.kyvo.app.R

const val KYVO_LAUNCH_COVER_TAG = "kyvo_launch_cover"

@Composable
fun KyvoLaunchScreen(
    @DrawableRes launchCoverRes: Int = R.drawable.kyvo_launch_cover,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(launchCoverRes),
        contentDescription = "Portada de apertura KYVO",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .fillMaxSize()
            .semantics {
                testTag = KYVO_LAUNCH_COVER_TAG
                contentDescription = "Portada de apertura KYVO"
            },
    )
}
