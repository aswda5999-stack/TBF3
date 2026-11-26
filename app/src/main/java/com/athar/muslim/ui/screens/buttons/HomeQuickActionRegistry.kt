package com.athar.muslim.ui.screens.buttons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FormatListNumbered
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.LibraryBooks
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Share
import androidx.compose.ui.graphics.vector.ImageVector

enum class HomeQuickAction(
    val id: String,
    val title: String,
    val icon: ImageVector
) {
    QURAN(HomeButtonQuran.ID, HomeButtonQuran.TITLE, HomeButtonQuran.ICON),
    AUDIO("audio_voices", "الصوتيات", Icons.Rounded.Headphones),
    PRAYER("prayer_guide", "الصلاة", Icons.Rounded.Mosque),
    FOLLOW_US("follow_share", "تابعنا", Icons.Rounded.Share),
    HADITH("hadith_books", "الأحاديث", Icons.Rounded.LibraryBooks),
    AZKAR("azkar_categories", "الأذكار", Icons.Rounded.SelfImprovement),
    DUA("duas_list", "الأدعية", Icons.Rounded.FavoriteBorder),
    TASBIH("tasbih_counter", "المسبحة", Icons.Rounded.FormatListNumbered),
    QIBLA("qibla_compass", "البوصلة", Icons.Rounded.Explore)
}

object HomeQuickActionRegistry {
    fun orderedActions(): List<HomeQuickAction> = listOf(
        HomeQuickAction.QURAN,
        HomeQuickAction.AUDIO,
        HomeQuickAction.PRAYER,
        HomeQuickAction.FOLLOW_US,
        HomeQuickAction.HADITH,
        HomeQuickAction.AZKAR,
        HomeQuickAction.DUA,
        HomeQuickAction.TASBIH,
        HomeQuickAction.QIBLA
    )
}
