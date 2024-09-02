package com.grex.vyay.ui.components


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.grex.vyay.MonthlyTotal
import com.grex.vyay.Utilities
import com.grex.vyay.ui.theme.CustomColors
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow


//data class Expense(val month: String, val amount: Float)

@Composable
fun ExpenseBarChart(
    expenses: List<MonthlyTotal>,
    incomes: List<MonthlyTotal>,
    modifier: Modifier = Modifier,
    onItemClick: (String) -> Unit
) {
    var maxExpense by remember { mutableFloatStateOf(0f) }
    var maxIncome by remember { mutableFloatStateOf(0f) }
    var maxAmount by remember { mutableFloatStateOf(0f) }
    val utils = Utilities()

    val scrollState = rememberScrollState()

    // Function to round up to nearest nice number
    fun roundUpToNiceNumber(number: Float): Float {
        val magnitude = 10f.pow(floor(log10(number)))
        return ceil(number / magnitude) * magnitude
    }

    // Function to format amount in K, L, Cr
    fun formatAmount(amount: Float): String {
        return when {
            amount >= 10_000_000 -> String.format("%.1fCr", amount / 10_000_000)
            amount >= 100_000 -> String.format("%.1fL", amount / 100_000)
            amount >= 1_000 -> String.format("%.0fK", amount / 1_000)
            else -> String.format("%.0f", amount)
        }
    }

    val leftPadding = 16.dp
    val spaceBetween = 8.dp
    maxExpense = (expenses.maxByOrNull { it.totalAmount }?.totalAmount ?: 0f)
    maxIncome = incomes.maxByOrNull { it.totalAmount }?.totalAmount ?: 0f
    maxAmount = maxOf(maxExpense, maxIncome)
    val roundedMaxAmount = roundUpToNiceNumber(maxAmount)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = modifier.fillMaxWidth()) {
            // Amount levels
            Column(
                modifier = Modifier
                    .width(30.dp)
                    .height(200.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                val lineCount = 5
                val numberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
                val symbol = numberFormat.currency?.symbol
                for (i in lineCount downTo 0) {
                    val amount = roundedMaxAmount * i / lineCount
                    Text(
                        text = "${symbol}${
                            formatAmount(
                                amount
                            )
                        }",
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth(),
                        color = CustomColors.onPrimary
                    )
                }
            }
            // Graph
            Column(modifier = modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                ) {
                    Canvas(
                        modifier = Modifier
                            .height(200.dp)
                            .width(
                                (80.dp * expenses.size + leftPadding + spaceBetween * expenses.size).coerceAtLeast(
                                    LocalConfiguration.current.screenWidthDp.dp - 40.dp
                                )
                            )
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    if (expenses.isNotEmpty()) {
                                        val index =
                                            (offset.x / (size.width / expenses.size)).toInt()
                                        val month = expenses[index].month
                                        onItemClick(month)
                                    }
                                }
                            }
                    ) {
//                    val colors = listOf(
//                        ChampagnePink,
//                        Linen,
//                        MistyRose,
//                        MimiPink,
//                        LightCyan,
//                        MintCream,
//                        Isabelline,
//                        AliceBlue,
//                        ColumbiaBlue,
//                        PowderBlue
//                    )

                        // Draw background lines
                        val lineCount = 5
                        for (i in 0..lineCount) {
                            val y = size.height * (1 - i.toFloat() / lineCount)
                            drawLine(
                                color = CustomColors.secondaryInactive.copy(alpha = 0.5f),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

//                    val cornerRadius = 16.dp // .toPx()
                        val cornerRadius = CornerRadius(36f, 36f)
                        val barWidth = (size.width - leftPadding.toPx()) / expenses.size
                        expenses.forEachIndexed { index, expense ->
//                        val xOffset = leftPadding.toPx() + index * barWidth + barWidth * 0.05f
                            val expenseBarHeight = (expense.totalAmount / maxAmount) * size.height
                            val incomeBarHeight =
                                (incomes.getOrNull(index)?.totalAmount
                                    ?: 0f) / maxAmount * size.height
//                        val topLeftExpense = Offset(
//                            (xOffset + barWidth * 0.4f),
//                            size.height - expenseBarHeight
//                        )
//                        val topLeftIncome =
//                            Offset(xOffset, size.height - incomeBarHeight)
//                        val barSizeExpense = Size(barWidth * 0.38f, expenseBarHeight)
//                        val barSizeIncome = Size(barWidth * 0.38f, incomeBarHeight)

//                        // Draw income bar
//                        val pathIncome = Path().apply {
//                            // Move to bottom-left corner
//                            moveTo(topLeftIncome.x, size.height)
//                            // Line to top-left corner
//                            lineTo(topLeftIncome.x, topLeftIncome.y + cornerRadius)
//                            // Top-left arc
//                            arcTo(
//                                rect = Rect(
//                                    topLeftIncome.x,
//                                    topLeftIncome.y,
//                                    topLeftIncome.x + cornerRadius * 2,
//                                    topLeftIncome.y + cornerRadius * 2
//                                ),
//                                startAngleDegrees = 180f,
//                                sweepAngleDegrees = 90f,
//                                forceMoveTo = false
//                            )
//                            // Line to top-right corner
//                            lineTo(
//                                topLeftIncome.x + barSizeIncome.width - cornerRadius,
//                                topLeftIncome.y
//                            )
//                            // Top-right arc
//                            arcTo(
//                                rect = Rect(
//                                    topLeftIncome.x + barSizeIncome.width - cornerRadius * 2,
//                                    topLeftIncome.y,
//                                    topLeftIncome.x + barSizeIncome.width,
//                                    topLeftIncome.y + cornerRadius * 2
//                                ),
//                                startAngleDegrees = 270f,
//                                sweepAngleDegrees = 90f,
//                                forceMoveTo = false
//                            )
//                            // Line to bottom-right corner
//                            lineTo(topLeftIncome.x + barSizeIncome.width, size.height)
//                            // Close the path
//                            close()
//                        }
//                        drawPath(
//                            path = pathIncome,
//                            color = CustomColors.income
//                        )
//
//                        // Draw expense bar
//                        val pathExpense = Path().apply {
//                            // Move to bottom-left corner
//                            moveTo(topLeftExpense.x, size.height)
//                            // Line to top-left corner
//                            lineTo(topLeftExpense.x, topLeftExpense.y + cornerRadius)
//                            // Top-left arc
//                            arcTo(
//                                rect = Rect(
//                                    topLeftExpense.x,
//                                    topLeftExpense.y,
//                                    topLeftExpense.x + cornerRadius * 2,
//                                    topLeftExpense.y + cornerRadius * 2
//                                ),
//                                startAngleDegrees = 180f,
//                                sweepAngleDegrees = 90f,
//                                forceMoveTo = false
//                            )
//                            // Line to top-right corner
//                            lineTo(
//                                topLeftExpense.x + barSizeExpense.width - cornerRadius,
//                                topLeftExpense.y
//                            )
//                            // Top-right arc
//                            arcTo(
//                                rect = Rect(
//                                    topLeftExpense.x + barSizeExpense.width - cornerRadius * 2,
//                                    topLeftExpense.y,
//                                    topLeftExpense.x + barSizeExpense.width,
//                                    topLeftExpense.y + cornerRadius * 2
//                                ),
//                                startAngleDegrees = 270f,
//                                sweepAngleDegrees = 90f,
//                                forceMoveTo = false
//                            )
//                            // Line to bottom-right corner
//                            lineTo(topLeftExpense.x + barSizeExpense.width, size.height)
//                            // Close the path
//                            close()
//                        }
//                        drawPath(
//                            path = pathExpense,
//                            color = CustomColors.expense
//                        )

                            // Income Bar
                            val pathIncome = Path().apply {
                                addRoundRect(
                                    RoundRect(
                                        rect = Rect(
                                            offset = Offset(
                                                leftPadding.toPx() + index * barWidth + barWidth * 0.07f,
                                                size.height - incomeBarHeight
                                            ),
                                            size = androidx.compose.ui.geometry.Size(
                                                barWidth * 0.38f,
                                                incomeBarHeight
                                            ),
                                        ),
                                        topLeft = cornerRadius,
                                        topRight = cornerRadius,
                                    )
                                )
                            }
                            drawPath(pathIncome, color = CustomColors.incomeDim)

                            // Expense Bar
                            val pathExpense = Path().apply {
                                addRoundRect(
                                    RoundRect(
                                        rect = Rect(
                                            offset = Offset(
                                                leftPadding.toPx() + index * barWidth + barWidth * 0.42f + barWidth * 0.05f,
                                                size.height - expenseBarHeight
                                            ),
                                            size = androidx.compose.ui.geometry.Size(
                                                barWidth * 0.38f,
                                                expenseBarHeight
                                            ),
                                        ),
                                        topLeft = cornerRadius,
                                        topRight = cornerRadius,
                                    )
                                )
                            }
                            drawPath(pathExpense, color = CustomColors.primary)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = leftPadding)
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    expenses.forEachIndexed { index, expense ->
                        val income = incomes.getOrNull(index)
                        val expenseAmount =
                            NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                                .format(expense.totalAmount)
                        val incomeAmount = income?.let {
                            NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                                .format(it.totalAmount)
                        } ?: "-"

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(80.dp)
                                .clickable { onItemClick(expense.month) }
                        ) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = utils.getMonthName(expense.month),
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                color = CustomColors.onPrimary,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = incomeAmount,
                                color = CustomColors.income,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Text(
                                text = expenseAmount,
                                color = CustomColors.primary,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.align(Alignment.Start)
                            )
                        }
                    }
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(16.dp))
                    .size(30.dp, 10.dp)
                    .background(CustomColors.incomeDim)
                    .align(Alignment.CenterVertically)
            )
            Text(text = "Income", modifier = Modifier.padding(start = 4.dp))
            Spacer(modifier = Modifier.width(50.dp))
            Box(
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(16.dp))
                    .size(30.dp, 10.dp)
                    .background(CustomColors.primary)
                    .align(Alignment.CenterVertically)
            )
            Text(text = "Expenses", modifier = Modifier.padding(start = 4.dp))
        }
    }
}

