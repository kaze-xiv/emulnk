package com.emulnk.model

import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

enum class ScreenTarget {
    @SerializedName("primary") PRIMARY,
    @SerializedName("secondary") SECONDARY;
}

/** Widget definition from the remote store; converted to WidgetConfig for overlay layout and rendering. */
data class StoreWidget(
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
    val screenTarget: ScreenTarget? = ScreenTarget.PRIMARY,
    val description: String = "",
    val previewUrl: String? = null,
    val tags: List<String> = emptyList()
) {
    companion object {
        val LIST_TYPE: java.lang.reflect.Type = object : TypeToken<List<StoreWidget>>() {}.type
    }
}

fun StoreWidget.toWidgetConfig(): WidgetConfig = WidgetConfig(
    id = id, label = label, src = src,
    defaultWidth = defaultWidth, defaultHeight = defaultHeight,
    defaultX = defaultX, defaultY = defaultY,
    resizable = resizable, transparent = transparent,
    minWidth = minWidth, minHeight = minHeight,
    screenTarget = screenTarget
)
