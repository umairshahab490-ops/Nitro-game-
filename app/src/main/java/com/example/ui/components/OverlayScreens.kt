package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerPreferencesSnapshot
import com.example.game.RunSummary
import com.example.model.BikeCatalog
import com.example.model.BikeUpgradeState
import com.example.model.Difficulty
import com.example.model.GameMode
import com.example.model.RealRoadTrack
import com.example.model.TrafficMode
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BoostCyan
import com.example.ui.theme.CashGreen
import com.example.ui.theme.DangerRed
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeonPink
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import java.util.Locale

@Composable
fun MainMenuOverlay(
    prefs: PlayerPreferencesSnapshot,
    activeRoute: RealRoadTrack,
    claimableMissionCount: Int,
    tiltAvailable: Boolean,
    onSelectDifficulty: (Difficulty) -> Unit,
    onSelectGameMode: (GameMode) -> Unit,
    onSelectTrafficMode: (TrafficMode) -> Unit,
    onToggleTilt: () -> Unit,
    onOpenGarage: () -> Unit,
    onOpenMissions: () -> Unit,
    onOpenGpsMap: () -> Unit,
    onStartRide: () -> Unit
) {
    val equippedBike = BikeCatalog.findById(prefs.equippedBikeId)
    val upgrades = prefs.bikeUpgrades[equippedBike.id] ?: BikeUpgradeState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(ObsidianBg.copy(alpha = 0.62f), ObsidianBg.copy(alpha = 0.90f))
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val isLandscape = maxWidth > maxHeight
        val scroll = rememberScrollState()

        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scroll),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MenuHeaderBrand(prefs, equippedBike.fullName, upgrades.totalUpgradeLevel)
                    ActiveRouteBanner(activeRoute = activeRoute, onClick = onOpenGpsMap)
                    RideCtaButton(onClick = onStartRide)
                }

                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ModeAndDifficultyControls(
                        prefs = prefs,
                        tiltAvailable = tiltAvailable,
                        onSelectDifficulty = onSelectDifficulty,
                        onSelectGameMode = onSelectGameMode,
                        onSelectTrafficMode = onSelectTrafficMode,
                        onToggleTilt = onToggleTilt
                    )
                    FeatureNavRow(
                        claimableMissionCount = claimableMissionCount,
                        onOpenGarage = onOpenGarage,
                        onOpenMissions = onOpenMissions,
                        onOpenGpsMap = onOpenGpsMap
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(modifier = Modifier.height(6.dp))
                MenuHeaderBrand(prefs, equippedBike.fullName, upgrades.totalUpgradeLevel)
                ActiveRouteBanner(activeRoute = activeRoute, onClick = onOpenGpsMap)

                ModeAndDifficultyControls(
                    prefs = prefs,
                    tiltAvailable = tiltAvailable,
                    onSelectDifficulty = onSelectDifficulty,
                    onSelectGameMode = onSelectGameMode,
                    onSelectTrafficMode = onSelectTrafficMode,
                    onToggleTilt = onToggleTilt
                )

                FeatureNavRow(
                    claimableMissionCount = claimableMissionCount,
                    onOpenGarage = onOpenGarage,
                    onOpenMissions = onOpenMissions,
                    onOpenGpsMap = onOpenGpsMap
                )

                RideCtaButton(onClick = onStartRide)

                Text(
                    text = "NEAR-MISS FOR COMBO · DRAFT FOR CASH · COLLECT $ & SHIELD",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun MenuHeaderBrand(
    prefs: PlayerPreferencesSnapshot,
    bikeName: String,
    upgradeLevelSum: Int
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "NITRO\nRUSH",
            style = TextStyle(
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                fontSize = 42.sp,
                lineHeight = 40.sp,
                brush = Brush.horizontalGradient(listOf(NeonPink, BoostCyan))
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "MOTORBIKE HIGHWAY RACER",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 10.sp,
            letterSpacing = 3.sp,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(CashGreen.copy(alpha = 0.14f))
                    .border(1.dp, CashGreen.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AttachMoney, contentDescription = null, tint = CashGreen, modifier = Modifier.size(15.dp))
                Text(
                    text = String.format(Locale.US, "%,d USD", prefs.usdBalance),
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CashGreen
                )
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard.copy(alpha = 0.85f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BEST ${String.format(Locale.US, "%,d", prefs.bestScore)} · ${formatDist(prefs.bestDistanceMeters)}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    color = BoostCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "$bikeName · TUNING LV.$upgradeLevelSum",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AmberGold
        )
    }
}

@Composable
private fun ActiveRouteBanner(activeRoute: RealRoadTrack, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .widthIn(max = 420.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard.copy(alpha = 0.88f))
            .border(1.dp, BoostCyan.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(Icons.Default.Map, contentDescription = null, tint = BoostCyan, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = activeRoute.roadName,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${activeRoute.regionLabel} · ${if (activeRoute.isGpsDerived) "LIVE GPS HIGHWAY" else "WORLD CIRCUIT"}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    color = if (activeRoute.isGpsDerived) CashGreen else TextMuted
                )
            }
        }
        Text("CHANGE", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = BoostCyan)
    }
}

