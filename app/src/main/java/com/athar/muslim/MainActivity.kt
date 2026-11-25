package com.athar.muslim

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Construction
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.athar.muslim.data.AzkarRepository
import com.athar.muslim.data.HadithRepository
import com.athar.muslim.model.AzkarSubCategory
import com.athar.muslim.model.HadithLoadResult
import com.athar.muslim.model.HadithTopic
import com.athar.muslim.model.MainBottomTab
import com.athar.muslim.model.ZikrItem
import com.athar.muslim.notifications.AzkarReminderReceiver
import com.athar.muslim.ui.components.SkyBottomBar
import com.athar.muslim.ui.components.SkyHeaderDecor
import com.athar.muslim.ui.components.SoundHelper
import com.athar.muslim.ui.components.ZikrCard
import com.athar.muslim.ui.screens.SettingsScreen
import com.athar.muslim.ui.theme.AppThemePreset
import com.athar.muslim.ui.theme.AtharMuslimTheme
import com.athar.muslim.ui.theme.BlueRoyal
import com.athar.muslim.ui.theme.GoldAccent
import com.athar.muslim.ui.theme.NavyDark
import com.athar.muslim.ui.theme.SkyBackground
import com.athar.muslim.ui.theme.SkyBorder
import com.athar.muslim.ui.theme.SkyContainer
import com.athar.muslim.ui.theme.SkySurface
import com.athar.muslim.ui.theme.TextBody
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class PermissionStep {
    NONE,
    NOTIFICATIONS,
    EXACT_ALARM
}

