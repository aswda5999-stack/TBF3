package com.athar.muslim

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.athar.muslim.audio.VoiceAzkarPlayer
import com.athar.muslim.data.AzkarRepository
import com.athar.muslim.data.HadithRepository
import com.athar.muslim.model.AzkarSubCategory
import com.athar.muslim.model.HadithLoadResult
import com.athar.muslim.model.HadithTopic
import com.athar.muslim.model.MainBottomTab
import com.athar.muslim.model.VoiceReminderIntensity
import com.athar.muslim.model.ZikrItem
import com.athar.muslim.notifications.AzkarReminderReceiver
import com.athar.muslim.permissions.AppPermissionsManager
import com.athar.muslim.ui.components.SkyBottomBar
import com.athar.muslim.ui.components.SkyHeaderDecor
import com.athar.muslim.ui.components.SoundHelper
import com.athar.muslim.ui.components.ZikrCard
import com.athar.muslim.ui.screens.HomeDashboardScreen
import com.athar.muslim.ui.screens.QuickToolPage
import com.athar.muslim.ui.screens.QuickToolScreen
import com.athar.muslim.ui.screens.SettingsScreen
import com.athar.muslim.ui.screens.buttons.HomeQuickAction
import com.athar.muslim.ui.theme.AppThemePreset
import com.athar.muslim.ui.theme.AtharMuslimTheme
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
    val activeToolPageState = remember { mutableStateOf<QuickToolPage?>(null) }
    val selectedAzkarCategoryState = remember { mutableStateOf<AzkarSubCategory?>(null) }
    val selectedHadithTopicState = remember { mutableStateOf<HadithTopic?>(null) }
    val hadithLoadState = remember { mutableStateOf<HadithLoadResult?>(null) }
    val isLoadingHadithState = remember { mutableStateOf(false) }
    val retryNonceState = remember { mutableIntStateOf(0) }
    val showExitDialogState = remember { mutableStateOf(false) }
    val permissionStepState = remember { mutableStateOf(PermissionPromptStep.NONE) }

    val soundEnabled = soundEnabledState.value
    val vibrationEnabled = vibrationEnabledState.value
    val remindersEnabled = remindersEnabledState.value
    val voiceAzkarEnabled = voiceAzkarEnabledState.value
    val voiceIntensity = voiceIntensityState.value
    val fontScaleStep = fontScaleStepState.intValue
    val selectedTab = selectedTabState.value
    val activeToolPage = activeToolPageState.value
    val selectedAzkarCategory = selectedAzkarCategoryState.value
    val selectedHadithTopic = selectedHadithTopicState.value

    val discoveredVoicesCount = remember {
        VoiceAzkarPlayer.discoverRawVoiceResIds(context).size
    }

    val favoriteHadiths = remember { mutableStateMapOf<Int, ZikrItem>() }
    val favoriteQuranIds = remember { mutableStateMapOf<Int, Boolean>() }
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

    val exactAlarmLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        prefs.edit().putBoolean("exact_alarm_prompted", true).apply()
        AzkarReminderReceiver.scheduleAllDailyReminders(context)
        permissionStepState.value = PermissionPromptStep.NONE
    }

    val multiPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        prefs.edit().putBoolean("runtime_perms_prompted", true).apply()
        AzkarReminderReceiver.ensureNotificationChannel(context)
        AzkarReminderReceiver.scheduleAllDailyReminders(context)
        if (!AzkarReminderReceiver.canScheduleExactAlarms(context) &&
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
        SoundHelper.playSystemTouchSound(context, soundEnabled, view, vibrationEnabled)
        when {
            showExitDialogState.value -> showExitDialogState.value = false
            activeToolPage != null -> activeToolPageState.value = null
            selectedHadithTopic != null -> selectedHadithTopicState.value = null
            selectedTab == MainBottomTab.AZKAR && selectedAzkarCategory != null -> {
                selectedAzkarCategoryState.value = null
            }
            selectedTab != MainBottomTab.HOME -> {
                selectedAzkarCategoryState.value = null
                selectedHadithTopicState.value = null
                activeToolPageState.value = null
                selectedTabState.value = MainBottomTab.HOME
            }
            else -> showExitDialogState.value = true
        }
    }

    LaunchedEffect(Unit) {
        syncDailyCounts()
        AzkarReminderReceiver.scheduleAllDailyReminders(context)

        if (!AppPermissionsManager.hasAllRequiredRuntimePermissions(context) &&
            !prefs.getBoolean("runtime_perms_prompted", false)
        ) {
            permissionStepState.value = PermissionPromptStep.RUNTIME_BUNDLE
        } else if (!AzkarReminderReceiver.canScheduleExactAlarms(context) &&
            !prefs.getBoolean("exact_alarm_prompted", false)
        ) {
            permissionStepState.value = PermissionPromptStep.EXACT_ALARM
        }
    }

    LaunchedEffect(selectedHadithTopic, retryNonceState.intValue) {
        val topic = selectedHadithTopic
        if (topic != null && !topic.isFavorites) {
            isLoadingHadithState.value = true
            hadithLoadState.value = null
            val res = HadithRepository.loadTopicHadiths(topic)
            if (res is HadithLoadResult.Success) {
                res.items.forEach { entry ->
                    if (!countsMap.containsKey(entry.id)) {
                        countsMap[entry.id] = entry.count
                    }
                }
            }
            hadithLoadState.value = res
            isLoadingHadithState.value = false
        }
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
                        SoundHelper.playSystemTouchSound(context, soundEnabled, view, vibrationEnabled)
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
                            AzkarReminderReceiver.scheduleAllDailyReminders(context)
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

    val isHomeDashboard = selectedTab == MainBottomTab.HOME &&
        activeToolPage == null &&
        selectedHadithTopic == null

    val subtitleBadgeText = activeToolPage?.title
        ?: selectedHadithTopic?.title
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
                    activeToolPageState.value = null
                    selectedHadithTopicState.value = null
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
            if (activeToolPage != null) {
                QuickToolScreen(
                    page = activeToolPage,
                    soundEnabled = soundEnabled,
                    hapticEnabled = vibrationEnabled,
                    favoriteQuranIds = favoriteQuranIds.keys,
                    onToggleFavoriteQuranAyah = { ayahId ->
                        if (favoriteQuranIds.containsKey(ayahId)) {
                            favoriteQuranIds.remove(ayahId)
                        } else {
                            favoriteQuranIds[ayahId] = true
                        }
                    },
                    onOpenMorningAzkar = {
                        activeToolPageState.value = null
                        selectedTabState.value = MainBottomTab.AZKAR
                        selectedAzkarCategoryState.value = AzkarSubCategory.MORNING
                    }
                )
                return@Box
            }

            if (selectedHadithTopic != null) {
                HadithTopicDetailScreen(
                    topic = selectedHadithTopic,
                    isLoading = isLoadingHadithState.value,
                    loadResult = hadithLoadState.value,
                    favoriteMap = favoriteHadiths,
                    countsMap = countsMap,
                    soundEnabled = soundEnabled,
                    vibrationEnabled = vibrationEnabled,
                    fontScaleStep = fontScaleStep,
                    onRetry = { retryNonceState.intValue++ },
                    onToggleFavorite = { hadithItem ->
                        if (favoriteHadiths.containsKey(hadithItem.id)) {
                            favoriteHadiths.remove(hadithItem.id)
                        } else {
                            favoriteHadiths[hadithItem.id] = hadithItem
                        }
                    },
                    onSaveCount = { id, newVal -> saveCountForItem(id, newVal) }
                )
                return@Box
            }

            when (selectedTab) {
                MainBottomTab.HOME -> {
                    HomeDashboardScreen(
                        favoritesCount = favoriteHadiths.size + favoriteQuranIds.size,
                        soundEnabled = soundEnabled,
                        hapticEnabled = vibrationEnabled,
                        onActionClick = { action ->
                            when (action) {
                                HomeQuickAction.QURAN -> activeToolPageState.value = QuickToolPage.QURAN
                                HomeQuickAction.AUDIO -> activeToolPageState.value = QuickToolPage.AUDIO
                                HomeQuickAction.PRAYER -> activeToolPageState.value = QuickToolPage.PRAYER_GUIDE
                                HomeQuickAction.FOLLOW_US -> {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, "تطبيق أثر مسلم — للأذكار والأدعية والأحاديث النبوية.")
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "مشاركة أثر مسلم"))
                                }
                                HomeQuickAction.HADITH -> selectedHadithTopicState.value = HadithTopic.BUKHARI
                                HomeQuickAction.AZKAR -> {
                                    selectedTabState.value = MainBottomTab.AZKAR
                                    selectedAzkarCategoryState.value = null
                                }
                                HomeQuickAction.DUA -> selectedTabState.value = MainBottomTab.DUA
                                HomeQuickAction.TASBIH -> activeToolPageState.value = QuickToolPage.TASBIH
                                HomeQuickAction.QIBLA -> activeToolPageState.value = QuickToolPage.QIBLA
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
                            onSelectCategory = { cat -> selectedAzkarCategoryState.value = cat }
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

                MainBottomTab.SETTINGS -> {
                    SettingsScreen(
                        selectedTheme = selectedTheme,
                        onSelectTheme = onSelectTheme,
                        voiceAzkarEnabled = voiceAzkarEnabled,
                        onToggleVoiceAzkar = { checked ->
                            voiceAzkarEnabledState.value = checked
                            prefs.edit().putBoolean("voice_azkar_enabled", checked).apply()
                            if (checked) {
                                AzkarReminderReceiver.scheduleVoiceReminderLoop(context, voiceIntensity)
                                VoiceAzkarPlayer.playNextVoiceReminder(context, voiceIntensity)
                            } else {
                                AzkarReminderReceiver.cancelVoiceReminderLoop(context)
                            }
                        },
                        voiceIntensity = voiceIntensity,
                        onSelectVoiceIntensity = { level ->
                            voiceIntensityState.value = level
                            prefs.edit().putString("voice_intensity", level.id).apply()
                            if (voiceAzkarEnabled) {
                                AzkarReminderReceiver.scheduleVoiceReminderLoop(context, level)
                            }
                        },
                        discoveredVoicesCount = discoveredVoicesCount,
                        remindersEnabled = remindersEnabled,
                        onToggleReminders = { checked ->
                            remindersEnabledState.value = checked
                            prefs.edit().putBoolean("reminders_enabled", checked).apply()
                            if (checked) {
                                AzkarReminderReceiver.scheduleAllDailyReminders(context)
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
                    SoundHelper.playZikrPressSound(
                        enabled = soundEnabled,
                        isCompletion = false,
                        view = view,
                        hapticEnabled = hapticEnabled
                    )
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
    val context = LocalContext.current
    val view = LocalView.current
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
                                onClick = {
                                    SoundHelper.playSystemTouchSound(
                                        context = context,
                                        enabled = soundEnabled,
                                        view = view,
                                        hapticEnabled = vibrationEnabled
                                    )
                                    onRetry()
                                },
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
