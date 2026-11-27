package com.athar.muslim.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FlightTakeoff
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Restaurant
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
    ),
    WAKEUP(
        key = "wakeup",
        title = "أذكار الاستيقاظ",
        subtitle = "عند الاستيقاظ من النوم",
        icon = Icons.Rounded.WbSunny,
        reminderHour = -1,
        reminderMinute = 0
    ),
    HOME(
        key = "home",
        title = "المنزل والمسجد",
        subtitle = "الخروج والدخول والمسجد والخلاء",
        icon = Icons.Rounded.Home,
        reminderHour = -1,
        reminderMinute = 0
    ),
    TRAVEL(
        key = "travel",
        title = "أذكار السفر",
        subtitle = "دعاء السفر والنزول",
        icon = Icons.Rounded.FlightTakeoff,
        reminderHour = -1,
        reminderMinute = 0
    ),
    FOOD(
        key = "food",
        title = "أذكار الطعام",
        subtitle = "قبل الأكل وبعده",
        icon = Icons.Rounded.Restaurant,
        reminderHour = -1,
        reminderMinute = 0
    )
}

enum class HadithTopic(
    val title: String,
    val subtitle: String,
    val remoteSlug: String,
    val icon: ImageVector,
    val isFavorites: Boolean = false
) {
    FAVORITES(
        title = "المفضلة",
        subtitle = "الأحاديث المحفوظة في مفضلتك",
        remoteSlug = "",
        icon = Icons.Rounded.Favorite,
        isFavorites = true
    ),
    BUKHARI(
        title = "صحيح البخاري",
        subtitle = "أحاديث مختارة ومرتبة من صحيح البخاري",
        remoteSlug = "bukhari",
        icon = Icons.Rounded.AutoStories
    ),
    MUSLIM(
        title = "صحيح مسلم",
        subtitle = "أحاديث مختارة ومرتبة من صحيح مسلم",
        remoteSlug = "muslim",
        icon = Icons.Rounded.AutoStories
    ),
    ABU_DAWUD(
        title = "سنن أبي داود",
        subtitle = "مختارات من سنن أبي داود",
        remoteSlug = "abu-daud",
        icon = Icons.Rounded.AutoStories
    ),
    TIRMIDHI(
        title = "جامع الترمذي",
        subtitle = "مختارات من جامع الترمذي",
        remoteSlug = "tirmidzi",
        icon = Icons.Rounded.AutoStories
    ),
    NASAI(
        title = "سنن النسائي",
        subtitle = "مختارات من سنن النسائي",
        remoteSlug = "nasai",
        icon = Icons.Rounded.AutoStories
    ),
    IBN_MAJAH(
        title = "سنن ابن ماجه",
        subtitle = "مختارات من سنن ابن ماجه",
        remoteSlug = "ibnu-majjah",
        icon = Icons.Rounded.AutoStories
    );

    companion object {
        val sixMainCategories: List<HadithTopic> = listOf(
            BUKHARI,
            MUSLIM,
            ABU_DAWUD,
            TIRMIDHI,
            NASAI,
            IBN_MAJAH
        )
    }
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
