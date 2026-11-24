package com.athar.muslim.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.NightsStay
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
    val icon: ImageVector
) {
    MORNING("morning", "أذكار الصباح", "تقال من الفجر حتى الزوال", Icons.Rounded.WbSunny),
    EVENING("evening", "أذكار المساء", "تقال من العصر حتى الليل", Icons.Rounded.NightsStay),
    SLEEP("sleep", "أذكار النوم", "تحصين وسكينة قبل النوم", Icons.Rounded.Bedtime),
    PRAYER("prayer", "أذكار بعد الصلاة", "تقال عقب الصلوات المكتوبة", Icons.Rounded.Mosque),
    WAKEUP("wakeup", "أذكار الاستيقاظ", "عند الاستيقاظ من النوم", Icons.Rounded.AutoStories),
    HOME("home", "أذكار المنزل", "دخول البيت والخروج منه", Icons.Rounded.Home)
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
