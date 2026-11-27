package com.athar.muslim.data

import android.content.Context
import com.athar.muslim.model.ZikrItem
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** كتاب حديث متاح عبر مستودع hadith-api المفتوح (نص عربي كامل). */
data class HadithBook(
    val edition: String,
    val title: String,
    val subtitle: String,
    val idBase: Int,
    val sectionCount: Int,
    val wholeFile: Boolean = false
)

data class HadithSection(
    val number: Int,
    val title: String,
    val items: List<ZikrItem>
)

sealed interface HadithSectionResult {
    data class Success(val section: HadithSection) : HadithSectionResult
    data object EndOfBook : HadithSectionResult
    data class Error(val message: String) : HadithSectionResult
}

object HadithBooksRepository {
    private const val BASE = "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions"

    val books: List<HadithBook> = listOf(
        HadithBook("ara-bukhari", "صحيح البخاري", "الكتاب كاملاً — ٩٧ كتاباً", 1, 97),
        HadithBook("ara-muslim", "صحيح مسلم", "الكتاب كاملاً", 2, 0),
        HadithBook("ara-abudawud", "سنن أبي داود", "الكتاب كاملاً مع حكم الحديث", 3, 0),
        HadithBook("ara-tirmidhi", "جامع الترمذي", "الكتاب كاملاً مع حكم الحديث", 4, 0),
        HadithBook("ara-nasai", "سنن النسائي", "الكتاب كاملاً مع حكم الحديث", 5, 0),
        HadithBook("ara-ibnmajah", "سنن ابن ماجه", "الكتاب كاملاً مع حكم الحديث", 6, 0),
        HadithBook("ara-nawawi", "الأربعون النووية", "الأحاديث الأربعون وزيادة", 7, 0, wholeFile = true)
    )

    /** أسماء كتب صحيح البخاري (٩٧ كتاباً). */
    val bukhariBookNames: List<String> = listOf(
        "بدء الوحي",
        "الإيمان",
        "العلم",
        "الوضوء",
        "الغسل",
        "الحيض",
        "التيمم",
        "الصلاة",
        "مواقيت الصلاة",
        "الأذان",
        "الجمعة",
        "صلاة الخوف",
        "العيدين",
        "الوتر",
        "الاستسقاء",
        "الكسوف",
        "سجود القرآن",
        "تقصير الصلاة",
        "التهجد",
        "فضل الصلاة في مسجد مكة والمدينة",
        "العمل في الصلاة",
        "السهو",
        "الجنائز",
        "الزكاة",
        "الحج",
        "العمرة",
        "المحصر",
        "جزاء الصيد",
        "فضائل المدينة",
        "الصوم",
        "صلاة التراويح",
        "فضل ليلة القدر",
        "الاعتكاف",
        "البيوع",
        "السلم",
        "الشفعة",
        "الإجارة",
        "الحوالات",
        "الكفالة",
        "الوكالة",
        "المزارعة",
        "المساقاة",
        "الاستقراض",
        "الخصومات",
        "اللقطة",
        "المظالم",
        "الشركة",
        "الرهن",
        "العتق",
        "المكاتب",
        "الهبة",
        "الشهادات",
        "الصلح",
        "الشروط",
        "الوصايا",
        "الجهاد والسير",
        "فرض الخمس",
        "الجزية والموادعة",
        "بدء الخلق",
        "أحاديث الأنبياء",
        "المناقب",
        "فضائل الصحابة",
        "مناقب الأنصار",
        "المغازي",
        "تفسير القرآن",
        "فضائل القرآن",
        "النكاح",
        "الطلاق",
        "النفقات",
        "الأطعمة",
        "العقيقة",
        "الذبائح والصيد",
        "الأضاحي",
        "الأشربة",
        "المرضى",
        "الطب",
        "اللباس",
        "الأدب",
        "الاستئذان",
        "الدعوات",
        "الرقاق",
        "القدر",
        "الأيمان والنذور",
        "كفارات الأيمان",
        "الفرائض",
        "الحدود",
        "الديات",
        "استتابة المرتدين",
        "الإكراه",
        "الحيل",
        "التعبير",
        "الفتن",
        "الأحكام",
        "التمني",
        "أخبار الآحاد",
        "الاعتصام بالكتاب والسنة",
        "التوحيد"
    )

    fun sectionLabel(book: HadithBook, number: Int): String {
        if (book.wholeFile) return book.title
        val name = if (book.edition == "ara-bukhari") bukhariBookNames.getOrNull(number - 1) else null
        return if (name != null) "كتاب " + name else "القسم $number"
    }

