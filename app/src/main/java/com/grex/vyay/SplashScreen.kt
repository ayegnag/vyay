package com.grex.vyay

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.grex.vyay.ui.theme.CustomColors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SplashScreen(
    smsAnalysisService: SmsAnalysisService,
    onLoadingComplete: () -> Unit
) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(Manifest.permission.READ_SMS)
    val showRationale = remember { mutableStateOf(false) }

    val currentProgress by smsAnalysisService.progress.collectAsState()

    val systemUiController = rememberSystemUiController()
    DisposableEffect(systemUiController) {
        systemUiController.setStatusBarColor(
            color = CustomColors.backgroundPrimaryTop,
            darkIcons = false // Set to false for light icons
        )
        systemUiController.setNavigationBarColor(
            color = CustomColors.backgroundPrimaryBottom,
            darkIcons = false // Set to false for light icons
        )
        onDispose {}
    }
    var showSmsPermissionDialog by remember { mutableStateOf(false) }

    val smsPermissionText = "This application needs access to your SMS to generate " +
            "expense reports. You data remains in your Phone."
    val smsDeclinedPermissionText = "It seems you have declined SMS permission. This application needs access to your SMS to generate " +
            "expense reports. You can enable permission from App Settings."

    LaunchedEffect(Unit) {
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }
    LaunchedEffect(permissionState.status.shouldShowRationale) {
        showRationale.value = permissionState.status.shouldShowRationale
    }
    LaunchedEffect(key1 = permissionState.status.isGranted) {
        Log.d("INIT", "READ SMS Permission: $permissionState.status.isGranted")
        if (permissionState.status.isGranted) {
            showSmsPermissionDialog = false
            smsAnalysisService.startAnalysis()
            smsAnalysisService.progress.collect { progress ->
                if (progress >= 1f) {
                    onLoadingComplete()
                }
            }
        } else {
            showSmsPermissionDialog = true
        }
    }

//    Compose ---------

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CustomColors.backgroundPrimaryTop,
                        CustomColors.backgroundPrimaryBottom
                    )
                )
            )
            .padding(bottom = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .widthIn(max = 80.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher),
                modifier = Modifier.size(80.dp),
                contentDescription = "Vyay Logo"
            )
            if (!showSmsPermissionDialog) {
                LinearProgressIndicator(
                    progress = { currentProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = CustomColors.primary,
                    trackColor = CustomColors.onSecondaryInactive,
                )
            }


        }
        if (!permissionState.status.isGranted) {
            PermissionCard(
                permissionText = smsPermissionText,
                declinedPermissionText = smsDeclinedPermissionText,
                isPermanentlyDeclined = showRationale.value,
                onOkClick = {
                    if (permissionState.status.shouldShowRationale) {
                        permissionState.launchPermissionRequest()
                    } else {
                        // Open app settings
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.fromParts("package", context.packageName, null)
                        context.startActivity(intent)
                    }
                }
            )
        }
    }
}

