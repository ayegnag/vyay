package com.grex.vyay

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.grex.vyay.ui.components.CustomInputField
import com.grex.vyay.ui.components.InputFieldType
import com.grex.vyay.ui.components.SharedViewModel
import com.grex.vyay.ui.theme.CustomColors


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    activity: MainActivity, padding: PaddingValues, viewModel: SettingsViewModel,
    sharedViewModel: SharedViewModel,
) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val systemUiController = rememberSystemUiController()

    val backgroundTopColor = MaterialTheme.colorScheme.background
    val backgroundBottomColor = MaterialTheme.colorScheme.onBackground
    var showExpenseLimitSetting by remember {
        mutableStateOf(false)
    }
    var showDeveloperSetting by remember {
        mutableStateOf(false)
    }
    val expenseThreshold by sharedViewModel.prefExpenseThreshold.collectAsState()
    LaunchedEffect(Unit) {
        if (expenseThreshold != 0.0) {
            showExpenseLimitSetting = true
        }
    }
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
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = CustomColors.backgroundPrimaryTop,
                    titleContentColor = CustomColors.onPrimary,
                ),
                title = { Text("Settings") },
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
                                CustomColors.backgroundPrimaryTop,
                                CustomColors.backgroundPrimaryBottom
                            )
                        )
                    )
                    .padding(padding)
            ) {
                LazyColumn() {
                    item {
                        Column(
                            modifier = Modifier
                                .padding(start = 16.dp, end = 16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Developer Options",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                TextButton(
                                    onClick = {
                                        showDeveloperSetting = !showDeveloperSetting
                                    }
                                ) {
                                    Text(
                                        text = if (showDeveloperSetting) "Hide" else "Show",
                                        modifier = Modifier
                                            .padding(start = 12.dp)
                                    )
                                }
                            }
                            if (showDeveloperSetting) {
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Reset Data Options",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier
                                        .padding(bottom = 16.dp),
                                )
                                Button(
                                    onClick = {
                                        userPreferences.resetUserPreference()
                                        val toast =
                                            Toast.makeText(
                                                context,
                                                "Profile data erased!",
                                                Toast.LENGTH_SHORT
                                            )
                                        toast.show()
                                    }
                                ) {
                                    val iconWidth = 24.dp
                                    Icon(
                                        imageVector = Icons.Outlined.Warning,
                                        contentDescription = "drawable icons",
                                        tint = Color.Unspecified
                                    )
                                    Text(
                                        "Reset Profile",
                                        modifier = Modifier
                                            .padding(start = 12.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        viewModel.deleteAllMessages()
                                        val toast = Toast.makeText(
                                            context,
                                            "SMS DB records erased!",
                                            Toast.LENGTH_SHORT
                                        )
                                        toast.show()
                                    }
                                ) {
                                    val iconWidth = 24.dp
                                    Icon(
                                        imageVector = Icons.Outlined.Warning,
                                        contentDescription = "drawable icons",
                                        tint = Color.Unspecified
                                    )
                                    Text(
                                        "Reset SMS Data",
                                        modifier = Modifier
                                            .padding(start = 12.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider()
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        if (showExpenseLimitSetting) {
                            MonthlyExpenseThresholdSettings(
                                expenseLimit = expenseThreshold.toString(),
                                onSave = { newValue ->
                                    sharedViewModel.setExpenseThreshold(newValue.toDouble())
                                })
                        }
                    }
                }
            }
        }
    )
}


@Composable
fun MonthlyExpenseThresholdSettings(expenseLimit: String, onSave: (String) -> Unit) {
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
        var showLimitInput by remember { mutableStateOf(false) }
        var expenseLimitValue by remember { mutableStateOf(expenseLimit) }

        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "MONTHLY EXPENSE LIMIT",
                    style = MaterialTheme.typography.labelMedium,
                    color = CustomColors.onPrimary
                )
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "Update how much you can spend each month.",
                    color = CustomColors.onPrimaryDim,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Left,
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(top = 8.dp)
                )

                if (showLimitInput) {
                    IconButton(
                        onClick = { showLimitInput = false },
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
                            showLimitInput = true
                        },
                        modifier = Modifier.weight(0.3f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = CustomColors.active,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Edit", color = CustomColors.active)
                    }
                }

            }
            if (showLimitInput) {
                Row {
                    Spacer(modifier = Modifier.width(16.dp))
                    CustomInputField(
                        value = expenseLimitValue,
                        placeholder = "Monthly Limit",
                        inputType = InputFieldType.CURRENCY,
                        onValueChange = { expenseLimitValue = it },
                        modifier = Modifier.padding(top = 8.dp),
                        onSaveClick = {
                            onSave(expenseLimitValue)
                            showLimitInput = false
                        }
                    )
                }
            }
        }
    }
}
