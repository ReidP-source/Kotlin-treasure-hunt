package com.example.treasurehunt.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.treasurehunt.model.PlayerProgress
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
/**
 *  Reid Pettibone
 *  CS 492
 *  OSU
 * */
private val Context.progressDataStore by preferencesDataStore(name = "treasure_hunt_progress")

class PlayerProgressStore(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    val progressFlow: Flow<PlayerProgress> = context.progressDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PROGRESS_KEY]?.let { stored ->
                runCatching { json.decodeFromString<PlayerProgress>(stored) }.getOrDefault(PlayerProgress())
            } ?: PlayerProgress()
        }

    suspend fun update(transform: (PlayerProgress) -> PlayerProgress) {
        context.progressDataStore.edit { preferences ->
            val current = preferences[PROGRESS_KEY]?.let { stored ->
                runCatching { json.decodeFromString<PlayerProgress>(stored) }.getOrDefault(PlayerProgress())
            } ?: PlayerProgress()
            preferences[PROGRESS_KEY] = json.encodeToString(transform(current))
        }
    }

    companion object {
        private val PROGRESS_KEY = stringPreferencesKey("player_progress_json")
    }
}
