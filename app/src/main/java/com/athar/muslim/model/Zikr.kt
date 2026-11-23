package com.athar.muslim.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

data class Zikr(
    val id: Int,
    val category: String,
    val text: String,
    val count: Int,
    val benefit: String
)

enum class ZikrCategory(val key: String, val title: String, val icon: ImageVector) {
    MORNING("morning", "الصباح", Icons.Rounded.WbSunny),
    EVENING("evening", "المساء", Icons.Rounded.NightsStay),
    SLEEP("sleep", "النوم", Icons.Rounded.Bedtime),
    PRAYER("prayer", "الصلاة", Icons.Rounded.Mosque)
}
