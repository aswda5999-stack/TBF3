package com.athar.muslim.data

import com.athar.muslim.model.HadithLoadResult
import com.athar.muslim.model.HadithTopic
import com.athar.muslim.model.ZikrItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object HadithRepository {
    private val cache = mutableMapOf<HadithTopic, List<ZikrItem>>()

    private fun primaryUrlsFor(topic: HadithTopic): List<String> = when (topic) {
        HadithTopic.BUKHARI -> listOf(
            "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/ara-bukhari/1.json",
            "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/ara-bukhari/2.json"
        )
        HadithTopic.PROPHET -> listOf(
            "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/ara-bukhari/3.json",
            "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/ara-muslim/1.json"
        )
        HadithTopic.MUSLIM -> listOf(
            "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/ara-muslim/2.json",
            "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/ara-muslim/3.json"
        )
        HadithTopic.FAVORITES -> emptyList()
    }

    private fun backupUrlsFor(topic: HadithTopic): List<String> = when (topic) {
        HadithTopic.BUKHARI -> listOf(
            "https://raw.githubusercontent.com/fawazahmed0/hadith-api/1/editions/ara-bukhari/1.json",
            "https://raw.githubusercontent.com/fawazahmed0/hadith-api/1/editions/ara-bukhari/2.json"
        )
        HadithTopic.PROPHET -> listOf(
            "https://raw.githubusercontent.com/fawazahmed0/hadith-api/1/editions/ara-bukhari/3.json",
            "https://raw.githubusercontent.com/fawazahmed0/hadith-api/1/editions/ara-muslim/1.json"
        )
        HadithTopic.MUSLIM -> listOf(
            "https://raw.githubusercontent.com/fawazahmed0/hadith-api/1/editions/ara-muslim/2.json",
            "https://raw.githubusercontent.com/fawazahmed0/hadith-api/1/editions/ara-muslim/3.json"
        )
        HadithTopic.FAVORITES -> emptyList()
    }

    suspend fun loadTopicHadiths(topic: HadithTopic): HadithLoadResult = withContext(Dispatchers.IO) {
        cache[topic]?.let { cached ->
            if (cached.isNotEmpty()) return@withContext HadithLoadResult.Success(cached)
        }

        // 1. محاولة المصدر الأساسي
        val primaryItems = fetchFromUrlList(primaryUrlsFor(topic), topic)
        if (primaryItems.isNotEmpty()) {
            cache[topic] = primaryItems
            return@withContext HadithLoadResult.Success(primaryItems)
        }

        // 2. الانتقال للمصدر الاحتياطي في حال وجود خلل في المصدر الأساسي
        val backupItems = fetchFromUrlList(backupUrlsFor(topic), topic)
        if (backupItems.isNotEmpty()) {
            cache[topic] = backupItems
            return@withContext HadithLoadResult.Success(backupItems)
        }

        // 3. في حال فشل المصدرين معاً
        HadithLoadResult.UnderDevelopment
    }

    private fun fetchFromUrlList(urls: List<String>, topic: HadithTopic): List<ZikrItem> {
        val result = mutableListOf<ZikrItem>()
        for (urlStr in urls) {
            try {
                val jsonText = downloadText(urlStr) ?: continue
                val parsed = parseHadithsJson(jsonText, topic, result.size)
                result.addAll(parsed)
                if (result.size >= 15) break
            } catch (_: Exception) {
                // ننتقل للرابط التالي أو المصدر الاحتياطي
            }
        }
        return result.take(15)
    }

    private fun downloadText(urlStr: String): String? {
        val connection = (URL(urlStr).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 7000
            readTimeout = 8000
            setRequestProperty("Accept", "application/json")
        }
        return try {
            if (connection.responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                null
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun parseHadithsJson(
        jsonText: String,
        topic: HadithTopic,
        startOffset: Int
    ): List<ZikrItem> {
        val list = mutableListOf<ZikrItem>()
        val root = JSONObject(jsonText)
        val hadithsArray = root.optJSONArray("hadiths") ?: JSONArray()
        val sectionName = root.optJSONObject("metadata")?.optString("section")?.takeIf { it.isNotBlank() }
            ?: topic.title

        for (i in 0 until hadithsArray.length()) {
            val obj = hadithsArray.optJSONObject(i) ?: continue
            val text = obj.optString("text", "").trim()
            if (text.isEmpty()) continue
            val hadithNum = obj.optInt("hadithnumber", i + 1)
            val idBase = when (topic) {
                HadithTopic.BUKHARI -> 10000
                HadithTopic.PROPHET -> 20000
                HadithTopic.MUSLIM -> 30000
                HadithTopic.FAVORITES -> 40000
            }
            list.add(
                ZikrItem(
                    id = idBase + startOffset + i + 1,
                    title = "${topic.title} • حديث رقم $hadithNum",
                    text = text,
                    count = 1,
                    benefit = sectionName
                )
            )
            if (list.size >= 15) break
        }
        return list
    }
}
