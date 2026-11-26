package com.athar.muslim.data

import com.athar.muslim.model.HadithLoadResult
import com.athar.muslim.model.HadithTopic
import com.athar.muslim.model.ZikrItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object HadithRepository {

    suspend fun loadTopicHadiths(topic: HadithTopic): HadithLoadResult {
        if (topic.isFavorites) {
            return HadithLoadResult.Success(emptyList())
        }
        return withContext(Dispatchers.IO) {
            val remoteList = fetchFromPublicHadithApi(topic)
            if (remoteList.isNotEmpty()) {
                HadithLoadResult.Success(remoteList)
            } else {
                HadithLoadResult.Success(curatedHadithsForTopic(topic))
            }
        }
    }

    private fun fetchFromPublicHadithApi(topic: HadithTopic): List<ZikrItem> {
        if (topic.remoteSlug.isBlank()) return emptyList()
        var connection: HttpURLConnection? = null
        return try {
            val endpoint = "https://api.hadith.gading.dev/books/${topic.remoteSlug}?range=1-10"
            val url = URL(endpoint)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 6000
                readTimeout = 6000
                setRequestProperty("Accept", "application/json")
            }
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return emptyList()
            }
            val rawJson = connection.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(rawJson)
            val dataObj = root.optJSONObject("data") ?: return emptyList()
            val hadithsArr = dataObj.optJSONArray("hadiths") ?: return emptyList()

            val parsed = mutableListOf<ZikrItem>()
            for (i in 0 until hadithsArr.length()) {
                val entry = hadithsArr.optJSONObject(i) ?: continue
                val num = entry.optInt("number", i + 1)
                val arabText = entry.optString("arab", "").trim()
                if (arabText.isNotEmpty()) {
                    parsed.add(
                        ZikrItem(
                            id = (topic.ordinal + 1) * 10000 + num,
                            title = "${topic.title} • حديث رقم $num",
                            text = arabText,
                            count = 1,
                            benefit = "من ${topic.title} • رقم الحديث: $num",
                            category = topic.title
                        )
                    )
                }
            }
            parsed
        } catch (_: Exception) {
            emptyList()
        } finally {
            connection?.disconnect()
        }
    }

    private fun curatedHadithsForTopic(topic: HadithTopic): List<ZikrItem> {
        val baseId = (topic.ordinal + 1) * 10000
        return listOf(
            ZikrItem(
                id = baseId + 1,
                title = "${topic.title} • مختار 1",
                text = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى.",
                count = 1,
                benefit = "أساس الإخلاص واستحضار النية الصالحة في كل عمل.",
                category = topic.title
            ),
            ZikrItem(
                id = baseId + 2,
                title = "${topic.title} • مختار 2",
                text = "مَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَالْيَوْمِ الآخِرِ فَلْيَقُلْ خَيْرًا أَوْ لِيَصْمُتْ.",
                count = 1,
                benefit = "حفظ اللسان وصون الكلام عن اللغو والأذى.",
                category = topic.title
            ),
            ZikrItem(
                id = baseId + 3,
                title = "${topic.title} • مختار 3",
                text = "لا يُؤْمِنُ أَحَدُكُمْ حَتَّى يُحِبَّ لأَخِيهِ مَا يُحِبُّ لِنَفْسِهِ.",
                count = 1,
                benefit = "رسوخ الأخوة الإيمانية وسلامة الصدر.",
                category = topic.title
            ),
            ZikrItem(
                id = baseId + 4,
                title = "${topic.title} • مختار 4",
                text = "الْمُسْلِمُ مَنْ سَلِمَ الْمُسْلِمُونَ مِنْ لِسَانِهِ وَيَدِهِ.",
                count = 1,
                benefit = "الالتزام بحقوق الناس وكف الأذى قولاً وفعلاً.",
                category = topic.title
            ),
            ZikrItem(
                id = baseId + 5,
                title = "${topic.title} • مختار 5",
                text = "مَنْ سَلَكَ طَرِيقًا يَلْتَمِسُ فِيهِ عِلْمًا سَهَّلَ اللَّهُ لَهُ بِهِ طَرِيقًا إِلَى الْجَنَّةِ.",
                count = 1,
                benefit = "فضل طلب العلم النافع والسعي في تحصيله.",
                category = topic.title
            )
        )
    }
}
