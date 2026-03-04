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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.grex.vyay.ui.components.TagSelector
import com.grex.vyay.ui.theme.CustomColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetails(
    transactionId: String?,
    isManual: Boolean,
    navController: NavController,
    onAckUpdate: (Boolean) -> Unit
) {
    val applicationContext: Context = VyayApp.instance.applicationContext
    val database: AppDatabase = AppDatabase.getDatabase(applicationContext)
    val appDao: AppDao = database.appDao()
    var transaction by remember { mutableStateOf<TransactionRecord?>(null) }
    var backUpState by remember { mutableStateOf<TransactionRecord?>(null) }
    val systemUiController = rememberSystemUiController()
    val utils = Utilities()

    var isTransactionUpdated by remember { mutableStateOf(false) }
    var isNotTransaction by remember { mutableStateOf(false) }
    val transactionTagsState = remember { mutableStateOf(transaction?.tags.toString()) }
    var tagSelectorKey by remember { mutableStateOf(0) }

    var expenseColor by remember { mutableStateOf(CustomColors.expense) }
    var incomeColor by remember { mutableStateOf(CustomColors.income) }
    var amountColor by remember { mutableStateOf(CustomColors.activeAlt) }
    var shapeColor by remember { mutableStateOf(CustomColors.secondary) }
    var iconColor by remember { mutableStateOf(CustomColors.onSecondary) }
    var toggleTextColor by remember { mutableStateOf(CustomColors.active) }

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

    LaunchedEffect(transactionId, isManual) {
        transaction = transactionId.let {
            it?.let { it1 ->
                appDao.getTransactionById(
                    it1.toInt(), isManual
                )
            }
        }
        backUpState = transaction?.copy()
        isNotTransaction = transaction?.isTransaction == false
        transactionTagsState.value = transaction?.tags.toString()
        tagSelectorKey++
        assignColors(transaction)
    }

    LaunchedEffect(transaction) {
        assignColors(transaction)

    }
    var showSmsSource by remember { mutableStateOf<Boolean>(false) }


    fun getTransactionColor(type: String? = null): Color {
        return when (type) {
            "expense" -> expenseColor
            "income" -> incomeColor
            else -> CustomColors.onPrimaryInactive
        }
    }

    fun onCancelClick() {
        isTransactionUpdated = false
        isNotTransaction = backUpState?.isTransaction == false
        transactionTagsState.value = backUpState?.tags.toString()
        transaction = backUpState?.copy()
        tagSelectorKey++
    }

    suspend fun onSaveClick() {
        transaction?.let { appDao.updateTransaction(it) }
        onAckUpdate(true)
        navController.navigateUp()
    }

    suspend fun deleteThisTransaction() {
        withContext(Dispatchers.IO) {
            transaction?.let { appDao.deleteManualRecord(it.id) }
        }
        withContext(Dispatchers.Main) {
            onAckUpdate(true)
            navController.navigateUp()
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = CustomColors.backgroundPrimaryTop,
                titleContentColor = CustomColors.onPrimary,
            ),
            title = {
                when (isTransactionUpdated) {
                    false -> Text("Details")
                    true -> Text("Save Changes")
                }
            },
            navigationIcon = {
                if (isTransactionUpdated) {
                    IconButton(onClick = {
                        onCancelClick()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close, contentDescription = "Cancel"
                        )
                    }
                } else {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            },
            actions = {
                if (isTransactionUpdated) {
                    TextButton(onClick = {
                        CoroutineScope(Dispatchers.Main).launch {
                            onSaveClick()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save Changes",
                            tint = CustomColors.active,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Save", color = CustomColors.active)
                    }
                }
            },
        )
    }, content = { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            CustomColors.backgroundPrimaryTop, CustomColors.backgroundPrimaryBottom
                        )
                    )
                )
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center)
            ) {
                if (transaction != null) {
                    Box() {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            color = CustomColors.surface
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Spacer(
                                        modifier = Modifier
                                            .height(20.dp)
                                            .weight(1f)
                                    )
                                    if (transaction!!.isManual) {
                                        Icon(
                                            imageVector = Icons.Outlined.DeleteForever,
                                            contentDescription = "income",
                                            modifier = Modifier
                                                .width(20.dp)
                                                .clickable(onClick = {
                                                    CoroutineScope(Dispatchers.Main).launch { deleteThisTransaction() }
                                                }),
                                            tint = CustomColors.onSecondary
                                        )
                                    }
                                }
                                Column {

                                    Text("${
                                        transaction!!.amount?.let {
                                            utils.getCurrencyFormat(
                                                it
                                            )
                                        }
                                    }",
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Start,
                                        color = amountColor)
                                    Row(
                                        horizontalArrangement = Arrangement.End,
                                        modifier = Modifier.align(Alignment.Start)
                                    ) {
                                        if (transaction!!.transactionType == "expense") {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                                contentDescription = "expense",
                                                modifier = Modifier
                                                    .rotate(-45f)
                                                    .offset(x = (-4).dp),
                                                tint = expenseColor
                                            )
                                        } else if (transaction!!.transactionType == "income") {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                                contentDescription = "income",
                                                modifier = Modifier
                                                    .rotate(-45f)
                                                    .offset(x = (-4).dp, y = (-4).dp),
                                                tint = incomeColor
                                            )
                                        }
                                        Text(
                                            "${transaction!!.transactionType}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = getTransactionColor(transaction!!.transactionType)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))

                                    transaction!!.receivedAt?.let {
                                        DetailRow(
                                            Icons.Filled.Person,
                                            iconColor,
                                            it,
                                            CustomColors.onPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(
                                        Date(transaction!!.receivedOnDate)
                                    ).let {
                                        DetailRow(
                                            Icons.Filled.AccessTime,
                                            iconColor,
                                            it,
                                            CustomColors.onPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    transaction!!.transactionMode?.let {
                                        DetailRow(
                                            Icons.Filled.Wallet,
                                            iconColor,
                                            it,
                                            CustomColors.onPrimary
                                        )
                                    }
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
                                        tint = CustomColors.onSecondary
                                    )
                                    TagSelector(
                                        tagsState = transactionTagsState,
                                        type = transaction!!.transactionType,
                                        key = tagSelectorKey,
                                        modifier = Modifier.padding(start = 8.dp)
                                    ) { newTags ->
                                        transactionTagsState.value = newTags
                                        transaction = transaction?.copy(tags = newTags)!!
                                        isTransactionUpdated = true
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (transaction!!.source == "sms") {
                                        Icon(
                                            imageVector = Icons.Filled.Info,
                                            contentDescription = "income",
                                            modifier = Modifier.size(20.dp),
                                            tint = CustomColors.onSecondary
                                        )
                                        Text(
                                            text = transaction!!.source.uppercase(Locale("en")),
                                            color = CustomColors.onPrimary,
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.padding(start = 10.dp)
                                        )
                                        Spacer(Modifier.weight(1f))
                                        if (showSmsSource) {
                                            TextButton(colors = ButtonColors(
                                                containerColor = Color.Transparent,
                                                contentColor = CustomColors.active,
                                                disabledContainerColor = Color.Transparent,
                                                disabledContentColor = CustomColors.onPrimaryInactive
                                            ), onClick = { showSmsSource = false }) {
                                                Text(text = "Hide")
                                            }
                                        } else {
                                            TextButton(colors = ButtonColors(
                                                containerColor = Color.Transparent,
                                                contentColor = CustomColors.active,
                                                disabledContainerColor = Color.Transparent,
                                                disabledContentColor = CustomColors.onPrimaryInactive
                                            ), onClick = { showSmsSource = true }) {
                                                Text(text = "Show")
                                            }
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Filled.EditNote,
                                            contentDescription = "Note",
                                            modifier = Modifier
                                                .width(20.dp)
                                                .offset(y = (2).dp),
                                            tint = CustomColors.onSecondary
                                        )
                                        Text(
                                            text = transaction!!.body,
                                            color = CustomColors.onPrimary,
                                            modifier = Modifier.padding(start = 10.dp)
                                        )
                                    }
                                }
                                if (showSmsSource) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                    ) {
                                        Text(
                                            text = transaction!!.body,
                                            color = CustomColors.onSecondary,
                                            modifier = Modifier
                                                .background(CustomColors.onTertiary)
                                                .padding(8.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                Row {
                                    Column {

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Start
                                        ) {
                                            Checkbox(
                                                checked = isNotTransaction,
                                                onCheckedChange = { value ->
                                                    isTransactionUpdated = true
                                                    isNotTransaction = value
                                                    transaction =
                                                        transaction?.copy(isTransaction = !value)
                                                },
                                                modifier = Modifier.offset(x = (-11).dp)
                                            )
                                            Text(
                                                text = "Not a transaction",
                                                color = toggleTextColor,
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

                        Surface(
                            shape = CircleShape,
                            modifier = Modifier
                                .size(48.dp)
                                .offset(x = 24.dp, y = (-24).dp)
                                .background(
                                    Color.Transparent
                                )
                        ) {
                            Icon(
                                imageVector = TagIcon(transaction!!.tags),
                                contentDescription = "Category Icon",
                                tint = iconColor,
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(shapeColor)
                                    .padding(8.dp)
                            )
                        }
                    }
                } else {
                    Text("Loading...")
                }
            }
        }
    })

}

@Composable
fun DetailRow(iconName: ImageVector, iconColor: Color, text: String, textColor: Color) {
    Row(verticalAlignment = Alignment.Bottom) {
        Icon(
            imageVector = iconName,
            contentDescription = "income",
            modifier = Modifier.size(20.dp),
            tint = CustomColors.onSecondary
        )
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            modifier = Modifier
                .padding(start = 8.dp)
                .drawBehind {
                    val strokeWidthPx = 1.dp.toPx()
                    val verticalOffset = size.height
                    drawLine(
                        color = iconColor,
                        strokeWidth = strokeWidthPx,
                        start = Offset(0f, verticalOffset),
                        end = Offset(size.width, verticalOffset)
                    )
                },
            color = textColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TransactionDetailPreview() {
    val utils = Utilities()
    var transaction = TransactionRecord(
        id = 376,
        isManual = false,
        address = "AX-HDFCBK",
        receivedOnDate = 1719460930192,
        transactionType = "income",
        currency = "Rs.",
        amount = 162.0,
        receivedAt = "NEERAJ JAKHAR",
        transactionMode = "UPI",
        messageDate = "27-06",
        source = "sms",
        isTransaction = true,
        body = "Amt Sent Rs.162.00 From HDFC Bank A/C *1331 To NEERAJA JAKHAR On 27-06 Ref 417943558423 Not You? Call 18002586161/SMS BLOCK UPI to 7308080808",
        category = "",
        tags = "Groceries, Monthly, House1",
        isProcessed = false
    )
    var isNotTransaction by remember { mutableStateOf(false) }
    var isTransactionUpdated by remember { mutableStateOf(true) }
    var showSmsSource by remember { mutableStateOf<Boolean>(true) }
    var expenseColor by remember { mutableStateOf(CustomColors.expense) }
    var incomeColor by remember { mutableStateOf(CustomColors.income) }
    var amountColor by remember { mutableStateOf(CustomColors.activeAlt) }
    var shapeColor by remember { mutableStateOf(CustomColors.secondary) }
    var iconColor by remember { mutableStateOf(CustomColors.onSecondary) }
    var toggleTextColor by remember { mutableStateOf(CustomColors.active) }
    val transactionTagsState = remember { mutableStateOf(transaction?.tags.toString()) }

    fun deleteThisTransaction() {
//        mock
    }

    fun getTransactionColor(type: String? = null): Color {
        return when (type) {
            "expense" -> expenseColor
            "income" -> incomeColor
            else -> CustomColors.onPrimaryInactive
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = CustomColors.backgroundPrimaryTop,
                titleContentColor = CustomColors.onPrimary,
            ),
            title = {
                when (isTransactionUpdated) {
                    false -> Text("Details")
                    true -> Text("Save Changes")
                }
            },
            navigationIcon = {
                if (isTransactionUpdated) {
                    IconButton(onClick = {
                        /*TODO*/
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close, contentDescription = "Cancel"
                        )
                    }
                } else {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            },
            actions = {
                if (isTransactionUpdated) {
                    TextButton(onClick = {
                        CoroutineScope(Dispatchers.Main).launch {
                            /*TODO*/
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save Changes",
                            tint = CustomColors.active,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Save", color = CustomColors.active)
                    }
                }
            },
        )
    }, content = { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            CustomColors.backgroundPrimaryTop, CustomColors.backgroundPrimaryBottom
                        )
                    )
                )
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center)
            ) {
                if (transaction != null) {
                    Box() {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            color = CustomColors.surface
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Spacer(
                                        modifier = Modifier
                                            .height(20.dp)
                                            .weight(1f)
                                    )
                                    if (transaction.isManual) {
                                        Icon(
                                            imageVector = Icons.Outlined.DeleteForever,
                                            contentDescription = "income",
                                            modifier = Modifier.width(20.dp),
                                            tint = CustomColors.onSecondary
                                        )
                                    }
                                }
                                Column {

                                    Text("${
                                        transaction!!.amount?.let {
                                            utils.getCurrencyFormat(
                                                it
                                            )
                                        }
                                    }",
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Start,
                                        color = amountColor)
                                    Row(
                                        horizontalArrangement = Arrangement.End,
                                        modifier = Modifier.align(Alignment.Start)
                                    ) {
                                        if (transaction!!.transactionType == "expense") {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                                contentDescription = "expense",
                                                modifier = Modifier
                                                    .rotate(-45f)
                                                    .offset(x = (-4).dp),
                                                tint = expenseColor
                                            )
                                        } else if (transaction.transactionType == "income") {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                                contentDescription = "income",
                                                modifier = Modifier
                                                    .rotate(-45f)
                                                    .offset(x = (-4).dp, y = (-4).dp),
                                                tint = incomeColor
                                            )
                                        }
                                        Text(
                                            "${transaction.transactionType}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = getTransactionColor(transaction.transactionType)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    transaction.receivedAt?.let {
                                        DetailRow(
                                            Icons.Filled.Person,
                                            iconColor,
                                            it,
                                            CustomColors.onPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(
                                        Date(transaction.receivedOnDate)
                                    ).let {
                                        DetailRow(
                                            Icons.Filled.AccessTime,
                                            iconColor,
                                            it,
                                            CustomColors.onPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    transaction.transactionMode?.let {
                                        DetailRow(
                                            Icons.Filled.Wallet,
                                            iconColor,
                                            it,
                                            CustomColors.onPrimary
                                        )
                                    }
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
                                        tint = CustomColors.onSecondary
                                    )
                                    TagSelector(
                                        transactionTagsState,
                                        transaction!!.transactionType,
                                        0,
                                        modifier = Modifier.padding(start = 8.dp)
                                    ) { newTags ->
                                        transaction = transaction?.copy(tags = newTags)!!
                                        isTransactionUpdated = true
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (transaction!!.source == "sms") {
                                        Icon(
                                            imageVector = Icons.Filled.Info,
                                            contentDescription = "income",
                                            modifier = Modifier.size(20.dp),
                                            tint = CustomColors.onSecondary
                                        )
                                        Text(
                                            text = transaction!!.source.uppercase(Locale("en")),
                                            color = CustomColors.onPrimary,
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.padding(start = 10.dp)
                                        )
                                        Spacer(Modifier.weight(1f))
                                        if (showSmsSource) {
                                            TextButton(colors = ButtonColors(
                                                containerColor = Color.Transparent,
                                                contentColor = CustomColors.active,
                                                disabledContainerColor = Color.Transparent,
                                                disabledContentColor = CustomColors.onPrimaryInactive
                                            ), onClick = { showSmsSource = false }) {
                                                Text(text = "Hide")
                                            }
                                        } else {
                                            TextButton(colors = ButtonColors(
                                                containerColor = Color.Transparent,
                                                contentColor = CustomColors.active,
                                                disabledContainerColor = Color.Transparent,
                                                disabledContentColor = CustomColors.onPrimaryInactive
                                            ), onClick = { showSmsSource = true }) {
                                                Text(text = "Show")
                                            }
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Filled.EditNote,
                                            contentDescription = "Note",
                                            modifier = Modifier
                                                .width(20.dp)
                                                .offset(y = (3).dp),
                                            tint = CustomColors.onPrimary
                                        )
                                        Text(
                                            text = transaction!!.body,
                                            color = CustomColors.onSecondary,
                                            modifier = Modifier.padding(start = 10.dp)
                                        )
                                    }
                                }
                                if (showSmsSource) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                    ) {
                                        Text(
                                            text = transaction!!.body,
                                            color = CustomColors.onSecondary,
                                            modifier = Modifier
                                                .background(CustomColors.onTertiary)
                                                .padding(8.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                Row {
                                    Column {

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Start
                                        ) {
                                            Checkbox(
                                                checked = isNotTransaction,
                                                onCheckedChange = { value ->
                                                    isTransactionUpdated = true
                                                    isNotTransaction = value
                                                    transaction =
                                                        transaction?.copy(isTransaction = !value)!!
                                                },
                                                modifier = Modifier.offset(x = (-11).dp)
                                            )
                                            Text(
                                                text = "Not a transaction",
                                                color = toggleTextColor,
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

                        Surface(
                            shape = CircleShape,
                            modifier = Modifier
                                .size(48.dp)
                                .offset(x = 24.dp, y = (-24).dp)
                                .background(
                                    Color.Transparent
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ShoppingCart,
                                contentDescription = "Category Icon",
                                tint = iconColor,
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(shapeColor)
                                    .padding(8.dp)
                            )
                        }
                    }
                } else {
                    Text("Loading...")
                }
            }
        }
    })

}