//@Preview
//@Composable
//fun ExpenseBarChart() {
//    val expenses = listOf(
//        MonthlyTotal(month = "2024-05", totalAmount = 5434990f),
//        MonthlyTotal(month = "2024-06", totalAmount = 7064692.5f),
//        MonthlyTotal(month = "2024-07", totalAmount = 6528317.5f),
//        MonthlyTotal(month = "2024-08", totalAmount = 2661517.2f)
//    )
//    val maxExpense = expenses.maxByOrNull { it.totalAmount }?.totalAmount ?: 0f
//    Column(modifier = Modifier.fillMaxWidth()) {
//        Canvas(
//            modifier = Modifier
//                .height(200.dp)
//                .fillMaxWidth()
//        ) {
//            val barWidth = size.width / expenses.size
//            val colors = listOf(
//                ChampagnePink,
//                Linen,
//                MistyRose,
//                MimiPink,
//                LightCyan,
//                MintCream,
//                Isabelline,
//                AliceBlue,
//                ColumbiaBlue,
//                PowderBlue
//            )
//
//            expenses.forEachIndexed { index, expense ->
//                val barHeight = (expense.totalAmount / maxExpense) * size.height
//                drawRect(
//                    color = colors[index % colors.size],
//                    topLeft = Offset(index * barWidth, size.height - barHeight),
//                    size = androidx.compose.ui.geometry.Size(barWidth * 0.3f, barHeight)
//                )
//            }
//        }
//
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            expenses.forEach { expense ->
//                val amount =
//                    NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(expense.totalAmount)
//
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
////                    modifier = Modifier
//                ) {
//                    Text(
//                        text = expense.month,
//                        color = ClayBeige,
//                        style = MaterialTheme.typography.labelSmall,
////                        modifier = Modifier
////                            .rotate(-90f)
////                            .padding(bottom = 24.dp)
//                    )
//                    Text(
//                        text = amount,
//                        color = ClayBeige,
//                        style = MaterialTheme.typography.labelSmall,
////                        modifier = Modifier
////                            .rotate(-90f)
//                    )
//                }
//            }
//        }
//    }
//}