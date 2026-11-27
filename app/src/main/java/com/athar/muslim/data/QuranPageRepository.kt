package com.athar.muslim.data

import android.content.Context
import android.graphics.Typeface
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

/** آية داخل صفحة المصحف. */
data class PageAyah(
    val surahNumber: Int,
    val numberInSurah: Int,
    val text: String
)

/**
 * صفحات المصحف الـ 604 (نص عثماني من api.alquran.cloud) مع تخزين محلي
 * يعمل بعده دون اتصال، وتنزيل خط حفص من مجمع الملك فهد مرة واحدة.
 */
object QuranPageRepository {
    const val PAGE_COUNT = 604

    /** صفحة بداية كل سورة في مصحف المدينة النبوية (١١٤ سورة). */
    val SURAH_START_PAGES: IntArray = intArrayOf(
        1, 2, 50, 77, 106, 128, 151, 177, 187, 208, 221, 235, 249, 255, 262, 267, 282, 293, 305, 312,
        322, 332, 342, 350, 359, 367, 377, 385, 396, 404, 411, 415, 418, 428, 434, 440, 446, 453, 458, 467,
        477, 483, 489, 496, 499, 502, 507, 511, 515, 518, 520, 523, 526, 528, 531, 534, 537, 542, 545, 549,
        551, 553, 554, 556, 558, 560, 562, 564, 566, 568, 570, 572, 574, 575, 577, 578, 580, 582, 583, 585,
        586, 587, 587, 589, 590, 591, 591, 592, 593, 594, 595, 595, 596, 596, 597, 597, 598, 598, 599, 599,
        600, 600, 601, 601, 601, 602, 602, 602, 603, 603, 603, 604, 604, 604
    )

    private const val FONT_URL =
        "https://cdn.jsdelivr.net/gh/thetruetruth/quran-data-kfgqpc@main/hafs/font/hafs.18.ttf"

    private val memory = ConcurrentHashMap<Int, List<PageAyah>>()

    fun startPageOf(surahNumber: Int): Int =
        SURAH_START_PAGES.getOrNull(surahNumber - 1) ?: 1

    fun cached(page: Int): List<PageAyah>? = memory[page]

    private fun pageFile(context: Context, page: Int) =
        File(File(context.filesDir, "quran_pages"), "page_$page.json")

    private fun stripDiacritics(word: String): String =
        word.filter { ch -> ch.code !in 0x064B..0x065F && ch.code != 0x0670 && ch.code != 0x0640 }

    private fun parsePage(raw: String): List<PageAyah>? {
        val ayahs = JSONObject(raw).optJSONObject("data")?.optJSONArray("ayahs") ?: return null
        val result = mutableListOf<PageAyah>()
        for (i in 0 until ayahs.length()) {
            val obj = ayahs.optJSONObject(i) ?: continue
            val surah = obj.optJSONObject("surah")?.optInt("number", 0) ?: 0
            val number = obj.optInt("numberInSurah", 0)
            var text = obj.optString("text", "").trim()
            if (surah == 0 || number == 0 || text.isBlank()) continue
            // نسخة الواجهة تضيف البسملة لأول آية في السور، فنفصلها ونعرضها كعنوان
            if (number == 1 && surah != 1 && surah != 9) {
                val words = text.split(" ").filter { it.isNotBlank() }
                if (words.size > 4 && stripDiacritics(words[0]).startsWith("بسم")) {
                    text = words.drop(4).joinToString(" ")
                }
            }
            result.add(PageAyah(surah, number, text))
        }
        return if (result.isEmpty()) null else result
    }

    /** يُستدعى من خيط الخلفية. يعيد null عند الفشل. */
    fun loadPage(context: Context, page: Int): List<PageAyah>? {
        if (page < 1 || page > PAGE_COUNT) return null
        memory[page]?.let { return it }

        val file = pageFile(context, page)
        val fromDisk = runCatching {
            if (file.exists()) parsePage(file.readText()) else null
        }.getOrNull()
        if (fromDisk != null) {
            memory[page] = fromDisk
            return fromDisk
        }

        var connection: HttpURLConnection? = null
        return try {
            val url = "https://api.alquran.cloud/v1/page/$page/quran-uthmani"
            connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 12000
                setRequestProperty("Accept", "application/json")
            }
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return null
            val raw = connection.inputStream.bufferedReader().use { it.readText() }
            val parsed = parsePage(raw) ?: return null
            runCatching {
                file.parentFile?.mkdirs()
                file.writeText(raw)
            }
            memory[page] = parsed
            parsed
        } catch (_: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }

    // ===== خط مجمع الملك فهد (حفص) =====

    private fun fontFile(context: Context) = File(File(context.filesDir, "fonts"), "hafs18.ttf")

    fun loadCachedTypeface(context: Context): Typeface? {
        return try {
            val file = fontFile(context)
            if (file.exists() && file.length() > 50_000L) Typeface.createFromFile(file) else null
        } catch (_: Exception) {
            null
        }
    }

    /** ينزّل الخط مرة واحدة ويحفظه. يُستدعى من خيط الخلفية. */
    fun downloadFont(context: Context): Typeface? {
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(FONT_URL).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 30000
            }
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return null
            val bytes = connection.inputStream.use { it.readBytes() }
            if (bytes.size < 50_000) return null
            val file = fontFile(context)
            file.parentFile?.mkdirs()
            file.writeBytes(bytes)
            loadCachedTypeface(context)
        } catch (_: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }
}
