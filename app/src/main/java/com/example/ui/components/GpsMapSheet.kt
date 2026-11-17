package com.example.ui.components

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.model.GpsLocationState
import com.example.model.RealRoadTrack
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CharcoalBg
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.CharcoalHairline
import com.example.ui.theme.CharcoalPanel
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElectricBlueEnd
import com.example.ui.theme.ElectricBlueStart
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.PlusJakartaFontFamily
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
    usdBalance: Int = 0,
    onRequestGpsAndScan: () -> Unit,
    onSelectRoute: (String) -> Unit,
    onStartRace: () -> Unit = {},
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
            // Apply dark charcoal telemetry invert filter to OSM tiles
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

    val recenterMap = {
        val targetPt = activeRoute.waypoints.firstOrNull()?.let { GeoPoint(it.lat, it.lon) }
            ?: if (gpsState.latitude != null && gpsState.longitude != null) {
                GeoPoint(gpsState.latitude, gpsState.longitude)
            } else {
                null
            }
        if (targetPt != null) {
            mapView.controller.setZoom(14.0)
            mapView.controller.animateTo(targetPt)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBg.copy(alpha = 0.98f))
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.TopCenter
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 1080.dp)
                .padding(horizontal = 16.dp)
        ) {
            val isLandscape = maxWidth > maxHeight

            Column(modifier = Modifier.fillMaxSize()) {
                MotorsportSheetHeader(
                    title = "REAL GPS & HIGHWAY MAP",
                    subtitle = "Select your race route · ${gpsState.statusMessage}",
                    backTag = "map_back",
                    onBack = onClose,
                    trailingContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isLandscape && usdBalance > 0) {
                                CurrencyPill(usdBalance = usdBalance)
                            }
                            Button(
                                onClick = onRequestGpsAndScan,
                                enabled = !gpsState.isScanningRoads,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyanAccent,
                                    contentColor = CharcoalBg
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("scan_gps_button")
                            ) {
                                if (gpsState.isScanningRoads) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = CharcoalBg
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = "Scan GPS",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SCAN GPS",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                )

                if (isLandscape) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        HighwayOsmViewport(
                            mapView = mapView,
                            routes = routes,
                            activeRoute = activeRoute,
                            gpsState = gpsState,
                            onZoomIn = { mapView.controller.zoomIn() },
                            onZoomOut = { mapView.controller.zoomOut() },
                            onRecenter = recenterMap,
                            modifier = Modifier
                                .weight(0.50f)
                                .fillMaxHeight()
                        )

                        Column(
                            modifier = Modifier
                                .weight(0.50f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentPadding = PaddingValues(bottom = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(routes, key = { it.id }) { route ->
                                    RouteCourseCard(
                                        route = route,
                                        selected = route.id == activeRoute.id,
                                        onSelect = { onSelectRoute(route.id) }
                                    )
                                }
                            }

                            StartRaceMapButton(onClick = onStartRace)
                        }
                    }
                } else {
                    HighwayOsmViewport(
                        mapView = mapView,
                        routes = routes,
                        activeRoute = activeRoute,
                        gpsState = gpsState,
                        onZoomIn = { mapView.controller.zoomIn() },
                        onZoomOut = { mapView.controller.zoomOut() },
                        onRecenter = recenterMap,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(routes, key = { it.id }) { route ->
                            RouteCourseCard(
                                route = route,
                                selected = route.id == activeRoute.id,
                                onSelect = { onSelectRoute(route.id) }
                            )
                        }
                    }

                    StartRaceMapButton(
                        onClick = onStartRace,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StartRaceMapButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(listOf(ElectricBlueStart, ElectricBlueEnd)))
            .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("map_start_race"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Flag,
                contentDescription = null,
                tint = CharcoalBg,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "START RACE",
                fontFamily = OrbitronFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                letterSpacing = 1.2.sp,
                color = CharcoalBg
            )
        }
    }
}

