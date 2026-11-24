package com.athar.muslim.ui.components

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object SoundHelper {
    private const val SAMPLE_RATE = 22050

    fun playTapSound(enabled: Boolean, isCompletion: Boolean = false) {
        if (!enabled) return
        Thread {
            try {
                if (isCompletion) {
                    playTone(freqHz = 784.0, durationMs = 110, peakVolume = 0.22)
                    Thread.sleep(45)
                    playTone(freqHz = 1046.5, durationMs = 150, peakVolume = 0.25)
                } else {
                    playTone(freqHz = 620.0, durationMs = 38, peakVolume = 0.18)
                }
            } catch (_: Exception) {
                // تجاهل آمن إذا كان النظام الصوتي مشغولاً
            }
        }.start()
    }

    private fun playTone(freqHz: Double, durationMs: Int, peakVolume: Double) {
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val decay = exp(-t * (28.0 / (durationMs / 100.0).coerceAtLeast(0.04)))
            val sample = sin(2.0 * PI * freqHz * t) * decay * peakVolume
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        val minBufferBytes = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(numSamples * 2)

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(minBufferBytes)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        try {
            track.write(buffer, 0, numSamples)
            track.play()
            Thread.sleep(durationMs.toLong() + 15L)
        } finally {
            track.stop()
            track.release()
        }
    }
}

