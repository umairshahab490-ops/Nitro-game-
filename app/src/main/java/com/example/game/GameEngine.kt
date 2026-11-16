package com.example.game

import com.example.audio.SoundEffect
import com.example.model.BikeCatalog
import com.example.model.BikeModel
import com.example.model.BikeUpgradeState
import com.example.model.Difficulty
import com.example.model.GameMode
import com.example.model.GameState
import com.example.model.PickupType
import com.example.model.PresetWorldRoutes
import com.example.model.RealRoadTrack
import com.example.model.TrafficMode
import com.example.model.VehicleType
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.random.Random

data class RoadsideSprite(val kind: Int, val offset: Float) {
    companion object {
        const val POST = 0
        const val LAMP = 1
        const val PALM = 2
        const val BILL = 3
    }
}

class TrackSegment(
    val index: Int,
    var curve: Float,
    var wy1: Float,
    var wy2: Float,
    val z1: Float,
    val z2: Float,
    val sprites: MutableList<RoadsideSprite> = mutableListOf()
) {
    var x1: Float = 0f
    var y1: Float = 0f
    var w1: Float = 0f
    var x2: Float = 0f
    var y2: Float = 0f
    var w2: Float = 0f
    var ok: Boolean = false
    var vis: Boolean = false
    var clip: Float = 0f
    var fog: Int = 0
    var stripe: Int = 0
}

class TrafficVehicle(
    var z: Float,
    var offset: Float,
    var baseOffset: Float,
    var speed: Float,
    val type: VehicleType,
    val widthNorm: Float,
    val aspect: Float,
    val bodyColor: Int,
    val isOncoming: Boolean,
    var prevRel: Float,
    val phase: Float
)

class HighwayPickup(
    var z: Float,
    var offset: Float,
    var type: PickupType,
    var bobPhase: Float,
    var active: Boolean = true
)

class RainDrop(var x: Float, var y: Float, val len: Float, val speed: Float)
class SpeedLine(var angle: Float = 0f, var r: Float = 0f, var life: Float = 0f)
class FloatingToast(
    var text: String,
    var colorArgb: Int,
    var sizeSp: Float,
    var xNorm: Float,
    var yNorm: Float,
    var life: Float
)

data class RunSummary(
    val score: Int,
    val distanceMeters: Int,
    val topSpeedKmh: Int,
    val nearMisses: Int,
    val draftSeconds: Int,
    val twoWayMeters: Int,
    val pickupsCollected: Int,
    val usdEarned: Int,
    val bonusUsdFromPickupsAndDraft: Int,
    val endReason: String,
    val bikeName: String,
    val routeName: String,
    val difficulty: Difficulty,
    val gameMode: GameMode,
    val trafficMode: TrafficMode
)

interface GameEventCallbacks {
    fun onSound(sfx: SoundEffect)
    fun onHaptic(durationMs: Long, amplitude: Int)
    fun onBanner(text: String, colorArgb: Int)
    fun onRunFinished(summary: RunSummary)
}

class GameEngine(private val callbacks: GameEventCallbacks) {

    companion object {
        const val SEG = 200f
        const val ROADW = 2200f
        const val CAMH = 1100f
        const val DEPTH = 0.84f
        const val DRAW = 260
        const val PLAYERZ = CAMH * DEPTH
        const val MAXSP = 7000f
        const val KMH = 0.036f
        const val STEP = 1f / 60f
        const val STAGE_METERS = 1500f
    }

    val palettes = StagePaletteState()
    val segments = ArrayList<TrackSegment>(900)
    var trackLength: Float = 0f
        private set

    val vehicles = ArrayList<TrafficVehicle>(40)
    val pickups = ArrayList<HighwayPickup>(14)
    val rainDrops = Array(130) {
        RainDrop(
            x = Random.nextFloat(),
            y = Random.nextFloat(),
            len = 14f + Random.nextFloat() * 12f,
            speed = 0.8f + Random.nextFloat() * 0.6f
        )
    }
    val speedLines = Array(16) { SpeedLine() }
    val toasts = ArrayList<FloatingToast>(8)

