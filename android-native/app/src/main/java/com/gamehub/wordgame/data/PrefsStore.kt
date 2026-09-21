package com.gamehub.wordgame.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gamehub.wordgame.logic.TileState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

val Context.dataStore by preferencesDataStore(name = "wordgame_prefs")

/** Snapshot of an in-progress stage, so the player can resume exactly where they left off. */
data class SavedStageState(
    val stageIndex: Int,
    val tiles: List<String>,
    val guesses: List<SavedGuess>,
    val isRoundOver: Boolean,
    val hintUsed: Boolean
)

data class SavedGuess(
    val tiles: List<String>,
    val feedback: List<TileState>,
    val word: String,
    val isExactMatch: Boolean
)

data class SaveData(
    val stageIndex: Int,
    val score: Int,
    val clearedStages: Set<Int>,
    val savedStageState: SavedStageState?,
    val lastStageBySet: Map<String, Int> = emptyMap(),
    val autoArrangeDate: String = "",
    val autoArrangeCount: Int = 0
)

data class Settings(
    val soundOn: Boolean = true,
    val hapticOn: Boolean = true,
    val darkMode: Boolean = false,
    val nickname: String = ""
)

/** Ported storage shape from the web app's `wordgame_save_data` localStorage entry. */
class SaveStore(private val context: Context) {
    private object Keys {
        val STAGE_INDEX = intPreferencesKey("stage_index")
        val SCORE = intPreferencesKey("score")
        val CLEARED_STAGES = stringPreferencesKey("cleared_stages") // csv
        val SAVED_STATE_JSON = stringPreferencesKey("saved_state_json")
        val LAST_STAGE_BY_SET = stringPreferencesKey("last_stage_by_set") // "key:idx,key:idx"
        val AUTO_ARRANGE_DATE = stringPreferencesKey("auto_arrange_date") // "yyyyMMdd"
        val AUTO_ARRANGE_COUNT = intPreferencesKey("auto_arrange_count")
    }

    val saveDataFlow: Flow<SaveData> = context.dataStore.data.map { prefs ->
        val stageIndex = prefs[Keys.STAGE_INDEX] ?: 0
        val score = prefs[Keys.SCORE] ?: 0
        val cleared = prefs[Keys.CLEARED_STAGES]
            ?.split(',')
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet()
            ?: emptySet()
        val savedState = prefs[Keys.SAVED_STATE_JSON]?.let { parseSavedState(it) }
        val lastStageBySet = prefs[Keys.LAST_STAGE_BY_SET]
            ?.split(',')
            ?.mapNotNull { entry ->
                val parts = entry.split(':')
                val idx = parts.getOrNull(1)?.toIntOrNull()
                if (parts.size == 2 && idx != null) parts[0] to idx else null
            }
            ?.toMap()
            ?: emptyMap()
        val autoArrangeDate = prefs[Keys.AUTO_ARRANGE_DATE] ?: ""
        val autoArrangeCount = prefs[Keys.AUTO_ARRANGE_COUNT] ?: 0
        SaveData(stageIndex, score, cleared, savedState, lastStageBySet, autoArrangeDate, autoArrangeCount)
    }

    suspend fun load(): SaveData = saveDataFlow.first()

    suspend fun save(
        stageIndex: Int,
        score: Int,
        clearedStages: Set<Int>,
        savedStageState: SavedStageState?,
        lastStageBySet: Map<String, Int>
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.STAGE_INDEX] = stageIndex
            prefs[Keys.SCORE] = score
            prefs[Keys.CLEARED_STAGES] = clearedStages.sorted().joinToString(",")
            prefs[Keys.LAST_STAGE_BY_SET] = lastStageBySet.entries.joinToString(",") { "${it.key}:${it.value}" }
            if (savedStageState != null) {
                prefs[Keys.SAVED_STATE_JSON] = serializeSavedState(savedStageState)
            } else {
                prefs.remove(Keys.SAVED_STATE_JSON)
            }
        }
    }

    private fun serializeSavedState(s: SavedStageState): String {
        val root = JSONObject()
        root.put("stageIndex", s.stageIndex)
        root.put("tiles", JSONArray(s.tiles))
        root.put("isRoundOver", s.isRoundOver)
        root.put("hintUsed", s.hintUsed)
        val guessArr = JSONArray()
        for (g in s.guesses) {
            val go = JSONObject()
            go.put("tiles", JSONArray(g.tiles))
            go.put("feedback", JSONArray(g.feedback.map { it.name }))
            go.put("word", g.word)
            go.put("isExactMatch", g.isExactMatch)
            guessArr.put(go)
        }
        root.put("guesses", guessArr)
        return root.toString()
    }

    private fun parseSavedState(json: String): SavedStageState? = try {
        val root = JSONObject(json)
        val tilesArr = root.getJSONArray("tiles")
        val tiles = (0 until tilesArr.length()).map { tilesArr.getString(it) }
        val guessesArr = root.getJSONArray("guesses")
        val guesses = (0 until guessesArr.length()).map { i ->
            val go = guessesArr.getJSONObject(i)
            val gt = go.getJSONArray("tiles")
            val gf = go.getJSONArray("feedback")
            SavedGuess(
                tiles = (0 until gt.length()).map { gt.getString(it) },
                feedback = (0 until gf.length()).map { TileState.valueOf(gf.getString(it)) },
                word = go.getString("word"),
                isExactMatch = go.getBoolean("isExactMatch")
            )
        }
        SavedStageState(
            stageIndex = root.getInt("stageIndex"),
            tiles = tiles,
            guesses = guesses,
            isRoundOver = root.getBoolean("isRoundOver"),
            hintUsed = root.getBoolean("hintUsed")
        )
    } catch (e: Exception) {
        null
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun saveAutoArrangeUsage(date: String, count: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AUTO_ARRANGE_DATE] = date
            prefs[Keys.AUTO_ARRANGE_COUNT] = count
        }
    }
}

class SettingsStore(private val context: Context) {
    private object Keys {
        val SOUND_ON = booleanPreferencesKey("sound_on")
        val HAPTIC_ON = booleanPreferencesKey("haptic_on")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val NICKNAME = stringPreferencesKey("nickname")
        val ONBOARDED = booleanPreferencesKey("onboarded")
    }

    val settingsFlow: Flow<Settings> = context.dataStore.data.map { prefs ->
        Settings(
            soundOn = prefs[Keys.SOUND_ON] ?: true,
            hapticOn = prefs[Keys.HAPTIC_ON] ?: true,
            darkMode = prefs[Keys.DARK_MODE] ?: false,
            nickname = prefs[Keys.NICKNAME] ?: ""
        )
    }

    val onboardedFlow: Flow<Boolean> = context.dataStore.data.map { it[Keys.ONBOARDED] ?: false }

    suspend fun setOnboarded() {
        context.dataStore.edit { it[Keys.ONBOARDED] = true }
    }

    suspend fun setSoundOn(on: Boolean) = context.dataStore.edit { it[Keys.SOUND_ON] = on }
    suspend fun setHapticOn(on: Boolean) = context.dataStore.edit { it[Keys.HAPTIC_ON] = on }
    suspend fun setDarkMode(on: Boolean) = context.dataStore.edit { it[Keys.DARK_MODE] = on }
    suspend fun setNickname(name: String) = context.dataStore.edit { it[Keys.NICKNAME] = name }
}
