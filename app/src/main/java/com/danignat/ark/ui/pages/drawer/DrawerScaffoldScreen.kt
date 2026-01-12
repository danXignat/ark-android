package com.danignat.ark.ui.pages.drawer

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.danignat.ark.ui.pages.command.task.SwipeableDrawerScaffold

/**
 * A reusable screen wrapper that provides:
 * - Left side drawer with custom content
 * - Swipe gestures (open drawer from left, navigate screens by swiping right/left)
 * - Clean separation between drawer content and screen content
 *
 * This makes it easy to attach the same drawer to different screens or change navigation order.
 */
@Composable
fun DrawerScaffoldScreen(
    modifier: Modifier = Modifier,
    drawerWidth: Dp = 280.dp,
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
    drawerContent: @Composable ColumnScope.() -> Unit,
    screenContent: @Composable (openDrawer: () -> Unit, closeDrawer: () -> Unit) -> Unit
) {
    SwipeableDrawerScaffold(
        modifier = modifier,
        drawerWidth = drawerWidth,
        onRightToLeftSwipe = onSwipeLeft,
        onLeftToRightSwipe = onSwipeRight,
        drawerContent = drawerContent,
        content = screenContent
    )
}

