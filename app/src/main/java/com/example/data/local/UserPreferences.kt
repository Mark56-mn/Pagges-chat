package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferences(private val context: Context) {
    
    companion object {
        val THEME_KEY = stringPreferencesKey("theme")
        val SYNC_WIFI_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("sync_wifi")
        val AUTO_DOWNLOAD_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("auto_download")
    }

    val themeMode: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[THEME_KEY] ?: "System"
        }
        
    val syncOnlyOnWifi: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[SYNC_WIFI_KEY] ?: false
        }
        
    val autoDownloadMedia: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[AUTO_DOWNLOAD_KEY] ?: false
        }

    suspend fun saveThemeMode(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }

    suspend fun setSyncOnlyOnWifi(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SYNC_WIFI_KEY] = value
        }
    }

    suspend fun setAutoDownloadMedia(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_DOWNLOAD_KEY] = value
        }
    }
}
