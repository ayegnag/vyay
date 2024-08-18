package com.grex.vyay

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.grex.vyay.ui.theme.backgroundPrimaryBottom
import com.grex.vyay.ui.theme.backgroundPrimaryTop
import com.grex.vyay.ui.theme.primaryColor

@Composable
fun ReportsScreen(activity: MainActivity, padding: PaddingValues) {
    val smsAnalysisService = activity.smsAnalysisService
    val expenseData = smsAnalysisService.getMonthlyExpense()
    val incomeData = smsAnalysisService.getMonthlyIncome()

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
    expenseData.forEach { item ->
        Log.d("Expense Data", item.toString())
    }
    incomeData.forEach { item ->
        Log.d("Income Data", item.toString())
    }
    Column(
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
            .padding(24.dp)
    ) {
        Text(text = "Monthly Expense Reports", color = Color.White)
        Spacer(modifier = Modifier.height(24.dp))
        ExpenseBarChart(expenses = expenseData, incomes = incomeData)
    }
}

@Preview
@Composable
fun ReportsScreen() {
    val expenseData = listOf(
        MonthlyTotal(month="2024-05", totalAmount=44990f),
        MonthlyTotal(month="2024-06", totalAmount=104692.5f),
        MonthlyTotal(month="2024-07", totalAmount=98317.5f),
        MonthlyTotal(month="2024-08", totalAmount=81517.2f)
    )
    val incomeData = listOf(
        MonthlyTotal(month="2024-05", totalAmount=174404.0f),
        MonthlyTotal(month="2024-06", totalAmount=146982.5f),
        MonthlyTotal(month="2024-07", totalAmount=152831.5f),
        MonthlyTotal(month="2024-08", totalAmount=166151.2f)
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(primaryColor)
    ) {
        Text(text = "Expense Reports", color = Color.White)
        Spacer(modifier = Modifier.height(24.dp))
        ExpenseBarChart(expenses = expenseData, incomes = incomeData)
    }
}