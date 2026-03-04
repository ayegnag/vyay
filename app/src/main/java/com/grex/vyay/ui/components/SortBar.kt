package com.grex.vyay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.GroupWork
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.grex.vyay.ui.theme.CustomColors

enum class SortType {
    ASCENDING, DESCENDING, GROUP
}

@Composable
fun SortBar(
    onSortTypeChanged: (SortType) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSortType by remember { mutableStateOf(SortType.ASCENDING) }

    LaunchedEffect(key1 = Unit) {
        onSortTypeChanged(SortType.ASCENDING)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Icon(
            Icons.Default.SwapVert,
            contentDescription = "Sort",
            tint = CustomColors.onPrimaryDim
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = {
                selectedSortType = SortType.ASCENDING
                onSortTypeChanged(SortType.ASCENDING)
            },
            modifier = Modifier
                .padding(start = 8.dp)
                .background(
                    color = if (selectedSortType == SortType.ASCENDING) {
                        CustomColors.secondary
                    } else { Color.Transparent}, shape = RoundedCornerShape(4.dp)
                )
        ) {
            Icon(
                Icons.Outlined.ArrowUpward,
                contentDescription = "Sort ascending",
                tint = CustomColors.active
            )
        }
        IconButton(
            onClick = {
                selectedSortType = SortType.DESCENDING
                onSortTypeChanged(SortType.DESCENDING)
            },
            modifier = Modifier
                .padding(start = 8.dp)
                .background(
                    color = if (selectedSortType == SortType.DESCENDING) {
                        CustomColors.secondary
                    } else { Color.Transparent}, shape = RoundedCornerShape(4.dp)
                )
        ) {
            Icon(
                Icons.Outlined.ArrowDownward,
                contentDescription = "Sort descending",
                tint = CustomColors.active
            )
        }
        IconButton(
            onClick = {
                selectedSortType = SortType.GROUP
                onSortTypeChanged(SortType.GROUP)
            },
            modifier = Modifier
                .padding(start = 8.dp)
                .background(
                    color = if (selectedSortType == SortType.GROUP) {
                        CustomColors.secondary
                    } else { Color.Transparent}, shape = RoundedCornerShape(4.dp)
                )
        ) {
            Icon(
                Icons.Outlined.GroupWork,
                contentDescription = "Sort group",
                tint = CustomColors.active
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = {
                onSortTypeChanged(SortType.DESCENDING )
                onClose()
            },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Close",
                tint = CustomColors.onPrimary
            )
        }
    }
}