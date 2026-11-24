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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Construction
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                AtharMuslimTheme {
                    AtharMuslimApp()
                }
            }
        }
    }
}

@Composable
fun AtharMuslimApp() {
    val context = LocalContext.current
    val activity = context as? Activity
    val prefs = remember { context.getSharedPreferences("athar_muslim_prefs", Context.MODE_PRIVATE) }

    var soundEnabled by remember { mutableStateOf(prefs.getBoolean("sound_enabled", true)) }
    var vibrationEnabled by remember { mutableStateOf(prefs.getBoolean("vibration_enabled", true)) }
    var remindersEnabled by remember { mutableStateOf(prefs.getBoolean("reminders_enabled", true)) }
    var fontScaleStep by remember { mutableIntStateOf(prefs.getInt("font_scale_step", 1)) }

    var selectedTab by remember { mutableStateOf(MainBottomTab.AZKAR) }
    var selectedAzkarCategory by remember { mutableStateOf<AzkarSubCategory?>(null) }
    var selectedHadithTopic by remember { mutableStateOf<HadithTopic?>(null) }
    var hadithLoadState by remember { mutableStateOf<HadithLoadResult?>(null) }
    var isLoadingHadith by remember { mutableStateOf(false) }
    var retryNonce by remember { mutableIntStateOf(0) }
    var showSettingsDialog by remember { mutableStateOf(false) }
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

        if (savedDay != currentDay) {
            val editor = prefs.edit().putString("counts_day_key", currentDay)
            allItems.forEach { entry ->
                countsMap[entry.id] = entry.count
                editor.putInt("zikr_count_${entry.id}", entry.count)
            }
            editor.apply()
        } else {
            val editor = prefs.edit().putString("counts_day_key", currentDay)
            allItems.forEach { entry ->
                val stored = prefs.getInt("zikr_count_${entry.id}", entry.count)
                countsMap[entry.id] = stored
                if (!prefs.contains("zikr_count_${entry.id}")) {
                    editor.putInt("zikr_count_${entry.id}", entry.count)
                }
            }
            editor.apply()
        }
    }

    // رجوع خطوة بخطوة:
    // 1) إغلاق النوافذ المنبثقة إن كانت مفتوحة
    // 2) الخروج من قسم الأذكار إلى قائمة أقسام الأذكار
    // 3) الخروج من باب الأحاديث إلى قائمة أبواب الأحاديث
    // 4) العودة من أي تبويب آخر إلى تبويب الأذكار الرئيسي
    // 5) طلب تأكيد الخروج النهائي عند الوصول إلى الجذر الرئيسي للأذكار
    BackHandler(enabled = true) {
        when {
            showSettingsDialog -> showSettingsDialog = false
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

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            containerColor = SkySurface,
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "تأكيد الخروج من التطبيق",
                    color = NavyDark,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "هل تريد الخروج من تطبيق أثر مسلم الآن؟",
                    color = TextBody,
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
                    colors = ButtonDefaults.buttonColors(containerColor = BlueRoyal)
                ) {
                    Text("خروج", color = SkySurface, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("البقاء", color = NavyDark, fontWeight = FontWeight.Bold)
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
            containerColor = SkySurface,
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "إذن إشعارات الأذكار",
                    color = NavyDark,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "اسمح للتطبيق بإرسال إشعارات أذكار الصباح والمساء والنوم لتصلك حتى عند إغلاق الشاشة أو استخدام تطبيق آخر.",
                    color = TextBody,
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
                    colors = ButtonDefaults.buttonColors(containerColor = BlueRoyal)
                ) {
                    Text("منح الإذن", color = SkySurface, fontWeight = FontWeight.Bold)
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
                    Text("لاحقاً", color = NavyDark)
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
            containerColor = SkySurface,
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "إذن التنبيه في الموعد المحدد",
                    color = NavyDark,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "لتصل إشعارات الأذكار بدقة في وقتها اليومي حتى في وضع السكون، فعّل خيار المنبهات والتذكيرات من الإعدادات.",
                    color = TextBody,
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
                    colors = ButtonDefaults.buttonColors(containerColor = BlueRoyal)
                ) {
                    Text("فتح الإعدادات", color = SkySurface, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        prefs.edit().putBoolean("exact_alarm_prompted", true).apply()
                        permissionStep = PermissionStep.NONE
                    }
                ) {
                    Text("تخطي", color = NavyDark)
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

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            containerColor = SkySurface,
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = "إعدادات أثر مسلم",
                    color = NavyDark,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "إشعارات الأذكار اليومية",
                                color = NavyDark,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "تذكير الصباح والمساء والنوم حتى في وضع السكون",
                                color = TextBody.copy(alpha = 0.75f),
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = remindersEnabled,
                            onCheckedChange = { checked ->
                                remindersEnabled = checked
                                prefs.edit().putBoolean("reminders_enabled", checked).apply()
                                if (checked) {
                                    AzkarReminderReceiver.scheduleAllDailyReminders(context)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SkySurface,
                                checkedTrackColor = BlueRoyal
                            )
                        )
                    }

                    HorizontalDivider(color = SkyBorder.copy(alpha = 0.6f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "صوت اللمس والنقر",
                                color = NavyDark,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "نغمة نقر ناعمة ونغمة إتمام لطيفة",
                                color = TextBody.copy(alpha = 0.75f),
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = { checked ->
                                soundEnabled = checked
                                prefs.edit().putBoolean("sound_enabled", checked).apply()
                                if (checked) {
                                    SoundHelper.playTapSound(enabled = true, isCompletion = false)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SkySurface,
                                checkedTrackColor = BlueRoyal
                            )
                        )
                    }

                    HorizontalDivider(color = SkyBorder.copy(alpha = 0.6f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "الاهتزاز اللمسي",
                                color = NavyDark,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "استجابة لمسية خفيفة مع كل لمسة",
                                color = TextBody.copy(alpha = 0.75f),
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = vibrationEnabled,
                            onCheckedChange = { checked ->
                                vibrationEnabled = checked
                                prefs.edit().putBoolean("vibration_enabled", checked).apply()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SkySurface,
                                checkedTrackColor = BlueRoyal
                            )
                        )
                    }

                    HorizontalDivider(color = SkyBorder.copy(alpha = 0.6f))

                    Text(
                        text = "حجم خط الأذكار والأحاديث",
                        color = NavyDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val sizeOptions = listOf(0 to "صغير", 1 to "متوسط", 2 to "كبير")
                        sizeOptions.forEach { (step, label) ->
                            val selected = fontScaleStep == step
                            Surface(
                                onClick = {
                                    fontScaleStep = step
                                    prefs.edit().putInt("font_scale_step", step).apply()
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) BlueRoyal else SkyContainer,
                                border = BorderStroke(1.dp, if (selected) GoldAccent else SkyBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (selected) SkySurface else NavyDark,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = SkyBorder.copy(alpha = 0.6f))

                    Button(
                        onClick = {
                            resetAllCountsToDefault()
                            showSettingsDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BlueRoyal),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = null,
                            tint = SkySurface,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تصفير جميع العدادات", color = SkySurface, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("إغلاق", color = NavyDark, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            SkyHeaderDecor(
                title = "أثر مسلم",
                subtitleBadge = subtitleBadgeText,
                onSettingsClick = { showSettingsDialog = true }
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
        containerColor = SkyBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SkyBackground)
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
                            onSelectTopic = { topic ->
                                selectedHadithTopic = topic
                            }
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
                colors = CardDefaults.cardColors(containerColor = SkySurface),
                border = BorderStroke(
                    1.5.dp,
                    if (isCategoryCompleted) GoldAccent else SkyBorder
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
                            color = if (isCategoryCompleted) BlueRoyal else SkyContainer,
                            border = BorderStroke(1.dp, SkyBorder),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = category.icon,
                                    contentDescription = null,
                                    tint = if (isCategoryCompleted) SkySurface else BlueRoyal,
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
                                    color = NavyDark,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                if (isCategoryCompleted) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = "مكتمل",
                                        tint = BlueRoyal,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = category.subtitle,
                                color = TextBody.copy(alpha = 0.8f),
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
                            color = if (isCategoryCompleted) BlueRoyal else SkyContainer,
                            border = BorderStroke(1.dp, SkyBorder)
                        ) {
                            Text(
                                text = if (isCategoryCompleted) "مكتمل $doneCount/$totalCount" else "$doneCount/$totalCount",
                                color = if (isCategoryCompleted) SkySurface else NavyDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.ChevronLeft,
                            contentDescription = null,
                            tint = BlueRoyal,
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
                colors = CardDefaults.cardColors(containerColor = SkySurface),
                border = BorderStroke(1.5.dp, SkyBorder),
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
                            color = SkyContainer,
                            border = BorderStroke(1.dp, SkyBorder),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = topic.icon,
                                    contentDescription = null,
                                    tint = BlueRoyal,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = topic.title,
                                color = NavyDark,
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
                                color = TextBody.copy(alpha = 0.8f),
                                fontSize = 12.5.sp
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Rounded.ChevronLeft,
                        contentDescription = null,
                        tint = BlueRoyal,
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
                        color = NavyDark,
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
                        CircularProgressIndicator(color = BlueRoyal)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "جاري تحميل الأحاديث...",
                            color = NavyDark,
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
                        colors = CardDefaults.cardColors(containerColor = SkySurface),
                        border = BorderStroke(1.5.dp, SkyBorder),
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
                                tint = BlueRoyal,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "هذا القسم قيد التطوير",
                                color = NavyDark,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = onRetry,
                                colors = ButtonDefaults.buttonColors(containerColor = BlueRoyal)
                            ) {
                                Text("إعادة المحاولة", color = SkySurface, fontWeight = FontWeight.Bold)
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = SkySurface),
            border = BorderStroke(1.8.dp, SkyBorder),
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
                    color = BlueRoyal,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "رفيقك اليومي للأذكار والأدعية والأحاديث بتصميم سماوي هادئ ومريح للعين.",
                    color = TextBody,
                    fontSize = 15.sp,
                    lineHeight = 25.sp,
                    textAlign = TextAlign.Center
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = SkyContainer,
                    border = BorderStroke(1.dp, SkyBorder)
                ) {
                    Text(
                        text = "نسخة أثر مسلم • الإصدار 2.8.2",
                        color = NavyDark,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