private fun todayKey(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return formatter.format(Date())
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val prefs = remember { context.getSharedPreferences("athar_muslim_prefs", Context.MODE_PRIVATE) }
            var selectedTheme by remember {
                mutableStateOf(
                    AppThemePreset.fromId(
                        prefs.getString("theme_preset", AppThemePreset.SKY_CLASSIC.id)
                    )
                )
            }

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                AtharMuslimTheme(themePreset = selectedTheme) {
                    AtharMuslimApp(
                        prefs = prefs,
                        selectedTheme = selectedTheme,
                        onSelectTheme = { preset ->
                            selectedTheme = preset
                            prefs.edit().putString("theme_preset", preset.id).apply()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AtharMuslimApp(
    prefs: android.content.SharedPreferences,
    selectedTheme: AppThemePreset,
    onSelectTheme: (AppThemePreset) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scheme = MaterialTheme.colorScheme

    var soundEnabled by remember { mutableStateOf(prefs.getBoolean("sound_enabled", true)) }
    var vibrationEnabled by remember { mutableStateOf(prefs.getBoolean("vibration_enabled", true)) }
    var remindersEnabled by remember { mutableStateOf(prefs.getBoolean("reminders_enabled", true)) }
    var autoSlideEnabled by remember { mutableStateOf(prefs.getBoolean("auto_slide_enabled", true)) }
    var fontScaleStep by remember { mutableIntStateOf(prefs.getInt("font_scale_step", 1)) }

    var selectedTab by remember { mutableStateOf(MainBottomTab.AZKAR) }
    var selectedAzkarCategory by remember { mutableStateOf<AzkarSubCategory?>(null) }
    var selectedHadithTopic by remember { mutableStateOf<HadithTopic?>(null) }
    var hadithLoadState by remember { mutableStateOf<HadithLoadResult?>(null) }
    var isLoadingHadith by remember { mutableStateOf(false) }
    var retryNonce by remember { mutableIntStateOf(0) }
    var showSettingsScreen by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    val favoriteHadiths = remember { mutableStateMapOf<Int, ZikrItem>() }
    val subCategories = AzkarSubCategory.entries

    val currentAzkarList = remember(selectedAzkarCategory) {
        selectedAzkarCategory?.let { AzkarRepository.getAzkarByCategory(context, it.key) }.orEmpty()
    }
    val duasList = remember { AzkarRepository.getDuas(context) }
    val countsMap = remember { mutableStateMapOf<Int, Int>() }

    fun saveCountForItem(itemId: Int, value: Int) {
        prefs.edit()
            .putString("counts_day_key", todayKey())
            .putInt("zikr_count_$itemId", value)
            .apply()
    }

    fun resetAllCountsToDefault() {
        val allItems = AzkarRepository.getAllAzkar(context) + duasList
        val editor = prefs.edit().putString("counts_day_key", todayKey())
        allItems.forEach { entry ->
            countsMap[entry.id] = entry.count
            editor.putInt("zikr_count_${entry.id}", entry.count)
        }
        editor.apply()
    }

    fun syncDailyCounts() {
        val currentDay = todayKey()
        val savedDay = prefs.getString("counts_day_key", currentDay)
        val allItems = AzkarRepository.getAllAzkar(context) + duasList
        val editor = prefs.edit().putString("counts_day_key", currentDay)

        if (savedDay != currentDay) {
            allItems.forEach { entry ->
                countsMap[entry.id] = entry.count
                editor.putInt("zikr_count_${entry.id}", entry.count)
            }
        } else {
            allItems.forEach { entry ->
                val stored = prefs.getInt("zikr_count_${entry.id}", entry.count)
                countsMap[entry.id] = stored
                if (!prefs.contains("zikr_count_${entry.id}")) {
                    editor.putInt("zikr_count_${entry.id}", entry.count)
                }
            }
        }
        editor.apply()
    }

    BackHandler(enabled = true) {
        when {
            showSettingsScreen -> showSettingsScreen = false
            showExitDialog -> showExitDialog = false
            selectedTab == MainBottomTab.AZKAR && selectedAzkarCategory != null -> {
                selectedAzkarCategory = null
            }
            selectedTab == MainBottomTab.HADITH && selectedHadithTopic != null -> {
                selectedHadithTopic = null
            }
            selectedTab != MainBottomTab.AZKAR -> {
                selectedAzkarCategory = null
                selectedHadithTopic = null
                selectedTab = MainBottomTab.AZKAR
            }
            else -> {
                showExitDialog = true
            }
        }
    }

    var permissionStep by remember { mutableStateOf(PermissionStep.NONE) }

    val exactAlarmLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        prefs.edit().putBoolean("exact_alarm_prompted", true).apply()
        AzkarReminderReceiver.scheduleAllDailyReminders(context)
        permissionStep = PermissionStep.NONE
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        prefs.edit().putBoolean("notif_permission_prompted", true).apply()
        AzkarReminderReceiver.ensureNotificationChannel(context)
        AzkarReminderReceiver.scheduleAllDailyReminders(context)

        if (!AzkarReminderReceiver.canScheduleExactAlarms(context) &&
            !prefs.getBoolean("exact_alarm_prompted", false)
        ) {
            permissionStep = PermissionStep.EXACT_ALARM
        } else {
            permissionStep = PermissionStep.NONE
        }
    }

    LaunchedEffect(Unit) {
        syncDailyCounts()
        AzkarReminderReceiver.scheduleAllDailyReminders(context)

        val needsNotifRuntime = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED

        if (needsNotifRuntime && !prefs.getBoolean("notif_permission_prompted", false)) {
            permissionStep = PermissionStep.NOTIFICATIONS
        } else if (!AzkarReminderReceiver.canScheduleExactAlarms(context) &&
            !prefs.getBoolean("exact_alarm_prompted", false)
        ) {
            permissionStep = PermissionStep.EXACT_ALARM
        }
    }

    LaunchedEffect(selectedTab, selectedHadithTopic, retryNonce) {
        val topic = selectedHadithTopic
        if (selectedTab == MainBottomTab.HADITH && topic != null && !topic.isFavorites) {
            isLoadingHadith = true
            hadithLoadState = null
            val res = HadithRepository.loadTopicHadiths(topic)
            if (res is HadithLoadResult.Success) {
                res.items.forEach { entry ->
                    if (!countsMap.containsKey(entry.id)) {
                        countsMap[entry.id] = entry.count
                    }
                }
            }
            hadithLoadState = res
            isLoadingHadith = false
        }
    }

    if (showSettingsScreen) {
        SettingsScreen(
            selectedTheme = selectedTheme,
            onSelectTheme = onSelectTheme,
            autoSlideEnabled = autoSlideEnabled,
            onToggleAutoSlide = { enabled ->
                autoSlideEnabled = enabled
                prefs.edit().putBoolean("auto_slide_enabled", enabled).apply()
            },
            remindersEnabled = remindersEnabled,
            onToggleReminders = { checked ->
                remindersEnabled = checked
                prefs.edit().putBoolean("reminders_enabled", checked).apply()
                if (checked) {
                    AzkarReminderReceiver.scheduleAllDailyReminders(context)
                }
            },
            soundEnabled = soundEnabled,
            onToggleSound = { checked ->
                soundEnabled = checked
                prefs.edit().putBoolean("sound_enabled", checked).apply()
                if (checked) {
                    SoundHelper.playTapSound(enabled = true, isCompletion = false)
                }
            },
            vibrationEnabled = vibrationEnabled,
            onToggleVibration = { checked ->
                vibrationEnabled = checked
                prefs.edit().putBoolean("vibration_enabled", checked).apply()
            },
            fontScaleStep = fontScaleStep,
            onChangeFontScale = { step ->
                fontScaleStep = step
                prefs.edit().putInt("font_scale_step", step).apply()
            },
            onResetAllCounts = { resetAllCountsToDefault() },
            onBack = { showSettingsScreen = false }
        )
        return
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            containerColor = scheme.surface,
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "تأكيد الخروج من التطبيق",
                    color = scheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "هل تريد الخروج من تطبيق أثر مسلم الآن؟",
                    color = scheme.onBackground,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        activity?.finishAffinity()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = scheme.primary)
                ) {
                    Text("خروج", color = scheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("البقاء", color = scheme.onSurface, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (permissionStep == PermissionStep.NOTIFICATIONS) {
        AlertDialog(
            onDismissRequest = {
                prefs.edit().putBoolean("notif_permission_prompted", true).apply()
                permissionStep = if (!AzkarReminderReceiver.canScheduleExactAlarms(context)) {
                    PermissionStep.EXACT_ALARM
                } else {
                    PermissionStep.NONE
                }
            },
            containerColor = scheme.surface,
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "إذن إشعارات الأذكار",
                    color = scheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "اسمح للتطبيق بإرسال إشعارات أذكار الصباح والمساء والنوم لتصلك في مواعيدها.",
                    color = scheme.onBackground,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            permissionStep = PermissionStep.EXACT_ALARM
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = scheme.primary)
                ) {
                    Text("منح الإذن", color = scheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        prefs.edit().putBoolean("notif_permission_prompted", true).apply()
                        permissionStep = if (!AzkarReminderReceiver.canScheduleExactAlarms(context)) {
                            PermissionStep.EXACT_ALARM
                        } else {
                            PermissionStep.NONE
                        }
                    }
                ) {
                    Text("لاحقاً", color = scheme.onSurface)
                }
            }
        )
    }

    if (permissionStep == PermissionStep.EXACT_ALARM) {
        AlertDialog(
            onDismissRequest = {
                prefs.edit().putBoolean("exact_alarm_prompted", true).apply()
                permissionStep = PermissionStep.NONE
            },
            containerColor = scheme.surface,
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "إذن التنبيه في الموعد المحدد",
                    color = scheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "لتصل إشعارات الأذكار بدقة في وقتها اليومي، فعّل خيار المنبهات والتذكيرات من الإعدادات.",
                    color = scheme.onBackground,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val settingsIntent = AzkarReminderReceiver.createExactAlarmSettingsIntent(context)
                        if (settingsIntent != null) {
                            exactAlarmLauncher.launch(settingsIntent)
                        } else {
                            AzkarReminderReceiver.scheduleAllDailyReminders(context)
                            permissionStep = PermissionStep.NONE
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = scheme.primary)
                ) {
                    Text("فتح الإعدادات", color = scheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        prefs.edit().putBoolean("exact_alarm_prompted", true).apply()
                        permissionStep = PermissionStep.NONE
                    }
                ) {
                    Text("تخطي", color = scheme.onSurface)
                }
            }
        )
    }

    val subtitleBadgeText = when (selectedTab) {
        MainBottomTab.AZKAR -> {
            val activeCat = selectedAzkarCategory
            if (activeCat == null) {
                "أقسام الأذكار"
            } else {
                val done = currentAzkarList.count { (countsMap[it.id] ?: it.count) <= 0 }
                "${activeCat.title} ($done/${currentAzkarList.size})"
            }
        }
        MainBottomTab.DUA -> {
            val done = duasList.count { (countsMap[it.id] ?: it.count) <= 0 }
            "الأدعية المختارة ($done/${duasList.size})"
        }
        MainBottomTab.HADITH -> selectedHadithTopic?.title ?: "أبواب الأحاديث"
        MainBottomTab.FOLLOW_US -> "تطبيق أثر مسلم"
    }

    Scaffold(
        topBar = {
            SkyHeaderDecor(
                title = "أثر مسلم",
                subtitleBadge = subtitleBadgeText,
                autoSlideEnabled = autoSlideEnabled,
                onSettingsClick = { showSettingsScreen = true }
            )
        },
        bottomBar = {
            SkyBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    selectedTab = tab
                    if (tab != MainBottomTab.AZKAR) {
                        selectedAzkarCategory = null
                    }
                    if (tab != MainBottomTab.HADITH) {
                        selectedHadithTopic = null
                    }
                }
            )
        },
        containerColor = scheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scheme.background)
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                MainBottomTab.AZKAR -> {
                    val activeCat = selectedAzkarCategory
                    if (activeCat == null) {
                        AzkarCategoriesVerticalList(
                            categories = subCategories,
                            countsMap = countsMap,
                            onSelectCategory = { cat -> selectedAzkarCategory = cat }
                        )
                    } else {
                        ItemsCardsList(
                            items = currentAzkarList,
                            countsMap = countsMap,
                            isZikrMode = true,
                            soundEnabled = soundEnabled,
                            vibrationEnabled = vibrationEnabled,
                            fontScaleStep = fontScaleStep,
                            onSaveCount = { id, newVal -> saveCountForItem(id, newVal) }
                        )
                    }
                }

                MainBottomTab.DUA -> {
                    ItemsCardsList(
                        items = duasList,
                        countsMap = countsMap,
                        isZikrMode = false,
                        soundEnabled = soundEnabled,
                        vibrationEnabled = vibrationEnabled,
                        fontScaleStep = fontScaleStep,
                        onSaveCount = { id, newVal -> saveCountForItem(id, newVal) }
                    )
                }

                MainBottomTab.HADITH -> {
                    val activeTopic = selectedHadithTopic
                    if (activeTopic == null) {
                        HadithTopicsVerticalList(
                            favoritesCount = favoriteHadiths.size,
                            onSelectTopic = { topic -> selectedHadithTopic = topic }
                        )
                    } else {
                        HadithTopicDetailScreen(
                            topic = activeTopic,
                            isLoading = isLoadingHadith,
                            loadResult = hadithLoadState,
                            favoriteMap = favoriteHadiths,
                            countsMap = countsMap,
                            soundEnabled = soundEnabled,
                            vibrationEnabled = vibrationEnabled,
                            fontScaleStep = fontScaleStep,
                            onRetry = { retryNonce++ },
                            onToggleFavorite = { hadithItem ->
                                if (favoriteHadiths.containsKey(hadithItem.id)) {
                                    favoriteHadiths.remove(hadithItem.id)
                                } else {
                                    favoriteHadiths[hadithItem.id] = hadithItem
                                }
                            },
                            onSaveCount = { id, newVal -> saveCountForItem(id, newVal) }
                        )
                    }
                }

                MainBottomTab.FOLLOW_US -> {
                    FollowUsCardScreen()
                }
            }
        }
    }
}

