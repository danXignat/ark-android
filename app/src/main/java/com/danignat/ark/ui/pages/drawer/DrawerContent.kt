package com.danignat.ark.ui.pages.command.task

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.danignat.ark.viewmodel.TaskFilter

/**
 * Drawer content specific to the Task screen
 */
@Composable
fun TaskDrawerContent(
    currentFilter: TaskFilter = TaskFilter.ALL,
    onAllTasksClick: () -> Unit = {},
    onCompletedClick: () -> Unit = {},
    onUncompletedClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    Text(
        text = "Menu",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(16.dp)
    )
    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

    NavigationDrawerItem(
        label = { Text("All tasks") },
        selected = currentFilter == TaskFilter.ALL,
        onClick = onAllTasksClick
    )
    NavigationDrawerItem(
        label = { Text("Completed") },
        selected = currentFilter == TaskFilter.COMPLETED,
        onClick = onCompletedClick
    )
    NavigationDrawerItem(
        label = { Text("Uncompleted") },
        selected = currentFilter == TaskFilter.UNCOMPLETED,
        onClick = onUncompletedClick
    )

    Spacer(modifier = Modifier.height(16.dp))
    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

    NavigationDrawerItem(
        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
        label = { Text("Settings") },
        selected = false,
        onClick = onSettingsClick
    )
}

