package com.fitflow.app.util

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore by preferencesDataStore(name = "fitflow_prefs")

/**
 * Handles the app's simple "Guest" identity system, plus theme and
 * notification preferences - all in ONE shared DataStore instance.
 *
 * IMPORTANT: DataStore must only be instantiated once per file name per
 * process. Using more than one `preferencesDataStore` delegate pointing at
 * different names is fine, but multiple delegates in different files
 * accidentally targeting overlapping usage is a common source of bugs -
 * so every preference in this app lives here, in a single place.
 *
 * No backend, no Firebase, no network calls - everything is stored
 * on-device only.
 */
class UserPreferences(private val context: Context) {

    companion object {
        private val KEY_GUEST_ID = stringPreferencesKey("guest_id")
        private val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        private val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    val isOnboardingComplete: Flow<Boolean> =
        context.dataStore.data.map { it[KEY_ONBOARDING_DONE] ?: false }

    val displayName: Flow<String> =
        context.dataStore.data.map { it[KEY_DISPLAY_NAME] ?: "Guest" }

    val themeMode: Flow<String> =
        context.dataStore.data.map { it[KEY_THEME_MODE] ?: "SYSTEM" }

    val notificationsEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[KEY_NOTIFICATIONS_ENABLED] ?: true }

    suspend fun getOrCreateGuestId(): String {
        val existing = context.dataStore.data.first()[KEY_GUEST_ID]
        if (existing != null) return existing
        val newId = "guest_${UUID.randomUUID().toString().take(8)}"
        context.dataStore.edit { it[KEY_GUEST_ID] = newId }
        return newId
    }

    suspend fun setDisplayName(name: String) {
        context.dataStore.edit { it[KEY_DISPLAY_NAME] = name.ifBlank { "Guest" } }
    }

    suspend fun completeOnboarding() {
        context.dataStore.edit { it[KEY_ONBOARDING_DONE] = true }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[KEY_THEME_MODE] = mode }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_NOTIFICATIONS_ENABLED] = enabled }
    }
}