    // Configuration
    var difficulty: Difficulty = Difficulty.PRO
    var gameMode: GameMode = GameMode.TIME_ATTACK
    var trafficMode: TrafficMode = TrafficMode.ONE_WAY
    var equippedBike: BikeModel = BikeCatalog.bikes.first()
    var bikeUpgrades: BikeUpgradeState = BikeUpgradeState()
    var activeRoute: RealRoadTrack = PresetWorldRoutes.presets.first()

    // Live simulation state
    var state: GameState = GameState.MENU
    var position: Float = 0f
    var distanceMeters: Float = 0f
    var twoWayMeters: Float = 0f
    var speed: Float = MAXSP * 0.22f
    var playerX: Float = 0f
    var steerVisual: Float = 0f
    var nitro: Float = 0f
    var nitroOn: Boolean = false
    var lives: Int = 3
    var timeLeft: Float = 42f
    var elapsedRunSeconds: Float = 0f
    var score: Float = 0f
    var combo: Int = 0
    var comboTimer: Float = 0f
    var topSpeedKmh: Float = 0f
    var nearMisses: Int = 0
    var pickupsCollected: Int = 0
    var bonusCashUsd: Int = 0
    var draftSecondsAccum: Float = 0f
    var isDrafting: Boolean = false
    var hasShield: Boolean = false
    var invulnTimer: Float = 0f
    var shakeTimer: Float = 0f
    var shakeAmp: Float = 0f
    var stageIdx: Int = 0
    var skyOffset: Float = 0f
    var countdownTimer: Float = 0f
    var countdownLabel: String = "3"
    var wreckTimer: Float = 0f
    var wreckRot: Float = 0f
    var endReason: String = ""
    var lightningTimer: Float = 8f
    var flashTimer: Float = 0f
    var offroad: Boolean = false
    var simClockMs: Float = 0f

    // Inputs
    var touchLeft: Boolean = false
    var touchRight: Boolean = false
    var touchBrake: Boolean = false
    var touchNitro: Boolean = false
    var tiltSteer: Float = 0f
    var tiltEnabled: Boolean = false

    private var draftToastTick = 0f
    private var lastCountdownDigit = 3
    private val rng = Random(2026)

    init {
        rebuildTrack(activeRoute)
        spawnTrafficAndPickups()
    }

    fun applyConfig(
        difficulty: Difficulty,
        gameMode: GameMode,
        trafficMode: TrafficMode,
        bike: BikeModel,
        upgrades: BikeUpgradeState,
        route: RealRoadTrack,
        tiltEnabled: Boolean
    ) {
        val routeChanged = route.id != activeRoute.id
        this.difficulty = difficulty
        this.gameMode = gameMode
        this.trafficMode = trafficMode
        this.equippedBike = bike
        this.bikeUpgrades = upgrades
        this.tiltEnabled = tiltEnabled
        if (routeChanged) {
            this.activeRoute = route
            if (state == GameState.MENU || state == GameState.OVER) {
                rebuildTrack(route)
                spawnTrafficAndPickups()
            }
        }
    }

    fun startNewRun() {
        if (state == GameState.COUNTDOWN || state == GameState.RUNNING || state == GameState.DYING) return
        rebuildTrack(activeRoute)
        position = 0f
        distanceMeters = 0f
        twoWayMeters = 0f
        speed = 0f
        playerX = 0f
        steerVisual = 0f
        nitro = 20f // small starter surge
        nitroOn = false
        lives = 3
        timeLeft = difficulty.startSeconds
        elapsedRunSeconds = 0f
        score = 0f
        combo = 0
        comboTimer = 0f
        topSpeedKmh = 0f
        nearMisses = 0
        pickupsCollected = 0
        bonusCashUsd = 0
        draftSecondsAccum = 0f
        isDrafting = false
        hasShield = false
        invulnTimer = 0f
        shakeTimer = 0f
        shakeAmp = 0f
        wreckTimer = 0f
        wreckRot = 0f
        flashTimer = 0f
        lightningTimer = 7f
        skyOffset = 0f
        toasts.clear()
        stageIdx = 0
        palettes.reset()

        spawnTrafficAndPickups()
        for (v in vehicles) {
            v.prevRel = (v.z - PLAYERZ + trackLength) % trackLength
        }

        state = GameState.COUNTDOWN
        countdownTimer = 2.7f
        lastCountdownDigit = 3
        countdownLabel = "3"
        callbacks.onSound(SoundEffect.COUNT)
    }

