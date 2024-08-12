package com.grex.vyay

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Onboarding : Screen("onboarding", label = "Onboarding", icon = Icons.Rounded.Star)
    data object Splash : Screen(route = "splash", label = "-", icon = Icons.Rounded.Home)
    data object Home : Screen(route = "home", label = "Home", icon = Icons.Rounded.Home)
    data object Reports : Screen(route = "reports", label = "Reports", icon = Icons.Rounded.List)
    data object Settings : Screen("settings", label = "Settings", icon = Icons.Rounded.Settings)
}
