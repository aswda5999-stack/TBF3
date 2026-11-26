package com.athar.muslim.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class MainBottomTab(
    val label: String,
    val icon: ImageVector
) {
    HOME(
        label = "الرئيسية",
        icon = Icons.Rounded.Home
    ),
    AZKAR(
        label = "الأذكار",
        icon = Icons.Rounded.MenuBook
    ),
    DUA(
        label = "الأدعية",
        icon = Icons.Rounded.AutoStories
    ),
    SETTINGS(
        label = "الاعدادات",
        icon = Icons.Rounded.Settings
    )
}
