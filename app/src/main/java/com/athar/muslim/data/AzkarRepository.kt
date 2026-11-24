package com.athar.muslim.data

import android.content.Context
import com.athar.muslim.model.ZikrItem
import org.json.JSONArray
import org.json.JSONObject

object AzkarRepository {
    private var cachedAzkarByCategory: Map<String, List<ZikrItem>>? = null
    private var cachedDuas: List<ZikrItem>? = null
    private var cachedHadiths: List<ZikrItem>? = null

    fun getAzkarByCategory(context: Context, categoryKey: String): List<ZikrItem> {
        val map = cachedAzkarByCategory ?: loadAzkarMap(context).also { cachedAzkarByCategory = it }
        return map[categoryKey].orEmpty()
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

    private fun loadAzkarMap(context: Context): Map<String, List<ZikrItem>> {
        val grouped = mutableMapOf<String, MutableList<ZikrItem>>()
        try {
            val jsonString = context.assets.open("azkar.json").bufferedReader().use { it.readText() }
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val cat = obj.optString("category", "morning")
                val item = ZikrItem(
                    id = obj.getInt("id"),
                    title = obj.optString("title", ""),
                    text = obj.getString("text"),
                    count = obj.optInt("count", 1).coerceAtLeast(1),
                    benefit = obj.optString("benefit", "")
                )
                grouped.getOrPut(cat) { mutableListOf() }.add(item)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return grouped
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
        cachedDuas = duasList
        cachedHadiths = hadithsList
    }
}
