package com.danignat.ark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.danignat.ark.ui.theme.ArkTheme
import com.danignat.ark.ui.navigation.AppNavigationPager
import com.danignat.ark.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeType by settingsViewModel.themeType.collectAsState()
            ArkTheme(themeType, dynamicColor = false) {
                AppNavigationPager()
            }
        }
    }
}