    fun returnToMenu() {
        state = GameState.MENU
        wreckRot = 0f
        playerX = 0f
        speed = MAXSP * 0.22f
        hasShield = false
        isDrafting = false
        nitroOn = false
        toasts.clear()
    }

    fun pause() {
        if (state == GameState.RUNNING) state = GameState.PAUSED
    }

    fun resume() {
        if (state == GameState.PAUSED) state = GameState.RUNNING
    }

    fun step(dt: Float) {
        if (state == GameState.PAUSED) return
        simClockMs += dt * 1000f

        palettes.blendToward(stageIdx, dt)

        var ti = toasts.size - 1
        while (ti >= 0) {
            val t = toasts[ti]
            t.life -= dt
            t.yNorm -= 0.045f * dt
            if (t.life <= 0f) toasts.removeAt(ti)
            ti--
        }

        when (state) {
            GameState.MENU, GameState.OVER -> {
                speed = MAXSP * 0.22f
                position = (position + speed * dt) % trackLength
                distanceMeters += speed * dt / 100f
                skyOffset += findSeg(position).curve * 0.22f * dt * 14f
                steerVisual = sin(simClockMs * 0.0006f) * 0.25f
                updateTraffic(dt, interact = false)
            }
            GameState.COUNTDOWN -> {
                countdownTimer -= dt
                val digit = ceil(countdownTimer / 0.9f).toInt().coerceAtLeast(0)
                if (digit != lastCountdownDigit && digit > 0) {
                    lastCountdownDigit = digit
                    countdownLabel = digit.toString()
                    callbacks.onSound(SoundEffect.COUNT)
                }
                if (countdownTimer <= 0f) {
                    state = GameState.RUNNING
                    countdownLabel = ""
                    callbacks.onBanner("GO!", 0xFF7EF07E.toInt())
                    callbacks.onSound(SoundEffect.GO)
                    addToast("Draft behind cars & collect $ pickups!", 0xFFFFD34D.toInt(), 13f)
                }
            }
            GameState.RUNNING -> simRunning(dt)
            GameState.DYING -> {
                speed = (speed * (1f - 3f * dt)).coerceAtLeast(0f)
                position = (position + speed * dt) % trackLength
                wreckRot += dt * 9f
                wreckTimer -= dt
                updateTraffic(dt, interact = false)
                if (wreckTimer <= 0f) finalizeOver()
            }
            GameState.PAUSED -> Unit
        }
    }

