package com.grex.vyay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.grex.vyay.ui.theme.PowderBlue

@Composable
fun ReportsScreen(activity: MainActivity, padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PowderBlue)
            .padding(padding)
    ) {
        Text(text = "Expense Reports", color = Color.Black)
    }
}