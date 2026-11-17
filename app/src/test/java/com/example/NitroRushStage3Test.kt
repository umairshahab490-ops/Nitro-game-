package com.example

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.up
import androidx.test.core.app.ApplicationProvider
import com.example.model.GameState
import com.example.ui.NitroRushViewModel
import com.example.ui.theme.NitroRushTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NitroRushStage3Test {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun startActiveRaceAndAssertRunning(vm: NitroRushViewModel) {
        composeTestRule.onNodeWithTag("start_ride_button").performClick()
        repeat(210) { composeTestRule.mainClock.advanceTimeByFrame() }
        assertEquals(GameState.RUNNING, vm.gameEngine.state)
    }

    private fun verifyLandscapeHudGeometryAndClearRoadCorridor() {
        val rootBounds = composeTestRule.onNodeWithTag("landscape_hud_root").fetchSemanticsNode().boundsInRoot
        val telemetryBounds = composeTestRule.onNodeWithTag("landscape_telemetry_panel").fetchSemanticsNode().boundsInRoot
        val missionBounds = composeTestRule.onNodeWithTag("landscape_mission_pill").fetchSemanticsNode().boundsInRoot
        val timerBounds = composeTestRule.onNodeWithTag("landscape_timer_panel").fetchSemanticsNode().boundsInRoot
        val miniMapBounds = composeTestRule.onNodeWithTag("landscape_minimap").fetchSemanticsNode().boundsInRoot
        val speedBounds = composeTestRule.onNodeWithTag("landscape_speed_panel").fetchSemanticsNode().boundsInRoot
        val leftBounds = composeTestRule.onNodeWithTag("pad_left").fetchSemanticsNode().boundsInRoot
        val rightBounds = composeTestRule.onNodeWithTag("pad_right").fetchSemanticsNode().boundsInRoot
        val brakeBounds = composeTestRule.onNodeWithTag("pad_brake").fetchSemanticsNode().boundsInRoot
        val nitroBounds = composeTestRule.onNodeWithTag("pad_nitro").fetchSemanticsNode().boundsInRoot
        val pauseBounds = composeTestRule.onNodeWithTag("pause_button").fetchSemanticsNode().boundsInRoot
        val muteBounds = composeTestRule.onNodeWithTag("mute_button").fetchSemanticsNode().boundsInRoot

        val w = rootBounds.width
        val h = rootBounds.height
        assertTrue("Expected landscape dimensions, got ${w}x${h}", w > h)

        // All interactive controls must satisfy >= 48dp minimum touch target size.
        val minTouchPx = 47.9f * composeTestRule.density.density
        listOf(leftBounds, rightBounds, brakeBounds, nitroBounds, pauseBounds, muteBounds).forEach { b ->
            assertTrue("Touch width ${b.width} below 48dp ($minTouchPx)", b.width >= minTouchPx)
            assertTrue("Touch height ${b.height} below 48dp ($minTouchPx)", b.height >= minTouchPx)
        }

        // Left steering deck ergonomic side & ordering
        assertTrue(leftBounds.right < rightBounds.left)
        assertTrue("Right steer pad intrudes into center corridor", rightBounds.right < w * 0.44f)

        // Right cockpit speedometer + Brake/Nitro pedals ergonomic side & vertical stacking
        assertTrue("Brake pad intrudes into center corridor", brakeBounds.left > w * 0.56f)
        assertTrue(brakeBounds.right < nitroBounds.left)
        assertTrue("Speed panel must sit above action pads", speedBounds.bottom <= brakeBounds.top + 1f)
        assertTrue("Speed panel intrudes into center corridor", speedBounds.left > w * 0.56f)
        assertTrue("Minimap must stay cleanly above speed panel", miniMapBounds.bottom < speedBounds.top)
        assertTrue("Utility buttons must stay above minimap", pauseBounds.bottom <= miniMapBounds.top + 1f)

        // Top telemetry & mission stay left of top-center timer; timer stays left of pause/mute
        assertTrue(telemetryBounds.bottom <= missionBounds.top + 1f)
        assertTrue(telemetryBounds.right < timerBounds.left)
        assertTrue(missionBounds.right < timerBounds.left)
        assertTrue(timerBounds.right < pauseBounds.left)

        // Top panels & mission stay in the upper sky/horizon zone, above bottom controls
        assertTrue(missionBounds.bottom < leftBounds.top)
        assertTrue("Timer overlaps central road area", timerBounds.bottom < h * 0.30f)

        // Central road + player motorcycle corridor [0.40w..0.60w, 0.35h..h] remains completely unobstructed
        val centerRoadLeft = w * 0.40f
        val centerRoadRight = w * 0.60f
        val centerRoadTop = h * 0.35f
        val allHudNodes = listOf(
            telemetryBounds, missionBounds, timerBounds, miniMapBounds,
            speedBounds, leftBounds, rightBounds, brakeBounds, nitroBounds
        )
        allHudNodes.forEach { b ->
            val overlapsHorizontal = b.right > centerRoadLeft && b.left < centerRoadRight
            val overlapsVertical = b.bottom > centerRoadTop && b.top < h
            assertFalse(
                "HUD element $b overlaps central road/motorcycle zone [$centerRoadLeft..$centerRoadRight, $centerRoadTop..$h]",
                overlapsHorizontal && overlapsVertical
            )
        }
    }

    @Test
    @Config(sdk = [36], qualifiers = "w800dp-h400dp-land")
    fun landscapeHud_standardAspectRatioRendersCleanlyWithoutOverlappingRoadOrBike() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = NitroRushViewModel(app)

        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            NitroRushTheme {
                NitroRushApp(vm = vm)
            }
        }
        repeat(3) { composeTestRule.mainClock.advanceTimeByFrame() }

        startActiveRaceAndAssertRunning(vm)

        listOf(
            "landscape_hud_root", "landscape_telemetry_panel", "landscape_mission_pill",
            "landscape_timer_panel", "landscape_minimap", "landscape_speed_panel",
            "pad_left", "pad_right", "pad_brake", "pad_nitro", "pause_button", "mute_button"
        ).forEach { tag ->
            composeTestRule.onNodeWithTag(tag).assertIsDisplayed()
        }

        verifyLandscapeHudGeometryAndClearRoadCorridor()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w640dp-h360dp-land")
    fun landscapeHud_compactAspectRatioKeepsAllControlsAndPanelsUnobstructed() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = NitroRushViewModel(app)

        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            NitroRushTheme {
                NitroRushApp(vm = vm)
            }
        }
        repeat(3) { composeTestRule.mainClock.advanceTimeByFrame() }

        startActiveRaceAndAssertRunning(vm)
        verifyLandscapeHudGeometryAndClearRoadCorridor()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w960dp-h540dp-land")
    fun landscapeHud_wideTabletAspectRatioKeepsControlsAccessibleAndMissionLive() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = NitroRushViewModel(app)

        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            NitroRushTheme {
                NitroRushApp(vm = vm)
            }
        }
        repeat(3) { composeTestRule.mainClock.advanceTimeByFrame() }

        startActiveRaceAndAssertRunning(vm)
        verifyLandscapeHudGeometryAndClearRoadCorridor()

        // Drive live mission metrics upward and confirm mission pill reflects completion
        vm.gameEngine.nearMisses = 25
        vm.gameEngine.draftSecondsAccum = 25f
        vm.gameEngine.twoWayMeters = 3500f
        vm.gameEngine.pickupsCollected = 15
        vm.gameEngine.topSpeedKmh = 310f
        repeat(4) { composeTestRule.mainClock.advanceTimeByFrame() }

        assertTrue(vm.hudState.value.activeMission?.progress ?: 0 > 0)
        composeTestRule.onNodeWithTag("landscape_mission_pill").assertIsDisplayed()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w800dp-h400dp-land")
    fun landscapeControls_touchDetectionMultiTouchAndPauseResetWorkReliably() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = NitroRushViewModel(app)

        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            NitroRushTheme {
                NitroRushApp(vm = vm)
            }
        }
        repeat(3) { composeTestRule.mainClock.advanceTimeByFrame() }

        startActiveRaceAndAssertRunning(vm)
        vm.gameEngine.nitro = 80f

        // Left steering press & release
        composeTestRule.onNodeWithTag("pad_left").performTouchInput { down(center) }
        repeat(2) { composeTestRule.mainClock.advanceTimeByFrame() }
        assertTrue(vm.gameEngine.touchLeft)
        composeTestRule.onNodeWithTag("pad_left").performTouchInput { up() }
        repeat(2) { composeTestRule.mainClock.advanceTimeByFrame() }
        assertFalse(vm.gameEngine.touchLeft)

        // Right steering press & release
        composeTestRule.onNodeWithTag("pad_right").performTouchInput { down(center) }
        repeat(2) { composeTestRule.mainClock.advanceTimeByFrame() }
        assertTrue(vm.gameEngine.touchRight)
        composeTestRule.onNodeWithTag("pad_right").performTouchInput { up() }
        repeat(2) { composeTestRule.mainClock.advanceTimeByFrame() }
        assertFalse(vm.gameEngine.touchRight)

        // Brake press & release
        composeTestRule.onNodeWithTag("pad_brake").performTouchInput { down(center) }
        repeat(2) { composeTestRule.mainClock.advanceTimeByFrame() }
        assertTrue(vm.gameEngine.touchBrake)
        composeTestRule.onNodeWithTag("pad_brake").performTouchInput { up() }
        repeat(2) { composeTestRule.mainClock.advanceTimeByFrame() }
        assertFalse(vm.gameEngine.touchBrake)

        // Simultaneous left steer (left thumb, pointer 1) + nitro (right thumb, pointer 2)
        composeTestRule.onNodeWithTag("pad_left").performTouchInput { down(1, center) }
        composeTestRule.onNodeWithTag("pad_nitro").performTouchInput { down(2, center) }
        repeat(2) { composeTestRule.mainClock.advanceTimeByFrame() }
        assertTrue(vm.gameEngine.touchLeft)
        assertTrue(vm.gameEngine.touchNitro)

        // Pausing while multi-touch inputs are held clears all active touch flags and shows PauseOverlay
        composeTestRule.onNodeWithTag("pause_button").performClick()
        repeat(3) { composeTestRule.mainClock.advanceTimeByFrame() }
        assertEquals(GameState.PAUSED, vm.gameEngine.state)
        assertFalse(vm.gameEngine.touchLeft)
        assertFalse(vm.gameEngine.touchNitro)
        composeTestRule.onNodeWithTag("resume_button").assertIsDisplayed()

        // Resuming returns cleanly to active race without stuck inputs
        composeTestRule.onNodeWithTag("resume_button").performClick()
        repeat(3) { composeTestRule.mainClock.advanceTimeByFrame() }
        assertEquals(GameState.RUNNING, vm.gameEngine.state)
        assertFalse(vm.gameEngine.touchLeft)
        assertFalse(vm.gameEngine.touchNitro)
        composeTestRule.onNodeWithTag("landscape_hud_root").assertIsDisplayed()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w411dp-h891dp-port")
    fun portraitHud_regressionCheckPreservesPortraitLayoutAndControls() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = NitroRushViewModel(app)

        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            NitroRushTheme {
                NitroRushApp(vm = vm)
            }
        }
        repeat(3) { composeTestRule.mainClock.advanceTimeByFrame() }

        startActiveRaceAndAssertRunning(vm)

        // Landscape-only HUD root must NOT be mounted in portrait
        composeTestRule.onNodeWithTag("landscape_hud_root").assertDoesNotExist()

        val leftBounds = composeTestRule.onNodeWithTag("pad_left").fetchSemanticsNode().boundsInRoot
        val brakeBounds = composeTestRule.onNodeWithTag("pad_brake").fetchSemanticsNode().boundsInRoot
        val nitroBounds = composeTestRule.onNodeWithTag("pad_nitro").fetchSemanticsNode().boundsInRoot
        val rightBounds = composeTestRule.onNodeWithTag("pad_right").fetchSemanticsNode().boundsInRoot

        // Portrait keeps the 4-column bottom control deck: [LEFT] [BRAKE] [NITRO] [RIGHT]
        assertTrue(leftBounds.right <= brakeBounds.left + 1f)
        assertTrue(brakeBounds.right <= nitroBounds.left + 1f)
        assertTrue(nitroBounds.right <= rightBounds.left + 1f)

        composeTestRule.onNodeWithTag("pad_left").performTouchInput { down(center) }
        repeat(2) { composeTestRule.mainClock.advanceTimeByFrame() }
        assertTrue(vm.gameEngine.touchLeft)
        composeTestRule.onNodeWithTag("pad_left").performTouchInput { up() }
        repeat(2) { composeTestRule.mainClock.advanceTimeByFrame() }
        assertFalse(vm.gameEngine.touchLeft)

        composeTestRule.onNodeWithTag("pause_button").performClick()
        repeat(3) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.onNodeWithTag("resume_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pause_menu_button").performClick()
        repeat(3) { composeTestRule.mainClock.advanceTimeByFrame() }
        assertEquals(GameState.MENU, vm.gameEngine.state)
        composeTestRule.onNodeWithTag("start_ride_button").assertIsDisplayed()
    }
}
