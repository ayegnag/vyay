package com.grex.vyay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.grex.vyay.ui.theme.Grey
import com.grex.vyay.ui.theme.backgroundPrimaryBottom
import com.grex.vyay.ui.theme.backgroundPrimaryTop
import com.grex.vyay.ui.theme.primaryColor

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
        expenseData = smsAnalysisService.fetchMonthlyExpense()
        incomeData = smsAnalysisService.fetchMonthlyIncome()
        isLoading = false
        onAckUpdate(false)
    }
    LaunchedEffect(Unit) {
        if (isTransactionRecordUpdated) {
            fetchData()
        } else {
            expenseData = smsAnalysisService.getMonthlyExpense()
            incomeData = smsAnalysisService.getMonthlyIncome()
            isLoading = false
        }
    }
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
                    containerColor = backgroundPrimaryTop,
                    titleContentColor = Grey,
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
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    backgroundPrimaryTop,
                                    backgroundPrimaryBottom
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Text(text = "Monthly Expense Reports", color = Color.White)
                    Spacer(modifier = Modifier.height(24.dp))
                    ExpenseBarChart(
                        expenses = expenseData,
                        incomes = incomeData,
                        onItemClick = { monthYear ->
                            onReportClick(monthYear)
                        })
                }
            }
        }
    )
}

@Preview
@Composable
fun ReportsScreen() {
    val expenseData = listOf(
        MonthlyTotal(month = "2024-05", totalAmount = 44990f),
        MonthlyTotal(month = "2024-06", totalAmount = 104692.5f),
        MonthlyTotal(month = "2024-07", totalAmount = 98317.5f),
        MonthlyTotal(month = "2024-08", totalAmount = 81517.2f)
    )
    val incomeData = listOf(
        MonthlyTotal(month = "2024-05", totalAmount = 174404.0f),
        MonthlyTotal(month = "2024-06", totalAmount = 146982.5f),
        MonthlyTotal(month = "2024-07", totalAmount = 152831.5f),
        MonthlyTotal(month = "2024-08", totalAmount = 166151.2f)
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(primaryColor)
    ) {
        Text(text = "Expense Reports", color = Color.White)
        Spacer(modifier = Modifier.height(24.dp))
        ExpenseBarChart(expenses = expenseData, incomes = incomeData) { onItemClick ->
            /* TODO */
        }
    }
}