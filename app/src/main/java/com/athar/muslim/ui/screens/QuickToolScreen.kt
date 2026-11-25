package com.athar.muslim.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.athar.muslim.ui.components.SoundHelper

enum class QuickToolPage(val title: String) {
    TASBIH("المسبحة الإلكترونية"),
    QIBLA("بوصلة القبلة"),
    PRAYER_GUIDE("تعلم الصلاة"),
    MORNING_FOCUS("ورد اليوم")
}

@Composable
fun QuickToolScreen(
    page: QuickToolPage,
    soundEnabled: Boolean,
    onOpenMorningAzkar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    var tasbihCount by remember { mutableIntStateOf(0) }
    var targetGoal by remember { mutableIntStateOf(33) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 22.dp)
    ) {
        when (page) {
            QuickToolPage.TASBIH -> {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = scheme.surface),
                        border = BorderStroke(1.2.dp, scheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "سبحان الله وبحمده، سبحان الله العظيم",
                                color = scheme.onSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Surface(
                                shape = CircleShape,
                                color = scheme.primaryContainer,
                                border = BorderStroke(2.dp, scheme.tertiary),
                                modifier = Modifier.size(124.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "$tasbihCount",
                                            color = scheme.primary,
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = "الهدف: $targetGoal",
                                            color = scheme.onPrimaryContainer,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(33, 100, 1000).forEach { goal ->
                                    val selected = targetGoal == goal
                                    Surface(
                                        onClick = {
                                            SoundHelper.playTapSound(enabled = soundEnabled, isCompletion = false)
                                            targetGoal = goal
                                        },
                                        shape = RoundedCornerShape(50),
                                        color = if (selected) scheme.primary else scheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "$goal",
                                            color = if (selected) scheme.onPrimary else scheme.onPrimaryContainer,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        tasbihCount++
                                        val completed = tasbihCount % targetGoal == 0
                                        SoundHelper.playTapSound(enabled = soundEnabled, isCompletion = completed)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = scheme.primary),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(2f).height(48.dp)
                                ) {
                                    Icon(Icons.Rounded.Add, contentDescription = null, tint = scheme.onPrimary)
                                    Text("تسبيح", color = scheme.onPrimary, fontWeight = FontWeight.ExtraBold)
                                }

                                Button(
                                    onClick = {
                                        SoundHelper.playTapSound(enabled = soundEnabled, isCompletion = false)
                                        tasbihCount = 0
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = scheme.primaryContainer),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) {
                                    Icon(Icons.Rounded.Refresh, contentDescription = null, tint = scheme.onPrimaryContainer)
                                    Text("تصفير", color = scheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            QuickToolPage.QIBLA -> {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = scheme.surface),
                        border = BorderStroke(1.2.dp, scheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Rounded.Explore, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(44.dp))
                            Text("اتجاه القبلة نحو الكعبة المشرفة", color = scheme.onSurface, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                            Text(
                                text = "لأدق تحديد لاتجاه القبلة، يُنصح بتفعيل الموقع الجغرافي (GPS) والابتعاد عن المجالات المغناطيسية القوية.",
                                color = scheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                lineHeight = 21.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            QuickToolPage.PRAYER_GUIDE -> {
                val steps = listOf(
                    "النية واستقبال القبلة" to "استحضار نية الصلاة في القلب والوقوف باتجاه القبلة بخشوع وطمأنينة.",
                    "تكبيرة الإحرام والقيام" to "رفع اليدين حذو المنكبين أو الأذنين وقول (الله أكبر)، ثم قراءة الفاتحة وما تيسر من القرآن.",
                    "الركوع والاعتدال" to "الانحناء مع تسوية الظهر وقول (سبحان ربي العظيم)، ثم الرفع وقول (سمع الله لمن حمده، ربنا ولك الحمد).",
                    "السجود والتشهد والتسليم" to "السجود على الأعضاء السبعة وقول (سبحان ربي الأعلى)، ثم التشهد الأخير والصلاة الإبراهيمية والتسليم."
                )
                items(steps) { (title, desc) ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = scheme.surface),
                        border = BorderStroke(1.2.dp, scheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Rounded.Mosque, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(18.dp))
                                Text(title, color = scheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            Text(desc, color = scheme.onSurfaceVariant, fontSize = 12.5.sp, lineHeight = 20.sp)
                        }
                    }
                }
            }

            QuickToolPage.MORNING_FOCUS -> {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = scheme.surface),
                        border = BorderStroke(1.2.dp, scheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("ورد اليوم المختار", color = scheme.onSurface, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                            Text(
                                text = "ابدأ يومك بأذكار الصباح والسكينة، وتابع إتمام أعدادك اليومية خطوة بخطوة.",
                                color = scheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                lineHeight = 21.sp,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = {
                                    SoundHelper.playTapSound(enabled = soundEnabled, isCompletion = false)
                                    onOpenMorningAzkar()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = scheme.primary),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("فتح أذكار الصباح", color = scheme.onPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
