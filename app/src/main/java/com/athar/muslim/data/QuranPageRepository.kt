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
    val text: String,
    val juz: Int = 0
) {
    val key: Int get() = surahNumber * 1000 + numberInSurah
}

/** جزء من نص الآية مع رمز حكم التجويد (null = نص عادي). */
data class TajweedRun(val text: String, val code: Char?)

/**
 * صفحات المصحف الـ 604 (نص عثماني + تجويد + التفسير الميسر من api.alquran.cloud)
 * مع تخزين محلي يعمل بعده دون اتصال، وتنزيل خط حفص من مجمع الملك فهد مرة واحدة.
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

    private const val API_PAGE = "https://api.alquran.cloud/v1/page"
    private const val FONT_URL =
        "https://cdn.jsdelivr.net/gh/thetruetruth/quran-data-kfgqpc@main/hafs/font/hafs.18.ttf"

    private val memory = ConcurrentHashMap<Int, List<PageAyah>>()
    private val tajweedMemory = ConcurrentHashMap<Int, Map<Int, List<TajweedRun>>>()
    private val tafsirMemory = ConcurrentHashMap<Int, Map<Int, String>>()

    fun startPageOf(surahNumber: Int): Int =
        SURAH_START_PAGES.getOrNull(surahNumber - 1) ?: 1

    fun cached(page: Int): List<PageAyah>? = memory[page]

    fun cachedTajweed(page: Int): Map<Int, List<TajweedRun>>? = tajweedMemory[page]

    fun cachedTafsir(page: Int): Map<Int, String>? = tafsirMemory[page]

    private fun stripDiacritics(word: String): String =
        word.filter { ch -> ch.code !in 0x064B..0x065F && ch.code != 0x0670 && ch.code != 0x0640 }

    private fun startsWithBasmala(firstWord: String): Boolean =
        stripDiacritics(firstWord).startsWith("بسم")

    /** يجلب نسخة (نص/تجويد/تفسير) لصفحة من الإنترنت أو من الملف المحفوظ. يُستدعى من خيط الخلفية. */
    private fun fetchEditionRaw(context: Context, page: Int, edition: String, prefix: String): String? {
        if (page < 1 || page > PAGE_COUNT) return null
        val file = File(File(context.filesDir, "quran_pages"), "${prefix}_$page.json")
        val cached = runCatching { if (file.exists()) file.readText() else null }.getOrNull()
        if (!cached.isNullOrBlank()) return cached

        var connection: HttpURLConnection? = null
        return try {
            connection = (URL("$API_PAGE/$page/$edition").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 12000
                setRequestProperty("Accept", "application/json")
            }
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return null
            val raw = connection.inputStream.bufferedReader().use { it.readText() }
            runCatching {
                file.parentFile?.mkdirs()
                file.writeText(raw)
            }
            raw
        } catch (_: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }

    private fun ayahObjects(raw: String): List<JSONObject> {
        val array = JSONObject(raw).optJSONObject("data")?.optJSONArray("ayahs") ?: return emptyList()
        val result = mutableListOf<JSONObject>()
        for (i in 0 until array.length()) {
            array.optJSONObject(i)?.let { result.add(it) }
        }
        return result
    }

    private fun surahOf(obj: JSONObject): Int = obj.optJSONObject("surah")?.optInt("number", 0) ?: 0

    // ===== النص العثماني =====

    private fun parsePage(raw: String): List<PageAyah>? {
        val result = mutableListOf<PageAyah>()
        for (obj in ayahObjects(raw)) {
            val surah = surahOf(obj)
            val number = obj.optInt("numberInSurah", 0)
            var text = obj.optString("text", "").trim()
            if (surah == 0 || number == 0 || text.isBlank()) continue
            // نسخة الواجهة تضيف البسملة لأول آية في السور، فنفصلها ونعرضها كعنوان
            if (number == 1 && surah != 1 && surah != 9) {
                val words = text.split(" ").filter { it.isNotBlank() }
                if (words.size > 4 && startsWithBasmala(words[0])) {
                    text = words.drop(4).joinToString(" ")
                }
            }
            result.add(PageAyah(surah, number, text, obj.optInt("juz", 0)))
        }
        return if (result.isEmpty()) null else result
    }

    /** يُستدعى من خيط الخلفية. يعيد null عند الفشل. */
    fun loadPage(context: Context, page: Int): List<PageAyah>? {
        memory[page]?.let { return it }
        val raw = fetchEditionRaw(context, page, "quran-uthmani", "page") ?: return null
        val parsed = runCatching { parsePage(raw) }.getOrNull() ?: return null
        memory[page] = parsed
        return parsed
    }

    // ===== التجويد (ترميز [h:1[ٱ] من نسخة quran-tajweed) =====

    private fun parseTajweedMarkup(raw: String): List<TajweedRun> {
        val runs = mutableListOf<TajweedRun>()
        val normal = StringBuilder()
        fun flushNormal() {
            if (normal.isNotEmpty()) {
                runs.add(TajweedRun(normal.toString(), null))
                normal.setLength(0)
            }
        }
        var i = 0
        while (i < raw.length) {
            val c = raw[i]
            if (c == '[' && i + 1 < raw.length && raw[i + 1].isLetter()) {
                val code = raw[i + 1]
                var j = i + 2
                if (j < raw.length && raw[j] == ':') {
                    j++
                    while (j < raw.length && raw[j].isDigit()) j++
                }
                if (j < raw.length && raw[j] == '[') {
                    val end = raw.indexOf(']', j + 1)
                    if (end > j) {
                        flushNormal()
                        runs.add(TajweedRun(raw.substring(j + 1, end), code))
                        i = end + 1
                        continue
                    }
                }
            }
            normal.append(c)
            i++
        }
        flushNormal()
        return runs
    }

    private fun stripBasmalaRuns(runs: List<TajweedRun>): List<TajweedRun> {
        val plain = runs.joinToString("") { it.text }
        var cut = -1
        var wordsSeen = 0
        var inWord = false
        for (index in plain.indices) {
            if (plain[index] == ' ') {
                inWord = false
            } else if (!inWord) {
                inWord = true
                wordsSeen++
                if (wordsSeen == 5) {
                    cut = index
                    break
                }
            }
        }
        if (cut < 0) return runs
        val firstSpace = plain.indexOf(' ')
        val firstWord = if (firstSpace < 0) plain else plain.substring(0, firstSpace)
        if (!startsWithBasmala(firstWord)) return runs

        val result = mutableListOf<TajweedRun>()
        var position = 0
        for (run in runs) {
            val end = position + run.text.length
            if (end > cut) {
                val startInRun = (cut - position).coerceAtLeast(0)
                val remaining = run.text.substring(startInRun)
                if (remaining.isNotEmpty()) result.add(TajweedRun(remaining, run.code))
            }
            position = end
        }
        return result
    }

    private fun parseTajweedPage(raw: String): Map<Int, List<TajweedRun>>? {
        val result = HashMap<Int, List<TajweedRun>>()
        for (obj in ayahObjects(raw)) {
            val surah = surahOf(obj)
            val number = obj.optInt("numberInSurah", 0)
            val text = obj.optString("text", "")
            if (surah == 0 || number == 0 || text.isBlank()) continue
            var runs = parseTajweedMarkup(text.trim())
            if (number == 1 && surah != 1 && surah != 9) runs = stripBasmalaRuns(runs)
            result[surah * 1000 + number] = runs
        }
        return if (result.isEmpty()) null else result
    }

    /** يُستدعى من خيط الخلفية. */
    fun loadTajweedPage(context: Context, page: Int): Map<Int, List<TajweedRun>>? {
        tajweedMemory[page]?.let { return it }
        val raw = fetchEditionRaw(context, page, "quran-tajweed", "tajweed") ?: return null
        val parsed = runCatching { parseTajweedPage(raw) }.getOrNull() ?: return null
        tajweedMemory[page] = parsed
        return parsed
    }

    // ===== التفسير الميسر =====

    private fun parseTafsirPage(raw: String): Map<Int, String>? {
        val result = HashMap<Int, String>()
        for (obj in ayahObjects(raw)) {
            val surah = surahOf(obj)
            val number = obj.optInt("numberInSurah", 0)
            val text = obj.optString("text", "").trim()
            if (surah == 0 || number == 0 || text.isBlank()) continue
            result[surah * 1000 + number] = text
        }
        return if (result.isEmpty()) null else result
    }

    /** يُستدعى من خيط الخلفية. */
    fun loadTafsirPage(context: Context, page: Int): Map<Int, String>? {
        tafsirMemory[page]?.let { return it }
        val raw = fetchEditionRaw(context, page, "ar.muyassar", "tafsir") ?: return null
        val parsed = runCatching { parseTafsirPage(raw) }.getOrNull() ?: return null
        tafsirMemory[page] = parsed
        return parsed
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
