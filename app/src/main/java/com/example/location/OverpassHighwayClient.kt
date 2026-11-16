package com.example.location

import com.example.model.GeoWaypoint
import com.example.model.RealRoadTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

class OverpassHighwayClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun fetchNearbyHighways(lat: Double, lon: Double): Result<List<RealRoadTrack>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val query = String.format(
                    Locale.US,
                    """
                    [out:json][timeout:12];
                    way["highway"~"^(motorway|trunk|primary|secondary)$"](around:4500,%.5f,%.5f);
                    out geom 25;
                    """.trimIndent(),
                    lat,
                    lon
                )
                val body = FormBody.Builder()
                    .add("data", query)
                    .build()
                val request = Request.Builder()
                    .url("https://overpass-api.de/api/interpreter")
                    .header("User-Agent", "NitroRushAndroid/1.0")
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        error("Overpass API HTTP ${response.code}")
                    }
                    val raw = response.body?.string() ?: error("Empty Overpass response")
                    parseOverpassRoads(raw, lat, lon)
                }
            }
        }

    private fun parseOverpassRoads(json: String, centerLat: Double, centerLon: Double): List<RealRoadTrack> {
        val root = JSONObject(json)
        val elements = root.optJSONArray("elements") ?: return emptyList()
        val seenNames = HashSet<String>()
        val tracks = mutableListOf<RealRoadTrack>()

        for (i in 0 until elements.length()) {
            val el = elements.optJSONObject(i) ?: continue
            if (el.optString("type") != "way") continue
            val geom = el.optJSONArray("geometry") ?: continue
            if (geom.length() < 4) continue

            val tags = el.optJSONObject("tags")
            val highwayType = tags?.optString("highway").orEmpty().ifBlank { "highway" }
            val ref = tags?.optString("ref").orEmpty()
            val name = tags?.optString("name").orEmpty()
            val displayName = when {
                ref.isNotBlank() && name.isNotBlank() -> "$ref · $name"
                name.isNotBlank() -> name
                ref.isNotBlank() -> "$ref Highway"
                else -> "${highwayType.replaceFirstChar { it.uppercase() }} Sector #${el.optLong("id") % 100}"
            }
            if (!seenNames.add(displayName)) continue

            val waypoints = buildList {
                val step = maxOf(1, geom.length() / 24)
                for (j in 0 until geom.length() step step) {
                    val pt = geom.getJSONObject(j)
                    add(GeoWaypoint(pt.getDouble("lat"), pt.getDouble("lon")))
                }
                val last = geom.getJSONObject(geom.length() - 1)
                val lastWp = GeoWaypoint(last.getDouble("lat"), last.getDouble("lon"))
                if (lastOrNull() != lastWp) add(lastWp)
            }
            if (waypoints.size < 3) continue

            val region = String.format(Locale.US, "GPS %.3f, %.3f", centerLat, centerLon)
            val track = TrackGeometryConverter.buildTrackFromWaypoints(
                id = "osm_${el.optLong("id")}",
                roadName = displayName,
                regionLabel = region,
                roadClass = highwayType.replaceFirstChar { it.uppercase() },
                waypoints = waypoints,
                isGpsDerived = true
            )
            if (track.lengthMeters >= 350) {
                tracks.add(track)
            }
        }
        return tracks.sortedByDescending { it.lengthMeters }.take(8)
    }
}
