package com.grex.vyay

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.grex.vyay.ui.components.ExpenseBarChart
import com.grex.vyay.ui.theme.CustomColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    activity: MainActivity,
    padding: PaddingValues,
    isTransactionRecordUpdated: Boolean,
    onReportClick: (String) -> Unit,
    onAckUpdate: (Boolean) -> Unit
) {
    val smsAnalysisService = activity.smsAnalysisService
    var expenseData by remember { mutableStateOf<List<MonthlyTotal>>(emptyList()) }
    var incomeData by remember { mutableStateOf<List<MonthlyTotal>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val systemUiController = rememberSystemUiController()

    suspend fun fetchData() {
        isLoading = true
//        expenseData = smsAnalysisService.fetchMonthlyExpense()
//        incomeData = smsAnalysisService.fetchMonthlyIncome()
        expenseData = smsAnalysisService.getMonthlyExpenseFromDB()
        incomeData = smsAnalysisService.getMonthlyIncomeFromDB()
        isLoading = false
        onAckUpdate(false)
    }
    LaunchedEffect(Unit) {
        if (isTransactionRecordUpdated) {
            fetchData()
        } else {
//            expenseData = smsAnalysisService.getMonthlyExpense()
//            incomeData = smsAnalysisService.getMonthlyIncome()
            expenseData = smsAnalysisService.getMonthlyExpenseFromDB()
            incomeData = smsAnalysisService.getMonthlyIncomeFromDB()
            isLoading = false
        }
        Log.d("ChartExpData", expenseData.toString())
        Log.d("ChartExpData", incomeData.toString())
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

//        expenseData.forEach { item ->
//            Log.d("Expense Data", item.toString())
//        }
//        incomeData.forEach { item ->
//            Log.d("Income Data", item.toString())
//        }


    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = CustomColors.backgroundPrimaryTop,
                    titleContentColor = CustomColors.onPrimary,
                ),
                title = { Text("Reports") },
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
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
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
                    val painter = painterResource(id = R.drawable.moonlessvalley)
                    Image(
                        painter = painter,
                        contentDescription = "Dawn Background",
                        contentScale = ContentScale.FillHeight,
                        alignment = BiasAlignment(-0.35f, 1f),
                        modifier = Modifier
                            .scale(1f)
                            .fillMaxSize().blur(4.dp)
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
                    LazyColumn(
                        modifier = Modifier
                            .padding(start = 1.dp, end = 1.dp)
//                        .padding(start = 24.dp, end = 24.dp)
                            .fillMaxSize()
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = CustomColors.onTertiary.copy(alpha = 0.9f),
                                shadowElevation = 12.dp
                            ) {
                                Column(
//                            modifier = Modifier
                                ) {
                                    Text(
                                        text = "Monthly Expense Reports",
                                        color = CustomColors.onPrimary,
                                        modifier = Modifier
                                            .padding(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    ExpenseBarChart(
                                        expenses = expenseData,
                                        incomes = incomeData,
                                        onItemClick = { monthYear ->
                                            onReportClick(monthYear)
                                        })

                                    Spacer(modifier = Modifier.height(24.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Preview
@Composable
fun ReportsScreen() {
    val expenseData = listOf(MonthlyTotal(month="2021-10", totalAmount=61574.0f), MonthlyTotal(month="2021-11", totalAmount=49799.0f), MonthlyTotal(month="2021-12", totalAmount=39164.0f), MonthlyTotal(month="2022-01", totalAmount=118034.1f), MonthlyTotal(month="2022-02", totalAmount=84124.0f), MonthlyTotal(month="2022-03", totalAmount=141169.67f), MonthlyTotal(month="2022-04", totalAmount=126766.0f), MonthlyTotal(month="2022-05", totalAmount=75449.0f), MonthlyTotal(month="2022-06", totalAmount=40031.0f), MonthlyTotal(month="2022-07", totalAmount=258348.0f), MonthlyTotal(month="2022-08", totalAmount=161658.0f), MonthlyTotal(month="2022-09", totalAmount=450509.7f), MonthlyTotal(month="2022-10", totalAmount=36872.5f), MonthlyTotal(month="2022-11", totalAmount=53221.0f), MonthlyTotal(month="2022-12", totalAmount=321245.0f), MonthlyTotal(month="2023-01", totalAmount=53050.0f), MonthlyTotal(month="2023-02", totalAmount=256632.16f), MonthlyTotal(month="2023-03", totalAmount=91588.0f), MonthlyTotal(month="2023-04", totalAmount=124451.0f), MonthlyTotal(month="2023-05", totalAmount=159494.0f), MonthlyTotal(month="2023-06", totalAmount=59544.32f), MonthlyTotal(month="2023-07", totalAmount=194619.77f), MonthlyTotal(month="2023-08", totalAmount=153272.81f), MonthlyTotal(month="2023-09", totalAmount=236400.77f), MonthlyTotal(month="2023-10", totalAmount=60685.82f), MonthlyTotal(month="2023-11", totalAmount=176938.78f), MonthlyTotal(month="2023-12", totalAmount=61094.1f), MonthlyTotal(month="2024-01", totalAmount=193477.84f), MonthlyTotal(month="2024-02", totalAmount=426907.62f), MonthlyTotal(month="2024-03", totalAmount=54620.22f), MonthlyTotal(month="2024-04", totalAmount=75525.0f), MonthlyTotal(month="2024-05", totalAmount=198477.05f), MonthlyTotal(month="2024-06", totalAmount=254622.1f), MonthlyTotal(month="2024-07", totalAmount=39429.0f), MonthlyTotal(month="2024-09", totalAmount=6727.0f))
    val incomeData = listOf(MonthlyTotal(month="2021-10", totalAmount=115262.0f), MonthlyTotal(month="2021-12", totalAmount=102499.0f), MonthlyTotal(month="2022-01", totalAmount=102792.0f), MonthlyTotal(month="2022-02", totalAmount=107812.0f), MonthlyTotal(month="2022-03", totalAmount=121300.0f), MonthlyTotal(month="2022-04", totalAmount=229603.0f), MonthlyTotal(month="2022-05", totalAmount=28213.0f), MonthlyTotal(month="2022-06", totalAmount=132286.0f), MonthlyTotal(month="2022-07", totalAmount=330331.0f), MonthlyTotal(month="2022-08", totalAmount=594790.0f), MonthlyTotal(month="2022-09", totalAmount=28322.25f), MonthlyTotal(month="2022-10", totalAmount=149682.0f), MonthlyTotal(month="2022-11", totalAmount=145543.0f), MonthlyTotal(month="2022-12", totalAmount=254190.0f), MonthlyTotal(month="2023-01", totalAmount=8.82f), MonthlyTotal(month="2023-02", totalAmount=133710.0f), MonthlyTotal(month="2023-03", totalAmount=103061.0f), MonthlyTotal(month="2023-04", totalAmount=227539.0f), MonthlyTotal(month="2023-05", totalAmount=180.0f), MonthlyTotal(month="2023-06", totalAmount=128988.0f), MonthlyTotal(month="2023-07", totalAmount=144757.5f), MonthlyTotal(month="2023-08", totalAmount=149988.0f), MonthlyTotal(month="2023-09", totalAmount=605242.0f), MonthlyTotal(month="2023-11", totalAmount=302052.0f), MonthlyTotal(month="2023-12", totalAmount=153190.0f), MonthlyTotal(month="2024-02", totalAmount=153211.0f), MonthlyTotal(month="2024-03", totalAmount=306420.0f), MonthlyTotal(month="2024-05", totalAmount=153210.0f), MonthlyTotal(month="2024-06", totalAmount=156570.0f), MonthlyTotal(month="2024-07", totalAmount=155610.0f), MonthlyTotal(month="2024-08", totalAmount=615513.0f), MonthlyTotal(month="2024-09", totalAmount=240005.0f))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CustomColors.backgroundPrimaryTop)
    ) {
        Text(text = "Expense Reports", color = CustomColors.onPrimary)
        Spacer(modifier = Modifier.height(24.dp))
        ExpenseBarChart(expenses = expenseData, incomes = incomeData) { onItemClick ->
            /* TODO */
        }
    }
}