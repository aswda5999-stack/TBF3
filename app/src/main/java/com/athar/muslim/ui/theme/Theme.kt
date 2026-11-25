package com.athar.muslim.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppThemePreset(
    val id: String,
    val title: String,
    val description: String,
    val swatchPrimary: Color,
    val swatchSecondary: Color,
    val swatchAccent: Color
) {
    SKY_CLASSIC(
        id = "sky_classic",
        title = "السماء الهادئة",
        description = "الافتراضي — أزرق سماوي ملكي مع لمسة ذهبية مريحة للعين",
        swatchPrimary = Color(0xFF0F4C81),
        swatchSecondary = Color(0xFF1D70B8),
        swatchAccent = Color(0xFFD4A017)
    ),
    EMERALD_HARAM(
        id = "emerald_haram",
        title = "زمرد المساجد",
        description = "أخضر زمردي أصيل مستوحى من أروقة المساجد والسكينة",
        swatchPrimary = Color(0xFF0E5A46),
        swatchSecondary = Color(0xFF1B7F63),
        swatchAccent = Color(0xFFE0B13B)
    ),
    SAND_QIBLA(
        id = "sand_qibla",
        title = "رمال القبلة",
        description = "درجات دافئة هادئة من الرمال والبرونز لقراءة مريحة نهاراً",
        swatchPrimary = Color(0xFF6E4726),
        swatchSecondary = Color(0xFF96643A),
        swatchAccent = Color(0xFFC9933B)
    ),
    NIGHT_TAHAJJUD(
        id = "night_tahajjud",
        title = "ليل التهجد",
        description = "وضع ليلي داكن عالي الراحة للقراءة في الإضاءة المنخفضة",
        swatchPrimary = Color(0xFF132238),
        swatchSecondary = Color(0xFF1E3A5F),
        swatchAccent = Color(0xFFE5B84B)
    ),
    ROYAL_MARBLE(
        id = "royal_marble",
        title = "رخام المحراب",
        description = "تناغم نيلي وزمردي ناعم مع إطار ذهبي أنيق",
        swatchPrimary = Color(0xFF23395D),
        swatchSecondary = Color(0xFF2F5D62),
        swatchAccent = Color(0xFFD9A441)
    );

    companion object {
        fun fromId(value: String?): AppThemePreset {
            return entries.firstOrNull { it.id == value } ?: SKY_CLASSIC
        }
    }
}

// ألوان مساعدة متوافقة مع بقية الشاشات
val BlueRoyal = Color(0xFF0F4C81)
val NavyDark = Color(0xFF0B2545)
val GoldAccent = Color(0xFFD4A017)
val SkyBackground = Color(0xFFF2F7FC)
val SkySurface = Color(0xFFFFFFFF)
val SkyContainer = Color(0xFFE1EFFA)
val SkyBorder = Color(0xFFBBD6EE)
val TextBody = Color(0xFF1E293B)

fun colorSchemeForPreset(preset: AppThemePreset): ColorScheme = when (preset) {
    AppThemePreset.SKY_CLASSIC -> lightColorScheme(
        primary = Color(0xFF0F4C81),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE1EFFA),
        onPrimaryContainer = Color(0xFF0B2545),
        secondary = Color(0xFF1D70B8),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFD6EAFA),
        onSecondaryContainer = Color(0xFF0C2D48),
        tertiary = Color(0xFFD4A017),
        onTertiary = Color(0xFF1A1405),
        background = Color(0xFFF2F7FC),
        onBackground = Color(0xFF1E293B),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF0B2545),
        surfaceVariant = Color(0xFFE8F1F8),
        onSurfaceVariant = Color(0xFF334155),
        outlineVariant = Color(0xFFBBD6EE)
    )

    AppThemePreset.EMERALD_HARAM -> lightColorScheme(
        primary = Color(0xFF0E5A46),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFDDF3EC),
        onPrimaryContainer = Color(0xFF07362A),
        secondary = Color(0xFF1B7F63),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFCBECE1),
        onSecondaryContainer = Color(0xFF083A2D),
        tertiary = Color(0xFFE0B13B),
        onTertiary = Color(0xFF1F1704),
        background = Color(0xFFF1F8F5),
        onBackground = Color(0xFF162722),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF07362A),
        surfaceVariant = Color(0xFFE3F2EC),
        onSurfaceVariant = Color(0xFF2D473F),
        outlineVariant = Color(0xFFB2D8CA)
    )

    AppThemePreset.SAND_QIBLA -> lightColorScheme(
        primary = Color(0xFF6E4726),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFF6E9DC),
        onPrimaryContainer = Color(0xFF3B2310),
        secondary = Color(0xFF96643A),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFEFDEC9),
        onSecondaryContainer = Color(0xFF452A14),
        tertiary = Color(0xFFC9933B),
        onTertiary = Color(0xFF1E1404),
        background = Color(0xFFFAF6F0),
        onBackground = Color(0xFF2B2118),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF3B2310),
        surfaceVariant = Color(0xFFF3EAE0),
        onSurfaceVariant = Color(0xFF4E3D2F),
        outlineVariant = Color(0xFFDDC7AE)
    )

    AppThemePreset.NIGHT_TAHAJJUD -> darkColorScheme(
        primary = Color(0xFF4EA8DE),
        onPrimary = Color(0xFF061322),
        primaryContainer = Color(0xFF132238),
        onPrimaryContainer = Color(0xFFD6ECFA),
        secondary = Color(0xFF3A7CA5),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFF192D46),
        onSecondaryContainer = Color(0xFFC7E3F5),
        tertiary = Color(0xFFE5B84B),
        onTertiary = Color(0xFF1F1602),
        background = Color(0xFF0B1320),
        onBackground = Color(0xFFE6EEF8),
        surface = Color(0xFF121D2E),
        onSurface = Color(0xFFE6EEF8),
        surfaceVariant = Color(0xFF19283E),
        onSurfaceVariant = Color(0xFFA9BDD4),
        outlineVariant = Color(0xFF273B57)
    )

    AppThemePreset.ROYAL_MARBLE -> lightColorScheme(
        primary = Color(0xFF23395D),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE2E8F0),
        onPrimaryContainer = Color(0xFF122038),
        secondary = Color(0xFF2F5D62),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFD9ECEE),
        onSecondaryContainer = Color(0xFF133336),
        tertiary = Color(0xFFD9A441),
        onTertiary = Color(0xFF1D1403),
        background = Color(0xFFF4F6F9),
        onBackground = Color(0xFF1C2430),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF122038),
        surfaceVariant = Color(0xFFE8EDF3),
        onSurfaceVariant = Color(0xFF3A4656),
        outlineVariant = Color(0xFFC5D0DE)
    )
}

@Composable
fun AtharMuslimTheme(
    themePreset: AppThemePreset = AppThemePreset.SKY_CLASSIC,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = colorSchemeForPreset(themePreset),
        content = content
    )
}
