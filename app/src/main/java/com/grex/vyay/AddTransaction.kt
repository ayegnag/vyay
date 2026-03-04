package com.grex.vyay

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.grex.vyay.ui.components.CustomInputField
import com.grex.vyay.ui.components.DynamicSelectTextField
import com.grex.vyay.ui.components.InputFieldType
import com.grex.vyay.ui.components.SelectableTagPill
import com.grex.vyay.ui.components.TagSelector
import com.grex.vyay.ui.theme.CustomColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransaction(
    navController: NavController,
    onAckUpdate: (Boolean) -> Unit
) {
    val applicationContext: Context = VyayApp.instance.applicationContext
    val database: AppDatabase = AppDatabase.getDatabase(applicationContext)
    val appDao: AppDao = database.appDao()
    var transaction by remember { mutableStateOf(createEmptyTransactionRecord()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val transactionModeOptions = listOf("Cash", "UPI", "Card", "NetBanking")

    val systemUiController = rememberSystemUiController()
    val utils = Utilities()
    val scrollState = rememberScrollState()

    // Colors
    var expenseColor by remember { mutableStateOf(CustomColors.expense) }
    var incomeColor by remember { mutableStateOf(CustomColors.income) }
    var amountColor by remember { mutableStateOf(CustomColors.activeAlt) }
    var shapeColor by remember { mutableStateOf(CustomColors.secondary) }
    var iconColor by remember { mutableStateOf(CustomColors.onSecondary) }
    var toggleTextColor by remember { mutableStateOf(CustomColors.active) }

    // Form data
    var amount by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf("expense") }
    var transactionReceiver by remember { mutableStateOf("") }
    var transactionTime by remember { mutableLongStateOf(0) }
    var selectedOptionValue by remember { mutableStateOf<String?>(null) }
    var transactionNote by remember { mutableStateOf("") }
    var isNotTransaction by remember { mutableStateOf(false) }

    var tagState = remember { mutableStateOf("") }
    var expenseTags = ""
    var incomeTags = ""
    var tagSelectorKey by remember { mutableIntStateOf(0) }

    val isFormValid by remember(amount, transactionType, transactionReceiver, selectedOptionValue) {
        derivedStateOf {
            amount.isNotBlank() && transactionType.isNotBlank() && transactionReceiver.isNotBlank() && selectedOptionValue != null
        }
    }

    fun assignColors(currentTransaction: TransactionRecord?) {
        currentTransaction?.let { t ->
            expenseColor =
                if (!t.isTransaction) CustomColors.onPrimaryInactive else CustomColors.expense
            incomeColor =
                if (!t.isTransaction) CustomColors.onPrimaryInactive else CustomColors.income
            amountColor =
                if (!t.isTransaction) CustomColors.onPrimaryInactive else CustomColors.activeAlt
            shapeColor =
                if (!t.isTransaction) CustomColors.onPrimaryInactive else CustomColors.secondary
            iconColor = if (!t.isTransaction) CustomColors.surface else CustomColors.onPrimaryDim
            toggleTextColor =
                if (!t.isTransaction) CustomColors.onSecondary else CustomColors.active
        }
    }

    var isTransactionUpdated by remember { mutableStateOf(false) }

    suspend fun saveNewTransaction() {
        val transactionData = TransactionRecord(
            id = 0,
            isManual = true,
            address = "",
            receivedOnDate = System.currentTimeMillis(),
            transactionType = transactionType,  // Unknown transaction type
            currency = "INR",  // Unknown currency
            amount = amount.toDouble(),  // Unknown amount
            receivedAt = transactionReceiver,
            transactionMode = selectedOptionValue,
            messageDate = null,
            source = "manual",
            isTransaction = !isNotTransaction,
            body = transactionNote,
            tags = tagState.value,
            category = null,
            isProcessed = false
        )
        appDao.insertTransactionRecord(transactionData)
        onAckUpdate(true)
        navController.navigateUp()
    }

    DisposableEffect(systemUiController) {
        systemUiController.setStatusBarColor(
            color = CustomColors.backgroundPrimaryTop,
            darkIcons = false // Set to false for light icons
        )
        systemUiController.setNavigationBarColor(
            color = CustomColors.backgroundPrimaryBottom,
            darkIcons = false // Set to false for light icons
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
                title = {
                    when (isTransactionUpdated) {
                        false -> Text("Add Transaction")
                        true -> Text("Save Changes")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CustomColors.onPrimary
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            CoroutineScope(Dispatchers.Main).launch {
                                saveNewTransaction()
                            }
                        },
                        enabled = isFormValid
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save Changes",
                            tint = if (isFormValid) CustomColors.active else {
                                CustomColors.onPrimaryInactive
                            },
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Save",
                            color = if (isFormValid) CustomColors.active else {
                                CustomColors.onPrimaryInactive
                            }
                        )
                    }
                },
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .verticalScroll(state = scrollState)
                    .padding(paddingValues)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                CustomColors.backgroundPrimaryTop,
                                CustomColors.backgroundPrimaryBottom
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .align(Alignment.Center)
                ) {
                    Box {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth(),
                            color = CustomColors.surface
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(24.dp)
                                    .padding(top = 12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Payments,
                                        contentDescription = "Amount",
                                        modifier = Modifier
                                            .size(20.dp)
                                            .offset(y = (2).dp),
                                        tint = CustomColors.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    CustomInputField(
                                        value = amount,
                                        inputType = InputFieldType.CURRENCY,
                                        placeholder = "How much?",
                                        onValueChange = { newValue ->
                                            amount = newValue
                                        },
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(28.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                                        contentDescription = "Amount",
                                        modifier = Modifier
                                            .size(20.dp),
                                        tint = CustomColors.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(32.dp))
                                    Row {
                                        SelectableTagPill("Expense",
                                            isSelected = (transactionType == "expense"),
                                            onTagSelected = {
                                                transactionType = "expense"
                                                tagState.value = expenseTags
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        SelectableTagPill("Income",
                                            isSelected = (transactionType == "income"),
                                            onTagSelected = {
                                                transactionType = "income"
                                                tagState.value = incomeTags
                                            }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = "Receiver",
                                        modifier = Modifier
                                            .size(20.dp)
                                            .offset(y = (-3).dp),
                                        tint = CustomColors.onSecondary
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    CustomInputField(
                                        value = transactionReceiver,
                                        inputType = InputFieldType.TEXT,
                                        placeholder = if (transactionType == "expense") {
                                            "Paid to"
                                        } else {
                                            "Received from"
                                        },
                                        onValueChange = { newValue ->
                                            transactionReceiver = newValue
                                        },
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AccessTime,
                                        contentDescription = "Time",
                                        modifier = Modifier
                                            .size(20.dp)
                                            .offset(y = (-3).dp),
                                        tint = CustomColors.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(enabled = true, onClick = {
                                                showDatePicker = true
                                            }),
                                        contentAlignment = Alignment.BottomStart
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .wrapContentHeight()
                                                .padding(
                                                    start = 16.dp,
                                                    end = 0.dp,
                                                    bottom = 12.dp,
                                                    top = 8.dp
                                                )
                                        ) {
                                            Text(
                                                text =
                                                if (transactionTime.toInt() == 0) {
                                                    "When?"
                                                } else {
                                                    SimpleDateFormat(
                                                        "dd MMM yyyy",
                                                        Locale.getDefault()
                                                    )
                                                        .format(Date(transactionTime))
                                                },
                                                style = TextStyle(fontSize = 16.sp),
                                                color = CustomColors.onPrimaryDim
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .padding(end = 16.dp),
                                                tint = CustomColors.onPrimaryDim
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(1.dp)
                                                .background(CustomColors.onPrimaryDim)
                                        )
                                    }
                                    if (showDatePicker) {
                                        DatePickerModal(onDateSelected = { newDate ->
                                            if (newDate != null) {
                                                transactionTime = newDate
                                            }
                                        }, onDismiss = {
                                            showDatePicker = false
                                        })
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Wallet,
                                        contentDescription = "Transaction Mode",
                                        modifier = Modifier
                                            .size(20.dp),
                                        tint = CustomColors.onSecondary
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    DynamicSelectTextField(
                                        selectedValue = selectedOptionValue, // or some string value if selected
                                        options = transactionModeOptions,
                                        label = "Select transaction mode",
                                        placeholder = "Choose an option",
                                        onValueChangedEvent = { newValue ->
                                            selectedOptionValue = newValue
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Tag,
                                        contentDescription = "income",
                                        modifier = Modifier
                                            .width(20.dp)
                                            .align(Alignment.Top)
                                            .padding(top = 15.dp),
                                        tint = CustomColors.onPrimary
                                    )
                                    TagSelector(
                                        tagsState = tagState,
                                        type = "",
                                        key = tagSelectorKey,
                                        modifier = Modifier
                                            .padding(start = 8.dp),
                                    ) { newSelection ->
                                        tagState.value = newSelection
                                        if (transactionType == "expense") {
                                            expenseTags = newSelection
                                        } else {
                                            incomeTags = newSelection
                                        }
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.EditNote,
                                        contentDescription = "Note",
                                        modifier = Modifier
                                            .width(20.dp)
                                            .offset(y = (3).dp),
                                        tint = CustomColors.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    CustomInputField(
                                        value = transactionNote,
                                        placeholder = "Notes",
                                        inputType = InputFieldType.TEXT,
                                        onValueChange = { newValue -> transactionNote = newValue },
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Checkbox(
                                        checked = isNotTransaction,
                                        onCheckedChange = { newValue ->
                                            isNotTransaction = newValue
                                        },
                                        modifier = Modifier.offset(x = (-11).dp)
                                    )
                                    Text(
                                        text = "Not a transaction",
                                        color = CustomColors.active,
                                        style = MaterialTheme.typography.labelLarge,
                                        modifier = Modifier
                                            .padding(start = 0.dp)
                                            .offset(x = (-6).dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

        }
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
//        colors = DatePickerColors()
    ) {
        DatePicker(state = datePickerState)
    }
}

fun createEmptyTransactionRecord(): TransactionRecord {
    return TransactionRecord(
        id = 0,
        isManual = false,
        address = "",
        receivedOnDate = 0L,
        transactionType = "",
        currency = null,
        amount = null,
        receivedAt = null,
        transactionMode = null,
        messageDate = null,
        source = "",
        isTransaction = false,
        body = "",
        tags = null,
        category = null,
        isProcessed = false
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun AddTransactionPreview() {
    val isTransactionUpdated = false
    val transactionModeOptions = listOf("Cash", "UPI", "Card", "NetBanking")
    var amount = ""
    var type = "expense"
    val tagState = remember { mutableStateOf("") }
    var transactionNote = ""
    var transactionType = "expense"
    var transactionTime by remember { mutableLongStateOf(0) }
    var selectedOptionValue = ""
    var transactionReceiver = ""
    var showDatePicker = false

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = CustomColors.backgroundPrimaryTop,
                    titleContentColor = CustomColors.onPrimary,
                ),
                title = {
                    when (isTransactionUpdated) {
                        false -> Text("Add Transaction")
                        true -> Text("Save Changes")
                    }
                },
                navigationIcon = {
                    if (isTransactionUpdated) {
                        IconButton(onClick = {
                            /* Mock */
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel"
                            )
                        }
                    } else {
                        IconButton(onClick = { /* Mock */ }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = CustomColors.onPrimary
                            )
                        }
                    }
                },
                actions = {
                    if (isTransactionUpdated) {
                        IconButton(onClick = {
                            CoroutineScope(Dispatchers.Main).launch {
                                /* Mock */
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save Changes"
                            )
                        }
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
                                CustomColors.backgroundPrimaryTop,
                                CustomColors.backgroundPrimaryBottom
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .align(Alignment.Center)
                ) {
                    Box {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth(),
                            color = CustomColors.onTertiary
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(24.dp)
                                    .padding(top = 12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Payments,
                                        contentDescription = "Amount",
                                        modifier = Modifier
                                            .size(20.dp)
                                            .offset(y = (2).dp),
                                        tint = CustomColors.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    CustomInputField(
                                        value = amount,
                                        inputType = InputFieldType.CURRENCY,
                                        placeholder = "How much?",
                                        onValueChange = { newValue ->
                                            amount = newValue
                                        },
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(28.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                                        contentDescription = "Amount",
                                        modifier = Modifier
                                            .size(20.dp),
                                        tint = CustomColors.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(32.dp))
                                    Row {
                                        SelectableTagPill(
                                            "Expense",
                                            isSelected = (transactionType == "expense"),
                                            onTagSelected = {
                                                transactionType = "expense"
                                            },
                                            tint = CustomColors.active,
                                            textColor = CustomColors.onTertiary
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        SelectableTagPill(
                                            "Income",
                                            isSelected = (transactionType == "income"),
                                            onTagSelected = {
                                                transactionType = "income"
                                            },
                                            tint = CustomColors.active
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = "Receiver",
                                        modifier = Modifier
                                            .size(20.dp)
                                            .offset(y = (-3).dp),
                                        tint = CustomColors.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    CustomInputField(
                                        value = transactionReceiver,
                                        inputType = InputFieldType.TEXT,
                                        placeholder = if (transactionType == "expense") {
                                            "Paid to"
                                        } else {
                                            "Received from"
                                        },
                                        onValueChange = { newValue ->
                                            transactionReceiver = newValue
                                        },
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AccessTime,
                                        contentDescription = "Time",
                                        modifier = Modifier
                                            .size(20.dp)
                                            .offset(y = (-3).dp),
                                        tint = CustomColors.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(enabled = true, onClick = {
                                                showDatePicker = true
                                            }),
                                        contentAlignment = Alignment.BottomStart
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .wrapContentHeight()
                                                .padding(
                                                    start = 16.dp,
                                                    end = 0.dp,
                                                    bottom = 12.dp,
                                                    top = 8.dp
                                                )
                                        ) {
                                            Text(
                                                text =
                                                if (transactionTime.toInt() == 0) {
                                                    "When?"
                                                } else {
                                                    SimpleDateFormat(
                                                        "dd MMM yyyy",
                                                        Locale.getDefault()
                                                    )
                                                        .format(Date(transactionTime))
                                                },
                                                style = TextStyle(fontSize = 16.sp),
                                                color = CustomColors.onPrimaryDim
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .padding(end = 16.dp),
                                                tint = CustomColors.onPrimaryDim
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(1.dp)
                                                .background(CustomColors.onPrimaryDim)
                                        )
                                    }
                                    if (showDatePicker) {
                                        DatePickerModal(onDateSelected = { newDate ->
                                            if (newDate != null) {
                                                transactionTime = newDate
                                            }
                                        }, onDismiss = {
                                            showDatePicker = false
                                        })
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Wallet,
                                        contentDescription = "Transaction Mode",
                                        modifier = Modifier
                                            .size(20.dp),
                                        tint = CustomColors.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    DynamicSelectTextField(
                                        selectedValue = selectedOptionValue, // or some string value if selected
                                        options = transactionModeOptions,
                                        label = "Select transaction mode",
                                        placeholder = "Choose an option",
                                        onValueChangedEvent = { newValue ->
                                            selectedOptionValue = newValue
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Tag,
                                        contentDescription = "income",
                                        modifier = Modifier
                                            .width(20.dp)
                                            .align(Alignment.Top)
                                            .padding(top = 15.dp),
                                        tint = CustomColors.onPrimary
                                    )
                                    TagSelector(
                                        tagsState = tagState,
                                        type = "",
                                        key = 0,
                                        modifier = Modifier
                                            .padding(start = 8.dp),
                                    ) { newSelection ->
                                        tagState.value = newSelection
                                    }
                                }
//                                Spacer(modifier = Modifier.height(16.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.EditNote,
                                        contentDescription = "Note",
                                        modifier = Modifier
                                            .width(20.dp)
                                            .offset(y = (3).dp),
                                        tint = CustomColors.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    CustomInputField(
                                        value = transactionNote,
                                        placeholder = "Notes",
                                        inputType = InputFieldType.TEXT,
                                        onValueChange = { newValue -> transactionNote = newValue },
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Checkbox(
                                        checked = false,
                                        onCheckedChange = { },
                                        modifier = Modifier.offset(x = (-11).dp)
                                    )
                                    Text(
                                        text = "Not a transaction",
                                        color = CustomColors.active,
                                        style = MaterialTheme.typography.labelLarge,
                                        modifier = Modifier
                                            .padding(start = 0.dp)
                                            .offset(x = (-6).dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}