package com.danignat.ark.ui.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.danignat.ark.ui.pages.ai.AiTaskScreen
import com.danignat.ark.ui.pages.command.CommandLineScreen
import com.danignat.ark.ui.pages.task.TaskScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppNavigationPager() {
    val pageCount = 3
    val initialPage = 0
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { pageCount }
    )
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        HorizontalPager(state = pagerState) { page ->
            when (page) {
                0 -> TaskScreen(
                    navigation = NavigationCallbacks(
                        onNavigateBack = null,
                        onNavigateNext = {
                            scope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        }
                    )
                )
                1 -> AiTaskScreen(
                    modifier = Modifier.fillMaxSize(),
                    navigation = NavigationCallbacks(
                        onNavigateBack = {
                            scope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        },
                        onNavigateNext = {
                            scope.launch {
                                pagerState.animateScrollToPage(2)
                            }
                        }
                    )
                )

                2 -> CommandLineScreen(
                    modifier = Modifier.fillMaxSize(),
                    navigation = NavigationCallbacks(
                        onNavigateBack = {
                            scope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        },
                        onNavigateNext = null
                    )
                )
            }
        }
    }
}

