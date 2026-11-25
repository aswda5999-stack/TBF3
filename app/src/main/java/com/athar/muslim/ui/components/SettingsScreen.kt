package com.athar.muslim.ui.screens

import android.content.Intent
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

private enum class SettingsSectionPage(val title: String, val subtitle: String) {
    ROOT("الضبط والإعدادات", "إعدادات منظمة وموسّعة لتطبيق أثر مسلم"),
    THEMES("الثيمات", "اختر من بين 5 ثيمات متكاملة"),
    GALLERY("معرض الصور الـ 10", "مراجعة اللوحات وضبط التقليب التلقائي"),
    NOTIFICATIONS("الإشعارات", "تخصيص تنبيهات الأذكار اليومية"),
    READING("القراءة والصوت والعدادات", "حجم الخط، الصوت، الاهتزاز، والتصفير"),
    FOLLOW_US("تابعنا", "مشاركة التطبيق ونشر الأجر"),
    ABOUT("عن التطبيق", "معلومات التطبيق والخصائص")
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
    var currentPage by remember { mutableStateOf(SettingsSectionPage.ROOT) }

    BackHandler(enabled = true) {
        if (currentPage != SettingsSectionPage.ROOT) {
            currentPage = SettingsSectionPage.ROOT
        } else {
            onBack()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
    ) {
        Surface(
            color = scheme.surface,
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        if (currentPage != SettingsSectionPage.ROOT) {
                            currentPage = SettingsSectionPage.ROOT
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
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = currentPage.subtitle,
                        color = scheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }

        when (currentPage) {
            SettingsSectionPage.ROOT -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 22.dp)
                ) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "الصور الإسلامية (10 لوحات)",
                                    color = scheme.onSurface,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    onClick = { currentPage = SettingsSectionPage.GALLERY },
                                    shape = RoundedCornerShape(50),
                                    color = scheme.primaryContainer
                                ) {
                                    Text(
                                        text = "المعرض الكامل",
                                        color = scheme.onPrimaryContainer,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(slides, key = { it.id }) { slide ->
                                    IslamicArtworkBanner(
                                        slide = slide,
                                        modifier = Modifier
                                            .width(220.dp)
                                            .height(92.dp)
                                    )
                                }
                            }
                        }
                    }

                    item { SettingsGroupLabel("المظهر والعرض") }
                    item {
                        CompactSettingRow(
                            title = "الثيمات",
                            subtitle = "الثيم الحالي: ${selectedTheme.title}",
                            icon = Icons.Rounded.Palette,
                            onClick = { currentPage = SettingsSectionPage.THEMES }
                        )
                    }
                    item {
                        CompactSettingRow(
                            title = "الصور والتقليب التلقائي",
                            subtitle = if (autoSlideEnabled) "يعمل تلقائياً كل 4 ثوانٍ" else "متوقف حالياً",
                            icon = Icons.Rounded.Collections,
                            onClick = { currentPage = SettingsSectionPage.GALLERY }
                        )
                    }

                    item { SettingsGroupLabel("التنبيهات والقراءة") }
                    item {
                        CompactSettingRow(
                            title = "الإشعارات والتذكيرات",
                            subtitle = if (remindersEnabled) "مفعلة للأذكار اليومية" else "متوقفة",
                            icon = Icons.Rounded.NotificationsActive,
                            onClick = { currentPage = SettingsSectionPage.NOTIFICATIONS }
                        )
                    }
                    item {
                        CompactSettingRow(
                            title = "القراءة والصوت والعدادات",
                            subtitle = "حجم الخط، الصوت، الاهتزاز، وتصفير العدادات",
                            icon = Icons.Rounded.FormatSize,
                            onClick = { currentPage = SettingsSectionPage.READING }
                        )
                    }

