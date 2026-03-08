package com.emulnk.model

data class GalleryIndex(
    val consoles: List<GalleryConsole>
)

data class GalleryConsole(
    val id: String,
    val games: List<GalleryGame>
)

data class GalleryGame(
    val profileId: String,
    val name: String,
    val console: String,
    val themes: List<GalleryTheme>,
    val hasWidgets: Boolean = false
)

/** console, profileId, gameName are denormalized from parent GalleryConsole/GalleryGame for flat search/filtering. */
data class GalleryTheme(
    val id: String,
    val name: String,
    val author: String,
    val version: String?,
    val description: String,
    val type: String,
    val tags: List<String>,
    val minAppVersion: Int,
    val previewUrl: String?,
    val console: String,
    val profileId: String,
    val gameName: String
)
