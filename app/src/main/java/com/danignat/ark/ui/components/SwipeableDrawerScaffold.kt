package com.danignat.ark.ui.pages.command.task

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.foundation.interaction.MutableInteractionSource

@Composable
fun SwipeableDrawerScaffold(
    modifier: Modifier = Modifier,
    drawerWidth: Dp = 280.dp,
    onRightToLeftSwipe: (() -> Unit)? = null,
    onLeftToRightSwipe: (() -> Unit)? = null,
    drawerContent: @Composable ColumnScope.() -> Unit,
    content: @Composable (openDrawer: () -> Unit, closeDrawer: () -> Unit) -> Unit
) {
    val density = LocalDensity.current
    val drawerWidthPx = with(density) { drawerWidth.toPx() }

    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(-drawerWidthPx) } // -width = closed, 0 = open

    fun openDrawer() {
        scope.launch {
            offsetX.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 250)
            )
        }
    }

    fun closeDrawer() {
        scope.launch {
            offsetX.animateTo(
                targetValue = -drawerWidthPx,
                animationSpec = tween(durationMillis = 250)
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    scope.launch {
                        val newOffset = (offsetX.value + delta)
                            .coerceIn(-drawerWidthPx, 0f)
                        offsetX.snapTo(newOffset)
                    }
                },
                onDragStopped = { velocity ->
                    scope.launch {
                        val isDrawerClosed = offsetX.value <= -drawerWidthPx + 1f

                        // If drawer is closed and user swipes left fast, navigate to next screen
                        if (isDrawerClosed && velocity < -1000f && onRightToLeftSwipe != null) {
                            onRightToLeftSwipe.invoke()
                            return@launch
                        }

                        // If drawer is closed and user swipes right fast, navigate to previous screen
                        if (isDrawerClosed && velocity > 1000f && onLeftToRightSwipe != null) {
                            onLeftToRightSwipe.invoke()
                            return@launch
                        }

                        // Normal drawer settle behaviour
                        val shouldOpen =
                            offsetX.value > -drawerWidthPx / 2f || velocity > 1000f
                        val target = if (shouldOpen) 0f else -drawerWidthPx
                        offsetX.animateTo(target, tween(250))
                    }
                }
            )
    ) {
        // Main screen content UNDER the drawer
        content(
            { openDrawer() },
            { closeDrawer() }
        )

        // Scrim when drawer is visible
        val progress = 1f - (-offsetX.value / drawerWidthPx) // 0..1
        if (progress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f * progress))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { closeDrawer() }
            )
        }

        // Drawer itself
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .width(drawerWidth)
                .offset { IntOffset(offsetX.value.roundToInt(), 0) },
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                content = drawerContent
            )
        }
    }
}
