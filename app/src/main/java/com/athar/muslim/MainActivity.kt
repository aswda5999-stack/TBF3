package com.athar.muslim

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Construction
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
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
import com.athar.muslim.ui.screens.HomeDashboardScreen
import com.athar.muslim.ui.screens.HomeQuickAction
import com.athar.muslim.ui.screens.SettingsScreen
import com.athar.muslim.ui.theme.AppThemePreset
import com.athar.muslim.ui.theme.AtharMuslimTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class PermissionStep {
    NONE,
    NOTIFICATIONS,
    EXACT_ALARM
}

private enum class QuickToolPage(val title: String) {
    TASBIH("المسبحة الإلكترونية"),
    QIBLA("بوصلة القبلة"),
    PRAYER_GUIDE("تعلم الصلاة"),
    MORNING_FOCUS("ورد اليوم")
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

    var selectedTab by remember { mutableStateOf(MainBottomTab.HOME) }
    var activeToolPage by remember { mutableStateOf<QuickToolPage?>(null) }
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
            activeToolPage != null -> activeToolPage = null
            selectedTab == MainBottomTab.AZKAR && selectedAzkarCategory != null -> {
                selectedAzkarCategory = null
            }
            selectedTab == MainBottomTab.HADITH && selectedHadithTopic != null -> {
                selectedHadithTopic = null
            }
            selectedTab != MainBottomTab.HOME -> {
                selectedAzkarCategory = null
                selectedHadithTopic = null
                activeToolPage = null
                selectedTab = MainBottomTab.HOME
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

    val subtitleBadgeText = activeToolPage?.title ?: when (selectedTab) {
        MainBottomTab.HOME -> "الرئيسية • اختصارات سريعة"
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
                    activeToolPage = null
                    selectedHadithTopic = null
                    if (tab != MainBottomTab.AZKAR) {
                        selectedAzkarCategory = null
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
                    page = activeToolPage!!,
                    soundEnabled = soundEnabled,
                    vibrationEnabled = vibrationEnabled,
                    onOpenMorningAzkar = {
                        activeToolPage = null
                        selectedTab = MainBottomTab.AZKAR
                        selectedAzkarCategory = AzkarSubCategory.entries.firstOrNull { it.key == "morning" }
                    }
                )
                return@Box
            }

            if (selectedHadithTopic != null) {
                HadithTopicDetailScreen(
                    topic = selectedHadithTopic!!,
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
                return@Box
            }

            when (selectedTab) {
                MainBottomTab.HOME -> {
                    HomeDashboardScreen(
                        favoritesCount = favoriteHadiths.size,
                        onActionClick = { action ->
                            when (action) {
                                HomeQuickAction.HADITH -> {
                                    selectedHadithTopic = HadithTopic.entries.firstOrNull { !it.isFavorites }
                                }
                                HomeQuickAction.AZKAR -> {
                                    selectedTab = MainBottomTab.AZKAR
                                    selectedAzkarCategory = null
                                }
                                HomeQuickAction.DUA -> {
                                    selectedTab = MainBottomTab.DUA
                                }
                                HomeQuickAction.FAVORITES -> {
                                    selectedHadithTopic = HadithTopic.entries.firstOrNull { it.isFavorites }
                                }
                                HomeQuickAction.TASBIH -> {
                                    activeToolPage = QuickToolPage.TASBIH
                                }
                                HomeQuickAction.QIBLA -> {
                                    activeToolPage = QuickToolPage.QIBLA
                                }
                                HomeQuickAction.PRAYER_GUIDE -> {
                                    activeToolPage = QuickToolPage.PRAYER_GUIDE
                                }
                                HomeQuickAction.MORNING_FOCUS -> {
                                    activeToolPage = QuickToolPage.MORNING_FOCUS
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

                MainBottomTab.FOLLOW_US -> {
                    FollowUsCardScreen()
                }
            }
        }
    }
}

@Composable
private fun QuickToolScreen(
    page: QuickToolPage,
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    onOpenMorningAzkar: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    var tasbihCount by remember { mutableIntStateOf(0) }
    var targetGoal by remember { mutableIntStateOf(33) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 22.dp)
    ) {
        when (page) {
            QuickToolPage.TASBIH -> {
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
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "سبحان الله وبحمده، سبحان الله العظيم",
                                color = scheme.onSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Surface(
                                shape = CircleShape,
                                color = scheme.primaryContainer,
                                border = BorderStroke(2.dp, scheme.tertiary),
                                modifier = Modifier.size(124.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "$tasbihCount",
                                            color = scheme.primary,
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = "الهدف: $targetGoal",
                                            color = scheme.onPrimaryContainer,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(33, 100, 1000).forEach { goal ->
                                    val selected = targetGoal == goal
                                    Surface(
                                        onClick = { targetGoal = goal },
                                        shape = RoundedCornerShape(50),
                                        color = if (selected) scheme.primary else scheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "$goal",
                                            color = if (selected) scheme.onPrimary else scheme.onPrimaryContainer,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        tasbihCount++
                                        val completed = tasbihCount % targetGoal == 0
                                        SoundHelper.playTapSound(enabled = soundEnabled, isCompletion = completed)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = scheme.primary),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(2f).height(48.dp)
                                ) {
                                    Icon(Icons.Rounded.Add, contentDescription = null, tint = scheme.onPrimary)
                                    Text("تسبيح", color = scheme.onPrimary, fontWeight = FontWeight.ExtraBold)
                                }

                                Button(
                                    onClick = { tasbihCount = 0 },
                                    colors = ButtonDefaults.buttonColors(containerColor = scheme.primaryContainer),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) {
                                    Icon(Icons.Rounded.Refresh, contentDescription = null, tint = scheme.onPrimaryContainer)
                                    Text("تصفير", color = scheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            QuickToolPage.QIBLA -> {
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
                            Icon(Icons.Rounded.Explore, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(44.dp))
                            Text("اتجاه القبلة نحو الكعبة المشرفة", color = scheme.onSurface, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                            Text(
                                text = "لأدق تحديد لاتجاه القبلة، يُنصح بتفعيل الموقع الجغرافي (GPS) والابتعاد عن المجالات المغناطيسية القوية.",
                                color = scheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                lineHeight = 21.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            QuickToolPage.PRAYER_GUIDE -> {
                val steps = listOf(
                    "النية واستقبال القبلة" to "استحضار نية الصلاة في القلب والوقوف باتجاه القبلة بخشوع وطمأنينة.",
                    "تكبيرة الإحرام والقيام" to "رفع اليدين حذو المنكبين أو الأذنين وقول (الله أكبر)، ثم قراءة الفاتحة وما تيسر من القرآن.",
                    "الركوع والاعتدال" to "الانحناء مع تسوية الظهر وقول (سبحان ربي العظيم)، ثم الرفع وقول (سمع الله لمن حمده، ربنا ولك الحمد).",
                    "السجود والتشهد والتسليم" to "السجود على الأعضاء السبعة وقول (سبحان ربي الأعلى)، ثم التشهد الأخير والصلاة الإبراهيمية والتسليم."
                )
                items(steps) { (title, desc) ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = scheme.surface),
                        border = BorderStroke(1.2.dp, scheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Rounded.Mosque, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(18.dp))
                                Text(title, color = scheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            Text(desc, color = scheme.onSurfaceVariant, fontSize = 12.5.sp, lineHeight = 20.sp)
                        }
                    }
                }
            }

            QuickToolPage.MORNING_FOCUS -> {
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
                            Text("ورد اليوم المختار", color = scheme.onSurface, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                            Text(
                                text = "ابدأ يومك بأذكار الصباح والسكينة، وتابع إتمام أعدادك اليومية خطوة بخطوة.",
                                color = scheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                lineHeight = 21.sp,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = onOpenMorningAzkar,
                                colors = ButtonDefaults.buttonColors(containerColor = scheme.primary),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("فتح أذكار الصباح", color = scheme.onPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
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
    val context = LocalContext.current
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "أثر مسلم",
                    color = scheme.primary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "رفيقك اليومي للأذكار والأدعية والأحاديث. شارك التطبيق مع من تحب ليكون لك أجر ونشر للخير.",
                    color = scheme.onBackground,
                    fontSize = 14.5.sp,
                    lineHeight = 24.sp,
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
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.Share, contentDescription = null, tint = scheme.onPrimary)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("مشاركة أثر مسلم", color = scheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
