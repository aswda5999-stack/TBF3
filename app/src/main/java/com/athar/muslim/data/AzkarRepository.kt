package com.athar.muslim.data

import android.content.Context
import com.athar.muslim.model.ZikrItem
import org.json.JSONArray
import org.json.JSONObject

object AzkarRepository {
    private val cachedAzkarByCategory = mutableMapOf<String, List<ZikrItem>>()
    private var cachedDuas: List<ZikrItem>? = null
    private var cachedHadiths: List<ZikrItem>? = null

    fun getAzkarByCategory(context: Context, categoryKey: String): List<ZikrItem> {
        cachedAzkarByCategory[categoryKey]?.let { return it }
        val loaded = loadCategoryFile(context, categoryKey)
        cachedAzkarByCategory[categoryKey] = loaded
        return loaded
    }

    fun getAllAzkar(context: Context): List<ZikrItem> {
        val keys = listOf(
            "morning", "evening", "sleep", "prayer",
            "wakeup", "home", "travel", "food"
        )
        return keys.flatMap { getAzkarByCategory(context, it) }
    }

    fun getDuas(context: Context): List<ZikrItem> {
        cachedDuas?.let { return it }
        loadLibraryLists(context)
        return cachedDuas.orEmpty()
    }

    fun getHadiths(context: Context): List<ZikrItem> {
        cachedHadiths?.let { return it }
        loadLibraryLists(context)
        return cachedHadiths.orEmpty()
    }

    private fun loadCategoryFile(context: Context, categoryKey: String): List<ZikrItem> {
        val list = mutableListOf<ZikrItem>()
        val fileName = "azkar_${categoryKey}.json"
        try {
            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ZikrItem(
                        id = obj.getInt("id"),
                        title = obj.optString("title", ""),
                        text = obj.getString("text"),
                        count = obj.optInt("count", 1).coerceAtLeast(1),
                        benefit = obj.optString("benefit", "")
                    )
                )
            }
        } catch (_: Exception) {
            // إرجاع قائمة فارغة بأمان في حال عدم وجود ملف الفئة
        }
        return list
    }

    private fun loadLibraryLists(context: Context) {
        val duasList = mutableListOf<ZikrItem>()
        val hadithsList = mutableListOf<ZikrItem>()
        try {
            val jsonString = context.assets.open("library_content.json").bufferedReader().use { it.readText() }
            val root = JSONObject(jsonString)
            val duasArray = root.optJSONArray("duas") ?: JSONArray()
            for (i in 0 until duasArray.length()) {
                val obj = duasArray.getJSONObject(i)
                duasList.add(
                    ZikrItem(
                        id = obj.getInt("id"),
                        title = obj.optString("title", ""),
                        text = obj.getString("text"),
                        count = obj.optInt("count", 1).coerceAtLeast(1),
                        benefit = obj.optString("benefit", "")
                    )
                )
            }
            val hadithsArray = root.optJSONArray("hadiths") ?: JSONArray()
            for (i in 0 until hadithsArray.length()) {
                val obj = hadithsArray.getJSONObject(i)
                hadithsList.add(
                    ZikrItem(
                        id = obj.getInt("id"),
                        title = obj.optString("title", ""),
                        text = obj.getString("text"),
                        count = obj.optInt("count", 1).coerceAtLeast(1),
                        benefit = obj.optString("benefit", "")
                    )
                )
            }
        } catch (_: Exception) {
            // إرجاع قوائم آمنة
        }
        cachedDuas = duasList
        cachedHadiths = hadithsList
    }
}
