package com.danignat.ark.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.danignat.ark.ui.theme.legacy.LegacyTheme
import com.example.compose.NatureTheme

enum class ThemeType {
    NATURE,
    LEGACY
}

@Composable
fun ArkTheme(
    themeType: ThemeType = ThemeType.NATURE,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    when (themeType) {
        ThemeType.NATURE -> NatureTheme(
            darkTheme = darkTheme,
            dynamicColor = dynamicColor,
            content = content
        )
        ThemeType.LEGACY -> LegacyTheme(
            darkTheme = darkTheme,
            dynamicColor = dynamicColor,
            content = content
        )
    }
}