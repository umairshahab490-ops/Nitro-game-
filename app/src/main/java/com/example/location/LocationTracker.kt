package com.example.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LiveGpsReading(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val timestampMillis: Long
)

class LocationTracker(private val context: Context) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    private val _reading = MutableStateFlow<LiveGpsReading?>(null)
    val reading: StateFlow<LiveGpsReading?> = _reading.asStateFlow()

    private var callback: LocationCallback? = null

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    fun startUpdates() {
        if (!hasLocationPermission() || callback != null) return

        fusedClient.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                _reading.value = LiveGpsReading(
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    accuracyMeters = loc.accuracy,
                    timestampMillis = loc.time
                )
            }
        }

        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    _reading.value = LiveGpsReading(
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        accuracyMeters = loc.accuracy,
                        timestampMillis = loc.time
                    )
                }
            }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
            .setMinUpdateIntervalMillis(1500L)
            .build()

        val cb = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                _reading.value = LiveGpsReading(
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    accuracyMeters = loc.accuracy,
                    timestampMillis = loc.time
                )
            }
        }
        callback = cb
        fusedClient.requestLocationUpdates(request, cb, Looper.getMainLooper())
    }

    fun stopUpdates() {
        val cb = callback ?: return
        fusedClient.removeLocationUpdates(cb)
        callback = null
    }
}
