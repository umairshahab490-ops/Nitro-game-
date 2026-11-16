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

        val distAtOver = engine.distanceMeters
        repeat(60) { engine.step(GameEngine.STEP) }
        assertEquals(distAtOver, engine.distanceMeters, 0.001f)
    }

    @Test
    fun gameEngine_rpmAndGearScaleWithBikeMaxSpeedWithoutWrappingOnNitro() {
        val engine = GameEngine(object : GameEventCallbacks {
            override fun onSound(sfx: SoundEffect) = Unit
            override fun onHaptic(durationMs: Long, amplitude: Int) = Unit
            override fun onBanner(text: String, colorArgb: Int) = Unit
            override fun onRunFinished(summary: RunSummary) = Unit
        })
        val flagship = BikeCatalog.findById("ducati_panigale_v4s")
        val maxed = BikeUpgradeState(engineLevel = 5, tiresLevel = 5, nitroLevel = 5)
        engine.applyConfig(
            difficulty = Difficulty.PRO,
            gameMode = GameMode.ENDLESS,
            trafficMode = TrafficMode.ONE_WAY,
            bike = flagship,
            upgrades = maxed,
            route = PresetWorldRoutes.presets.first(),
            tiltEnabled = false
        )

        val bikeMax = engine.effectiveBikeMaxSpeed()
        assertTrue(bikeMax > GameEngine.MAXSP * 1.3f)

        // At 50% of upgraded bikeMax, should not have prematurely hit Gear 6
        engine.speed = bikeMax * 0.50f
        assertTrue(engine.currentGear() in 3..4)

        // Across top-gear and Nitro overboost (95%..145% of bikeMax), Gear stays 6 and RPM rises smoothly
        engine.speed = bikeMax * 0.95f
        assertEquals(6, engine.currentGear())
        val rpm95 = engine.currentRpm()

        engine.speed = bikeMax * 1.05f
        assertEquals(6, engine.currentGear())
        val rpm105 = engine.currentRpm()

        engine.speed = bikeMax * 1.35f
        assertEquals(6, engine.currentGear())
        val rpm135 = engine.currentRpm()

        assertTrue(rpm105 > rpm95)
        assertTrue(rpm135 > rpm105)
        assertTrue(rpm105 > 0.90f)
    }

    @Test
    fun gameEngine_pauseAndResetClearAllTouchInputs() {
        val engine = GameEngine(object : GameEventCallbacks {
            override fun onSound(sfx: SoundEffect) = Unit
            override fun onHaptic(durationMs: Long, amplitude: Int) = Unit
            override fun onBanner(text: String, colorArgb: Int) = Unit
            override fun onRunFinished(summary: RunSummary) = Unit
        })
        engine.startNewRun()
        repeat(180) { engine.step(GameEngine.STEP) }
        assertEquals(GameState.RUNNING, engine.state)

        engine.touchLeft = true
        engine.touchRight = true
        engine.touchBrake = true
        engine.touchNitro = true
        engine.tiltSteer = 0.75f

        engine.pause()
        assertEquals(GameState.PAUSED, engine.state)
        assertEquals(false, engine.touchLeft)
        assertEquals(false, engine.touchRight)
        assertEquals(false, engine.touchBrake)
        assertEquals(false, engine.touchNitro)
        assertEquals(0f, engine.tiltSteer, 0.0001f)
    }
}
