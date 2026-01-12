package com.danignat.ark.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.danignat.ark.ui.theme.ThemeType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val THEME_TYPE = stringPreferencesKey("theme_type")
        val AI_API_KEY = stringPreferencesKey("ai_api_key")
    }

    val themeType: Flow<ThemeType> = context.dataStore.data.map { preferences ->
        val themeString = preferences[PreferencesKeys.THEME_TYPE] ?: ThemeType.NATURE.name
        ThemeType.valueOf(themeString)
    }

    val aiApiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AI_API_KEY] ?: ""
    }

    suspend fun setThemeType(themeType: ThemeType) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_TYPE] = themeType.name
        }
    }

    suspend fun setAiApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AI_API_KEY] = apiKey
        }
    }
}

