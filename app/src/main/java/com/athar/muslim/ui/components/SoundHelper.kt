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

    /** صوت لمس النظام الأصلي للتنقل والأزرار العامة */
    fun playSystemTouchSound(
        context: Context,
        enabled: Boolean,
        view: View? = null,
        hapticEnabled: Boolean = false
    ) {
        if (hapticEnabled && view != null) {
            runCatching {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }
        }
        if (!enabled) return
        mainHandler.post {
            runCatching {
                if (view != null) {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                } else {
                    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                    audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK)
                }
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
            runCatching {
                val feedback = if (isCompletion) {
                    HapticFeedbackConstants.CONFIRM
                } else {
                    HapticFeedbackConstants.CONTEXT_CLICK
                }
                view.performHapticFeedback(feedback)
            }
        }
        if (!enabled) return
        mainHandler.post {
            var toneGen: ToneGenerator? = null
            try {
                toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
                if (isCompletion) {
                    toneGen.startTone(ToneGenerator.TONE_PROP_PROMPT, 120)
                } else {
                    toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 55)
                }
                val releaseDelay = if (isCompletion) 160L else 90L
                val ref = toneGen
                mainHandler.postDelayed({
                    runCatching { ref.release() }
                }, releaseDelay)
                toneGen = null
            } catch (_: Throwable) {
                runCatching { toneGen?.release() }
            }
        }
    }

    fun playTapSound(enabled: Boolean, isCompletion: Boolean = false) {
        playZikrPressSound(enabled = enabled, isCompletion = isCompletion)
    }
}