    private fun simRunning(dt: Float) {
        elapsedRunSeconds += dt
        val rainW = palettes.stageW[2]
        val bikeTopMult = BikeCatalog.effectiveTopSpeedMult(equippedBike, bikeUpgrades)
        val bikeAccelMult = BikeCatalog.effectiveAccelMult(equippedBike, bikeUpgrades)
        val bikeGripMult = BikeCatalog.effectiveGripMult(equippedBike, bikeUpgrades)
        val nitroBurnScale = BikeCatalog.effectiveNitroBurnRateScale(bikeUpgrades)

        val grip = (difficulty.baseGrip * bikeGripMult) * (1f - 0.22f * rainW)
        val buttonSteer = (if (touchRight) 1f else 0f) - (if (touchLeft) 1f else 0f)
        val steer = if (buttonSteer != 0f) buttonSteer else if (tiltEnabled) tiltSteer else 0f
        steerVisual += (steer - steerVisual) * (dt * 7f).coerceAtMost(1f)

        val bikeMaxSp = MAXSP * bikeTopMult
        val pct = (speed / bikeMaxSp).coerceIn(0f, 1.5f)
        val wantNitro = touchNitro && nitro > 0f
        if (touchNitro && !nitroOn && nitro <= 0f) {
            callbacks.onSound(SoundEffect.DENY)
        }
        nitroOn = wantNitro
        if (wantNitro) {
            nitro = (nitro - 30f * nitroBurnScale * dt).coerceAtLeast(0f)
        }

        offroad = abs(playerX) > 1.0f
        val draftFactor = if (isDrafting) 1.12f else 1.0f
        var target = (if (nitroOn) bikeMaxSp * 1.32f else bikeMaxSp) * draftFactor
        var rate = (if (nitroOn) 0.85f else 0.35f) * bikeAccelMult
        if (offroad) {
            target = target.coerceAtMost(2200f)
            rate = 1.4f
        }
        if (touchBrake) {
            speed = (speed - 9200f * dt).coerceAtLeast(0f)
        } else {
            val dirScale = if (speed < target) 1f else 2.2f
            speed += (target - speed) * (rate * dt * dirScale).coerceAtMost(1f)
        }

        val pSeg = findSeg(position + PLAYERZ)
        playerX += steer * dt * 2.2f * (0.3f + 0.7f * pct) * grip
        playerX -= dt * pct * pct * pSeg.curve * difficulty.centrifugal / grip
        playerX = playerX.coerceIn(-2.2f, 2.2f)

        if (offroad && speed > 1500f) {
            shakeTimer = maxOf(shakeTimer, 0.05f)
            shakeAmp = 4f
        }

        val prev = position
        position = (position + speed * dt) % trackLength
        if (position < prev) onCheckpoint()

        val distDelta = speed * dt / 100f
        distanceMeters += distDelta
        if (trafficMode == TrafficMode.TWO_WAY) {
            twoWayMeters += distDelta
        }
        skyOffset += pSeg.curve * pct * dt * 14f

        updateTraffic(dt, interact = true)
        if (state != GameState.RUNNING) return

        updatePickups()
        updateSlipstream(dt)

        val kmh = speed * KMH
        topSpeedKmh = maxOf(topSpeedKmh, kmh)
        score += kmh * dt * 0.5f

        if (comboTimer > 0f) {
            comboTimer -= dt
            if (comboTimer <= 0f) combo = 0
        }

        if (gameMode == GameMode.TIME_ATTACK) {
            timeLeft -= dt
            if (timeLeft <= 0f) {
                timeLeft = 0f
                endReason = "TIME UP"
                finalizeOver()
                return
            }
        }

        val nextStage = (floor(distanceMeters / STAGE_METERS).toInt()) % 3
        if (nextStage != stageIdx) {
            stageIdx = nextStage
            callbacks.onBanner("▸ ${StagePaletteState.STAGE_NAMES[nextStage]} ◂", 0xFFFFD34D.toInt())
            callbacks.onSound(SoundEffect.PICKUP)
        }

        invulnTimer = (invulnTimer - dt).coerceAtLeast(0f)
        shakeTimer = (shakeTimer - dt).coerceAtLeast(0f)
        if (rainW > 0.5f) {
            lightningTimer -= dt
            if (lightningTimer <= 0f) {
                flashTimer = 0.12f
                lightningTimer = 6f + rng.nextFloat() * 9f
                callbacks.onSound(SoundEffect.THUNDER)
            }
        }
        flashTimer = (flashTimer - dt).coerceAtLeast(0f)
    }

