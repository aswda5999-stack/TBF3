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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    val textSizeSp = when (fontScaleStep) {
        0 -> 15.sp
        2 -> 21.sp
        else -> 18.sp
    }
    val lineHeightSp = when (fontScaleStep) {
        0 -> 25.sp
        2 -> 34.sp
        else -> 29.sp
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) scheme.primaryContainer.copy(alpha = 0.65f) else scheme.surface
        ),
        border = BorderStroke(
            width = if (isDone) 1.8.dp else 1.2.dp,
            color = if (isDone) scheme.tertiary else scheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // شريط علوي للبطاقة (نسخ، مشاركة، مفضلة، حالة الإتمام)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isDone) scheme.primary else scheme.primaryContainer,
                    border = BorderStroke(1.dp, scheme.outlineVariant)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        if (isDone) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = scheme.onPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = if (isDone) "تمت القراءة" else "المتبقي: $remainingCount",
                            color = if (isDone) scheme.onPrimary else scheme.onPrimaryContainer,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showFavoriteButton && onToggleFavorite != null) {
                        IconButton(onClick = onToggleFavorite, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = "مفضلة",
                                tint = if (isFavorite) scheme.tertiary else scheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("أثر مسلم", item.text))
                            Toast.makeText(context, "تم نسخ النص", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = "نسخ",
                            tint = scheme.primary,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "${item.text}\n\n— تطبيق أثر مسلم")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "مشاركة"))
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "مشاركة",
                            tint = scheme.primary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            // نص الذكر أو الدعاء أو الحديث
            Text(
                text = item.text,
                color = scheme.onSurface,
                fontSize = textSizeSp,
                lineHeight = lineHeightSp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            if (item.benefit.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = scheme.surfaceVariant.copy(alpha = 0.75f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.benefit,
                        color = scheme.onSurfaceVariant,
                        fontSize = 12.5.sp,
                        lineHeight = 19.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.5f))

            // زر العد التفاعلي
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
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = scheme.primary,
                    contentColor = scheme.onPrimary,
                    disabledContainerColor = scheme.primaryContainer,
                    disabledContentColor = scheme.onPrimaryContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Text(
                    text = if (isDone) "أحسنت • اكتمل العدد" else if (isZikrMode) "اضغط للتسبيح ($remainingCount)" else "تمت القراءة ($remainingCount)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
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

