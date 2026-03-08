package com.emulnk.ui.navigation

sealed class Screen {
    object Onboarding : Screen()
    object Home : Screen()
    object Gallery : Screen()
    object Overlay : Screen()
    data class Builder(val profileId: String, val console: String) : Screen()
}
