package com.grex.vyay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.grex.vyay.ui.theme.CustomColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    onSearchQueryChanged: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(key1 = Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(CustomColors.onTertiary)
            .height(42.dp)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search",
                tint = if (searchQuery.isEmpty()) CustomColors.onSecondary else CustomColors.onPrimary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (searchQuery.isEmpty()) {
                    Text(
                        "Search transactions",
                        color = CustomColors.onSecondary,
                        modifier = Modifier
                            .fillMaxHeight()
                            .height(42.dp)
                            .wrapContentHeight(align = Alignment.CenterVertically),
                    )
                }

                BasicTextField(
                    value = searchQuery,
                    onValueChange = { newQuery ->
                        searchQuery = newQuery
                        onSearchQueryChanged(newQuery.trim())
                    },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = CustomColors.onPrimary),
                    modifier = Modifier
                        .fillMaxSize()
                        .height(42.dp)
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .focusRequester(focusRequester),
                    cursorBrush = SolidColor(CustomColors.onPrimary)
                )
            }

            IconButton(
                onClick = {
                    onClose()
                    onSearchQueryChanged("")
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
//    TextField(
//        value = searchQuery,
//        onValueChange = { newQuery ->
//            searchQuery = newQuery
//            onSearchQueryChanged(newQuery.trim())
//        },
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(42.dp)
//            .background(CustomColors.onTertiary)
//            .padding(top = 0.dp, start = 8.dp, end = 8.dp, bottom = 0.dp),
//        placeholder = { Text("Search transactions", modifier = Modifier.fillMaxHeight()) },
//        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
//        colors = TextFieldDefaults.colors(
//            focusedTextColor = CustomColors.onPrimary,
//            unfocusedTextColor = CustomColors.onPrimary,
//            focusedPlaceholderColor = CustomColors.onPrimary,
//            unfocusedPlaceholderColor = CustomColors.onSecondary,
//            focusedContainerColor = CustomColors.tertiary,
//            unfocusedContainerColor = CustomColors.onTertiary,
//            focusedIndicatorColor = Color.Transparent,
//            unfocusedIndicatorColor = Color.Transparent,
//            focusedLeadingIconColor = CustomColors.onPrimary,
//            unfocusedLeadingIconColor = CustomColors.onSecondary,
//            focusedTrailingIconColor = CustomColors.onPrimary,
//            unfocusedTrailingIconColor = CustomColors.onSecondary,
//        ),
//        trailingIcon = {
//            IconButton(onClick = {
//                onClose()
//            }) {
//                Icon(
//                    imageVector = Icons.Filled.Close,
//                    contentDescription = "Close"
//                )
//            }
//        },
//    )
}