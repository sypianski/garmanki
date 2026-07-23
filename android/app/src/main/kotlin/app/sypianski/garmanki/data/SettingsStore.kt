package app.sypianski.garmanki.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

data class Settings(
    val selectedDecks: Set<String>,
    val cardLimit: Int,
    val lastRev: Int,
    val lastAppliedBatch: Int,
    /** epochDay → watch-originated answers applied that day (SCHEMA.md §7). */
    val replayLog: Map<Long, Int>,
    val lastAnkiSyncMs: Long,
    /** Watch-UI config, SCHEMA.md §8: event → ease 1–4, 0 = no grade. */
    val actionMap: Map<String, Int> = SettingsStore.DEFAULT_ACTION_MAP,
    val cardActions: Set<String> = SettingsStore.ALL_CARD_ACTIONS,
    val guideReset: Int = 0,
    /** App appearance, SettingsStore.THEME_*: "system" (dynamic/slate) or "eink". */
    val themeMode: String = SettingsStore.THEME_SYSTEM,
)

/** Persistence seam for SyncEngine — faked in unit tests. */
interface SyncPrefs {
    suspend fun snapshot(): Settings
    suspend fun setLastRev(rev: Int)
    suspend fun setLastAppliedBatch(batch: Int)
    suspend fun recordReplays(count: Int, epochDay: Long)
    suspend fun setLastAnkiSyncMs(ms: Long)
}

private val Context.dataStore by preferencesDataStore(name = "garmanki")

class SettingsStore(private val context: Context) : SyncPrefs {

    private object Keys {
        val selectedDecks = stringSetPreferencesKey("selectedDecks")
        val cardLimit = intPreferencesKey("cardLimit")
        val lastRev = intPreferencesKey("lastRev")
        val lastAppliedBatch = intPreferencesKey("lastAppliedBatch")
        val replayLog = stringPreferencesKey("replayLog")
        val lastAnkiSyncMs = longPreferencesKey("lastAnkiSyncMs")
        val actionMap = stringPreferencesKey("actionMap")
        val cardActions = stringSetPreferencesKey("cardActions")
        val guideReset = intPreferencesKey("guideReset")
        val themeMode = stringPreferencesKey("themeMode")
    }

    private val logSerializer = MapSerializer(Long.serializer(), Int.serializer())
    private val mapSerializer = MapSerializer(String.serializer(), Int.serializer())
    private val json = Json { ignoreUnknownKeys = true }

    val flow: Flow<Settings> = context.dataStore.data.map { p ->
        Settings(
            selectedDecks = p[Keys.selectedDecks] ?: emptySet(),
            cardLimit = p[Keys.cardLimit] ?: DEFAULT_CARD_LIMIT,
            lastRev = p[Keys.lastRev] ?: 0,
            lastAppliedBatch = p[Keys.lastAppliedBatch] ?: 0,
            replayLog = decodeLog(p[Keys.replayLog]),
            lastAnkiSyncMs = p[Keys.lastAnkiSyncMs] ?: 0L,
            actionMap = decodeActionMap(p[Keys.actionMap]),
            cardActions = p[Keys.cardActions] ?: ALL_CARD_ACTIONS,
            guideReset = p[Keys.guideReset] ?: 0,
            themeMode = p[Keys.themeMode] ?: THEME_SYSTEM,
        )
    }

    override suspend fun snapshot(): Settings = flow.first()

    suspend fun setSelectedDecks(ids: Set<String>) {
        context.dataStore.edit { it[Keys.selectedDecks] = ids }
    }

    suspend fun setCardLimit(limit: Int) {
        context.dataStore.edit { it[Keys.cardLimit] = limit.coerceIn(10, 100) }
    }

    override suspend fun setLastRev(rev: Int) {
        context.dataStore.edit { it[Keys.lastRev] = rev }
    }

    override suspend fun setLastAppliedBatch(batch: Int) {
        context.dataStore.edit { it[Keys.lastAppliedBatch] = batch }
    }

    override suspend fun setLastAnkiSyncMs(ms: Long) {
        context.dataStore.edit { it[Keys.lastAnkiSyncMs] = ms }
    }

    override suspend fun recordReplays(count: Int, epochDay: Long) {
        if (count <= 0) return
        context.dataStore.edit { p ->
            val log = decodeLog(p[Keys.replayLog]).toMutableMap()
            log[epochDay] = (log[epochDay] ?: 0) + count
            // prune entries older than 400 days so the blob stays small
            log.keys.filter { it < epochDay - 400 }.forEach { log.remove(it) }
            p[Keys.replayLog] = json.encodeToString(logSerializer, log)
        }
    }

    suspend fun setActionMap(map: Map<String, Int>) {
        val clean = map.filterKeys { it in WATCH_EVENTS }
            .mapValues { it.value.coerceIn(0, 4) }
        context.dataStore.edit {
            it[Keys.actionMap] = json.encodeToString(mapSerializer, clean)
        }
    }

    suspend fun setCardActions(actions: Set<String>) {
        context.dataStore.edit {
            it[Keys.cardActions] = actions.intersect(ALL_CARD_ACTIONS)
        }
    }

    suspend fun setThemeMode(mode: String) {
        val clean = if (mode == THEME_EINK) THEME_EINK else THEME_SYSTEM
        context.dataStore.edit { it[Keys.themeMode] = clean }
    }

    /** SCHEMA.md §8 `gr`: any new value replays the guide on the watch. */
    suspend fun bumpGuideReset() {
        context.dataStore.edit { it[Keys.guideReset] = (it[Keys.guideReset] ?: 0) + 1 }
    }

    private fun decodeLog(raw: String?): Map<Long, Int> = try {
        if (raw == null) emptyMap() else json.decodeFromString(logSerializer, raw)
    } catch (t: Throwable) {
        emptyMap()
    }

    private fun decodeActionMap(raw: String?): Map<String, Int> = try {
        if (raw == null) DEFAULT_ACTION_MAP
        else json.decodeFromString(mapSerializer, raw)
    } catch (t: Throwable) {
        DEFAULT_ACTION_MAP
    }

    companion object {
        const val DEFAULT_CARD_LIMIT = 100

        /** Appearance modes (Settings.themeMode). */
        const val THEME_SYSTEM = "system"
        const val THEME_EINK = "eink"

        /** Watch input events, SCHEMA.md §8 / DECYZJE.md D13–D16. */
        val WATCH_EVENTS = listOf(
            "down", "start", "up", "tap", "swipeR", "swipeL", "swipeU", "swipeD",
        )

        /** Defaults mirror the watch's ActionMap.defaults(). */
        val DEFAULT_ACTION_MAP = mapOf(
            "up" to 4, "down" to 3, "start" to 1, "tap" to 3,
            "swipeR" to 3, "swipeL" to 1, "swipeU" to 4, "swipeD" to 2,
        )

        val ALL_CARD_ACTIONS = setOf("susp", "bury", "flag", "del")
    }
}
