package com.athar.muslim.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.athar.muslim.model.VoiceReminderIntensity
import com.athar.muslim.ui.components.SoundHelper
import com.athar.muslim.ui.theme.AppThemePreset

private enum class SettingsSection(val sectionTitle: String, val sectionSubtitle: String) {
    MAIN("الضبط والإعدادات", "إعدادات موسّعة ومنظمة لتطبيق أثر مسلم"),
    THEMES("الثيمات", "اختر من بين 5 ثيمات متكاملة"),
    VOICE_AZKAR("الأذكار الصوتية", "قراءة صوتية تلقائية وشدة التذكير"),
    NOTIFICATIONS("الإشعارات", "تخصيص تنبيهات الأذكار اليومية"),
    READING("القراءة والصوت والعدادات", "حجم الخط، الصوت، الاهتزاز، والتصفير"),
    PERMISSIONS_ABOUT("الأذونات وعن التطبيق", "حالة الأذونات ومعلومات التطبيق")
}

@Composable
fun SettingsScreen(
    selectedTheme: AppThemePreset,
    onSelectTheme: (AppThemePreset) -> Unit,
    voiceAzkarEnabled: Boolean,
    onToggleVoiceAzkar: (Boolean) -> Unit,
    voiceIntensity: VoiceReminderIntensity,
    onSelectVoiceIntensity: (VoiceReminderIntensity) -> Unit,
    discoveredVoicesCount: Int,
    remindersEnabled: Boolean,
    onToggleReminders: (Boolean) -> Unit,
    soundEnabled: Boolean,
    onToggleSound: (Boolean) -> Unit,
    vibrationEnabled: Boolean,
    onToggleVibration: (Boolean) -> Unit,
    fontScaleStep: Int,
    onChangeFontScale: (Int) -> Unit,
    onResetAllCounts: () -> Unit,
    onRequestPermissions: () -> Unit,
    onBack: () -> Unit = {},
    onBackToHome: () -> Unit = onBack,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val view = LocalView.current
    val sectionState = remember { mutableStateOf(SettingsSection.MAIN) }
    val activeSection = sectionState.value

    fun clickSound() {
        SoundHelper.playSystemTouchSound(
            context = context,
            enabled = soundEnabled,
            view = view,
            hapticEnabled = vibrationEnabled
        )
    }

    BackHandler(enabled = true) {
        if (sectionState.value != SettingsSection.MAIN) {
            sectionState.value = SettingsSection.MAIN
        } else if (onBackToHome !== onBack) {
            onBackToHome()
        } else {
            onBack()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
    ) {
        if (activeSection != SettingsSection.MAIN) {
            Surface(
                color = scheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = {
                            clickSound()
                            sectionState.value = SettingsSection.MAIN
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
                            text = activeSection.sectionTitle,
                            color = scheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = activeSection.sectionSubtitle,
                            color = scheme.onSurfaceVariant,
                            fontSize = 10.5.sp
                        )
                    }
                }
            }
        }

        when (activeSection) {
            SettingsSection.MAIN -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 22.dp)
                ) {
                    item { SettingsGroupLabel("المظهر والتخصيص") }
                    item {
                        CompactSettingRow(
                            title = "الثيمات",
                            subtitle = "الثيم الحالي: ${selectedTheme.title}",
                            icon = Icons.Rounded.Palette,
                            onClick = {
                                clickSound()
                                sectionState.value = SettingsSection.THEMES
                            }
                        )
                    }

                    item { SettingsGroupLabel("التذكير الصوتي والإشعارات") }
                    item {
                        CompactSettingRow(
                            title = "الأذكار الصوتية",
                            subtitle = if (voiceAzkarEnabled) {
                                "مفعلة • الشدة: ${voiceIntensity.label} ($discoveredVoicesCount صوت متاح)"
                            } else {
                                "مطفأة افتراضياً • الشدة: ${voiceIntensity.label}"
                            },
                            icon = Icons.Rounded.RecordVoiceOver,
                            onClick = {
                                clickSound()
                                sectionState.value = SettingsSection.VOICE_AZKAR
                            }
                        )
                    }
                    item {
                        CompactSettingRow(
                            title = "الإشعارات والتذكيرات",
                            subtitle = if (remindersEnabled) "مفعلة للأذكار اليومية" else "متوقفة حالياً",
                            icon = Icons.Rounded.NotificationsActive,
                            onClick = {
                                clickSound()
                                sectionState.value = SettingsSection.NOTIFICATIONS
                            }
                        )
                    }

                    item { SettingsGroupLabel("القراءة والصوت والعدادات") }
                    item {
                        CompactSettingRow(
                            title = "القراءة والصوت والعدادات",
                            subtitle = "حجم الخط، صوت اللمس، الاهتزاز، وتصفير العدادات",
                            icon = Icons.Rounded.FormatSize,
                            onClick = {
                                clickSound()
                                sectionState.value = SettingsSection.READING
                            }
                        )
                    }

                    item { SettingsGroupLabel("النظام والمعلومات") }
                    item {
                        CompactSettingRow(
                            title = "الأذونات وعن التطبيق",
                            subtitle = "إدارة الأذونات ومعلومات تطبيق أثر مسلم",
                            icon = Icons.Rounded.Security,
                            onClick = {
                                clickSound()
                                sectionState.value = SettingsSection.PERMISSIONS_ABOUT
                            }
                        )
                    }
                }
            }

            SettingsSection.VOICE_AZKAR -> {
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
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "تشغيل الأذكار الصوتية",
                                            color = scheme.onSurface,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "يستمر الهاتف في قراءة التذكير الصوتي حتى عند إطفاء الشاشة",
                                            color = scheme.onSurfaceVariant,
                                            fontSize = 10.5.sp
                                        )
                                    }
                                    Switch(
                                        checked = voiceAzkarEnabled,
                                        onCheckedChange = { checked ->
                                            clickSound()
                                            onToggleVoiceAzkar(checked)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = scheme.onPrimary,
                                            checkedTrackColor = scheme.primary
                                        )
                                    )
                                }

                                HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.5f))

                                Text(
                                    text = "شدة التذكير",
                                    color = scheme.onSurface,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    VoiceReminderIntensity.entries.forEach { level ->
                                        val isSelected = level == voiceIntensity
                                        Surface(
                                            onClick = {
                                                clickSound()
                                                onSelectVoiceIntensity(level)
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) scheme.primary else scheme.primaryContainer,
                                            border = BorderStroke(
                                                1.dp,
                                                if (isSelected) scheme.tertiary else scheme.outlineVariant
                                            ),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = level.label,
                                                    color = if (isSelected) scheme.onPrimary else scheme.onPrimaryContainer,
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                Text(
                                    text = "الأصوات المكتشفة في res/raw: $discoveredVoicesCount ملف صوتي (يدعم 15 إلى 20+ صوتاً تلقائياً).",
                                    color = scheme.onSurfaceVariant,
                                    fontSize = 10.5.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            SettingsSection.THEMES -> {
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
                            onClick = {
                                clickSound()
                                onSelectTheme(preset)
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) scheme.primaryContainer else scheme.surface,
                            border = BorderStroke(
                                width = if (isSelected) 1.6.dp else 1.dp,
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
                                            .size(36.dp)
                                            .background(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(preset.swatchPrimary, preset.swatchSecondary)
                                                ),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .align(Alignment.TopEnd)
                                                .padding(2.dp)
                                                .background(preset.swatchAccent, CircleShape)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = preset.title,
                                            color = scheme.onSurface,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = preset.description,
                                            color = scheme.onSurfaceVariant,
                                            fontSize = 10.5.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = scheme.primary,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            SettingsSection.NOTIFICATIONS -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "إشعارات الأذكار اليومية",
                                        color = scheme.onSurface,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "تذكير يومي بالصباح والمساء والنوم وبعد الصلاة",
                                        color = scheme.onSurfaceVariant,
                                        fontSize = 10.5.sp
                                    )
                                }
                                Switch(
                                    checked = remindersEnabled,
                                    onCheckedChange = { checked ->
                                        clickSound()
                                        onToggleReminders(checked)
                                    },
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

            SettingsSection.READING -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
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
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "حجم خط البطاقات",
                                    color = scheme.onSurface,
                                    fontSize = 12.5.sp,
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
                                            onClick = {
                                                clickSound()
                                                onChangeFontScale(step)
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (selected) scheme.primary else scheme.primaryContainer,
                                            border = BorderStroke(
                                                1.dp,
                                                if (selected) scheme.tertiary else scheme.outlineVariant
                                            ),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 7.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = label,
                                                    color = if (selected) scheme.onPrimary else scheme.onPrimaryContainer,
                                                    fontSize = 11.5.sp,
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
                                        Icon(Icons.Rounded.VolumeUp, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(17.dp))
                                        Text("صوت اللمس والأذكار", color = scheme.onSurface, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Switch(
                                        checked = soundEnabled,
                                        onCheckedChange = { checked ->
                                            onToggleSound(checked)
                                            if (checked) {
                                                SoundHelper.playSystemTouchSound(context, true, view, vibrationEnabled)
                                            }
                                        },
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
                                        Icon(Icons.Rounded.Vibration, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(17.dp))
                                        Text("الاهتزاز اللمسي", color = scheme.onSurface, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Switch(
                                        checked = vibrationEnabled,
                                        onCheckedChange = { checked ->
                                            onToggleVibration(checked)
                                            clickSound()
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = scheme.onPrimary,
                                            checkedTrackColor = scheme.primary
                                        )
                                    )
                                }

                                HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.5f))

                                Button(
                                    onClick = {
                                        SoundHelper.playZikrPressSound(
                                            enabled = soundEnabled,
                                            isCompletion = true,
                                            view = view,
                                            hapticEnabled = vibrationEnabled
                                        )
                                        onResetAllCounts()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = scheme.primary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Rounded.Refresh, contentDescription = null, tint = scheme.onPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("تصفير جميع العدادات", color = scheme.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            SettingsSection.PERMISSIONS_ABOUT -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
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
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Rounded.Info, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(22.dp))
                                Text(
                                    text = "تطبيق أثر مسلم",
                                    color = scheme.onSurface,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "يشمل أذونات الموقع بدقة، الصوت، التخزين، الإشعارات، والمنبهات الدقيقة لدعم القبلة والأذكار الصوتية.",
                                    color = scheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    lineHeight = 17.sp,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = {
                                        clickSound()
                                        onRequestPermissions()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = scheme.primary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("مراجعة وطلب الأذونات", color = scheme.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
private fun SettingsGroupLabel(label: String) {
    val scheme = MaterialTheme.colorScheme
    Text(
        text = label,
        color = scheme.primary,
        fontSize = 10.5.sp,
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
                .padding(horizontal = 12.dp, vertical = 10.dp),
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
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = scheme.primary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = scheme.onSurface,
                        fontSize = 12.5.sp,
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
                modifier = Modifier.size(19.dp)
            )
        }
    }
}
