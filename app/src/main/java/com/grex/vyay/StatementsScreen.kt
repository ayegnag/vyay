package com.grex.vyay

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.EmojiNature
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.grex.vyay.ui.components.CustomScrollbar
import com.grex.vyay.ui.components.SearchBar
import com.grex.vyay.ui.theme.CustomColors
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
    val applicationContext: Context = LocalContext.current.applicationContext
    val database: AppDatabase = AppDatabase.getDatabase(applicationContext)
    val utils = Utilities()
    val appDao: AppDao = database.appDao()
    var transactionData by remember { mutableStateOf<List<TransactionRecord>>(emptyList()) }
    var filteredTransactions by remember { mutableStateOf(transactionData) }
    var displayMonth by remember { mutableStateOf<String>("") }

    var toggleSearch by remember { mutableStateOf(false) }

    LaunchedEffect(yearMonth) {
        val month = if (yearMonth == "{yearMonth}" || yearMonth == null) {
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        } else {
            yearMonth
        }
        Log.d("Transaction Month", month)
        transactionData = appDao.getTransactionsForMonth(month)
        filteredTransactions = transactionData
//        transactionData.forEach { transaction ->
//            Log.d("Transactions", transaction.toString())
//        }
        displayMonth = month?.let { utils.convertYearMonthToMonthName(it) }.toString()
    }

    val systemUiController = rememberSystemUiController()
    DisposableEffect(systemUiController) {
        systemUiController.setStatusBarColor(
            color = CustomColors.backgroundPrimaryTop,
            darkIcons = false
        )
        systemUiController.setNavigationBarColor(
            color = CustomColors.backgroundPrimaryTop,
            darkIcons = false
        )
        onDispose {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = CustomColors.backgroundPrimaryTop,
                    titleContentColor = CustomColors.onPrimary,
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 0.dp)
                        .background(CustomColors.onTertiary)
                ) {
                    if (!toggleSearch) {
                        IconButton(
                            onClick = { toggleSearch = true },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = CustomColors.active
                            )
                        }
                    }
                    if (toggleSearch) {
                        SearchBar(onSearchQueryChanged = { query ->
                            filteredTransactions = filterTransactions(transactionData, query)
                        }, onClose = {
                            toggleSearch = false
                        },
                            modifier = Modifier.padding(start = 11.dp, top = 3.dp, bottom = 3.dp)
                        )
                    }
                }
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
                ) {

                    if (filteredTransactions.isEmpty()) {
                        Box(modifier = Modifier.align(Alignment.Center)) {
                            Column {
                                Icon(
                                    imageVector = Icons.Outlined.EmojiNature,
                                    contentDescription = "Just Nature",
                                    modifier = Modifier
                                        .size(44.dp)
                                        .align(Alignment.CenterHorizontally),
                                    tint = CustomColors.onPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "so empty...",
                                    color = CustomColors.onPrimary,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    } else {

                        val listState = rememberLazyListState()

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(end = 8.dp),
                            contentPadding = padding
                        ) {
                            items(filteredTransactions) { transaction ->
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
            }
        }
    )
}

fun filterTransactions(
    transactions: List<TransactionRecord>,
    query: String
): List<TransactionRecord> {
    if (query.isBlank()) return transactions

    val lowercaseQuery = query.lowercase()
    return transactions.filter { transaction ->
        transaction.address.lowercase().contains(lowercaseQuery) ||
                transaction.transactionType?.lowercase()?.contains(lowercaseQuery) == true ||
                transaction.currency?.lowercase()?.contains(lowercaseQuery) == true ||
                transaction.amount?.toString()?.contains(lowercaseQuery) == true ||
                transaction.transactionMode?.lowercase()?.contains(lowercaseQuery) == true ||
                transaction.source.lowercase().contains(lowercaseQuery) ||
                transaction.body.lowercase().contains(lowercaseQuery) ||
                transaction.tags?.lowercase()?.contains(lowercaseQuery) == true ||
                transaction.category?.lowercase()?.contains(lowercaseQuery) == true
    }
}

@Composable
fun TransactionItem(
    transaction: TransactionRecord,
    onClick: () -> Unit
) {

    val isIncome = ifIncome(transaction.transactionType)
    val amountColor =
        if (!transaction.isTransaction) CustomColors.onPrimaryInactive
        else if (isIncome) CustomColors.income
        else CustomColors.expense // Custom green and red colors
    val amountPrefix = if (isIncome) "+" else "-"
    val labelColor =
        if (!transaction.isTransaction) CustomColors.onPrimaryInactive
        else CustomColors.onPrimary
    val shapeColor =
        if (!transaction.isTransaction) CustomColors.onPrimaryInactive
        else CustomColors.onPrimaryDim
    val barColor = if (!transaction.isTransaction) CustomColors.secondaryInactive
    else CustomColors.surface
    val utils = Utilities()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick)
            .clip(shape = RoundedCornerShape(36.dp))
            .background(barColor),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Placeholder for icon
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
//                    .background(Color.Gray, CircleShape)
            ) {
                Surface(
                    shape = CircleShape,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color.Transparent
                        )
                ) {
                    Icon(
                        imageVector = TagIcon(transaction.tags),
                        contentDescription = "Category Icon",
                        tint = CustomColors.tertiary,
                        modifier = Modifier
                            .size(38.dp)
                            .background(shapeColor)
                            .padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        .format(Date(transaction.receivedOnDate)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = labelColor
                )
                Text(
                    text = transaction.transactionType ?: "Unknown",
                    style = MaterialTheme.typography.bodySmall,
                    color = labelColor
                )
            }

            Column(modifier = Modifier.height(40.dp), verticalArrangement = Arrangement.Center) {
                Row {

                    Text(
                        text = "$amountPrefix ${
                            transaction.amount?.let {
                                utils.getCurrencyFormat(transaction.amount)
                            } ?: "-"
                        }",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = amountColor,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = CustomColors.onSecondary,
                        modifier = Modifier.offset(x = 8.dp)
                    )
                }
            }
        }
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
//                            MaterialTheme.colorScheme.background,
//                            CustomColors.backgroundPrimaryBottom
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
class FakeMainActivity : MainActivity() {
    // Override any necessary methods or properties here
}

@Composable
fun StatementsScreenWithMockContext(
    yearMonth: String?,
    activity: MainActivity,
    padding: PaddingValues,
    onTransactionClick: (TransactionRecord) -> Unit
) {
    CompositionLocalProvider(
        LocalContext provides LocalContext.current.applicationContext
    ) {
        StatementsScreen(
            yearMonth = yearMonth,
            activity = activity,
            padding = padding,
            onTransactionClick = onTransactionClick
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StatementsScreenPreview() {
    val fakeYearMonth = "2023-08"
    val fakeActivity = FakeMainActivity()
    val fakePadding = PaddingValues(16.dp)
    val fakeOnTransactionClick: (TransactionRecord) -> Unit = {}

    StatementsScreenWithMockContext(
        yearMonth = fakeYearMonth,
        activity = fakeActivity,
        padding = fakePadding,
        onTransactionClick = fakeOnTransactionClick
    )
}