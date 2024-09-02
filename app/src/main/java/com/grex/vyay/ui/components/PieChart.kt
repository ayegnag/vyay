package com.grex.vyay.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.dp

@Composable
fun PieChart(
    data: List<PieChartData>,
    modifier: Modifier = Modifier
) {
    val totalValue = data.fold(0f) { acc, pieChartData -> acc + pieChartData.value }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Canvas(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxSize()
                .align(Alignment.Center)
        ) {
            var startAngle = 0f
            data.forEach { pieChartData ->
                drawArc(
                    color = pieChartData.color,
                    startAngle = startAngle,
                    sweepAngle = 360f * (pieChartData.value / totalValue),
                    useCenter = true,
                    style = Fill
                )
                startAngle += 360f * (pieChartData.value / totalValue)
            }
        }
    }
}

//@Composable
//fun PieChart(
//    data: List<PieChartData>,
//    modifier: Modifier = Modifier
//) {
//    Box(
//        modifier = modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        Canvas(
//            modifier = Modifier.fillMaxSize()
//        ) {
//            var startAngle = 0f
//            data.forEach { pieChartData ->
//                drawArc(
//                    color = pieChartData.color,
//                    startAngle = startAngle,
//                    sweepAngle = 360f * (pieChartData.value / data.sumOf { it.value }),
//                    useCenter = true,
//                    style = Fill
//                )
//                startAngle += 360f * (pieChartData.value / data.sumOf { it.value })
//            }
//        }
//    }
//}

data class PieChartData(
    val value: Float,
    val color: Color
)