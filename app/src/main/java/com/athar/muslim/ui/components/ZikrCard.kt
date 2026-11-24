package com.athar.muslim.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
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
    isZikrMode: Boolean = true,
    isFavorite: Boolean = false,
    showFavoriteButton: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    soundEnabled: Boolean = true,
    vibrationEnabled: Boolean = true,
    fontScaleStep: Int = 1,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val totalCount = item.count.coerceAtLeast(1)
    val isDone = remainingCount <= 0

    val baseBodySp = 17.5f
    val bodyFontSize = (baseBodySp + (fontScaleStep - 1) * 2f).coerceIn(15f, 24f).sp
    val bodyLineHeight = (bodyFontSize.value * 1.72f).sp

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

    fun triggerTouchFeedback(willComplete: Boolean) {
        if (vibrationEnabled) {
            view.performHapticFeedback(
                if (willComplete) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.KEYBOARD_TAP
            )
        }
        SoundHelper.playTapSound(enabled = soundEnabled, isCompletion = willComplete)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (isZikrMode) 18.dp else 22.dp),
        colors = CardDefaults.cardColors(containerColor = SkySurface),
        border = BorderStroke(1.5.dp, SkyBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                val cornerSize = if (isZikrMode) 36.dp.toPx() else 52.dp.toPx()

                val rightCorner = Path().apply {
                    moveTo(w, h)
                    lineTo(w - cornerSize, h)
                    cubicTo(
                        w - cornerSize * 0.45f, h - cornerSize * 0.25f,
                        w - cornerSize * 0.25f, h - cornerSize * 0.45f,
                        w, h - cornerSize
                    )
                    close()
                }
                drawPath(path = rightCorner, color = SkyOrnament.copy(alpha = 0.5f))

                val leftCorner = Path().apply {
                    moveTo(0f, h)
                    lineTo(cornerSize, h)
                    cubicTo(
                        cornerSize * 0.45f, h - cornerSize * 0.25f,
                        cornerSize * 0.25f, h - cornerSize * 0.45f,
                        0f, h - cornerSize
                    )
                    close()
                }
                drawPath(path = leftCorner, color = SkyOrnament.copy(alpha = 0.5f))
            }

            if (isZikrMode) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = item.text,
                        color = TextBody,
                        fontSize = bodyFontSize,
                        lineHeight = bodyLineHeight,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (item.benefit.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SkyContainer.copy(alpha = 0.72f)
                        ) {
                            Text(
                                text = "✨ ${item.benefit}",
                                color = NavyDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // في RTL يبدأ العنصر الأول من اليمين: زر اضغط / مكتمل على اليمين وبجانبه النسخ والمشاركة
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = {
                                if (!isDone) {
                                    val willComplete = (remainingCount - 1) <= 0
                                    triggerTouchFeedback(willComplete)
                                    onDecrement()
                                }
                            },
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.2.dp, if (isDone) GoldAccent else SkyBorder),
                            shadowElevation = if (isDone) 3.dp else 1.dp,
                            color = Color.Transparent,
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .height(36.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(buttonTopColor, buttonBottomColor)
                                        )
                                    )
                                    .padding(horizontal = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    AnimatedVisibility(
                                        visible = isDone,
                                        enter = fadeIn(animationSpec = tween(220)),
                                        exit = fadeOut(animationSpec = tween(180))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = SkySurface,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    AnimatedVisibility(
                                        visible = !isDone,
                                        enter = fadeIn(animationSpec = tween(220)),
                                        exit = fadeOut(animationSpec = tween(180))
                                    ) {
                                        Text(
                                            text = "اضغط • $remainingCount",
                                            color = SkySurface,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    AnimatedVisibility(
                                        visible = isDone,
                                        enter = fadeIn(animationSpec = tween(250)),
                                        exit = fadeOut(animationSpec = tween(180))
                                    ) {
                                        Text(
                                            text = "مكتمل $totalCount/$totalCount",
                                            color = SkySurface,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Surface(
                            onClick = {
                                val clipboard =
                                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("athar_zikr", shareContent))
                                Toast.makeText(context, "تم نسخ الذكر", Toast.LENGTH_SHORT).show()
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
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareContent)
                                }
                                context.startActivity(Intent.createChooser(intent, "مشاركة الذكر"))
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
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (showFavoriteButton) {
                        if (item.title.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SkyContainer,
                                border = BorderStroke(1.dp, SkyBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.MenuBook,
                                        contentDescription = null,
                                        tint = BlueRoyal,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item.title,
                                        color = NavyDark,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = item.text,
                            color = TextBody,
                            fontSize = bodyFontSize,
                            lineHeight = bodyLineHeight,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (item.benefit.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = item.benefit,
                                color = BluePrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                onClick = {
                                    onToggleFavorite()
                                    Toast.makeText(
                                        context,
                                        if (!isFavorite) "أُضيف إلى المفضلة" else "أُزيل من المفضلة",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                shape = CircleShape,
                                color = if (isFavorite) BlueRoyal else SkyContainer,
                                border = BorderStroke(1.dp, if (isFavorite) GoldAccent else SkyBorder),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                        contentDescription = "المفضلة",
                                        tint = if (isFavorite) SkySurface else BlueRoyal,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                onClick = {
                                    val clipboard =
                                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("athar_hadith", shareContent))
                                    Toast.makeText(context, "تم نسخ الحديث", Toast.LENGTH_SHORT).show()
                                },
                                shape = CircleShape,
                                color = SkyContainer,
                                border = BorderStroke(1.dp, SkyBorder),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.ContentCopy,
                                        contentDescription = "نسخ",
                                        tint = BlueRoyal,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareContent)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "مشاركة الحديث"))
                                },
                                shape = CircleShape,
                                color = SkyContainer,
                                border = BorderStroke(1.dp, SkyBorder),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Share,
                                        contentDescription = "مشاركة",
                                        tint = BlueRoyal,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Surface(
                                onClick = {
                                    if (!isDone) {
                                        val willComplete = (remainingCount - 1) <= 0
                                        triggerTouchFeedback(willComplete)
                                        onDecrement()
                                    }
                                },
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.2.dp, if (isDone) GoldAccent else SkyBorder),
                                color = Color.Transparent,
                                modifier = Modifier.height(38.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(buttonTopColor, buttonBottomColor)
                                            )
                                        )
                                        .padding(horizontal = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = SkySurface,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = if (isDone) "مكتمل" else "اضغط",
                                            color = SkySurface,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    } else {
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
                            fontSize = bodyFontSize,
                            lineHeight = bodyLineHeight,
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
                                    val willComplete = (remainingCount - 1) <= 0
                                    triggerTouchFeedback(willComplete)
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
    }
}
