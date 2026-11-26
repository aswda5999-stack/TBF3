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
    val title: String = "",
    val text: String,
    val count: Int,
    val benefit: String = "",
    val category: String = ""
)

enum class AzkarSubCategory(
    val key: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val reminderHour: Int,
    val reminderMinute: Int
) {
    MORNING(
        key = "morning",
        title = "أذكار الصباح",
        subtitle = "تبدأ من الفجر وحتى الزوال",
        icon = Icons.Rounded.WbSunny,
        reminderHour = 7,
        reminderMinute = 0
    ),
    EVENING(
        key = "evening",
        title = "أذكار المساء",
        subtitle = "تبدأ من العصر وحتى الليل",
        icon = Icons.Rounded.WbTwilight,
        reminderHour = 17,
        reminderMinute = 0
    ),
    SLEEP(
        key = "sleep",
        title = "أذكار النوم",
        subtitle = "تُقال قبل النوم وبعد الاستيقاظ",
        icon = Icons.Rounded.Bedtime,
        reminderHour = 22,
        reminderMinute = 0
    ),
    PRAYER(
        key = "prayer",
        title = "أذكار بعد الصلاة",
        subtitle = "عقب الصلوات المكتوبة",
        icon = Icons.Rounded.Mosque,
        reminderHour = 13,
        reminderMinute = 0
    ),
    GENERAL(
        key = "general",
        title = "أذكار متنوعة",
        subtitle = "تسبيح واستغفار وتهليل",
        icon = Icons.Rounded.MenuBook,
        reminderHour = 10,
        reminderMinute = 0
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
    BUKHARI(
        title = "صحيح البخاري",
        subtitle = "مختارات من صحيح الإمام البخاري",
        queryTerm = "البخاري",
        icon = Icons.Rounded.AutoStories
    ),
    PROPHET(
        title = "سيرة وهدي النبي ﷺ",
        subtitle = "أحاديث في أخلاق النبي ﷺ وهديه",
        queryTerm = "النبي",
        icon = Icons.Rounded.AutoStories
    ),
    MUSLIM(
        title = "صحيح مسلم",
        subtitle = "مختارات من صحيح الإمام مسلم",
        queryTerm = "مسلم",
        icon = Icons.Rounded.AutoStories
    )
}

enum class VoiceReminderIntensity(
    val id: String,
    val label: String,
    val volumeFloat: Float,
    val intervalMinutes: Long
) {
    LOW("low", "منخفض", 0.35f, 60L),
    MEDIUM("medium", "متوسط", 0.68f, 30L),
    HIGH("high", "مرتفع", 1.0f, 15L);

    companion object {
        fun fromId(value: String?): VoiceReminderIntensity =
            entries.firstOrNull { it.id == value } ?: MEDIUM
    }
}

sealed interface HadithLoadResult {
    data class Success(val items: List<ZikrItem>) : HadithLoadResult
    data class Error(val message: String) : HadithLoadResult
    data object UnderDevelopment : HadithLoadResult
}
