package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BikeModel
import com.example.model.BikeUpgradeState
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CharcoalBg
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.CharcoalHairline
import com.example.ui.theme.CharcoalPanel
import com.example.ui.theme.CrimsonPlayStart
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import java.util.Locale
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

enum class PaddockNavTab {
    HOME,
    GARAGE,
    MISSIONS,
    SETTINGS
}

@Composable
fun MotorsportSheetHeader(
    title: String,
    subtitle: String,
    backTag: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CharcoalPanel.copy(alpha = 0.9f))
                    .border(1.dp, CharcoalHairline, RoundedCornerShape(12.dp))
                    .testTag(backTag)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontFamily = OrbitronFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    letterSpacing = 0.6.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(10.dp))
            trailingContent()
        }
    }
}

@Composable
fun CurrencyPill(
    usdBalance: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CharcoalPanel.copy(alpha = 0.92f))
            .border(1.dp, EmeraldAccent.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .padding(horizontal = 11.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.AttachMoney,
            contentDescription = null,
            tint = EmeraldAccent,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = String.format(Locale.US, "%,d USD", usdBalance),
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = EmeraldAccent
        )
    }
}

@Composable
fun NitroRushEmblemLogo(
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(
                modifier = Modifier
                    .width(if (compact) 190.dp else 248.dp)
                    .height(if (compact) 60.dp else 78.dp)
            ) {
                val w = size.width
                val h = size.height
                // Outer tachometer red/cyan sweeping arc above title
                drawArc(
                    brush = Brush.horizontalGradient(
                        listOf(CyanAccent, CrimsonPlayStart, AmberAccent)
                    ),
                    startAngle = 195f,
                    sweepAngle = 150f,
                    useCenter = false,
                    topLeft = Offset(w * 0.10f, h * 0.04f),
                    size = Size(w * 0.80f, h * 1.10f),
                    style = Stroke(width = h * 0.09f, cap = StrokeCap.Round)
                )
                // Inner subtle tick marks
                for (i in 0..8) {
                    val ang = Math.toRadians(202.0 + i * 17.0)
                    val cx = w * 0.5f
                    val cy = h * 0.59f
                    val r1 = w * 0.34f
                    val r2 = w * 0.38f
                    drawLine(
                        color = if (i >= 6) CrimsonPlayStart else Color.White.copy(alpha = 0.45f),
                        start = Offset(cx + (cos(ang) * r1).toFloat(), cy + (sin(ang) * r1 * 0.52f).toFloat()),
                        end = Offset(cx + (cos(ang) * r2).toFloat(), cy + (sin(ang) * r2 * 0.52f).toFloat()),
                        strokeWidth = 2.5f
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(if (compact) 12.dp else 16.dp))
                Text(
                    text = "NITRO",
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    fontSize = if (compact) 28.sp else 36.sp,
                    letterSpacing = 2.sp,
                    color = TextPrimary,
                    lineHeight = if (compact) 28.sp else 36.sp
                )
                Text(
                    text = "RUSH",
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    fontSize = if (compact) 26.sp else 32.sp,
                    letterSpacing = 3.sp,
                    color = CrimsonPlayStart,
                    lineHeight = if (compact) 24.sp else 30.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "MOTORBIKE HIGHWAY RACER",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = if (compact) 9.sp else 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.6.sp,
            color = CyanAccent
        )
    }
}

@Composable
fun PaddockBottomNavBar(
    currentTab: PaddockNavTab,
    onNavigate: (PaddockNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(top = 6.dp, bottom = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CharcoalPanel.copy(alpha = 0.96f))
            .border(1.dp, CharcoalHairline, RoundedCornerShape(16.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PaddockNavBarItem(
            label = "Home",
            icon = Icons.Default.Home,
            selected = currentTab == PaddockNavTab.HOME,
            modifier = Modifier.weight(1f).testTag("nav_home"),
            onClick = { onNavigate(PaddockNavTab.HOME) }
        )
        PaddockNavBarItem(
            label = "Garage",
            icon = Icons.Default.TwoWheeler,
            selected = currentTab == PaddockNavTab.GARAGE,
            modifier = Modifier.weight(1f).testTag("nav_garage"),
            onClick = { onNavigate(PaddockNavTab.GARAGE) }
        )
        PaddockNavBarItem(
            label = "Missions",
            icon = Icons.Default.EmojiEvents,
            selected = currentTab == PaddockNavTab.MISSIONS,
            modifier = Modifier.weight(1f).testTag("nav_missions"),
            onClick = { onNavigate(PaddockNavTab.MISSIONS) }
        )
        PaddockNavBarItem(
            label = "Settings",
            icon = Icons.Default.Settings,
            selected = currentTab == PaddockNavTab.SETTINGS,
            modifier = Modifier.weight(1f).testTag("nav_settings"),
            onClick = { onNavigate(PaddockNavTab.SETTINGS) }
        )
    }
}

@Composable
private fun PaddockNavBarItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) CyanAccent.copy(alpha = 0.14f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) AmberAccent else TextMuted,
            modifier = Modifier.size(19.dp)
        )
        Text(
            text = label,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (selected) TextPrimary else TextMuted
        )
    }
}

