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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.RadioButtonChecked
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
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
import com.example.ui.theme.CharcoalBg
import com.example.ui.theme.CrimsonPlayStart
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EmeraldAccent
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
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
            )
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
    val onLeftPress = remember(onTouchInput) { { pressed: Boolean -> onTouchInput(pressed, null, null, null) } }
    val onRightPress = remember(onTouchInput) { { pressed: Boolean -> onTouchInput(null, pressed, null, null) } }
    val onBrakePress = remember(onTouchInput) { { pressed: Boolean -> onTouchInput(null, null, pressed, null) } }
    val onNitroPress = remember(onTouchInput) { { pressed: Boolean -> onTouchInput(null, null, null, pressed) } }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val isLandscape = maxWidth > maxHeight
        if (isLandscape) {
            LandscapeRacingHudAndControls(
                hud = hud,
                bestScore = bestScore,
                gameMode = gameMode,
                activeRoute = activeRoute,
                liveGpsLocked = liveGpsLocked,
                tiltEnabled = tiltEnabled,
                banner = banner,
                maxWidth = maxWidth,
                maxHeight = maxHeight,
                onLeftPress = onLeftPress,
                onRightPress = onRightPress,
                onBrakePress = onBrakePress,
                onNitroPress = onNitroPress
            )
        } else {
            PortraitRacingHudAndControls(
                hud = hud,
                bestScore = bestScore,
                gameMode = gameMode,
                activeRoute = activeRoute,
                liveGpsLocked = liveGpsLocked,
                tiltEnabled = tiltEnabled,
                banner = banner,
                onLeftPress = onLeftPress,
                onRightPress = onRightPress,
                onBrakePress = onBrakePress,
                onNitroPress = onNitroPress
            )
        }
    }
}

