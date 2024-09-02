package com.grex.vyay.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.grex.vyay.Screen
import com.grex.vyay.ui.theme.CustomColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopMenuBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    TopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = CustomColors.backgroundPrimaryTop,
            titleContentColor = CustomColors.onPrimaryInactive,
        ),
        title = {
            when (currentRoute) {
                Screen.Reports.route -> Text("Reports")
                Screen.Statements.route -> Text("Monthly Statements")
                Screen.Settings.route -> Text("Settings")
                Screen.Onboarding.route -> Text("Welcome")
                else -> Text(
                    "Vyay",
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ) // HomeScreen
            }
        },
        actions = {
            IconButton(onClick = { /* Handle menu click */ }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Menu"
                )
            }
        },
    )
}

fun getScreenTitle(route: NavDestination) {

}