package com.grex.vyay

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import com.grex.vyay.ui.theme.Cream
import com.grex.vyay.ui.theme.Grey
import com.grex.vyay.ui.theme.LimeGreen
import com.grex.vyay.ui.theme.PetalRed
import com.grex.vyay.ui.theme.PurpleGrey40
import com.grex.vyay.ui.theme.backgroundPrimaryBottom
import com.grex.vyay.ui.theme.backgroundPrimaryTop
import com.grex.vyay.ui.theme.secondaryInactive
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatementsScreen(
    yearMonth: String?,
    activity: MainActivity,
    padding: PaddingValues,
    onTransactionClick: (TransactionRecord) -> Unit
) {
    val applicationContext: Context = VyayApp.instance.applicationContext
    val database: AppDatabase = AppDatabase.getDatabase(applicationContext)
    val utils = Utilities()
    val appDao: AppDao = database.appDao()
    var transactionData by remember { mutableStateOf<List<TransactionRecord>>(emptyList()) }
    var displayMonth by remember { mutableStateOf<String>("") }

    LaunchedEffect(yearMonth) {
        val month = if (yearMonth == "{yearMonth}" || yearMonth == null) {
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        } else {
            yearMonth
        }
        Log.d("Transaction Month", month)
        transactionData = appDao.getTransactionsForMonth(month)
        transactionData.forEach { transaction ->
            Log.d("Transactions", transaction.toString())
        }
        displayMonth = month?.let { utils.convertYearMonthToMonthName(it) }.toString()
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

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = backgroundPrimaryTop,
                    titleContentColor = Grey,
                ),
                title = { Text("$displayMonth Statements") },
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
                    .fillMaxSize()
                    .padding(paddingValues)
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
                        TransactionItem(transaction = transaction,
                            onClick = { onTransactionClick(transaction) })
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
    )
}

@Composable
fun TransactionItem(
    transaction: TransactionRecord,
    onClick: () -> Unit
) {
    val isIncome = ifIncome(transaction.transactionType)
    val amountColor =
        if (!transaction.isTransaction) PurpleGrey40
        else if (isIncome) LimeGreen
        else PetalRed // Custom green and red colors
    val amountPrefix = if (isIncome) "+" else "-"
    val utils = Utilities()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = onClick),
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
                style = MaterialTheme.typography.bodyMedium,
                color = Cream
            )
            Text(
                text = transaction.transactionType ?: "Unknown",
                style = MaterialTheme.typography.bodySmall,
                color = Cream
            )
        }

        Text(
            text = "$amountPrefix ${
                transaction.amount?.let {
                    utils.getCurrencyFormat(transaction.amount)
                } ?: "-"
            }",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = amountColor
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null,
            tint = secondaryInactive, modifier = Modifier.offset(x = 8.dp)
        )
    }
}

fun ifIncome(transactionType: String?): Boolean {
    return transactionType != null && transactionType == "income"
}