@Composable
private fun AzkarCategoriesVerticalList(
    categories: List<AzkarSubCategory>,
    countsMap: Map<Int, Int>,
    onSelectCategory: (AzkarSubCategory) -> Unit
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 18.dp)
    ) {
        items(categories, key = { it.key }) { category ->
            val catItems = remember(category) {
                AzkarRepository.getAzkarByCategory(context, category.key)
            }
            val totalCount = catItems.size
            val doneCount = catItems.count { (countsMap[it.id] ?: it.count) <= 0 }
            val isCategoryCompleted = totalCount > 0 && doneCount >= totalCount

            Card(
                onClick = { onSelectCategory(category) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = scheme.surface),
                border = BorderStroke(
                    1.5.dp,
                    if (isCategoryCompleted) scheme.tertiary else scheme.outlineVariant
                ),
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
                            color = if (isCategoryCompleted) scheme.primary else scheme.primaryContainer,
                            border = BorderStroke(1.dp, scheme.outlineVariant),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = category.icon,
                                    contentDescription = null,
                                    tint = if (isCategoryCompleted) scheme.onPrimary else scheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = category.title,
                                    color = scheme.onSurface,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                if (isCategoryCompleted) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = "مكتمل",
                                        tint = scheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = category.subtitle,
                                color = scheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isCategoryCompleted) scheme.primary else scheme.primaryContainer,
                            border = BorderStroke(1.dp, scheme.outlineVariant)
                        ) {
                            Text(
                                text = if (isCategoryCompleted) "مكتمل $doneCount/$totalCount" else "$doneCount/$totalCount",
                                color = if (isCategoryCompleted) scheme.onPrimary else scheme.onPrimaryContainer,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
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
        }
    }
}

