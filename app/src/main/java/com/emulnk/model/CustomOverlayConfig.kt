package com.emulnk.model

/** User's per-profile widget selection, persisted separately from StoreWidget definitions and WidgetConfig layout state. */
data class CustomOverlayConfig(
    val profileId: String,
    val selectedWidgetIds: List<String>,
    val console: String? = null,
    val screenAssignments: Map<String, ScreenTarget> = emptyMap()
)
