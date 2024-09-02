package com.grex.vyay

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.grex.vyay.ui.components.PieChartData
import com.grex.vyay.ui.theme.CustomColors
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(activity: MainActivity, padding: PaddingValues) {

    val applicationContext: Context = VyayApp.instance.applicationContext
    val database: AppDatabase = AppDatabase.getDatabase(applicationContext)
    val appDao: AppDao = database.appDao()
    val chartData = remember { mutableListOf<PieChartData>() }
    val userPreferences = UserPreferences(LocalContext.current)
    val userName = userPreferences.getUserName()

    val systemUiController = rememberSystemUiController()
    val monthExpense = remember {
        mutableDoubleStateOf(0.0)
    }

    DisposableEffect(systemUiController) {
        systemUiController.setStatusBarColor(
            color = CustomColors.backgroundPrimaryTop,
            darkIcons = false // Set to false for light icons
        )
        systemUiController.setNavigationBarColor(
            color = CustomColors.backgroundPrimaryTop,
            darkIcons = false // Set to false for light icons
        )
        onDispose {}
    }

    LaunchedEffect(Unit) {
        monthExpense.doubleValue = appDao.getCurrentMonthExpense()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = CustomColors.backgroundPrimaryTop,
                    titleContentColor = CustomColors.onPrimary,
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
//                    .background(
//                        brush = Brush.verticalGradient(
//                            colors = listOf(
//                                CustomColors.backgroundPrimaryTop,
//                                CustomColors.backgroundPrimaryBottom
//                            )
//                        )
//                    )
                    .background(CustomColors.backgroundPrimaryBottom)
                    .fillMaxSize(),
                contentAlignment = Alignment.TopStart
            ) {
                val painter = painterResource(id = R.drawable.dawnpeaks)
                Image(
                    painter = painter,
                    contentDescription = "Dawn Background",
                    contentScale = ContentScale.FillHeight,
                    modifier = Modifier
                        .fillMaxSize()
//                        .offset(x = (1 * painter.intrinsicSize.width * 0.1f).dp)
//                        .graphicsLayer { alpha = 0.15F }
//                        .drawWithContent {
//                            drawContent()
//                            drawRect(
//                                brush = Brush.verticalGradient(
//                                    0.0f to Color.Transparent,
//                                    0.1f to Color.Gray,
//                                    0.2f to Color.Black,
//                                    0.6f to Color.Gray,
//                                    0.8f to Color.Transparent
//                                ),
//                                blendMode = BlendMode.DstIn
//                            )
//                        }
//                        .blur(radius = 4.dp)
                )
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(start = 24.dp, end = 24.dp)
                        .fillMaxSize()
                ) {
                    Spacer(modifier = Modifier.height(36.dp))
                    Row {
                        Column {
                            Text(
                                text = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                                    .format(monthExpense.doubleValue),
                                color = CustomColors.primary,
                                style = MaterialTheme.typography.headlineLarge,
                                textAlign = TextAlign.Left
                            )
                            Text(
                                text = "Expenses this month",
                                color = CustomColors.onPrimaryDim,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Left
                            )
                        }
                    }

//                    Greeting(userName)
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