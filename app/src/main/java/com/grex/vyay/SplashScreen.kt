package com.grex.vyay

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.grex.vyay.ui.theme.ClayBeige
import com.grex.vyay.ui.theme.DarkwingPurple
import com.grex.vyay.ui.theme.SlateBlue

@Composable
fun SplashScreen(
    smsAnalysisService: SmsAnalysisService,
    smsPermissionHandler: SmsPermissionHandler,
    onLoadingComplete: () -> Unit
//    checkAndRequestPermission: (() -> Unit) -> Unit
) {
    val context = LocalContext.current
    val currentProgress by smsAnalysisService.progress.collectAsState()
    var isPermissionGranted by remember { mutableStateOf(smsPermissionHandler.checkSmsPermission()) }
    val systemUiController = rememberSystemUiController()
    val unifiedBackgroundColor = DarkwingPurple
    var showSmsPermissionDialog by remember { mutableStateOf(false) }

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

    LaunchedEffect(key1 = isPermissionGranted) {
        Log.d("INIT", "READ SMS Permission: $isPermissionGranted")
        if (isPermissionGranted) {
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(unifiedBackgroundColor)
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
                contentDescription = "Vyay Logo")
            if (!showSmsPermissionDialog) {
                LinearProgressIndicator(
                    progress = currentProgress,
                    modifier = Modifier.fillMaxWidth(),
                    color = ClayBeige,
                    trackColor = SlateBlue
                )
            }


        }
        if (showSmsPermissionDialog) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(all = 24.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Text(
                    text = "Permission to read SMS is required to generate expense reports. Without this the app won't function. All data remains within the phone.",
                    modifier = Modifier
                        .padding(20.dp),
                    textAlign = TextAlign.Justify
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            smsPermissionHandler.requestSmsPermission(context) {
                                isPermissionGranted = true
                                Log.d("INIT", "READ SMS Permission Granted!")
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                    ) {
                        Text("Got it!")
                    }
                }
            }
        }
    }
}

//suspend fun loadProgress(updateProgress: (Float) -> Unit, onComplete: () -> Unit) {
//    for (i in 1..100) {
//        updateProgress(i.toFloat() / 100)
//        delay(20.milliseconds)
//        if (i >= 100 ) {
//            onComplete()
//        }
//    }
//}

@Preview
@Composable
fun PermissionCard() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkwingPurple)
            .padding(bottom = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(all = 24.dp)
                .align(Alignment.BottomCenter)

        ) {
            Text(
                text = "Permission to read SMS is required to generate expense reports. Without this the app won't function. All data remains within the phone.",
                modifier = Modifier
                    .padding(20.dp),
                textAlign = TextAlign.Justify,
            )
            Box(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        print("Okay")
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                ) {
                    Text("Got it!")
                }
            }
        }
    }
}