    private fun updateTraffic(dt: Float, interact: Boolean) {
        val pz = position + PLAYERZ
        val bucket = ((simClockMs / 250f).toInt()) and 3

        for (i in vehicles.indices) {
            val c = vehicles[i]
            if (c.isOncoming) {
                c.z = ((c.z - c.speed * dt) % trackLength + trackLength) % trackLength
            } else {
                c.z = (c.z + c.speed * dt) % trackLength
                if (c.type == VehicleType.BIKE) {
                    c.offset = c.baseOffset + sin(simClockMs * 0.0005f + c.phase) * 0.18f
                }
                if ((i and 3) == bucket) {
                    for (j in vehicles.indices) {
                        if (j == i) continue
                        val o = vehicles[j]
                        if (o.isOncoming) continue
                        val dz = (o.z - c.z + trackLength) % trackLength
                        if (dz < 1200f && abs(o.offset - c.offset) < 0.3f && o.speed < c.speed) {
                            c.speed = o.speed * 0.96f
                        }
                    }
                }
            }

            if (!interact) continue
            val rel = (c.z - pz + trackLength) % trackLength
            val hitSpeedOk = c.isOncoming || speed >= c.speed * 0.9f
            if (invulnTimer <= 0f && hitSpeedOk && (rel < 230f || rel > trackLength - 140f)) {
                if (abs(playerX - c.offset) < (0.14f + c.widthNorm) * 0.45f) {
                    onCrash()
                }
            }

            val passedForward = !c.isOncoming && c.prevRel < 1500f && rel > trackLength - 1500f && speed > c.speed
            val passedOncoming = c.isOncoming && c.prevRel < 1600f && rel > trackLength - 1600f
            if (passedForward || passedOncoming) {
                val gap = abs(playerX - c.offset)
                if (gap < 0.58f && speed > MAXSP * 0.52f) {
                    onNearMiss(isOncoming = c.isOncoming)
                }
            }
            c.prevRel = rel
        }
    }

    private fun updatePickups() {
        val pz = position + PLAYERZ
        for (p in pickups) {
            val rel = (p.z - pz + trackLength) % trackLength
            if (p.active && (rel < 260f || rel > trackLength - 160f)) {
                if (abs(playerX - p.offset) < 0.34f) {
                    p.active = false
                    pickupsCollected++
                    when (p.type) {
                        PickupType.USD_CASH -> {
                            val cash = if (trafficMode == TrafficMode.TWO_WAY) 260 else 180
                            bonusCashUsd += cash
                            score += 250f
                            addToast("+$$cash USD", 0xFF7EF07E.toInt(), 17f)
                            callbacks.onSound(SoundEffect.CASH)
                            callbacks.onHaptic(18L, 120)
                        }
                        PickupType.NITRO_CELL -> {
                            nitro = (nitro + 36f).coerceAtMost(100f)
                            score += 150f
                            addToast("NITRO +36%", 0xFF22E6FF.toInt(), 16f)
                            callbacks.onSound(SoundEffect.PICKUP)
                            callbacks.onHaptic(18L, 120)
                        }
                        PickupType.SHIELD -> {
                            hasShield = true
                            score += 200f
                            addToast("KINETIC SHIELD", 0xFF22E6FF.toInt(), 16f)
                            callbacks.onSound(SoundEffect.PICKUP)
                            callbacks.onHaptic(22L, 140)
                        }
                    }
                }
            }
            // Respawn passed pickup further down the highway
            if (rel > trackLength - 1200f && rel < trackLength - 220f) {
                if (!p.active || rng.nextFloat() < 0.05f) {
                    p.z = (pz + 3200f + rng.nextFloat() * (trackLength * 0.45f)) % trackLength
                    p.offset = listOf(-0.67f, 0f, 0.67f)[rng.nextInt(3)]
                    p.type = randomPickupType()
                    p.active = true
                }
            }
        }
    }

    private fun updateSlipstream(dt: Float) {
        val pz = position + PLAYERZ
        var draftingNow = false
        if (speed > MAXSP * 0.35f) {
            for (c in vehicles) {
                if (c.isOncoming) continue
                val dz = (c.z - pz + trackLength) % trackLength
                if (dz in 240f..1350f && abs(playerX - c.offset) < 0.28f) {
                    draftingNow = true
                    break
                }
            }
        }
        isDrafting = draftingNow
        if (draftingNow) {
            draftSecondsAccum += dt
            draftToastTick += dt
            val rechargeMult = BikeCatalog.effectiveNitroRechargeMult(equippedBike, bikeUpgrades)
            nitro = (nitro + 8f * rechargeMult * dt).coerceAtMost(100f)
            score += 90f * dt
            if (draftToastTick >= 1.0f) {
                draftToastTick -= 1.0f
                val cash = if (trafficMode == TrafficMode.TWO_WAY) 45 else 25
                bonusCashUsd += cash
                addToast("SLIPSTREAM +$$cash", 0xFF22E6FF.toInt(), 14f)
            }
        } else {
            draftToastTick = 0f
        }
    }

