package com.athar.muslim.ui.components

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper

object SoundHelper {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var toneGenerator: ToneGenerator? = null

    private fun getGenerator(): ToneGenerator? {
        if (toneGenerator != null) return toneGenerator
        return try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 75).also {
                toneGenerator = it
            }
        } catch (_: Exception) {
            try {
                ToneGenerator(AudioManager.STREAM_NOTIFICATION, 75).also {
                    toneGenerator = it
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    fun playTapSound(enabled: Boolean, isCompletion: Boolean = false) {
        if (!enabled) return
        mainHandler.post {
            val gen = getGenerator() ?: return@post
            try {
                if (isCompletion) {
                    gen.startTone(ToneGenerator.TONE_PROP_ACK, 120)
                } else {
                    gen.startTone(ToneGenerator.TONE_PROP_BEEP, 55)
                }
            } catch (_: Exception) {
                // تجاهل أي استثناء صوتي مؤقت على الجهاز
            }
        }
    }
}
