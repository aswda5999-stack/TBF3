package com.athar.muslim.data

import android.content.Context
import com.athar.muslim.model.Zikr
import org.json.JSONArray

object AzkarRepository {
    private var cachedAzkar: List<Zikr>? = null

    fun getAzkarByCategory(context: Context, categoryKey: String): List<Zikr> {
        val all = cachedAzkar ?: loadFromAssets(context).also { cachedAzkar = it }
        return all.filter { it.category == categoryKey }
    }

    private fun loadFromAssets(context: Context): List<Zikr> {
        val list = mutableListOf<Zikr>()
        try {
            val jsonString = context.assets.open("azkar.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    Zikr(
                        id = obj.getInt("id"),
                        category = obj.getString("category"),
                        text = obj.getString("text"),
                        count = obj.getInt("count"),
                        benefit = obj.optString("benefit", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}