    private fun onCheckpoint() {
        val bonus = difficulty.checkpointBonus.toInt()
        if (gameMode == GameMode.TIME_ATTACK) {
            timeLeft = (timeLeft + difficulty.checkpointBonus).coerceAtMost(99f)
            callbacks.onBanner("CHECKPOINT +${bonus}s", 0xFF7EF07E.toInt())
        } else {
            bonusCashUsd += 250
            callbacks.onBanner("CHECKPOINT +$250", 0xFF7EF07E.toInt())
        }
        score += 500f
        nitro = (nitro + 14f).coerceAtMost(100f)
        callbacks.onSound(SoundEffect.CHECKPOINT)
        callbacks.onHaptic(22L, 140)
    }

    private fun onCrash() {
        if (hasShield) {
            hasShield = false
            invulnTimer = 1.6f
            speed = (speed * 0.78f).coerceAtLeast(MAXSP * 0.25f)
            shakeTimer = 0.35f
            shakeAmp = 8f
            callbacks.onSound(SoundEffect.SHIELD_BREAK)
            callbacks.onHaptic(45L, 180)
            addToast("SHIELD ABSORBED!", 0xFF22E6FF.toInt(), 19f)
            return
        }

        lives--
        invulnTimer = 2.5f
        speed = MAXSP * 0.15f
        combo = 0
        nitro *= 0.5f
        shakeTimer = 0.6f
        shakeAmp = 13f
        callbacks.onSound(SoundEffect.CRASH)
        callbacks.onHaptic(95L, 240)
        addToast("WRECK!", 0xFFFF4D5E.toInt(), 22f)
        if (lives <= 0) {
            endReason = "WRECKED"
            state = GameState.DYING
            wreckTimer = 1.0f
            wreckRot = 0f
        }
    }

    private fun onNearMiss(isOncoming: Boolean) {
        combo = (combo + 1).coerceAtMost(8)
        comboTimer = 5f
        nearMisses++
        val mult = if (isOncoming) 2 else 1
        val pts = 100 * combo * mult
        score += pts
        val rechargeMult = BikeCatalog.effectiveNitroRechargeMult(equippedBike, bikeUpgrades)
        nitro = (nitro + 22f * rechargeMult * (if (isOncoming) 1.25f else 1f)).coerceAtMost(100f)
        val label = if (isOncoming) "ONCOMING ×$combo +$pts" else "NEAR MISS ×$combo +$pts"
        val col = if (isOncoming) 0xFFFFD34D.toInt() else 0xFF22E6FF.toInt()
        addToast(label, col, 15f)
        callbacks.onSound(SoundEffect.NEAR_MISS)
        callbacks.onHaptic(15L, 110)
    }

    private fun finalizeOver() {
        state = GameState.OVER
        wreckRot = 0f
        playerX = 0f
        hasShield = false
        isDrafting = false
        nitroOn = false
        speed = MAXSP * 0.22f

        val finalScore = score.toInt()
        val finalDist = distanceMeters.toInt()
        val baseRunUsd = (finalDist * 0.28f) + (nearMisses * 38f) + (finalScore * 0.03f)
        val combinedMult = difficulty.usdMultiplier * trafficMode.usdMultiplier * equippedBike.payoutBonus
        val totalUsd = ((baseRunUsd * combinedMult) + bonusCashUsd).toInt().coerceAtLeast(if (finalDist > 50) 75 else 25)

        callbacks.onRunFinished(
            RunSummary(
                score = finalScore,
                distanceMeters = finalDist,
                topSpeedKmh = topSpeedKmh.toInt(),
                nearMisses = nearMisses,
                draftSeconds = draftSecondsAccum.toInt(),
                twoWayMeters = twoWayMeters.toInt(),
                pickupsCollected = pickupsCollected,
                usdEarned = totalUsd,
                bonusUsdFromPickupsAndDraft = bonusCashUsd,
                endReason = endReason,
                bikeName = equippedBike.fullName,
                routeName = activeRoute.roadName,
                difficulty = difficulty,
                gameMode = gameMode,
                trafficMode = trafficMode
            )
        )
    }

