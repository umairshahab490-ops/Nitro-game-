package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.HapticHelper
import com.example.audio.SoundEffect
import com.example.audio.SynthAudioEngine
import com.example.data.NitroRushDatabase
import com.example.data.OfflineRepository
import com.example.data.PlayerPreferencesSnapshot
import com.example.data.PreferencesStore
import com.example.data.RunRecordEntity
import com.example.game.GameEngine
import com.example.game.GameEventCallbacks
import com.example.game.RunSummary
import com.example.location.LocationTracker
import com.example.location.OverpassHighwayClient
import com.example.model.BikeCatalog
import com.example.model.BikeModel
import com.example.model.BikeUpgradeState
import com.example.model.CareerMilestone
import com.example.model.DailyMission
import com.example.model.Difficulty
import com.example.model.GameMode
import com.example.model.GameState
import com.example.model.GpsLocationState
import com.example.model.MissionCatalog
import com.example.model.MissionMetric
import com.example.model.PresetWorldRoutes
import com.example.model.RealRoadTrack
import com.example.model.TrafficMode
import com.example.model.UpgradeStat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HudUiState(
    val gameState: GameState = GameState.MENU,
    val score: Int = 0,
    val lives: Int = 3,
    val timeLeft: Float = 42f,
    val elapsedSeconds: Float = 0f,
    val distanceMeters: Int = 0,
    val speedKmh: Int = 0,
    val gear: Int = 1,
    val nitroPercent: Float = 0f,
    val nitroActive: Boolean = false,
    val isDrafting: Boolean = false,
    val hasShield: Boolean = false,
    val combo: Int = 0,
    val bonusCashUsd: Int = 0,
    val countdownLabel: String = "",
    val stageIndex: Int = 0,
    val routeProgressNorm: Float = 0f,
    val dailyMissions: List<DailyMission> = emptyList()
) {
    val activeMission: DailyMission?
        get() = dailyMissions.firstOrNull { !it.isComplete && !it.claimed }
            ?: dailyMissions.firstOrNull { !it.claimed }
            ?: dailyMissions.firstOrNull()
}

data class BannerMessage(
    val id: Long,
    val text: String,
    val colorArgb: Int
)

class NitroRushViewModel(application: Application) : AndroidViewModel(application), GameEventCallbacks {

    private val db = NitroRushDatabase.getInstance(application)
    private val prefsStore = PreferencesStore(application)
    private val repository = OfflineRepository(db.nitroRushDao(), prefsStore)
    private val audioEngine = SynthAudioEngine()
    private val haptics = HapticHelper(application)
    private val locationTracker = LocationTracker(application)
    private val overpassClient = OverpassHighwayClient()

    val gameEngine = GameEngine(this)

