package com.grex.vyay

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    data object Onboarding : Screen("onboarding", label = "Onboarding", icon = Icons.Rounded.Star, selectedIcon = Icons.Rounded.Star)
    data object Splash : Screen(route = "splash", label = "-", icon = Icons.Rounded.Home, selectedIcon = Icons.Filled.Home)
    data object Home : Screen(route = "home", label = "Home", icon = Icons.Rounded.Home, selectedIcon = Icons.Filled.Home)
    data object Reports : Screen(route = "reports", label = "Reports", icon = Icons.Rounded.DateRange, selectedIcon = Icons.Filled.DateRange)
    data object Statements : Screen(route = "statements/{yearMonth}", label = "Statements", icon = Icons.AutoMirrored.Rounded.List, selectedIcon = Icons.AutoMirrored.Filled.List) {
        fun createRoute(yearMonth: String) = "statements/$yearMonth"
    }
    data object TransactionDetails : Screen(route = "transaction/{transactionId}/{isManual}", label = "Transaction Details", icon = Icons.Rounded.Edit, selectedIcon = Icons.Filled.Edit) {
        fun createRoute(transactionId: Int, isManual: Boolean) = "transaction/${transactionId}/${isManual}"
    }
    data object AddTransaction : Screen("add-transaction", label = "Add Transaction", icon = Icons.Rounded.Add, selectedIcon = Icons.Filled.Add)
    data object Settings : Screen("settings", label = "Settings", icon = Icons.Rounded.Settings, selectedIcon = Icons.Filled.Settings)
}
