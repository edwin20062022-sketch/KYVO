package com.kyvo.app.feature.settings.data

import java.io.File

/** Removes only KYVO-generated Meal Share files in the app cache. Picker source files are external. */
fun clearOwnedMealShareCache(cacheDir: File) {
    val root = File(cacheDir, "meal_share").canonicalFile
    if (!root.isDirectory) return
    root.listFiles().orEmpty().forEach { child ->
        if (child.isFile && child.parentFile?.canonicalFile == root && child.name.startsWith("meal_share_")) child.delete()
        if (child.isDirectory && child.name == "rendered" && child.canonicalFile.parentFile == root) {
            child.listFiles().orEmpty().forEach { file ->
                if (file.isFile && (file.name.startsWith("meal_share_render_") || file.name.endsWith(".partial"))) file.delete()
            }
        }
    }
}
