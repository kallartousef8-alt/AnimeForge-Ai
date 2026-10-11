package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "anime_forge_settings")

class UserPreferencesRepository(private val context: Context) {
    companion object {
        val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val KEY_DARK_THEME = booleanPreferencesKey("dark_theme")
        val KEY_DEFAULT_ASPECT = stringPreferencesKey("default_aspect")
        val KEY_DEFAULT_RES = stringPreferencesKey("default_res")
        val KEY_GEMINI_KEY = stringPreferencesKey("gemini_key")
        val KEY_ELEVENLABS_KEY = stringPreferencesKey("elevenlabs_key")
        val KEY_PREFERRED_VOICE = stringPreferencesKey("preferred_voice")
    }

    val isOnboardingDone: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_DONE] ?: false
    }

    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_DARK_THEME] ?: true // Default to anime dark
    }

    val defaultAspect: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_ASPECT] ?: "PORTRAIT_9_16"
    }

    val defaultResolution: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_RES] ?: "RES_1080P"
    }

    val preferredVoice: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_PREFERRED_VOICE] ?: "ENERGETIC_SHONEN"
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING_DONE] = done }
    }

    suspend fun setDarkTheme(dark: Boolean) {
        context.dataStore.edit { it[KEY_DARK_THEME] = dark }
    }

    suspend fun setDefaultAspect(aspect: String) {
        context.dataStore.edit { it[KEY_DEFAULT_ASPECT] = aspect }
    }

    suspend fun setDefaultResolution(res: String) {
        context.dataStore.edit { it[KEY_DEFAULT_RES] = res }
    }

    suspend fun setPreferredVoice(voice: String) {
        context.dataStore.edit { it[KEY_PREFERRED_VOICE] = voice }
    }
}
