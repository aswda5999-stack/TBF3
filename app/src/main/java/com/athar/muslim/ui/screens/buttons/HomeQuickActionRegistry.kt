package com.athar.muslim.ui.screens.buttons

import androidx.compose.ui.graphics.vector.ImageVector

enum class HomeQuickAction(
    val id: String,
    val title: String,
    val icon: ImageVector
) {
    QURAN(HomeButtonQuran.ID, HomeButtonQuran.TITLE, HomeButtonQuran.ICON),
    AUDIO(HomeButtonAudio.ID, HomeButtonAudio.TITLE, HomeButtonAudio.ICON),
    PRAYER(HomeButtonPrayer.ID, HomeButtonPrayer.TITLE, HomeButtonPrayer.ICON),
    FOLLOW_US(HomeButtonFollowUs.ID, HomeButtonFollowUs.TITLE, HomeButtonFollowUs.ICON),
    HADITH(HomeButtonHadith.ID, HomeButtonHadith.TITLE, HomeButtonHadith.ICON),
    AZKAR(HomeButtonAzkar.ID, HomeButtonAzkar.TITLE, HomeButtonAzkar.ICON),
    DUA(HomeButtonDua.ID, HomeButtonDua.TITLE, HomeButtonDua.ICON),
    TASBIH(HomeButtonTasbih.ID, HomeButtonTasbih.TITLE, HomeButtonTasbih.ICON),
    QIBLA(HomeButtonQibla.ID, HomeButtonQibla.TITLE, HomeButtonQibla.ICON)
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
