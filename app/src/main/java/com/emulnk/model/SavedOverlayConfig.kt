package com.emulnk.model

import java.io.File

data class SavedOverlayConfig(
    val id: String,
    val name: String,
    val profileId: String,
    val console: String? = null,
    val selectedWidgetIds: List<String>,
    val screenAssignments: Map<String, ScreenTarget> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis(),
    val iconPath: String? = null
) {
    companion object {
        /** Prefix for user-created overlay IDs */
        const val ID_PREFIX = "uo_"

        /** Returns the icon file path for a given user overlay ID */
        fun iconPath(id: String): String = "user_overlays/${id}_icon.png"

        /** Resolves the preview image: user icon if it exists, otherwise the theme's preview.png */
        fun resolvePreviewFile(rootPath: String, themeId: String): File {
            val userIcon = File(rootPath, iconPath(themeId))
            return if (userIcon.exists()) userIcon else File(rootPath, "themes/$themeId/preview.png")
        }
    }
}
