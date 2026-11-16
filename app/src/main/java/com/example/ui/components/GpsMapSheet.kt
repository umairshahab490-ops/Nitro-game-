package com.example.ui.components

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.GpsLocationState
import com.example.model.RealRoadTrack
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BoostCyan
import com.example.ui.theme.CashGreen
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeonPink
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.File
import java.util.Locale

@Composable
fun GpsMapSheet(
    gpsState: GpsLocationState,
    routes: List<RealRoadTrack>,
    activeRouteId: String,
    onRequestGpsAndScan: () -> Unit,
    onSelectRoute: (String) -> Unit,
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)
    val context = LocalContext.current

    // Configure OSMDroid strictly inside app-private cache/files directories (Play-compliant)
    remember(context) {
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            osmdroidBasePath = File(context.filesDir, "osmdroid")
            osmdroidTileCache = File(context.cacheDir, "osmdroid_tiles")
        }
        true
    }

    val activeRoute = routes.firstOrNull { it.id == activeRouteId } ?: routes.first()
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(14.0)
            // Apply dark midnight neon invert filter to OSM tiles
            val invertMatrix = ColorMatrix(
                floatArrayOf(
                    -0.88f, 0f, 0f, 0f, 240f,
                    0f, -0.84f, 0f, 0f, 245f,
                    0f, 0f, -0.72f, 0f, 255f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            overlayManager.tilesOverlay.setColorFilter(ColorMatrixColorFilter(invertMatrix))
        }
    }

    DisposableEffect(Unit) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg.copy(alpha = 0.96f))
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    IconButton(onClick = onClose, modifier = Modifier.size(48.dp).testTag("map_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "REAL GPS & HIGHWAY MAP",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Text(
                            text = gpsState.statusMessage,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            color = if (gpsState.liveGpsActive) CashGreen else TextMuted
                        )
                    }
                }

                Button(
                    onClick = onRequestGpsAndScan,
                    enabled = !gpsState.isScanningRoads,
                    colors = ButtonDefaults.buttonColors(containerColor = BoostCyan, contentColor = ObsidianBg),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(44.dp).testTag("scan_gps_button")
                ) {
                    if (gpsState.isScanningRoads) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = ObsidianBg)
                    } else {
                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SCAN GPS", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            // Interactive OpenStreetMap Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.2.dp, BoostCyan.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
            ) {
                AndroidView(
                    factory = { mapView },
                    update = { mv ->
                        mv.overlays.clear()
                        routes.forEach { r ->
                            val pts = r.waypoints.map { GeoPoint(it.lat, it.lon) }
                            val isSel = r.id == activeRoute.id
                            val line = Polyline().apply {
                                setPoints(pts)
                                outlinePaint.color = if (isSel) 0xFF22E6FF.toInt() else 0x88FF2D78.toInt()
                                outlinePaint.strokeWidth = if (isSel) 11f else 6f
                            }
                            mv.overlays.add(line)
                        }

                        if (gpsState.latitude != null && gpsState.longitude != null) {
                            val gpsPt = GeoPoint(gpsState.latitude, gpsState.longitude)
                            val marker = Marker(mv).apply {
                                position = gpsPt
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = "Your Live GPS"
                            }
                            mv.overlays.add(marker)
                            if (activeRoute.isGpsDerived) {
                                val firstWp = activeRoute.waypoints.firstOrNull()
                                mv.controller.setCenter(
                                    if (firstWp != null) GeoPoint(firstWp.lat, firstWp.lon) else gpsPt
                                )
                            } else {
                                val firstWp = activeRoute.waypoints.firstOrNull()
                                if (firstWp != null) mv.controller.setCenter(GeoPoint(firstWp.lat, firstWp.lon))
                            }
                        } else {
                            val firstWp = activeRoute.waypoints.firstOrNull()
                            if (firstWp != null) mv.controller.setCenter(GeoPoint(firstWp.lat, firstWp.lon))
                        }
                        mv.invalidate()
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (gpsState.latitude != null && gpsState.longitude != null) {
                    Text(
                        text = String.format(
                            Locale.US,
                            "LIVE %.4f, %.4f",
                            gpsState.latitude,
                            gpsState.longitude
                        ),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CashGreen,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ObsidianBg.copy(alpha = 0.85f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "SELECT REAL HIGHWAY COURSE (${routes.size})",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BoostCyan
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 22.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(routes, key = { it.id }) { route ->
                    val selected = route.id == activeRoute.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) SurfaceElevated else SurfaceCard)
                            .border(
                                if (selected) 1.5.dp else 1.dp,
                                if (selected) BoostCyan else Color.White.copy(alpha = 0.1f),
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { onSelectRoute(route.id) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.Route,
                                contentDescription = null,
                                tint = if (route.isGpsDerived) CashGreen else NeonPink,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = route.roadName,
                                    fontFamily = OrbitronFontFamily,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${route.regionLabel} · ${route.roadClass} · ${formatDist(route.lengthMeters)}",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = if (route.isGpsDerived) CashGreen else TextMuted
                                )
                            }
                        }

                        if (selected) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = BoostCyan)
                        } else {
                            Text(
                                text = if (route.isGpsDerived) "LOCAL GPS" else "PRESET",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = if (route.isGpsDerived) CashGreen else AmberGold
                            )
                        }
                    }
                }
            }
        }
    }
}
