package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RealRoadTrack
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BoostCyan
import com.example.ui.theme.CashGreen
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeonPink
import com.example.ui.theme.PanelDark
import com.example.ui.theme.TextPrimary

@Composable
fun LiveMiniMapHud(
    route: RealRoadTrack,
    progressNorm: Float,
    liveGpsLocked: Boolean,
    modifier: Modifier = Modifier
) {
    val normalizedPoints = remember(route.id) {
        val wps = route.waypoints
        if (wps.size < 2) {
            listOf(Offset(0.2f, 0.8f), Offset(0.8f, 0.2f))
        } else {
            val minLat = wps.minOf { it.lat }
            val maxLat = wps.maxOf { it.lat }
            val minLon = wps.minOf { it.lon }
            val maxLon = wps.maxOf { it.lon }
            val spanLat = (maxLat - minLat).coerceAtLeast(0.0005)
            val spanLon = (maxLon - minLon).coerceAtLeast(0.0005)
            wps.map { wp ->
                val nx = ((wp.lon - minLon) / spanLon).toFloat()
                val ny = 1f - ((wp.lat - minLat) / spanLat).toFloat()
                Offset(0.14f + nx * 0.72f, 0.14f + ny * 0.72f)
            }
        }
    }

    Column(
        modifier = modifier
            .width(108.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(PanelDark)
            .border(1.dp, BoostCyan.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (liveGpsLocked) CashGreen else AmberGold)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (liveGpsLocked) "GPS LIVE" else "MAP",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (liveGpsLocked) CashGreen else AmberGold
                )
            }
            Text(
                text = if (route.isGpsDerived) "OSM" else "SIM",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 8.sp,
                color = BoostCyan
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            val w = size.width
            val h = size.height
            val path = Path()
            normalizedPoints.forEachIndexed { idx, pt ->
                val sx = pt.x * w
                val sy = pt.y * h
                if (idx == 0) path.moveTo(sx, sy) else path.lineTo(sx, sy)
            }
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.2f),
                style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            drawPath(
                path = path,
                color = BoostCyan,
                style = Stroke(width = 2.8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Interpolate rider dot along real road waypoints
            val totalSegs = (normalizedPoints.size - 1).coerceAtLeast(1)
            val scaled = progressNorm.coerceIn(0f, 0.9999f) * totalSegs
            val segIdx = scaled.toInt().coerceIn(0, totalSegs - 1)
            val frac = scaled - segIdx
            val a = normalizedPoints[segIdx]
            val b = normalizedPoints[(segIdx + 1).coerceAtMost(normalizedPoints.lastIndex)]
            val dotX = (a.x + (b.x - a.x) * frac) * w
            val dotY = (a.y + (b.y - a.y) * frac) * h

            drawCircle(
                color = NeonPink.copy(alpha = 0.45f),
                radius = 9f,
                center = Offset(dotX, dotY)
            )
            drawCircle(
                color = NeonPink,
                radius = 5f,
                center = Offset(dotX, dotY)
            )
        }

        Text(
            text = route.roadName,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
