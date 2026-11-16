package com.example.model

data class GeoWaypoint(
    val lat: Double,
    val lon: Double
)

data class RealRoadTrack(
    val id: String,
    val roadName: String,
    val regionLabel: String,
    val roadClass: String,
    val waypoints: List<GeoWaypoint>,
    val curvatureProfile: List<Float>,
    val lengthMeters: Int,
    val isGpsDerived: Boolean
)

data class GpsLocationState(
    val permissionGranted: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Float? = null,
    val liveGpsActive: Boolean = false,
    val isScanningRoads: Boolean = false,
    val statusMessage: String = "Tap GPS Scan to load real highways near you"
)

object PresetWorldRoutes {
    val presets: List<RealRoadTrack> = listOf(
        RealRoadTrack(
            id = "preset_pch",
            roadName = "CA-1 · Pacific Coast Hwy",
            regionLabel = "Big Sur, California",
            roadClass = "Primary Highway",
            waypoints = listOf(
                GeoWaypoint(36.3715, -121.9017),
                GeoWaypoint(36.3640, -121.8980),
                GeoWaypoint(36.3575, -121.8915),
                GeoWaypoint(36.3510, -121.8935),
                GeoWaypoint(36.3442, -121.8890),
                GeoWaypoint(36.3370, -121.8812),
                GeoWaypoint(36.3298, -121.8840),
                GeoWaypoint(36.3220, -121.8780),
                GeoWaypoint(36.3145, -121.8705),
                GeoWaypoint(36.3070, -121.8690)
            ),
            curvatureProfile = listOf(
                0f, 1.8f, 3.2f, -2.5f, -3.4f, 2.2f, 4.1f, -3.1f, 1.5f, 0f
            ),
            lengthMeters = 7400,
            isGpsDerived = false
        ),
        RealRoadTrack(
            id = "preset_wangan",
            roadName = "Route B · Wangan Expressway",
            regionLabel = "Tokyo Bay, Japan",
            roadClass = "Motorway",
            waypoints = listOf(
                GeoWaypoint(35.6215, 139.7760),
                GeoWaypoint(35.6270, 139.7845),
                GeoWaypoint(35.6318, 139.7950),
                GeoWaypoint(35.6352, 139.8060),
                GeoWaypoint(35.6378, 139.8180),
                GeoWaypoint(35.6410, 139.8285),
                GeoWaypoint(35.6462, 139.8368),
                GeoWaypoint(35.6520, 139.8440)
            ),
            curvatureProfile = listOf(
                0f, 0.8f, 1.6f, 0.5f, -1.2f, -2.1f, 1.4f, 0f
            ),
            lengthMeters = 8100,
            isGpsDerived = false
        ),
        RealRoadTrack(
            id = "preset_autobahn",
            roadName = "A95 · Alpine Autobahn",
            regionLabel = "Bavaria, Germany",
            roadClass = "Motorway",
            waypoints = listOf(
                GeoWaypoint(47.6512, 11.2140),
                GeoWaypoint(47.6440, 11.2090),
                GeoWaypoint(47.6365, 11.2015),
                GeoWaypoint(47.6285, 11.1980),
                GeoWaypoint(47.6200, 11.2035),
                GeoWaypoint(47.6122, 11.2115),
                GeoWaypoint(47.6048, 11.2145),
                GeoWaypoint(47.5970, 11.2080)
            ),
            curvatureProfile = listOf(
                0f, -2.4f, 2.8f, 3.6f, -3.8f, -1.9f, 2.5f, 0f
            ),
            lengthMeters = 6800,
            isGpsDerived = false
        )
    )
}