    private fun addToast(text: String, colorArgb: Int, sizeSp: Float) {
        if (toasts.size >= 6) toasts.removeAt(0)
        toasts.add(
            FloatingToast(
                text = text,
                colorArgb = colorArgb,
                sizeSp = sizeSp,
                xNorm = 0.5f + (rng.nextFloat() - 0.5f) * 0.14f,
                yNorm = 0.60f,
                life = 1.15f
            )
        )
    }

    fun findSeg(z: Float): TrackSegment {
        val idx = ((floor(z / SEG).toInt() % segments.size) + segments.size) % segments.size
        return segments[idx]
    }

    private fun rebuildTrack(route: RealRoadTrack) {
        segments.clear()
        val curves = route.curvatureProfile
        if (curves.size >= 4) {
            // Map real GPS bearing curvature profile into smooth hilly highway stretches
            val hillPattern = floatArrayOf(0f, 24f, -18f, 35f, -30f, 16f, -22f)
            for (i in curves.indices) {
                val c = curves[i].coerceIn(-4.3f, 4.3f)
                val dy = if (i == curves.lastIndex) {
                    val lastWy = if (segments.isEmpty()) 0f else segments.last().wy2
                    -lastWy / SEG
                } else {
                    hillPattern[i % hillPattern.size]
                }
                addRoad(18, 38, 18, c, dy)
            }
        } else {
            addRoad(20, 60, 20, 0f, 0f)
            addRoad(25, 50, 25, 3f, 30f)
            addRoad(20, 40, 20, -2f, 10f)
            addRoad(20, 40, 20, 2f, -10f)
            addRoad(15, 45, 15, 0f, -20f)
            addRoad(30, 60, 30, 4.5f, 45f)
            addRoad(25, 40, 25, -4f, -35f)
            addRoad(15, 35, 15, 0f, 0f)
            addRoad(20, 40, 20, -2.5f, 0f)
            addRoad(20, 30, 20, 0f, 50f)
            addRoad(20, 40, 20, 3.5f, -50f)
            val lastWy = segments.last().wy2
            addRoad(25, 45, 25, 0f, -lastWy / SEG)
        }

        val count = segments.size
        trackLength = count * SEG

        val propRng = Random(route.id.hashCode())
        for (i in 0 until count) {
            val sg = segments[i]
            if (i % 6 == 0) {
                sg.sprites.add(RoadsideSprite(RoadsideSprite.POST, -1.22f))
                sg.sprites.add(RoadsideSprite(RoadsideSprite.POST, 1.22f))
            }
            if (i % 14 == 0) {
                sg.sprites.add(RoadsideSprite(RoadsideSprite.LAMP, if (i % 28 == 0) -1.55f else 1.55f))
            }
            if (i % 23 == 0) {
                val side = if (propRng.nextFloat() < 0.5f) -1f else 1f
                sg.sprites.add(RoadsideSprite(RoadsideSprite.PALM, side * (1.9f + propRng.nextFloat() * 0.9f)))
            }
            if (i % 97 == 0) {
                val side = if (propRng.nextFloat() < 0.5f) -1f else 1f
                sg.sprites.add(RoadsideSprite(RoadsideSprite.BILL, side * (1.7f + propRng.nextFloat() * 0.5f)))
            }
        }
    }

    private fun addSeg(curve: Float, y: Float) {
        val i = segments.size
        val prevY = if (segments.isEmpty()) 0f else segments.last().wy2
        segments.add(
            TrackSegment(
                index = i,
                curve = curve,
                wy1 = prevY,
                wy2 = y,
                z1 = i * SEG,
                z2 = (i + 1) * SEG
            )
        )
    }

