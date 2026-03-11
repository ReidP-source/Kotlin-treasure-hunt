package com.example.treasurehunt.data

import android.content.Context
import com.example.treasurehunt.model.ContentEnvelope
import com.example.treasurehunt.model.GameContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
/**
 *  Reid Pettibone
 *  CS 492
 *  OSU
 * */
class GameContentRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    suspend fun load(): GameContent = withContext(Dispatchers.IO) {
        context.assets.open(CONTENT_FILE_NAME).bufferedReader().use { reader ->
            json.decodeFromString<ContentEnvelope>(reader.readText()).toGameContent()
        }
    }

    companion object {
        private const val CONTENT_FILE_NAME = "game_content.json"
    }
}
