package com.athar.muslim.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.athar.muslim.model.MainBottomTab
import com.athar.muslim.ui.theme.*

@Composable
fun SkyBottomBar(
    selectedTab: MainBottomTab,
    onTabSelected: (MainBottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val orderedTabs = listOf(
        MainBottomTab.AZKAR,
        MainBottomTab.DUA,
        MainBottomTab.HADITH,
        MainBottomTab.FOLLOW_US
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .height(82.dp)
    ) {
        Canvas(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(width = 76.dp, height = 28.dp)
        ) {
            val w = size.width
            val h = size.height
            drawArc(
                color = SkyOrnament,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(w * 0.30f, h * 0.25f),
                size = Size(w * 0.40f, h * 0.75f)
            )
            drawArc(
                color = SkyOrnament.copy(alpha = 0.8f),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(w * 0.14f, h * 0.50f),
                size = Size(w * 0.22f, h * 0.50f)
            )
            drawArc(
                color = SkyOrnament.copy(alpha = 0.8f),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(w * 0.64f, h * 0.50f),
                size = Size(w * 0.22f, h * 0.50f)
            )
        }

        Surface(
            shape = RoundedCornerShape(22.dp),
            color = SkySurface,
            border = BorderStroke(1.5.dp, GoldAccent.copy(alpha = 0.85f)),
            shadowElevation = 6.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(66.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(SkyBackground, SkySurface)
                        )
                    )
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                orderedTabs.forEachIndexed { idx, tab ->
                    val isSelected = selectedTab == tab
                    val itemModifier = Modifier
                        .weight(if (isSelected) 1.25f else 1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onTabSelected(tab) }

                    if (isSelected) {
                        Box(
                            modifier = itemModifier
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(BluePrimary, BlueRoyal)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label,
                                    tint = SkySurface,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = tab.label,
                                    color = SkySurface,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .width(28.dp)
                                        .height(2.5.dp)
                                        .background(GoldAccent, RoundedCornerShape(50))
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = itemModifier,
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label,
                                    tint = BlueRoyal,
                                    modifier = Modifier.size(21.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = tab.label,
                                    color = NavyDark,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (idx < orderedTabs.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(34.dp)
                                .background(SkyBorder.copy(alpha = 0.7f))
                        )
                    }
                }
            }
        }
    }
}
