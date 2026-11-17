package com.example

import android.app.Application
import android.os.Looper
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.game.GameEngine
import com.example.model.BikeCatalog
import com.example.model.Difficulty
import com.example.model.GameMode
import com.example.model.GameState
import com.example.model.TrafficMode
import com.example.ui.NitroRushViewModel
import com.example.ui.theme.NitroRushTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NitroRushStage4Test {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun waitUntilWithMainLooper(timeoutMs: Long = 5000L, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            shadowOf(Looper.getMainLooper()).idle()
            if (condition()) return
            Thread.sleep(15)
        }
        shadowOf(Looper.getMainLooper()).idle()
        assertTrue("Timed out after ${timeoutMs}ms waiting for async state condition", condition())
    }

    @Test
    @Config(sdk = [36], qualifiers = "w411dp-h891dp-port")
    fun mainMenuAndSettings_portrait_displaysControlsWithoutScrollAndUpdatesConfig() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = NitroRushViewModel(app)

        composeTestRule.setContent {
            NitroRushTheme {
                NitroRushApp(vm = vm)
            }
        }

        // Portrait Main Menu shows PLAY NOW + 2x2 grid + top settings gear immediately above the fold
        listOf(
            "start_ride_button",
            "open_garage",
            "open_missions",
            "open_map",
            "open_settings",
            "open_settings_gear"
        ).forEach { tag ->
            composeTestRule.onNodeWithTag(tag).assertIsDisplayed()
        }

        // Open Settings modal and update Difficulty / Mode / Traffic
        composeTestRule.onNodeWithTag("open_settings").performClick()
        composeTestRule.onNodeWithTag("settings_back").assertIsDisplayed()
        composeTestRule.onNodeWithTag("diff_INSANE").performClick()
        composeTestRule.onNodeWithTag("mode_ENDLESS").performClick()
        composeTestRule.onNodeWithTag("traffic_TWO_WAY").performClick()

        waitUntilWithMainLooper {
            vm.prefsState.value.difficulty == Difficulty.INSANE &&
                vm.prefsState.value.gameMode == GameMode.ENDLESS &&
                vm.prefsState.value.trafficMode == TrafficMode.TWO_WAY
        }
        assertEquals(Difficulty.INSANE, vm.prefsState.value.difficulty)
        assertEquals(GameMode.ENDLESS, vm.prefsState.value.gameMode)
        assertEquals(TrafficMode.TWO_WAY, vm.prefsState.value.trafficMode)

        // Restore default mode/difficulty so shared DataStore singleton stays clean for sibling tests
        composeTestRule.onNodeWithTag("diff_PRO").performClick()
        composeTestRule.onNodeWithTag("mode_TIME_ATTACK").performClick()
        composeTestRule.onNodeWithTag("traffic_ONE_WAY").performClick()
        waitUntilWithMainLooper {
            vm.prefsState.value.difficulty == Difficulty.PRO &&
                vm.prefsState.value.gameMode == GameMode.TIME_ATTACK &&
                vm.prefsState.value.trafficMode == TrafficMode.ONE_WAY
        }

        composeTestRule.onNodeWithTag("settings_done_button").performClick()
        composeTestRule.onNodeWithTag("start_ride_button").assertIsDisplayed()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w640dp-h360dp-land")
    fun mainMenu_compactLandscape_keepsPlayAndNavGridVisibleAboveFold() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = NitroRushViewModel(app)

        composeTestRule.setContent {
            NitroRushTheme {
                NitroRushApp(vm = vm)
            }
        }

        val playBounds = composeTestRule.onNodeWithTag("start_ride_button").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val garageBounds = composeTestRule.onNodeWithTag("open_garage").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val missionsBounds = composeTestRule.onNodeWithTag("open_missions").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val mapBounds = composeTestRule.onNodeWithTag("open_map").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val settingsBounds = composeTestRule.onNodeWithTag("open_settings").assertIsDisplayed().fetchSemanticsNode().boundsInRoot

        // In the compact landscape right-column deck, PLAY NOW sits cleanly above the 2x2 paddock grid
        assertTrue(playBounds.bottom <= garageBounds.top + 2f)
        assertTrue(garageBounds.right < missionsBounds.left)
        assertTrue(mapBounds.right < settingsBounds.left)
        assertTrue(garageBounds.bottom < mapBounds.top)
    }

    @Test
    @Config(sdk = [36], qualifiers = "w800dp-h400dp-land")
    fun garageUpgradesAndPaint_landscape_performsUpgradeAndPersistsCustomPaint() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = NitroRushViewModel(app)

        composeTestRule.setContent {
            NitroRushTheme {
                NitroRushApp(vm = vm)
            }
        }

        composeTestRule.onNodeWithTag("open_garage").performClick()
        composeTestRule.onNodeWithTag("garage_back").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tab_bikes").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tab_upgrades").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tab_paint").assertIsDisplayed()

        // Switch to UPGRADES tab and perform real engine upgrade
        composeTestRule.onNodeWithTag("tab_upgrades").performClick()
        val starterBike = BikeCatalog.bikes.first()
        val initialLevel = vm.prefsState.value.bikeUpgrades[starterBike.id]?.engineLevel ?: 1
        val startingUsd = vm.prefsState.value.usdBalance
        val expectedCost = BikeCatalog.upgradeCostUsd(starterBike, initialLevel)
        assertTrue(startingUsd >= expectedCost)

        composeTestRule.onNodeWithTag("upgrade_ENGINE").performClick()
        waitUntilWithMainLooper {
            (vm.prefsState.value.bikeUpgrades[starterBike.id]?.engineLevel ?: 1) == initialLevel + 1
        }
        assertEquals(initialLevel + 1, vm.prefsState.value.bikeUpgrades[starterBike.id]?.engineLevel)
        assertEquals(startingUsd - expectedCost, vm.prefsState.value.usdBalance)

        // Switch to PAINT tab and select Crimson Track livery
        composeTestRule.onNodeWithTag("tab_paint").performClick()
        composeTestRule.onNodeWithTag("paint_crimson_track").performClick()
        waitUntilWithMainLooper {
            vm.prefsState.value.bikePaintIds[starterBike.id] == "crimson_track"
        }
        assertEquals("crimson_track", vm.prefsState.value.bikePaintIds[starterBike.id])

        // Cycle to next bike in carousel without losing equipped bike
        composeTestRule.onNodeWithTag("garage_next_bike").performClick()
        composeTestRule.onNodeWithTag("selected_bike_title").assertTextEquals("CBR650R")

        composeTestRule.onNodeWithTag("garage_back").performClick()
        composeTestRule.onNodeWithTag("start_ride_button").assertIsDisplayed()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w800dp-h400dp-land")
    fun missionsAndRuns_landscape_displaysCareerProgressRingAndSwitchesTabs() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = NitroRushViewModel(app)

        composeTestRule.setContent {
            NitroRushTheme {
                NitroRushApp(vm = vm)
            }
        }

        composeTestRule.onNodeWithTag("open_missions").performClick()
        composeTestRule.onNodeWithTag("missions_back").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tab_missions").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tab_best_runs").assertIsDisplayed()

        val dailyBounds = composeTestRule.onNodeWithText("DAILY MISSIONS").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val progressBounds = composeTestRule.onNodeWithTag("career_progress_panel").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertTrue("Missions list should sit left of Career Progress panel in landscape", dailyBounds.right < progressBounds.left)

        composeTestRule.onNodeWithTag("tab_best_runs").performClick()
        composeTestRule.onNodeWithText("NO RUNS RECORDED YET").assertIsDisplayed()

        composeTestRule.onNodeWithTag("missions_back").performClick()
        composeTestRule.onNodeWithTag("start_ride_button").assertIsDisplayed()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w800dp-h400dp-land")
    fun gpsMap_landscape_selectsHighwayCourseExposesControlsAndStartsRace() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = NitroRushViewModel(app)

        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            NitroRushTheme {
                NitroRushApp(vm = vm)
            }
        }
        repeat(3) { composeTestRule.mainClock.advanceTimeByFrame() }

        composeTestRule.onNodeWithTag("open_map").performClick()
        repeat(2) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.onNodeWithTag("map_back").assertIsDisplayed()
        composeTestRule.onNodeWithTag("scan_gps_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("map_zoom_in").assertIsDisplayed()
        composeTestRule.onNodeWithTag("map_zoom_out").assertIsDisplayed()
        composeTestRule.onNodeWithTag("map_recenter").assertIsDisplayed()
        composeTestRule.onNodeWithTag("map_start_race").assertIsDisplayed()

        composeTestRule.onNodeWithTag("map_zoom_in").performClick()
        composeTestRule.onNodeWithTag("map_recenter").performClick()

        composeTestRule.onNodeWithTag("route_preset_wangan").performClick()
        waitUntilWithMainLooper {
            vm.prefsState.value.activeRouteId == "preset_wangan"
        }
        assertEquals("preset_wangan", vm.prefsState.value.activeRouteId)

        // Start race directly from GPS map START RACE CTA
        composeTestRule.onNodeWithTag("map_start_race").performClick()
        repeat(210) { composeTestRule.mainClock.advanceTimeByFrame() }
        assertEquals(GameState.RUNNING, vm.gameEngine.state)
        composeTestRule.onNodeWithTag("landscape_hud_root").assertIsDisplayed()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w800dp-h400dp-land")
    fun postRaceAndPauseOverlays_landscape_renderResultsAndActionsCleanly() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = NitroRushViewModel(app)
        vm.setGameMode(GameMode.TIME_ATTACK)
        waitUntilWithMainLooper { vm.gameEngine.gameMode == GameMode.TIME_ATTACK }

        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            NitroRushTheme {
                NitroRushApp(vm = vm)
            }
        }
        repeat(3) { composeTestRule.mainClock.advanceTimeByFrame() }

        // Start race -> advance into RUNNING -> pause -> verify PauseOverlay actions
        composeTestRule.onNodeWithTag("start_ride_button").performClick()
        repeat(210) { composeTestRule.mainClock.advanceTimeByFrame() }
        assertEquals(GameState.RUNNING, vm.gameEngine.state)

        composeTestRule.onNodeWithTag("pause_button").performClick()
        repeat(3) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.onNodeWithTag("resume_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("restart_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pause_menu_button").assertIsDisplayed()

        composeTestRule.onNodeWithTag("resume_button").performClick()
        repeat(5) { composeTestRule.mainClock.advanceTimeByFrame() }
        assertEquals(GameState.RUNNING, vm.gameEngine.state)

        // Expire timer to trigger Game Over cleanly and verify RunOverOverlay actions
        vm.gameEngine.timeLeft = 0.01f
        repeat(10) { vm.stepSimulation(GameEngine.STEP, 0f) }
        repeat(4) { composeTestRule.mainClock.advanceTimeByFrame() }
        assertEquals(GameState.OVER, vm.gameEngine.state)

        composeTestRule.onNodeWithTag("ride_again_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("over_garage_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("over_menu_button").assertIsDisplayed()

        composeTestRule.onNodeWithTag("over_garage_button").performClick()
        repeat(3) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.onNodeWithTag("garage_back").assertIsDisplayed()

        composeTestRule.onNodeWithTag("garage_back").performClick()
        repeat(3) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.onNodeWithTag("over_menu_button").performClick()
        repeat(3) { composeTestRule.mainClock.advanceTimeByFrame() }
        assertEquals(GameState.MENU, vm.gameEngine.state)
        composeTestRule.onNodeWithTag("start_ride_button").assertIsDisplayed()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w411dp-h891dp-port")
    fun portraitBottomNavAndSheets_verifyGarageMissionsSettingsAndGps() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = NitroRushViewModel(app)

        composeTestRule.setContent {
            NitroRushTheme {
                NitroRushApp(vm = vm)
            }
        }

        // Portrait Garage + bottom nav switching to Missions & Settings
        composeTestRule.onNodeWithTag("open_garage").performClick()
        composeTestRule.onNodeWithTag("garage_back").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_missions").performClick()
        composeTestRule.onNodeWithTag("missions_back").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tab_best_runs").performClick()
        composeTestRule.onNodeWithText("NO RUNS RECORDED YET").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_settings").performClick()
        composeTestRule.onNodeWithTag("settings_back").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_home").performClick()
        composeTestRule.onNodeWithTag("start_ride_button").assertIsDisplayed()

        // Portrait GPS Map
        composeTestRule.onNodeWithTag("open_map").performClick()
        composeTestRule.onNodeWithTag("map_back").assertIsDisplayed()
        composeTestRule.onNodeWithTag("scan_gps_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("map_zoom_in").assertIsDisplayed()
        composeTestRule.onNodeWithTag("map_start_race").assertIsDisplayed()
        composeTestRule.onNodeWithTag("route_preset_autobahn").performClick()
        waitUntilWithMainLooper {
            vm.prefsState.value.activeRouteId == "preset_autobahn"
        }
        composeTestRule.onNodeWithTag("map_back").performClick()
        composeTestRule.onNodeWithTag("start_ride_button").assertIsDisplayed()
    }
}
