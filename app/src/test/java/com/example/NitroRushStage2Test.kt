package com.example

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.game.GameEngine
import com.example.model.GameState
import com.example.model.MissionMetric
import com.example.ui.NitroRushViewModel
import com.example.ui.theme.NitroRushTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NitroRushStage2Test {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun viewModel_exposesLiveMissionProgressFromGameEngineDuringRun() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = NitroRushViewModel(app)

        assertEquals(GameState.MENU, vm.hudState.value.gameState)
        assertTrue(vm.hudState.value.dailyMissions.isNotEmpty())
        assertNotNull(vm.hudState.value.activeMission)

        vm.startRun()
        assertEquals(GameState.COUNTDOWN, vm.hudState.value.gameState)

        repeat(180) { vm.stepSimulation(GameEngine.STEP, 0f) }
        assertEquals(GameState.RUNNING, vm.hudState.value.gameState)

        vm.gameEngine.nearMisses = 7
        vm.gameEngine.draftSecondsAccum = 6.4f
        vm.gameEngine.twoWayMeters = 450f
        vm.gameEngine.pickupsCollected = 4
        vm.gameEngine.topSpeedKmh = 268f

        vm.stepSimulation(GameEngine.STEP, 0f)

        val liveMissions = vm.hudState.value.dailyMissions
        assertEquals(3, liveMissions.size)
        liveMissions.forEach { mission ->
            val expected = when (mission.metric) {
                MissionMetric.NEAR_MISSES -> 7
                MissionMetric.DRAFT_SECONDS -> 6
                MissionMetric.TWO_WAY_METERS -> 450
                MissionMetric.PICKUPS_COLLECTED -> 4
                MissionMetric.TOP_SPEED_KMH -> 268
                MissionMetric.RUNS_COMPLETED -> 0
            }
            assertEquals("Mission ${mission.id} should mirror live engine progress", expected, mission.progress)
        }

        vm.pauseGame()
        assertEquals(GameState.PAUSED, vm.hudState.value.gameState)
        assertEquals(liveMissions, vm.hudState.value.dailyMissions)

        vm.returnToMenu()
        assertEquals(GameState.MENU, vm.hudState.value.gameState)
        vm.hudState.value.dailyMissions.forEach { mission ->
            assertEquals(0, mission.progress)
        }
    }

    @Test
    @Config(sdk = [36], qualifiers = "w411dp-h891dp-port")
    fun composeUi_frameLoopPausesWhenInactiveOrPausedAndResumesGameplay() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = NitroRushViewModel(app)

        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            NitroRushTheme {
                NitroRushApp(vm = vm)
            }
        }
        repeat(3) { composeTestRule.mainClock.advanceTimeByFrame() }

        // 1. On MainMenuOverlay, frame-loop must not advance simulation.
        val menuClockStart = vm.gameEngine.simClockMs
        repeat(30) { composeTestRule.mainClock.advanceTimeByFrame() }
        assertEquals(menuClockStart, vm.gameEngine.simClockMs, 0.001f)

        // 2. Opening and closing blocking sheets keeps frame-loop inactive and returns cleanly.
        composeTestRule.onNodeWithTag("open_garage").performClick()
        repeat(3) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.onNodeWithTag("garage_back").assertIsDisplayed()
        repeat(20) { composeTestRule.mainClock.advanceTimeByFrame() }
        assertEquals(menuClockStart, vm.gameEngine.simClockMs, 0.001f)
        composeTestRule.onNodeWithTag("garage_back").performClick()
        repeat(3) { composeTestRule.mainClock.advanceTimeByFrame() }

        composeTestRule.onNodeWithTag("open_missions").performClick()
        repeat(3) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.onNodeWithTag("missions_back").assertIsDisplayed()
        repeat(20) { composeTestRule.mainClock.advanceTimeByFrame() }
        assertEquals(menuClockStart, vm.gameEngine.simClockMs, 0.001f)
        composeTestRule.onNodeWithTag("missions_back").performClick()
        repeat(3) { composeTestRule.mainClock.advanceTimeByFrame() }

        // 3. Starting a ride activates the 60 FPS loop through COUNTDOWN -> RUNNING.
        composeTestRule.onNodeWithTag("start_ride_button").performClick()
        repeat(220) { composeTestRule.mainClock.advanceTimeByFrame() }
        assertEquals(GameState.RUNNING, vm.gameEngine.state)
        assertTrue(vm.gameEngine.simClockMs > 2700f)
        assertTrue(vm.gameEngine.distanceMeters > 0f)

        // 4. Pausing stops frame-loop simulation updates completely.
        composeTestRule.onNodeWithTag("pause_button").performClick()
        repeat(3) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.onNodeWithTag("resume_button").assertIsDisplayed()
        assertEquals(GameState.PAUSED, vm.gameEngine.state)
        val pausedSimClock = vm.gameEngine.simClockMs
        val pausedDistance = vm.gameEngine.distanceMeters

        repeat(60) { composeTestRule.mainClock.advanceTimeByFrame() }
        assertEquals(pausedSimClock, vm.gameEngine.simClockMs, 0.001f)
        assertEquals(pausedDistance, vm.gameEngine.distanceMeters, 0.001f)

        // 5. Resuming restores active gameplay frame-loop rendering and simulation.
        composeTestRule.onNodeWithTag("resume_button").performClick()
        repeat(60) { composeTestRule.mainClock.advanceTimeByFrame() }
        assertEquals(GameState.RUNNING, vm.gameEngine.state)
        assertTrue(vm.gameEngine.simClockMs > pausedSimClock)
        assertTrue(vm.gameEngine.distanceMeters > pausedDistance)
    }

    @Test
    @Config(sdk = [36], qualifiers = "w800dp-h400dp-land")
    fun composeUi_landscapeHudControlsRemainOperationalThroughPauseAndResume() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = NitroRushViewModel(app)

        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            NitroRushTheme {
                NitroRushApp(vm = vm)
            }
        }
        repeat(3) { composeTestRule.mainClock.advanceTimeByFrame() }

        composeTestRule.onNodeWithTag("start_ride_button").performClick()
        repeat(200) { composeTestRule.mainClock.advanceTimeByFrame() }
        assertEquals(GameState.RUNNING, vm.gameEngine.state)

        composeTestRule.onNodeWithTag("pad_left").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pad_brake").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pad_nitro").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pad_right").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pause_button").assertIsDisplayed()

        composeTestRule.onNodeWithTag("pause_button").performClick()
        repeat(3) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.onNodeWithTag("resume_button").assertIsDisplayed()

        composeTestRule.onNodeWithTag("resume_button").performClick()
        repeat(30) { composeTestRule.mainClock.advanceTimeByFrame() }
        assertEquals(GameState.RUNNING, vm.gameEngine.state)
        composeTestRule.onNodeWithTag("pad_left").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pad_right").assertIsDisplayed()
    }
}
