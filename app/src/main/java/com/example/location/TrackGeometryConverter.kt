package com.example.location

import com.example.model.GeoWaypoint
import com.example.model.RealRoadTrack
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object TrackGeometryConverter {

    private const val EARTH_RADIUS_METERS = 6_371_000.0

    fun haversineMeters(a: GeoWaypoint, b: GeoWaypoint): Double {
        val dLat = Math.toRadians(b.lat - a.lat)
        val dLon = Math.toRadians(b.lon - a.lon)
        val lat1 = Math.toRadians(a.lat)
        val lat2 = Math.toRadians(b.lat)
        val sinDLat = sin(dLat / 2.0)
        val sinDLon = sin(dLon / 2.0)
        val h = sinDLat * sinDLat + cos(lat1) * cos(lat2) * sinDLon * sinDLon
        return 2.0 * EARTH_RADIUS_METERS * atan2(sqrt(h), sqrt((1.0 - h).coerceAtLeast(0.0)))
    }

    private fun bearingRad(a: GeoWaypoint, b: GeoWaypoint): Double {
        val lat1 = Math.toRadians(a.lat)
        val lat2 = Math.toRadians(b.lat)
        val dLon = Math.toRadians(b.lon - a.lon)
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        return atan2(y, x)
    }

    fun buildTrackFromWaypoints(
        id: String,
        roadName: String,
        regionLabel: String,
        roadClass: String,
        waypoints: List<GeoWaypoint>,
        isGpsDerived: Boolean
    ): RealRoadTrack {
        var totalMeters = 0.0
        for (i in 0 until waypoints.size - 1) {
            totalMeters += haversineMeters(waypoints[i], waypoints[i + 1])
        }

        val curves = MutableList(waypoints.size) { 0f }
        if (waypoints.size >= 3) {
            var prevBearing = bearingRad(waypoints[0], waypoints[1])
            for (i in 1 until waypoints.size - 1) {
                val nextBearing = bearingRad(waypoints[i], waypoints[i + 1])
                var delta = nextBearing - prevBearing
                while (delta > Math.PI) delta -= 2.0 * Math.PI
                while (delta < -Math.PI) delta += 2.0 * Math.PI
                // Scale turn angle into Nitro Rush road curve units [-4.2 .. 4.2]
                curves[i] = (Math.toDegrees(delta).toFloat() * 0.14f).coerceIn(-4.2f, 4.2f)
                prevBearing = nextBearing
            }
        }

        return RealRoadTrack(
            id = id,
            roadName = roadName,
            regionLabel = regionLabel,
            roadClass = roadClass,
            waypoints = waypoints,
            curvatureProfile = curves,
            lengthMeters = totalMeters.toInt().coerceAtLeast(1200),
            isGpsDerived = isGpsDerived
        )
    }
}
