package com.example.game

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import com.example.model.GameState
import com.example.model.PickupType
import com.example.model.TrafficMode
import com.example.model.VehicleType
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

class HighwayCanvasRenderer {

    private val reusableQuadPath = Path()
    private val reusableTrianglePath = Path()
    private val reusableRidgePath = Path()

    private val vehicleBuckets = Array(GameEngine.DRAW) { ArrayList<TrafficVehicle>(4) }
    private val pickupBuckets = Array(GameEngine.DRAW) { ArrayList<HighwayPickup>(2) }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
    }

    private val shadowTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        color = 0xAA000000.toInt()
    }

    fun render(scope: DrawScope, engine: GameEngine, dt: Float) = with(scope) {
        val w = size.width
        val h = size.height
        if (w <= 1f || h <= 1f) return@with

        val horizon = h * 0.5f
        var shakeX = 0f
        var shakeY = 0f
        if (engine.shakeTimer > 0f) {
            val a = engine.shakeAmp * (engine.shakeTimer / 0.6f)
            shakeX = (Random.nextFloat() - 0.5f) * 2f * a
            shakeY = (Random.nextFloat() - 0.5f) * 2f * a
        } else if (engine.nitroOn) {
            shakeX = (Random.nextFloat() - 0.5f) * 2.4f
            shakeY = (Random.nextFloat() - 0.5f) * 2.4f
        }

        withTransform({ translate(shakeX, shakeY) }) {
            drawSky(engine, w, h, horizon)
            val base = drawRoad(engine, w, h)
            drawSprites(engine, base, w)
            val bikeInfo = drawPlayerBike(engine, w, h)
            drawHeadlight(bikeInfo, engine.palettes.stageW[1], w, h)
            drawRain(engine, dt, w, h)
            drawFx(engine, dt, w, h)
        }
    }

    private fun DrawScope.drawSky(engine: GameEngine, w: Float, h: Float, horizon: Float) {
        val colors = engine.palettes.currentColors
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    colors[StagePaletteState.KEY_SKY_T],
                    colors[StagePaletteState.KEY_SKY_B]
                ),
                startY = 0f,
                endY = horizon + h * 0.1f
            ),
            topLeft = Offset.Zero,
            size = Size(w, horizon + h * 0.1f)
        )
        drawRect(
            color = colors[StagePaletteState.KEY_FOG],
            topLeft = Offset(0f, horizon),
            size = Size(w, h - horizon)
        )

        val sunW = engine.palettes.stageW[0]
        val nightW = engine.palettes.stageW[1]
        val rainW = engine.palettes.stageW[2]

        if (nightW > 0.03f) {
            for (st in SkyBackdropData.stars) {
                val alpha = nightW * (1f - rainW) * (0.4f + 0.6f * abs(sin(engine.simClockMs * 0.001f + st.phase)))
                drawRect(
                    color = Color(0xFFDFE8FF).copy(alpha = alpha.coerceIn(0f, 1f)),
                    topLeft = Offset(st.x * w, st.y * horizon),
                    size = Size(st.r, st.r)
                )
            }
        }

        val sx = w * 0.5f - engine.skyOffset * 0.08f
        val sy = horizon - h * 0.055f
        if (sunW > 0.03f) {
            val haloR = h * 0.16f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(1f, 0.745f, 0.47f, 0.85f * sunW),
                        Color(1f, 0.55f, 0.27f, 0f)
                    ),
                    center = Offset(sx, sy),
                    radius = haloR
                ),
                radius = haloR,
                center = Offset(sx, sy)
            )
            drawCircle(
                color = Color(0xFFFFCF7A).copy(alpha = sunW.coerceIn(0f, 1f)),
                radius = h * 0.075f,
                center = Offset(sx, sy)
            )
        }

        if (nightW > 0.03f) {
            val mx = w * 0.72f - engine.skyOffset * 0.05f
            val my = horizon - h * 0.24f
            drawCircle(
                color = Color(0xFFE8EEFB).copy(alpha = nightW.coerceIn(0f, 1f)),
                radius = h * 0.032f,
                center = Offset(mx, my)
            )
            drawCircle(
                color = colors[StagePaletteState.KEY_SKY_T].copy(alpha = nightW.coerceIn(0f, 1f)),
                radius = h * 0.026f,
                center = Offset(mx + h * 0.014f, my - h * 0.007f)
            )
        }

        drawRidge(
            pts = SkyBackdropData.ridgeFar,
            off = engine.skyOffset * 0.25f + engine.playerX * 18f,
            col = colors[StagePaletteState.KEY_MFAR],
            amp = h * 0.2f,
            w = w,
            horizon = horizon
        )

        val cityA = maxOf(nightW, rainW * 0.5f)
        if (cityA > 0.04f) {
            val span = w * 2f
            val o = (((engine.skyOffset * 0.45f + engine.playerX * 30f) % span) + span) % span
            val nearCol = colors[StagePaletteState.KEY_MNEAR].copy(alpha = cityA.coerceIn(0f, 1f))
            val winCol = Color(1f, 0.84f, 0.43f, (0.75f * nightW).coerceIn(0f, 1f))
            for (b in SkyBackdropData.buildings) {
                var bx = b.x * span - o
                if (bx < -w * 0.1f) bx += span
                val bw = b.w * w
                val bh = b.h * h
                drawRect(color = nearCol, topLeft = Offset(bx, horizon - bh), size = Size(bw, bh))
                if (nightW > 0.1f) {
                    for (k in 0..4) {
                        if (((b.seedMask shr k) and 1) == 0) continue
                        drawRect(
                            color = winCol,
                            topLeft = Offset(
                                bx + bw * 0.18f + (k % 2) * bw * 0.45f,
                                horizon - bh + bh * 0.15f + ((k * 0.19f) % 0.7f) * bh
                            ),
                            size = Size(bw * 0.16f, bh * 0.07f)
                        )
                    }
                }
            }
        }

        drawRidge(
            pts = SkyBackdropData.ridgeNear,
            off = engine.skyOffset * 0.55f + engine.playerX * 36f,
            col = colors[StagePaletteState.KEY_MNEAR],
            amp = h * 0.11f,
            w = w,
            horizon = horizon
        )
    }

    private fun DrawScope.drawRidge(
        pts: FloatArray,
        off: Float,
        col: Color,
        amp: Float,
        w: Float,
        horizon: Float
    ) {
        reusableRidgePath.reset()
        reusableRidgePath.moveTo(0f, horizon + 2f)
        val span = w * 2f
        val o = ((off % span) + span) % span
        for (k in 0..64) {
            var x = (k / 64f) * span - o
            if (x < -w * 0.1f) x += span
            reusableRidgePath.lineTo(x, horizon - pts[k] * amp)
        }
        reusableRidgePath.lineTo(w, horizon + 2f)
        reusableRidgePath.close()
        drawPath(reusableRidgePath, col)
    }

    private fun DrawScope.drawRoad(engine: GameEngine, w: Float, h: Float): Int {
        val segs = engine.segments
        val nCount = segs.size
        val base = (floor(engine.position / GameEngine.SEG).toInt() % nCount + nCount) % nCount
        val basePct = (engine.position % GameEngine.SEG) / GameEngine.SEG
        val pIdx = (floor((engine.position + GameEngine.PLAYERZ) / GameEngine.SEG).toInt() % nCount + nCount) % nCount
        val pSeg = segs[pIdx]
        val pPct = ((engine.position + GameEngine.PLAYERZ) % GameEngine.SEG) / GameEngine.SEG
        val playerY = lerp(pSeg.wy1, pSeg.wy2, pPct)
        val camY = playerY + GameEngine.CAMH
        var x = 0f
        var dx = -(segs[base].curve * basePct)
        var maxy = h
        val camXBase = engine.playerX * GameEngine.ROADW
        val fogTab = engine.palettes.fogTable
        val twoWay = engine.trafficMode == TrafficMode.TWO_WAY

        for (n in 0 until GameEngine.DRAW) {
            val i = (base + n) % nCount
            val s = segs[i]
            val camZ = engine.position - (if (i < base) engine.trackLength else 0f)
            val dz1 = s.z1 - camZ
            val dz2 = s.z2 - camZ
            s.ok = dz1 > 1f
            if (s.ok) {
                val sc1 = GameEngine.DEPTH / dz1
                val sc2 = GameEngine.DEPTH / dz2
                s.x1 = w * 0.5f - sc1 * (camXBase - x) * w * 0.5f
                s.y1 = h * 0.5f - sc1 * (s.wy1 - camY) * h * 0.5f
                s.w1 = sc1 * GameEngine.ROADW * w * 0.5f
                s.x2 = w * 0.5f - sc2 * (camXBase - x - dx) * w * 0.5f
                s.y2 = h * 0.5f - sc2 * (s.wy2 - camY) * h * 0.5f
                s.w2 = sc2 * GameEngine.ROADW * w * 0.5f
            }
            x += dx
            dx += s.curve
            s.clip = maxy
            val normN = n.toFloat() / GameEngine.DRAW
            s.fog = kotlin.math.round(16f * (1f - kotlin.math.exp(-normN * normN * 4.5f))).toInt().coerceIn(0, 16)
            s.stripe = (i / 3) % 2
            s.vis = false
            if (!s.ok || s.y2 >= s.y1 || s.y2 >= maxy) continue
            s.vis = true

            val f = s.fog
            val st = s.stripe != 0
            val grass = fogTab[if (st) StagePaletteState.KEY_GRASS_A else StagePaletteState.KEY_GRASS_B][f]
            val road = if (i < 2) {
                fogTab[StagePaletteState.KEY_RUM_A][f]
            } else {
                fogTab[if (st) StagePaletteState.KEY_ROAD_A else StagePaletteState.KEY_ROAD_B][f]
            }
            val rum = fogTab[if (st) StagePaletteState.KEY_RUM_A else StagePaletteState.KEY_RUM_B][f]
            val lane = fogTab[StagePaletteState.KEY_LANE][f]

            drawRect(color = grass, topLeft = Offset(0f, s.y2), size = Size(w, s.y1 - s.y2 + 1.5f))
            val r1 = s.w1 * 0.12f
            val r2 = s.w2 * 0.12f
            drawPoly(s.x1 - s.w1 - r1, s.y1, s.x1 - s.w1, s.y1, s.x2 - s.w2, s.y2, s.x2 - s.w2 - r2, s.y2, rum)
            drawPoly(s.x1 + s.w1 + r1, s.y1, s.x1 + s.w1, s.y1, s.x2 + s.w2, s.y2, s.x2 + s.w2 + r2, s.y2, rum)
            drawPoly(s.x1 - s.w1, s.y1, s.x1 + s.w1, s.y1, s.x2 + s.w2, s.y2, s.x2 - s.w2, s.y2, road)

            if (i % 4 < 2 && i >= 2) {
                for (k in 1..2) {
                    val lx1 = s.x1 - s.w1 + (2f * s.w1 / 3f) * k
                    val lx2 = s.x2 - s.w2 + (2f * s.w2 / 3f) * k
                    val lw1 = maxOf(0.5f, s.w1 * 0.018f)
                    val lw2 = maxOf(0.3f, s.w2 * 0.018f)
                    val laneCol = if (twoWay && k == 1) Color(0xFFFFD34D).copy(alpha = lane.alpha) else lane
                    drawPoly(lx1 - lw1, s.y1, lx1 + lw1, s.y1, lx2 + lw2, s.y2, lx2 - lw2, s.y2, laneCol)
                }
            }
            maxy = s.y2
        }
        return base
    }

    private fun DrawScope.drawPoly(
        x1: Float, y1: Float, x2: Float, y2: Float,
        x3: Float, y3: Float, x4: Float, y4: Float,
        color: Color
    ) {
        reusableQuadPath.reset()
        reusableQuadPath.moveTo(x1, y1)
        reusableQuadPath.lineTo(x2, y2)
        reusableQuadPath.lineTo(x3, y3)
        reusableQuadPath.lineTo(x4, y4)
        reusableQuadPath.close()
        drawPath(reusableQuadPath, color)
    }

    private fun DrawScope.drawSprites(engine: GameEngine, base: Int, w: Float) {
        val segs = engine.segments
        val nCount = segs.size
        val nightW = engine.palettes.stageW[1]
        val sunW = engine.palettes.stageW[0]

        for (n in 0 until GameEngine.DRAW) {
            vehicleBuckets[n].clear()
            pickupBuckets[n].clear()
        }
        for (c in engine.vehicles) {
            val si = (floor(c.z / GameEngine.SEG).toInt() % nCount + nCount) % nCount
            val dist = (si - base + nCount) % nCount
            if (dist < GameEngine.DRAW) vehicleBuckets[dist].add(c)
        }
        for (p in engine.pickups) {
            if (!p.active) continue
            val si = (floor(p.z / GameEngine.SEG).toInt() % nCount + nCount) % nCount
            val dist = (si - base + nCount) % nCount
            if (dist < GameEngine.DRAW) pickupBuckets[dist].add(p)
        }

        for (n in GameEngine.DRAW - 1 downTo 1) {
            val i = (base + n) % nCount
            val s = segs[i]
            if (!s.ok) continue

            for (sp in s.sprites) {
                drawRoadside(sp, s, nightW, sunW, w)
            }
            if (i == 0) drawGantry(s, w)

            val pBucket = pickupBuckets[n]
            for (b in pBucket.indices) {
                val p = pBucket[b]
                val pct = (p.z % GameEngine.SEG) / GameEngine.SEG
                val px = lerp(s.x1, s.x2, pct) + lerp(s.w1, s.w2, pct) * p.offset
                val py = lerp(s.y1, s.y2, pct)
                val pw = lerp(s.w1, s.w2, pct) * 0.16f
                if (pw < 3f) continue
                clipped(s.clip, py - pw * 2f, py, w) {
                    drawPickup(p, px, py, pw, engine.simClockMs)
                }
            }

            val vBucket = vehicleBuckets[n]
            for (b in vBucket.indices) {
                val cc = vBucket[b]
                val pct = (cc.z % GameEngine.SEG) / GameEngine.SEG
                val cx = lerp(s.x1, s.x2, pct) + lerp(s.w1, s.w2, pct) * cc.offset
                val cy = lerp(s.y1, s.y2, pct)
                val cw = lerp(s.w1, s.w2, pct) * cc.widthNorm
                if (cw < 2f) continue
                clipped(s.clip, cy - cw * cc.aspect, cy, w) {
                    drawVehicle(cc, cx, cy, cw, nightW)
                }
            }
        }
    }

    private inline fun DrawScope.clipped(
        clipY: Float,
        topY: Float,
        botY: Float,
        w: Float,
        block: () -> Unit
    ) {
        if (topY >= clipY) return
        if (botY > clipY) {
            clipRect(left = 0f, top = 0f, right = w, bottom = clipY) { block() }
        } else {
            block()
        }
    }

    private fun DrawScope.drawPickup(p: HighwayPickup, cx: Float, groundY: Float, r: Float, clockMs: Float) {
        val bob = sin(clockMs * 0.006f + p.bobPhase) * r * 0.22f
        val cy = groundY - r * 1.1f + bob
        drawOval(
            color = Color.Black.copy(alpha = 0.35f),
            topLeft = Offset(cx - r * 0.55f, groundY - r * 0.12f),
            size = Size(r * 1.1f, r * 0.24f)
        )

        val (coreColor, glyph) = when (p.type) {
            PickupType.USD_CASH -> Color(0xFF7EF07E) to "$"
            PickupType.NITRO_CELL -> Color(0xFF22E6FF) to "⚡"
            PickupType.SHIELD -> Color(0xFF60A5FA) to "S"
        }

        drawCircle(
            color = coreColor.copy(alpha = 0.28f),
            radius = r * 1.15f,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = Color(0xFF0E1629),
            radius = r * 0.72f,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = coreColor,
            radius = r * 0.72f,
            center = Offset(cx, cy),
            style = Stroke(width = maxOf(1.5f, r * 0.18f))
        )

        textPaint.color = android.graphics.Color.WHITE
        textPaint.textSize = maxOf(8f, r * 0.9f)
        drawContext.canvas.nativeCanvas.drawText(glyph, cx, cy + textPaint.textSize * 0.35f, textPaint)
    }

    private fun DrawScope.drawVehicle(
        c: TrafficVehicle,
        cx: Float,
        cy: Float,
        cw: Float,
        nightW: Float
    ) {
        val ch = cw * c.aspect
        drawOval(
            color = Color.Black.copy(alpha = 0.35f),
            topLeft = Offset(cx - cw * 0.55f, cy - cw * 0.1f),
            size = Size(cw * 1.1f, cw * 0.2f)
        )

        val bodyCol = Color(c.bodyColor)
        val lightColor = when {
            c.isOncoming -> Color(0xFFFFF6C9)
            nightW > 0.3f -> Color(0xFFFF5A4D)
            else -> Color(0xFFC0392B)
        }

        when (c.type) {
            VehicleType.TRUCK -> {
                drawRect(Color(0xFF20242E), Offset(cx - cw * 0.46f, cy - ch * 0.16f), Size(cw * 0.92f, ch * 0.16f))
                drawRoundRect(bodyCol, Offset(cx - cw * 0.5f, cy - ch), Size(cw, ch * 0.86f), CornerRadius(cw * 0.04f))
                drawRect(Color.White.copy(alpha = 0.14f), Offset(cx - cw * 0.5f, cy - ch * 0.62f), Size(cw, ch * 0.07f))
                drawRect(Color(0xFF12151D), Offset(cx - cw * 0.44f, cy - ch * 0.13f), Size(cw * 0.16f, ch * 0.13f))
                drawRect(Color(0xFF12151D), Offset(cx + cw * 0.28f, cy - ch * 0.13f), Size(cw * 0.16f, ch * 0.13f))
                drawRect(lightColor, Offset(cx - cw * 0.42f, cy - ch * 0.2f), Size(cw * 0.12f, ch * 0.06f))
                drawRect(lightColor, Offset(cx + cw * 0.30f, cy - ch * 0.2f), Size(cw * 0.12f, ch * 0.06f))
            }
            VehicleType.CAR -> {
                drawRect(Color(0xFF14171F), Offset(cx - cw * 0.48f, cy - ch * 0.22f), Size(cw * 0.16f, ch * 0.22f))
                drawRect(Color(0xFF14171F), Offset(cx + cw * 0.32f, cy - ch * 0.22f), Size(cw * 0.16f, ch * 0.22f))
                drawRoundRect(bodyCol, Offset(cx - cw * 0.5f, cy - ch * 0.62f), Size(cw, ch * 0.6f), CornerRadius(cw * 0.09f))
                drawRoundRect(bodyCol, Offset(cx - cw * 0.36f, cy - ch), Size(cw * 0.72f, ch * 0.5f), CornerRadius(cw * 0.1f))
                drawRoundRect(Color(0xFF0E1420), Offset(cx - cw * 0.3f, cy - ch * 0.95f), Size(cw * 0.6f, ch * 0.34f), CornerRadius(cw * 0.06f))
                drawRoundRect(lightColor, Offset(cx - cw * 0.44f, cy - ch * 0.5f), Size(cw * 0.18f, ch * 0.1f), CornerRadius(2f))
                drawRoundRect(lightColor, Offset(cx + cw * 0.26f, cy - ch * 0.5f), Size(cw * 0.18f, ch * 0.1f), CornerRadius(2f))
                drawRect(Color(0xFFD8DCE8), Offset(cx - cw * 0.12f, cy - ch * 0.36f), Size(cw * 0.24f, ch * 0.1f))
            }
            VehicleType.BIKE -> {
                drawRoundRect(Color(0xFF14171F), Offset(cx - cw * 0.18f, cy - ch * 0.3f), Size(cw * 0.36f, ch * 0.3f), CornerRadius(cw * 0.1f))
                drawRoundRect(bodyCol, Offset(cx - cw * 0.34f, cy - ch * 0.62f), Size(cw * 0.68f, ch * 0.36f), CornerRadius(cw * 0.12f))
                drawRoundRect(Color(0xFF1B2230), Offset(cx - cw * 0.24f, cy - ch * 0.95f), Size(cw * 0.48f, ch * 0.4f), CornerRadius(cw * 0.12f))
                drawCircle(Color(0xFFE8ECF5), radius = cw * 0.2f, center = Offset(cx, cy - ch * 0.98f))
                drawRect(lightColor, Offset(cx - cw * 0.12f, cy - ch * 0.5f), Size(cw * 0.24f, ch * 0.07f))
            }
        }

        if (c.isOncoming) {
            // Headlight beam cones for oncoming two-way traffic
            drawOval(
                color = Color(0xFFFFF4BD).copy(alpha = 0.24f),
                topLeft = Offset(cx - cw * 0.65f, cy - ch * 0.45f),
                size = Size(cw * 1.3f, ch * 0.32f)
            )
        } else if (nightW > 0.25f) {
            drawOval(
                color = Color(0xFFFF5A4D).copy(alpha = nightW * 0.35f),
                topLeft = Offset(cx - cw * 0.5f, cy - ch * 0.52f),
                size = Size(cw, ch * 0.24f)
            )
        }
    }

    private fun DrawScope.drawRoadside(
        sp: RoadsideSprite,
        s: TrackSegment,
        nightW: Float,
        sunW: Float,
        w: Float
    ) {
        val cx = s.x1 + s.w1 * sp.offset
        val cy = s.y1
        val u = s.w1
        when (sp.kind) {
            RoadsideSprite.POST -> {
                val h = u * 0.14f
                val pw = maxOf(1f, u * 0.016f)
                clipped(s.clip, cy - h, cy, w) {
                    drawRect(Color(0xFFCFD6E4), Offset(cx - pw * 0.5f, cy - h), Size(pw, h))
                    val cap = if (nightW > 0.3f) Color(0xFFFFD34D) else Color(0xFFD24545)
                    drawRect(cap, Offset(cx - pw * 0.5f, cy - h), Size(pw, h * 0.25f))
                }
            }
            RoadsideSprite.LAMP -> {
                val h = u * 0.85f
                val lw = maxOf(1f, u * 0.02f)
                val dir = if (sp.offset < 0f) 1f else -1f
                clipped(s.clip, cy - h, cy, w) {
                    drawRect(Color(0xFF3A4152), Offset(cx - lw * 0.5f, cy - h), Size(lw, h))
                    val armStart = if (dir > 0f) cx else cx + dir * u * 0.14f
                    drawRect(Color(0xFF3A4152), Offset(armStart, cy - h), Size(u * 0.14f, maxOf(1f, u * 0.015f)))
                    val lx = cx + dir * u * 0.14f
                    val bulbCol = if (nightW > 0.2f) Color(0xFFFFE9A8) else Color(0xFF8A93A6)
                    drawRect(bulbCol, Offset(lx - u * 0.03f, cy - h), Size(u * 0.06f, maxOf(1.5f, u * 0.025f)))
                    if (nightW > 0.2f) {
                        val gr = u * 0.2f
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(1f, 0.91f, 0.66f, (0.5f * nightW).coerceIn(0f, 1f)),
                                    Color.Transparent
                                ),
                                center = Offset(lx, cy - h),
                                radius = gr
                            ),
                            radius = gr,
                            center = Offset(lx, cy - h)
                        )
                    }
                }
            }
            RoadsideSprite.PALM -> {
                val h = u * 0.75f
                val tw = maxOf(1.2f, u * 0.03f)
                clipped(s.clip, cy - h, cy, w) {
                    val trunkCol = if (sunW > 0.4f) Color(0xFF2C1A38) else Color(0xFF22303A)
                    val leafCol = if (sunW > 0.4f) Color(0xFF3D2450) else Color(0xFF2C4048)
                    val topX = cx + tw * 1.2f
                    val topY = cy - h
                    val trunkPath = Path().apply {
                        moveTo(cx, cy)
                        quadraticTo(cx + tw * 2f, cy - h * 0.6f, topX, topY)
                    }
                    drawPath(trunkPath, trunkCol, style = Stroke(width = tw))
                    for (k in 0..4) {
                        val a = -0.4f - k * 0.5f
                        val leafPath = Path().apply {
                            moveTo(topX, topY)
                            quadraticTo(
                                topX + cos(a) * h * 0.3f,
                                topY + sin(a) * h * 0.22f,
                                topX + cos(a) * h * 0.42f,
                                topY + sin(a) * h * 0.4f + h * 0.08f
                            )
                        }
                        drawPath(leafPath, leafCol, style = Stroke(width = maxOf(1f, tw * 0.7f)))
                    }
                }
            }
            RoadsideSprite.BILL -> {
                val h = u * 0.6f
                val bw = u * 0.5f
                clipped(s.clip, cy - h, cy, w) {
                    drawRect(Color(0xFF3A4152), Offset(cx - bw * 0.06f, cy - h * 0.5f), Size(bw * 0.12f, h * 0.5f))
                    drawRoundRect(
                        Color(0xFF141C2C),
                        Offset(cx - bw * 0.5f, cy - h),
                        Size(bw, h * 0.55f),
                        CornerRadius(u * 0.02f)
                    )
                    val neon = if (nightW > 0.3f) Color(0xFF22E6FF) else Color(0xFFFF2D78)
                    drawRoundRect(
                        neon,
                        Offset(cx - bw * 0.5f, cy - h),
                        Size(bw, h * 0.55f),
                        CornerRadius(u * 0.02f),
                        style = Stroke(width = maxOf(1f, u * 0.012f))
                    )
                    if (u > 40f) {
                        textPaint.color = if (nightW > 0.3f) 0xFF22E6FF.toInt() else 0xFFFF2D78.toInt()
                        textPaint.textSize = maxOf(4f, u * 0.09f)
                        drawContext.canvas.nativeCanvas.drawText("NITRO", cx, cy - h * 0.66f, textPaint)
                    }
                }
            }
        }
    }

    private fun DrawScope.drawGantry(s: TrackSegment, w: Float) {
        val u = s.w1
        if (u < 3f) return
        val ph = u * 0.75f
        val pw = maxOf(1.5f, u * 0.05f)
        val lx = s.x1 - u * 1.18f
        val rx = s.x1 + u * 1.18f
        val top = s.y1 - ph
        clipped(s.clip, top, s.y1, w) {
            drawRect(Color(0xFFD24545), Offset(lx - pw * 0.5f, top), Size(pw, ph))
            drawRect(Color(0xFFD24545), Offset(rx - pw * 0.5f, top), Size(pw, ph))
            val bh = u * 0.16f
            val cells = 10
            val cw2 = (rx - lx) / cells
            for (k in 0 until cells) {
                drawRect(
                    if (k % 2 != 0) Color(0xFFE8ECF5) else Color(0xFF12151D),
                    Offset(lx + k * cw2, top),
                    Size(cw2 + 0.5f, bh)
                )
            }
            if (u > 50f) {
                textPaint.color = 0xFF7EF07E.toInt()
                textPaint.textSize = maxOf(4f, u * 0.07f)
                drawContext.canvas.nativeCanvas.drawText("CHECKPOINT", s.x1, top + bh * 0.72f, textPaint)
            }
        }
    }

    private data class BikeDrawInfo(val px: Float, val py: Float, val bh: Float, val bw: Float)

    private fun DrawScope.drawPlayerBike(engine: GameEngine, w: Float, h: Float): BikeDrawInfo? {
        val pct = engine.speed / GameEngine.MAXSP
        val bh = (h * 0.155f).coerceIn(88f, 180f)
        val bw = bh * 0.62f
        val bounce = sin(engine.simClockMs * 0.03f) * pct * 2f +
            (if (engine.offroad && pct > 0.2f) (Random.nextFloat() - 0.5f) * 5f else 0f)
        val px = w * 0.5f + engine.steerVisual * w * 0.02f
        val py = h * 0.965f + bounce
        val pSeg = engine.findSeg(engine.position + GameEngine.PLAYERZ)
        var lean = engine.steerVisual * 0.42f + pSeg.curve * pct * 0.05f
        if (engine.state == GameState.DYING) lean += engine.wreckRot

        drawOval(
            color = Color.Black.copy(alpha = 0.4f),
            topLeft = Offset(px - bw * 0.8f, py + 2f - bw * 0.15f),
            size = Size(bw * 1.6f, bw * 0.3f)
        )

        val info = BikeDrawInfo(px, py, bh, bw)
        if (engine.invulnTimer > 0f && floor(engine.invulnTimer * 10f).toInt() % 2 == 0 && engine.state == GameState.RUNNING) {
            return info
        }

        val bikePrimary = Color(engine.equippedBike.primaryColor)
        val bikeTrim = Color(engine.equippedBike.trimColor)
        val bikeAccent = Color(engine.equippedBike.accentColor)

        withTransform({
            translate(px, py)
            rotate(degrees = Math.toDegrees(lean.toDouble()).toFloat(), pivot = Offset.Zero)
        }) {
            if (engine.nitroOn) {
                val fl = bh * (0.3f + Random.nextFloat() * 0.18f)
                for (e in 0..1) {
                    val ex = if (e == 1) bw * 0.26f else -bw * 0.26f
                    reusableTrianglePath.reset()
                    reusableTrianglePath.moveTo(ex - bw * 0.09f, -bh * 0.16f)
                    reusableTrianglePath.lineTo(ex + bw * 0.09f, -bh * 0.16f)
                    reusableTrianglePath.lineTo(ex, fl * 0.6f)
                    reusableTrianglePath.close()
                    drawPath(
                        path = reusableTrianglePath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0.55f, 0.94f, 1f, 0.95f),
                                Color(1f, 0.59f, 0.24f, 0.8f),
                                Color.Transparent
                            ),
                            startY = -bh * 0.16f,
                            endY = fl * 0.6f
                        )
                    )
                }
            }

            // Rear tire
            drawRoundRect(
                color = Color(0xFF10131B),
                topLeft = Offset(-bw * 0.17f, -bh * 0.36f),
                size = Size(bw * 0.34f, bh * 0.36f),
                cornerRadius = CornerRadius(bw * 0.12f)
            )
            drawLine(Color(0xFF2A3040), Offset(-bw * 0.13f, -bh * 0.28f), Offset(bw * 0.13f, -bh * 0.28f), maxOf(1f, bw * 0.03f))
            drawLine(Color(0xFF2A3040), Offset(-bw * 0.13f, -bh * 0.16f), Offset(bw * 0.13f, -bh * 0.16f), maxOf(1f, bw * 0.03f))

            // Dual exhaust pipes
            drawOval(Color(0xFF8A93A6), Offset(-bw * 0.34f, -bh * 0.25f), Size(bw * 0.16f, bh * 0.1f))
            drawOval(Color(0xFF8A93A6), Offset(bw * 0.18f, -bh * 0.25f), Size(bw * 0.16f, bh * 0.1f))

            // Fairing in equipped real-world bike livery
            reusableQuadPath.reset()
            reusableQuadPath.moveTo(-bw * 0.36f, -bh * 0.38f)
            reusableQuadPath.lineTo(bw * 0.36f, -bh * 0.38f)
            reusableQuadPath.lineTo(bw * 0.26f, -bh * 0.62f)
            reusableQuadPath.lineTo(-bw * 0.26f, -bh * 0.62f)
            reusableQuadPath.close()
            drawPath(reusableQuadPath, bikePrimary)

            reusableQuadPath.reset()
            reusableQuadPath.moveTo(-bw * 0.26f, -bh * 0.62f)
            reusableQuadPath.lineTo(bw * 0.26f, -bh * 0.62f)
            reusableQuadPath.lineTo(bw * 0.18f, -bh * 0.70f)
            reusableQuadPath.lineTo(-bw * 0.18f, -bh * 0.70f)
            reusableQuadPath.close()
            drawPath(reusableQuadPath, bikeTrim)

            // Taillight
            val braking = engine.touchBrake
            val tailCol = if (braking) Color(0xFFFF6A5E) else Color(0xFFA02020)
            drawRoundRect(tailCol, Offset(-bw * 0.13f, -bh * 0.56f), Size(bw * 0.26f, bh * 0.055f), CornerRadius(2f))
            if (braking) {
                drawOval(
                    color = Color(0xFFFF5A4D).copy(alpha = 0.5f),
                    topLeft = Offset(-bw * 0.3f, -bh * 0.61f),
                    size = Size(bw * 0.6f, bh * 0.16f)
                )
            }

            // Rider jacket & arms
            drawRoundRect(
                color = Color(0xFF1B2230),
                topLeft = Offset(-bw * 0.24f, -bh * 0.98f),
                size = Size(bw * 0.48f, bh * 0.38f),
                cornerRadius = CornerRadius(bw * 0.14f)
            )
            drawLine(Color(0xFF1B2230), Offset(-bw * 0.2f, -bh * 0.86f), Offset(-bw * 0.4f, -bh * 0.66f), bw * 0.11f)
            drawLine(Color(0xFF1B2230), Offset(bw * 0.2f, -bh * 0.86f), Offset(bw * 0.4f, -bh * 0.66f), bw * 0.11f)

            // Helmet & visor accent matching bike livery
            drawCircle(Color(0xFFE8ECF5), radius = bw * 0.21f, center = Offset(0f, -bh * 1.02f))
            drawRoundRect(bikeAccent, Offset(-bw * 0.16f, -bh * 1.06f), Size(bw * 0.32f, bh * 0.05f), CornerRadius(2f))
        }

        // Slipstream aerodynamic streaks
        if (engine.isDrafting) {
            val alpha = 0.55f + 0.3f * sin(engine.simClockMs * 0.02f)
            drawArc(
                color = Color(0xFF22E6FF).copy(alpha = alpha),
                startAngle = 205f,
                sweepAngle = 130f,
                useCenter = false,
                topLeft = Offset(px - bw * 0.9f, py - bh * 1.25f),
                size = Size(bw * 1.8f, bh * 0.8f),
                style = Stroke(width = 3f)
            )
        }

        // Kinetic Shield energy dome
        if (engine.hasShield) {
            val pulse = 0.45f + 0.2f * sin(engine.simClockMs * 0.01f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color(0xFF22E6FF).copy(alpha = pulse)),
                    center = Offset(px, py - bh * 0.58f),
                    radius = bh * 0.72f
                ),
                radius = bh * 0.72f,
                center = Offset(px, py - bh * 0.58f)
            )
            drawCircle(
                color = Color(0xFF22E6FF).copy(alpha = 0.75f),
                radius = bh * 0.72f,
                center = Offset(px, py - bh * 0.58f),
                style = Stroke(width = 2.5f)
            )
        }

        return info
    }

    private fun DrawScope.drawHeadlight(bike: BikeDrawInfo?, nightW: Float, w: Float, h: Float) {
        if (nightW < 0.05f || bike == null) return
        reusableQuadPath.reset()
        reusableQuadPath.moveTo(bike.px - bike.bw * 0.2f, bike.py - bike.bh * 0.55f)
        reusableQuadPath.lineTo(bike.px + bike.bw * 0.2f, bike.py - bike.bh * 0.55f)
        reusableQuadPath.lineTo(w * 0.8f, h * 0.52f)
        reusableQuadPath.lineTo(w * 0.2f, h * 0.52f)
        reusableQuadPath.close()
        drawPath(
            path = reusableQuadPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(1f, 0.91f, 0.66f, (0.16f * nightW).coerceIn(0f, 1f)),
                    Color.Transparent
                ),
                startY = bike.py - bike.bh * 0.6f,
                endY = h * 0.5f
            )
        )
    }

    private fun DrawScope.drawRain(engine: GameEngine, dt: Float, w: Float, h: Float) {
        val rainW = engine.palettes.stageW[2]
        if (rainW < 0.04f) return
        val pct = engine.speed / GameEngine.MAXSP
        val col = Color(0.78f, 0.86f, 0.94f, (0.26f * rainW).coerceIn(0f, 1f))
        for (r in engine.rainDrops) {
            if (engine.state != GameState.PAUSED) {
                r.y += (0.9f + pct * 1.4f) * r.speed * dt
                r.x -= (0.12f + engine.steerVisual * 0.1f) * dt
                if (r.y > 1f) {
                    r.y -= 1.05f
                    r.x = Random.nextFloat()
                }
                if (r.x < 0f) r.x += 1f
            }
            val x = r.x * w
            val y = r.y * h
            val len = r.len * (0.7f + pct)
            drawLine(col, Offset(x, y), Offset(x - len * 0.22f, y + len), strokeWidth = 1.4f)
        }
    }

    private fun DrawScope.drawFx(engine: GameEngine, dt: Float, w: Float, h: Float) {
        val pct = engine.speed / GameEngine.MAXSP
        val intensity = maxOf(0f, (pct - 0.72f) * 3.5f) + (if (engine.nitroOn) 0.7f else 0f)
        if (intensity > 0.05f) {
            val lineCol = Color(0.86f, 0.94f, 1f, min(0.5f, intensity * 0.35f))
            val minDim = min(w, h)
            for (l in engine.speedLines) {
                l.life -= dt * 2.4f
                if (l.life <= 0f) {
                    l.angle = Random.nextFloat() * 6.284f
                    l.r = 0.32f + Random.nextFloat() * 0.2f
                    l.life = 0.4f + Random.nextFloat() * 0.6f
                }
                val r0 = l.r * minDim * (1f + (1f - l.life) * 0.8f)
                val len = (18f + intensity * 46f) * l.life
                val ca = cos(l.angle)
                val sa = sin(l.angle)
                drawLine(
                    lineCol,
                    Offset(w * 0.5f + ca * r0, h * 0.45f + sa * r0),
                    Offset(w * 0.5f + ca * (r0 + len), h * 0.45f + sa * (r0 + len)),
                    strokeWidth = 1.6f
                )
            }
        }

        // Vignette
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.42f)),
                center = Offset(w * 0.5f, h * 0.48f),
                radius = maxOf(w, h) * 0.78f
            ),
            topLeft = Offset.Zero,
            size = Size(w, h)
        )

        if (engine.nitroOn) {
            val alpha = 0.28f + 0.1f * sin(engine.simClockMs * 0.02f)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color(0xFF22E6FF).copy(alpha = alpha)),
                    center = Offset(w * 0.5f, h * 0.5f),
                    radius = maxOf(w, h) * 0.75f
                ),
                topLeft = Offset.Zero,
                size = Size(w, h)
            )
        }

        if (engine.flashTimer > 0f) {
            drawRect(
                color = Color(0xFFE6F0FF).copy(alpha = (engine.flashTimer / 0.12f) * 0.28f),
                topLeft = Offset.Zero,
                size = Size(w, h)
            )
        }

        val densityScale = density
        for (t in engine.toasts) {
            val alphaInt = (t.life.coerceIn(0f, 1f) * 255).toInt()
            val tx = t.xNorm * w
            val ty = t.yNorm * h
            shadowTextPaint.alpha = (alphaInt * 0.6f).toInt()
            shadowTextPaint.textSize = t.sizeSp * densityScale
            textPaint.color = t.colorArgb
            textPaint.alpha = alphaInt
            textPaint.textSize = t.sizeSp * densityScale
            drawContext.canvas.nativeCanvas.drawText(t.text, tx + 2f, ty + 3f, shadowTextPaint)
            drawContext.canvas.nativeCanvas.drawText(t.text, tx, ty, textPaint)
        }
        textPaint.alpha = 255
    }

    private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t
}
