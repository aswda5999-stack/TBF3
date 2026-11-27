package com.athar.muslim.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.athar.muslim.model.ZikrItem

@Composable
fun ZikrCard(
    item: ZikrItem,
    remainingCount: Int,
    onDecrement: () -> Unit,
    isZikrMode: Boolean = true,
    isFavorite: Boolean = false,
    showFavoriteButton: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null,
    soundEnabled: Boolean = true,
    vibrationEnabled: Boolean = true,
    fontScaleStep: Int = 1,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val isDone = remainingCount <= 0
    val total = item.count.coerceAtLeast(1)
    val progress = ((total - remainingCount.coerceIn(0, total)).toFloat() / total.toFloat())

    val textSizeSp = when (fontScaleStep) {
        0 -> 14.sp
        2 -> 19.sp
        else -> 16.5.sp
    }
    val lineHeightSp = when (fontScaleStep) {
        0 -> 23.sp
        2 -> 31.sp
        else -> 27.sp
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) scheme.primaryContainer.copy(alpha = 0.6f) else scheme.surface
        ),
        border = BorderStroke(
            width = if (isDone) 1.5.dp else 1.dp,
            color = if (isDone) scheme.tertiary else scheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title.ifBlank { item.category },
                    color = scheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onToggleFavorite != null) {
                        IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = "مفضلة",
                                tint = if (isFavorite) scheme.tertiary else scheme.primary,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            runCatching {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("أثر مسلم", item.text))
                                Toast.makeText(context, "تم نسخ النص", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = "نسخ",
                            tint = scheme.primary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            runCatching {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "${item.text}\n\n— تطبيق أثر مسلم")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "مشاركة"))
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "مشاركة",
                            tint = scheme.primary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            Text(
                text = item.text,
                color = scheme.onSurface,
                fontSize = textSizeSp,
                lineHeight = lineHeightSp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            if (item.benefit.isNotBlank()) {
                Text(
                    text = item.benefit,
                    color = scheme.onSurfaceVariant,
                    fontSize = 11.5.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (total > 1) {
                    LinearProgressIndicator(
                        progress = { progress },
                        color = scheme.tertiary,
                        trackColor = scheme.outlineVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                Button(
                    onClick = {
                        if (remainingCount > 0) {
                            val willComplete = remainingCount == 1
                            SoundHelper.playTapSound(enabled = soundEnabled, isCompletion = willComplete)
                            if (vibrationEnabled) {
                                triggerLightHaptic(context)
                            }
                            onDecrement()
                        }
                    },
                    enabled = !isDone,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = scheme.primary,
                        contentColor = scheme.onPrimary,
                        disabledContainerColor = scheme.primaryContainer,
                        disabledContentColor = scheme.onPrimaryContainer
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    if (isDone) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(text = "تم", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                    } else {
                        Text(
                            text = if (isZikrMode) "سبّح  $remainingCount" else if (total > 1) "اقرأ  $remainingCount" else "تمت القراءة",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

private fun triggerLightHaptic(context: Context) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(25L, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(25L)
        }
    } catch (_: Exception) {
    }
}

