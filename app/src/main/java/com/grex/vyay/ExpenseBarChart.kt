package com.grex.vyay


import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grex.vyay.ui.theme.AliceBlue
import com.grex.vyay.ui.theme.ChampagnePink
import com.grex.vyay.ui.theme.ClayBeige
import com.grex.vyay.ui.theme.ColumbiaBlue
import com.grex.vyay.ui.theme.Isabelline
import com.grex.vyay.ui.theme.LightCyan
import com.grex.vyay.ui.theme.LimeGreen
import com.grex.vyay.ui.theme.Linen
import com.grex.vyay.ui.theme.MimiPink
import com.grex.vyay.ui.theme.MintCream
import com.grex.vyay.ui.theme.MistyRose
import com.grex.vyay.ui.theme.PatinaShine
import com.grex.vyay.ui.theme.PowderBlue
import com.grex.vyay.ui.theme.secondaryInactive
import java.text.NumberFormat
import java.util.Locale


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

    maxExpense = (expenses.maxByOrNull { it.totalAmount }?.totalAmount ?: 0f)
    maxIncome = incomes.maxByOrNull { it.totalAmount }?.totalAmount ?: 0f
    maxAmount = maxOf(maxExpense, maxIncome)

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
                    .width((80.dp * expenses.size).coerceAtLeast(LocalConfiguration.current.screenWidthDp.dp))
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            if (expenses.isNotEmpty()) {
                                val index = (offset.x / (size.width / expenses.size)).toInt()
                                val month = expenses[index].month
                                onItemClick(month)
                            }
                        }
                    }
            ) {
                val colors = listOf(
                    ChampagnePink,
                    Linen,
                    MistyRose,
                    MimiPink,
                    LightCyan,
                    MintCream,
                    Isabelline,
                    AliceBlue,
                    ColumbiaBlue,
                    PowderBlue
                )

                // Draw background lines
                val lineCount = 5
                for (i in 0..lineCount) {
                    val y = size.height * (1 - i.toFloat() / lineCount)
                    drawLine(
                        color = secondaryInactive.copy(alpha = 0.5f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val barWidth = size.width / expenses.size
                expenses.forEachIndexed { index, expense ->
                    val expenseBarHeight = (expense.totalAmount / maxAmount) * size.height
                    val incomeBarHeight =
                        (incomes.getOrNull(index)?.totalAmount ?: 0f) / maxAmount * size.height

                    // Draw income bar
                    drawRect(
                        color = LimeGreen,
                        topLeft = Offset(index * barWidth, size.height - incomeBarHeight),
                        size = androidx.compose.ui.geometry.Size(barWidth * 0.4f, incomeBarHeight)
                    )

                    // Draw expense bar
                    drawRect(
                        color = colors[index % colors.size],
                        topLeft = Offset(
                            index * barWidth + barWidth * 0.4f,
                            size.height - expenseBarHeight
                        ),
                        size = androidx.compose.ui.geometry.Size(barWidth * 0.4f, expenseBarHeight)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            expenses.forEachIndexed { index, expense ->
                val income = incomes.getOrNull(index)
                val expenseAmount =
                    NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(expense.totalAmount)
                val incomeAmount = income?.let {
                    NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(it.totalAmount)
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
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = incomeAmount,
                        color = PatinaShine,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Text(
                        text = expenseAmount,
                        color = ClayBeige,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ExpenseBarChart() {
    val expenses = listOf(
        MonthlyTotal(month = "2024-05", totalAmount = 5434990f),
        MonthlyTotal(month = "2024-06", totalAmount = 7064692.5f),
        MonthlyTotal(month = "2024-07", totalAmount = 6528317.5f),
        MonthlyTotal(month = "2024-08", totalAmount = 2661517.2f)
    )
    val maxExpense = expenses.maxByOrNull { it.totalAmount }?.totalAmount ?: 0f
    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
        ) {
            val barWidth = size.width / expenses.size
            val colors = listOf(
                ChampagnePink,
                Linen,
                MistyRose,
                MimiPink,
                LightCyan,
                MintCream,
                Isabelline,
                AliceBlue,
                ColumbiaBlue,
                PowderBlue
            )

            expenses.forEachIndexed { index, expense ->
                val barHeight = (expense.totalAmount / maxExpense) * size.height
                drawRect(
                    color = colors[index % colors.size],
                    topLeft = Offset(index * barWidth, size.height - barHeight),
                    size = androidx.compose.ui.geometry.Size(barWidth * 0.3f, barHeight)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            expenses.forEach { expense ->
                val amount =
                    NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(expense.totalAmount)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
//                    modifier = Modifier
                ) {
                    Text(
                        text = expense.month,
                        color = ClayBeige,
                        style = MaterialTheme.typography.labelSmall,
//                        modifier = Modifier
//                            .rotate(-90f)
//                            .padding(bottom = 24.dp)
                    )
                    Text(
                        text = amount,
                        color = ClayBeige,
                        style = MaterialTheme.typography.labelSmall,
//                        modifier = Modifier
//                            .rotate(-90f)
                    )
                }
            }
        }
    }
}