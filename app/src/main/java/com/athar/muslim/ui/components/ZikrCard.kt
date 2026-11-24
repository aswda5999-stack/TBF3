package com.athar.muslim.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.quadraticTo
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.athar.muslim.model.ZikrItem
import com.athar.muslim.ui.theme.*

@Composable
fun ZikrCard(
    item: ZikrItem,
    remainingCount: Int,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val isDone = remainingCount == 0

    val buttonTopColor by animateColorAsState(
        targetValue = if (isDone) BlueRoyal else BluePrimary,
        label = "buttonTopColor"
    )
    val buttonBottomColor by animateColorAsState(
        targetValue = if (isDone) NavyDark else BlueRoyal,
        label = "buttonBottomColor"
    )

    val shareContent = buildString {
        if (item.title.isNotEmpty()) {
            appendLine(item.title)
        }
        append(item.text)
        append("\n\n— تطبيق أثر مسلم")
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SkySurface),
        border = BorderStroke(1.8.dp, SkyBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                val cornerSize = 54.dp.toPx()

                val rightCorner = Path().apply {
                    moveTo(w, h)
                    lineTo(w - cornerSize, h)
                    quadraticTo(w - cornerSize * 0.35f, h - cornerSize * 0.35f, w, h - cornerSize)
                    close()
                }
                drawPath(path = rightCorner, color = SkyOrnament.copy(alpha = 0.55f))
                drawCircle(
                    color = SkyBorder.copy(alpha = 0.45f),
                    radius = 10.dp.toPx(),
                    center = Offset(w - 18.dp.toPx(), h - 18.dp.toPx()),
                    style = Stroke(width = 1.5.dp.toPx())
                )

                val leftCorner = Path().apply {
                    moveTo(0f, h)
                    lineTo(cornerSize, h)
                    quadraticTo(cornerSize * 0.35f, h - cornerSize * 0.35f, 0f, h - cornerSize)
                    close()
                }
                drawPath(path = leftCorner, color = SkyOrnament.copy(alpha = 0.55f))
                drawCircle(
                    color = SkyBorder.copy(alpha = 0.45f),
                    radius = 10.dp.toPx(),
                    center = Offset(18.dp.toPx(), h - 18.dp.toPx()),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            onClick = {
                                val clipboard =
                                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("athar_zikr", shareContent))
                                Toast.makeText(context, "تم نسخ النص", Toast.LENGTH_SHORT).show()
                            },
                            shape = CircleShape,
                            color = SkyContainer,
                            border = BorderStroke(1.dp, SkyBorder),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.ContentCopy,
                                    contentDescription = "نسخ",
                                    tint = BlueRoyal,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }

                        Surface(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareContent)
                                }
                                context.startActivity(Intent.createChooser(intent, "مشاركة"))
                            },
                            shape = CircleShape,
                            color = SkyContainer,
                            border = BorderStroke(1.dp, SkyBorder),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Share,
                                    contentDescription = "مشاركة",
                                    tint = BlueRoyal,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = BlueRoyal
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "المطلوب : ${item.count}",
                                color = SkySurface,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Rounded.MenuBook,
                                contentDescription = null,
                                tint = SkySurface,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                if (item.title.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.title,
                        color = BlueRoyal,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = item.text,
                    color = TextBody,
                    fontSize = 18.sp,
                    lineHeight = 31.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth()
                )

                if (item.benefit.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SkyContainer.copy(alpha = 0.78f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "✨ ${item.benefit}",
                            color = NavyDark,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    onClick = {
                        if (!isDone) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onDecrement()
                        }
                    },
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.5.dp, if (isDone) GoldAccent else SkyBorder),
                    shadowElevation = if (isDone) 4.dp else 2.dp,
                    color = Color.Transparent,
                    modifier = Modifier
                        .widthIn(min = 165.dp, max = 240.dp)
                        .height(42.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(buttonTopColor, buttonBottomColor)
                                )
                            )
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = SkySurface,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isDone) "اضغط • مكتمل ($remainingCount)" else "اضغط • $remainingCount",
                                color = SkySurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
