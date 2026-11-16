package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.BikeCatalog
import com.example.model.BikeUpgradeState
import com.example.model.Difficulty
import com.example.model.GameMode
import com.example.model.LifetimeStats
import com.example.model.MissionMetric
import com.example.model.PresetWorldRoutes
import com.example.model.TrafficMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject

private val Context.nitroDataStore: DataStore<Preferences> by preferencesDataStore(name = "nitro_rush_prefs")

data class PlayerPreferencesSnapshot(
    val usdBalance: Int = 1200,
    val ownedBikeIds: Set<String> = setOf(BikeCatalog.bikes.first().id),
    val equippedBikeId: String = BikeCatalog.bikes.first().id,
    val bikeUpgrades: Map<String, BikeUpgradeState> = emptyMap(),
    val difficulty: Difficulty = Difficulty.PRO,
    val gameMode: GameMode = GameMode.TIME_ATTACK,
    val trafficMode: TrafficMode = TrafficMode.ONE_WAY,
    val tiltEnabled: Boolean = false,
    val muted: Boolean = false,
    val bestScore: Int = 0,
    val bestDistanceMeters: Int = 0,
    val bestTopSpeedKmh: Int = 0,
    val activeRouteId: String = PresetWorldRoutes.presets.first().id,
    val missionDay: Long = 0L,
    val dailyProgress: Map<MissionMetric, Int> = emptyMap(),
    val claimedDailyMissionIds: Set<String> = emptySet(),
    val claimedMilestoneIds: Set<String> = emptySet(),
    val lifetimeStats: LifetimeStats = LifetimeStats()
)

class PreferencesStore(private val context: Context) {

    private object Keys {
        val USD_BALANCE = intPreferencesKey("usd_balance")
        val OWNED_BIKES = stringSetPreferencesKey("owned_bikes")
        val EQUIPPED_BIKE = stringPreferencesKey("equipped_bike")
        val BIKE_UPGRADES_JSON = stringPreferencesKey("bike_upgrades_json")
        val DIFFICULTY = stringPreferencesKey("difficulty")
        val GAME_MODE = stringPreferencesKey("game_mode")
        val TRAFFIC_MODE = stringPreferencesKey("traffic_mode")
        val TILT_ENABLED = booleanPreferencesKey("tilt_enabled")
        val MUTED = booleanPreferencesKey("muted")
        val BEST_SCORE = intPreferencesKey("best_score")
        val BEST_DIST = intPreferencesKey("best_dist")
        val BEST_TOP_SPEED = intPreferencesKey("best_top_speed")
        val ACTIVE_ROUTE_ID = stringPreferencesKey("active_route_id")
        val MISSION_DAY = longPreferencesKey("mission_day")
        val DAILY_PROGRESS_JSON = stringPreferencesKey("daily_progress_json")
        val CLAIMED_DAILY_MISSIONS = stringSetPreferencesKey("claimed_daily_missions")
        val CLAIMED_MILESTONES = stringSetPreferencesKey("claimed_milestones")
        val LIFETIME_STATS_JSON = stringPreferencesKey("lifetime_stats_json")
    }

