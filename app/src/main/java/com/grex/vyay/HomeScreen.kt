package com.grex.vyay

import android.app.Application
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableDoubleState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Database
import androidx.room.RoomDatabase
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.grex.vyay.services.MockSharedPreferences
import com.grex.vyay.ui.components.CustomInputField
import com.grex.vyay.ui.components.HomeViewModel
import com.grex.vyay.ui.components.InputFieldType
import com.grex.vyay.ui.components.PieChartData
import com.grex.vyay.ui.components.TransactionPieChart
import com.grex.vyay.ui.theme.CustomColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    activity: MainActivity,
    padding: PaddingValues,
    viewModel: HomeViewModel,
    sharedViewModel: SharedViewModelInterface,
    onRequestAutoAssignTags: () -> Unit,
    appDao: AppDao
) {
//    val applicationContext: Context = VyayApp.instance.applicationContext
//    val applicationContext: Context = context
//    val database: AppDatabase = AppDatabase.getDatabase(applicationContext)
//    val appDao: AppDao = database.appDao()
    val chartData = remember { mutableListOf<PieChartData>() }
    val userPreferences = UserPreferences(LocalContext.current)
    val userName = userPreferences.getUserName()
    val prefUpdateFlag by viewModel.prefSimilarRecordUpdateFlag.collectAsState()
    val prefProcessingFlag by viewModel.prefProcessingSimilarRecordFlag.collectAsState()
    val showPieChart by remember { mutableStateOf(true) }
    var showExpenseLimitInput by remember { mutableStateOf(false) }
    var monthlyExpenseLimit by remember { mutableStateOf("") }
    val systemUiController = rememberSystemUiController()
    val monthExpense = remember {
        mutableDoubleStateOf(0.0)
    }
    var currentMonthTransactions by remember { mutableStateOf<List<TransactionRecord>>(emptyList()) }
    val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))

    val expenseThreshold by sharedViewModel.prefExpenseThreshold.collectAsState()

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
        if (expenseThreshold == 0.0) {
            showExpenseLimitInput = true
        }
    }
    LaunchedEffect(key1 = Unit) {
        Log.d("LoadPie", "LaunchEffect")
        currentMonthTransactions = appDao.getTransactionsForMonth(currentMonth)
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
                    .background(CustomColors.onPrimaryDim)
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
                        .blur(radius = 4.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .padding(start = 1.dp, end = 1.dp)
//                        .padding(start = 24.dp, end = 24.dp)
                        .fillMaxSize()
                ) {
                    item {
                        Spacer(modifier = Modifier.height(36.dp))
                        ExpenseHighlighter(monthExpense, expenseThreshold)
                    }

                    if (prefUpdateFlag) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Log.d("ShowAlert", "prefUpdateFlag ${prefProcessingFlag}")
                            ProcessSimilarTransactions(prefProcessingFlag, onRequestAutoAssignTags)
                        }
                    }

                    if (showExpenseLimitInput) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            GetMonthlyExpenseThreshold(
                                onSave = { newValue ->
                                    sharedViewModel.setExpenseThreshold(newValue.toDouble())
                                    showExpenseLimitInput = false
                                })
                        }
                    }

                    item {
                        if (showPieChart) {
                            Spacer(modifier = Modifier.height(16.dp))
                            PieChartContainer(currentMonthTransactions)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(64.dp))
                    }

                }
            }
        }
    )
}

