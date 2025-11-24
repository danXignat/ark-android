package com.danignat.ark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.danignat.ark.ui.theme.ThemeType
import com.danignat.ark.ui.theme.ArkTheme
import com.danignat.ark.ui.navigation.AppNavigationPager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArkTheme(ThemeType.NATURE, dynamicColor = false) {
                AppNavigationPager()
            }
        }
    }
}