@Composable
private fun HighwayOsmViewport(
    mapView: MapView,
    routes: List<RealRoadTrack>,
    activeRoute: RealRoadTrack,
    gpsState: GpsLocationState,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onRecenter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CharcoalPanel)
            .border(1.2.dp, CyanAccent.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
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
                        outlinePaint.color = if (isSel) 0xFF19D4EA.toInt() else 0x88E62E6B.toInt()
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
                    val firstWp = activeRoute.waypoints.firstOrNull()
                    mv.controller.setCenter(
                        if (firstWp != null) GeoPoint(firstWp.lat, firstWp.lon) else gpsPt
                    )
                } else {
                    val firstWp = activeRoute.waypoints.firstOrNull()
                    if (firstWp != null) mv.controller.setCenter(GeoPoint(firstWp.lat, firstWp.lon))
                }
                mv.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        // Compass N badge + active route telemetry overlay
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(CharcoalBg.copy(alpha = 0.88f))
                    .border(1.2.dp, CyanAccent.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = "North Compass",
                        tint = CyanAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "N",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .widthIn(max = 220.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CharcoalBg.copy(alpha = 0.88f))
                    .border(1.dp, CharcoalHairline, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = activeRoute.roadName,
                    fontFamily = OrbitronFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${activeRoute.regionLabel} · ${formatDist(activeRoute.lengthMeters)}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    color = if (activeRoute.isGpsDerived) EmeraldAccent else TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Functional map navigation controls (Zoom In, Zoom Out, Recenter)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CharcoalBg.copy(alpha = 0.88f))
                .border(1.dp, CharcoalHairline, RoundedCornerShape(12.dp)),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            IconButton(
                onClick = onZoomIn,
                modifier = Modifier.size(48.dp).testTag("map_zoom_in")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom in", tint = TextPrimary)
            }
            Box(
                modifier = Modifier
                    .width(34.dp)
                    .height(1.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(CharcoalHairline)
            )
            IconButton(
                onClick = onZoomOut,
                modifier = Modifier.size(48.dp).testTag("map_zoom_out")
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom out", tint = TextPrimary)
            }
            Box(
                modifier = Modifier
                    .width(34.dp)
                    .height(1.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(CharcoalHairline)
            )
            IconButton(
                onClick = onRecenter,
                modifier = Modifier.size(48.dp).testTag("map_recenter")
            ) {
                Icon(Icons.Default.CenterFocusStrong, contentDescription = "Recenter route", tint = CyanAccent)
            }
        }

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
                color = EmeraldAccent,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CharcoalBg.copy(alpha = 0.88f))
                    .border(1.dp, EmeraldAccent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

private fun routeThumbnailResOrNull(routeId: String): Int? = when (routeId) {
    "preset_pch" -> R.drawable.img_route_pch
    "preset_wangan" -> R.drawable.img_route_wangan
    "preset_autobahn" -> R.drawable.img_route_autobahn
    else -> null
}

@Composable
private fun RouteCourseCard(
    route: RealRoadTrack,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val thumbRes = routeThumbnailResOrNull(route.id)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) CharcoalElevated else CharcoalPanel)
            .border(
                width = if (selected) 1.6.dp else 1.dp,
                color = if (selected) CyanAccent else CharcoalHairline,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onSelect)
            .padding(10.dp)
            .testTag("route_${route.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .width(74.dp)
                    .height(50.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CharcoalBg)
                    .border(
                        1.dp,
                        if (selected) CyanAccent.copy(alpha = 0.65f) else CharcoalHairline,
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (thumbRes != null) {
                    Image(
                        painter = painterResource(id = thumbRes),
                        contentDescription = route.roadName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Route,
                        contentDescription = null,
                        tint = EmeraldAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = route.roadName,
                    fontFamily = OrbitronFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${route.regionLabel} · ${formatDist(route.lengthMeters)}",
                    fontFamily = PlusJakartaFontFamily,
                    fontSize = 11.sp,
                    color = if (route.isGpsDerived) EmeraldAccent else TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))
        if (selected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Selected",
                tint = CyanAccent,
                modifier = Modifier.size(22.dp)
            )
        } else {
            StatusTagPill(
                text = if (route.isGpsDerived) "GPS" else "PRESET",
                accentColor = if (route.isGpsDerived) EmeraldAccent else AmberAccent
            )
        }
    }
}
