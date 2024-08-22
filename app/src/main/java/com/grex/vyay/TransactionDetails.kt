package com.grex.vyay

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.grex.vyay.ui.theme.CopperLight
import com.grex.vyay.ui.theme.DarkwingPurple
import com.grex.vyay.ui.theme.Geru
import com.grex.vyay.ui.theme.Grey
import com.grex.vyay.ui.theme.LimeGreen
import com.grex.vyay.ui.theme.MistyRose
import com.grex.vyay.ui.theme.PetalRed
import com.grex.vyay.ui.theme.PurpleGrey40
import com.grex.vyay.ui.theme.backgroundPrimaryBottom
import com.grex.vyay.ui.theme.backgroundPrimaryTop
import com.grex.vyay.ui.theme.primaryInactive
import com.grex.vyay.ui.theme.secondaryInactive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetails(
    transactionId: String?,
    isManual: Boolean,
    navController: NavController,
    isTransactionRecordUpdated: Boolean,
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

    DisposableEffect(systemUiController) {
        systemUiController.setStatusBarColor(
            color = backgroundPrimaryTop,
            darkIcons = false // Set to false for light icons
        )
        systemUiController.setNavigationBarColor(
            color = backgroundPrimaryBottom,
            darkIcons = false // Set to false for light icons
        )
        onDispose {}
    }

    LaunchedEffect(transactionId, isManual) {
        transaction = transactionId.let {
            it?.let { it1 ->
                appDao.getTransactionById(
                    it1.toInt(),
                    isManual
                )
            }
        }
        backUpState = transaction?.copy()
        isNotTransaction = transaction?.isTransaction == false
    }
    var showSmsSource by remember { mutableStateOf<Boolean>(false) }
    fun getTransactionColor(type: String? = null): Color {
        return when (type) {
            "expense" -> PetalRed
            "income" -> LimeGreen
            else -> PurpleGrey40
        }
    }

    fun onNotTransactionChange(value: Boolean) {
        isTransactionUpdated = true
        isNotTransaction = value
        transaction?.isTransaction ?: value
        Log.d("NotTransaction", value.toString())
    }

    fun onCancelClick() {
        isTransactionUpdated = false
        isNotTransaction = backUpState?.isTransaction == false
        transaction = backUpState?.copy()
    }

    suspend fun onSaveClick() {
        transaction?.let { appDao.updateTransaction(it) }
        onAckUpdate(true)
        navController.navigateUp()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = backgroundPrimaryTop,
                    titleContentColor = Grey,
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
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel"
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
                        IconButton(onClick = {
                            CoroutineScope(Dispatchers.Main).launch {
                                onSaveClick()
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
                                backgroundPrimaryTop,
                                backgroundPrimaryBottom
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
                    if (transaction != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxSize(),
                            color = DarkwingPurple
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(24.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                        .background(Color.Transparent)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(
                                                Color.Transparent
                                            )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ShoppingCart,
                                            contentDescription = "Category Icon",
                                            tint = MistyRose,
                                            modifier = Modifier
                                                .size(38.dp)
                                                .background(secondaryInactive)
                                                .padding(8.dp)
                                        )
                                    }
                                    Text(
                                        "${transaction?.amount?.let { utils.getCurrencyFormat(it) }}",
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(start = 24.dp),
                                        color = CopperLight
                                    )
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    "${transaction?.receivedAt}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    modifier = Modifier
                                        .drawBehind {
                                            val strokeWidthPx = 1.dp.toPx()
                                            val verticalOffset = size.height + 2.sp.toPx()
                                            drawLine(
                                                color = primaryInactive,
                                                strokeWidth = strokeWidthPx,
                                                start = Offset(0f, verticalOffset),
                                                end = Offset(size.width, verticalOffset)
                                            )
                                        },
                                    color = MistyRose
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Row {
                                    Column {
                                        Row {
                                            if (transaction!!.transactionType == "expense") {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                                    contentDescription = "expense",
                                                    modifier = Modifier.rotate(-45f),
                                                    tint = Geru
                                                )
                                            } else if (transaction!!.transactionType == "income") {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                                    contentDescription = "income",
                                                    modifier = Modifier.rotate(-45f),
                                                    tint = LimeGreen
                                                )
                                            }
                                            Text(
                                                "${transaction!!.transactionType}",
                                                style = MaterialTheme.typography.bodyLarge,
                                                modifier = Modifier
                                                    .padding(start = 10.dp),
                                                color = getTransactionColor(transaction!!.transactionType)
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            transaction?.isTransaction?.let {
                                                Checkbox(
                                                    checked = isNotTransaction,
                                                    onCheckedChange = { value ->
                                                        isTransactionUpdated = true
                                                        isNotTransaction = value
                                                        transaction?.isTransaction = !value
                                                    }
                                                )
                                            }
                                            Text(
                                                text = "Not a transaction",
                                                color = secondaryInactive,
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.padding(start = 10.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "Mode",
                                        color = secondaryInactive,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    transaction!!.transactionMode?.let {
                                        Text(
                                            text = it,
                                            color = MistyRose,
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.padding(start = 10.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Data Source",
                                        color = secondaryInactive,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Text(
                                        text = transaction!!.source.uppercase(Locale("en")),
                                        color = MistyRose,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(start = 10.dp)
                                    )
                                    if (transaction!!.source == "sms") {
                                        Spacer(Modifier.weight(1f))
                                        if (showSmsSource) {
                                            TextButton(onClick = { showSmsSource = false }) {
                                                Text(text = "Hide")
                                            }
                                        } else {
                                            TextButton(onClick = { showSmsSource = true }) {
                                                Text(text = "Show")
                                            }
                                        }
                                    }
                                }
                                if (showSmsSource) {
                                    Text(text = transaction!!.body)
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Tags",
                                        color = secondaryInactive,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    TextButton(onClick = { /*TODO*/ }) {
                                        Text(text = "+ Add Tag")
                                    }
                                }

                            }
                        }
                    } else {
                        Text("Loading...")
                    }
                }
            }
        }
    )

}

@Preview
@Composable
fun TransactionDetailPreview() {
    val utils = Utilities()
    val transaction = TransactionRecord(
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
        tags = ""
    )
    var showSmsSource by remember { mutableStateOf<Boolean>(false) }
    fun getTransactionColor(type: String? = null): Color {
        return when (type) {
            "expense" -> PetalRed
            "income" -> LimeGreen
            else -> PurpleGrey40
        }
    }
    Box(
        modifier = Modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundPrimaryTop,
                        backgroundPrimaryBottom
                    )
                )
            )
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.Center)
        ) {
            if (transaction != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxSize(),
                    color = DarkwingPurple
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .background(Color.Transparent)
                        ) {
                            Surface(
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(
                                        Color.Transparent
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ShoppingCart,
                                    contentDescription = "Category Icon",
                                    tint = MistyRose,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(secondaryInactive)
                                        .padding(8.dp)
                                )
                            }
                            Text(
                                "${transaction?.amount?.let { utils.getCurrencyFormat(it) }}",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(start = 24.dp),
                                color = CopperLight
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "${transaction?.receivedAt}",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            modifier = Modifier
                                .drawBehind {
                                    val strokeWidthPx = 1.dp.toPx()
                                    val verticalOffset = size.height + 2.sp.toPx()
                                    drawLine(
                                        color = primaryInactive,
                                        strokeWidth = strokeWidthPx,
                                        start = Offset(0f, verticalOffset),
                                        end = Offset(size.width, verticalOffset)
                                    )
                                },
                            color = MistyRose
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row {
                            Column {
                                Row {
                                    if (transaction.transactionType == "expense") {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                            contentDescription = "expense",
                                            modifier = Modifier.rotate(-45f),
                                            tint = Geru
                                        )
                                    } else if (transaction.transactionType == "income") {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                            contentDescription = "income",
                                            modifier = Modifier.rotate(-45f),
                                            tint = LimeGreen
                                        )
                                    }
                                    Text(
                                        "${transaction.transactionType}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier
                                            .padding(start = 10.dp),
                                        color = getTransactionColor(transaction.transactionType)
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = false, onCheckedChange = null)
                                    Text(
                                        text = "Not a transaction",
                                        color = secondaryInactive,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(start = 10.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "Mode",
                                color = secondaryInactive,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            transaction.transactionMode?.let {
                                Text(
                                    text = it,
                                    color = MistyRose,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 10.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Data Source",
                                color = secondaryInactive,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = "${transaction.source.uppercase(Locale("en"))}",
                                color = MistyRose,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 10.dp)
                            )
                            if (transaction.source == "sms") {
                                Spacer(Modifier.weight(1f))
                                if (showSmsSource) {
                                    TextButton(onClick = { showSmsSource = false }) {
                                        Text(text = "Hide")
                                    }
                                } else {
                                    TextButton(onClick = { showSmsSource = true }) {
                                        Text(text = "Show")
                                    }
                                }
                            }
                        }
                        if (showSmsSource) {
                            Text(text = transaction.body)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Tags",
                                color = secondaryInactive,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            TextButton(onClick = { showSmsSource = true }) {
                                Text(text = "+ Add Tag")
                            }
                        }

                    }
                }
            } else {
                Text("Loading...")
            }
        }
    }
}