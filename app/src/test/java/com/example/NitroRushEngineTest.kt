package com.example

import com.example.audio.SoundEffect
import com.example.game.GameEngine
import com.example.game.GameEventCallbacks
import com.example.game.RunSummary
import com.example.location.TrackGeometryConverter
import com.example.model.BikeCatalog
import com.example.model.BikeUpgradeState
import com.example.model.Difficulty
import com.example.model.GameMode
import com.example.model.GameState
import com.example.model.GeoWaypoint
import com.example.model.PresetWorldRoutes
import com.example.model.TrafficMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NitroRushEngineTest {

    @Test
    fun bikeCatalog_hasVerifiedMsrpsAndScalingUpgrades() {
        val starter = BikeCatalog.findById("kawasaki_ninja_500")
        assertEquals(5499, starter.msrpUsd)

        val flagship = BikeCatalog.findById("ducati_panigale_v4s")
        assertEquals(33510, flagship.msrpUsd)

        val base = BikeUpgradeState()
        val maxed = BikeUpgradeState(engineLevel = 5, tiresLevel = 5, nitroLevel = 5)
        assertTrue(
            BikeCatalog.effectiveTopSpeedMult(starter, maxed) >
                BikeCatalog.effectiveTopSpeedMult(starter, base)
        )
        assertTrue(BikeCatalog.upgradeCostUsd(flagship, 1) > BikeCatalog.upgradeCostUsd(starter, 1))
    }

    @Test
    fun trackGeometryConverter_computesCurvatureFromGpsWaypoints() {
        val wps = listOf(
            GeoWaypoint(37.7700, -122.4200),
            GeoWaypoint(37.7750, -122.4200),
            GeoWaypoint(37.7790, -122.4150),
            GeoWaypoint(37.7820, -122.4090)
        )
        val track = TrackGeometryConverter.buildTrackFromWaypoints(
            id = "test_way",
            roadName = "Test Freeway",
            regionLabel = "San Francisco",
            roadClass = "Motorway",
            waypoints = wps,
            isGpsDerived = true
        )
        assertTrue(track.lengthMeters > 1000)
        assertEquals(wps.size, track.curvatureProfile.size)
        assertTrue(track.curvatureProfile[1] != 0f)
    }

    @Test
    fun gameEngine_runsCountdownToActiveAndCompletesTimeUpPayout() {
        var finishedSummary: RunSummary? = null
        val engine = GameEngine(object : GameEventCallbacks {
            override fun onSound(sfx: SoundEffect) = Unit
            override fun onHaptic(durationMs: Long, amplitude: Int) = Unit
            override fun onBanner(text: String, colorArgb: Int) = Unit
            override fun onRunFinished(summary: RunSummary) {
                finishedSummary = summary
            }
        })

        engine.applyConfig(
            difficulty = Difficulty.INSANE,
            gameMode = GameMode.TIME_ATTACK,
            trafficMode = TrafficMode.TWO_WAY,
            bike = BikeCatalog.findById("kawasaki_ninja_500"),
            upgrades = BikeUpgradeState(),
            route = PresetWorldRoutes.presets.first(),
            tiltEnabled = false
        )

        engine.startNewRun()
        assertEquals(GameState.COUNTDOWN, engine.state)

        // Advance past 2.7s countdown
        repeat(180) { engine.step(GameEngine.STEP) }
        assertEquals(GameState.RUNNING, engine.state)

        // Advance until run finishes via TIME UP or WRECKED (incl. DYING tumble)
        repeat(5000) {
            if (engine.state == GameState.RUNNING || engine.state == GameState.DYING) {
                engine.step(GameEngine.STEP)
            }
        }
        assertEquals(GameState.OVER, engine.state)
        assertNotNull(finishedSummary)
        assertTrue(finishedSummary?.endReason == "TIME UP" || finishedSummary?.endReason == "WRECKED")
        assertTrue((finishedSummary?.usdEarned ?: 0) > 0)
        assertTrue((finishedSummary?.distanceMeters ?: 0) > 0)
    }
}
