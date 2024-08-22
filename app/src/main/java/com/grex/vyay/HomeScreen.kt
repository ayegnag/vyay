package com.grex.vyay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.grex.vyay.ui.theme.Grey
import com.grex.vyay.ui.theme.backgroundPrimaryBottom
import com.grex.vyay.ui.theme.backgroundPrimaryTop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(activity: MainActivity, padding: PaddingValues) {

    val chartData = remember { mutableListOf<PieChartData>() }
    val userPreferences = UserPreferences(LocalContext.current)
    val userName = userPreferences.getUserName()

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

//    LaunchedEffect(activity) {
//        if (isPermissionGranted) {
//        smsPermissionHandler.readAndStoreSms()
//        smsCount = smsPermissionHandler.getSmsCount()
//        totalSmsCount = smsPermissionHandler.getTotalSmsCount()
//        chartData.clear()
//        chartData.addAll(listOf(
//            PieChartData(value = smsCount.toFloat(), color = MistyRose),
//            PieChartData(value = totalSmsCount.toFloat(), color = ColumbiaBlue)
//        ))
//        }
//    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = backgroundPrimaryTop,
                    titleContentColor = Grey,
                ),
                title = {
                    Text(
                        "Vyay",
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
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
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                backgroundPrimaryTop,
                                backgroundPrimaryBottom
                            )
                        )
                    )
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(padding)
                ) {
                    Greeting(userName)
//            if (isPermissionGranted) {
//                Row {
//                    TotalSMSCount(totalSmsCount)
//                    SMSCount(smsCount)
//                }
//                PieChart(data = chartData, modifier = Modifier.height(300.dp))
//            } else {
//                Button(
//                    onClick = {
//                        smsPermissionHandler.checkSmsPermission {
//                            isPermissionGranted = true
//                        }
//                    }
//                ) {
//                    Text("Request SMS Permission")
//                }
//            }
                }
            }
        }
    )
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Vyay : Welcome - $name!",
        modifier = modifier
    )
}

@Composable
fun SMSCount(count: Int, modifier: Modifier = Modifier) {
    Text(
        text = "Bank SMS Count: $count",
        modifier = modifier
    )
}

@Composable
fun TotalSMSCount(count: Int, modifier: Modifier = Modifier) {
    Text(
        text = "Total SMS Count: $count",
        modifier = modifier
    )
}