    private fun addRoad(enter: Int, hold: Int, leave: Int, curve: Float, dy: Float) {
        val sy = if (segments.isEmpty()) 0f else segments.last().wy2
        val ey = sy + dy * SEG
        val total = (enter + hold + leave).toFloat()
        var n = 0
        for (i in 0 until enter) {
            val t = i.toFloat() / enter
            addSeg(curve * t * t, easeInOut(sy, ey, n / total))
            n++
        }
        for (i in 0 until hold) {
            addSeg(curve, easeInOut(sy, ey, n / total))
            n++
        }
        for (i in 0 until leave) {
            val t = i.toFloat() / leave
            addSeg(easeInOut(curve, 0f, t), easeInOut(sy, ey, n / total))
            n++
        }
    }

    private fun easeInOut(a: Float, b: Float, t: Float): Float =
        a + (b - a) * (-cos(t * PI.toFloat()) / 2f + 0.5f)

    private fun spawnTrafficAndPickups() {
        vehicles.clear()
        pickups.clear()

        val carColors = intArrayOf(
            0xFF3D7DD8.toInt(), 0xFFD8B23D.toInt(), 0xFF58B06A.toInt(),
            0xFFB0588F.toInt(), 0xFF8A93A6.toInt(), 0xFFC96A3D.toInt()
        )
        val bikeColors = intArrayOf(
            0xFF22E6FF.toInt(), 0xFFFFD34D.toInt(), 0xFF7EF07E.toInt(),
            0xFFFF8A3D.toInt(), 0xFFC07EFF.toInt()
        )
        val lanes = floatArrayOf(-0.67f, 0f, 0.67f)
        val count = difficulty.trafficCount

        for (i in 0 until count) {
            val z = ((i.toFloat() / count) * trackLength + rng.nextFloat() * 900f) % trackLength
            val lane = lanes[rng.nextInt(3)]
            val isOncoming = trafficMode == TrafficMode.TWO_WAY && lane < -0.3f
            val roll = rng.nextFloat()
            val type: VehicleType
            val widthNorm: Float
            val aspect: Float
            val sp: Float
            val col: Int

            if (roll < 0.20f) {
                type = VehicleType.TRUCK
                widthNorm = 0.40f
                aspect = 1.05f
                sp = MAXSP * (0.30f + rng.nextFloat() * 0.08f)
                col = 0xFF7D8698.toInt()
            } else if (roll < 0.75f) {
                type = VehicleType.CAR
                widthNorm = 0.30f
                aspect = 0.78f
                sp = MAXSP * (0.42f + rng.nextFloat() * (difficulty.trafficMaxRatio - 0.42f))
                col = carColors[rng.nextInt(carColors.size)]
            } else {
                type = VehicleType.BIKE
                widthNorm = 0.16f
                aspect = 1.15f
                sp = MAXSP * (0.60f + rng.nextFloat() * 0.22f)
                col = bikeColors[rng.nextInt(bikeColors.size)]
            }

            vehicles.add(
                TrafficVehicle(
                    z = z,
                    offset = lane,
                    baseOffset = lane,
                    speed = sp,
                    type = type,
                    widthNorm = widthNorm,
                    aspect = aspect,
                    bodyColor = col,
                    isOncoming = isOncoming,
                    prevRel = 0f,
                    phase = rng.nextFloat() * 6.28f
                )
            )
        }

        // Spawn 10 collectible pickups spread across the highway
        for (i in 0 until 10) {
            val z = ((i + 0.5f) / 10f * trackLength) % trackLength
            val lane = lanes[rng.nextInt(3)]
            pickups.add(
                HighwayPickup(
                    z = z,
                    offset = lane,
                    type = randomPickupType(),
                    bobPhase = rng.nextFloat() * 6.28f,
                    active = true
                )
            )
        }
    }

    private fun randomPickupType(): PickupType {
        val r = rng.nextFloat()
        return when {
            r < 0.55f -> PickupType.USD_CASH
            r < 0.85f -> PickupType.NITRO_CELL
            else -> PickupType.SHIELD
        }
    }
}
