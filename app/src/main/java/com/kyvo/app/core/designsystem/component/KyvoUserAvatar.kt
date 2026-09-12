package com.kyvo.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.kyvo.app.core.designsystem.KyvoColors

@Composable
fun KyvoUserAvatar(
    name: String?,
    avatarUrl: String?,
    avatarVersion: String? = null,
    size: Dp = 52.dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val sizedModifier = modifier.size(size).clip(CircleShape).then(
        if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    )
    val initial = name?.trim()?.firstOrNull()?.uppercase().orEmpty()
    val displayUrl = if (!avatarUrl.isNullOrBlank()) {
        if (!avatarVersion.isNullOrBlank()) "$avatarUrl?v=$avatarVersion" else avatarUrl
    } else null
    Box(sizedModifier.background(KyvoColors.PurpleSoft), contentAlignment = Alignment.Center) {
        if (!displayUrl.isNullOrBlank()) {
            AsyncImage(
                model = displayUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        } else if (initial.isNotBlank()) {
            Text(
                text = initial,
                color = KyvoColors.PurplePrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        } else {
            Icon(
                Icons.Outlined.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(size * 0.5f),
            )
        }
    }
}
