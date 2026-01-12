package com.danignat.ark.ui.navigation

/**
 * Navigation callbacks interface that all navigable screens should implement.
 * Provides consistent navigation behavior across the application.
 */
data class NavigationCallbacks(
    val onNavigateBack: (() -> Unit)? = null,
    val onNavigateNext: (() -> Unit)? = null
)

