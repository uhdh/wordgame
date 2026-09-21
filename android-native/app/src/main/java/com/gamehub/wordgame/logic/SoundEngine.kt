package com.gamehub.wordgame.logic

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

/**
 * Ported from src/js/audioEffects.js — same envelopes/frequencies, re-synthesized as PCM16
 * instead of Web Audio oscillator nodes. No sound assets required.
 */
class SoundEngine {
    var muted: Boolean = false
    var volume: Float = 0.35f
    private val sampleRate = 44100

    private enum class Wave { SINE, TRIANGLE, SAWTOOTH }

    private fun wave(shape: Wave, phase: Double): Double = when (shape) {
        Wave.SINE -> sin(phase)
        Wave.TRIANGLE -> 2.0 / PI * kotlin.math.asin(sin(phase))
        Wave.SAWTOOTH -> 2.0 * ((phase / (2 * PI)) - kotlin.math.floor(0.5 + phase / (2 * PI)))
    }

    /** freqStart→freqEnd exponential glide, exponential decay envelope, [gainMul] * volume peak. */
    private fun tone(freqStart: Double, freqEnd: Double, durationSec: Double, gainMul: Double, shape: Wave = Wave.SINE): ShortArray {
        val n = (sampleRate * durationSec).toInt().coerceAtLeast(1)
        val out = ShortArray(n)
        var phase = 0.0
        val peak = (volume * gainMul).coerceIn(0.0, 1.0)
        for (i in 0 until n) {
            val t = i.toDouble() / n
            val freq = freqStart * Math.pow(freqEnd / freqStart, t)
            phase += 2 * PI * freq / sampleRate
            val envelope = peak * exp(-5.0 * t)
            out[i] = (wave(shape, phase) * envelope * Short.MAX_VALUE).toInt().toShort()
        }
        return out
    }

    private fun playBuffer(samples: ShortArray) {
        if (muted || samples.isEmpty()) return
        Thread {
            try {
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setSampleRate(sampleRate)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(samples.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
                track.write(samples, 0, samples.size)
                track.play()
                Thread.sleep(min(2000L, (samples.size * 1000L / sampleRate) + 60))
                track.release()
            } catch (_: Exception) {
                // best-effort sound; never crash gameplay over it
            }
        }.start()
    }

    private fun mix(vararg parts: Pair<Double, ShortArray>): ShortArray {
        val total = parts.maxOf { (offsetSec, arr) -> (offsetSec * sampleRate).toInt() + arr.size }
        val out = ShortArray(total)
        for ((offsetSec, arr) in parts) {
            val start = (offsetSec * sampleRate).toInt()
            for (i in arr.indices) {
                val idx = start + i
                if (idx < out.size) {
                    val sum = out[idx] + arr[i]
                    out[idx] = sum.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
        }
        return out
    }

    fun playTileClick() = playBuffer(tone(440.0, 880.0, 0.04, 0.4))
    fun playTileRotate() = playBuffer(tone(320.0, 640.0, 0.08, 0.35, Wave.TRIANGLE))

    fun playTileCombine() = playBuffer(
        mix(
            0.00 to tone(523.25, 523.25, 0.15, 0.3),
            0.03 to tone(659.25, 659.25, 0.15, 0.3),
            0.06 to tone(783.99, 783.99, 0.15, 0.3)
        )
    )

    fun playFlip(state: TileState, index: Int = 0) {
        val delay = index * 0.25
        val buf = when (state) {
            TileState.CORRECT -> tone(587.33 + index * 60, (587.33 + index * 60) * 1.5, 0.2, 0.45)
            TileState.PRESENT -> tone(440.0, 554.37, 0.18, 0.35, Wave.TRIANGLE)
            TileState.ABSENT -> tone(220.0, 140.0, 0.15, 0.3)
        }
        playBuffer(mix(delay to buf))
    }

    fun playWin() = playBuffer(
        mix(
            0.0 to tone(523.25, 523.25, 0.12, 0.5),
            0.1 to tone(659.25, 659.25, 0.12, 0.5),
            0.2 to tone(783.99, 783.99, 0.12, 0.5),
            0.3 to tone(1046.50, 1046.50, 0.4, 0.5)
        )
    )

    fun playError() = playBuffer(
        mix(
            0.0 to tone(180.0, 180.0, 0.07, 0.25, Wave.SAWTOOTH),
            0.08 to tone(150.0, 150.0, 0.07, 0.25, Wave.SAWTOOTH)
        )
    )

    fun playTick() = playBuffer(tone(800.0, 800.0, 0.02, 0.2))
}