//@Preview(showBackground = true, name = "Standard")
//@Composable
//fun StatementPagePreview() {
//    val transactionData = listOf(
//        TransactionRecord(
//            _id = 156,
//            address = "JM-SBIMFD",
//            receivedOnDate = 1717655149218,
//            transactionType = null,
//            currency = "INR",
//            amount = 1.400208E7,
//            receivedAt = null,
//            transactionMode = null,
//            messageDate = null,
//            body = "Dear Investor, Purchase - Systematic in Folio 14002080 in Fund : SBI Magnum Global Fund -Reg G for date 05-Jun-2024 for amount of INR 1,999.90 at NAV of 352.6899 is processed and 5.670 units have been allotted - SBIMF"
//        ),
//        TransactionRecord(
//            _id = 155,
//            address = "AD-HDFCBK",
//            receivedOnDate = 1717652486418,
//            transactionType = "spent",
//            currency = "Rs.",
//            amount = 700.0,
//            receivedAt = "Block & Reissue Call 18002586161/SMS BLOCK CC 4216 to 7308080808",
//            transactionMode = "Card",
//            messageDate = "2024-06-06",
//            body = "Rs.700 spent on HDFC Bank Card x4216 at _SURYA CHILDRENS .. on 2024-06-06:11:11:18.Not U? To Block & Reissue Call 18002586161/SMS BLOCK CC 4216 to 7308080808"
//        ),
//        TransactionRecord(
//            _id = 154,
//            address = "JM-SBIMFD",
//            receivedOnDate = 1717647610599,
//            transactionType = null,
//            currency = "null",
//            amount = 3.0,
//            receivedAt = null,
//            transactionMode = null,
//            messageDate = null,
//            body = "Dear Investor, Please click the link https://cams.co.in/3WjJNcPjrpg & enter PAN/FOLIO NO. to view statement of account for your latest transaction in Folio No. XXXXX004 - SBIMF"
//        ),
//        TransactionRecord(
//            _id = 153,
//            address = "JM-SBIMFD",
//            receivedOnDate = 1717640163757,
//            transactionType = null,
//            currency = "INR",
//            amount = 1.4002004E7,
//            receivedAt = null,
//            transactionMode = null,
//            messageDate = null,
//            body = "Dear Investor, Purchase- Systematic in Folio 14002004 in Fund : SBI Blue Chip Fund Reg Plan-G for date 05-Jun-2024 for amount of INR 999.95 at NAV of 83.1941 is processed and 12.019 units have been allotted - SBIMF"
//        ),
//        TransactionRecord(
//            _id = 223,
//            address = "VD-HDFCBK",
//            receivedOnDate = 1718271274468,
//            transactionType = "deposited",
//            currency = "INR",
//            amount = 20000.0,
//            receivedAt = null,
//            transactionMode = null,
//            messageDate = "13-JUN-24",
//            body = "Update! INR 20,000.00 deposited in HDFC Bank A/c XX1331 on 13-JUN-24 for NEFT Cr-SBIN0003977-Sbi lho-Gangeya Upadhyaya-SBIN524165245332.Avl bal INR 4,14,270.01. Cheque deposits in A/C are subject to clearing"
//        ),
//        TransactionRecord(
//            _id = 149,
//            address = "AD-HDFCBK",
//            receivedOnDate = 1717577839910,
//            transactionType = "debited",
//            currency = "INR",
//            amount = 1000.0,
//            receivedAt = null,
//            transactionMode = null,
//            messageDate = null,
//            body = "INR 1000.00 debited to HDFC Bank A/C No XXXXXXXXXX1331 towards COMPUTER AGE MANAGEMENT SERVICES PVT LTD / 14002004/SBIMF/590700109312  with UMRN HDFC9201711800019311"
//        ),
//        TransactionRecord(
//            _id = 147,
//            address = "AD-HDFCBK",
//            receivedOnDate = 1717575623196,
//            transactionType = "debited",
//            currency = "INR",
//            amount = 2000.0,
//            receivedAt = null,
//            transactionMode = null,
//            messageDate = null,
//            body = "INR 2000.00 debited to HDFC Bank A/C No XXXXXXXXXX1331 towards COMPUTER AGE MANAGEMENT SERVICES PVT LTD / 14002080/SBIMF/590700109311  with UMRN HDFC9201711800019310"
//        ),
//        TransactionRecord(
//            _id = 145,
//            address = "AD-HDFCBK",
//            receivedOnDate = 1717570602279,
//            transactionType = "sent",
//            currency = "Rs.",
//            amount = 5500.0,
//            receivedAt = "IndianClearingCorporation",
//            transactionMode = "UPI",
//            messageDate = "05 - 06",
//            body = "Amt Sent Rs.5500.00 From HDFC Bank A/C *1331 To IndianClearingCorporation On 05-06 Ref 415795307686 Not You? Call 18002586161/SMS BLOCK UPI to 7308080808"
//        ),
//    )
//
//    VyayTheme {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(
//                    brush = Brush.verticalGradient(
//                        colors = listOf(
//                            backgroundPrimaryTop,
//                            backgroundPrimaryBottom
//                        )
//                    )
//                )
//        ) {
//            val listState = rememberLazyListState()
//
//            LazyColumn(
//                state = listState,
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(end = 8.dp)
//            ) {
//                items(transactionData) { transaction ->
//                    TransactionItem(transaction)
//                }
//            }
//        }
//    }
//}