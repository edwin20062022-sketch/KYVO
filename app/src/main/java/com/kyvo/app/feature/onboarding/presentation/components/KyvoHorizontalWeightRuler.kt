package com.kyvo.app.feature.onboarding.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.roundToInt

private const val TICK_ITEM_WIDTH_DP = 20

@Composable
fun KyvoHorizontalWeightRuler(
    values: List<Double>,
    selectedValue: Double,
    onValueSelected: (Double) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "kg",
) {
    if (values.isEmpty()) return

    val listState = rememberLazyListState()
    val flingBehavior: FlingBehavior = rememberSnapFlingBehavior(listState)
    val tickItemWidthPx = with(LocalDensity.current) { TICK_ITEM_WIDTH_DP.dp.roundToPx() }
    val startIndex = values.indexOf(selectedValue).coerceAtLeast(0)

    LaunchedEffect(Unit) {
        val target = startIndex.coerceIn(values.indices)
        listState.scrollToItem(
            target,
            scrollOffset = -(listState.layoutInfo.viewportEndOffset / 2 - tickItemWidthPx / 2),
        )
    }

    LaunchedEffect(selectedValue) {
        val idx = values.indexOf(selectedValue)
        if (idx >= 0) {
            val firstVisible = listState.firstVisibleItemIndex
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            if (idx < firstVisible || idx > lastVisible) {
                listState.animateScrollToItem(
                    idx,
                    scrollOffset = -(listState.layoutInfo.viewportEndOffset / 2 - tickItemWidthPx / 2),
                )
            }
        }
    }

    var centeredIndex by remember { mutableIntStateOf(startIndex) }

    val derivedCenteredIndex by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2
            info.visibleItemsInfo.minByOrNull { item ->
                val itemCenter = item.offset + item.size / 2
                abs(itemCenter - viewportCenter)
            }?.index?.coerceIn(values.indices) ?: centeredIndex
        }
    }

    LaunchedEffect(derivedCenteredIndex) {
        if (derivedCenteredIndex != centeredIndex) {
            centeredIndex = derivedCenteredIndex
        }
        if (derivedCenteredIndex in values.indices) {
            val snap = values[derivedCenteredIndex]
            if (snap != selectedValue) {
                onValueSelected(snap)
            }
        }
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        val displayValue = if (centeredIndex in values.indices) values[centeredIndex] else selectedValue
        Text(
            text = "%.1f".format(displayValue),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Black,
                fontSize = 48.sp,
            ),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        val primaryColor = MaterialTheme.colorScheme.primary
        val onSurfaceColor = MaterialTheme.colorScheme.onSurface
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
            ) {
                drawLine(
                    color = primaryColor.copy(alpha = 0.6f),
                    start = Offset(size.width / 2, 0f),
                    end = Offset(size.width / 2, size.height),
                    strokeWidth = 3.dp.toPx(),
                )
                drawCircle(
                    color = primaryColor,
                    radius = 6.dp.toPx(),
                    center = Offset(size.width / 2, size.height),
                )
            }

            LazyRow(
                state = listState,
                flingBehavior = flingBehavior,
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(values.size) { index ->
                    val value = values[index]
                    val distance = abs(index - centeredIndex)
                    val tickAlpha = when {
                        distance == 0 -> 1f
                        distance <= 3 -> 0.6f
                        distance <= 8 -> 0.3f
                        else -> 0.12f
                    }
                    val isMajor = value.roundToInt() % 10 == 0
                    val isMinor5 = value.roundToInt() % 5 == 0 && !isMajor
                    val tickColor = onSurfaceColor.copy(alpha = tickAlpha)
                    val tickHeight = when {
                        distance == 0 -> 48.dp
                        isMajor -> 32.dp
                        isMinor5 -> 24.dp
                        else -> 16.dp
                    }
                    val showLabel = isMajor && distance <= 10

                    Column(
                        modifier = Modifier
                            .width(TICK_ITEM_WIDTH_DP.dp)
                            .height(80.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                    ) {
                        if (showLabel) {
                            Text(
                                text = value.roundToInt().toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = tickColor,
                                textAlign = TextAlign.Center,
                            )
                        }
                        Canvas(
                            modifier = Modifier
                                .width(if (isMajor || isMinor5) 2.dp else 1.dp)
                                .height(tickHeight),
                        ) {
                            drawRect(color = tickColor)
                        }
                    }
                }
            }
        }
    }
}