    val snapshotFlow: Flow<PlayerPreferencesSnapshot> = context.nitroDataStore.data.map { prefs ->
        val starterId = BikeCatalog.bikes.first().id
        val owned = prefs[Keys.OWNED_BIKES]?.takeIf { it.isNotEmpty() } ?: setOf(starterId)
        val equipped = prefs[Keys.EQUIPPED_BIKE]?.takeIf { it in owned } ?: starterId
        PlayerPreferencesSnapshot(
            usdBalance = prefs[Keys.USD_BALANCE] ?: 1200,
            ownedBikeIds = owned,
            equippedBikeId = equipped,
            bikeUpgrades = parseUpgrades(prefs[Keys.BIKE_UPGRADES_JSON]),
            difficulty = runCatching { Difficulty.valueOf(prefs[Keys.DIFFICULTY] ?: "PRO") }
                .getOrDefault(Difficulty.PRO),
            gameMode = runCatching { GameMode.valueOf(prefs[Keys.GAME_MODE] ?: "TIME_ATTACK") }
                .getOrDefault(GameMode.TIME_ATTACK),
            trafficMode = runCatching { TrafficMode.valueOf(prefs[Keys.TRAFFIC_MODE] ?: "ONE_WAY") }
                .getOrDefault(TrafficMode.ONE_WAY),
            tiltEnabled = prefs[Keys.TILT_ENABLED] ?: false,
            muted = prefs[Keys.MUTED] ?: false,
            bestScore = prefs[Keys.BEST_SCORE] ?: 0,
            bestDistanceMeters = prefs[Keys.BEST_DIST] ?: 0,
            bestTopSpeedKmh = prefs[Keys.BEST_TOP_SPEED] ?: 0,
            activeRouteId = prefs[Keys.ACTIVE_ROUTE_ID] ?: PresetWorldRoutes.presets.first().id,
            missionDay = prefs[Keys.MISSION_DAY] ?: 0L,
            dailyProgress = parseDailyProgress(prefs[Keys.DAILY_PROGRESS_JSON]),
            claimedDailyMissionIds = prefs[Keys.CLAIMED_DAILY_MISSIONS] ?: emptySet(),
            claimedMilestoneIds = prefs[Keys.CLAIMED_MILESTONES] ?: emptySet(),
            lifetimeStats = parseLifetimeStats(prefs[Keys.LIFETIME_STATS_JSON])
        )
    }

