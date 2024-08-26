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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransaction(
    navController: NavController,
    smsAnalysisService: SmsAnalysisService,
    onAckUpdate: (Boolean) -> Unit
) {
    val applicationContext: Context = VyayApp.instance.applicationContext
    val database: AppDatabase = AppDatabase.getDatabase(applicationContext)
    val appDao: AppDao = database.appDao()
    var transaction by remember { mutableStateOf<TransactionRecord?>(null) }
    var backUpState by remember { mutableStateOf<TransactionRecord?>(null) }
    val systemUiController = rememberSystemUiController()
    val utils = Utilities()

    // Colors
    val expenseColorCode = MaterialTheme.colorScheme.tertiary
    val incomeColorCode = MaterialTheme.colorScheme.secondary
    val amountColorCode = MaterialTheme.colorScheme.onTertiary
    val shapeColorCode = MaterialTheme.colorScheme.surfaceBright
    val iconColorCode = MaterialTheme.colorScheme.onSurface
    val toggleTextColorCode = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val textValueColor = MaterialTheme.colorScheme.onSecondary
    val backgroundTopColor = MaterialTheme.colorScheme.background
    val backgroundBottomColor = MaterialTheme.colorScheme.onBackground

    var isTransactionUpdated by remember { mutableStateOf(false) }
    var isNotTransaction by remember { mutableStateOf(false) }

    DisposableEffect(systemUiController) {
        systemUiController.setStatusBarColor(
            color = backgroundTopColor,
            darkIcons = false // Set to false for light icons
        )
        systemUiController.setNavigationBarColor(
            color = backgroundBottomColor,
            darkIcons = false // Set to false for light icons
        )
        onDispose {}
    }

    var showSmsSource by remember { mutableStateOf<Boolean>(false) }
    fun getTransactionColor(type: String? = null): Color {
        return when (type) {
            "expense" -> expenseColorCode
            "income" -> incomeColorCode
            else -> toggleTextColorCode
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
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
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
                                MaterialTheme.colorScheme.background,
                                backgroundBottomColor
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
                            color = surfaceColor
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
                                            tint = iconColorCode,
                                            modifier = Modifier
                                                .size(38.dp)
                                                .background(MaterialTheme.colorScheme.onSurface)
                                                .padding(8.dp)
                                        )
                                    }
                                    Text(
                                        "${transaction?.amount?.let { utils.getCurrencyFormat(it) }}",
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(start = 24.dp),
                                        color = toggleTextColorCode
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
                                                color = textValueColor,
                                                strokeWidth = strokeWidthPx,
                                                start = Offset(0f, verticalOffset),
                                                end = Offset(size.width, verticalOffset)
                                            )
                                        },
                                    color = textValueColor
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
                                                    tint = expenseColorCode
                                                )
                                            } else if (transaction!!.transactionType == "income") {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                                    contentDescription = "income",
                                                    modifier = Modifier.rotate(-45f),
                                                    tint = incomeColorCode
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
                                                color = MaterialTheme.colorScheme.onSurface,
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
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    transaction!!.transactionMode?.let {
                                        Text(
                                            text = it,
                                            color = textValueColor,
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.padding(start = 10.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Data Source",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Text(
                                        text = transaction!!.source.uppercase(Locale("en")),
                                        color = textValueColor,
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
                                        color = MaterialTheme.colorScheme.onSurface,
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
