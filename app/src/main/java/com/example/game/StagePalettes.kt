package com.example.game

import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.random.Random

class StagePaletteState {
    companion object {
        val STAGE_NAMES = arrayOf("SUNSET HIGHWAY", "NEON NIGHT", "STORM RUN")

        private val baseHex = arrayOf(
            // Sunset Highway
            intArrayOf(
                0x35175E, 0xFF7A3D, 0xE08A5C, 0x6E3B52, 0x63344A,
                0x3A3348, 0x363043, 0xE8E4F0, 0xD24545, 0xF5E9D0,
                0x4A2A6E, 0x38204F
            ),
            // Neon Night
            intArrayOf(
                0x04060E, 0x152238, 0x101A30, 0x0D1626, 0x0B1320,
                0x232A3A, 0x202634, 0x9FB4D8, 0x2C3A5C, 0xCFE6FF,
                0x0E1830, 0x0A1224
            ),
            // Storm Run
            intArrayOf(
                0x262E38, 0x55606C, 0x5A6570, 0x33413F, 0x2E3A38,
                0x3C4450, 0x38404A, 0xC9D2DA, 0x8A2F3A, 0xE8F0F6,
                0x3A4652, 0x2C3640
            )
        )

        const val KEY_SKY_T = 0
        const val KEY_SKY_B = 1
        const val KEY_FOG = 2
        const val KEY_GRASS_A = 3
        const val KEY_GRASS_B = 4
        const val KEY_ROAD_A = 5
        const val KEY_ROAD_B = 6
        const val KEY_RUM_A = 7
        const val KEY_RUM_B = 8
        const val KEY_LANE = 9
        const val KEY_MFAR = 10
        const val KEY_MNEAR = 11
        private const val KEY_COUNT = 12
    }

    val stageW = floatArrayOf(1f, 0f, 0f)
    val currentColors = Array(KEY_COUNT) { Color.Black }
    val fogTable = Array(KEY_COUNT) { Array(17) { Color.Black } }

    private val curR = FloatArray(KEY_COUNT)
    private val curG = FloatArray(KEY_COUNT)
    private val curB = FloatArray(KEY_COUNT)

    init {
        rebuild()
    }

    fun reset() {
        stageW[0] = 1f
        stageW[1] = 0f
        stageW[2] = 0f
        rebuild()
    }

    fun blendToward(targetStage: Int, dt: Float) {
        var changed = false
        for (s in 0..2) {
            val target = if (s == targetStage) 1f else 0f
            val delta = target - stageW[s]
            if (abs(delta) > 0.001f) {
                val step = (dt / 2.5f)
                stageW[s] += delta.coerceIn(-step, step)
                changed = true
            }
        }
        if (changed) rebuild()
    }

    private fun rebuild() {
        for (k in 0 until KEY_COUNT) {
            var r = 0f
            var g = 0f
            var b = 0f
            for (s in 0..2) {
                val hex = baseHex[s][k]
                val w = stageW[s]
                r += ((hex shr 16) and 0xFF) * w
                g += ((hex shr 8) and 0xFF) * w
                b += (hex and 0xFF) * w
            }
            curR[k] = r
            curG[k] = g
            curB[k] = b
            currentColors[k] = Color(
                red = (r.toInt()).coerceIn(0, 255),
                green = (g.toInt()).coerceIn(0, 255),
                blue = (b.toInt()).coerceIn(0, 255)
            )
        }
        val fogR = curR[KEY_FOG]
        val fogG = curG[KEY_FOG]
        val fogB = curB[KEY_FOG]
        for (k in 0 until KEY_COUNT) {
            for (level in 0..16) {
                val t = level / 16f
                val r = (curR[k] + (fogR - curR[k]) * t).toInt().coerceIn(0, 255)
                val g = (curG[k] + (fogG - curG[k]) * t).toInt().coerceIn(0, 255)
                val b = (curB[k] + (fogB - curB[k]) * t).toInt().coerceIn(0, 255)
                fogTable[k][level] = Color(red = r, green = g, blue = b)
            }
        }
    }
}

data class StarPoint(val x: Float, val y: Float, val r: Float, val phase: Float)
data class SkyBuilding(val x: Float, val w: Float, val h: Float, val seedMask: Int)

object SkyBackdropData {
    val stars: List<StarPoint>
    val ridgeFar: FloatArray
    val ridgeNear: FloatArray
    val buildings: List<SkyBuilding>

    init {
        val rng = Random(1337)
        stars = List(90) {
            StarPoint(
                x = rng.nextFloat(),
                y = rng.nextFloat() * 0.42f,
                r = 0.8f + rng.nextFloat() * 1.4f,
                phase = rng.nextFloat() * 6.28f
            )
        }
        ridgeFar = FloatArray(65)
        var ry = 0.55f
        for (i in 0..64) {
            ry = (ry + (rng.nextFloat() - 0.5f) * 0.16f).coerceIn(0.25f, 0.8f)
            ridgeFar[i] = ry
        }
        ridgeNear = FloatArray(65)
        ry = 0.7f
        for (i in 0..64) {
            ry = (ry + (rng.nextFloat() - 0.5f) * 0.12f).coerceIn(0.5f, 0.92f)
            ridgeNear[i] = ry
        }
        buildings = List(36) { i ->
            SkyBuilding(
                x = i / 36f + rng.nextFloat() * 0.015f,
                w = 0.012f + rng.nextFloat() * 0.02f,
                h = 0.06f + rng.nextFloat() * 0.16f,
                seedMask = rng.nextInt(999)
            )
        }
    }
}
