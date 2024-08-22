package com.grex.vyay

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    data object Onboarding : Screen("onboarding", label = "Onboarding", icon = Icons.Rounded.Star, selectedIcon = Icons.Rounded.Star)
    data object Splash : Screen(route = "splash", label = "-", icon = Icons.Outlined.Home, selectedIcon = Icons.Filled.Home)
    data object Home : Screen(route = "home", label = "Home", icon = Icons.Outlined.Home, selectedIcon = Icons.Filled.Home)
    data object Reports : Screen(route = "reports", label = "Reports", icon = Icons.Outlined.DateRange, selectedIcon = Icons.Filled.DateRange)
    data object Statements : Screen(route = "statements/{yearMonth}", label = "Statements", icon = Icons.AutoMirrored.Outlined.List, selectedIcon = Icons.AutoMirrored.Filled.List) {
        fun createRoute(yearMonth: String) = "statements/$yearMonth"
    }
    data object TransactionDetails : Screen(route = "transaction/{transactionId}/{isManual}", label = "Transaction Details", icon = Icons.Outlined.Edit, selectedIcon = Icons.Filled.Edit) {
        fun createRoute(transactionId: Int, isManual: Boolean) = "transaction/${transactionId}/${isManual}"
    }
    data object Settings : Screen("settings", label = "Settings", icon = Icons.Outlined.Settings, selectedIcon = Icons.Filled.Settings)
}
