package com.emulnk.model

// v2 index.json format
data class RepoIndexV2(
    val version: String,
    val games: List<RepoGame>
)

data class RepoGame(
    val profileId: String,
    val name: String,
    val console: String,
    val themes: List<RepoThemeV2>,
    val hasWidgets: Boolean = false
)

data class RepoThemeV2(
    val id: String,
    val name: String,
    val author: String = "Unknown",
    val version: String? = "1.0.0",
    val description: String = "",
    val type: String? = "theme",
    val tags: List<String> = emptyList(),
    val minAppVersion: Int? = 1,
    val previewUrl: String? = null
)
