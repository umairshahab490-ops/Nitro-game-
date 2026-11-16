package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.view.Surface
import android.view.WindowManager
import kotlin.math.abs

class TiltSteeringSensor(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val sensor: Sensor? =
        sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var registered = false
    @Volatile
    var steerValue: Float = 0f
        private set

    fun isAvailable(): Boolean = sensor != null

    fun start() {
        if (registered || sensor == null) return
        sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
        registered = true
    }

    fun stop() {
        if (!registered) return
        sensorManager?.unregisterListener(this)
        registered = false
        steerValue = 0f
    }

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val rotation = getDisplayRotation()

        // Map lateral gravity axis according to current display rotation
        val rawTilt = when (rotation) {
            Surface.ROTATION_90 -> y
            Surface.ROTATION_270 -> -y
            Surface.ROTATION_180 -> x
            else -> -x
        }

        // Normalize ~4.2 m/s^2 (~25 deg tilt) to full steering [-1..1] with small deadzone
        val normalized = (rawTilt / 4.2f).coerceIn(-1f, 1f)
        val withDeadzone = if (abs(normalized) < 0.08f) 0f else normalized
        steerValue += (withDeadzone - steerValue) * 0.32f
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun getDisplayRotation(): Int {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.display?.rotation ?: Surface.ROTATION_0
            } else {
                @Suppress("DEPRECATION")
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                @Suppress("DEPRECATION")
                wm?.defaultDisplay?.rotation ?: Surface.ROTATION_0
            }
        }.getOrDefault(Surface.ROTATION_0)
    }
}
