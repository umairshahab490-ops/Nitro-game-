package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

enum class SoundEffect {
    NEAR_MISS,
    CHECKPOINT,
    CRASH,
    SHIELD_BREAK,
    COUNT,
    GO,
    DENY,
    THUNDER,
    CASH,
    PICKUP
}

class SynthAudioEngine {

    private data class ActiveTone(
        var freq: Float,
        var freqEnd: Float,
        var remainingSamples: Int,
        val totalSamples: Int,
        var phase: Float = 0f,
        val isNoise: Boolean = false,
        val volume: Float = 0.25f
    )

    private val sampleRate = 22050
    private val running = AtomicBoolean(false)
    private var thread: Thread? = null

    @Volatile var muted: Boolean = false
    @Volatile var paused: Boolean = false
    @Volatile private var targetRpm: Float = 0.22f
    @Volatile private var targetSpeedPct: Float = 0.2f
    @Volatile private var targetNitro: Boolean = false
    @Volatile private var targetOffroad: Boolean = false
    @Volatile private var targetDrafting: Boolean = false

    private val pendingEvents = ConcurrentLinkedQueue<SoundEffect>()

    fun start() {
        if (running.getAndSet(true)) return
        thread = Thread({ runAudioLoop() }, "NitroSynthAudio").apply {
            priority = Thread.MAX_PRIORITY
            start()
        }
    }

    fun stop() {
        running.set(false)
        thread?.interrupt()
        thread = null
    }

    fun updateContinuous(
        rpm: Float,
        speedPct: Float,
        nitro: Boolean,
        offroad: Boolean,
        drafting: Boolean
    ) {
        targetRpm = rpm.coerceIn(0f, 1.3f)
        targetSpeedPct = speedPct.coerceIn(0f, 1.4f)
        targetNitro = nitro
        targetOffroad = offroad
        targetDrafting = drafting
    }

    fun play(sfx: SoundEffect) {
        if (!muted && !paused) {
            pendingEvents.offer(sfx)
        }
    }

    private fun runAudioLoop() {
        val minBuf = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(1024)
        val chunkSamples = minBuf / 2
        val pcm = ShortArray(chunkSamples)

        val track = runCatching {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBuf * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        }.getOrNull() ?: return

        runCatching { track.play() }

        var phase1 = 0f
        var phase2 = 0f
        var smoothRpm = 0.2f
        var smoothSpeed = 0.2f
        var lpFilter = 0f
        var hpPrev = 0f
        var hpOut = 0f
        val activeTones = mutableListOf<ActiveTone>()
        val twoPi = (2.0 * PI).toFloat()

        while (running.get()) {
            while (true) {
                val ev = pendingEvents.poll() ?: break
                enqueueTones(ev, activeTones)
            }

            if (paused || muted) {
                pcm.fill(0)
                runCatching { track.write(pcm, 0, pcm.size) }
                try {
                    Thread.sleep(25)
                } catch (_: InterruptedException) {
                    break
                }
                continue
            }

            val rTarget = targetRpm
            val sTarget = targetSpeedPct
            val nitro = targetNitro
            val off = targetOffroad
            val draft = targetDrafting

            for (i in 0 until chunkSamples) {
                smoothRpm += (rTarget - smoothRpm) * 0.0025f
                smoothSpeed += (sTarget - smoothSpeed) * 0.002f

                val engFreq = 52f + smoothRpm * 150f + (if (nitro) 38f else 0f)
                phase1 = (phase1 + engFreq / sampleRate) % 1f
                phase2 = (phase2 + (engFreq * 0.5f + 2f) / sampleRate) % 1f

                val saw = phase1 * 2f - 1f
                val sq = if (phase2 < 0.5f) 1f else -1f
                val rawEng = (saw * 0.6f + sq * 0.4f)
                val cutoff = (0.14f + smoothRpm * 0.32f).coerceIn(0.1f, 0.7f)
                lpFilter += (rawEng - lpFilter) * cutoff
                val engGain = 0.055f + smoothRpm * 0.085f

                val noise = Random.nextFloat() * 2f - 1f
                hpOut = 0.85f * (hpOut + noise - hpPrev)
                hpPrev = noise

                val windGain = (smoothSpeed * smoothSpeed * 0.08f).coerceAtMost(0.18f)
                val skidGain = if (off) 0.11f else 0f
                val nitroGain = if (nitro) 0.13f else 0f
                val draftGain = if (draft) 0.06f else 0f

                var sample = lpFilter * engGain +
                    noise * (windGain + skidGain + draftGain) +
                    hpOut * nitroGain

                var ti = activeTones.size - 1
                while (ti >= 0) {
                    val t = activeTones[ti]
                    val progress = 1f - t.remainingSamples.toFloat() / t.totalSamples.toFloat()
                    val env = (1f - progress)
                    if (t.isNoise) {
                        sample += (Random.nextFloat() * 2f - 1f) * t.volume * env
                    } else {
                        val f = t.freq + (t.freqEnd - t.freq) * progress
                        t.phase = (t.phase + f / sampleRate) % 1f
                        sample += sin(t.phase * twoPi) * t.volume * env
                    }
                    t.remainingSamples--
                    if (t.remainingSamples <= 0) activeTones.removeAt(ti)
                    ti--
                }

                val clamped = (sample * 0.75f).coerceIn(-0.98f, 0.98f)
                pcm[i] = (clamped * 32767f).toInt().toShort()
            }

            runCatching { track.write(pcm, 0, pcm.size) }
        }

        runCatching {
            track.pause()
            track.flush()
            track.release()
        }
    }