@Composable
private fun HadithTopicsVerticalList(
    favoritesCount: Int,
    onSelectTopic: (HadithTopic) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 18.dp)
    ) {
        items(HadithTopic.entries, key = { it.key }) { topic ->
            Card(
                onClick = { onSelectTopic(topic) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = scheme.surface),
                border = BorderStroke(1.5.dp, scheme.outlineVariant),
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
                                    imageVector = topic.icon,
                                    contentDescription = null,
                                    tint = scheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = topic.title,
                                color = scheme.onSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (topic.isFavorites) {
                                    "عدد الأحاديث المحفوظة: $favoritesCount"
                                } else {
                                    topic.subtitle
                                },
                                color = scheme.onSurfaceVariant,
                                fontSize = 12.5.sp
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Rounded.ChevronLeft,
                        contentDescription = null,
                        tint = scheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HadithTopicDetailScreen(
    topic: HadithTopic,
    isLoading: Boolean,
    loadResult: HadithLoadResult?,
    favoriteMap: Map<Int, ZikrItem>,
    countsMap: MutableMap<Int, Int>,
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    fontScaleStep: Int,
    onRetry: () -> Unit,
    onToggleFavorite: (ZikrItem) -> Unit,
    onSaveCount: (Int, Int) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxSize()) {
        if (topic.isFavorites) {
            val favList = favoriteMap.values.toList()
            if (favList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد أحاديث في المفضلة بعد.",
                        color = scheme.onSurface,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 6.dp, bottom = 18.dp)
                ) {
                    items(favList, key = { it.id }) { hadithItem ->
                        val rem = countsMap[hadithItem.id] ?: hadithItem.count
                        ZikrCard(
                            item = hadithItem,
                            remainingCount = rem,
                            onDecrement = {
                                if (rem > 0) {
                                    val nextVal = rem - 1
                                    countsMap[hadithItem.id] = nextVal
                                    onSaveCount(hadithItem.id, nextVal)
                                }
                            },
                            isZikrMode = false,
                            isFavorite = favoriteMap.containsKey(hadithItem.id),
                            showFavoriteButton = true,
                            onToggleFavorite = { onToggleFavorite(hadithItem) },
                            soundEnabled = soundEnabled,
                            vibrationEnabled = vibrationEnabled,
                            fontScaleStep = fontScaleStep
                        )
                    }
                }
            }
            return@Column
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = scheme.primary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "جاري تحميل الأحاديث...",
                            color = scheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            loadResult is HadithLoadResult.Success -> {
                val list = loadResult.items
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 6.dp, bottom = 18.dp)
                ) {
                    items(list, key = { it.id }) { hadithItem ->
                        val rem = countsMap[hadithItem.id] ?: hadithItem.count
                        ZikrCard(
                            item = hadithItem,
                            remainingCount = rem,
                            onDecrement = {
                                if (rem > 0) {
                                    val nextVal = rem - 1
                                    countsMap[hadithItem.id] = nextVal
                                    onSaveCount(hadithItem.id, nextVal)
                                }
                            },
                            isZikrMode = false,
                            isFavorite = favoriteMap.containsKey(hadithItem.id),
                            showFavoriteButton = true,
                            onToggleFavorite = { onToggleFavorite(hadithItem) },
                            soundEnabled = soundEnabled,
                            vibrationEnabled = vibrationEnabled,
                            fontScaleStep = fontScaleStep
                        )
                    }
                }
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = scheme.surface),
                        border = BorderStroke(1.5.dp, scheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Construction,
                                contentDescription = null,
                                tint = scheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "هذا القسم قيد التطوير",
                                color = scheme.onSurface,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = onRetry,
                                colors = ButtonDefaults.buttonColors(containerColor = scheme.primary)
                            ) {
                                Text("إعادة المحاولة", color = scheme.onPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemsCardsList(
    items: List<ZikrItem>,
    countsMap: MutableMap<Int, Int>,
    isZikrMode: Boolean,
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    fontScaleStep: Int,
    onSaveCount: (Int, Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = if (isZikrMode) 16.dp else 14.dp),
        verticalArrangement = Arrangement.spacedBy(if (isZikrMode) 10.dp else 14.dp),
        contentPadding = PaddingValues(top = 6.dp, bottom = 18.dp)
    ) {
        items(items, key = { zikrEntry -> zikrEntry.id }) { zikrEntry ->
            val currentCount = countsMap[zikrEntry.id] ?: zikrEntry.count
            ZikrCard(
                item = zikrEntry,
                remainingCount = currentCount,
                onDecrement = {
                    if (currentCount > 0) {
                        val nextVal = currentCount - 1
                        countsMap[zikrEntry.id] = nextVal
                        onSaveCount(zikrEntry.id, nextVal)
                    }
                },
                isZikrMode = isZikrMode,
                soundEnabled = soundEnabled,
                vibrationEnabled = vibrationEnabled,
                fontScaleStep = fontScaleStep
            )
        }
    }
}

@Composable
private fun FollowUsCardScreen() {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = scheme.surface),
            border = BorderStroke(1.8.dp, scheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "أثر مسلم",
                    color = scheme.primary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "رفيقك اليومي للأذكار والأدعية والأحاديث بتصميم إسلامي هادئ ومريح للعين.",
                    color = scheme.onBackground,
                    fontSize = 15.sp,
                    lineHeight = 25.sp,
                    textAlign = TextAlign.Center
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = scheme.primaryContainer,
                    border = BorderStroke(1.dp, scheme.outlineVariant)
                ) {
                    Text(
                        text = "نسخة أثر مسلم • 5 ثيمات وواجهة إعدادات مستقلة",
                        color = scheme.onPrimaryContainer,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
