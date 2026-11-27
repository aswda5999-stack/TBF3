package com.athar.muslim.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.coroutines.resume

/** قارئ متاح عبر everyayah.com (ملف صوتي لكل آية). */
data class QuranReciter(val folder: String, val name: String)

object QuranReciters {
    val all: List<QuranReciter> = listOf(
        QuranReciter("Maher_AlMuaiqly_64kbps", "ماهر المعيقلي"),
        QuranReciter("Alafasy_128kbps", "مشاري العفاسي"),
        QuranReciter("Abdurrahmaan_As-Sudais_192kbps", "عبدالرحمن السديس"),
        QuranReciter("Saood_ash-Shuraym_128kbps", "سعود الشريم"),
        QuranReciter("Abdul_Basit_Murattal_64kbps", "عبدالباسط عبدالصمد (مرتل)"),
        QuranReciter("Husary_128kbps", "محمود خليل الحصري"),
        QuranReciter("Minshawy_Murattal_128kbps", "محمد صديق المنشاوي"),
        QuranReciter("Ghamadi_40kbps", "سعد الغامدي"),
        QuranReciter("Abu_Bakr_Ash-Shaatree_128kbps", "أبو بكر الشاطري"),
        QuranReciter("Hani_Rifai_192kbps", "هاني الرفاعي"),
        QuranReciter("Hudhaify_128kbps", "علي الحذيفي"),
        QuranReciter("Abdullah_Basfar_192kbps", "عبدالله بصفر"),
        QuranReciter("Muhammad_Ayyoub_128kbps", "محمد أيوب"),
        QuranReciter("Mohammad_al_Tablaway_128kbps", "محمد الطبلاوي")
    )

    fun byFolder(folder: String?): QuranReciter =
        all.firstOrNull { it.folder == folder } ?: all.first()
}

/** تشغيل تلاوة آية آية: تُنزَّل الآية مرة واحدة ثم تُحفظ وتعمل دون اتصال. */
object AyahAudioPlayer {
    private var current: MediaPlayer? = null

    fun stop() {
        val player = current
        current = null
        if (player != null) releasePlayer(player)
    }

    private fun releasePlayer(player: MediaPlayer) {
        try {
            player.setOnCompletionListener(null)
            player.setOnErrorListener(null)
            player.setOnPreparedListener(null)
            player.stop()
        } catch (_: Exception) {
        }
        try {
            player.release()
        } catch (_: Exception) {
        }
    }

    private fun ensureFile(context: Context, folder: String, surah: Int, ayah: Int): File? {
        val name = String.format(Locale.US, "%03d%03d.mp3", surah, ayah)
        val dir = File(File(context.filesDir, "ayah_audio"), folder)
        val file = File(dir, name)
        if (file.exists() && file.length() > 1000L) return file

        var connection: HttpURLConnection? = null
        return try {
            connection = (URL("https://everyayah.com/data/$folder/$name").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 20000
            }
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return null
            dir.mkdirs()
            val temp = File(dir, "$name.part")
            connection.inputStream.use { input ->
                temp.outputStream().use { output -> input.copyTo(output) }
            }
            if (temp.length() > 1000L && temp.renameTo(file)) {
                file
            } else {
                temp.delete()
                null
            }
        } catch (_: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }

    /** يعيد true عند انتهاء الآية بنجاح، و false عند الفشل. يُلغى بإلغاء الـ coroutine. */
    suspend fun play(context: Context, folder: String, surah: Int, ayah: Int): Boolean {
        val file = withContext(Dispatchers.IO) { ensureFile(context, folder, surah, ayah) }
            ?: return false

        return suspendCancellableCoroutine { continuation ->
            stop()
            val player = MediaPlayer()
            current = player
            try {
                player.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                player.setDataSource(file.absolutePath)
                player.setOnPreparedListener { it.start() }
                player.setOnCompletionListener {
                    if (current === player) current = null
                    releasePlayer(player)
                    if (continuation.isActive) continuation.resume(true)
                }
                player.setOnErrorListener { _, _, _ ->
                    if (current === player) current = null
                    releasePlayer(player)
                    if (continuation.isActive) continuation.resume(false)
                    true
                }
                player.prepareAsync()
            } catch (_: Exception) {
                if (current === player) current = null
                releasePlayer(player)
                if (continuation.isActive) continuation.resume(false)
            }
            continuation.invokeOnCancellation {
                if (current === player) current = null
                releasePlayer(player)
            }
        }
    }
}
