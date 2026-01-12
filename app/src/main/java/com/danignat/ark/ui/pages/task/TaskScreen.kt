package com.danignat.ark.ui.pages.task

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.danignat.ark.ui.pages.drawer.DrawerScaffoldScreen
import com.danignat.ark.ui.navigation.NavigationCallbacks
import com.danignat.ark.ui.pages.command.task.TaskDrawerContent
import com.danignat.ark.ui.pages.settings.SettingsDialog
import com.danignat.ark.viewmodel.SettingsViewModel
import com.danignat.ark.viewmodel.TaskFilter
import com.danignat.ark.viewmodel.TaskViewModel

/**
 * Root composable for the Task screen.
 *
 * This is now modular and decoupled from specific navigation logic.
 * You can easily:
 * - Change the order of screens by swapping navigation callbacks
 * - Attach the drawer to different screens
 * - Reuse this with different navigation patterns
 */
@Composable
fun TaskScreen(
    viewModel: TaskViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    navigation: NavigationCallbacks
) {
    var showSettingsDialog by remember { mutableStateOf(false) }
    val currentTheme by settingsViewModel.themeType.collectAsState()
    val currentApiKey by settingsViewModel.aiApiKey.collectAsState()
    val currentFilter by viewModel.filter.collectAsState()

    if (showSettingsDialog) {
        SettingsDialog(
            currentTheme = currentTheme,
            currentApiKey = currentApiKey,
            onThemeChange = { settingsViewModel.setThemeType(it) },
            onApiKeyChange = { settingsViewModel.setAiApiKey(it) },
            onDismiss = { showSettingsDialog = false }
        )
    }

    DrawerScaffoldScreen(
        onSwipeLeft = navigation.onNavigateNext,
        onSwipeRight = navigation.onNavigateBack,
        drawerContent = {
            TaskDrawerContent(
                currentFilter = currentFilter,
                onAllTasksClick = {
                    viewModel.setFilter(TaskFilter.ALL)
                },
                onCompletedClick = {
                    viewModel.setFilter(TaskFilter.COMPLETED)
                },
                onUncompletedClick = {
                    viewModel.setFilter(TaskFilter.UNCOMPLETED)
                },
                onSettingsClick = {
                    showSettingsDialog = true
                }
            )
        },
        screenContent = { openDrawer, _ ->
            TaskScreenContent(
                viewModel = viewModel,
                onMenuClick = openDrawer
            )
        }
    )
}
