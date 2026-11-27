package com.athar.muslim

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.athar.muslim.audio.VoiceAzkarPlayer
import com.athar.muslim.data.AzkarRepository
import com.athar.muslim.model.AzkarSubCategory
import com.athar.muslim.model.MainBottomTab
import com.athar.muslim.model.VoiceReminderIntensity
import com.athar.muslim.model.ZikrItem
import com.athar.muslim.notifications.AzkarReminderReceiver
import com.athar.muslim.permissions.AppPermissionsManager
import com.athar.muslim.ui.components.SkyBottomBar
import com.athar.muslim.ui.components.SkyHeaderDecor
import com.athar.muslim.ui.components.SoundHelper
import com.athar.muslim.ui.components.ZikrCard
import com.athar.muslim.ui.screens.FavoritesScreen
import com.athar.muslim.ui.screens.HadithScreen
import com.athar.muslim.ui.screens.HomeDashboardScreen
import com.athar.muslim.ui.screens.QiblaCompassScreen
import com.athar.muslim.ui.screens.QuickToolPage
import com.athar.muslim.ui.screens.QuickToolScreen
import com.athar.muslim.ui.screens.SettingsScreen
import com.athar.muslim.ui.screens.buttons.HomeQuickAction
import com.athar.muslim.ui.theme.AppThemePreset
import com.athar.muslim.ui.theme.AtharMuslimTheme
import com.athar.muslim.util.CrashLogger
import com.athar.muslim.util.FavoritesStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class PermissionPromptStep {
    NONE,
    RUNTIME_BUNDLE,
    EXACT_ALARM
}