                    item { SettingsGroupLabel("التواصل والمعلومات") }
                    item {
                        CompactSettingRow(
                            title = "تابعنا",
                            subtitle = "مشاركة التطبيق ونشر الأجر",
                            icon = Icons.Rounded.Share,
                            onClick = { currentPage = SettingsSectionPage.FOLLOW_US }
                        )
                    }
                    item {
                        CompactSettingRow(
                            title = "عن التطبيق",
                            subtitle = "نبذة عن تطبيق أثر مسلم ومميزاته",
                            icon = Icons.Rounded.Info,
                            onClick = { currentPage = SettingsSectionPage.ABOUT }
                        )
                    }
                }
            }

            SettingsSectionPage.THEMES -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 22.dp)
                ) {
                    items(AppThemePreset.entries, key = { it.id }) { preset ->
                        val isSelected = preset == selectedTheme
                        Surface(
                            onClick = { onSelectTheme(preset) },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) scheme.primaryContainer else scheme.surface,
                            border = BorderStroke(
                                width = if (isSelected) 1.8.dp else 1.dp,
                                color = if (isSelected) scheme.tertiary else scheme.outlineVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(preset.swatchPrimary, preset.swatchSecondary)
                                                ),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(11.dp)
                                                .align(Alignment.TopEnd)
                                                .padding(2.dp)
                                                .background(preset.swatchAccent, CircleShape)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = preset.title,
                                            color = scheme.onSurface,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = preset.description,
                                            color = scheme.onSurfaceVariant,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = scheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            SettingsSectionPage.GALLERY -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 22.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = scheme.surface),
                            border = BorderStroke(1.dp, scheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Rounded.AutoAwesomeMotion, contentDescription = null, tint = scheme.primary)
                                    Column {
                                        Text(
                                            text = "التقليب التلقائي (كل 4 ثوانٍ)",
                                            color = scheme.onSurface,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "الانتقال التلقائي المتواصل بين اللوحات الـ 10",
                                            color = scheme.onSurfaceVariant,
                                            fontSize = 11.sp
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
                                .height(112.dp)
                        )
                    }
                }
            }

            SettingsSectionPage.NOTIFICATIONS -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 22.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = scheme.surface),
                            border = BorderStroke(1.dp, scheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "إشعارات الأذكار اليومية",
                                        color = scheme.onSurface,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "تذكير يومي بالصباح والمساء والنوم",
                                        color = scheme.onSurfaceVariant,
                                        fontSize = 11.sp
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

            SettingsSectionPage.READING -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 22.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = scheme.surface),
                            border = BorderStroke(1.dp, scheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "حجم خط البطاقات",
                                    color = scheme.onSurface,
                                    fontSize = 13.sp,
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
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (selected) scheme.primary else scheme.primaryContainer,
                                            border = BorderStroke(
                                                1.dp,
                                                if (selected) scheme.tertiary else scheme.outlineVariant
                                            ),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = label,
                                                    color = if (selected) scheme.onPrimary else scheme.onPrimaryContainer,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.5f))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Rounded.VolumeUp, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(18.dp))
                                        Text("صوت النقر", color = scheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                                        Icon(Icons.Rounded.Vibration, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(18.dp))
                                        Text("الاهتزاز اللمسي", color = scheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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

                                HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.5f))

                                Button(
                                    onClick = onResetAllCounts,
                                    colors = ButtonDefaults.buttonColors(containerColor = scheme.primary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Rounded.Refresh, contentDescription = null, tint = scheme.onPrimary, modifier = Modifier.size(17.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("تصفير جميع العدادات", color = scheme.onPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            SettingsSectionPage.FOLLOW_US -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 22.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = scheme.surface),
                            border = BorderStroke(1.dp, scheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "شارك تطبيق أثر مسلم",
                                    color = scheme.onSurface,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "انشر الخير وشارك الأذكار والأدعية مع أهلك وأحبابك.",
                                    color = scheme.onSurfaceVariant,
                                    fontSize = 12.5.sp,
                                    lineHeight = 19.sp,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "تطبيق أثر مسلم — للأذكار والأدعية والأحاديث النبوية."
                                            )
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "مشاركة أثر مسلم"))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = scheme.primary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Rounded.Share, contentDescription = null, tint = scheme.onPrimary, modifier = Modifier.size(17.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("مشاركة التطبيق", color = scheme.onPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            SettingsSectionPage.ABOUT -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 22.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = scheme.surface),
                            border = BorderStroke(1.dp, scheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "تطبيق أثر مسلم",
                                    color = scheme.onSurface,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "تطبيق إسلامي متكامل للأذكار والأدعية والأحاديث مع 5 ثيمات كاملة وواجهة منظمة ومريحة للعين.",
                                    color = scheme.onSurfaceVariant,
                                    fontSize = 12.5.sp,
                                    lineHeight = 20.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsGroupLabel(label: String) {
    val scheme = MaterialTheme.colorScheme
    Text(
        text = label,
        color = scheme.primary,
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
    )
}

@Composable
private fun CompactSettingRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        border = BorderStroke(1.dp, scheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = scheme.primaryContainer,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = scheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = scheme.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        color = scheme.onSurfaceVariant,
                        fontSize = 10.5.sp
                    )
                }
            }
            Icon(
                imageVector = Icons.Rounded.ChevronLeft,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
