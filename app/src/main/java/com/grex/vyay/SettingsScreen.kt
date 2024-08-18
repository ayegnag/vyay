package com.grex.vyay

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.grex.vyay.ui.theme.backgroundPrimaryBottom
import com.grex.vyay.ui.theme.backgroundPrimaryTop


@Composable
fun SettingsScreen(activity: MainActivity, padding: PaddingValues, viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val systemUiController = rememberSystemUiController()
    DisposableEffect(systemUiController) {
        systemUiController.setStatusBarColor(
            color = backgroundPrimaryTop,
            darkIcons = false // Set to false for light icons
        )
        systemUiController.setNavigationBarColor(
            color = backgroundPrimaryTop,
            darkIcons = false // Set to false for light icons
        )
        onDispose {}
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundPrimaryTop,
                        backgroundPrimaryBottom
                    )
                )
            )
            .padding(padding)
    ) {
        Column(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp)
        ) {

            Text(
                text = "Reset Data Options",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(bottom = 16.dp),
            )
            Button(
                onClick = {
                    userPreferences.resetUserPreference()
                    val toast = Toast.makeText(context, "Profile data erased!", Toast.LENGTH_SHORT)
                    toast.show()
                }
            ) {
                val iconWidth = 24.dp
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = "drawable icons",
                    tint = Color.Unspecified
                )
                Text(
                    "Reset Profile",
                    modifier = Modifier
                        .padding(start = 12.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.deleteAllMessages()
                    val toast = Toast.makeText(context, "SMS DB records erased!", Toast.LENGTH_SHORT)
                    toast.show()
                }
            ) {
                val iconWidth = 24.dp
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = "drawable icons",
                    tint = Color.Unspecified
                )
                Text(
                    "Reset SMS Data",
                    modifier = Modifier
                        .padding(start = 12.dp)
                )
            }
        }
    }
}