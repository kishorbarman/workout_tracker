package com.workouttracker.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Divider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier,
    label: String = ""
) {
    val values = range.toList()
    NumberPickerInternal(
        value = value,
        onValueChange = onValueChange,
        values = values,
        modifier = modifier,
        label = label
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    values: List<Int>,
    modifier: Modifier = Modifier,
    label: String = ""
) {
    NumberPickerInternal(
        value = value,
        onValueChange = onValueChange,
        values = values,
        modifier = modifier,
        label = label
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NumberPickerInternal(
    value: Int,
    onValueChange: (Int) -> Unit,
    values: List<Int>,
    modifier: Modifier = Modifier,
    label: String = ""
) {
    val currentIndex = values.indexOf(value).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = currentIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(listState.firstVisibleItemIndex, listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val newIndex = listState.firstVisibleItemIndex
            if (newIndex in values.indices) {
                val newValue = values[newIndex]
                if (newValue != value) {
                    onValueChange(newValue)
                }
            }
        }
    }

    LaunchedEffect(value) {
        if (!listState.isScrollInProgress) {
            val targetIndex = values.indexOf(value)
            if (targetIndex >= 0 && targetIndex != listState.firstVisibleItemIndex) {
                coroutineScope.launch {
                    listState.scrollToItem(targetIndex)
                }
            }
        }
    }

    Box(
        modifier = modifier
            .height(150.dp)
            .width(80.dp),
        contentAlignment = Alignment.Center
    ) {
        // Selection indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        ) {
            Divider(
                modifier = Modifier.align(Alignment.TopCenter),
                color = MaterialTheme.colorScheme.primary,
                thickness = 2.dp
            )
            Divider(
                modifier = Modifier.align(Alignment.BottomCenter),
                color = MaterialTheme.colorScheme.primary,
                thickness = 2.dp
            )
        }

        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            // Add padding items at the start
            items(2) {
                Box(modifier = Modifier.height(40.dp))
            }

            items(values.size) { index ->
                val itemValue = values[index]
                val isSelected = listState.firstVisibleItemIndex == index

                Text(
                    text = itemValue.toString().padStart(2, '0'),
                    fontSize = if (isSelected) 24.sp else 18.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth()
                        .wrapContentHeight(Alignment.CenterVertically)
                )
            }

            // Add padding items at the end
            items(2) {
                Box(modifier = Modifier.height(40.dp))
            }
        }

        // Label at bottom
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
            )
        }
    }
}
