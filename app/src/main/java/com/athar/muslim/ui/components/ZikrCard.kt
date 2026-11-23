package com.athar.muslim.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
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

    val buttonColor by animateColorAsState(
        targetValue = if (isDone) BluePrimary else SkyContainer,
        label = "buttonColor"
    )
    val buttonContentColor by animateColorAsState(
        targetValue = if (isDone) SkySurface else NavyDark,
        label = "buttonContentColor"
    )
    val progress by animateFloatAsState(
        targetValue = (zikr.count - remainingCount).toFloat() / zikr.count.toFloat(),
        label = "zikrProgress"
    )

    val formattedShareText = "${zikr.text}\n\n— تطبيق أثر مسلم"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SkySurface),
        border = BorderStroke(1.dp, SkyAccent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // شريط علوي صغير: عدد التكرار الأصلي + زر نسخ + زر مشاركة
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
                        text = "المطلوب: ${zikr.count}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("zikr", formattedShareText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "تم نسخ الذكر", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = "نسخ",
                            tint = BluePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, formattedShareText)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "مشاركة الذكر"))
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "مشاركة",
                            tint = BluePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // نص الذكر
            Text(
                text = zikr.text,
                fontSize = 18.sp,
                lineHeight = 30.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                color = TextBody
            )

            // الفضل أو الفائدة إن وجدت
            if (zikr.benefit.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "✨ ${zikr.benefit}",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = BluePrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // شريط التقدم الخاص بالذكر
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(CircleShape),
                color = BluePrimary,
                trackColor = SkyContainer,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // زر العداد المصغّر (يتلوّن الزر فقط عند الاكتمال)
            Button(
                onClick = {
                    if (!isDone) {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onDecrement()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = buttonContentColor,
                    disabledContainerColor = BluePrimary,
                    disabledContentColor = SkySurface
                ),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isDone) "اضغط • مكتمل ($remainingCount)" else "اضغط • $remainingCount",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
