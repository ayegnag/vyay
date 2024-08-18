package com.grex.vyay

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.grex.vyay.ui.theme.PatinaShine
import com.grex.vyay.ui.theme.backgroundPrimaryBottom
import com.grex.vyay.ui.theme.backgroundPrimaryTop
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun StatementsScreen(activity: MainActivity, padding: PaddingValues) {
    val applicationContext: Context = VyayApp.instance.applicationContext
    var database: AppDatabase = AppDatabase.getDatabase(applicationContext)
    var appDao: AppDao = database.appDao()
    var transactionData by remember { mutableStateOf<List<SmsMessage>>(emptyList()) }

    LaunchedEffect(Unit) {
        transactionData = appDao.getTransactionsForMonth("2024-06")
    }

    val systemUiController = rememberSystemUiController()
    DisposableEffect(systemUiController) {
        systemUiController.setStatusBarColor(
            color = backgroundPrimaryTop,
            darkIcons = false
        )
        systemUiController.setNavigationBarColor(
            color = backgroundPrimaryTop,
            darkIcons = false
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
    ) {
        val listState = rememberLazyListState()

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 8.dp),
            contentPadding = padding
        ) {
            items(transactionData) { transaction ->
                TransactionItem(transaction)
            }
        }

        CustomScrollbar(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .padding(end = 2.dp),
            scrollState = listState
        )
    }
}

@Composable
fun TransactionItem(transaction: SmsMessage) {
    val isIncome = ifIncome(transaction.transactionType)
    val amountColor =
        if (isIncome) PatinaShine else Color(0xFFE57373) // Custom green and red colors
    val amountPrefix = if (isIncome) "+" else "-"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Placeholder for icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.Gray, CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    .format(Date(transaction.receivedOnDate)),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = transaction.transactionType ?: "Unknown",
                style = MaterialTheme.typography.bodySmall
            )
        }

        Text(
            text = "$amountPrefix ${
                transaction.amount?.let {
                    NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(transaction.amount)
                } ?: "-"
            }",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = amountColor
        )
    }
}

fun ifIncome(transactionType: String?): Boolean {
    if (transactionType != null) {
        val incomeTypes = listOf("credited", "deposited")
        return incomeTypes.any { transactionType.contains(it, ignoreCase = true) }
    }
    return false
}
