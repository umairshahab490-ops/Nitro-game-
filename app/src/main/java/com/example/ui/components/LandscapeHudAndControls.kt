package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DailyMission
import com.example.model.GameMode
import com.example.model.GameState
import com.example.model.RealRoadTrack
import com.example.ui.BannerMessage
import com.example.ui.HudUiState
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BoostCyan
import com.example.ui.theme.CashGreen
import com.example.ui.theme.CharcoalBg
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricBlueEnd
import com.example.ui.theme.ElectricBlueStart
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeonPink
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.PanelDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import java.util.Locale

@Composable
fun LandscapeRacingHudAndControls(
    hud: HudUiState,
    bestScore: Int,
    gameMode: GameMode,
    activeRoute: RealRoadTrack,
    liveGpsLocked: Boolean,
    tiltEnabled: Boolean,
    banner: BannerMessage?,
    maxWidth: Dp,
    maxHeight: Dp,
    onLeftPress: (Boolean) -> Unit,
    onRightPress: (Boolean) -> Unit,
    onBrakePress: (Boolean) -> Unit,
    onNitroPress: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompactHeight = maxHeight < 380.dp
    val isCompactWidth = maxWidth < 700.dp
    val circleDiameter = if (isCompactHeight) 56.dp else 64.dp
    val padGap = if (isCompactWidth) 10.dp else 14.dp
    val edgeMargin = if (isCompactWidth) 12.dp else 16.dp
    val bottomMargin = if (isCompactHeight) 8.dp else 12.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("landscape_hud_root")
    ) {
        // Top-Left: Score, Lives/Shield/Bonus, Distance/Best + Live Active Mission Pill
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = edgeMargin, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            LandscapeTelemetryCard(
                hud = hud,
                bestScore = bestScore,
                compact = isCompactHeight
            )

            hud.activeMission?.let { mission ->
                LandscapeMissionPill(
                    mission = mission,
                    compact = isCompactHeight
                )
            }
        }

        // Top-Center: Timer / Survival Stopwatch + Drafting / Combo chip in sky zone
        val isLow = gameMode == GameMode.TIME_ATTACK && hud.timeLeft < 10f && hud.gameState == GameState.RUNNING
        val timeDisplay = if (gameMode == GameMode.TIME_ATTACK) {
            if (hud.timeLeft < 10f) String.format(Locale.US, "%.1f", hud.timeLeft)
            else kotlin.math.ceil(hud.timeLeft).toInt().toString()
        } else {
            val sec = hud.elapsedSeconds.toInt()
            String.format(Locale.US, "%d:%02d", sec / 60, sec % 60)
        }
        LandscapeTimerPill(
            modeLabel = if (gameMode == GameMode.TIME_ATTACK) "TIME" else "SURVIVAL",
            timeDisplay = timeDisplay,
            isLow = isLow,
            isDrafting = hud.isDrafting,
            combo = hud.combo,
            compact = isCompactHeight,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
                .testTag("landscape_timer_panel")
        )

        // Top-Right: Compact Mini-Map Radar tucked below TopUtilityButtons
        LiveMiniMapHud(
            route = activeRoute,
            progressNorm = hud.routeProgressNorm,
            liveGpsLocked = liveGpsLocked,
            compact = isCompactHeight,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = if (isCompactHeight) 56.dp else 62.dp, end = 10.dp)
                .testTag("landscape_minimap")
        )

        // Upper-Sky Event Banner (keeps central road corridor clear)
        AnimatedVisibility(
            visible = banner != null,
            enter = fadeIn(tween(150)) + scaleIn(tween(180), initialScale = 0.75f),
            exit = fadeOut(tween(350)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = if (isCompactHeight) 66.dp else 76.dp)
        ) {
            banner?.let {
                Text(
                    text = it.text,
                    fontFamily = OrbitronFontFamily,
                    fontSize = if (isCompactHeight) 20.sp else 23.sp,
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
                fontFamily = OrbitronFontFamily,
                fontSize = if (isCompactHeight) 72.sp else 84.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                color = NeonPink,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Bottom-Left: Circular Frosted-Glass Left/Right Steering Pedals matching mockup
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = edgeMargin, bottom = bottomMargin),
            horizontalArrangement = Arrangement.spacedBy(padGap),
            verticalAlignment = Alignment.Bottom
        ) {
            CircularControlPedal(
                contentDesc = if (tiltEnabled) "Tilt or Steer Left" else "Steer Left",
                icon = Icons.Default.ChevronLeft,
                accent = BoostCyan,
                sizeDp = circleDiameter,
                modifier = Modifier.testTag("pad_left"),
                onPressedChange = onLeftPress
            )
            CircularControlPedal(
                contentDesc = if (tiltEnabled) "Tilt or Steer Right" else "Steer Right",
                icon = Icons.Default.ChevronRight,
                accent = BoostCyan,
                sizeDp = circleDiameter,
                modifier = Modifier.testTag("pad_right"),
                onPressedChange = onRightPress
            )
        }

        // Bottom-Right: Sleek Speedometer + Gear + N₂O bar above Circular Brake & N₂O Bottle Pedals
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = edgeMargin, bottom = bottomMargin),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(if (isCompactHeight) 6.dp else 8.dp)
        ) {
            LandscapeSpeedAndNitroPanel(
                speedKmh = hud.speedKmh,
                gear = hud.gear,
                nitroPercent = hud.nitroPercent,
                nitroActive = hud.nitroActive,
                compact = isCompactHeight,
                modifier = Modifier
                    .width(circleDiameter * 2 + padGap + 16.dp)
                    .testTag("landscape_speed_panel")
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(padGap),
                verticalAlignment = Alignment.Bottom
            ) {
                CircularControlPedal(
                    contentDesc = "Brake",
                    icon = Icons.Default.RadioButtonChecked,
                    accent = DangerRed,
                    sizeDp = circleDiameter,
                    modifier = Modifier.testTag("pad_brake"),
                    onPressedChange = onBrakePress
                )
                CircularNitroBottlePedal(
                    nitroPercent = hud.nitroPercent,
                    nitroActive = hud.nitroActive,
                    sizeDp = circleDiameter,
                    modifier = Modifier.testTag("pad_nitro"),
                    onPressedChange = onNitroPress
                )
            }
        }
    }
}

