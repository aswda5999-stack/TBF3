package com.athar.muslim.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

data class ZikrItem(
    val id: Int,
    val title: String = "",
    val text: String,
    val count: Int,
    val benefit: String = ""
)

enum class AzkarSubCategory(
    val key: String,
    val title: String,
    val subtitle: String,
    val reminderHour: Int,
    val reminderMinute: Int,
    val icon: ImageVector
) {
    MORNING(
        key = "morning",
        title = "أذكار الصباح",
        subtitle = "تقال من الفجر حتى الزوال • تذكير 07:00 ص",
        reminderHour = 7,
        reminderMinute = 0,
        icon = Icons.Rounded.WbSunny
    ),
    EVENING(
        key = "evening",
        title = "أذكار المساء",
        subtitle = "تقال من العصر حتى الليل • تذكير 04:30 م",
        reminderHour = 16,
        reminderMinute = 30,
        icon = Icons.Rounded.NightsStay
    ),
    SLEEP(
        key = "sleep",
        title = "أذكار النوم",
        subtitle = "تحصين وسكينة قبل النوم • تذكير 10:30 م",
        reminderHour = 22,
        reminderMinute = 30,
        icon = Icons.Rounded.Bedtime
    ),
    PRAYER(
        key = "prayer",
        title = "أذكار بعد الصلاة",
        subtitle = "تقال عقب الصلوات المكتوبة",
        reminderHour = 13,
        reminderMinute = 0,
        icon = Icons.Rounded.Mosque
    ),
    WAKEUP(
        key = "wakeup",
        title = "أذكار الاستيقاظ",
        subtitle = "عند الاستيقاظ من النوم",
        reminderHour = 6,
        reminderMinute = 30,
        icon = Icons.Rounded.AutoStories
    ),
    HOME(
        key = "home",
        title = "أذكار المنزل",
        subtitle = "دخول البيت والخروج منه",
        reminderHour = 18,
        reminderMinute = 0,
        icon = Icons.Rounded.Home
    ),
    TRAVEL(
        key = "travel",
        title = "أذكار السفر",
        subtitle = "الركوب والتنقل وحفظ الطريق",
        reminderHour = 9,
        reminderMinute = 0,
        icon = Icons.Rounded.DirectionsCar
    ),
    FOOD(
        key = "food",
        title = "أذكار الطعام",
        subtitle = "قبل الطعام وبعد الفراغ منه",
        reminderHour = 14,
        reminderMinute = 0,
        icon = Icons.Rounded.Restaurant
    )
}

enum class MainBottomTab(
    val key: String,
    val label: String,
    val icon: ImageVector
) {
    FOLLOW_US("follow", "تابعنا", Icons.Rounded.MenuBook),
    HADITH("hadith", "الأحاديث", Icons.Rounded.AutoStories),
    DUA("dua", "الدعاء", Icons.Rounded.Mosque),
    AZKAR("azkar", "الأذكار", Icons.Rounded.WbSunny)
}

enum class HadithTopic(
    val key: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val isFavorites: Boolean = false
) {
    BUKHARI(
        key = "bukhari",
        title = "صحيح البخاري",
        subtitle = "أحاديث مختارة من صحيح الإمام البخاري",
        icon = Icons.Rounded.MenuBook
    ),
    PROPHET(
        key = "prophet",
        title = "الرسول ﷺ",
        subtitle = "هَدْي النبي محمد ﷺ وسيرته ووصاياه",
        icon = Icons.Rounded.AutoAwesome
    ),
    MUSLIM(
        key = "muslim",
        title = "صحيح مسلم",
        subtitle = "أحاديث مختارة من صحيح الإمام مسلم",
        icon = Icons.Rounded.AutoStories
    ),
    FAVORITES(
        key = "favorites",
        title = "المفضلة",
        subtitle = "الأحاديث التي قمت بحفظها",
        icon = Icons.Rounded.Favorite,
        isFavorites = true
    )
}

sealed class HadithLoadResult {
    data class Success(val items: List<ZikrItem>) : HadithLoadResult()
    data object UnderDevelopment : HadithLoadResult()
}
