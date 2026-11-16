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
import com.example.model.GameState
import com.example.sensor.TiltSteeringSensor
import com.example.ui.NitroRushViewModel
import com.example.ui.components.AdaptiveRacingHudAndControls
import com.example.ui.components.GarageSheet
import com.example.ui.components.GpsMapSheet
import com.example.ui.components.MainMenuOverlay
import com.example.ui.components.MissionsAndRunsSheet
import com.example.ui.components.PauseOverlay
import com.example.ui.components.RunOverOverlay
import com.example.ui.components.TopUtilityButtons
import com.example.ui.theme.NitroRushTheme
import com.example.ui.theme.ObsidianBg

private enum class ActiveSheet {
    NONE,
    GARAGE,
    MISSIONS,
    GPS_MAP
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

    val hud by vm.hudState.collectAsStateWithLifecycle()
    val prefs by vm.prefsState.collectAsStateWithLifecycle()
    val routes by vm.routesState.collectAsStateWithLifecycle()
    val topRuns by vm.topRunsState.collectAsStateWithLifecycle()
    val gpsState by vm.gpsState.collectAsStateWithLifecycle()
    val banner by vm.bannerState.collectAsStateWithLifecycle()
    val lastSummary by vm.lastRunSummary.collectAsStateWithLifecycle()
    val isNewBest by vm.isNewBestRun.collectAsStateWithLifecycle()

    var activeSheet by remember { mutableStateOf(ActiveSheet.NONE) }
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

    // Enable Tilt sensor only when actively racing with Tilt enabled
    LaunchedEffect(prefs.tiltEnabled, hud.gameState) {
        if (prefs.tiltEnabled && hud.gameState == GameState.RUNNING) {
            tiltSensor.start()
        } else {
            tiltSensor.stop()
        }
    }

    // Fixed-timestep 60 FPS game loop driven by Compose frame clock
    var frameTick by remember { mutableLongStateOf(0L) }
    var lastDt by remember { mutableStateOf(GameEngine.STEP) }
    LaunchedEffect(Unit) {
        var lastNs = 0L
        var accumulator = 0f
        while (true) {
            withFrameNanos { nowNs ->
                if (lastNs == 0L) lastNs = nowNs
                val dt = ((nowNs - lastNs) / 1_000_000_000f).coerceIn(0f, 0.1f)
                lastNs = nowNs
                lastDt = dt

                if (hud.gameState != GameState.PAUSED) {
                    accumulator += dt
                    var steps = 0
                    while (accumulator >= GameEngine.STEP && steps < 4) {
                        vm.stepSimulation(GameEngine.STEP, tiltSensor.steerValue)
                        accumulator -= GameEngine.STEP
                        steps++
                    }
                    if (steps == 4) accumulator = 0f
                }
                frameTick = nowNs
            }
        }
    }

    val activeRoute = routes.firstOrNull { it.id == prefs.activeRouteId } ?: routes.first()
    val dailyMissions = vm.currentDailyMissions()
    val milestones = vm.currentMilestones()
    val claimableCount = dailyMissions.count { it.isComplete && !it.claimed } +
        milestones.count { it.isComplete && !it.claimed }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
    ) {
        // Pseudo-3D Highway Canvas (always live behind menus for attract mode)
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Read frameTick to trigger deterministic 60 FPS redraws
            @Suppress("UNUSED_EXPRESSION")
            frameTick
            renderer.render(this, vm.gameEngine, lastDt)
        }

        val showRaceHud = hud.gameState == GameState.COUNTDOWN ||
            hud.gameState == GameState.RUNNING ||
            hud.gameState == GameState.DYING ||
            hud.gameState == GameState.PAUSED

        LaunchedEffect(activeSheet, showRaceHud) {
            if (activeSheet != ActiveSheet.NONE || !showRaceHud) {
                vm.clearTouchInput()
            }
        }

        if (showRaceHud) {
            AdaptiveRacingHudAndControls(
                hud = hud,
                bestScore = prefs.bestScore,
                gameMode = prefs.gameMode,
                activeRoute = activeRoute,
                liveGpsLocked = gpsState.liveGpsActive,
                tiltEnabled = prefs.tiltEnabled,
                banner = banner,
                onTouchInput = { l, r, b, n -> vm.setTouchInput(l, r, b, n) }
            )
        }

        if (hud.gameState == GameState.MENU && activeSheet == ActiveSheet.NONE) {
            MainMenuOverlay(
                prefs = prefs,
                activeRoute = activeRoute,
                claimableMissionCount = claimableCount,
                tiltAvailable = tiltAvailable,
                onSelectDifficulty = { vm.setDifficulty(it) },
                onSelectGameMode = { vm.setGameMode(it) },
                onSelectTrafficMode = { vm.setTrafficMode(it) },
                onToggleTilt = { vm.toggleTilt() },
                onOpenGarage = { activeSheet = ActiveSheet.GARAGE },
                onOpenMissions = { activeSheet = ActiveSheet.MISSIONS },
                onOpenGpsMap = { activeSheet = ActiveSheet.GPS_MAP },
                onStartRide = { vm.startRun() }
            )
        }

        if (hud.gameState == GameState.PAUSED) {
            PauseOverlay(
                tiltEnabled = prefs.tiltEnabled,
                tiltAvailable = tiltAvailable,
                onToggleTilt = { vm.toggleTilt() },
                onResume = { vm.resumeGame() },
                onRestart = { vm.startRun() },
                onMenu = { vm.returnToMenu() }
            )
        }

        if (hud.gameState == GameState.OVER && activeSheet == ActiveSheet.NONE) {
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
                showPause = hud.gameState == GameState.RUNNING || hud.gameState == GameState.PAUSED,
                muted = prefs.muted,
                onPause = {
                    if (hud.gameState == GameState.PAUSED) vm.resumeGame() else vm.pauseGame()
                },
                onToggleMute = { vm.toggleMute() },
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }

        when (activeSheet) {
            ActiveSheet.GARAGE -> GarageSheet(
                prefs = prefs,
                onBuyBike = { vm.buyBike(it) },
                onEquipBike = { vm.equipBike(it) },
                onUpgradeBike = { bike, stat -> vm.upgradeBike(bike, stat) },
                onClose = { activeSheet = ActiveSheet.NONE }
            )
            ActiveSheet.MISSIONS -> MissionsAndRunsSheet(
                dailyMissions = dailyMissions,
                milestones = milestones,
                topRuns = topRuns,
                onClaimDailyMission = { vm.claimDailyMission(it) },
                onClaimMilestone = { vm.claimMilestone(it) },
                onClose = { activeSheet = ActiveSheet.NONE }
            )
            ActiveSheet.GPS_MAP -> GpsMapSheet(
                gpsState = gpsState,
                routes = routes,
                activeRouteId = activeRoute.id,
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
                onClose = { activeSheet = ActiveSheet.NONE }
            )
            ActiveSheet.NONE -> Unit
        }
    }
}
