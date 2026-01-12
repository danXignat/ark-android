package com.danignat.ark.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danignat.ark.repository.SettingsRepository
import com.danignat.ark.ui.theme.ThemeType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val themeType: StateFlow<ThemeType> = settingsRepository.themeType.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemeType.NATURE
    )

    val aiApiKey: StateFlow<String> = settingsRepository.aiApiKey.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    fun setThemeType(themeType: ThemeType) {
        viewModelScope.launch {
            settingsRepository.setThemeType(themeType)
        }
    }

    fun setAiApiKey(apiKey: String) {
        viewModelScope.launch {
            settingsRepository.setAiApiKey(apiKey)
        }
    }
}

