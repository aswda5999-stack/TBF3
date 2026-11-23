package com.athar.muslim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.athar.muslim.data.AzkarRepository
import com.athar.muslim.model.ZikrCategory
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
                    AtharMainScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtharMainScreen() {
    val context = LocalContext.current
    val categories = ZikrCategory.entries
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { categories.size }
    )
    val coroutineScope = rememberCoroutineScope()

    val selectedCategory = categories[pagerState.currentPage]
    val currentAzkarList = remember(selectedCategory) {
        AzkarRepository.getAzkarByCategory(context, selectedCategory.key)
    }

    val countsMap = remember { mutableStateMapOf<Int, Int>() }
    categories.forEach { cat ->
        AzkarRepository.getAzkarByCategory(context, cat.key).forEach { zikr ->
            if (!countsMap.containsKey(zikr.id)) {
                countsMap[zikr.id] = zikr.count
            }
        }
    }

    val completedItems = currentAzkarList.count { (countsMap[it.id] ?: it.count) == 0 }
    val totalProgress by animateFloatAsState(
        targetValue = if (currentAzkarList.isEmpty()) 0f else completedItems.toFloat() / currentAzkarList.size,
        label = "totalProgress"
    )

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "أثر مسلم",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = NavyDark
                            )
                            Text(
                                text = "أذكار ${selectedCategory.title} ($completedItems/${currentAzkarList.size})",
                                fontSize = 12.sp,
                                color = BluePrimary
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                currentAzkarList.forEach { zikr ->
                                    countsMap[zikr.id] = zikr.count
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "إعادة العدادات",
                                tint = NavyDark
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = SkyContainer
                    )
                )
                LinearProgressIndicator(
                    progress = { totalProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = BluePrimary,
                    trackColor = SkyAccent
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = SkySurface,
                tonalElevation = 12.dp
            ) {
                categories.forEachIndexed { index, category ->
                    val isSelected = pagerState.currentPage == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = category.title
                            )
                        },
                        label = {
                            Text(
                                text = category.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NavyDark,
                            selectedTextColor = NavyDark,
                            indicatorColor = SkyContainer,
                            unselectedIconColor = BluePrimary.copy(alpha = 0.7f),
                            unselectedTextColor = TextBody.copy(alpha = 0.7f)
                        )
                    )
                }
            }
        },
        containerColor = SkyBackground
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { pageIndex ->
            val pageCategory = categories[pageIndex]
            val pageAzkarList = remember(pageCategory) {
                AzkarRepository.getAzkarByCategory(context, pageCategory.key)
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
            ) {
                items(pageAzkarList, key = { it.id }) { zikr ->
                    val currentRemaining = countsMap[zikr.id] ?: zikr.count
                    ZikrCard(
                        zikr = zikr,
                        remainingCount = currentRemaining,
                        onDecrement = {
                            if (currentRemaining > 0) {
                                countsMap[zikr.id] = currentRemaining - 1
                            }
                        }
                    )
                }
            }
        }
    }
}
