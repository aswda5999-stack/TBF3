package com.athar.muslim.ui.components

import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.athar.muslim.model.Zikr
import com.athar.muslim.ui.theme.*

@Composable
fun ZikrCard(
    zikr: Zikr,
    remainingCount: Int,
    onDecrement: () -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val isDone = remainingCount == 0

    val cardBg by animateColorAsState(
        targetValue = if (isDone) SkyContainer.copy(alpha = 0.6f) else SkySurface,
        label = "cardBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isDone) BluePrimary.copy(alpha = 0.4f) else SkyAccent,
        label = "borderColor"
    )
    val progress by animateFloatAsState(
        targetValue = (zikr.count - remainingCount).toFloat() / zikr.count.toFloat(),
        label = "zikrProgress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(enabled = !isDone) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onDecrement()
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDone) 0.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = SkyContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "التكرار: ${zikr.count}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )
                }

                IconButton(
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "${zikr.text}\n\n— تطبيق أثر مسلم")
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "مشاركة الذكر"))
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = "مشاركة",
                        tint = BluePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = zikr.text,
                fontSize = 19.sp,
                lineHeight = 32.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                color = if (isDone) NavyDark.copy(alpha = 0.55f) else TextBody
            )

            if (zikr.benefit.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "✨ ${zikr.benefit}",
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    color = BluePrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = BluePrimary,
                trackColor = SkyContainer,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isDone) BluePrimary else SkyContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isDone) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = SkySurface,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "اكتمل الذكر",
                            fontWeight = FontWeight.Bold,
                            color = SkySurface,
                            fontSize = 15.sp
                        )
                    } else {
                        Text(
                            text = "اضغط للتسبيح • المتبقي: $remainingCount",
                            fontWeight = FontWeight.Bold,
                            color = NavyDark,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

