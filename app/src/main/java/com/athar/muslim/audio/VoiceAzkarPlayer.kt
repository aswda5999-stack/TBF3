package com.athar.muslim.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.PowerManager
import com.athar.muslim.R
import com.athar.muslim.model.VoiceReminderIntensity
import java.lang.reflect.Field
import kotlin.random.Random

object VoiceAzkarPlayer {
    private var mediaPlayer: MediaPlayer? = null

    fun discoverRawVoiceResIds(context: Context): List<Int> {
        val result = mutableListOf<Int>()
        try {
            val fields: Array<Field> = R.raw::class.java.fields
            for (field in fields) {
                val name = field.name.lowercase()
                if (
                    name.startsWith("azkar_") ||
                    name.startsWith("voice_") ||
                    name.startsWith("dhikr_") ||
                    name.startsWith("rem_")
                ) {
                    val resId = field.getInt(null)
                    if (resId != 0) {
                        result.add(resId)
                    }
                }
            }
        } catch (_: Exception) {
        }
        return result.distinct()
    }

    fun playNextVoiceReminder(
        context: Context,
        intensity: VoiceReminderIntensity
    ): Boolean {
        val prefs = context.getSharedPreferences("athar_muslim_prefs", Context.MODE_PRIVATE)
        val voiceEnabled = prefs.getBoolean("voice_azkar_enabled", false)
        if (!voiceEnabled) return false

        val voices = discoverRawVoiceResIds(context)
        if (voices.isEmpty()) return false

        val lastIndex = prefs.getInt("last_voice_index", -1)
        val chosenIndex = if (voices.size == 1) {
            0
        } else {
            var candidate = Random.nextInt(voices.size)
            if (candidate == lastIndex) {
                candidate = (candidate + 1) % voices.size
            }
            candidate
        }
        prefs.edit().putInt("last_voice_index", chosenIndex).apply()

        val chosenResId = voices[chosenIndex]
        stopCurrent()

        return try {
            val mp = MediaPlayer.create(context.applicationContext, chosenResId) ?: return false
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            mp.setWakeMode(context.applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            mp.setVolume(intensity.volumeFloat, intensity.volumeFloat)
            mp.setOnCompletionListener { completedPlayer ->
                try {
                    completedPlayer.release()
                } catch (_: Exception) {
                }
                if (mediaPlayer === completedPlayer) {
                    mediaPlayer = null
                }
            }
            mp.start()
            mediaPlayer = mp
            true
        } catch (_: Exception) {
            stopCurrent()
            false
        }
    }

    fun stopCurrent() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (_: Exception) {
        } finally {
            mediaPlayer = null
        }
    }
}
