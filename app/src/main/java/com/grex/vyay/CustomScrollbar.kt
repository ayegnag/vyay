package com.grex.vyay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CustomScrollbar(
    modifier: Modifier,
    scrollState: LazyListState
) {
    val showScrollbar = remember {
        derivedStateOf {
            scrollState.firstVisibleItemIndex > 0 || scrollState.canScrollForward
        }
    }

    AnimatedVisibility(
        visible = showScrollbar.value,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Canvas(
            modifier = modifier
                .width(8.dp)
                .fillMaxHeight()
        ) {
            val firstVisibleElementIndex = scrollState.firstVisibleItemIndex
            val needScrollToEnd = scrollState.canScrollForward
            val isDragging = scrollState.isScrollInProgress

            val height = size.height
            val heightOfScrollbar = height / scrollState.layoutInfo.totalItemsCount
            val scrollbarOffsetY = firstVisibleElementIndex * heightOfScrollbar

            drawRoundRect(
                color = if (isDragging) Color.DarkGray else Color.LightGray,
                topLeft = Offset(0f, scrollbarOffsetY),
                size = Size(size.width, heightOfScrollbar),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
        }
    }
}