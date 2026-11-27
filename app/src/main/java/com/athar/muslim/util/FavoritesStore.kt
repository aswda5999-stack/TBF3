package com.athar.muslim.util

import android.content.SharedPreferences
import com.athar.muslim.model.ZikrItem
import org.json.JSONArray
import org.json.JSONObject

/** حفظ المفضلة (أذكار + أدعية + أحاديث) في SharedPreferences مع نصوصها لتعمل دون اتصال. */
object FavoritesStore {
    private const val KEY = "favorite_items_v1"

    fun load(prefs: SharedPreferences): List<ZikrItem> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            val result = mutableListOf<ZikrItem>()
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                result.add(
                    ZikrItem(
                        id = obj.optInt("id"),
                        title = obj.optString("title", ""),
                        text = obj.optString("text", ""),
                        count = obj.optInt("count", 1).coerceAtLeast(1),
                        benefit = obj.optString("benefit", ""),
                        category = obj.optString("category", "")
                    )
                )
            }
            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun save(prefs: SharedPreferences, items: List<ZikrItem>) {
        try {
            val array = JSONArray()
            items.forEach { item ->
                array.put(
                    JSONObject()
                        .put("id", item.id)
                        .put("title", item.title)
                        .put("text", item.text)
                        .put("count", item.count)
                        .put("benefit", item.benefit)
                        .put("category", item.category)
                )
            }
            prefs.edit().putString(KEY, array.toString()).apply()
        } catch (_: Exception) {
        }
    }
}
