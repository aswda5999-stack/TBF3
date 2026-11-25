package com.athar.muslim.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesomeMotion
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Refresh
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.athar.muslim.ui.theme.AppThemePreset

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
    ) {
        // شريط علوي مستقل لصفحة الإعدادات
        Surface(
            color = scheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = "رجوع",
                            tint = scheme.primary
                        )
                    }
                    Column {
                        Text(
                            text = "إعدادات أثر مسلم",
                            color = scheme.onSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "الثيمات، الشريط العلوي، التنبيهات والخط",
                            color = scheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = scheme.primaryContainer
                ) {
                    Text(
                        text = "5 ثيمات",
                        color = scheme.onPrimaryContainer,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
        ) {
            // قسم الثيمات الـ 5 الافتراضية
            item {
                SettingsSectionCard(
                    title = "الثيمات الافتراضية (5 ثيمات)",
                    subtitle = "اختر المظهر المناسب للقراءة وتطبيق فوراً",
                    icon = Icons.Rounded.Palette
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppThemePreset.entries.forEach { preset ->
                            val isSelected = preset == selectedTheme
                            Surface(
                                onClick = { onSelectTheme(preset) },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) scheme.primaryContainer else scheme.surfaceVariant.copy(alpha = 0.45f),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) scheme.tertiary else scheme.outlineVariant
                                ),
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
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .background(
                                                    brush = Brush.linearGradient(
                                                        colors = listOf(preset.swatchPrimary, preset.swatchSecondary)
                                                    ),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .align(Alignment.TopEnd)
                                                    .padding(3.dp)
                                                    .background(preset.swatchAccent, CircleShape)
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = preset.title,
                                                color = scheme.onSurface,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = preset.description,
                                                color = scheme.onSurfaceVariant,
                                                fontSize = 11.5.sp,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = "مفعل",
                                            tint = scheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // قسم الشريط العلوي والتنبيهات والاستجابة
            item {
                SettingsSectionCard(
                    title = "الشريط العلوي والتنبيهات",
                    subtitle = "تحكم في تقليب الصور التلقائي كل 4 ثوانٍ والإشعارات",
                    icon = Icons.Rounded.NotificationsActive
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SettingSwitchRow(
                            title = "تقليب الصور التلقائي (كل 4 ثوانٍ)",
                            subtitle = "تبديل تلقائي بين 10 صور إسلامية وإعادة الدورة من البداية",
                            icon = Icons.Rounded.AutoAwesomeMotion,
                            checked = autoSlideEnabled,
                            onCheckedChange = onToggleAutoSlide
                        )

                        HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.6f))

                        SettingSwitchRow(
                            title = "إشعارات الأذكار اليومية",
                            subtitle = "تذكير الصباح والمساء والنوم في مواعيدها",
                            icon = Icons.Rounded.NotificationsActive,
                            checked = remindersEnabled,
                            onCheckedChange = onToggleReminders
                        )

                        HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.6f))

                        SettingSwitchRow(
                            title = "صوت اللمس والنقر",
                            subtitle = "نغمة نقر عند التسبيح أو إتمام العدد",
                            icon = Icons.Rounded.VolumeUp,
                            checked = soundEnabled,
                            onCheckedChange = onToggleSound
                        )

                        HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.6f))

                        SettingSwitchRow(
                            title = "الاهتزاز اللمسي",
                            subtitle = "استجابة لمسية خفيفة مع كل ضغطة",
                            icon = Icons.Rounded.Vibration,
                            checked = vibrationEnabled,
                            onCheckedChange = onToggleVibration
                        )
                    }
                }
            }

            // قسم حجم الخط وإدارة العدادات
            item {
                SettingsSectionCard(
                    title = "حجم الخط والعدادات",
                    subtitle = "تخصيص وضوح النصوص وتصفير العدادات اليومية",
                    icon = Icons.Rounded.FormatSize
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val fontOptions = listOf(0 to "صغير", 1 to "متوسط", 2 to "كبير")
                            fontOptions.forEach { (step, label) ->
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
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = onResetAllCounts,
                            colors = ButtonDefaults.buttonColors(containerColor = scheme.primary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = null,
                                tint = scheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تصفير جميع العدادات إلى القيمة الافتراضية",
                                color = scheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        border = BorderStroke(1.2.dp, scheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = scheme.primaryContainer,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = scheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = title,
                        color = scheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = subtitle,
                        color = scheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            content()
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = scheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = scheme.onSurfaceVariant,
                    fontSize = 11.5.sp
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = scheme.onPrimary,
                checkedTrackColor = scheme.primary
            )
        )
    }
}