    suspend fun update(transform: suspend (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.nitroDataStore.edit(transform)
    }

    suspend fun setDifficulty(difficulty: Difficulty) = update { it[Keys.DIFFICULTY] = difficulty.name }
    suspend fun setGameMode(mode: GameMode) = update { it[Keys.GAME_MODE] = mode.name }
    suspend fun setTrafficMode(mode: TrafficMode) = update { it[Keys.TRAFFIC_MODE] = mode.name }
    suspend fun setTiltEnabled(enabled: Boolean) = update { it[Keys.TILT_ENABLED] = enabled }
    suspend fun setMuted(muted: Boolean) = update { it[Keys.MUTED] = muted }
    suspend fun setActiveRouteId(routeId: String) = update { it[Keys.ACTIVE_ROUTE_ID] = routeId }
    suspend fun setEquippedBike(bikeId: String) = update { it[Keys.EQUIPPED_BIKE] = bikeId }

    suspend fun buyBike(bikeId: String, priceUsd: Int): Boolean {
        var success = false
        update { prefs ->
            val currentBal = prefs[Keys.USD_BALANCE] ?: 1200
            val starterId = BikeCatalog.bikes.first().id
            val owned = (prefs[Keys.OWNED_BIKES] ?: setOf(starterId)).toMutableSet()
            if (bikeId !in owned && currentBal >= priceUsd) {
                prefs[Keys.USD_BALANCE] = currentBal - priceUsd
                owned.add(bikeId)
                prefs[Keys.OWNED_BIKES] = owned
                prefs[Keys.EQUIPPED_BIKE] = bikeId
                success = true
            }
        }
        return success
    }

    suspend fun purchaseUpgrade(
        bikeId: String,
        costUsd: Int,
        transform: (BikeUpgradeState) -> BikeUpgradeState
    ): Boolean {
        var success = false
        update { prefs ->
            val currentBal = prefs[Keys.USD_BALANCE] ?: 1200
            val map = parseUpgrades(prefs[Keys.BIKE_UPGRADES_JSON]).toMutableMap()
            val cur = map[bikeId] ?: BikeUpgradeState()
            val next = transform(cur)
            if (next != cur && currentBal >= costUsd) {
                prefs[Keys.USD_BALANCE] = currentBal - costUsd
                map[bikeId] = next
                prefs[Keys.BIKE_UPGRADES_JSON] = encodeUpgrades(map)
                success = true
            }
        }
        return success
    }

    suspend fun recordRunOutcome(
        score: Int,
        distanceMeters: Int,
        topSpeedKmh: Int,
        nearMisses: Int,
        draftSeconds: Int,
        twoWayMeters: Int,
        pickupsCollected: Int,
        usdEarned: Int,
        currentDay: Long
    ) {
        update { prefs ->
            val currentBal = prefs[Keys.USD_BALANCE] ?: 1200
            prefs[Keys.USD_BALANCE] = currentBal + usdEarned
            prefs[Keys.BEST_SCORE] = maxOf(prefs[Keys.BEST_SCORE] ?: 0, score)
            prefs[Keys.BEST_DIST] = maxOf(prefs[Keys.BEST_DIST] ?: 0, distanceMeters)
            prefs[Keys.BEST_TOP_SPEED] = maxOf(prefs[Keys.BEST_TOP_SPEED] ?: 0, topSpeedKmh)

            val savedDay = prefs[Keys.MISSION_DAY] ?: 0L
            val dailyMap = if (savedDay == currentDay) {
                parseDailyProgress(prefs[Keys.DAILY_PROGRESS_JSON]).toMutableMap()
            } else {
                prefs[Keys.CLAIMED_DAILY_MISSIONS] = emptySet()
                mutableMapOf()
            }
            prefs[Keys.MISSION_DAY] = currentDay
            dailyMap[MissionMetric.NEAR_MISSES] = (dailyMap[MissionMetric.NEAR_MISSES] ?: 0) + nearMisses
            dailyMap[MissionMetric.DRAFT_SECONDS] = (dailyMap[MissionMetric.DRAFT_SECONDS] ?: 0) + draftSeconds
            dailyMap[MissionMetric.TWO_WAY_METERS] = (dailyMap[MissionMetric.TWO_WAY_METERS] ?: 0) + twoWayMeters
            dailyMap[MissionMetric.PICKUPS_COLLECTED] = (dailyMap[MissionMetric.PICKUPS_COLLECTED] ?: 0) + pickupsCollected
            dailyMap[MissionMetric.TOP_SPEED_KMH] = maxOf(dailyMap[MissionMetric.TOP_SPEED_KMH] ?: 0, topSpeedKmh)
            dailyMap[MissionMetric.RUNS_COMPLETED] = (dailyMap[MissionMetric.RUNS_COMPLETED] ?: 0) + 1
            prefs[Keys.DAILY_PROGRESS_JSON] = encodeDailyProgress(dailyMap)

            val oldStats = parseLifetimeStats(prefs[Keys.LIFETIME_STATS_JSON])
            val updatedStats = oldStats.copy(
                totalUsdEarned = oldStats.totalUsdEarned + usdEarned,
                totalNearMisses = oldStats.totalNearMisses + nearMisses,
                totalDraftSeconds = oldStats.totalDraftSeconds + draftSeconds,
                totalTwoWayMeters = oldStats.totalTwoWayMeters + twoWayMeters,
                totalPickups = oldStats.totalPickups + pickupsCollected,
                highestSpeedKmh = maxOf(oldStats.highestSpeedKmh, topSpeedKmh),
                totalRuns = oldStats.totalRuns + 1
            )
            prefs[Keys.LIFETIME_STATS_JSON] = encodeLifetimeStats(updatedStats)
        }
    }

    suspend fun claimDailyMission(missionId: String, rewardUsd: Int, currentDay: Long): Boolean {
        var claimed = false
        update { prefs ->
            val savedDay = prefs[Keys.MISSION_DAY] ?: 0L
            if (savedDay != currentDay) return@update
            val claimedSet = (prefs[Keys.CLAIMED_DAILY_MISSIONS] ?: emptySet()).toMutableSet()
            if (missionId !in claimedSet) {
                claimedSet.add(missionId)
                prefs[Keys.CLAIMED_DAILY_MISSIONS] = claimedSet
                prefs[Keys.USD_BALANCE] = (prefs[Keys.USD_BALANCE] ?: 1200) + rewardUsd
                claimed = true
            }
        }
        return claimed
    }

    suspend fun claimMilestone(milestoneId: String, rewardUsd: Int): Boolean {
        var claimed = false
        update { prefs ->
            val claimedSet = (prefs[Keys.CLAIMED_MILESTONES] ?: emptySet()).toMutableSet()
            if (milestoneId !in claimedSet) {
                claimedSet.add(milestoneId)
                prefs[Keys.CLAIMED_MILESTONES] = claimedSet
                prefs[Keys.USD_BALANCE] = (prefs[Keys.USD_BALANCE] ?: 1200) + rewardUsd
                claimed = true
            }
        }
        return claimed
    }

    private fun parseUpgrades(json: String?): Map<String, BikeUpgradeState> {
        if (json.isNullOrBlank()) return emptyMap()
        return runCatching {
            val obj = JSONObject(json)
            val result = mutableMapOf<String, BikeUpgradeState>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val id = keys.next()
                val item = obj.getJSONObject(id)
                result[id] = BikeUpgradeState(
                    engineLevel = item.optInt("e", 1).coerceIn(1, BikeCatalog.MAX_UPGRADE_LEVEL),
                    tiresLevel = item.optInt("t", 1).coerceIn(1, BikeCatalog.MAX_UPGRADE_LEVEL),
                    nitroLevel = item.optInt("n", 1).coerceIn(1, BikeCatalog.MAX_UPGRADE_LEVEL)
                )
            }
            result
        }.getOrDefault(emptyMap())
    }

