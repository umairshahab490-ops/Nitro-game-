package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.GameEngine
import com.example.game.HighwayCanvasRenderer
import com.example.model.GameMode
import com.example.model.GameState
import com.example.model.RealRoadTrack
import com.example.sensor.TiltSteeringSensor
import com.example.ui.NitroRushViewModel
import com.example.ui.components.AdaptiveRacingHudAndControls
import com.example.ui.components.GarageSheet
import com.example.ui.components.GpsMapSheet
import com.example.ui.components.MainMenuOverlay
import com.example.ui.components.MissionsAndRunsSheet
import com.example.ui.components.PaddockNavTab
import com.example.ui.components.PauseOverlay
import com.example.ui.components.RunOverOverlay
import com.example.ui.components.SettingsSheet
import com.example.ui.components.TopUtilityButtons
import com.example.ui.theme.NitroRushTheme
import com.example.ui.theme.ObsidianBg
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private enum class ActiveSheet {
    NONE,
    GARAGE,
    MISSIONS,
    GPS_MAP,
    SETTINGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NitroRushTheme {
                NitroRushApp()
            }
        }
    }
}

@Composable
fun NitroRushApp(
    vm: NitroRushViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val gameState by remember(vm) {
        vm.hudState.map { it.gameState }.distinctUntilChanged()
    }.collectAsStateWithLifecycle(initialValue = vm.hudState.value.gameState)
    val prefs by vm.prefsState.collectAsStateWithLifecycle()
    val routes by vm.routesState.collectAsStateWithLifecycle()
    val gpsState by vm.gpsState.collectAsStateWithLifecycle()

    var activeSheet by remember { mutableStateOf(ActiveSheet.NONE) }
    val currentActiveSheet by rememberUpdatedState(activeSheet)
    val tiltSensor = remember(context) { TiltSteeringSensor(context) }
    val tiltAvailable = remember(tiltSensor) { tiltSensor.isAvailable() }
    val renderer = remember { HighwayCanvasRenderer() }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        vm.onLocationPermissionResult(granted)
    }

    // Lifecycle audio/sensor handling
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    vm.onAppPause()
                    tiltSensor.stop()
                }
                Lifecycle.Event.ON_RESUME -> vm.onAppResume()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            tiltSensor.stop()
        }
    }

    // Enable Tilt sensor only when actively racing with Tilt enabled and no blocking sheet visible
    LaunchedEffect(prefs.tiltEnabled, gameState, activeSheet) {
        if (prefs.tiltEnabled && gameState == GameState.RUNNING && activeSheet == ActiveSheet.NONE) {
            tiltSensor.start()
        } else {
            tiltSensor.stop()
        }
    }

    val activeRoute = remember(routes, prefs.activeRouteId) {
        routes.firstOrNull { it.id == prefs.activeRouteId } ?: routes.first()
    }
    val dailyMissions = remember(prefs) { vm.currentDailyMissions(includeLiveRun = false) }
    val milestones = remember(prefs) { vm.currentMilestones() }
    val claimableCount = remember(dailyMissions, milestones) {
        dailyMissions.count { it.isComplete && !it.claimed } +
            milestones.count { it.isComplete && !it.claimed }
    }

    // Frame-loop runs at 60 FPS only during active gameplay with no blocking sheet/overlay.
    val isFrameLoopActive = activeSheet == ActiveSheet.NONE && (
        gameState == GameState.COUNTDOWN ||
            gameState == GameState.RUNNING ||
            gameState == GameState.DYING
        )
    var frameTick by remember { mutableLongStateOf(0L) }
    val lastDtRef = remember { FloatArray(1) { GameEngine.STEP } }

    LaunchedEffect(isFrameLoopActive) {
        if (!isFrameLoopActive) return@LaunchedEffect
        var lastNs = 0L
        var accumulator = 0f
        while (true) {
            withFrameNanos { nowNs ->
                if (lastNs == 0L) lastNs = nowNs
                val dt = ((nowNs - lastNs) / 1_000_000_000f).coerceIn(0f, 0.1f)
                lastNs = nowNs
                lastDtRef[0] = dt

                val engineState = vm.gameEngine.state
                if (currentActiveSheet == ActiveSheet.NONE && (
                        engineState == GameState.COUNTDOWN ||
                            engineState == GameState.RUNNING ||
                            engineState == GameState.DYING
                        )
                ) {
                    accumulator += dt
                    var steps = 0
                    while (accumulator >= GameEngine.STEP && steps < 4) {
                        vm.stepSimulation(GameEngine.STEP, tiltSensor.steerValue)
                        accumulator -= GameEngine.STEP
                        steps++
                    }
                    if (steps == 4) accumulator = 0f
                    frameTick = nowNs
                }
            }
        }
    }

    // Render one static frame when gameplay stops or visual config changes while idle/visible.
    LaunchedEffect(isFrameLoopActive, gameState, activeSheet, activeRoute.id, prefs.equippedBikeId, prefs.bikePaintIds) {
        if (!isFrameLoopActive && activeSheet == ActiveSheet.NONE) {
            lastDtRef[0] = 0f
            frameTick = frameTick + 1L
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
    ) {
        // Pseudo-3D Highway Canvas (draws only on frameTick invalidation)
        Canvas(modifier = Modifier.fillMaxSize()) {
            @Suppress("UNUSED_EXPRESSION")
            frameTick
            renderer.render(this, vm.gameEngine, lastDtRef[0])
        }

        val showRaceHud = gameState == GameState.COUNTDOWN ||
            gameState == GameState.RUNNING ||
            gameState == GameState.DYING ||
            gameState == GameState.PAUSED

        LaunchedEffect(activeSheet, showRaceHud) {
            if (activeSheet != ActiveSheet.NONE || !showRaceHud) {
                vm.clearTouchInput()
            }
        }

        if (showRaceHud && activeSheet == ActiveSheet.NONE) {
            RaceHudHost(
                vm = vm,
                bestScore = prefs.bestScore,
                gameMode = prefs.gameMode,
                activeRoute = activeRoute,
                liveGpsLocked = gpsState.liveGpsActive,
                tiltEnabled = prefs.tiltEnabled
            )
        }

        if (gameState == GameState.MENU && activeSheet == ActiveSheet.NONE) {
            MainMenuOverlay(
                prefs = prefs,
                activeRoute = activeRoute,
                claimableMissionCount = claimableCount,
                onOpenGarage = { activeSheet = ActiveSheet.GARAGE },
                onOpenMissions = { activeSheet = ActiveSheet.MISSIONS },
                onOpenGpsMap = { activeSheet = ActiveSheet.GPS_MAP },
                onOpenSettings = { activeSheet = ActiveSheet.SETTINGS },
                onStartRide = { vm.startRun() }
            )
        }

        if (gameState == GameState.PAUSED && activeSheet == ActiveSheet.NONE) {
            PauseOverlay(
                tiltEnabled = prefs.tiltEnabled,
                tiltAvailable = tiltAvailable,
                onToggleTilt = { vm.toggleTilt() },
                onResume = { vm.resumeGame() },
                onRestart = { vm.startRun() },
                onMenu = { vm.returnToMenu() }
            )
        }

        if (gameState == GameState.OVER && activeSheet == ActiveSheet.NONE) {
            val lastSummary by vm.lastRunSummary.collectAsStateWithLifecycle()
            val isNewBest by vm.isNewBestRun.collectAsStateWithLifecycle()
            RunOverOverlay(
                summary = lastSummary,
                isNewBest = isNewBest,
                onRideAgain = { vm.startRun() },
                onOpenGarage = { activeSheet = ActiveSheet.GARAGE },
                onMenu = { vm.returnToMenu() }
            )
        }

        if (activeSheet == ActiveSheet.NONE) {
            TopUtilityButtons(
                showPause = gameState == GameState.RUNNING || gameState == GameState.PAUSED,
                muted = prefs.muted,
                onPause = {
                    if (vm.gameEngine.state == GameState.PAUSED) vm.resumeGame() else vm.pauseGame()
                },
                onToggleMute = { vm.toggleMute() },
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }

        val navigateFromTab: (PaddockNavTab) -> Unit = { tab ->
            activeSheet = when (tab) {
                PaddockNavTab.HOME -> ActiveSheet.NONE
                PaddockNavTab.GARAGE -> ActiveSheet.GARAGE
                PaddockNavTab.MISSIONS -> ActiveSheet.MISSIONS
                PaddockNavTab.SETTINGS -> ActiveSheet.SETTINGS
            }
        }

        when (activeSheet) {
            ActiveSheet.GARAGE -> GarageSheet(
                prefs = prefs,
                onBuyBike = { vm.buyBike(it) },
                onEquipBike = { vm.equipBike(it) },
                onUpgradeBike = { bike, stat -> vm.upgradeBike(bike, stat) },
                onSelectPaint = { bikeId, paintId -> vm.setBikePaint(bikeId, paintId) },
                onNavigateTab = navigateFromTab,
                onClose = { activeSheet = ActiveSheet.NONE }
            )
            ActiveSheet.MISSIONS -> {
                val topRuns by vm.topRunsState.collectAsStateWithLifecycle()
                MissionsAndRunsSheet(
                    dailyMissions = dailyMissions,
                    milestones = milestones,
                    topRuns = topRuns,
                    usdBalance = prefs.usdBalance,
                    onClaimDailyMission = { vm.claimDailyMission(it) },
                    onClaimMilestone = { vm.claimMilestone(it) },
                    onNavigateTab = navigateFromTab,
                    onClose = { activeSheet = ActiveSheet.NONE }
                )
            }
            ActiveSheet.GPS_MAP -> GpsMapSheet(
                gpsState = gpsState,
                routes = routes,
                activeRouteId = activeRoute.id,
                usdBalance = prefs.usdBalance,
                onRequestGpsAndScan = {
                    if (gpsState.permissionGranted) {
                        vm.scanNearbyHighways()
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                onSelectRoute = { vm.selectRoute(it) },
                onStartRace = {
                    activeSheet = ActiveSheet.NONE
                    vm.startRun()
                },
                onClose = { activeSheet = ActiveSheet.NONE }
            )
            ActiveSheet.SETTINGS -> SettingsSheet(
                prefs = prefs,
                tiltAvailable = tiltAvailable,
                onSelectDifficulty = { vm.setDifficulty(it) },
                onSelectGameMode = { vm.setGameMode(it) },
                onSelectTrafficMode = { vm.setTrafficMode(it) },
                onToggleTilt = { vm.toggleTilt() },
                onToggleMute = { vm.toggleMute() },
                onNavigateTab = navigateFromTab,
                onClose = { activeSheet = ActiveSheet.NONE }
            )
            ActiveSheet.NONE -> Unit
        }
    }
}

@Composable
private fun RaceHudHost(
    vm: NitroRushViewModel,
    bestScore: Int,
    gameMode: GameMode,
    activeRoute: RealRoadTrack,
    liveGpsLocked: Boolean,
    tiltEnabled: Boolean
) {
    val hud by vm.hudState.collectAsStateWithLifecycle()
    val banner by vm.bannerState.collectAsStateWithLifecycle()
    val onTouchInput = remember(vm) {
        { l: Boolean?, r: Boolean?, b: Boolean?, n: Boolean? ->
            vm.setTouchInput(l, r, b, n)
        }
    }

    AdaptiveRacingHudAndControls(
        hud = hud,
        bestScore = bestScore,
        gameMode = gameMode,
        activeRoute = activeRoute,
        liveGpsLocked = liveGpsLocked,
        tiltEnabled = tiltEnabled,
        banner = banner,
        onTouchInput = onTouchInput
    )
}
