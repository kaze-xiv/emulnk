package com.emulnk.model

/**
 * Defines a single widget in an overlay theme.
 * Mirrors the widget entries in theme.json.
 *
 * [screenTarget]: Preferred display for dual-screen devices. `null` defaults to PRIMARY
 * via [com.emulnk.model.ThemeConfig.resolvedScreenTarget]. Callers must handle `null`
 * (Gson bypasses Kotlin defaults, so deserialized values may be `null` even for non-null types).
 */
data class WidgetConfig(
    val id: String,
    val label: String,
    val src: String,
    val defaultWidth: Int,
    val defaultHeight: Int,
    val defaultX: Int = 0,
    val defaultY: Int = 0,
    val resizable: Boolean = true,
    val transparent: Boolean = true,
    val minWidth: Int = 60,
    val minHeight: Int = 60,
    val screenTarget: ScreenTarget? = null,
    val isBaseLayer: Boolean = false,
    val assetsPath: String? = null
)
