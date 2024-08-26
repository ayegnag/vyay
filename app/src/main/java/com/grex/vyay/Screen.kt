package com.grex.vyay

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.sharp.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.sharp.Add
import androidx.compose.material.icons.sharp.DateRange
import androidx.compose.material.icons.sharp.Edit
import androidx.compose.material.icons.sharp.Home
import androidx.compose.material.icons.sharp.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    data object Onboarding : Screen("onboarding", label = "Onboarding", icon = Icons.Rounded.Star, selectedIcon = Icons.Rounded.Star)
    data object Splash : Screen(route = "splash", label = "-", icon = Icons.Sharp.Home, selectedIcon = Icons.Filled.Home)
    data object Home : Screen(route = "home", label = "Home", icon = Icons.Sharp.Home, selectedIcon = Icons.Filled.Home)
    data object Reports : Screen(route = "reports", label = "Reports", icon = Icons.Sharp.DateRange, selectedIcon = Icons.Filled.DateRange)
    data object Statements : Screen(route = "statements/{yearMonth}", label = "Statements", icon = Icons.AutoMirrored.Sharp.List, selectedIcon = Icons.AutoMirrored.Filled.List) {
        fun createRoute(yearMonth: String) = "statements/$yearMonth"
    }
    data object TransactionDetails : Screen(route = "transaction/{transactionId}/{isManual}", label = "Transaction Details", icon = Icons.Sharp.Edit, selectedIcon = Icons.Filled.Edit) {
        fun createRoute(transactionId: Int, isManual: Boolean) = "transaction/${transactionId}/${isManual}"
    }
    data object AddTransaction : Screen("add-transaction", label = "Add Transaction", icon = Icons.Sharp.Add, selectedIcon = Icons.Filled.Add)
    data object Settings : Screen("settings", label = "Settings", icon = Icons.Sharp.Settings, selectedIcon = Icons.Filled.Settings)
}