    private fun enqueueTones(sfx: SoundEffect, list: MutableList<ActiveTone>) {
        if (list.size > 12) list.removeAt(0)
        when (sfx) {
            SoundEffect.NEAR_MISS -> list.add(
                ActiveTone(920f, 1180f, (sampleRate * 0.08f).toInt(), (sampleRate * 0.08f).toInt(), volume = 0.22f)
            )
            SoundEffect.CHECKPOINT -> {
                list.add(ActiveTone(660f, 660f, (sampleRate * 0.12f).toInt(), (sampleRate * 0.12f).toInt(), volume = 0.24f))
                list.add(ActiveTone(880f, 990f, (sampleRate * 0.22f).toInt(), (sampleRate * 0.22f).toInt(), volume = 0.26f))
            }
            SoundEffect.CRASH -> {
                list.add(ActiveTone(140f, 55f, (sampleRate * 0.36f).toInt(), (sampleRate * 0.36f).toInt(), volume = 0.36f))
                list.add(ActiveTone(0f, 0f, (sampleRate * 0.32f).toInt(), (sampleRate * 0.32f).toInt(), isNoise = true, volume = 0.42f))
            }
            SoundEffect.SHIELD_BREAK -> {
                list.add(ActiveTone(1200f, 340f, (sampleRate * 0.26f).toInt(), (sampleRate * 0.26f).toInt(), volume = 0.30f))
                list.add(ActiveTone(0f, 0f, (sampleRate * 0.18f).toInt(), (sampleRate * 0.18f).toInt(), isNoise = true, volume = 0.25f))
            }
            SoundEffect.COUNT -> list.add(
                ActiveTone(440f, 440f, (sampleRate * 0.11f).toInt(), (sampleRate * 0.11f).toInt(), volume = 0.20f)
            )
            SoundEffect.GO -> list.add(
                ActiveTone(880f, 1046f, (sampleRate * 0.28f).toInt(), (sampleRate * 0.28f).toInt(), volume = 0.28f)
            )
            SoundEffect.DENY -> list.add(
                ActiveTone(190f, 150f, (sampleRate * 0.10f).toInt(), (sampleRate * 0.10f).toInt(), volume = 0.18f)
            )
            SoundEffect.THUNDER -> list.add(
                ActiveTone(0f, 0f, (sampleRate * 0.70f).toInt(), (sampleRate * 0.70f).toInt(), isNoise = true, volume = 0.28f)
            )
            SoundEffect.CASH -> {
                list.add(ActiveTone(1046f, 1318f, (sampleRate * 0.09f).toInt(), (sampleRate * 0.09f).toInt(), volume = 0.24f))
                list.add(ActiveTone(1568f, 1568f, (sampleRate * 0.14f).toInt(), (sampleRate * 0.14f).toInt(), volume = 0.22f))
            }
            SoundEffect.PICKUP -> list.add(
                ActiveTone(587f, 1174f, (sampleRate * 0.15f).toInt(), (sampleRate * 0.15f).toInt(), volume = 0.24f)
            )
        }
    }
}
