package com.grex.vyay.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.grex.vyay.ui.theme.CustomColors
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

//@Composable
//fun MonthYearSelector(
//    initialDate: YearMonth,
//    lastSupportedDate: YearMonth,
//    onDateSelected: (YearMonth) -> Unit
//) {
//    var selectedDate by remember { mutableStateOf(initialDate) }
//    var showDialog by remember { mutableStateOf(false) }
//
//    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
//    val currentYearMonth = YearMonth.now()
//
//    TextButton(
//        onClick = { showDialog = true },
//        colors = ButtonDefaults.textButtonColors(
//            contentColor = CustomColors.active, // Color when enabled
//            disabledContentColor = CustomColors.secondaryInactive   // Custom color when disabled
//        )
//    ) {
//        Text("${selectedDate.format(formatter)} ▼")
//    }
//
//    if (showDialog) {
//        Dialog(onDismissRequest = { showDialog = false }) {
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                colors = CardDefaults.cardColors(CustomColors.surface)
//            ) {
//                Column(modifier = Modifier.padding(16.dp)) {
//                    Text(
//                        text = selectedDate.format(formatter),
//                        style = MaterialTheme.typography.headlineSmall,
//                        modifier = Modifier.fillMaxWidth(),
//                        textAlign = TextAlign.Center
//                    )
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween
//                    ) {
//                        IconButton(
//                            onClick = {
//                                if (selectedDate.year > lastSupportedDate.year) {
//                                    selectedDate = selectedDate.minusYears(1)
//                                }
//                            },
//                            enabled = selectedDate.year > lastSupportedDate.year
//                        ) {
//                            Text(
//                                text = "◀",
//                                color = if (selectedDate.year > lastSupportedDate.year)
//                                    CustomColors.primary
//                                else
//                                    CustomColors.onPrimaryInactive
//                            )
//                        }
//                        Text(
//                            text = selectedDate.year.toString(),
//                            style = MaterialTheme.typography.titleMedium
//                        )
//                        IconButton(
//                            onClick = {
//                                if (selectedDate.year < currentYearMonth.year) {
//                                    selectedDate = selectedDate.plusYears(1)
//                                }
//                            },
//                            enabled = selectedDate.year < currentYearMonth.year
//                        ) {
//                            Text(
//                                text = "▶", color = if (selectedDate.year < currentYearMonth.year)
//                                    CustomColors.primary
//                                else
//                                    CustomColors.onPrimaryInactive
//                            )
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    LazyVerticalGrid(
//                        columns = GridCells.Fixed(3),
//                        verticalArrangement = Arrangement.spacedBy(8.dp),
//                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        items(
//                            count = 12,
//                            key = { it },
//                            itemContent = { index ->
//                                val month = YearMonth.of(selectedDate.year, index + 1)
//                                val isEnabled = month.isAfter(lastSupportedDate.minusMonths(1)) &&
//                                        !month.isAfter(currentYearMonth)
//                                Box(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .clickable(enabled = isEnabled) {
//                                            selectedDate = month
//                                            onDateSelected(selectedDate)
//                                            showDialog = false
//                                        }
//                                        .padding(8.dp),
//                                    contentAlignment = Alignment.Center
//                                ) {
//                                    Text(
//                                        text = month.month.getDisplayName(
//                                            TextStyle.SHORT,
//                                            Locale.getDefault()
//                                        ),
//                                        color = if (isEnabled) CustomColors.active
//                                        else CustomColors.onSecondaryInactive
//                                    )
//                                }
//                            }
//                        )
//                    }
//                }
//            }
//        }
//    }
//}

@Composable
fun MonthYearSelector(
    selectedDate: YearMonth, // Changed from initialDate to reflect current state
    lastSupportedDate: YearMonth,
    onDateSelected: (YearMonth) -> Unit
) {
    // We only need internal state for the Dialog's temporary browsing,
    // or we can use the passed selectedDate directly.
    // To allow the user to flip through years in the dialog WITHOUT
    // updating the parent until they click a month, we use a local "browsing" state.
    var browsingDate by remember(selectedDate) { mutableStateOf(selectedDate) }
    var showDialog by remember { mutableStateOf(false) }

    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    val currentYearMonth = YearMonth.now()

    TextButton(
        onClick = {
            browsingDate = selectedDate // Reset browsing to actual selection when opening
            showDialog = true
        },
        colors = ButtonDefaults.textButtonColors(
            contentColor = CustomColors.active,
            disabledContentColor = CustomColors.secondaryInactive
        )
    ) {
        // This now correctly reflects the date passed from the parent (e.g. from the Router)
        Text("${selectedDate.format(formatter)} ▼")
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(CustomColors.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = browsingDate.format(formatter),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                if (browsingDate.year > lastSupportedDate.year) {
                                    browsingDate = browsingDate.minusYears(1)
                                }
                            },
                            enabled = browsingDate.year > lastSupportedDate.year
                        ) {
                            Text(
                                text = "◀",
                                color = if (browsingDate.year > lastSupportedDate.year)
                                    CustomColors.primary
                                else
                                    CustomColors.onPrimaryInactive
                            )
                        }
                        Text(
                            text = browsingDate.year.toString(),
                            style = MaterialTheme.typography.titleMedium
                        )
                        IconButton(
                            onClick = {
                                if (browsingDate.year < currentYearMonth.year) {
                                    browsingDate = browsingDate.plusYears(1)
                                }
                            },
                            enabled = browsingDate.year < currentYearMonth.year
                        ) {
                            Text(
                                text = "▶",
                                color = if (browsingDate.year < currentYearMonth.year)
                                    CustomColors.primary
                                else
                                    CustomColors.onPrimaryInactive
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            count = 12,
                            key = { it },
                            itemContent = { index ->
                                val month = YearMonth.of(browsingDate.year, index + 1)
                                val isEnabled = month.isAfter(lastSupportedDate.minusMonths(1)) &&
                                        !month.isAfter(currentYearMonth)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = isEnabled) {
                                            // NOTIFY THE PARENT
                                            onDateSelected(month)
                                            showDialog = false
                                        }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = month.month.getDisplayName(
                                            TextStyle.SHORT, // From java.time.format.TextStyle
                                            Locale.getDefault() // From java.util.Locale
                                        ),
                                        // Highlight the currently selected month
                                        color = if (month == selectedDate) CustomColors.primary
                                        else if (isEnabled) CustomColors.active
                                        else CustomColors.onSecondaryInactive
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}