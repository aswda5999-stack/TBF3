package com.athar.muslim.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.athar.muslim.audio.VoiceAzkarPlayer
import com.athar.muslim.model.VoiceReminderIntensity
import com.athar.muslim.ui.components.SoundHelper

enum class QuickToolPage(val title: String) {
    QURAN("القرآن الكريم"),
    AUDIO("الصوتيات"),
    TASBIH("المسبحة الإلكترونية"),
    QIBLA("بوصلة القبلة"),
    PRAYER_GUIDE("تعلم الصلاة"),
    MORNING_FOCUS("ورد اليوم")
}

private data class RichGuideEntry(
    val heading: String,
    val detail: String,
    val tip: String
)

@Composable
fun QuickToolScreen(
    page: QuickToolPage,
    soundEnabled: Boolean,
    hapticEnabled: Boolean = true,
    favoriteQuranIds: Set<Int> = emptySet(),
    onToggleFavoriteQuranAyah: (Int) -> Unit = {},
    onOpenMorningAzkar: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (page == QuickToolPage.QURAN) {
        QuranReaderScreen(
            soundEnabled = soundEnabled,
            hapticEnabled = hapticEnabled,
            favoriteIds = favoriteQuranIds,
            onToggleFavoriteAyah = onToggleFavoriteQuranAyah,
            modifier = modifier
        )
        return
    }

    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val view = LocalView.current
    val tasbihCountState = remember { mutableIntStateOf(0) }
    val targetGoalState = remember { mutableIntStateOf(33) }
    val tasbihCount = tasbihCountState.intValue
    val targetGoal = targetGoalState.intValue

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        when (page) {
            QuickToolPage.QURAN -> Unit

            QuickToolPage.AUDIO -> {
                val voicesCount = VoiceAzkarPlayer.discoverRawVoiceResIds(context).size
                val audioGuides = listOf(
                    RichGuideEntry(
                        heading = "إضافة الأصوات إلى المشروع",
                        detail = "ضع ملفات الصوت داخل app/src/main/res/raw بأسماء إنجليزية صغيرة مثل azkar_voice_01.mp3 حتى azkar_voice_20.mp3 أو أكثر.",
                        tip = "كلما أضفت ملفاً جديداً بنفس البادئة يلتقطه التطبيق تلقائياً دون تعديل في الشيفرة."
                    ),
                    RichGuideEntry(
                        heading = "مستويات شدة التذكير الصوتي",
                        detail = "منخفض يراعي الأجواء الهادئة، ومتوسط يوازن بين الوضوح والاستمرارية، ومرتفع يمنح تنبيهاً أوضح وأقرب.",
                        tip = "يمكنك التجربة الفورية من زر الاستماع أدناه بعد إضافة ملفاتك الصوتية."
                    )
                )
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
                            Icon(Icons.Rounded.GraphicEq, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(40.dp))
                            Text("الصوتيات والتذكير الصوتي", color = scheme.onSurface, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                            Text(
                                text = "عدد الملفات الصوتية المكتشفة حالياً في الموارد: $voicesCount صوت.",
                                color = scheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        SoundHelper.playSystemTouchSound(context, soundEnabled, view, hapticEnabled)
                                        VoiceAzkarPlayer.playNextVoiceReminder(context, VoiceReminderIntensity.MEDIUM)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = scheme.primary),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = scheme.onPrimary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("تجربة صوت", color = scheme.onPrimary, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = {
                                        SoundHelper.playSystemTouchSound(context, soundEnabled, view, hapticEnabled)
                                        VoiceAzkarPlayer.stopCurrent()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = scheme.primaryContainer),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Rounded.Stop, contentDescription = null, tint = scheme.onPrimaryContainer)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("إيقاف", color = scheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                items(audioGuides) { entry ->
                    RichInfoCard(entry = entry)
                }
            }

            QuickToolPage.TASBIH -> {
                val tasbihSuggestions = listOf(
                    RichGuideEntry(
                        heading = "تنويع صيغ التسبيح والتحميد والتهليل",
                        detail = "الجمع بين التسبيح والتحميد والتكبير والتهليل والاستغفار يمنح القلب حضوراً وحيوية في الذكر.",
                        tip = "اختر هدف 33 أو 100 أو 1000 بحسب وقتك وطاقتك مع نغمة إتمام مميزة عند بلوغ الهدف."
                    ),
                    RichGuideEntry(
                        heading = "استثمار أوقات الانتظار",
                        detail = "استغل دقائق الانتظار أو المواصلات في عدّاد المسبحة الإلكترونية لتتحول الأوقات العابرة إلى رصيد يومي ثابت.",
                        tip = "اضغط على زر التصفير عند بدء جولة جديدة من الذكر."
                    )
                )
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
                                text = "المسبحة الإلكترونية التفاعلية",
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
                                            SoundHelper.playSystemTouchSound(context, soundEnabled, view, hapticEnabled)
                                            targetGoalState.intValue = goal
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
                                        val nextCount = tasbihCount + 1
                                        tasbihCountState.intValue = nextCount
                                        val completed = nextCount % targetGoal == 0
                                        SoundHelper.playZikrPressSound(
                                            enabled = soundEnabled,
                                            isCompletion = completed,
                                            view = view,
                                            hapticEnabled = hapticEnabled
                                        )
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
                                        SoundHelper.playSystemTouchSound(context, soundEnabled, view, hapticEnabled)
                                        tasbihCountState.intValue = 0
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
                items(tasbihSuggestions) { entry ->
                    RichInfoCard(entry = entry)
                }
            }

            QuickToolPage.QIBLA -> {
                val qiblaNotes = listOf(
                    RichGuideEntry(
                        heading = "تفعيل الموقع بدقة (GPS)",
                        detail = "يتطلب تحديد الاتجاه الصحيح تفعيل خدمة الموقع الجغرافي ومنح إذن الموقع الدقيق للتطبيق.",
                        tip = "في الأماكن المغلقة أو قرب الأجهزة المعدنية والمغناطيسية قد تتأثر قراءة البوصلة."
                    ),
                    RichGuideEntry(
                        heading = "معايرة مستشعر الاتجاه",
                        detail = "حرك الهاتف حركة هادئة على شكل رقم ثمانية أفقي إذا لاحظت انحرافاً في المؤشر قبل اعتماد الاتجاه.",
                        tip = "تأكد من إزالة الأغطية المغناطيسية السميكة عن الهاتف أثناء التحقق من القبلة."
                    )
                )
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
                            Icon(Icons.Rounded.Explore, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(42.dp))
                            Text("بوصلة القبلة نحو الكعبة المشرفة", color = scheme.onSurface, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                            Text(
                                text = "إرشادات دقيقة لاستقبال القبلة بثقة وطمأنينة في السفر والإقامة.",
                                color = scheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                lineHeight = 21.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                items(qiblaNotes) { entry ->
                    RichInfoCard(entry = entry)
                }
            }

            QuickToolPage.PRAYER_GUIDE -> {
                val steps = listOf(
                    RichGuideEntry(
                        heading = "الاستعداد والطهارة واستقبال القبلة",
                        detail = "إتمام الوضوء بإسباغ، واستحضار نية الصلاة في القلب، والتوجه نحو القبلة بخشوع وطمأنينة.",
                        tip = "الخشوع يبدأ من لحظة الاستعداد وتهيئة القلب قبل تكبيرة الإحرام."
                    ),
                    RichGuideEntry(
                        heading = "تكبيرة الإحرام والقيام والقراءة",
                        detail = "افتتاح الصلاة بتكبيرة الإحرام مع رفع اليدين، ثم قراءة الفاتحة وما تيسر من القرآن بتأنٍ وتدبر.",
                        tip = "التأنّي في القراءة يساعد على استحضار المعنى وتهدئة النفس."
                    ),
                    RichGuideEntry(
                        heading = "الركوع والاعتدال والسجود",
                        detail = "إتمام الركوع مع اطمئنان الظهر، ثم الاعتدال، ثم السجود على الأعضاء السبعة وإطالة الدعاء في السجود.",
                        tip = "حافظ على الطمأنينة في كل ركن وانتقال ولا تستعجل الحركات."
                    ),
                    RichGuideEntry(
                        heading = "التشهد الأخير والتسليم وأذكار ما بعد الصلاة",
                        detail = "إتمام التشهد والصلاة الإبراهيمية ثم التسليم، يلي ذلك الأذكار المشروعة عقب الصلوات المكتوبة.",
                        tip = "ابقَ لحظات بعد السلام لإتمام أذكار دبر الصلاة قبل الانصراف."
                    )
                )
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = scheme.surface),
                        border = BorderStroke(1.2.dp, scheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Rounded.Mosque, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(34.dp))
                            Text("دليل الصلاة والخشوع", color = scheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                            Text(
                                text = "خطوات منظمة ومبسطة لتعلّم أركان الصلاة وهيئاتها والخشوع فيها.",
                                color = scheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                items(steps) { entry ->
                    RichInfoCard(entry = entry)
                }
            }

            QuickToolPage.MORNING_FOCUS -> {
                val focusItems = listOf(
                    RichGuideEntry(
                        heading = "تنظيم بداية اليوم على ذكر الله",
                        detail = "البدء بأذكار الصباح والسكينة يمنح اليوم بركة وتنظيماً وراحة نفسية في مواجهة المهام.",
                        tip = "اجعل قراءة أذكار الصباح أول عادة بعد صلاة الفجر أو الاستيقاظ."
                    ),
                    RichGuideEntry(
                        heading = "متابعة الأعداد اليومية",
                        detail = "تابع إتمام أعداد الأذكار والأدعية خطوة بخطوة، واستعن بالتذكير الصوتي عند الحاجة.",
                        tip = "يمكنك الانتقال مباشرة إلى أذكار الصباح من الزر أدناه."
                    )
                )
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
                                    SoundHelper.playZikrPressSound(
                                        enabled = soundEnabled,
                                        isCompletion = false,
                                        view = view,
                                        hapticEnabled = hapticEnabled
                                    )
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
                items(focusItems) { entry ->
                    RichInfoCard(entry = entry)
                }
            }
        }
    }
}

@Composable
private fun RichInfoCard(entry: RichGuideEntry) {
    val scheme = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        border = BorderStroke(1.1.dp, scheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = entry.heading,
                color = scheme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = entry.detail,
                color = scheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 19.sp
            )
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = scheme.primaryContainer.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "فائدة: ${entry.tip}",
                    color = scheme.onPrimaryContainer,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                )
            }
        }
    }
}