private fun todayKey(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return formatter.format(Date())
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CrashLogger.install(applicationContext)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val prefs = remember { context.getSharedPreferences("athar_muslim_prefs", Context.MODE_PRIVATE) }
            val themeState = remember {
                mutableStateOf(
                    AppThemePreset.fromId(
                        prefs.getString("theme_preset", AppThemePreset.SKY_CLASSIC.id)
                    )
                )
            }
            val selectedTheme = themeState.value

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                AtharMuslimTheme(themePreset = selectedTheme) {
                    AtharMuslimApp(
                        prefs = prefs,
                        selectedTheme = selectedTheme,
                        onSelectTheme = { preset ->
                            themeState.value = preset
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
    val view = LocalView.current
    val scheme = MaterialTheme.colorScheme

    val soundEnabledState = remember { mutableStateOf(prefs.getBoolean("sound_enabled", true)) }
    val vibrationEnabledState = remember { mutableStateOf(prefs.getBoolean("vibration_enabled", true)) }
    val remindersEnabledState = remember { mutableStateOf(prefs.getBoolean("reminders_enabled", true)) }
    val voiceAzkarEnabledState = remember { mutableStateOf(prefs.getBoolean("voice_azkar_enabled", false)) }
    val voiceIntensityState = remember {
        mutableStateOf(
            VoiceReminderIntensity.fromId(
                prefs.getString("voice_intensity", VoiceReminderIntensity.MEDIUM.id)
            )
        )
    }
    val fontScaleStepState = remember { mutableIntStateOf(prefs.getInt("font_scale_step", 1)) }

    val selectedTabState = remember { mutableStateOf(MainBottomTab.HOME) }
    val activeHomeActionState = remember { mutableStateOf<HomeQuickAction?>(null) }
    val selectedAzkarCategoryState = remember { mutableStateOf<AzkarSubCategory?>(null) }
    val showExitDialogState = remember { mutableStateOf(false) }
    val permissionStepState = remember { mutableStateOf(PermissionPromptStep.NONE) }

    val lastReadingTitleState = remember {
        mutableStateOf(
            prefs.getString("last_reading_title", "القرآن الكريم • فهرس السور")
                ?: "القرآن الكريم • فهرس السور"
        )
    }
    val lastReadingActionIdState = remember {
        mutableStateOf(
            prefs.getString("last_reading_action_id", HomeQuickAction.QURAN.id)
                ?: HomeQuickAction.QURAN.id
        )
    }

    val soundEnabled = soundEnabledState.value
    val vibrationEnabled = vibrationEnabledState.value
    val remindersEnabled = remindersEnabledState.value
    val voiceAzkarEnabled = voiceAzkarEnabledState.value
    val voiceIntensity = voiceIntensityState.value
    val fontScaleStep = fontScaleStepState.intValue
    val selectedTab = selectedTabState.value
    val activeHomeAction = activeHomeActionState.value
    val selectedAzkarCategory = selectedAzkarCategoryState.value
    val lastReadingTitle = lastReadingTitleState.value
    val lastReadingAction = HomeQuickAction.entries.firstOrNull { it.id == lastReadingActionIdState.value }
        ?: HomeQuickAction.QURAN

    fun recordLastReading(title: String, action: HomeQuickAction) {
        lastReadingTitleState.value = title
        lastReadingActionIdState.value = action.id
        runCatching {
            prefs.edit()
                .putString("last_reading_title", title)
                .putString("last_reading_action_id", action.id)
                .apply()
        }
    }

    val favoriteQuranIds = remember { mutableStateOf<Set<Int>>(emptySet()) }

    fun toggleFavoriteAyah(id: Int) {
        // مفضلة الآيات القديمة (غير مستخدمة بعد قارئ المصحف الجديد)
        val current = favoriteQuranIds.value
        favoriteQuranIds.value = if (current.contains(id)) current - id else current + id
    }

    val favoriteItems = remember { mutableStateOf(FavoritesStore.load(prefs)) }
    val favoriteIds = favoriteItems.value.map { it.id }.toSet()

    fun toggleFavorite(item: ZikrItem) {
        val current = favoriteItems.value
        val updated = if (current.any { it.id == item.id }) {
            current.filter { it.id != item.id }
        } else {
            listOf(item) + current
        }
        favoriteItems.value = updated
        FavoritesStore.save(prefs, updated)
    }

    val crashReportState = remember { mutableStateOf(CrashLogger.readAndClear(context)) }

    val discoveredVoicesCount = remember {
        runCatching { VoiceAzkarPlayer.discoverRawVoiceResIds(context).size }.getOrDefault(0)
    }

    val subCategories = AzkarSubCategory.entries
    val currentAzkarList = remember(selectedAzkarCategory) {
        selectedAzkarCategory?.let { AzkarRepository.getAzkarByCategory(context, it.key) }.orEmpty()
    }
    val duasList = remember { AzkarRepository.getDuas(context) }
    val countsMap = remember { mutableStateMapOf<Int, Int>() }

    fun saveCountForItem(itemId: Int, value: Int) {
        runCatching {
            prefs.edit()
                .putString("counts_day_key", todayKey())
                .putInt("zikr_count_$itemId", value)
                .apply()
        }
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

    val exactAlarmLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        prefs.edit().putBoolean("exact_alarm_prompted", true).apply()
        runCatching { AzkarReminderReceiver.scheduleAllDailyReminders(context) }
        permissionStepState.value = PermissionPromptStep.NONE
    }

    val multiPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        prefs.edit().putBoolean("runtime_perms_prompted", true).apply()
        runCatching {
            AzkarReminderReceiver.ensureNotificationChannel(context)
            AzkarReminderReceiver.scheduleAllDailyReminders(context)
        }
        if (!runCatching { AzkarReminderReceiver.canScheduleExactAlarms(context) }.getOrDefault(true) &&
            !prefs.getBoolean("exact_alarm_prompted", false)
        ) {
            permissionStepState.value = PermissionPromptStep.EXACT_ALARM
        } else {
            permissionStepState.value = PermissionPromptStep.NONE
        }
    }

    fun triggerPermissionsRequest() {
        multiPermissionLauncher.launch(AppPermissionsManager.requiredRuntimePermissions())
    }

    BackHandler(enabled = true) {
        runCatching {
            SoundHelper.playSystemTouchSound(context, soundEnabled, view, vibrationEnabled)
        }
        when {
            showExitDialogState.value -> showExitDialogState.value = false
            activeHomeAction != null -> activeHomeActionState.value = null
            selectedTab == MainBottomTab.AZKAR && selectedAzkarCategory != null -> {
                selectedAzkarCategoryState.value = null
            }
            selectedTab != MainBottomTab.HOME -> {
                selectedAzkarCategoryState.value = null
                activeHomeActionState.value = null
                selectedTabState.value = MainBottomTab.HOME
            }
            else -> showExitDialogState.value = true
        }
    }

    LaunchedEffect(Unit) {
        runCatching {
            syncDailyCounts()
            AzkarReminderReceiver.scheduleAllDailyReminders(context)
        }

        if (!runCatching { AppPermissionsManager.hasAllRequiredRuntimePermissions(context) }.getOrDefault(true) &&
            !prefs.getBoolean("runtime_perms_prompted", false)
        ) {
            permissionStepState.value = PermissionPromptStep.RUNTIME_BUNDLE
        } else if (!runCatching { AzkarReminderReceiver.canScheduleExactAlarms(context) }.getOrDefault(true) &&
            !prefs.getBoolean("exact_alarm_prompted", false)
        ) {
            permissionStepState.value = PermissionPromptStep.EXACT_ALARM
        }
    }

    val pendingCrash = crashReportState.value
    if (pendingCrash != null) {
        AlertDialog(
            onDismissRequest = { crashReportState.value = null },
            containerColor = scheme.surface,
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "توقف التطبيق في المرة السابقة",
                    color = scheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = pendingCrash.take(700),
                    color = scheme.onBackground,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        runCatching {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("crash", pendingCrash))
                            Toast.makeText(context, "تم نسخ تقرير الخطأ", Toast.LENGTH_SHORT).show()
                        }
                        crashReportState.value = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = scheme.primary)
                ) {
                    Text("نسخ التقرير", color = scheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { crashReportState.value = null }) {
                    Text("إغلاق", color = scheme.onSurface)
                }
            }
        )
    }

    if (showExitDialogState.value) {
        AlertDialog(
            onDismissRequest = { showExitDialogState.value = false },
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
                        runCatching {
                            SoundHelper.playSystemTouchSound(context, soundEnabled, view, vibrationEnabled)
                        }
                        showExitDialogState.value = false
                        activity?.finishAffinity()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = scheme.primary)
                ) {
                    Text("خروج", color = scheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialogState.value = false }) {
                    Text("البقاء", color = scheme.onSurface, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (permissionStepState.value == PermissionPromptStep.RUNTIME_BUNDLE) {
        AlertDialog(
            onDismissRequest = {
                prefs.edit().putBoolean("runtime_perms_prompted", true).apply()
                permissionStepState.value = PermissionPromptStep.NONE
            },
            containerColor = scheme.surface,
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "أذونات التطبيق الأساسية",
                    color = scheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = "يطلب التطبيق إذن الموقع بدقة والصوت والتخزين والإشعارات لتشغيل البوصلة والأذكار الصوتية والتنبيهات بسلاسة.",
                    color = scheme.onBackground,
                    fontSize = 13.sp,
                    lineHeight = 21.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        permissionStepState.value = PermissionPromptStep.NONE
                        triggerPermissionsRequest()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = scheme.primary)
                ) {
                    Text("منح الأذونات", color = scheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        prefs.edit().putBoolean("runtime_perms_prompted", true).apply()
                        permissionStepState.value = PermissionPromptStep.NONE
                    }
                ) {
                    Text("لاحقاً", color = scheme.onSurface)
                }
            }
        )
    }

    if (permissionStepState.value == PermissionPromptStep.EXACT_ALARM) {
        AlertDialog(
            onDismissRequest = {
                prefs.edit().putBoolean("exact_alarm_prompted", true).apply()
                permissionStepState.value = PermissionPromptStep.NONE
            },
            containerColor = scheme.surface,
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "إذن التنبيه في الموعد المحدد",
                    color = scheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = "لتعمل الأذكار الصوتية والإشعارات بدقة حتى عند إغلاق الشاشة، يُنصح بتفعيل إذن المنبهات الدقيقة.",
                    color = scheme.onBackground,
                    fontSize = 13.sp,
                    lineHeight = 21.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val settingsIntent = AzkarReminderReceiver.createExactAlarmSettingsIntent(context)
                        if (settingsIntent != null) {
                            exactAlarmLauncher.launch(settingsIntent)
                        } else {
                            runCatching { AzkarReminderReceiver.scheduleAllDailyReminders(context) }
                            permissionStepState.value = PermissionPromptStep.NONE
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
                        permissionStepState.value = PermissionPromptStep.NONE
                    }
                ) {
                    Text("تخطي", color = scheme.onSurface)
                }
            }
        )
    }

    val isHomeDashboard = selectedTab == MainBottomTab.HOME && activeHomeAction == null

    val subtitleBadgeText = activeHomeAction?.title
        ?: when (selectedTab) {
            MainBottomTab.HOME -> "الرئيسية"
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
            MainBottomTab.FAVORITES -> "المفضلة (${favoriteItems.value.size})"
            MainBottomTab.SETTINGS -> "الضبط والإعدادات"
        }

    Scaffold(
        topBar = {
            SkyHeaderDecor(
                title = "أثر مسلم",
                subtitleBadge = subtitleBadgeText,
                showBannerCarousel = isHomeDashboard,
                autoSlideEnabled = isHomeDashboard,
                soundEnabled = soundEnabled,
                hapticEnabled = vibrationEnabled
            )
        },
        bottomBar = {
            SkyBottomBar(
                selectedTab = selectedTab,
                soundEnabled = soundEnabled,
                onTabSelected = { tab ->
                    selectedTabState.value = tab
                    activeHomeActionState.value = null
                    if (tab != MainBottomTab.AZKAR) {
                        selectedAzkarCategoryState.value = null
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
            if (activeHomeAction != null) {
                HomeActionContent(
                    action = activeHomeAction,
                    soundEnabled = soundEnabled,
                    vibrationEnabled = vibrationEnabled,
                    favoriteQuranIds = favoriteQuranIds.value,
                    onToggleFavoriteAyah = { id -> toggleFavoriteAyah(id) },
                    favoriteIds = favoriteIds,
                    onToggleFavorite = { item -> toggleFavorite(item) },
                    onOpenMorningAzkar = {
                        activeHomeActionState.value = null
                        selectedTabState.value = MainBottomTab.AZKAR
                        selectedAzkarCategoryState.value = AzkarSubCategory.MORNING
                    }
                )
            } else {
            when (selectedTab) {
                MainBottomTab.HOME -> {
                    HomeDashboardScreen(
                        favoritesCount = favoriteItems.value.size,
                        lastReadingTitle = lastReadingTitle,
                        lastReadingAction = lastReadingAction,
                        soundEnabled = soundEnabled,
                        hapticEnabled = vibrationEnabled,
                        onActionClick = { action ->
                            runCatching {
                                when (action) {
                                    HomeQuickAction.AZKAR -> {
                                        recordLastReading("الأذكار اليومية", action)
                                        selectedTabState.value = MainBottomTab.AZKAR
                                        selectedAzkarCategoryState.value = null
                                    }
                                    HomeQuickAction.DUA -> {
                                        recordLastReading("الأدعية المختارة", action)
                                        selectedTabState.value = MainBottomTab.DUA
                                    }
                                    HomeQuickAction.FOLLOW_US -> {
                                        recordLastReading("تابعنا ومشاركة التطبيق", action)
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "تطبيق أثر مسلم Pro — للأذكار والأدعية والقرآن والأحاديث."
                                            )
                                        }
                                        val chooser = Intent.createChooser(shareIntent, "مشاركة أثر مسلم")
                                        if (context !is Activity) {
                                            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(chooser)
                                    }
                                    else -> {
                                        recordLastReading(action.title, action)
                                        activeHomeActionState.value = action
                                    }
                                }
                            }
                        }
                    )
                }

                MainBottomTab.AZKAR -> {
                    val activeCat = selectedAzkarCategory
                    if (activeCat == null) {
                        AzkarCategoriesVerticalList(
                            categories = subCategories,
                            countsMap = countsMap,
                            soundEnabled = soundEnabled,
                            hapticEnabled = vibrationEnabled,
                            onSelectCategory = { cat ->
                                recordLastReading(cat.title, HomeQuickAction.AZKAR)
                                selectedAzkarCategoryState.value = cat
                            }
                        )
                    } else {
                        ItemsCardsList(
                            items = currentAzkarList,
                            countsMap = countsMap,
                            isZikrMode = true,
                            soundEnabled = soundEnabled,
                            vibrationEnabled = vibrationEnabled,
                            fontScaleStep = fontScaleStep,
                            favoriteIds = favoriteIds,
                            onToggleFavorite = { item -> toggleFavorite(item) },
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
                        favoriteIds = favoriteIds,
                        onToggleFavorite = { item -> toggleFavorite(item) },
                        onSaveCount = { id, newVal -> saveCountForItem(id, newVal) }
                    )
                }

                MainBottomTab.FAVORITES -> {
                    FavoritesScreen(
                        items = favoriteItems.value,
                        soundEnabled = soundEnabled,
                        vibrationEnabled = vibrationEnabled,
                        onToggleFavorite = { item -> toggleFavorite(item) }
                    )
                }

                MainBottomTab.SETTINGS -> {
                    SettingsScreen(
                        selectedTheme = selectedTheme,
                        onSelectTheme = onSelectTheme,
                        voiceAzkarEnabled = voiceAzkarEnabled,
                        onToggleVoiceAzkar = { checked ->
                            voiceAzkarEnabledState.value = checked
                            prefs.edit().putBoolean("voice_azkar_enabled", checked).apply()
                            runCatching {
                                if (checked) {
                                    AzkarReminderReceiver.scheduleVoiceReminderLoop(context, voiceIntensity)
                                    VoiceAzkarPlayer.playNextVoiceReminder(context, voiceIntensity)
                                } else {
                                    AzkarReminderReceiver.cancelVoiceReminderLoop(context)
                                }
                            }
                        },
                        voiceIntensity = voiceIntensity,
                        onSelectVoiceIntensity = { level ->
                            voiceIntensityState.value = level
                            prefs.edit().putString("voice_intensity", level.id).apply()
                            if (voiceAzkarEnabled) {
                                runCatching {
                                    AzkarReminderReceiver.scheduleVoiceReminderLoop(context, level)
                                }
                            }
                        },
                        discoveredVoicesCount = discoveredVoicesCount,
                        remindersEnabled = remindersEnabled,
                        onToggleReminders = { checked ->
                            remindersEnabledState.value = checked
                            prefs.edit().putBoolean("reminders_enabled", checked).apply()
                            if (checked) {
                                runCatching { AzkarReminderReceiver.scheduleAllDailyReminders(context) }
                            }
                        },
                        soundEnabled = soundEnabled,
                        onToggleSound = { checked ->
                            soundEnabledState.value = checked
                            prefs.edit().putBoolean("sound_enabled", checked).apply()
                        },
                        vibrationEnabled = vibrationEnabled,
                        onToggleVibration = { checked ->
                            vibrationEnabledState.value = checked
                            prefs.edit().putBoolean("vibration_enabled", checked).apply()
                        },
                        fontScaleStep = fontScaleStep,
                        onChangeFontScale = { step ->
                            fontScaleStepState.intValue = step
                            prefs.edit().putInt("font_scale_step", step).apply()
                        },
                        onResetAllCounts = { resetAllCountsToDefault() },
                        onRequestPermissions = { triggerPermissionsRequest() },
                        onBackToHome = { selectedTabState.value = MainBottomTab.HOME }
                    )
                }
            }
            }
        }
    }
}

@Composable
private fun AzkarCategoriesVerticalList(
    categories: List<AzkarSubCategory>,
    countsMap: Map<Int, Int>,
    soundEnabled: Boolean,
    hapticEnabled: Boolean,
    onSelectCategory: (AzkarSubCategory) -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
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
                onClick = {
                    runCatching {
                        SoundHelper.playZikrPressSound(
                            enabled = soundEnabled,
                            isCompletion = false,
                            view = view,
                            hapticEnabled = hapticEnabled
                        )
                    }
                    onSelectCategory(category)
                },
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
private fun ItemsCardsList(
    items: List<ZikrItem>,
    countsMap: MutableMap<Int, Int>,
    isZikrMode: Boolean,
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    fontScaleStep: Int,
    favoriteIds: Set<Int>,
    onToggleFavorite: (ZikrItem) -> Unit,
    onSaveCount: (Int, Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = if (isZikrMode) 16.dp else 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
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
                isFavorite = favoriteIds.contains(zikrEntry.id),
                showFavoriteButton = true,
                onToggleFavorite = { onToggleFavorite(zikrEntry) },
                soundEnabled = soundEnabled,
                vibrationEnabled = vibrationEnabled,
                fontScaleStep = fontScaleStep
            )
        }
    }
}

@Composable
private fun HomeActionContent(
    action: HomeQuickAction,
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    favoriteQuranIds: Set<Int>,
    onToggleFavoriteAyah: (Int) -> Unit,
    favoriteIds: Set<Int>,
    onToggleFavorite: (ZikrItem) -> Unit,
    onOpenMorningAzkar: () -> Unit
) {
    when (action) {
        HomeQuickAction.QURAN -> QuickToolScreen(
            page = QuickToolPage.QURAN,
            soundEnabled = soundEnabled,
            hapticEnabled = vibrationEnabled,
            favoriteQuranIds = favoriteQuranIds,
            onToggleFavoriteQuranAyah = onToggleFavoriteAyah,
            onOpenMorningAzkar = onOpenMorningAzkar
        )
        HomeQuickAction.AUDIO -> QuickToolScreen(
            page = QuickToolPage.AUDIO,
            soundEnabled = soundEnabled,
            hapticEnabled = vibrationEnabled,
            onOpenMorningAzkar = onOpenMorningAzkar
        )
        HomeQuickAction.PRAYER -> QuickToolScreen(
            page = QuickToolPage.PRAYER_GUIDE,
            soundEnabled = soundEnabled,
            hapticEnabled = vibrationEnabled,
            onOpenMorningAzkar = onOpenMorningAzkar
        )
        HomeQuickAction.TASBIH -> QuickToolScreen(
            page = QuickToolPage.TASBIH,
            soundEnabled = soundEnabled,
            hapticEnabled = vibrationEnabled,
            onOpenMorningAzkar = onOpenMorningAzkar
        )
        HomeQuickAction.QIBLA -> QiblaCompassScreen()
        HomeQuickAction.HADITH -> HadithScreen(
            soundEnabled = soundEnabled,
            vibrationEnabled = vibrationEnabled,
            favoriteIds = favoriteIds,
            onToggleFavorite = onToggleFavorite
        )
        else -> Unit
    }
}
