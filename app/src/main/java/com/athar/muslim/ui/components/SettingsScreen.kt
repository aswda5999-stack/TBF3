package com.athar.muslim.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesomeMotion
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.athar.muslim.ui.components.IslamicArtworkBanner
import com.athar.muslim.ui.components.getTenIslamicSlides
import com.athar.muslim.ui.theme.AppThemePreset

private enum class SettingsSubPage(val title: String, val subtitle: String) {
    MENU("الضبط والإعدادات", "تخصيص المظهر، الصور، التنبيهات والمزيد"),
    THEMES("الثيمات والمظهر", "اختر من بين 5 ثيمات متكاملة تشمل كامل التطبيق"),
    GALLERY("معرض الصور الإسلامية", "تصفح الصور الـ 10 وضبط التقليب التلقائي"),
    NOTIFICATIONS("الإشعارات والتنبيهات", "إدارة تذكيرات الأذكار اليومية"),
    READING("القراءة والصوت والاهتزاز", "حجم الخط، صوت النقر، وتصفير العدادات"),
    FOLLOW_US("تابعنا", "قنوات التواصل ومشاركة تطبيق أثر مسلم"),
    ABOUT("عن التطبيق", "رسالة تطبيق أثر مسلم ومعلومات الإصدار")
}

@Composable
fun SettingsScreen(
    selectedTheme: AppThemePreset,
    onSelectTheme: (AppThemePreset) -> Unit,
    autoSlideEnabled: Boolean,
    onToggleAutoSlide: (Boolean) -> Unit,
    remindersEnabled: Boolean,
    onToggleReminders: (Boolean) -> Unit,
    soundEnabled: Boolean,
    onToggleSound: (Boolean) -> Unit,
    vibrationEnabled: Boolean,
    onToggleVibration: (Boolean) -> Unit,
    fontScaleStep: Int,
    onChangeFontScale: (Int) -> Unit,
    onResetAllCounts: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val slides = remember { getTenIslamicSlides() }
    var currentPage by remember { mutableStateOf(SettingsSubPage.MENU) }

    // الرجوع خطوة بخطوة داخل شاشة الضبط:
    // إذا كان المستخدم داخل صفحة فرعية (مثل الثيمات) يعود أولاً إلى قائمة الضبط الرئيسية
    BackHandler(enabled = true) {
        if (currentPage != SettingsSubPage.MENU) {
            currentPage = SettingsSubPage.MENU
        } else {
            onBack()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
    ) {
        // رأس الصفحة الديناميكي حسب القسم المفتوح
        Surface(
            color = scheme.surface,
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        if (currentPage != SettingsSubPage.MENU) {
                            currentPage = SettingsSubPage.MENU
                        } else {
                            onBack()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = "رجوع",
                        tint = scheme.primary
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentPage.title,
                        color = scheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = currentPage.subtitle,
                        color = scheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }

        when (currentPage) {
            SettingsSubPage.MENU -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
                ) {
                    // شريط عرض الصور الإسلامية داخل الضبط كما طلبت
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "معاينة الصور الإسلامية (10 لوحات)",
                                    color = scheme.onSurface,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Surface(
                                    onClick = { currentPage = SettingsSubPage.GALLERY },
                                    shape = RoundedCornerShape(50),
                                    color = scheme.primaryContainer
                                ) {
                                    Text(
                                        text = "عرض الكل",
                                        color = scheme.onPrimaryContainer,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(slides, key = { it.id }) { slide ->
                                    IslamicArtworkBanner(
                                        slide = slide,
                                        modifier = Modifier
                                            .width(240.dp)
                                            .height(104.dp)
                                    )
                                }
                            }
                        }
                    }

                    // عناصر الضبط كعناوين مستقلة تفتح كل واحدة شاشة خاصة
                    item {
                        SettingsMenuTile(
                            title = "الثيمات",
                            subtitle = "الثيم الحالي: ${selectedTheme.title} (5 ثيمات كاملة)",
                            icon = Icons.Rounded.Palette,
                            onClick = { currentPage = SettingsSubPage.THEMES }
                        )
                    }
                    item {
                        SettingsMenuTile(
                            title = "صور الواجهة العلوية",
                            subtitle = if (autoSlideEnabled) "التقليب التلقائي مفعل (كل 4 ثوانٍ)" else "التقليب التلقائي متوقف",
                            icon = Icons.Rounded.Collections,
                            onClick = { currentPage = SettingsSubPage.GALLERY }
                        )
                    }
                    item {
                        SettingsMenuTile(
                            title = "الإشعارات والتذكيرات",
                            subtitle = if (remindersEnabled) "إشعارات الصباح والمساء والنوم مفعلة" else "الإشعارات متوقفة",
                            icon = Icons.Rounded.NotificationsActive,
                            onClick = { currentPage = SettingsSubPage.NOTIFICATIONS }
                        )
                    }
                    item {
                        SettingsMenuTile(
                            title = "القراءة والصوت والعدادات",
                            subtitle = "حجم الخط، صوت النقر، الاهتزاز، وتصفير العدادات",
                            icon = Icons.Rounded.FormatSize,
                            onClick = { currentPage = SettingsSubPage.READING }
                        )
                    }
                    item {
                        SettingsMenuTile(
                            title = "تابعنا",
                            subtitle = "مشاركة التطبيق وقنوات التواصل والدعم",
                            icon = Icons.Rounded.Share,
                            onClick = { currentPage = SettingsSubPage.FOLLOW_US }
                        )
                    }
                    item {
                        SettingsMenuTile(
                            title = "عن التطبيق",
                            subtitle = "تعرف على رسالة تطبيق أثر مسلم ومميزاته",
                            icon = Icons.Rounded.Info,
                            onClick = { currentPage = SettingsSubPage.ABOUT }
                        )
                    }
                }
            }

            SettingsSubPage.THEMES -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
                ) {
                    items(AppThemePreset.entries, key = { it.id }) { preset ->
                        val isSelected = preset == selectedTheme
                        Surface(
                            onClick = { onSelectTheme(preset) },
                            shape = RoundedCornerShape(18.dp),
                            color = if (isSelected) scheme.primaryContainer else scheme.surface,
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.2.dp,
                                color = if (isSelected) scheme.tertiary else scheme.outlineVariant
                            ),
                            shadowElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(preset.swatchPrimary, preset.swatchSecondary)
                                                ),
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .align(Alignment.TopEnd)
                                                .padding(3.dp)
                                                .background(preset.swatchAccent, CircleShape)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = preset.title,
                                            color = scheme.onSurface,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = preset.description,
                                            color = scheme.onSurfaceVariant,
                                            fontSize = 12.sp,
                                            lineHeight = 17.sp
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = "مختار",
                                        tint = scheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            SettingsSubPage.GALLERY -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = scheme.surface),
                            border = BorderStroke(1.2.dp, scheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesomeMotion,
                                        contentDescription = null,
                                        tint = scheme.primary
                                    )
                                    Column {
                                        Text(
                                            text = "التقليب التلقائي كل 4 ثوانٍ",
                                            color = scheme.onSurface,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "تبديل تلقائي مستمر بين اللوحات العشر في الشاشة الرئيسية",
                                            color = scheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                Switch(
                                    checked = autoSlideEnabled,
                                    onCheckedChange = onToggleAutoSlide,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = scheme.onPrimary,
                                        checkedTrackColor = scheme.primary
                                    )
                                )
                            }
                        }
                    }

                    items(slides, key = { it.id }) { slide ->
                        IslamicArtworkBanner(
                            slide = slide,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(128.dp)
                        )
                    }
                }
            }

            SettingsSubPage.NOTIFICATIONS -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = scheme.surface),
                            border = BorderStroke(1.2.dp, scheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "تفعيل إشعارات الأذكار اليومية",
                                            color = scheme.onSurface,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "تذكير بأذكار الصباح والمساء والنوم في مواعيدها اليومية",
                                            color = scheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Switch(
                                        checked = remindersEnabled,
                                        onCheckedChange = onToggleReminders,
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = scheme.onPrimary,
                                            checkedTrackColor = scheme.primary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            SettingsSubPage.READING -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = scheme.surface),
                            border = BorderStroke(1.2.dp, scheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text(
                                    text = "حجم خط الأذكار والأدعية والأحاديث",
                                    color = scheme.onSurface,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val options = listOf(0 to "صغير", 1 to "متوسط", 2 to "كبير")
                                    options.forEach { (step, label) ->
                                        val selected = fontScaleStep == step
                                        Surface(
                                            onClick = { onChangeFontScale(step) },
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (selected) scheme.primary else scheme.primaryContainer,
                                            border = BorderStroke(
                                                1.dp,
                                                if (selected) scheme.tertiary else scheme.outlineVariant
                                            ),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = label,
                                                    color = if (selected) scheme.onPrimary else scheme.onPrimaryContainer,
                                                    fontSize = 13.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.6f))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Rounded.VolumeUp, contentDescription = null, tint = scheme.primary)
                                        Text("صوت النقر والإتمام", color = scheme.onSurface, fontWeight = FontWeight.Bold)
                                    }
                                    Switch(
                                        checked = soundEnabled,
                                        onCheckedChange = onToggleSound,
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = scheme.onPrimary,
                                            checkedTrackColor = scheme.primary
                                        )
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Rounded.Vibration, contentDescription = null, tint = scheme.primary)
                                        Text("الاهتزاز اللمسي", color = scheme.onSurface, fontWeight = FontWeight.Bold)
                                    }
                                    Switch(
                                        checked = vibrationEnabled,
                                        onCheckedChange = onToggleVibration,
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = scheme.onPrimary,
                                            checkedTrackColor = scheme.primary
                                        )
                                    )
                                }

                                HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.6f))

                                Button(
                                    onClick = onResetAllCounts,
                                    colors = ButtonDefaults.buttonColors(containerColor = scheme.primary),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Rounded.Refresh, contentDescription = null, tint = scheme.onPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("تصفير جميع العدادات الآن", color = scheme.onPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            SettingsSubPage.FOLLOW_US -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = scheme.surface),
                            border = BorderStroke(1.2.dp, scheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Language,
                                    contentDescription = null,
                                    tint = scheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    text = "شارك الأجر مع من تحب",
                                    color = scheme.onSurface,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "الدال على الخير كفاعله — انشر تطبيق أثر مسلم ليكون صدقة جارية لك ولمن تحب.",
                                    color = scheme.onSurfaceVariant,
                                    fontSize = 14.sp,
                                    lineHeight = 22.sp,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "تطبيق أثر مسلم — للأذكار والأدعية والأحاديث النبوية بتصميم هادئ ومريح."
                                            )
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "مشاركة أثر مسلم"))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = scheme.primary),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Rounded.Share, contentDescription = null, tint = scheme.onPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("مشاركة تطبيق أثر مسلم", color = scheme.onPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            SettingsSubPage.ABOUT -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = scheme.surface),
                            border = BorderStroke(1.2.dp, scheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Info,
                                    contentDescription = null,
                                    tint = scheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    text = "تطبيق أثر مسلم",
                                    color = scheme.onSurface,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "رفيقك اليومي للمحافظة على أذكار الصباح والمساء والنوم والأدعية المأثورة والأحاديث النبوية، مع 5 ثيمات كاملة وتجربة قراءة هادئة بدون أي تعقيد.",
                                    color = scheme.onSurfaceVariant,
                                    fontSize = 14.sp,
                                    lineHeight = 23.sp,
                                    textAlign = TextAlign.Center
                                )
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = scheme.primaryContainer
                                ) {
                                    Text(
                                        text = "الإصدار الاحترافي • أثر مسلم",
                                        color = scheme.onPrimaryContainer,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsMenuTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        border = BorderStroke(1.2.dp, scheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = scheme.primaryContainer,
                    border = BorderStroke(1.dp, scheme.outlineVariant),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = scheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = scheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = scheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.Rounded.ChevronLeft,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
