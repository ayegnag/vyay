package com.grex.vyay

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.accompanist.flowlayout.FlowRow
import com.grex.vyay.ui.theme.CustomColors

//enum class TransactionType {
//    EXPENSE, INCOME
//}

@Composable
fun TagSelector(
    tagsState: MutableState<String>,
    type: String?,
    key: Any,
    modifier: Modifier,
    onTagsUpdated: (String) -> Unit
) {
    var selectedTags by remember(key) { mutableStateOf(tagsState.value?.split(", ")?.filter { it.isNotEmpty() } ?: emptyList()) }
    var showTagDialog by remember { mutableStateOf(false) }

    Column(modifier) {
        FlowRow(
            mainAxisSpacing = 8.dp,
            crossAxisSpacing = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            selectedTags.forEach { tag ->
                TagPill(tag)
            }
            AddTagButton(
                onClick = { showTagDialog = true },
                hasSelectedTags = selectedTags.isNotEmpty()
            )
        }
    }

    if (showTagDialog) {
        if (type != null) {
            TagSelectionDialog(
                currentTags = selectedTags,
                type = type,
                onDismiss = { showTagDialog = false },
                onTagsSelected = { newTags ->
                    selectedTags = newTags
                    val newTagsString = newTags.joinToString(", ")
                    tagsState.value = newTagsString
                    onTagsUpdated(newTagsString)
                    showTagDialog = false
                }
            )
        }
    }
}

@Composable
fun TagPill(tag: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CustomColors.secondary,
        contentColor = CustomColors.onPrimary,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Text(
            text = tag,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun AddTagButton(onClick: () -> Unit, hasSelectedTags: Boolean) {
    TextButton(
        colors = ButtonColors(
            containerColor = Color.Transparent,
            contentColor = CustomColors.active,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = CustomColors.onPrimaryInactive
        ),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(
            if (hasSelectedTags) Icons.Default.Edit else Icons.Default.Add,
            contentDescription = if (hasSelectedTags) "Edit Tags" else "Add Tags",
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(if (hasSelectedTags) "Edit Tags" else "Add Tags")
    }
}

@Composable
fun TagSelectionDialog(
    currentTags: List<String>,
    type: String,
    onDismiss: () -> Unit,
    onTagsSelected: (List<String>) -> Unit
) {
    val expensePredefinedTags =
        listOf("Groceries", "Bills", "EMI", "Fuel", "Rent", "Health", "Food", "Shopping", "Investment", "Monthly", "Annual", "Work", "Personal", "Shared", "Tax", "Subscription", "Gift", "Entertainment", "Education", "Travel", "Luxury")
    val incomePredefinedTags =
        listOf("Salary", "Bonus", "Commission", "Tax Refund", "Dividend", "Interest", "Rental Income", "Pension", "Gift", "Loan", "Reimbursement", "Cashback")

    val predefinedTags =
        if (type == "expense") expensePredefinedTags else incomePredefinedTags
    var selectedTags by remember { mutableStateOf(currentTags.toSet()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CustomColors.onTertiary
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Tags", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                FlowRow(
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    predefinedTags.forEach { tag ->
                        SelectableTagPill(
                            tag = tag,
                            isSelected = tag in selectedTags,
                            onTagSelected = { isSelected ->
                                selectedTags = if (isSelected) {
                                    selectedTags + tag
                                } else {
                                    selectedTags - tag
                                }
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = { onTagsSelected(selectedTags.toList()) },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Confirm")
                    }
                }
            }
        }
    }
}

@Composable
fun SelectableTagPill(
    tag: String,
    isSelected: Boolean,
    onTagSelected: (Boolean) -> Unit
) {
    val backgroundColor = if (isSelected) CustomColors.primary else Color.Transparent
    val contentColor = if (isSelected) CustomColors.onPrimary else CustomColors.primary

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        contentColor = contentColor,
        border = BorderStroke(1.dp, CustomColors.primary),
        modifier = Modifier.clickable { onTagSelected(!isSelected) }
    ) {
        Text(
            text = tag,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}