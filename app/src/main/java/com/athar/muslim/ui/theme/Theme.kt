package com.athar.muslim.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val SkyBackground = Color(0xFFEAF6FF)      // خلفية سماوية فاتحة مريحة
val SkyTopStart = Color(0xFFBEE3FF)        // أعلى السماء الفاتحة
val SkyTopMid = Color(0xFFE3F3FF)          // وسط السماء المزخرفة
val SkySurface = Color(0xFFFFFFFF)         // أبيض ناصع للبطاقات
val SkyContainer = Color(0xFFD6EEFF)       // سماوي فاتح للشارات والفضائل
val SkyBorder = Color(0xFF9ACDF8)          // إطار أزرق فاتح للبطاقات
val SkyOrnament = Color(0xFFC2E3FC)        // زخرفة إسلامية سماوية ناعمة
val BlueRoyal = Color(0xFF00569E)          // أزرق غني للأزرار والشارات البارزة
val BluePrimary = Color(0xFF0277BD)        // أزرق متوسط للأيقونات والتفاصيل
val GoldAccent = Color(0xFFDAA520)         // لمسة ذهبية هادئة للشارات والإطار
val NavyDark = Color(0xFF073B68)           // كحلي سماوي واضح للعناوين
val TextBody = Color(0xFF102A43)           // لون نص واضح ومريح للعين

private val AtharColorScheme = lightColorScheme(
    primary = BlueRoyal,
    onPrimary = Color.White,
    primaryContainer = SkyContainer,
    onPrimaryContainer = NavyDark,
    secondary = BluePrimary,
    background = SkyBackground,
    surface = SkySurface,
    onSurface = TextBody
)

@Composable
fun AtharMuslimTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AtharColorScheme,
        content = content
    )
}
