package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameMode
import com.example.model.GameState
import com.example.model.RealRoadTrack
import com.example.ui.BannerMessage
import com.example.ui.HudUiState
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BoostCyan
import com.example.ui.theme.CashGreen
import com.example.ui.theme.DangerRed
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeonPink
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.PanelDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import java.util.Locale

@Composable
fun TopUtilityButtons(
    showPause: Boolean,
    muted: Boolean,
    onPause: () -> Unit,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .statusBarsPadding()
            .padding(top = 8.dp, end = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (showPause) {
            IconButton(
                onClick = onPause,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PanelDark)
                    .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(14.dp))
                    .testTag("pause_button")
            ) {
                Icon(Icons.Default.Pause, contentDescription = "Pause", tint = TextPrimary)
            }
        }
        IconButton(
            onClick = onToggleMute,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(PanelDark)
                .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(14.dp))
                .testTag("mute_button")
        ) {
            Icon(
                imageVector = if (muted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = if (muted) "Unmute Sound" else "Mute Sound",
                tint = if (muted) TextMuted else BoostCyan
            )
        }
    }
}

@Composable
fun AdaptiveRacingHudAndControls(
    hud: HudUiState,
    bestScore: Int,
    gameMode: GameMode,
    activeRoute: RealRoadTrack,
    liveGpsLocked: Boolean,
    tiltEnabled: Boolean,
    banner: BannerMessage?,
    onTouchInput: (left: Boolean?, right: Boolean?, brake: Boolean?, nitro: Boolean?) -> Unit,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "hudPulse")
    val pulseScale by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(450), RepeatMode.Reverse),
        label = "pulseScale"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val isLandscape = maxWidth > maxHeight

        // Top Telemetry Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 122.dp, top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Left: Score + Lives + Shield + Bonus Cash
            Column {
                Text("SCORE", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = TextMuted)
                Text(
                    text = String.format(Locale.US, "%,d", hud.score),
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 3) {
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .clip(CircleShape)
                                .background(
                                    if (i < hud.lives) NeonPink else Color.White.copy(alpha = 0.16f)
                                )
                        )
                    }
                    if (hud.hasShield) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Shield Active",
                            tint = BoostCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    if (hud.bonusCashUsd > 0) {
                        Text(
                            text = "+$${hud.bonusCashUsd}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CashGreen
                        )
                    }
                }
            }

            // Center: Timer (or Survival Stopwatch) + Status Chips
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val isLow = gameMode == GameMode.TIME_ATTACK && hud.timeLeft < 10f && hud.gameState == GameState.RUNNING
                Text(
                    text = if (gameMode == GameMode.TIME_ATTACK) "TIME" else "SURVIVAL",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    color = TextMuted
                )
                val timeDisplay = if (gameMode == GameMode.TIME_ATTACK) {
                    if (hud.timeLeft < 10f) String.format(Locale.US, "%.1f", hud.timeLeft)
                    else kotlin.math.ceil(hud.timeLeft).toInt().toString()
                } else {
                    val sec = hud.elapsedSeconds.toInt()
                    String.format(Locale.US, "%d:%02d", sec / 60, sec % 60)
                }
                Text(
                    text = timeDisplay,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = if (isLandscape) 28.sp else 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isLow) DangerRed else TextPrimary,
                    modifier = if (isLow) Modifier.scale(pulseScale) else Modifier
                )
                if (hud.isDrafting) {
                    Text(
                        text = "SLIPSTREAM +12%",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BoostCyan
                    )
                } else if (hud.combo > 1) {
                    Text(
                        text = "COMBO ×${hud.combo}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AmberGold
                    )
                }
            }

            // Right: Distance & Best
            Column(horizontalAlignment = Alignment.End) {
                Text("DISTANCE", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = TextMuted)
                Text(
                    text = formatDist(hud.distanceMeters),
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = "BEST ${String.format(Locale.US, "%,d", bestScore)}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }

        // Live OSM Mini-Map Radar tucked below top-right action buttons
        LiveMiniMapHud(
            route = activeRoute,
            progressNorm = hud.routeProgressNorm,
            liveGpsLocked = liveGpsLocked,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 64.dp, end = 10.dp)
        )

        // Banner
        AnimatedVisibility(
            visible = banner != null,
            enter = fadeIn(tween(150)) + scaleIn(tween(180), initialScale = 0.75f),
            exit = fadeOut(tween(350)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = if (isLandscape) 66.dp else 128.dp)
        ) {
            banner?.let {
                Text(
                    text = it.text,
                    fontFamily = OrbitronFontFamily,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    color = Color(it.colorArgb)
                )
            }
        }

        // 3-2-1 Countdown
        if (hud.gameState == GameState.COUNTDOWN && hud.countdownLabel.isNotEmpty()) {
            Text(
                text = hud.countdownLabel,
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 88.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NeonPink,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Bottom Controls & Speedometer — adapts cleanly between Portrait & Landscape
        if (isLandscape) {
            // Landscape: Side-docked thumb controls + center speedometer & nitro
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ControlPad(
                        label = if (tiltEnabled) "TILT/L" else "LEFT",
                        icon = Icons.Default.ChevronLeft,
                        accent = BoostCyan,
                        modifier = Modifier.width(86.dp).height(68.dp).testTag("pad_left"),
                        onPressedChange = { onTouchInput(it, null, null, null) }
                    )
                    ControlPad(
                        label = "BRAKE",
                        icon = Icons.Default.KeyboardArrowDown,
                        accent = DangerRed,
                        modifier = Modifier.width(78.dp).height(68.dp).testTag("pad_brake"),
                        onPressedChange = { onTouchInput(null, null, it, null) }
                    )
                }

                SpeedAndNitroStrip(
                    speedKmh = hud.speedKmh,
                    gear = hud.gear,
                    nitroPercent = hud.nitroPercent,
                    modifier = Modifier.width(240.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ControlPad(
                        label = "NITRO",
                        icon = Icons.Default.Bolt,
                        accent = BoostCyan,
                        dimmed = hud.nitroPercent <= 0f,
                        modifier = Modifier.width(78.dp).height(68.dp).testTag("pad_nitro"),
                        onPressedChange = { onTouchInput(null, null, null, it) }
                    )
                    ControlPad(
                        label = if (tiltEnabled) "TILT/R" else "RIGHT",
                        icon = Icons.Default.ChevronRight,
                        accent = BoostCyan,
                        modifier = Modifier.width(86.dp).height(68.dp).testTag("pad_right"),
                        onPressedChange = { onTouchInput(null, it, null, null) }
                    )
                }
            }
        } else {
            // Portrait: Speedometer + Nitro bar stacked over 4-column bottom control deck
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SpeedAndNitroStrip(
                    speedKmh = hud.speedKmh,
                    gear = hud.gear,
                    nitroPercent = hud.nitroPercent,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ControlPad(
                        label = if (tiltEnabled) "TILT/L" else "LEFT",
                        icon = Icons.Default.ChevronLeft,
                        accent = BoostCyan,
                        modifier = Modifier.weight(1.25f).height(76.dp).testTag("pad_left"),
                        onPressedChange = { onTouchInput(it, null, null, null) }
                    )
                    ControlPad(
                        label = "BRAKE",
                        icon = Icons.Default.KeyboardArrowDown,
                        accent = DangerRed,
                        modifier = Modifier.weight(0.85f).height(76.dp).testTag("pad_brake"),
                        onPressedChange = { onTouchInput(null, null, it, null) }
                    )
                    ControlPad(
                        label = "NITRO",
                        icon = Icons.Default.Bolt,
                        accent = BoostCyan,
                        dimmed = hud.nitroPercent <= 0f,
                        modifier = Modifier.weight(0.85f).height(76.dp).testTag("pad_nitro"),
                        onPressedChange = { onTouchInput(null, null, null, it) }
                    )
                    ControlPad(
                        label = if (tiltEnabled) "TILT/R" else "RIGHT",
                        icon = Icons.Default.ChevronRight,
                        accent = BoostCyan,
                        modifier = Modifier.weight(1.25f).height(76.dp).testTag("pad_right"),
                        onPressedChange = { onTouchInput(null, it, null, null) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeedAndNitroStrip(
    speedKmh: Int,
    gear: Int,
    nitroPercent: Float,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = speedKmh.toString(),
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Text(
                text = "KM/H · GEAR ",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 5.dp)
            )
            Text(
                text = gear.toString(),
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BoostCyan,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(8.dp))
        ) {
            val frac = (nitroPercent / 100f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(frac)
                    .background(Brush.horizontalGradient(listOf(BoostCyan, NeonPink)))
            )
        }
    }
}

@Composable
private fun ControlPad(
    label: String,
    icon: ImageVector,
    accent: Color,
    dimmed: Boolean = false,
    modifier: Modifier = Modifier,
    onPressedChange: (Boolean) -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val currentOnPressedChange by rememberUpdatedState(onPressedChange)

    DisposableEffect(Unit) {
        onDispose {
            if (pressed) {
                pressed = false
                currentOnPressedChange(false)
            }
        }
    }

    Column(
        modifier = modifier
            .scale(if (pressed) 0.95f else 1f)
            .alpha(if (dimmed && !pressed) 0.48f else 1f)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (pressed) accent.copy(alpha = 0.24f) else Color(0xB8101628)
            )
            .border(
                width = 1.2.dp,
                color = if (pressed) accent else Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(16.dp)
            )
            .pointerInput(Unit) {
                try {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val isDown = event.changes.any { it.pressed }
                            if (isDown != pressed) {
                                pressed = isDown
                                currentOnPressedChange(isDown)
                            }
                            event.changes.forEach { it.consume() }
                        }
                    }
                } finally {
                    if (pressed) {
                        pressed = false
                        currentOnPressedChange(false)
                    }
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = label, tint = if (pressed) accent else TextPrimary, modifier = Modifier.size(26.dp))
        Text(
            text = label,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (pressed) accent else TextPrimary
        )
    }
}

fun formatDist(meters: Int): String =
    if (meters >= 1000) String.format(Locale.US, "%.2f km", meters / 1000f) else "$meters m"
