package com.athar.muslim.ui.components

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import android.view.View

object SoundHelper {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var zikrToneGenerator: ToneGenerator? = null

    private fun getZikrToneGenerator(): ToneGenerator? {
        if (zikrToneGenerator != null) return zikrToneGenerator
        return try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 85).also {
                zikrToneGenerator = it
            }
        } catch (_: Exception) {
            null
        }
    }

    /** صوت لمس النظام الأصلي للتنقل والأزرار العامة */
    fun playSystemTouchSound(
        context: Context,
        enabled: Boolean,
        view: View? = null,
        hapticEnabled: Boolean = false
    ) {
        if (hapticEnabled && view != null) {
            try {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            } catch (_: Exception) {
            }
        }
        if (!enabled) return
        mainHandler.post {
            try {
                if (view != null) {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                } else {
                    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                    audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK)
                }
            } catch (_: Exception) {
            }
        }
    }

    /** صوت مميز واحترافي مخصص للضغط على الأذكار والتسبيح */
    fun playZikrPressSound(
        enabled: Boolean,
        isCompletion: Boolean = false,
        view: View? = null,
        hapticEnabled: Boolean = false
    ) {
        if (hapticEnabled && view != null) {
            try {
                val feedback = if (isCompletion) {
                    HapticFeedbackConstants.CONFIRM
                } else {
                    HapticFeedbackConstants.CONTEXT_CLICK
                }
                view.performHapticFeedback(feedback)
            } catch (_: Exception) {
            }
        }
        if (!enabled) return
        mainHandler.post {
            val gen = getZikrToneGenerator() ?: return@post
            try {
                if (isCompletion) {
                    gen.startTone(ToneGenerator.TONE_PROP_PROMPT, 140)
                } else {
                    gen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 65)
                }
            } catch (_: Exception) {
            }
        }
    }

    fun playTapSound(enabled: Boolean, isCompletion: Boolean = false) {
        playZikrPressSound(enabled = enabled, isCompletion = isCompletion)
    }
}
