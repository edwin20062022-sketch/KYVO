package com.kyvo.app.feature.onboarding.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun KyvoVerticalWheelPicker(
    items: List<Int>,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    testTag: String? = null,
) {
    if (items.isEmpty()) return

    val selectedIndex = wheelInitialIndex(items, selectedItem)
    val scope = rememberCoroutineScope()

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = selectedIndex,
    )

    val flingBehavior: FlingBehavior = rememberSnapFlingBehavior(listState)

    val centeredIndex by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2
            info.visibleItemsInfo.minByOrNull { item ->
                val itemCenter = item.offset + item.size / 2
                abs(itemCenter - viewportCenter)
            }?.index?.coerceIn(items.indices) ?: selectedIndex
        }
    }

    LaunchedEffect(selectedItem) {
        val externalIndex = items.indexOf(selectedItem)
        if (externalIndex >= 0 && externalIndex != centeredIndex) {
            listState.scrollToItem(externalIndex)
        }
    }

    LaunchedEffect(centeredIndex) {
        if (centeredIndex in items.indices && items[centeredIndex] != selectedItem) {
            onItemSelected(items[centeredIndex])
        }
    }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 2.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            LazyColumn(
                state = listState,
                flingBehavior = flingBehavior,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(WHEEL_VIEWPORT_HEIGHT)
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(vertical = WHEEL_ITEM_HEIGHT),
            ) {
                items(items.size) { index ->
                    val item = items[index]
                    val isSelected = item == selectedItem
                    val distance = abs(index - centeredIndex)
                    val alpha = when {
                        distance == 0 -> 1f
                        distance == 1 -> 0.65f
                        distance == 2 -> 0.35f
                        else -> 0.15f
                    }
                    val scale = when {
                        distance == 0 -> 1f
                        distance == 1 -> 0.85f
                        else -> 0.7f
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(WHEEL_ITEM_HEIGHT)
                            .graphicsLayer {
                                this.alpha = alpha
                                scaleX = scale
                                scaleY = scale
                            }
                            .semantics { role = Role.Tab }
                            .clickable {
                                scope.launch {
                                    listState.animateScrollToItem(
                                        index,
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = item.toString(),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = if (isSelected) 36.sp else 22.sp,
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                            if (isSelected && label != null) {
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(WHEEL_ITEM_HEIGHT)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.HorizontalDivider(
                    modifier = Modifier.align(Alignment.TopCenter),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                )
                androidx.compose.material3.HorizontalDivider(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                )
            }
        }
    }
}

private val WHEEL_ITEM_HEIGHT = 56.dp
private val WHEEL_VIEWPORT_HEIGHT = WHEEL_ITEM_HEIGHT * 3

internal fun wheelInitialIndex(items: List<Int>, selectedItem: Int): Int = items.indexOf(selectedItem).coerceAtLeast(0)
