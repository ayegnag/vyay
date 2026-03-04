package com.grex.vyay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grex.vyay.ui.theme.CustomColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicSelectTextField(
    selectedValue: String?,
    options: List<String>,
    label: String,
    placeholder: String,
    onValueChangedEvent: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val shape = if (expanded) RoundedCornerShape(8.dp).copy(
        bottomEnd = CornerSize(0.dp),
        bottomStart = CornerSize(0.dp)
    )
    else RoundedCornerShape(8.dp)
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        TextField(
            readOnly = true,
            value = selectedValue ?: "",
            onValueChange = {},
            label = null,
            textStyle = TextStyle(fontSize = 16.sp, color = CustomColors.onPrimaryDim),
            shape = shape,
            placeholder = { Text(text = placeholder, color = CustomColors.onPrimaryDim) },
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                    contentDescription = "Dropdown arrow",
                    modifier = Modifier.size(20.dp),
                    tint = CustomColors.onPrimaryDim
                )
            },
            colors = TextFieldDefaults.colors(
                unfocusedIndicatorColor = CustomColors.onPrimaryDim.copy(alpha = 0.12f),
                focusedIndicatorColor = CustomColors.onPrimary,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                unfocusedTextColor = CustomColors.onPrimary,
                focusedTextColor = CustomColors.onPrimary,
                cursorColor = CustomColors.onPrimary,
            ),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .padding(0.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(CustomColors.onTertiary)
        ) {
            options.forEach { option: String ->
                DropdownMenuItem(
                    text = { Text(text = option) },
                    onClick = {
                        expanded = false
                        onValueChangedEvent(option)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DynamicSelectTextFieldPreview() {
    val options = listOf("Option 1", "Option 2", "Option 3")
    var selectedValue by remember { mutableStateOf<String?>(null) }

    Box() {
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            color = CustomColors.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .padding(top = 12.dp)
            ) {
                DynamicSelectTextField(
                    selectedValue = selectedValue,
                    options = options,
                    label = "Select an option",
                    placeholder = "Choose an option",
                    onValueChangedEvent = { selectedValue = it }
                )
            }
        }
    }
}