    val prefsState: StateFlow<PlayerPreferencesSnapshot> = repository.playerPrefsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = PlayerPreferencesSnapshot()
    )

    val routesState: StateFlow<List<RealRoadTrack>> = repository.routesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = PresetWorldRoutes.presets
    )

    val topRunsState: StateFlow<List<RunRecordEntity>> = repository.topRunsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    private val _hudState = MutableStateFlow(HudUiState())
    val hudState: StateFlow<HudUiState> = _hudState.asStateFlow()

    private val _gpsState = MutableStateFlow(
        GpsLocationState(permissionGranted = locationTracker.hasLocationPermission())
    )
    val gpsState: StateFlow<GpsLocationState> = _gpsState.asStateFlow()

    private val _bannerState = MutableStateFlow<BannerMessage?>(null)
    val bannerState: StateFlow<BannerMessage?> = _bannerState.asStateFlow()

    private val _lastRunSummary = MutableStateFlow<RunSummary?>(null)
    val lastRunSummary: StateFlow<RunSummary?> = _lastRunSummary.asStateFlow()

    private val _isNewBestRun = MutableStateFlow(false)
    val isNewBestRun: StateFlow<Boolean> = _isNewBestRun.asStateFlow()

    private var hudFrameCounter = 0
    private var bannerSeq = 0L
    private var pendingScanOnFix = false
    private var lastLiveNearMisses = -1
    private var lastLiveDraftSeconds = -1
    private var lastLiveTwoWayMeters = -1
    private var lastLivePickups = -1
    private var lastLiveTopSpeedKmh = -1

    init {
        audioEngine.start()

        viewModelScope.launch {
            combine(prefsState, routesState) { prefs, routes ->
                prefs to routes
            }.collectLatest { (prefs, routes) ->
                audioEngine.muted = prefs.muted
                val bike = BikeCatalog.findById(prefs.equippedBikeId)
                val upgrades = prefs.bikeUpgrades[bike.id] ?: BikeUpgradeState()
                val route = routes.firstOrNull { it.id == prefs.activeRouteId } ?: routes.first()
                gameEngine.applyConfig(
                    difficulty = prefs.difficulty,
                    gameMode = prefs.gameMode,
                    trafficMode = prefs.trafficMode,
                    bike = bike,
                    upgrades = upgrades,
                    route = route,
                    tiltEnabled = prefs.tiltEnabled
                )
                syncHudState(force = true)
            }
        }

        viewModelScope.launch {
            locationTracker.reading.collectLatest { reading ->
                if (reading != null) {
                    _gpsState.update {
                        it.copy(
                            latitude = reading.latitude,
                            longitude = reading.longitude,
                            accuracyMeters = reading.accuracyMeters,
                            liveGpsActive = true,
                            statusMessage = "GPS locked (±${reading.accuracyMeters.toInt()}m)"
                        )
                    }
                    if (pendingScanOnFix) {
                        pendingScanOnFix = false
                        scanNearbyHighways()
                    }
                }
            }
        }

        if (locationTracker.hasLocationPermission()) {
            locationTracker.startUpdates()
        }

        syncHudState(force = true)
    }

    fun stepSimulation(dt: Float, tiltSteer: Float) {
        gameEngine.tiltSteer = tiltSteer
        gameEngine.step(dt)

        when (gameEngine.state) {
            GameState.MENU, GameState.OVER -> {
                audioEngine.paused = false
                audioEngine.updateContinuous(0.28f, 0.22f, nitro = false, offroad = false, drafting = false)
            }
            GameState.COUNTDOWN -> {
                audioEngine.paused = false
                val rev = 0.25f + 0.15f * kotlin.math.sin(gameEngine.simClockMs * 0.02f)
                audioEngine.updateContinuous(rev, 0f, nitro = false, offroad = false, drafting = false)
            }
            GameState.RUNNING -> {
                audioEngine.paused = false
                audioEngine.updateContinuous(
                    rpm = gameEngine.currentRpm(),
                    speedPct = gameEngine.normalizedBikeSpeed().coerceIn(0f, 1.4f),
                    nitro = gameEngine.nitroOn,
                    offroad = gameEngine.offroad && gameEngine.speed > 1500f,
                    drafting = gameEngine.isDrafting
                )
            }
            GameState.DYING -> {
                gameEngine.clearTouchInputs()
                audioEngine.paused = false
                audioEngine.updateContinuous(
                    rpm = 0.14f,
                    speedPct = gameEngine.normalizedBikeSpeed().coerceIn(0f, 1.4f),
                    nitro = false,
                    offroad = false,
                    drafting = false
                )
            }
            GameState.PAUSED -> {
                gameEngine.clearTouchInputs()
                audioEngine.paused = true
            }
        }

        hudFrameCounter++
        val missionMetricsChanged = didLiveMissionMetricsChange()
        if (hudFrameCounter % 2 == 0 ||
            gameEngine.state != _hudState.value.gameState ||
            gameEngine.countdownLabel != _hudState.value.countdownLabel ||
            missionMetricsChanged
        ) {
            syncHudState()
        }
    }

    fun startRun() {
        _lastRunSummary.value = null
        _isNewBestRun.value = false
        _bannerState.value = null
        gameEngine.clearTouchInputs()
        gameEngine.startNewRun()
        hudFrameCounter = 0
        syncHudState(force = true)
    }

    fun pauseGame() {
        gameEngine.clearTouchInputs()
        gameEngine.pause()
        audioEngine.paused = true
        syncHudState(force = true)
    }

    fun resumeGame() {
        gameEngine.clearTouchInputs()
        gameEngine.resume()
        audioEngine.paused = false
        syncHudState(force = true)
    }

    fun returnToMenu() {
        _lastRunSummary.value = null
        _bannerState.value = null
        gameEngine.clearTouchInputs()
        gameEngine.returnToMenu()
        audioEngine.paused = false
        audioEngine.updateContinuous(0.28f, 0.22f, nitro = false, offroad = false, drafting = false)
        syncHudState(force = true)
    }

    fun clearTouchInput() {
        gameEngine.clearTouchInputs()
    }

    fun setTouchInput(left: Boolean? = null, right: Boolean? = null, brake: Boolean? = null, nitro: Boolean? = null) {
        val interactive = gameEngine.state == GameState.RUNNING || gameEngine.state == GameState.COUNTDOWN
        if (!interactive) {
            gameEngine.clearTouchInputs()
            return
        }
        if (left != null) gameEngine.touchLeft = left
        if (right != null) gameEngine.touchRight = right
        if (brake != null) gameEngine.touchBrake = brake
        if (nitro != null) gameEngine.touchNitro = nitro
    }

    fun setDifficulty(d: Difficulty) = viewModelScope.launch { repository.setDifficulty(d) }
    fun setGameMode(m: GameMode) = viewModelScope.launch { repository.setGameMode(m) }
    fun setTrafficMode(t: TrafficMode) = viewModelScope.launch { repository.setTrafficMode(t) }
    fun toggleTilt() = viewModelScope.launch { repository.setTiltEnabled(!prefsState.value.tiltEnabled) }
    fun toggleMute() = viewModelScope.launch { repository.setMuted(!prefsState.value.muted) }

    fun selectRoute(routeId: String) = viewModelScope.launch {
        repository.setActiveRouteId(routeId)
    }

    fun equipBike(bikeId: String) = viewModelScope.launch {
        repository.equipBike(bikeId)
        audioEngine.play(SoundEffect.PICKUP)
    }

    fun buyBike(bike: BikeModel) = viewModelScope.launch {
        val ok = repository.buyBike(bike)
        audioEngine.play(if (ok) SoundEffect.CASH else SoundEffect.DENY)
        if (ok) haptics.pulse(28L, 160)
    }

    fun upgradeBike(bike: BikeModel, stat: UpgradeStat) = viewModelScope.launch {
        val upgrades = prefsState.value.bikeUpgrades[bike.id] ?: BikeUpgradeState()
        val ok = repository.upgradeBike(bike, stat, upgrades)
        audioEngine.play(if (ok) SoundEffect.CASH else SoundEffect.DENY)
        if (ok) haptics.pulse(20L, 140)
    }

    fun currentDailyMissions(
        includeLiveRun: Boolean = isRunInProgress(gameEngine.state)
    ): List<DailyMission> {
        val day = currentEpochDay()
        val snap = prefsState.value
        val activeDay = snap.missionDay == day
        return MissionCatalog.dailyMissionsForDay(day).map { m ->
            val savedProgress = if (activeDay) (snap.dailyProgress[m.metric] ?: 0) else 0
            val liveProgress = if (includeLiveRun) {
                val runValue = liveMissionMetricValue(m.metric)
                if (m.metric == MissionMetric.TOP_SPEED_KMH) {
                    maxOf(savedProgress, runValue)
                } else {
                    savedProgress + runValue
                }
            } else {
                savedProgress
            }
            m.copy(
                progress = liveProgress,
                claimed = activeDay && m.id in snap.claimedDailyMissionIds
            )
        }
    }

    private fun isRunInProgress(state: GameState): Boolean =
        state == GameState.COUNTDOWN ||
            state == GameState.RUNNING ||
            state == GameState.PAUSED ||
            state == GameState.DYING

    private fun liveMissionMetricValue(metric: MissionMetric): Int = when (metric) {
        MissionMetric.NEAR_MISSES -> gameEngine.nearMisses
        MissionMetric.DRAFT_SECONDS -> gameEngine.draftSecondsAccum.toInt()
        MissionMetric.TWO_WAY_METERS -> gameEngine.twoWayMeters.toInt()
        MissionMetric.PICKUPS_COLLECTED -> gameEngine.pickupsCollected
        MissionMetric.TOP_SPEED_KMH -> gameEngine.topSpeedKmh.toInt()
        MissionMetric.RUNS_COMPLETED -> 0
    }

    private fun didLiveMissionMetricsChange(): Boolean {
        val nearMisses = gameEngine.nearMisses
        val draftSec = gameEngine.draftSecondsAccum.toInt()
        val twoWay = gameEngine.twoWayMeters.toInt()
        val pickups = gameEngine.pickupsCollected
        val topSpeed = gameEngine.topSpeedKmh.toInt()
        return nearMisses != lastLiveNearMisses ||
            draftSec != lastLiveDraftSeconds ||
            twoWay != lastLiveTwoWayMeters ||
            pickups != lastLivePickups ||
            topSpeed != lastLiveTopSpeedKmh
    }

    private fun syncHudState(force: Boolean = false) {
        val routeLen = gameEngine.activeRoute.lengthMeters.coerceAtLeast(1000)
        lastLiveNearMisses = gameEngine.nearMisses
        lastLiveDraftSeconds = gameEngine.draftSecondsAccum.toInt()
        lastLiveTwoWayMeters = gameEngine.twoWayMeters.toInt()
        lastLivePickups = gameEngine.pickupsCollected
        lastLiveTopSpeedKmh = gameEngine.topSpeedKmh.toInt()

        val next = HudUiState(
            gameState = gameEngine.state,
            score = gameEngine.score.toInt(),
            lives = gameEngine.lives,
            timeLeft = gameEngine.timeLeft,
            elapsedSeconds = gameEngine.elapsedRunSeconds,
            distanceMeters = gameEngine.distanceMeters.toInt(),
            speedKmh = (gameEngine.speed * GameEngine.KMH).toInt(),
            gear = gameEngine.currentGear(),
            nitroPercent = gameEngine.nitro,
            nitroActive = gameEngine.nitroOn,
            isDrafting = gameEngine.isDrafting,
            hasShield = gameEngine.hasShield,
            combo = gameEngine.combo,
            bonusCashUsd = gameEngine.bonusCashUsd,
            countdownLabel = gameEngine.countdownLabel,
            stageIndex = gameEngine.stageIdx,
            routeProgressNorm = (gameEngine.distanceMeters % routeLen) / routeLen.toFloat(),
            dailyMissions = currentDailyMissions(includeLiveRun = isRunInProgress(gameEngine.state))
        )
        if (force || next != _hudState.value) {
            _hudState.value = next
        }
    }

    fun currentMilestones(): List<CareerMilestone> {
        val snap = prefsState.value
        return MissionCatalog.buildMilestones(
            stats = snap.lifetimeStats,
            ownedBikeCount = snap.ownedBikeIds.size,
            claimedIds = snap.claimedMilestoneIds
        )
    }

    fun claimDailyMission(mission: DailyMission) = viewModelScope.launch {
        if (!mission.isComplete || mission.claimed) return@launch
        val ok = repository.claimDailyMission(mission.id, mission.rewardUsd, currentEpochDay())
        if (ok) {
            audioEngine.play(SoundEffect.CASH)
            haptics.pulse(25L, 160)
        }
    }

    fun claimMilestone(milestone: CareerMilestone) = viewModelScope.launch {
        if (!milestone.isComplete || milestone.claimed) return@launch
        val ok = repository.claimMilestone(milestone.id, milestone.rewardUsd)
        if (ok) {
            audioEngine.play(SoundEffect.CASH)
            haptics.pulse(30L, 180)
        }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        _gpsState.update {
            it.copy(
                permissionGranted = granted,
                statusMessage = if (granted) "Acquiring live GPS fix..." else "Location permission denied — using world presets"
            )
        }
        if (granted) {
            locationTracker.startUpdates()
            scanNearbyHighways()
        }
    }

    fun scanNearbyHighways() {
        val hasPerm = locationTracker.hasLocationPermission()
        if (!hasPerm) {
            _gpsState.update { it.copy(permissionGranted = false, statusMessage = "Grant location permission to scan your nearby roads") }
            return
        }
        locationTracker.startUpdates()
        val reading = locationTracker.reading.value
        if (reading == null) {
            pendingScanOnFix = true
            _gpsState.update { it.copy(permissionGranted = true, statusMessage = "Acquiring GPS lock… will scan nearby roads automatically") }
            return
        }
        pendingScanOnFix = false

        viewModelScope.launch {
            _gpsState.update {
                it.copy(
                    isScanningRoads = true,
                    statusMessage = "Scanning OpenStreetMap highways near you…"
                )
            }
            val result = overpassClient.fetchNearbyHighways(reading.latitude, reading.longitude)
            result.onSuccess { roads ->
                if (roads.isNotEmpty()) {
                    repository.saveGpsRoutes(roads)
                    repository.setActiveRouteId(roads.first().id)
                    _gpsState.update {
                        it.copy(
                            isScanningRoads = false,
                            statusMessage = "Loaded ${roads.size} real highways near your GPS"
                        )
                    }
                    audioEngine.play(SoundEffect.PICKUP)
                } else {
                    _gpsState.update {
                        it.copy(
                            isScanningRoads = false,
                            statusMessage = "No major highways within 4.5 km — preset routes ready"
                        )
                    }
                }
            }.onFailure { err ->
                _gpsState.update {
                    it.copy(
                        isScanningRoads = false,
                        statusMessage = "Map scan unavailable (${err.message ?: "offline"}) — using cached/preset routes"
                    )
                }
            }
        }
    }

    fun onAppPause() {
        gameEngine.clearTouchInputs()
        if (gameEngine.state == GameState.RUNNING) {
            pauseGame()
        } else {
            audioEngine.paused = true
        }
        locationTracker.stopUpdates()
    }

    fun onAppResume() {
        if (gameEngine.state != GameState.PAUSED) {
            audioEngine.paused = false
        }
        if (locationTracker.hasLocationPermission()) {
            locationTracker.startUpdates()
        }
    }

    override fun onSound(sfx: SoundEffect) {
        audioEngine.play(sfx)
    }

    override fun onHaptic(durationMs: Long, amplitude: Int) {
        haptics.pulse(durationMs, amplitude)
    }

    override fun onBanner(text: String, colorArgb: Int) {
        bannerSeq++
        _bannerState.value = BannerMessage(bannerSeq, text, colorArgb)
    }

    override fun onRunFinished(summary: RunSummary) {
        gameEngine.clearTouchInputs()
        audioEngine.paused = false
        audioEngine.updateContinuous(0.28f, 0.22f, nitro = false, offroad = false, drafting = false)
        val prevBest = prefsState.value.bestScore
        _isNewBestRun.value = summary.score > prevBest
        _lastRunSummary.value = summary
        syncHudState(force = true)
        viewModelScope.launch {
            repository.saveRunAndPayout(
                score = summary.score,
                distanceMeters = summary.distanceMeters,
                topSpeedKmh = summary.topSpeedKmh,
                nearMisses = summary.nearMisses,
                draftSeconds = summary.draftSeconds,
                twoWayMeters = summary.twoWayMeters,
                pickupsCollected = summary.pickupsCollected,
                usdEarned = summary.usdEarned,
                bikeName = summary.bikeName,
                routeName = summary.routeName,
                difficulty = summary.difficulty,
                gameMode = summary.gameMode,
                trafficMode = summary.trafficMode,
                endReason = summary.endReason,
                currentDay = currentEpochDay()
            )
        }
    }

    private fun currentEpochDay(): Long = System.currentTimeMillis() / 86_400_000L

    override fun onCleared() {
        super.onCleared()
        audioEngine.stop()
        locationTracker.stopUpdates()
    }
}
