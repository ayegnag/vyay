package com.grex.vyay.ui.components

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grex.vyay.TransactionRecord
import java.util.Date
import kotlin.random.Random

data class PieChartData(
    val value: Float,
    val color: Color
)

@Composable
fun TransactionPieChart(transactions: List<TransactionRecord>) {
    val (pieChartData, legend) = createPieChartWithLegend(transactions)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 16.dp)
    ) {
        PieChart(pieChartData, modifier = Modifier.weight(0.6f))

        Column(modifier = Modifier.padding(start = 16.dp)) {

            // Display the legend
            legend.forEach { (category, percentage) ->
                Log.d("Legend", category + percentage)
                var categoryText = category
                if (category == "") {
                    categoryText = "Uncategorized"
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                pieChartData[legend.indexOf(category to percentage)].color,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "$categoryText: $percentage",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

//@Composable
//fun PieChart(data: List<PieChartData>) {
//    // Implement your PieChart drawing logic here
//    // You can use Canvas to draw the pie chart
//    Canvas(modifier = Modifier.size(200.dp)) {
//        val total = data.sumOf { it.value.toDouble() }
//        var startAngle = 0f
//        data.forEach { slice ->
//            val sweepAngle = 360f * (slice.value / total.toFloat())
//            drawArc(
//                color = slice.color,
//                startAngle = startAngle,
//                sweepAngle = sweepAngle,
//                useCenter = true
//            )
//            startAngle += sweepAngle
//        }
//    }
//}

@Composable
fun PieChart(
    data: List<PieChartData>,
    modifier: Modifier = Modifier
) {
    val totalValue = data.fold(0f) { acc, pieChartData -> acc + pieChartData.value }
    val TOTAL_ANGLE = 360.0f
    val STROKE_SIZE_UNSELECTED = 40.dp
    val STROKE_SIZE_SELECTED = 60.dp

    Log.d("PieChartData", data.toString())
    Box(
        modifier = modifier
            .padding(0.dp)
    ) {
        Canvas(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxSize()
                .align(Alignment.Center)
        ) {
//            var startAngle = 0f
//            data.forEach { pieChartData ->
//                Log.d("PieChart", pieChartData.toString())
//                drawArc(
//                    color = pieChartData.color,
//                    startAngle = startAngle,
//                    sweepAngle = 360f * (pieChartData.value / totalValue),
//                    useCenter = true,
//                    style = Fill
//                )
//                startAngle += 360f * (pieChartData.value / totalValue)
//            }
            val defaultStrokeWidth = STROKE_SIZE_UNSELECTED.toPx()
            var lastAngle = 0f
            data.forEach { pieChartData ->
                drawArc(
                    color = pieChartData.color,
                    startAngle = lastAngle,
                    sweepAngle = 360f * (pieChartData.value / totalValue),
                    useCenter = false,
                    topLeft = Offset(defaultStrokeWidth / 2, defaultStrokeWidth / 2),
                    style = Stroke(defaultStrokeWidth, cap = StrokeCap.Butt),
                    size = Size(size.width - defaultStrokeWidth,
                        size.height - defaultStrokeWidth)
                )
                lastAngle += 360f * (pieChartData.value / totalValue)
            }
        }
    }
}


fun generatePieChartData(transactions: List<TransactionRecord>): List<PieChartData> {
    // Group transactions by category and sum the amounts
    val categoryTotals = transactions
        .filter { it.amount != null && it.isTransaction }
        .groupBy { it.tags?.split(",")?.firstOrNull()?.trim() ?: "Uncategorized" }
        .mapValues { (_, transactions) ->
            transactions.sumOf { it.amount!! }
        }

    // Calculate the total amount across all categories
    val totalAmount = categoryTotals.values.sum()

    // Generate a list of PieChartData
    return categoryTotals.map { (category, amount) ->
        PieChartData(
            value = (amount / totalAmount).toFloat(),
            color = getCategoryColor(category)
        )
    }
}

fun createPieChartWithLegend(transactions: List<TransactionRecord>): Pair<List<PieChartData>, List<Pair<String, String>>> {
    val pieChartData = generatePieChartData(transactions)
    val legend = pieChartData.getCategoryPercentages(transactions)
    return pieChartData to legend
}

fun generateRandomColor(): Color {
    return Color(
        red = Random.nextFloat(),
        green = Random.nextFloat(),
        blue = Random.nextFloat(),
        alpha = 1f
    )
}

fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "groceries" -> Color(80, 142, 124)
        "bills" -> Color(190, 125, 103)
        "emi" -> Color(98, 103, 125)
        "fuel" -> Color(193, 106, 79)
        "rent" -> Color(149, 99, 107)
        "health" -> Color(94, 170, 154)
        "food" -> Color(226, 161, 136)
        "shopping" -> Color(205, 150, 161)
        "investment" -> Color(96, 168, 105)
        "monthly" -> Color(99, 105, 159)
        "annual" -> Color(127, 99, 180)
        "work" -> Color(89, 107, 180)
        "personal" -> Color(190, 91, 93)
        "shared" -> Color(179, 149, 99)
        "tax" -> Color(155, 94, 149)
        "loan" -> Color(141, 109, 139)
        "subscription" -> Color(216, 126, 95)
        "gift" -> Color(228, 123, 132)
        "entertainment" -> Color(98, 158, 164)
        "education" -> Color(97, 110, 168)
        "travel" -> Color(172, 179, 127)
        "luxury" -> Color(214, 168, 31, 255)
//        "luxury" -> Color(177, 117, 169)
        "uncategorized" -> Color(136, 115, 101, 255)
        "" -> Color(246, 145, 79)
        else -> generateRandomColor()
    }
}
//fun getCategoryColor(category: String): Color {
//    return when (category.lowercase()) {
//        "groceries" -> Color(88, 125, 113)
//        "bills" -> Color(172, 132, 112)
//        "emi" -> Color(110, 117, 130)
//        "fuel" -> Color(178, 117, 94)
//        "rent" -> Color(140, 109, 116)
//        "health" -> Color(102, 151, 140)
//        "food" -> Color(209, 167, 148)
//        "shopping" -> Color(187, 159, 166)
//        "investment" -> Color(107, 148, 113)
//        "monthly" -> Color(112, 115, 141)
//        "annual" -> Color(136, 116, 163)
//        "work" -> Color(103, 122, 156)
//        "personal" -> Color(171, 106, 108)
//        "shared" -> Color(165, 157, 120)
//        "tax" -> Color(143, 108, 138)
//        "loan" -> Color(132, 121, 130)
//        "subscription" -> Color(196, 138, 110)
//        "gift" -> Color(209, 135, 145)
//        "entertainment" -> Color(111, 141, 146)
//        "education" -> Color(111, 123, 149)
//        "travel" -> Color(157, 165, 140)
//        "luxury" -> Color(162, 131, 154)
//        "uncategorized" -> Color(239, 156, 102)
//        "" -> Color(239, 156, 102)
//        else -> generateRandomColor()
//    }
//}
// Extension function to get a color-coded list of categories with their percentages
fun List<PieChartData>.getCategoryPercentages(transactions: List<TransactionRecord>): List<Pair<String, String>> {
    val categoryTotals = transactions
//        .filter { it.amount != null && it.category != null }
//        .groupBy { it.category!! }
        .filter { it.amount != null && it.isTransaction }
        .groupBy { it.tags?.split(",")?.firstOrNull()?.trim() ?: "Uncategorized" }
        .mapValues { (_, transactions) ->
            transactions.sumOf { it.amount!! }
        }

    val totalAmount = categoryTotals.values.sum()

    return this.zip(categoryTotals.entries) { pieData, (category, amount) ->
        val percentage = (amount / totalAmount * 100).toInt()
        category to "$percentage%"
    }
}


@Preview(showBackground = true)
@Composable
fun TransactionPieChartPreview() {
    Surface {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            TransactionPieChart(transactions = getSampleTransactions())
        }
    }
}

fun getSampleTransactions(): List<TransactionRecord> {
    return listOf(
        TransactionRecord(
            id = 1,
            isManual = false,
            address = "Sample Address 1",
            receivedOnDate = Date().time,
            transactionType = "expense",
            currency = "USD",
            amount = 100.0,
            receivedAt = "2023-09-09 10:00:00",
            transactionMode = "card",
            messageDate = "2023-09-09",
            source = "sms",
            isTransaction = true,
            body = "Grocery shopping",
            tags = "Groceries, Food",
            category = "Food",
            isProcessed = false,
        ),
        TransactionRecord(
            id = 2,
            isManual = false,
            address = "Sample Address 2",
            receivedOnDate = Date().time,
            transactionType = "expense",
            currency = "USD",
            amount = 50.0,
            receivedAt = "2023-09-09 11:00:00",
            transactionMode = "card",
            messageDate = "2023-09-09",
            source = "sms",
            isTransaction = true,
            body = "Gas station",
            tags = "Transportation, Car",
            category = "Transportation",
            isProcessed = false,
        ),
        TransactionRecord(
            id = 3,
            isManual = false,
            address = "Sample Address 3",
            receivedOnDate = Date().time,
            transactionType = "expense",
            currency = "USD",
            amount = 200.0,
            receivedAt = "2023-09-09 12:00:00",
            transactionMode = "card",
            messageDate = "2023-09-09",
            source = "sms",
            isTransaction = true,
            body = "Restaurant dinner",
            tags = "Dining, Food",
            category = "Food",
            isProcessed = false,
        ),
        TransactionRecord(
            id = 4,
            isManual = false,
            address = "Sample Address 4",
            receivedOnDate = Date().time,
            transactionType = "expense",
            currency = "USD",
            amount = 75.0,
            receivedAt = "2023-09-09 13:00:00",
            transactionMode = "card",
            messageDate = "2023-09-09",
            source = "sms",
            isTransaction = true,
            body = "Movie tickets",
            tags = "Entertainment, Movies",
            category = "Entertainment",
            isProcessed = false,
        )
    )
}