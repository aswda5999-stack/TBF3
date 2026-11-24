package com.athar.muslim

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.athar.muslim.model.AzkarSubCategory
import com.athar.muslim.model.MainBottomTab
import com.athar.muslim.model.ZikrItem
import com.athar.muslim.ui.components.SkyBottomBar
import com.athar.muslim.ui.components.SkyHeaderDecor
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

    var selectedTab by remember { mutableStateOf(MainBottomTab.AZKAR) }
    var showSettingsDialog by remember { mutableStateOf(false) }

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
    val hadithsList = remember { AzkarRepository.getHadiths(context) }

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
        ensureCountsInitialized(hadithsList)
    }

    val activeListForBadge: List<ZikrItem> = when (selectedTab) {
        MainBottomTab.AZKAR -> currentAzkarList
        MainBottomTab.DUA -> duasList
        MainBottomTab.HADITH -> hadithsList
        MainBottomTab.FOLLOW_US -> emptyList()
    }

    val completedCount = activeListForBadge.count { zikrEntry ->
        (countsMap[zikrEntry.id] ?: zikrEntry.count) == 0
    }

    val subtitleBadgeText = when (selectedTab) {
        MainBottomTab.AZKAR -> "${currentSubCategory.title} ($completedCount/${currentAzkarList.size})"
        MainBottomTab.DUA -> "الأدعية المختارة ($completedCount/${duasList.size})"
        MainBottomTab.HADITH -> "الأحاديث النبوية ($completedCount/${hadithsList.size})"
        MainBottomTab.FOLLOW_US -> "تطبيق أثر مسلم"
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            containerColor = SkySurface,
            title = {
                Text(
                    text = "إعدادات أثر مسلم",
                    color = NavyDark,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "يمكنك إعادة ضبط عدادات القسم الحالي أو جميع العدادات بضغطة واحدة.",
                        color = TextBody,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                    OutlinedButton(
                        onClick = {
                            activeListForBadge.forEach { zikrEntry ->
                                countsMap[zikrEntry.id] = zikrEntry.count
                            }
                            showSettingsDialog = false
                        },
                        border = BorderStroke(1.dp, SkyBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = null,
                            tint = BlueRoyal,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تصفير عدادات القسم الحالي", color = BlueRoyal, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            countsMap.keys.toList().forEach { key ->
                                val original = (subCategories.flatMap {
                                    AzkarRepository.getAzkarByCategory(context, it.key)
                                } + duasList + hadithsList).find { it.id == key }?.count ?: 1
                                countsMap[key] = original
                            }
                            showSettingsDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BlueRoyal),
                        modifier = Modifier.fillMaxWidth()
                    ) {
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
                onTabSelected = { selectedTab = it }
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
                                countsMap = countsMap
                            )
                        }
                    }
                }

                MainBottomTab.DUA -> {
                    ItemsCardsList(
                        items = duasList,
                        countsMap = countsMap
                    )
                }

                MainBottomTab.HADITH -> {
                    ItemsCardsList(
                        items = hadithsList,
                        countsMap = countsMap
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
private fun ItemsCardsList(
    items: List<ZikrItem>,
    countsMap: MutableMap<Int, Int>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
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
                }
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
                        text = "نسخة أثر مسلم • الإصدار 2.0",
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