@Composable
private fun CircularControlPedal(
    contentDesc: String,
    icon: ImageVector,
    accent: Color,
    sizeDp: Dp,
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

    Box(
        modifier = modifier
            .size(sizeDp)
            .scale(if (pressed) 0.92f else 1f)
            .clip(CircleShape)
            .background(if (pressed) accent.copy(alpha = 0.28f) else Color(0xB80E1525))
            .border(
                width = if (pressed) 2.dp else 1.4.dp,
                color = if (pressed) accent else Color.White.copy(alpha = 0.34f),
                shape = CircleShape
            )
            .semantics {
                role = Role.Button
                contentDescription = contentDesc
            }
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
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDesc,
            tint = if (pressed) accent else Color.White,
            modifier = Modifier.size(sizeDp * 0.52f)
        )
    }
}

@Composable
private fun CircularNitroBottlePedal(
    nitroPercent: Float,
    nitroActive: Boolean,
    sizeDp: Dp,
    modifier: Modifier = Modifier,
    onPressedChange: (Boolean) -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val currentOnPressedChange by rememberUpdatedState(onPressedChange)
    val empty = nitroPercent <= 0f

    DisposableEffect(Unit) {
        onDispose {
            if (pressed) {
                pressed = false
                currentOnPressedChange(false)
            }
        }
    }

    Box(
        modifier = modifier
            .size(sizeDp)
            .scale(if (pressed) 0.92f else 1f)
            .alpha(if (empty && !pressed) 0.50f else 1f)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(
                        if (pressed || nitroActive) BoostCyan.copy(alpha = 0.72f) else ElectricBlueStart.copy(alpha = 0.36f),
                        ElectricBlueEnd.copy(alpha = 0.55f),
                        CharcoalBg.copy(alpha = 0.92f)
                    )
                )
            )
            .border(
                width = if (pressed || nitroActive) 2.2.dp else 1.6.dp,
                color = if (pressed || nitroActive) Color.White else BoostCyan.copy(alpha = 0.85f),
                shape = CircleShape
            )
            .semantics {
                role = Role.Button
                contentDescription = "Nitro Boost"
            }
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
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp * 0.54f)) {
            val w = size.width
            val h = size.height
            // Valve neck
            drawRoundRect(
                color = Color(0xFFE2E8F0),
                topLeft = Offset(w * 0.38f, h * 0.04f),
                size = Size(w * 0.24f, h * 0.14f),
                cornerRadius = CornerRadius(3f)
            )
            // Cylinder shoulder + tank
            drawRoundRect(
                color = BoostCyan,
                topLeft = Offset(w * 0.24f, h * 0.18f),
                size = Size(w * 0.52f, h * 0.76f),
                cornerRadius = CornerRadius(w * 0.22f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.45f),
                topLeft = Offset(w * 0.31f, h * 0.26f),
                size = Size(w * 0.09f, h * 0.54f),
                cornerRadius = CornerRadius(4f)
            )
        }
        Text(
            text = "N₂O",
            fontFamily = OrbitronFontFamily,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = CharcoalBg,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun LandscapeTelemetryCard(
    hud: HudUiState,
    bestScore: Int,
    compact: Boolean
) {
    Column(
        modifier = Modifier
            .widthIn(min = 164.dp, max = 224.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(PanelDark)
            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = if (compact) 5.dp else 7.dp)
            .testTag("landscape_telemetry_panel"),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text("SCORE", fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, color = TextMuted)
                Text(
                    text = String.format(Locale.US, "%,d", hud.score),
                    fontFamily = OrbitronFontFamily,
                    fontSize = if (compact) 14.sp else 16.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("DISTANCE", fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, color = TextMuted)
                Text(
                    text = formatDist(hud.distanceMeters),
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = if (compact) 12.sp else 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BoostCyan
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 3) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
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
                    modifier = Modifier.size(12.dp)
                )
            }
            if (hud.bonusCashUsd > 0) {
                Text(
                    text = "+$${hud.bonusCashUsd}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CashGreen
                )
            }
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "BEST ${String.format(Locale.US, "%,d", bestScore)}",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 8.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun LandscapeMissionPill(
    mission: DailyMission,
    compact: Boolean
) {
    val complete = mission.isComplete
    val targetSafe = mission.target.coerceAtLeast(1)
    val clampedProgress = mission.progress.coerceIn(0, targetSafe)
    val fraction = (clampedProgress.toFloat() / targetSafe.toFloat()).coerceIn(0f, 1f)
    val accent = if (complete) CashGreen else AmberGold

    Column(
        modifier = Modifier
            .widthIn(min = 164.dp, max = 224.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(PanelDark)
            .border(1.dp, accent.copy(alpha = 0.42f), RoundedCornerShape(11.dp))
            .padding(horizontal = 9.dp, vertical = if (compact) 4.dp else 5.dp)
            .testTag("landscape_mission_pill"),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = if (complete) Icons.Default.CheckCircle else Icons.Default.EmojiEvents,
                contentDescription = "Mission Status",
                tint = accent,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = mission.title.uppercase(Locale.US),
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (complete) "DONE" else "$clampedProgress/$targetSafe",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (complete) CashGreen else BoostCyan
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.10f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .background(
                        Brush.horizontalGradient(
                            if (complete) listOf(CashGreen, BoostCyan) else listOf(AmberGold, CashGreen)
                        )
                    )
            )
        }
    }
}

@Composable
private fun LandscapeTimerPill(
    modeLabel: String,
    timeDisplay: String,
    isLow: Boolean,
    isDrafting: Boolean,
    combo: Int,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(PanelDark)
            .border(
                1.dp,
                if (isLow) DangerRed.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.16f),
                RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 14.dp, vertical = if (compact) 4.dp else 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = modeLabel,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 8.sp,
            color = TextMuted
        )
        TimerDisplayText(
            timeDisplay = timeDisplay,
            isLandscape = true,
            isLow = isLow
        )
        if (isDrafting) {
            Text(
                text = "SLIPSTREAM +12%",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BoostCyan
            )
        } else if (combo > 1) {
            Text(
                text = "COMBO ×$combo",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AmberGold
            )
        }
    }
}

@Composable
private fun LandscapeSpeedAndNitroPanel(
    speedKmh: Int,
    gear: Int,
    nitroPercent: Float,
    nitroActive: Boolean,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(PanelDark)
            .border(
                width = 1.dp,
                color = if (nitroActive) BoostCyan.copy(alpha = 0.72f) else Color.White.copy(alpha = 0.16f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 10.dp, vertical = if (compact) 5.dp else 7.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = speedKmh.toString(),
                    fontFamily = OrbitronFontFamily,
                    fontSize = if (compact) 22.sp else 26.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    color = if (nitroActive) BoostCyan else TextPrimary
                )
                Text(
                    text = "KM/H",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(7.dp))
                    .background(BoostCyan.copy(alpha = 0.14f))
                    .border(1.dp, BoostCyan.copy(alpha = 0.45f), RoundedCornerShape(7.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "G$gear",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = BoostCyan
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = "N₂O",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (nitroActive) BoostCyan else TextMuted
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(if (compact) 7.dp else 9.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(5.dp))
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
}
