package com.grex.vyay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.grex.vyay.ui.theme.DarkwingPurple


@Composable
fun SettingsScreen(activity: MainActivity, padding: PaddingValues) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }

    val systemUiController = rememberSystemUiController()
    val unifiedBackgroundColor = DarkwingPurple
    DisposableEffect(systemUiController) {
        systemUiController.setNavigationBarColor(
            color = unifiedBackgroundColor,
            darkIcons = false // Set to false for light icons
        )
        systemUiController.setStatusBarColor(
            color = unifiedBackgroundColor,
            darkIcons = false // Set to false for light icons
        )
        onDispose {}
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(unifiedBackgroundColor)
            .padding(padding)
    ) {
        Text(text = "Settings", color = Color.Black)
    }
    Button(
        onClick = {
            userPreferences.resetUserPreference()
        }
    ) {
        Text("Reset Profile")
    }
}