    private fun fetchText(url: String): String? {
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 20000
                setRequestProperty("Accept", "application/json")
            }
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                null
            } else {
                connection.inputStream.bufferedReader().use { it.readText() }
            }
        } catch (_: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }

    private fun gradeLabel(raw: String): String = when (raw.trim().lowercase()) {
        "sahih" -> "صحيح"
        "hasan" -> "حسن"
        "daif", "da'if", "daeef" -> "ضعيف"
        "hasan sahih" -> "حسن صحيح"
        "sahih lighairihi" -> "صحيح لغيره"
        "hasan lighairihi" -> "حسن لغيره"
        "maudu", "mawdu" -> "موضوع"
        else -> raw.trim()
    }

    private fun scholarLabel(raw: String): String = when (raw.trim()) {
        "Al-Albani" -> "الألباني"
        "Zubair Ali Zai" -> "زبير علي زئي"
        "Shuaib Al-Arnaut" -> "شعيب الأرناؤوط"
        else -> raw.trim()
    }

    private fun parseHadiths(book: HadithBook, array: org.json.JSONArray, onlySection: Int?): List<ZikrItem> {
        val result = mutableListOf<ZikrItem>()
        val seen = HashSet<Int>()
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            if (onlySection != null) {
                val sectionRef = obj.optJSONObject("reference")?.optInt("book", onlySection) ?: onlySection
                if (sectionRef != onlySection) continue
            }
            val number = obj.optDouble("hadithnumber", (i + 1).toDouble()).toInt()
            val text = obj.optString("text", "").trim()
            if (text.isBlank()) continue
            val id = book.idBase * 100000 + number
            if (!seen.add(id)) continue

            val grades = obj.optJSONArray("grades")
            val gradeParts = mutableListOf<String>()
            if (grades != null) {
                for (g in 0 until grades.length()) {
                    val gObj = grades.optJSONObject(g) ?: continue
                    val grade = gObj.optString("grade", "")
                    if (grade.isNotBlank()) {
                        val scholar = gObj.optString("name", "")
                        gradeParts.add(if (scholar.isBlank()) gradeLabel(grade) else scholarLabel(scholar) + ": " + gradeLabel(grade))
                    }
                }
            }
            val benefit = if (gradeParts.isEmpty()) {
                book.title + " • رقم " + number
            } else {
                book.title + " • رقم " + number + "\nالحكم: " + gradeParts.joinToString("، ")
            }
            result.add(
                ZikrItem(
                    id = id,
                    title = "حديث رقم $number",
                    text = text,
                    count = 1,
                    benefit = benefit,
                    category = book.title
                )
            )
        }
        return result
    }

    /** يحمّل قسماً (كتاباً) من كتب الحديث. يُحفظ محلياً بعد أول تحميل. يُستدعى من خيط الخلفية. */
    fun loadSection(context: Context, book: HadithBook, number: Int): HadithSectionResult {
        val dir = File(context.filesDir, "hadith")
        val cacheFile = File(dir, "${book.edition}_$number.json")

        var raw: String? = runCatching {
            if (cacheFile.exists()) cacheFile.readText() else null
        }.getOrNull()

        if (raw.isNullOrBlank()) {
            val urlBase = if (book.wholeFile) "$BASE/${book.edition}" else "$BASE/${book.edition}/sections/$number"
            raw = fetchText("$urlBase.min.json") ?: fetchText("$urlBase.json")
            if (raw.isNullOrBlank()) {
                return if (number > 1 && !book.wholeFile) {
                    HadithSectionResult.EndOfBook
                } else {
                    HadithSectionResult.Error("تعذّر التحميل. تأكد من اتصال الإنترنت وأعد المحاولة، وبعد أول تحميل يعمل هذا القسم دون اتصال.")
                }
            }
            runCatching {
                dir.mkdirs()
                cacheFile.writeText(raw)
            }
        }

        return try {
            val root = JSONObject(raw)
            val hadithsArray = root.optJSONArray("hadiths")
                ?: return HadithSectionResult.Error("صيغة البيانات غير متوقعة.")
            val items = parseHadiths(book, hadithsArray, null)
            if (items.isEmpty()) {
                runCatching { cacheFile.delete() }
                if (number > 1) HadithSectionResult.EndOfBook else HadithSectionResult.Error("لا توجد أحاديث في هذا القسم.")
            } else {
                HadithSectionResult.Success(HadithSection(number, sectionLabel(book, number), items))
            }
        } catch (e: Exception) {
            runCatching { cacheFile.delete() }
            HadithSectionResult.Error("تعذّر قراءة البيانات. أعد المحاولة.")
        }
    }
}