@Composable
private fun PortraitRacingHudAndControls(
    hud: HudUiState,
    bestScore: Int,
    gameMode: GameMode,
    activeRoute: RealRoadTrack,
    liveGpsLocked: Boolean,
    tiltEnabled: Boolean,
    banner: BannerMessage?,
    onLeftPress: (Boolean) -> Unit,
    onRightPress: (Boolean) -> Unit,
    onBrakePress: (Boolean) -> Unit,
    onNitroPress: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Top Telemetry Bar matching Portrait Racing HUD mockup
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 122.dp, top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text("SCORE", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, color = TextMuted)
                Text(
                    text = String.format(Locale.US, "%,d", hud.score),
                    fontFamily = OrbitronFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
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
                                .size(10.dp)
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
                TimerDisplayText(
                    timeDisplay = timeDisplay,
                    isLandscape = false,
                    isLow = isLow
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
                .padding(top = 122.dp)
        ) {
            banner?.let {
                Text(
                    text = it.text,
                    fontFamily = OrbitronFontFamily,
                    fontSize = 26.sp,
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
                fontSize = 86.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                color = NeonPink,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Portrait Cockpit: Arc Speedometer + Gear hex badge centered over unified 4-pad deck
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PortraitArcSpeedometerAndNitro(
                speedKmh = hud.speedKmh,
                gear = hud.gear,
                nitroPercent = hud.nitroPercent,
                nitroActive = hud.nitroActive
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xD20B101C))
                    .border(1.2.dp, BoostCyan.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ControlPad(
                    label = if (tiltEnabled) "TILT/L" else "LEFT",
                    icon = Icons.Default.ChevronLeft,
                    accent = BoostCyan,
                    modifier = Modifier.weight(1.1f).height(66.dp).testTag("pad_left"),
                    onPressedChange = onLeftPress
                )
                ControlPad(
                    label = "BRAKE",
                    icon = Icons.Default.RadioButtonChecked,
                    accent = DangerRed,
                    modifier = Modifier.weight(1f).height(66.dp).testTag("pad_brake"),
                    onPressedChange = onBrakePress
                )
                ControlPad(
                    label = "NITRO",
                    icon = Icons.Default.Bolt,
                    accent = BoostCyan,
                    dimmed = hud.nitroPercent <= 0f,
                    modifier = Modifier.weight(1f).height(66.dp).testTag("pad_nitro"),
                    onPressedChange = onNitroPress
                )
                ControlPad(
                    label = if (tiltEnabled) "TILT/R" else "RIGHT",
                    icon = Icons.Default.ChevronRight,
                    accent = BoostCyan,
                    modifier = Modifier.weight(1.1f).height(66.dp).testTag("pad_right"),
                    onPressedChange = onRightPress
                )
            }
        }
    }
}

@Composable
private fun PortraitArcSpeedometerAndNitro(
    speedKmh: Int,
    gear: Int,
    nitroPercent: Float,
    nitroActive: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Arc Speedometer dial matching Portrait HUD mockup
            Box(
                modifier = Modifier
                    .width(176.dp)
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                val speedFrac = (speedKmh / 360f).coerceIn(0.03f, 1f)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeW = 10.dp.toPx()
                    val pad = strokeW * 0.7f
                    val arcSize = Size(size.width - pad * 2f, size.width - pad * 2f)
                    val topLeft = Offset(pad, 6.dp.toPx())

                    drawCircle(
                        color = CharcoalBg.copy(alpha = 0.72f),
                        radius = size.width * 0.44f,
                        center = Offset(size.width * 0.5f, size.height * 0.78f)
                    )
                    drawArc(
                        color = Color.White.copy(alpha = 0.14f),
                        startAngle = 165f,
                        sweepAngle = 210f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                    drawArc(
                        brush = Brush.horizontalGradient(
                            listOf(BoostCyan, EmeraldAccent, AmberGold, CrimsonPlayStart)
                        ),
                        startAngle = 165f,
                        sweepAngle = 210f * speedFrac,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                }

                Column(
                    modifier = Modifier.padding(top = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = speedKmh.toString(),
                        fontFamily = OrbitronFontFamily,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        color = if (nitroActive) BoostCyan else TextPrimary
                    )
                    Text(
                        text = "KM/H",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                }
            }

            // Hexagonal GEAR badge matching Portrait HUD mockup
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val hex = Path().apply {
                        moveTo(w * 0.5f, 2f)
                        lineTo(w - 3f, h * 0.27f)
                        lineTo(w - 3f, h * 0.73f)
                        lineTo(w * 0.5f, h - 2f)
                        lineTo(3f, h * 0.73f)
                        lineTo(3f, h * 0.27f)
                        close()
                    }
                    drawPath(hex, CharcoalBg.copy(alpha = 0.86f))
                    drawPath(hex, BoostCyan.copy(alpha = 0.55f), style = Stroke(width = 2.dp.toPx()))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "GEAR",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Text(
                        text = gear.toString(),
                        fontFamily = OrbitronFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                }
            }
        }

        // Sleek Nitro gauge bar below dial
        Box(
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .height(8.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color.White.copy(alpha = 0.12f))
                .border(1.dp, BoostCyan.copy(alpha = 0.35f), RoundedCornerShape(5.dp))
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
internal fun TimerDisplayText(
    timeDisplay: String,
    isLandscape: Boolean,
    isLow: Boolean
) {
    if (isLow) {
        val infinite = rememberInfiniteTransition(label = "hudPulse")
        val pulseScale by infinite.animateFloat(
            initialValue = 1f,
            targetValue = 1.12f,
            animationSpec = infiniteRepeatable(tween(450), RepeatMode.Reverse),
            label = "pulseScale"
        )
        Text(
            text = timeDisplay,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = if (isLandscape) 26.sp else 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = DangerRed,
            modifier = Modifier.graphicsLayer {
                scaleX = pulseScale
                scaleY = pulseScale
            }
        )
    } else {
        Text(
            text = timeDisplay,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = if (isLandscape) 26.sp else 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )
    }
}

@Composable
internal fun ControlPad(
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
                if (pressed) accent.copy(alpha = 0.26f) else Color(0xCC12192B)
            )
            .border(
                width = 1.2.dp,
                color = if (pressed) accent else Color.White.copy(alpha = 0.16f),
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
        Icon(icon, contentDescription = label, tint = if (pressed) accent else TextPrimary, modifier = Modifier.size(24.dp))
        Text(
            text = label,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (pressed) accent else TextPrimary
        )
    }
}

fun formatDist(meters: Int): String =
    if (meters >= 1000) String.format(Locale.US, "%.2f km", meters / 1000f) else "$meters m"
