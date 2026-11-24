package com.athar.muslim

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Construction
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.athar.muslim.data.AzkarRepository
import com.athar.muslim.data.HadithRepository
import com.athar.muslim.model.*
import com.athar.muslim.ui.components.SkyBottomBar
import com.athar.muslim.ui.components.SkyHeaderDecor
import com.athar.muslim.ui.components.SoundHelper
import com.athar.muslim.ui.components.ZikrCard
import com.athar.muslim.ui.theme.*
import kotlinx.coroutines.launch

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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AtharMuslimApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("athar_muslim_prefs", Context.MODE_PRIVATE) }

    var soundEnabled by remember { mutableStateOf(prefs.getBoolean("sound_enabled", true)) }
    var vibrationEnabled by remember { mutableStateOf(prefs.getBoolean("vibration_enabled", true)) }
    var fontScaleStep by remember { mutableIntStateOf(prefs.getInt("font_scale_step", 1)) }

    var selectedTab by remember { mutableStateOf(MainBottomTab.AZKAR) }
    var selectedHadithTopic by remember { mutableStateOf<HadithTopic?>(null) }
    var hadithLoadState by remember { mutableStateOf<HadithLoadResult?>(null) }
    var isLoadingHadith by remember { mutableStateOf(false) }
    var retryNonce by remember { mutableIntStateOf(0) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val favoriteHadiths = remember { mutableStateMapOf<Int, ZikrItem>() }

    val subCategories = AzkarSubCategory.entries
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { subCategories.size }
    )

    val currentSubCategory = subCategories[pagerState.currentPage]
    val currentAzkarList = remember(currentSubCategory) {
        AzkarRepository.getAzkarByCategory(context, currentSubCategory.key)
    }
    val duasList = remember { AzkarRepository.getDuas(context) }

    val countsMap = remember { mutableStateMapOf<Int, Int>() }

    fun ensureCountsInitialized(list: List<ZikrItem>) {
        list.forEach { zikrEntry ->
            if (!countsMap.containsKey(zikrEntry.id)) {
                countsMap[zikrEntry.id] = zikrEntry.count
            }
        }
    }

    LaunchedEffect(Unit) {
        subCategories.forEach { cat ->
            ensureCountsInitialized(AzkarRepository.getAzkarByCategory(context, cat.key))
        }
        ensureCountsInitialized(duasList)
    }

    LaunchedEffect(selectedTab, selectedHadithTopic, retryNonce) {
        val topic = selectedHadithTopic
        if (selectedTab == MainBottomTab.HADITH && topic != null && !topic.isFavorites) {
            isLoadingHadith = true
            hadithLoadState = null
            val res = HadithRepository.loadTopicHadiths(topic)
            if (res is HadithLoadResult.Success) {
                ensureCountsInitialized(res.items)
            }
            hadithLoadState = res
            isLoadingHadith = false
        }
    }

    val subtitleBadgeText = when (selectedTab) {
        MainBottomTab.AZKAR -> {
            val done = currentAzkarList.count { (countsMap[it.id] ?: it.count) == 0 }
            "${currentSubCategory.title} ($done/${currentAzkarList.size})"
        }
        MainBottomTab.DUA -> {
            val done = duasList.count { (countsMap[it.id] ?: it.count) == 0 }
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
                    // تبديل صوت النقر
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "صوت النقر عند التسبيح",
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

                    // تبديل الاهتزاز اللمسي
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
                                text = "استجابة لمسية خفيفة مع كل ضغطة",
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

                    // حجم خط النصوص
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

                    // تصفير جميع العدادات
                    Button(
                        onClick = {
                            countsMap.keys.toList().forEach { key ->
                                val original = (subCategories.flatMap {
                                    AzkarRepository.getAzkarByCategory(context, it.key)
                                } + duasList).find { it.id == key }?.count ?: 1
                                countsMap[key] = original
                            }
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
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 6.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(subCategories.size) { index ->
                                val cat = subCategories[index]
                                val isSelected = pagerState.currentPage == index
                                Surface(
                                    onClick = {
                                        scope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    },
                                    shape = RoundedCornerShape(50),
                                    color = if (isSelected) BlueRoyal else SkySurface,
                                    border = BorderStroke(
                                        1.2.dp,
                                        if (isSelected) GoldAccent else SkyBorder
                                    ),
                                    shadowElevation = if (isSelected) 3.dp else 1.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = cat.icon,
                                            contentDescription = null,
                                            tint = if (isSelected) SkySurface else BlueRoyal,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = cat.title,
                                            color = if (isSelected) SkySurface else NavyDark,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { pageIndex ->
                            val pageCat = subCategories[pageIndex]
                            val pageItems = remember(pageCat) {
                                AzkarRepository.getAzkarByCategory(context, pageCat.key)
                            }
                            ItemsCardsList(
                                items = pageItems,
                                countsMap = countsMap,
                                isZikrMode = true,
                                soundEnabled = soundEnabled,
                                vibrationEnabled = vibrationEnabled,
                                fontScaleStep = fontScaleStep
                            )
                        }
                    }
                }

                MainBottomTab.DUA -> {
                    ItemsCardsList(
                        items = duasList,
                        countsMap = countsMap,
                        isZikrMode = false,
                        soundEnabled = soundEnabled,
                        vibrationEnabled = vibrationEnabled,
                        fontScaleStep = fontScaleStep
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
                            onBack = { selectedHadithTopic = null },
                            onRetry = { retryNonce++ },
                            onToggleFavorite = { hadithItem ->
                                if (favoriteHadiths.containsKey(hadithItem.id)) {
                                    favoriteHadiths.remove(hadithItem.id)
                                } else {
                                    favoriteHadiths[hadithItem.id] = hadithItem
                                }
                            }
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
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onToggleFavorite: (ZikrItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onBack,
                shape = RoundedCornerShape(50),
                color = SkySurface,
                border = BorderStroke(1.2.dp, SkyBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowForward,
                        contentDescription = "رجوع",
                        tint = BlueRoyal,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "الأبواب",
                        color = NavyDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

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
                    contentPadding = PaddingValues(top = 4.dp, bottom = 18.dp)
                ) {
                    items(favList, key = { it.id }) { hadithItem ->
                        val rem = countsMap[hadithItem.id] ?: hadithItem.count
                        ZikrCard(
                            item = hadithItem,
                            remainingCount = rem,
                            onDecrement = {
                                if (rem > 0) countsMap[hadithItem.id] = rem - 1
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
                    contentPadding = PaddingValues(top = 4.dp, bottom = 18.dp)
                ) {
                    items(list, key = { it.id }) { hadithItem ->
                        val rem = countsMap[hadithItem.id] ?: hadithItem.count
                        ZikrCard(
                            item = hadithItem,
                            remainingCount = rem,
                            onDecrement = {
                                if (rem > 0) countsMap[hadithItem.id] = rem - 1
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
    fontScaleStep: Int
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
                        countsMap[zikrEntry.id] = currentCount - 1
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
                        text = "نسخة أثر مسلم • الإصدار 2.1",
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
