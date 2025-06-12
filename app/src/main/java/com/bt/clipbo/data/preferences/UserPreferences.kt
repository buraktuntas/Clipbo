package com.bt.clipbo.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val COPY_COUNT = intPreferencesKey("copy_count")
        val LAST_RATING_PROMPT = longPreferencesKey("last_rating_prompt")
        val HAS_RATED = booleanPreferencesKey("has_rated")
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val AUTO_START_SERVICE = booleanPreferencesKey("auto_start_service")
        val MAX_HISTORY_ITEMS = intPreferencesKey("max_history_items")
        val ENABLE_SECURE_MODE = booleanPreferencesKey("enable_secure_mode")
    }

    val copyCount: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.COPY_COUNT] ?: 0
    }

    val lastRatingPrompt: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_RATING_PROMPT] ?: 0L
    }

    val hasRated: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HAS_RATED] ?: false
    }

    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_DARK_THEME] ?: false
    }

    val autoStartService: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_START_SERVICE] ?: false
    }

    val maxHistoryItems: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.MAX_HISTORY_ITEMS] ?: 100
    }

    val enableSecureMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ENABLE_SECURE_MODE] ?: false
    }

    suspend fun incrementCopyCount() {
        context.dataStore.edit { preferences ->
            val currentCount = preferences[PreferencesKeys.COPY_COUNT] ?: 0
            preferences[PreferencesKeys.COPY_COUNT] = currentCount + 1
        }
    }

    suspend fun getCopyCount(): Int {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.COPY_COUNT] ?: 0
        }.first()
    }

    suspend fun updateLastRatingPrompt(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_RATING_PROMPT] = timestamp
        }
    }

    suspend fun getLastRatingPrompt(): Long {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.LAST_RATING_PROMPT] ?: 0L
        }.first()
    }

    suspend fun setHasRated(hasRated: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_RATED] = hasRated
        }
    }

    suspend fun hasRated(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.HAS_RATED] ?: false
        }.first()
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_THEME] = isDark
        }
    }

    suspend fun setAutoStartService(autoStart: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_START_SERVICE] = autoStart
        }
    }

    suspend fun setMaxHistoryItems(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MAX_HISTORY_ITEMS] = count
        }
    }

    suspend fun setEnableSecureMode(enable: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENABLE_SECURE_MODE] = enable
        }
    }
}