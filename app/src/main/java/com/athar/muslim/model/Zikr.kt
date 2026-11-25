package com.athar.muslim.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.ui.graphics.vector.ImageVector

data class ZikrItem(
    val id: Int,
    val text: String,
    val count: Int,
    val benefit: String = "",
    val category: String = ""
)

enum class AzkarSubCategory(
    val key: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector
) {
    MORNING(
        key = "morning",
        title = "أذكار الصباح",
        subtitle = "تبدأ من الفجر وحتى الزوال",
        icon = Icons.Rounded.WbSunny
    ),
    EVENING(
        key = "evening",
        title = "أذكار المساء",
        subtitle = "تبدأ من العصر وحتى الليل",
        icon = Icons.Rounded.WbTwilight
    ),
    SLEEP(
        key = "sleep",
        title = "أذكار النوم",
        subtitle = "تُقال قبل النوم وبعد الاستيقاظ",
        icon = Icons.Rounded.Bedtime
    ),
    PRAYER(
        key = "prayer",
        title = "أذكار بعد الصلاة",
        subtitle = "عقب الصلوات المكتوبة",
        icon = Icons.Rounded.Mosque
    ),
    GENERAL(
        key = "general",
        title = "أذكار متنوعة",
        subtitle = "تسبيح واستغفار وتهليل",
        icon = Icons.Rounded.MenuBook
    )
}

enum class HadithTopic(
    val title: String,
    val subtitle: String,
    val queryTerm: String,
    val icon: ImageVector,
    val isFavorites: Boolean = false
) {
    FAVORITES(
        title = "المفضلة",
        subtitle = "الأحاديث المحفوظة لديك",
        queryTerm = "",
        icon = Icons.Rounded.Favorite,
        isFavorites = true
    ),
    INTENTION(
        title = "النية والإخلاص",
        subtitle = "أحاديث في صدق العمل لله",
        queryTerm = "النية",
        icon = Icons.Rounded.AutoStories
    ),
    GOOD_MANNERS(
        title = "حسن الخلق",
        subtitle = "أحاديث الرحمة والرفق والكلمة الطيبة",
        queryTerm = "الخلق",
        icon = Icons.Rounded.AutoStories
    ),
    REMEMBRANCE(
        title = "فضل الذكر",
        subtitle = "أحاديث التسبيح والذكر والاستغفار",
        queryTerm = "الذكر",
        icon = Icons.Rounded.AutoStories
    )
}

sealed interface HadithLoadResult {
    data class Success(val items: List<ZikrItem>) : HadithLoadResult
    data class Error(val message: String) : HadithLoadResult
}
