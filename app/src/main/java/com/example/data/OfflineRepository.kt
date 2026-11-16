package com.example.data

import com.example.model.BikeCatalog
import com.example.model.BikeModel
import com.example.model.BikeUpgradeState
import com.example.model.Difficulty
import com.example.model.GameMode
import com.example.model.GeoWaypoint
import com.example.model.PresetWorldRoutes
import com.example.model.RealRoadTrack
import com.example.model.TrafficMode
import com.example.model.UpgradeStat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class OfflineRepository(
    private val dao: NitroRushDao,
    private val prefs: PreferencesStore
) {
    val playerPrefsFlow: Flow<PlayerPreferencesSnapshot> = prefs.snapshotFlow
    val topRunsFlow: Flow<List<RunRecordEntity>> = dao.observeTopRuns()

    val routesFlow: Flow<List<RealRoadTrack>> = dao.observeCachedRoutes().map { entities ->
        val cached = entities.mapNotNull { entityToRoute(it) }
        val gpsRoutes = cached.filter { it.isGpsDerived }
        gpsRoutes + PresetWorldRoutes.presets
    }

    suspend fun saveGpsRoutes(routes: List<RealRoadTrack>) {
        if (routes.isEmpty()) return
        dao.clearGpsRoutes()
        dao.upsertRoutes(routes.map { routeToEntity(it) })
    }

    suspend fun saveRunAndPayout(
        score: Int,
        distanceMeters: Int,
        topSpeedKmh: Int,
        nearMisses: Int,
        draftSeconds: Int,
        twoWayMeters: Int,
        pickupsCollected: Int,
        usdEarned: Int,
        bikeName: String,
        routeName: String,
        difficulty: Difficulty,
        gameMode: GameMode,
        trafficMode: TrafficMode,
        endReason: String,
        currentDay: Long
    ) {
        dao.insertRun(
            RunRecordEntity(
                score = score,
                distanceMeters = distanceMeters,
                topSpeedKmh = topSpeedKmh,
                nearMisses = nearMisses,
                usdEarned = usdEarned,
                bikeName = bikeName,
                routeName = routeName,
                difficulty = difficulty.label,
                gameMode = gameMode.label,
                trafficMode = trafficMode.label,
                endReason = endReason
            )
        )
        prefs.recordRunOutcome(
            score = score,
            distanceMeters = distanceMeters,
            topSpeedKmh = topSpeedKmh,
            nearMisses = nearMisses,
            draftSeconds = draftSeconds,
            twoWayMeters = twoWayMeters,
            pickupsCollected = pickupsCollected,
            usdEarned = usdEarned,
            currentDay = currentDay
        )
    }

    suspend fun setDifficulty(d: Difficulty) = prefs.setDifficulty(d)
    suspend fun setGameMode(m: GameMode) = prefs.setGameMode(m)
    suspend fun setTrafficMode(t: TrafficMode) = prefs.setTrafficMode(t)
    suspend fun setTiltEnabled(enabled: Boolean) = prefs.setTiltEnabled(enabled)
    suspend fun setMuted(muted: Boolean) = prefs.setMuted(muted)
    suspend fun setActiveRouteId(id: String) = prefs.setActiveRouteId(id)
    suspend fun equipBike(bikeId: String) = prefs.setEquippedBike(bikeId)

    suspend fun buyBike(bike: BikeModel): Boolean = prefs.buyBike(bike.id, bike.msrpUsd)

    suspend fun upgradeBike(bike: BikeModel, stat: UpgradeStat, currentUpgrades: BikeUpgradeState): Boolean {
        val currentLevel = when (stat) {
            UpgradeStat.ENGINE -> currentUpgrades.engineLevel
            UpgradeStat.TIRES -> currentUpgrades.tiresLevel
            UpgradeStat.NITRO -> currentUpgrades.nitroLevel
        }
        if (currentLevel >= BikeCatalog.MAX_UPGRADE_LEVEL) return false
        val cost = BikeCatalog.upgradeCostUsd(bike, currentLevel)
        return prefs.purchaseUpgrade(bike.id, cost) { cur ->
            when (stat) {
                UpgradeStat.ENGINE -> cur.copy(engineLevel = (cur.engineLevel + 1).coerceAtMost(BikeCatalog.MAX_UPGRADE_LEVEL))
                UpgradeStat.TIRES -> cur.copy(tiresLevel = (cur.tiresLevel + 1).coerceAtMost(BikeCatalog.MAX_UPGRADE_LEVEL))
                UpgradeStat.NITRO -> cur.copy(nitroLevel = (cur.nitroLevel + 1).coerceAtMost(BikeCatalog.MAX_UPGRADE_LEVEL))
            }
        }
    }

    suspend fun claimDailyMission(id: String, rewardUsd: Int, currentDay: Long): Boolean =
        prefs.claimDailyMission(id, rewardUsd, currentDay)

    suspend fun claimMilestone(id: String, rewardUsd: Int): Boolean =
        prefs.claimMilestone(id, rewardUsd)

    private fun routeToEntity(route: RealRoadTrack): CachedRouteEntity {
        val wpArr = JSONArray()
        route.waypoints.forEach { wp ->
            wpArr.put(JSONObject().apply {
                put("lat", wp.lat)
                put("lon", wp.lon)
            })
        }
        val curvArr = JSONArray()
        route.curvatureProfile.forEach { curvArr.put(it.toDouble()) }
        return CachedRouteEntity(
            id = route.id,
            roadName = route.roadName,
            regionLabel = route.regionLabel,
            roadClass = route.roadClass,
            waypointsJson = wpArr.toString(),
            curvatureJson = curvArr.toString(),
            lengthMeters = route.lengthMeters,
            isGpsDerived = route.isGpsDerived
        )
    }

    private fun entityToRoute(entity: CachedRouteEntity): RealRoadTrack? = runCatching {
        val wpArr = JSONArray(entity.waypointsJson)
        val waypoints = buildList {
            for (i in 0 until wpArr.length()) {
                val o = wpArr.getJSONObject(i)
                add(GeoWaypoint(o.getDouble("lat"), o.getDouble("lon")))
            }
        }
        val curvArr = JSONArray(entity.curvatureJson)
        val curves = buildList {
            for (i in 0 until curvArr.length()) {
                add(curvArr.getDouble(i).toFloat())
            }
        }
        RealRoadTrack(
            id = entity.id,
            roadName = entity.roadName,
            regionLabel = entity.regionLabel,
            roadClass = entity.roadClass,
            waypoints = waypoints,
            curvatureProfile = curves,
            lengthMeters = entity.lengthMeters,
            isGpsDerived = entity.isGpsDerived
        )
    }.getOrNull()
}
