package com.athar.muslim.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val SkyBackground = Color(0xFFF0F9FF)      // سماوي هادئ جداً للخلفية
val SkySurface = Color(0xFFFFFFFF)         // أبيض ناصع للبطاقات
val SkyContainer = Color(0xFFE0F2FE)       // أزرق سماوي فاتح للبار العلوي والتحديد
val SkyAccent = Color(0xFFBAE6FD)          // أزرق فاتح للحدود والتقدم
val BluePrimary = Color(0xFF0284C7)        // أزرق سماوي متوسط للأزرار والعداد
val NavyDark = Color(0xFF0C4A6E)           // كحلي سماوي للنصوص والعناوين
val TextBody = Color(0xFF1E293B)           // لون مريح لقراءة النصوص الطويلة

private val AtharColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = SkyContainer,
    onPrimaryContainer = NavyDark,
    secondary = Color(0xFF38BDF8),
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
