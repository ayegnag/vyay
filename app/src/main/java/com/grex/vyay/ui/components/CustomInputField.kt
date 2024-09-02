package com.grex.vyay.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grex.vyay.ui.theme.CustomColors
import java.text.DecimalFormat
import java.util.Currency

enum class InputFieldType {
    TEXT, NUMBER, CURRENCY
}

@Composable
fun CustomInputField(
    value: String,
    onValueChange: (String) -> Unit,
    inputType: InputFieldType = InputFieldType.TEXT,
    modifier: Modifier = Modifier,
    placeholder: String = "Enter amount",
    underlineColorFocused: Color = CustomColors.onPrimary,
    underlineColorUnfocused: Color = CustomColors.onPrimaryDim,
    textColor: Color = CustomColors.onPrimary
) {
    var isFocused by remember { mutableStateOf(false) }
    val currencySymbol = Currency.getInstance(java.util.Locale("en", "in")).symbol
    val numberFormat = remember {
        DecimalFormat("#,##0.00").apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }

    var displayValue by remember(value, isFocused) {
        mutableStateOf(
            when {
                inputType == InputFieldType.CURRENCY && !isFocused ->
                    formatCurrencyValue(value, numberFormat)
                else -> value
            }
        )
    }

    val underlineColor by animateColorAsState(
        targetValue = if (isFocused) underlineColorFocused else underlineColorUnfocused,
        label = ""
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.Transparent)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.BottomStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(bottom = 4.dp)
                    .background(Color.Transparent)
            ) {
                if (inputType == InputFieldType.CURRENCY && displayValue.isNotEmpty()) {
                    Text(
                        text = currencySymbol,
                        color = textColor,
                        style = TextStyle(fontSize = 16.sp)
                    )
                }

                BasicTextField(
                    value = displayValue,
                    onValueChange = { newValue ->
                        when (inputType) {
                            InputFieldType.TEXT -> {
                                displayValue = newValue
                                onValueChange(newValue)
                            }
                            InputFieldType.NUMBER, InputFieldType.CURRENCY -> {
                                val numericValue = newValue.replace(Regex("[^0-9]"), "")
                                if (numericValue.length <= 15) { // Prevent overflow
                                    displayValue = numericValue
                                    onValueChange(numericValue)
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused != isFocused) {
                                isFocused = focusState.isFocused
                                if (inputType == InputFieldType.CURRENCY) {
                                    if (isFocused) {
                                        // Unformat when focused
                                        displayValue = value.replace(Regex("[^0-9]"), "")
                                    } else {
                                        // Format when unfocused
                                        displayValue = formatCurrencyValue(value, numberFormat)
                                        onValueChange(displayValue.replace(Regex("[^0-9.]"), ""))
                                    }
                                }
                            }
                        },
                    textStyle = TextStyle(color = textColor, fontSize = 16.sp),
                    keyboardOptions = when (inputType) {
                        InputFieldType.TEXT -> KeyboardOptions.Default
                        InputFieldType.NUMBER, InputFieldType.CURRENCY -> KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    },
                    cursorBrush = SolidColor(textColor),
                    decorationBox = { innerTextField ->
                        Box {
                            if (displayValue.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    color = underlineColorUnfocused,
                                    style = TextStyle(fontSize = 16.sp)
                                )
                            }
                            innerTextField()
                        }
                    },
                    singleLine = true
                )
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier.size(32.dp).padding(end = 16.dp),
                    tint = underlineColor
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(underlineColor)
            )
        }
    }
}

fun formatCurrencyValue(value: String, numberFormat: DecimalFormat): String {
    return value.toBigDecimalOrNull()?.let { numberFormat.format(it) } ?: value
}