/**
 * Large, studio-lit side-profile motorcycle vector showcase reusing each BikeModel's
 * real-world or custom-painted livery palette and tuning state.
 */
@Composable
fun MotorcycleShowcaseCanvas(
    bike: BikeModel,
    upgrades: BikeUpgradeState,
    modifier: Modifier = Modifier,
    showPaddockStage: Boolean = true
) {
    val fairingPath = remember { Path() }
    val bellyPath = remember { Path() }
    val tankPath = remember { Path() }
    val stripePath = remember { Path() }
    val screenPath = remember { Path() }

    val primary = Color(bike.primaryColor)
    val trim = Color(bike.trimColor)
    val accent = Color(bike.accentColor)
    val tuned = upgrades.totalUpgradeLevel > 3

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 2f || h <= 2f) return@Canvas

        val bikeW = min(w * 0.88f, h * 1.95f)
        val bikeH = bikeW * 0.48f
        val left = (w - bikeW) * 0.5f
        val groundY = h * 0.82f
        val top = groundY - bikeH

        if (showPaddockStage) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(primary.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(w * 0.5f, groundY - bikeH * 0.35f),
                    radius = bikeW * 0.65f
                ),
                topLeft = Offset.Zero,
                size = size
            )
            // Illuminated turntable ring glow
            drawOval(
                color = CyanAccent.copy(alpha = 0.25f),
                topLeft = Offset(left + bikeW * 0.01f, groundY - bikeH * 0.08f),
                size = Size(bikeW * 0.98f, bikeH * 0.16f),
                style = Stroke(width = 2.5f)
            )
        }

        drawOval(
            color = Color.Black.copy(alpha = 0.55f),
            topLeft = Offset(left + bikeW * 0.05f, groundY - bikeH * 0.05f),
            size = Size(bikeW * 0.90f, bikeH * 0.12f)
        )

        val wheelR = bikeH * 0.31f
        val rearCx = left + bikeW * 0.21f
        val frontCx = left + bikeW * 0.79f
        val wheelCy = groundY - wheelR

        // Rear swingarm & chain tensioner
        drawLine(
            color = Color(0xFF5D667A),
            start = Offset(rearCx, wheelCy),
            end = Offset(left + bikeW * 0.44f, wheelCy - bikeH * 0.08f),
            strokeWidth = maxOf(3f, bikeH * 0.065f),
            cap = StrokeCap.Round
        )

        // Under-engine exhaust collector + upswept titanium muffler
        val mufflerColor = if (tuned) Color(0xFFB5C2D8) else Color(0xFF7B8599)
        drawLine(
            color = Color(0xFF434B5C),
            start = Offset(left + bikeW * 0.52f, wheelCy + bikeH * 0.04f),
            end = Offset(left + bikeW * 0.35f, wheelCy + bikeH * 0.02f),
            strokeWidth = maxOf(2.5f, bikeH * 0.05f),
            cap = StrokeCap.Round
        )
        drawLine(
            color = mufflerColor,
            start = Offset(left + bikeW * 0.36f, wheelCy + bikeH * 0.02f),
            end = Offset(left + bikeW * 0.18f, wheelCy - bikeH * 0.17f),
            strokeWidth = maxOf(4f, bikeH * 0.085f),
            cap = StrokeCap.Round
        )
        if (tuned) {
            drawCircle(
                color = CyanAccent.copy(alpha = 0.85f),
                radius = maxOf(2f, bikeH * 0.032f),
                center = Offset(left + bikeW * 0.175f, wheelCy - bikeH * 0.175f)
            )
        }

        // Wheels
        for ((idx, cx) in listOf(rearCx, frontCx).withIndex()) {
            drawCircle(
                color = Color(0xFF0E1118),
                radius = wheelR,
                center = Offset(cx, wheelCy)
            )
            drawCircle(
                color = Color(0xFF2B3242),
                radius = wheelR * 0.67f,
                center = Offset(cx, wheelCy),
                style = Stroke(width = maxOf(2f, wheelR * 0.12f))
            )
            drawCircle(
                color = accent.copy(alpha = 0.85f),
                radius = wheelR * 0.72f,
                center = Offset(cx, wheelCy),
                style = Stroke(width = maxOf(1.2f, wheelR * 0.055f))
            )
            drawCircle(
                color = Color(0xFF8A95A8).copy(alpha = 0.72f),
                radius = wheelR * 0.45f,
                center = Offset(cx, wheelCy),
                style = Stroke(width = maxOf(1.5f, wheelR * 0.14f))
            )
            for (s in 0 until 5) {
                val angle = (s * 72f + if (idx == 1) 18f else 0f) * (Math.PI / 180.0)
                drawLine(
                    color = Color(0xFF475166),
                    start = Offset(cx, wheelCy),
                    end = Offset(
                        cx + (cos(angle) * wheelR * 0.62f).toFloat(),
                        wheelCy + (sin(angle) * wheelR * 0.62f).toFloat()
                    ),
                    strokeWidth = maxOf(1.5f, wheelR * 0.08f)
                )
            }
            drawCircle(
                color = accent,
                radius = wheelR * 0.16f,
                center = Offset(cx + (if (idx == 1) -wheelR * 0.34f else wheelR * 0.30f), wheelCy - wheelR * 0.22f)
            )
            drawCircle(color = Color(0xFFCBD5E1), radius = wheelR * 0.13f, center = Offset(cx, wheelCy))
        }

        // Inverted front fork
        drawLine(
            color = if (bike.id.contains("ducati") || bike.id.contains("bmw")) Color(0xFFE0B44A) else Color(0xFF94A3B8),
            start = Offset(frontCx, wheelCy),
            end = Offset(left + bikeW * 0.69f, top + bikeH * 0.27f),
            strokeWidth = maxOf(3f, bikeH * 0.065f),
            cap = StrokeCap.Round
        )

        bellyPath.reset()
        bellyPath.moveTo(left + bikeW * 0.35f, wheelCy + bikeH * 0.04f)
        bellyPath.lineTo(left + bikeW * 0.63f, wheelCy + bikeH * 0.03f)
        bellyPath.lineTo(left + bikeW * 0.69f, wheelCy - bikeH * 0.18f)
        bellyPath.lineTo(left + bikeW * 0.38f, wheelCy - bikeH * 0.18f)
        bellyPath.close()
        drawPath(bellyPath, trim)

        drawLine(
            color = Color(0xFF202634),
            start = Offset(left + bikeW * 0.20f, top + bikeH * 0.32f),
            end = Offset(left + bikeW * 0.44f, top + bikeH * 0.41f),
            strokeWidth = maxOf(3f, bikeH * 0.08f),
            cap = StrokeCap.Round
        )

        fairingPath.reset()
        fairingPath.moveTo(left + bikeW * 0.12f, top + bikeH * 0.26f)
        fairingPath.lineTo(left + bikeW * 0.34f, top + bikeH * 0.40f)
        fairingPath.lineTo(left + bikeW * 0.56f, top + bikeH * 0.24f)
        fairingPath.lineTo(left + bikeW * 0.77f, top + bikeH * 0.31f)
        fairingPath.lineTo(left + bikeW * 0.84f, top + bikeH * 0.48f)
        fairingPath.lineTo(left + bikeW * 0.65f, wheelCy - bikeH * 0.13f)
        fairingPath.lineTo(left + bikeW * 0.39f, wheelCy - bikeH * 0.13f)
        fairingPath.lineTo(left + bikeW * 0.23f, top + bikeH * 0.38f)
        fairingPath.close()
        drawPath(fairingPath, primary)

        tankPath.reset()
        tankPath.moveTo(left + bikeW * 0.38f, top + bikeH * 0.40f)
        tankPath.quadraticTo(
            left + bikeW * 0.49f, top + bikeH * 0.21f,
            left + bikeW * 0.61f, top + bikeH * 0.27f
        )
        tankPath.lineTo(left + bikeW * 0.56f, top + bikeH * 0.45f)
        tankPath.close()
        drawPath(tankPath, trim.copy(alpha = 0.78f))

        stripePath.reset()
        stripePath.moveTo(left + bikeW * 0.43f, top + bikeH * 0.49f)
        stripePath.lineTo(left + bikeW * 0.78f, top + bikeH * 0.39f)
        stripePath.lineTo(left + bikeW * 0.74f, top + bikeH * 0.46f)
        stripePath.lineTo(left + bikeW * 0.45f, top + bikeH * 0.56f)
        stripePath.close()
        drawPath(stripePath, accent)

        screenPath.reset()
        screenPath.moveTo(left + bikeW * 0.62f, top + bikeH * 0.28f)
        screenPath.quadraticTo(
            left + bikeW * 0.68f, top + bikeH * 0.14f,
            left + bikeW * 0.76f, top + bikeH * 0.31f
        )
        screenPath.close()
        drawPath(screenPath, Color(0xFF0E1420).copy(alpha = 0.90f))

        drawCircle(
            color = Color(0xFFCBD5E1),
            radius = maxOf(2f, bikeH * 0.032f),
            center = Offset(left + bikeW * 0.62f, top + bikeH * 0.29f)
        )

        drawRoundRect(
            color = Color(0xFFF8FAFC),
            topLeft = Offset(left + bikeW * 0.77f, top + bikeH * 0.37f),
            size = Size(bikeW * 0.055f, bikeH * 0.045f),
            cornerRadius = CornerRadius(4f)
        )
        drawRoundRect(
            color = Color(0xFFFF3D54),
            topLeft = Offset(left + bikeW * 0.125f, top + bikeH * 0.27f),
            size = Size(bikeW * 0.04f, bikeH * 0.04f),
            cornerRadius = CornerRadius(3f)
        )
    }
}

@Composable
fun PaddockSpecStatBar(
    label: String,
    valueLabel: String,
    progress: Float,
    accentColor: Color,
    modifier: Modifier = Modifier,
    gradientColors: List<Color>? = null
) {
    val fillBrush = Brush.horizontalGradient(
        gradientColors ?: listOf(accentColor, accentColor.copy(alpha = 0.72f))
    )
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            )
            Text(
                text = valueLabel,
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.08f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0.05f, 1f))
                    .clip(RoundedCornerShape(3.dp))
                    .background(fillBrush)
            )
        }
    }
}

@Composable
fun StatusTagPill(
    text: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(accentColor.copy(alpha = 0.14f))
            .border(1.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = accentColor,
            maxLines = 1
        )
    }
}