@Composable
fun ExpenseHighlighter(monthExpense: MutableDoubleState, expenseThreshold: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                ambientColor = Color.Black,
                spotColor = Color.Black,
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(CustomColors.onTertiary.copy(0.9f))
    ) {
        Row(modifier = Modifier.padding(24.dp)) {
            Column {
                Text(
                    text = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                        .format(monthExpense.doubleValue),
                    color = CustomColors.primaryLight,
                    style = MaterialTheme.typography.displayMedium,
                    textAlign = TextAlign.Left
                )
                Text(
                    text = "Your expenses this month",
                    color = CustomColors.onPrimaryDim,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Left
                )
                if (expenseThreshold != 0.0) {
                    val expenseDelta = monthExpense.doubleValue - expenseThreshold
                    val deltaPercentage = ((expenseDelta / monthExpense.doubleValue) * 100).toInt()

                    val percentageColor =
                        if (expenseDelta > 0) CustomColors.primaryLight else CustomColors.income // Orange if up, Green if down

                    Row {
                        Text(
                            text = buildAnnotatedString {
                                append("Your expenses are ")
                                append(if (expenseDelta > 0) "up by " else "down by ")

                                withStyle(
                                    style = SpanStyle(
                                        color = percentageColor,
                                        fontSize = MaterialTheme.typography.labelMedium.fontSize
                                    )
                                ) {
                                    append("$deltaPercentage%")
                                }
                            },
                            color = CustomColors.onPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Left
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProcessSimilarTransactions(prefProcessingFlag: Boolean, onRequestAutoAssignTags: () -> Unit) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                ambientColor = Color.Black,
                spotColor = Color.Black,
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(CustomColors.onTertiary.copy(0.9f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Information Alert",
                    tint = CustomColors.onPrimary
                )
                Text(
                    text = "SIMILAR TRANSACTIONS FOUND",
                    style = MaterialTheme.typography.labelMedium,
                    color = CustomColors.onPrimary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (prefProcessingFlag) {
                    Text(
                        text = "Processing...",
                        color = CustomColors.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Left,
                        modifier = Modifier
                            .weight(0.5f)
                            .padding(top = 12.dp)
                    )
                } else {
                    Text(
                        text = "Few transactions were recognized to be of same type, assign them same tags?",
                        color = CustomColors.onPrimaryDim,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Left,
                        modifier = Modifier.weight(0.5f)
                    )
                    TextButton(
                        onClick = {
                            Log.d("startAutoAssignTagsWorker", "Clicked")
                            onRequestAutoAssignTags()
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Apply",
                            tint = CustomColors.active,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Yes", color = CustomColors.active)
                    }
                }
            }
        }
    }
}

@Composable
fun GetMonthlyExpenseThreshold(onSave: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                ambientColor = Color.Black,
                spotColor = Color.Black,
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(CustomColors.onTertiary.copy(0.9f))
    ) {
        var showExpenseLimitInput by remember { mutableStateOf(false) }
        var expenseLimit by remember { mutableStateOf("") }

        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Information Alert",
                    tint = CustomColors.onPrimary
                )
                Text(
                    text = "MONTHLY EXPENSE LIMIT",
                    style = MaterialTheme.typography.labelMedium,
                    color = CustomColors.onPrimary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "What are your expected monthly spendings? Manage your expenses by monitoring how much you can spend each month. ",
                    color = CustomColors.onPrimaryDim,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Left,
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(top = 8.dp)
                )

                if (showExpenseLimitInput) {
                    IconButton(
                        onClick = { showExpenseLimitInput = false },
                        modifier = Modifier.weight(0.3f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel"
                        )
                    }
                } else {
                    TextButton(
                        onClick = {
                            Log.d("GetMonthlyExpenseThreshold", "Clicked")
                            showExpenseLimitInput = true
                        },
                        modifier = Modifier.weight(0.3f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Enter",
                            tint = CustomColors.active,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Enter", color = CustomColors.active)
                    }
                }
            }
            if (showExpenseLimitInput) {
                Row {
                    Spacer(modifier = Modifier.width(16.dp))
                    CustomInputField(
                        value = expenseLimit,
                        placeholder = "Monthly Limit",
                        inputType = InputFieldType.CURRENCY,
                        onValueChange = { expenseLimit = it },
                        modifier = Modifier.padding(top = 8.dp),
                        onSaveClick = {
                            onSave(expenseLimit)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PieChartContainer(transactionData: List<TransactionRecord>) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                ambientColor = Color.Black,
                spotColor = Color.Black,
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(CustomColors.onTertiary.copy(0.9f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "SPENDINGS THIS MONTH",
                    style = MaterialTheme.typography.labelMedium,
                    color = CustomColors.onPrimary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TransactionPieChart(transactionData)
            }
        }
    }
}

// Fake Code for Preview -------------------------------------------------

class FakeApplication : Application()
class FakeAppDao : AppDao {
    //    Content here
    override suspend fun getLatestRecordDate(): Long? {
        TODO("Not yet implemented")
    }

    override fun getAllRecords(): List<TransactionRecord> {
        TODO("Not yet implemented")
    }

    override suspend fun getFirstKnownYearMonthTuple(): YearMonthTuple? {
        TODO("Not yet implemented")
    }

    override suspend fun getFirstKnownYearMonth(): YearMonth? {
        TODO("Not yet implemented")
    }

    override suspend fun insertRecordInternal(record: TransactionRecord) {
        TODO("Not yet implemented")
    }

    override fun deleteAllRecords() {
        TODO("Not yet implemented")
    }

    override suspend fun getMonthlyExpenses(): List<MonthlyTotal> {
        TODO("Not yet implemented")
    }

    override suspend fun getMonthlyIncomes(): List<MonthlyTotal> {
        TODO("Not yet implemented")
    }

    override suspend fun updateTransactionRecord(
        id: Int,
        isManual: Boolean,
        address: String,
        receivedOnDate: Long,
        transactionType: String?,
        currency: String?,
        amount: Double?,
        receivedAt: String?,
        transactionMode: String?,
        messageDate: String?,
        source: String,
        isTransaction: Boolean,
        body: String,
        tags: String?,
        category: String?,
        isProcessed: Boolean?
    ): Int {
        TODO("Not yet implemented")
    }

    override suspend fun getTransactionsForMonth(yearMonth: String): List<TransactionRecord> {
        TODO("Not yet implemented")
    }

    override suspend fun getTransactionById(id: Int, isManual: Boolean): TransactionRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun getCurrentMonthExpense(): Double {
        TODO("Not yet implemented")
    }

    override suspend fun insertTransactionRecord(transactionRecord: TransactionRecord) {
        TODO("Not yet implemented")
    }

    override fun deleteManualRecord(id: Int) {
        TODO("Not yet implemented")
    }
}

@Database(entities = [TransactionRecord::class], version = 4)
abstract class FakeAppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        fun appDao(): AppDao {
            Log.d("FAKE", "")
            return FakeAppDao()
        }
    }
}

class FakeSharedViewModel : SharedViewModelInterface {
    private val _prefExpenseThreshold = MutableStateFlow(0.0)
    override val prefExpenseThreshold: StateFlow<Double> = _prefExpenseThreshold.asStateFlow()

    override fun setExpenseThreshold(value: Double) {
        _prefExpenseThreshold.value = value
    }
}

@Preview
@Composable
fun PreviewHomeScreen() {
    val fakeApplication = FakeApplication()
    val fakeActivity = FakeMainActivity()
    val fakePadding = PaddingValues(16.dp)
    val sharedPrefs = MockSharedPreferences()
    val fakeViewModel = HomeViewModel(sharedPrefs)
    val fakeSharedViewModel = FakeSharedViewModel()
    val fakeDatabase = FakeAppDatabase
    val fakeAppDao = fakeDatabase.appDao()

    CompositionLocalProvider(
        LocalContext provides LocalContext.current.applicationContext
    ) {
        HomeScreen(
            activity = fakeActivity,
            padding = fakePadding,
            viewModel = fakeViewModel,
            sharedViewModel = fakeSharedViewModel,
            onRequestAutoAssignTags = {},
            appDao = fakeAppDao
        )
    }
}