    private fun encodeUpgrades(map: Map<String, BikeUpgradeState>): String {
        val root = JSONObject()
        map.forEach { (id, st) ->
            root.put(id, JSONObject().apply {
                put("e", st.engineLevel)
                put("t", st.tiresLevel)
                put("n", st.nitroLevel)
            })
        }
        return root.toString()
    }

    private fun parseDailyProgress(json: String?): Map<MissionMetric, Int> {
        if (json.isNullOrBlank()) return emptyMap()
        return runCatching {
            val obj = JSONObject(json)
            val out = mutableMapOf<MissionMetric, Int>()
            MissionMetric.entries.forEach { metric ->
                if (obj.has(metric.name)) out[metric] = obj.getInt(metric.name)
            }
            out
        }.getOrDefault(emptyMap())
    }

    private fun encodeDailyProgress(map: Map<MissionMetric, Int>): String {
        val root = JSONObject()
        map.forEach { (k, v) -> root.put(k.name, v) }
        return root.toString()
    }

    private fun parseLifetimeStats(json: String?): LifetimeStats {
        if (json.isNullOrBlank()) return LifetimeStats()
        return runCatching {
            val obj = JSONObject(json)
            LifetimeStats(
                totalUsdEarned = obj.optInt("usd", 0),
                totalNearMisses = obj.optInt("near", 0),
                totalDraftSeconds = obj.optInt("draft", 0),
                totalTwoWayMeters = obj.optInt("twoWay", 0),
                totalPickups = obj.optInt("pickups", 0),
                highestSpeedKmh = obj.optInt("topSpeed", 0),
                totalRuns = obj.optInt("runs", 0)
            )
        }.getOrDefault(LifetimeStats())
    }

    private fun encodeLifetimeStats(s: LifetimeStats): String = JSONObject().apply {
        put("usd", s.totalUsdEarned)
        put("near", s.totalNearMisses)
        put("draft", s.totalDraftSeconds)
        put("twoWay", s.totalTwoWayMeters)
        put("pickups", s.totalPickups)
        put("topSpeed", s.highestSpeedKmh)
        put("runs", s.totalRuns)
    }.toString()
}
