package com.kyvo.app.feature.settings.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

enum class NotificationPermissionStatus { GRANTED, DENIED, NOT_REQUIRED }

fun notificationPermissionStatus(context: Context): NotificationPermissionStatus = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
    NotificationPermissionStatus.NOT_REQUIRED
} else if (
    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED &&
    NotificationManagerCompat.from(context).areNotificationsEnabled()
) {
    NotificationPermissionStatus.GRANTED
} else {
    NotificationPermissionStatus.DENIED
}

fun openKyvoAppSettings(context: Context) {
    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")))
}