@Composable
private fun ModeAndDifficultyControls(
    prefs: PlayerPreferencesSnapshot,
    tiltAvailable: Boolean,
    onSelectDifficulty: (Difficulty) -> Unit,
    onSelectGameMode: (GameMode) -> Unit,
    onSelectTrafficMode: (TrafficMode) -> Unit,
    onToggleTilt: () -> Unit
) {
    Column(
        modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Difficulty.entries.forEach { d ->
                val sel = prefs.difficulty == d
                SelectorChip(
                    label = d.label,
                    selected = sel,
                    selectedColor = BoostCyan,
                    modifier = Modifier.weight(1f).testTag("diff_${d.name}"),
                    onClick = { onSelectDifficulty(d) }
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GameMode.entries.forEach { gm ->
                val sel = prefs.gameMode == gm
                SelectorChip(
                    label = gm.label,
                    selected = sel,
                    selectedColor = NeonPink,
                    modifier = Modifier.weight(1f).testTag("mode_${gm.name}"),
                    onClick = { onSelectGameMode(gm) }
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TrafficMode.entries.forEach { tm ->
                val sel = prefs.trafficMode == tm
                SelectorChip(
                    label = tm.label,
                    selected = sel,
                    selectedColor = AmberGold,
                    modifier = Modifier.weight(1f).testTag("traffic_${tm.name}"),
                    onClick = { onSelectTrafficMode(tm) }
                )
            }
            if (tiltAvailable) {
                SelectorChip(
                    label = if (prefs.tiltEnabled) "TILT: ON" else "TILT: OFF",
                    selected = prefs.tiltEnabled,
                    selectedColor = CashGreen,
                    modifier = Modifier.weight(0.95f).testTag("tilt_toggle"),
                    onClick = onToggleTilt
                )
            }
        }
    }
}

@Composable
private fun SelectorChip(
    label: String,
    selected: Boolean,
    selectedColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(if (selected) selectedColor else Color.White.copy(alpha = 0.06f))
            .border(
                1.dp,
                if (selected) selectedColor else Color.White.copy(alpha = 0.14f),
                RoundedCornerShape(11.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (selected) ObsidianBg else TextMuted,
            maxLines = 1
        )
    }
}

@Composable
private fun FeatureNavRow(
    claimableMissionCount: Int,
    onOpenGarage: () -> Unit,
    onOpenMissions: () -> Unit,
    onOpenGpsMap: () -> Unit
) {
    Row(
        modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NavFeatureButton(
            label = "GARAGE ($)",
            icon = Icons.Default.TwoWheeler,
            accent = AmberGold,
            badgeCount = 0,
            modifier = Modifier.weight(1f).testTag("open_garage"),
            onClick = onOpenGarage
        )
        NavFeatureButton(
            label = "MISSIONS",
            icon = Icons.Default.EmojiEvents,
            accent = CashGreen,
            badgeCount = claimableMissionCount,
            modifier = Modifier.weight(1f).testTag("open_missions"),
            onClick = onOpenMissions
        )
        NavFeatureButton(
            label = "GPS MAP",
            icon = Icons.Default.Map,
            accent = BoostCyan,
            badgeCount = 0,
            modifier = Modifier.weight(1f).testTag("open_map"),
            onClick = onOpenGpsMap
        )
    }
}

@Composable
private fun NavFeatureButton(
    label: String,
    icon: ImageVector,
    accent: Color,
    badgeCount: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(SurfaceCard)
            .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(13.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = label, tint = accent, modifier = Modifier.size(20.dp))
            Text(
                text = label,
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
        }
        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(NeonPink),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeCount.toString(),
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun RideCtaButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .widthIn(max = 320.dp)
            .fillMaxWidth()
            .height(56.dp)
            .testTag("start_ride_button"),
        colors = ButtonDefaults.buttonColors(containerColor = NeonPink, contentColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "TAP TO RIDE",
            fontFamily = OrbitronFontFamily,
            fontSize = 17.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun PauseOverlay(
    tiltEnabled: Boolean,
    tiltAvailable: Boolean,
    onToggleTilt: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onMenu: () -> Unit
) {
    BackHandler(onBack = onResume)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg.copy(alpha = 0.88f))
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.widthIn(max = 340.dp).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "PAUSED",
                fontFamily = OrbitronFontFamily,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                color = BoostCyan
            )
            if (tiltAvailable) {
                OutlinedButton(
                    onClick = onToggleTilt,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.ScreenRotation, contentDescription = null, tint = CashGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (tiltEnabled) "TILT STEERING: ON" else "TILT STEERING: OFF",
                        fontFamily = JetBrainsMonoFontFamily,
                        color = if (tiltEnabled) CashGreen else TextMuted
                    )
                }
            }
            Button(
                onClick = onResume,
                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("resume_button"),
                colors = ButtonDefaults.buttonColors(containerColor = NeonPink, contentColor = Color.White),
                shape = RoundedCornerShape(15.dp)
            ) {
                Text("RESUME ▶", fontFamily = OrbitronFontFamily, fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onRestart,
                    modifier = Modifier.weight(1f).height(48.dp).testTag("restart_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("RESTART", fontFamily = JetBrainsMonoFontFamily, color = TextPrimary)
                }
                OutlinedButton(
                    onClick = onMenu,
                    modifier = Modifier.weight(1f).height(48.dp).testTag("pause_menu_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("MENU", fontFamily = JetBrainsMonoFontFamily, color = TextPrimary)
                }
            }
        }
    }
}

@Composable
fun RunOverOverlay(
    summary: RunSummary?,
    isNewBest: Boolean,
    onRideAgain: () -> Unit,
    onOpenGarage: () -> Unit,
    onMenu: () -> Unit
) {
    BackHandler(onBack = onMenu)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg.copy(alpha = 0.90f))
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "RUN OVER",
                fontFamily = OrbitronFontFamily,
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                color = TextPrimary
            )
            Text(
                text = summary?.endReason ?: "WRECKED",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 13.sp,
                letterSpacing = 3.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DangerRed
            )
            if (isNewBest) {
                NewBestBadge()
            }

            summary?.let { s ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CashGreen.copy(alpha = 0.14f))
                        .border(1.dp, CashGreen.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "+$${String.format(Locale.US, "%,d", s.usdEarned)} USD EARNED",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CashGreen
                    )
                    Text(
                        text = "${s.bikeName} · ${s.trafficMode.label} · ${s.draftSeconds}s Draft · ${s.pickupsCollected} Pickups",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile("SCORE", String.format(Locale.US, "%,d", s.score), Modifier.weight(1f))
                    StatTile("DISTANCE", formatDist(s.distanceMeters), Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile("TOP SPEED", "${s.topSpeedKmh} km/h", Modifier.weight(1f))
                    StatTile("NEAR MISSES", s.nearMisses.toString(), Modifier.weight(1f))
                }
            }

            Button(
                onClick = onRideAgain,
                modifier = Modifier.fillMaxWidth().height(54.dp).testTag("ride_again_button"),
                colors = ButtonDefaults.buttonColors(containerColor = NeonPink, contentColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("RIDE AGAIN ▶", fontFamily = OrbitronFontFamily, fontSize = 17.sp, fontWeight = FontWeight.Black)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onOpenGarage,
                    modifier = Modifier.weight(1f).height(48.dp).testTag("over_garage_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("GARAGE ($)", fontFamily = JetBrainsMonoFontFamily, color = AmberGold)
                }
                OutlinedButton(
                    onClick = onMenu,
                    modifier = Modifier.weight(1f).height(48.dp).testTag("over_menu_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("MENU", fontFamily = JetBrainsMonoFontFamily, color = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun NewBestBadge() {
    val infinite = rememberInfiniteTransition(label = "newBest")
    val pulse by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(550), RepeatMode.Reverse),
        label = "pulseBest"
    )
    Text(
        text = "★ NEW BEST ★",
        fontFamily = JetBrainsMonoFontFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold,
        color = ObsidianBg,
        modifier = Modifier
            .scale(pulse)
            .clip(CircleShape)
            .background(Brush.horizontalGradient(listOf(AmberGold, Color(0xFFFF9D2D))))
            .padding(horizontal = 16.dp, vertical = 6.dp)
    )
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(13.dp))
            .background(SurfaceElevated)
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(13.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = TextMuted)
        Spacer(modifier = Modifier.height(3.dp))
        Text(value, fontFamily = JetBrainsMonoFontFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
    }
}
