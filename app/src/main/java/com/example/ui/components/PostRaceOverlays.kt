package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.RunSummary
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CharcoalBg
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.CharcoalHairline
import com.example.ui.theme.CharcoalPanel
import com.example.ui.theme.CrimsonPlayEnd
import com.example.ui.theme.CrimsonPlayStart
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElectricBlueEnd
import com.example.ui.theme.ElectricBlueStart
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.SelectedGreenEnd
import com.example.ui.theme.SelectedGreenStart
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import java.util.Locale

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
            .background(CharcoalBg.copy(alpha = 0.88f))
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(CharcoalPanel)
                .border(1.dp, CharcoalHairline, RoundedCornerShape(18.dp))
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusTagPill(text = "SESSION SUSPENDED", accentColor = CyanAccent)
            Text(
                text = "PAUSED",
                fontFamily = OrbitronFontFamily,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                color = TextPrimary
            )

            if (tiltAvailable) {
                OutlinedButton(
                    onClick = onToggleTilt,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = null,
                        tint = if (tiltEnabled) EmeraldAccent else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (tiltEnabled) "TILT STEERING: ON" else "TILT STEERING: OFF",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (tiltEnabled) EmeraldAccent else TextMuted
                    )
                }
            }

            Button(
                onClick = onResume,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("resume_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrimsonPlayStart,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "RESUME",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onRestart,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("restart_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("RESTART", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                OutlinedButton(
                    onClick = onMenu,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("pause_menu_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Home, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("MENU", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
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

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        CharcoalBg.copy(alpha = 0.88f),
                        Color(0xFF140B14).copy(alpha = 0.94f),
                        CharcoalBg.copy(alpha = 0.96f)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        val isLandscape = maxWidth > maxHeight
        val isCompactLandscape = isLandscape && maxHeight < 385.dp

        if (isLandscape) {
            Column(
                modifier = Modifier
                    .widthIn(max = 880.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = if (isCompactLandscape) 8.dp else 14.dp),
                verticalArrangement = Arrangement.spacedBy(if (isCompactLandscape) 8.dp else 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Celebratory Trophy + Checkered Flags Hero
                    Column(
                        modifier = Modifier.weight(0.46f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TrophyCheckeredEmblem(compact = true)
                        ResultsTitleHeadline(summary = summary, isNewBest = isNewBest, compact = true)
                        summary?.let { s ->
                            Text(
                                text = "${s.bikeName} · ${s.routeName}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Right Key-Value Telemetry Panel + Payout Banner
                    Column(
                        modifier = Modifier
                            .weight(0.54f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(CharcoalPanel.copy(alpha = 0.92f))
                            .border(1.dp, CharcoalHairline, RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = if (isCompactLandscape) 8.dp else 12.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        summary?.let { s ->
                            LandscapeMetricLine("SCORE", String.format(Locale.US, "%,d", s.score), AmberAccent)
                            LandscapeMetricLine("DISTANCE", formatDist(s.distanceMeters), TextPrimary)
                            LandscapeMetricLine("TOP SPEED", "${s.topSpeedKmh} km/h", CyanAccent)
                            LandscapeMetricLine("NEAR MISSES", s.nearMisses.toString(), TextPrimary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AmberAccent.copy(alpha = 0.14f))
                                    .border(1.dp, AmberAccent.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AttachMoney, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "+$${String.format(Locale.US, "%,d", s.usdEarned)} EARNED",
                                    fontFamily = OrbitronFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = AmberAccent
                                )
                            }
                        }
                    }
                }

                // Bottom Landscape 3-button Action Row (RETRY / GARAGE / NEXT)
                Row(
                    modifier = Modifier
                        .widthIn(max = 580.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(listOf(ElectricBlueStart, ElectricBlueEnd)))
                            .clickable(onClick = onRideAgain)
                            .testTag("ride_again_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = CharcoalBg, modifier = Modifier.size(17.dp))
                            Text("RETRY", fontFamily = OrbitronFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Black, color = CharcoalBg)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF28204A))
                            .border(1.dp, Color(0xFF7B61FF).copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                            .clickable(onClick = onOpenGarage)
                            .testTag("over_garage_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(17.dp))
                            Text("GARAGE", fontFamily = OrbitronFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(listOf(SelectedGreenStart, SelectedGreenEnd)))
                            .clickable(onClick = onMenu)
                            .testTag("over_menu_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.FastForward, contentDescription = null, tint = CharcoalBg, modifier = Modifier.size(17.dp))
                            Text("MENU", fontFamily = OrbitronFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Black, color = CharcoalBg)
                        }
                    }
                }
            }
        } else {
            // Portrait Run Over Layout matching mockup
            Column(
                modifier = Modifier
                    .widthIn(max = 440.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TrophyCheckeredEmblem(compact = false)
                ResultsTitleHeadline(summary = summary, isNewBest = isNewBest, compact = false)

                summary?.let { s ->
                    Text(
                        text = "+$${String.format(Locale.US, "%,d", s.usdEarned)} USD EARNED",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic,
                        color = EmeraldAccent
                    )
                    Text(
                        text = "${s.bikeName} · ${s.pickupsCollected} Pickups · ${s.routeName}",
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 11.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                    ResultsTelemetryGrid(summary = s)
                }

                // Portrait Crimson RIDE AGAIN + bottom GARAGE ($) & MENU
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.horizontalGradient(listOf(CrimsonPlayStart, CrimsonPlayEnd)))
                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                        .clickable(onClick = onRideAgain)
                        .testTag("ride_again_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "RIDE AGAIN",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Italic,
                            color = Color.White
                        )
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenGarage,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("over_garage_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("GARAGE ($)", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = AmberAccent)
                    }
                    OutlinedButton(
                        onClick = onMenu,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("over_menu_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("MENU", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun TrophyCheckeredEmblem(compact: Boolean) {
    Canvas(
        modifier = Modifier
            .width(if (compact) 128.dp else 156.dp)
            .height(if (compact) 54.dp else 68.dp)
    ) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f

        // Left & right crossed checkered flags
        for (dir in listOf(-1f, 1f)) {
            rotate(degrees = dir * 24f, pivot = Offset(cx, h * 0.72f)) {
                val fx = if (dir < 0) cx - w * 0.34f else cx + w * 0.08f
                val fy = h * 0.14f
                val fw = w * 0.26f
                val fh = h * 0.36f
                val cols = 4
                val rows = 3
                val cw = fw / cols
                val ch = fh / rows
                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        drawRect(
                            color = if ((r + c) % 2 == 0) Color.White else Color(0xFF181E2B),
                            topLeft = Offset(fx + c * cw, fy + r * ch),
                            size = Size(cw, ch)
                        )
                    }
                }
                drawLine(
                    color = Color(0xFFCBD5E1),
                    start = Offset(if (dir < 0) fx + fw else fx, fy),
                    end = Offset(cx, h * 0.88f),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }
        }

        // Golden Champion Trophy cup
        val cupPath = Path().apply {
            moveTo(cx - w * 0.13f, h * 0.12f)
            lineTo(cx + w * 0.13f, h * 0.12f)
            lineTo(cx + w * 0.09f, h * 0.56f)
            lineTo(cx - w * 0.09f, h * 0.56f)
            close()
        }
        drawPath(
            path = cupPath,
            brush = Brush.verticalGradient(listOf(Color(0xFFFFE57F), AmberAccent, Color(0xFFCC7A00)))
        )
        // Cup handles
        drawArc(
            color = AmberAccent,
            startAngle = 90f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(cx - w * 0.20f, h * 0.16f),
            size = Size(w * 0.12f, h * 0.26f),
            style = Stroke(width = 3.5f)
        )
        drawArc(
            color = AmberAccent,
            startAngle = -90f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(cx + w * 0.08f, h * 0.16f),
            size = Size(w * 0.12f, h * 0.26f),
            style = Stroke(width = 3.5f)
        )
        // Stem & pedestal base
        drawRect(
            color = AmberAccent,
            topLeft = Offset(cx - w * 0.03f, h * 0.56f),
            size = Size(w * 0.06f, h * 0.18f)
        )
        drawRoundRect(
            color = Color(0xFFFFD34D),
            topLeft = Offset(cx - w * 0.11f, h * 0.74f),
            size = Size(w * 0.22f, h * 0.12f),
            cornerRadius = CornerRadius(4f)
        )
    }
}

@Composable
private fun ResultsTitleHeadline(
    summary: RunSummary?,
    isNewBest: Boolean,
    compact: Boolean
) {
    val reason = summary?.endReason ?: "WRECKED"
    val reasonColor = if (reason == "TIME UP") AmberAccent else CrimsonPlayStart

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "RUN OVER",
            fontFamily = OrbitronFontFamily,
            fontSize = if (compact) 26.sp else 32.sp,
            fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic,
            color = TextPrimary,
            lineHeight = if (compact) 26.sp else 32.sp
        )
        Text(
            text = reason,
            fontFamily = OrbitronFontFamily,
            fontSize = if (compact) 17.sp else 20.sp,
            fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic,
            letterSpacing = 1.6.sp,
            color = reasonColor
        )
        if (isNewBest) {
            NewBestRibbon()
        }
    }
}

@Composable
private fun LandscapeMetricLine(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted
        )
        Text(
            text = value,
            fontFamily = OrbitronFontFamily,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = valueColor
        )
    }
}

@Composable
private fun ResultsTelemetryGrid(summary: RunSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ResultMetricTile("SCORE", String.format(Locale.US, "%,d", summary.score), TextPrimary, Modifier.weight(1f))
            ResultMetricTile("DISTANCE", formatDist(summary.distanceMeters), TextPrimary, Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ResultMetricTile("TOP SPEED", "${summary.topSpeedKmh} km/h", TextPrimary, Modifier.weight(1f))
            ResultMetricTile("NEAR MISSES", summary.nearMisses.toString(), TextPrimary, Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ResultMiniChip("DRAFT", "${summary.draftSeconds}s", Modifier.weight(1f))
            ResultMiniChip("PICKUPS", summary.pickupsCollected.toString(), Modifier.weight(1f))
            ResultMiniChip("TWO-WAY", formatDist(summary.twoWayMeters), Modifier.weight(1f))
        }
    }
}

@Composable
private fun ResultMetricTile(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CharcoalElevated)
            .border(1.dp, CharcoalHairline, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = label,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted
        )
        Text(
            text = value,
            fontFamily = OrbitronFontFamily,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ResultMiniChip(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CharcoalPanel)
            .border(1.dp, CharcoalHairline, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, color = TextMuted)
        Text(value, fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

@Composable
private fun NewBestRibbon() {
    val infinite = rememberInfiniteTransition(label = "newBest")
    val pulse by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(650), RepeatMode.Reverse),
        label = "pulseBest"
    )
    Row(
        modifier = Modifier
            .graphicsLayer {
                scaleX = pulse
                scaleY = pulse
            }
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFFFFD34D), AmberAccent)))
            .padding(horizontal = 14.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(Icons.Default.Star, contentDescription = null, tint = CharcoalBg, modifier = Modifier.size(12.dp))
        Text(
            text = "NEW BEST",
            fontFamily = OrbitronFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = CharcoalBg
        )
        Icon(Icons.Default.Star, contentDescription = null, tint = CharcoalBg, modifier = Modifier.size(12.dp))
    }
}
