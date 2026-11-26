package com.athar.muslim.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.athar.muslim.data.KingFahadQuranRepository
import com.athar.muslim.model.QuranAyah
import com.athar.muslim.model.QuranSurahMeta
import com.athar.muslim.ui.components.SoundHelper
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuranReaderScreen(
    soundEnabled: Boolean,
    hapticEnabled: Boolean,
    favoriteIds: Set<Int>,
    onToggleFavoriteAyah: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val scheme = MaterialTheme.colorScheme

    val quranNightModeState = remember { mutableStateOf(false) }
    val drawerOpenState = remember { mutableStateOf(false) }
    val scrollSpeedState = remember { mutableFloatStateOf(0f) }
    val activeSurahNumberState = remember { mutableIntStateOf(0) }
    val targetAyahNumberState = remember { mutableIntStateOf(1) }
    val selectedMenuAyahState = remember { mutableStateOf<QuranAyah?>(null) }
    val tafsirDialogAyahState = remember { mutableStateOf<QuranAyah?>(null) }
    val underDevDialogState = remember { mutableStateOf(false) }
    val actionToastMessageState = remember { mutableStateOf<String?>(null) }

    val isNightMode = quranNightModeState.value
    val isDrawerOpen = drawerOpenState.value
    val scrollSpeed = scrollSpeedState.floatValue
    val activeSurahNumber = activeSurahNumberState.intValue
    val targetAyahNumber = targetAyahNumberState.intValue
    val selectedMenuAyah = selectedMenuAyahState.value
    val tafsirDialogAyah = tafsirDialogAyahState.value
    val isUnderDev = underDevDialogState.value
    val toastMessage = actionToastMessageState.value

    val quranFontFamily = remember {
        try {
            val resId = context.resources.getIdentifier("quran_font", "font", context.packageName)
            if (resId != 0) {
                FontFamily(Font(resId))
            } else {
                FontFamily.Serif
            }
        } catch (_: Exception) {
            FontFamily.Serif
        }
    }

    val surahIndex = remember { KingFahadQuranRepository.getFullSurahIndex() }
    val surahAyahs = remember(activeSurahNumber) {
        if (activeSurahNumber > 0) {
            KingFahadQuranRepository.getAyahsForSurah(activeSurahNumber)
        } else {
            emptyList()
        }
    }
    val listState = rememberLazyListState()

    val pageBg = if (isNightMode) Color(0xFF0B1420) else scheme.background
    val cardBg = if (isNightMode) Color(0xFF132235) else scheme.surface
    val primaryText = if (isNightMode) Color(0xFFF1F6FC) else scheme.onSurface
    val secondaryText = if (isNightMode) Color(0xFF9FB3C8) else scheme.onSurfaceVariant
    val borderCol = if (isNightMode) Color(0xFF233A56) else scheme.outlineVariant
    val accentCol = if (isNightMode) Color(0xFF64B5F6) else scheme.primary
    val badgeBg = if (isNightMode) Color(0xFF1B314C) else scheme.primaryContainer

    LaunchedEffect(activeSurahNumber, targetAyahNumber, surahAyahs.size) {
        if (activeSurahNumber > 0 && surahAyahs.isNotEmpty()) {
            val idx = (targetAyahNumber - 1).coerceIn(0, surahAyahs.lastIndex)
            listState.scrollToItem(idx)
        }
    }

    LaunchedEffect(scrollSpeed, isDrawerOpen, tafsirDialogAyah, selectedMenuAyah, activeSurahNumber) {
        if (
            activeSurahNumber <= 0 ||
            scrollSpeed <= 0.1f ||
            isDrawerOpen ||
            tafsirDialogAyah != null ||
            selectedMenuAyah != null
        ) {
            return@LaunchedEffect
        }
        while (true) {
            val stepDelayMs = (95L - (scrollSpeed * 7.5f).toLong()).coerceIn(18L, 95L)
            val stepPx = 1.5f + scrollSpeed * 0.65f
            delay(stepDelayMs)
            listState.dispatchRawDelta(stepPx)
        }
    }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(1800L)
            actionToastMessageState.value = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(pageBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                color = cardBg,
                shadowElevation = 3.dp,
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                SoundHelper.playSystemTouchSound(context, soundEnabled, view, hapticEnabled)
                                quranNightModeState.value = !isNightMode
                            }
                        ) {
                            Icon(
                                imageVector = if (isNightMode) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                                contentDescription = "الوضع الليلي",
                                tint = accentCol,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        if (activeSurahNumber > 0) {
                            IconButton(
                                onClick = {
                                    SoundHelper.playSystemTouchSound(context, soundEnabled, view, hapticEnabled)
                                    scrollSpeedState.value = 0f
                                    activeSurahNumberState.value = 0
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = "الفهرس",
                                    tint = accentCol,
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val currentSurahTitle = surahIndex.firstOrNull { it.number == activeSurahNumber }?.name
                            ?: "فهرس القرآن الكريم"
                        Text(
                            text = currentSurahTitle,
                            color = primaryText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (activeSurahNumber <= 0) {
                                "114 سورة • اختر سورة أو آية لجلبها"
                            } else if (scrollSpeed > 0.1f) {
                                "تمرير تلقائي • سرعة ${scrollSpeed.toInt()}"
                            } else {
                                "تنقل بالسحب • اضغط مطولاً على آية"
                            },
                            color = secondaryText,
                            fontSize = 10.5.sp
                        )
                    }

                    IconButton(
                        onClick = {
                            SoundHelper.playSystemTouchSound(context, soundEnabled, view, hapticEnabled)
                            drawerOpenState.value = !isDrawerOpen
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Menu,
                            contentDescription = "شريط السرعة",
                            tint = accentCol,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            if (toastMessage != null) {
                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = toastMessage,
                        color = primaryText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }

            if (activeSurahNumber <= 0) {
                LazyColumn(
                    userScrollEnabled = true,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 22.dp)
                ) {
                    items(surahIndex, key = { it.number }) { surah ->
                        SurahIndexRowCard(
                            surah = surah,
                            cardBg = cardBg,
                            borderCol = borderCol,
                            primaryText = primaryText,
                            secondaryText = secondaryText,
                            accentCol = accentCol,
                            badgeBg = badgeBg,
                            onSelectSurah = { selectedSurahNumber, selectedAyahNum ->
                                SoundHelper.playZikrPressSound(
                                    enabled = soundEnabled,
                                    isCompletion = false,
                                    view = view,
                                    hapticEnabled = hapticEnabled
                                )
                                activeSurahNumberState.value = selectedSurahNumber
                                targetAyahNumberState.value = selectedAyahNum
                            }
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    userScrollEnabled = true,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 22.dp)
                ) {
                    items(surahAyahs, key = { it.id }) { ayah ->
                        val isFav = favoriteIds.contains(ayah.id)
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = BorderStroke(
                                width = if (isFav) 1.6.dp else 1.dp,
                                color = if (isFav) accentCol else borderCol
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        SoundHelper.playSystemTouchSound(context, soundEnabled, view, hapticEnabled)
                                    },
                                    onLongClick = {
                                        SoundHelper.playZikrPressSound(
                                            enabled = soundEnabled,
                                            isCompletion = false,
                                            view = view,
                                            hapticEnabled = hapticEnabled
                                        )
                                        selectedMenuAyahState.value = ayah
                                    }
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = ayah.surahName,
                                        color = accentCol,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        shape = CircleShape,
                                        color = badgeBg
                                    ) {
                                        Text(
                                            text = "${ayah.ayahNumber}",
                                            color = primaryText,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = ayah.arabicText,
                                    color = primaryText,
                                    fontFamily = quranFontFamily,
                                    fontSize = 22.sp,
                                    lineHeight = 38.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isDrawerOpen,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Surface(
                color = cardBg,
                shadowElevation = 10.dp,
                border = BorderStroke(1.dp, borderCol),
                shape = RoundedCornerShape(topStart = 22.dp, bottomStart = 22.dp),
                modifier = Modifier
                    .width(250.dp)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "التمرير التلقائي",
                            color = primaryText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        IconButton(
                            onClick = {
                                SoundHelper.playSystemTouchSound(context, soundEnabled, view, hapticEnabled)
                                drawerOpenState.value = false
                            }
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = "إغلاق", tint = accentCol)
                        }
                    }

                    HorizontalDivider(color = borderCol)

                    Text(
                        text = if (scrollSpeed <= 0.1f) {
                            "الوضع الحالي: متوقف (تنقل بالسحب اليدوي)"
                        } else {
                            "سرعة الرفع التلقائي: ${scrollSpeed.toInt()} / 10"
                        },
                        color = secondaryText,
                        fontSize = 12.sp,
                        lineHeight = 19.sp
                    )

                    Slider(
                        value = scrollSpeed,
                        onValueChange = { newValue ->
                            scrollSpeedState.value = newValue
                        },
                        valueRange = 0f..10f,
                        colors = SliderDefaults.colors(
                            thumbColor = accentCol,
                            activeTrackColor = accentCol,
                            inactiveTrackColor = badgeBg
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                SoundHelper.playSystemTouchSound(context, soundEnabled, view, hapticEnabled)
                                scrollSpeedState.value = 0f
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = badgeBg),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إيقاف", color = primaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                SoundHelper.playSystemTouchSound(context, soundEnabled, view, hapticEnabled)
                                drawerOpenState.value = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentCol),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("تطبيق", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (selectedMenuAyah != null) {
            val menuAyah = selectedMenuAyah
            val isAlreadyFav = favoriteIds.contains(menuAyah.id)

            AlertDialog(
                onDismissRequest = { selectedMenuAyahState.value = null },
                containerColor = cardBg,
                shape = RoundedCornerShape(22.dp),
                title = {
                    Text(
                        text = "${menuAyah.surahName} • آية ${menuAyah.ayahNumber}",
                        color = primaryText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = menuAyah.arabicText,
                            color = primaryText,
                            fontFamily = quranFontFamily,
                            fontSize = 18.sp,
                            lineHeight = 30.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        HorizontalDivider(color = borderCol)

                        val options = listOf(
                            "نسخ" to {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                clipboard?.setPrimaryClip(
                                    ClipData.newPlainText("ayah", "${menuAyah.arabicText} [${menuAyah.surahName}: ${menuAyah.ayahNumber}]")
                                )
                                actionToastMessageState.value = "تم نسخ الآية"
                                selectedMenuAyahState.value = null
                            },
                            "مشاركة" to {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "${menuAyah.arabicText}\n(${menuAyah.surahName} - آية ${menuAyah.ayahNumber})")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "مشاركة الآية"))
                                selectedMenuAyahState.value = null
                            },
                            (if (isAlreadyFav) "إزالة من المفضلة" else "مفضلة") to {
                                onToggleFavoriteAyah(menuAyah.id)
                                actionToastMessageState.value = if (isAlreadyFav) {
                                    "تمت الإزالة من المفضلة"
                                } else {
                                    "تمت الإضافة إلى المفضلة"
                                }
                                selectedMenuAyahState.value = null
                            },
                            "تفسير" to {
                                selectedMenuAyahState.value = null
                                tafsirDialogAyahState.value = menuAyah
                            },
                            "قراءة" to {
                                selectedMenuAyahState.value = null
                                underDevDialogState.value = true
                            }
                        )

                        options.forEach { (label, handler) ->
                            Surface(
                                onClick = {
                                    SoundHelper.playSystemTouchSound(context, soundEnabled, view, hapticEnabled)
                                    handler()
                                },
                                shape = RoundedCornerShape(14.dp),
                                color = badgeBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = label,
                                    color = primaryText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedMenuAyahState.value = null }) {
                        Text("إغلاق", color = accentCol, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        if (tafsirDialogAyah != null) {
            val currentTafsirAyah = tafsirDialogAyah
            Dialog(onDismissRequest = { tafsirDialogAyahState.value = null }) {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.2.dp, borderCol),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "التفسير الميسر • ${currentTafsirAyah.surahName} (${currentTafsirAyah.ayahNumber})",
                                color = accentCol,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            IconButton(
                                onClick = { tafsirDialogAyahState.value = null },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Rounded.Close, contentDescription = "إغلاق", tint = primaryText)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = badgeBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = currentTafsirAyah.arabicText,
                                color = primaryText,
                                fontFamily = quranFontFamily,
                                fontSize = 20.sp,
                                lineHeight = 34.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(14.dp)
                            )
                        }

                        Text(
                            text = currentTafsirAyah.tafsirMuyassar,
                            color = primaryText,
                            fontSize = 13.5.sp,
                            lineHeight = 23.sp,
                            textAlign = TextAlign.Start
                        )

                        Button(
                            onClick = { tafsirDialogAyahState.value = null },
                            colors = ButtonDefaults.buttonColors(containerColor = accentCol),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("حسناً", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (isUnderDev) {
            AlertDialog(
                onDismissRequest = { underDevDialogState.value = false },
                containerColor = cardBg,
                shape = RoundedCornerShape(22.dp),
                title = {
                    Text(
                        text = "قيد التطوير",
                        color = primaryText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                text = {
                    Text(
                        text = "ميزة القراءة الصوتية للآيات قيد التطوير حالياً.",
                        color = secondaryText,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { underDevDialogState.value = false },
                        colors = ButtonDefaults.buttonColors(containerColor = accentCol)
                    ) {
                        Text("موافق", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
private fun SurahIndexRowCard(
    surah: QuranSurahMeta,
    cardBg: Color,
    borderCol: Color,
    primaryText: Color,
    secondaryText: Color,
    accentCol: Color,
    badgeBg: Color,
    onSelectSurah: (Int, Int) -> Unit
) {
    Card(
        onClick = { onSelectSurah(surah.number, 1) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderCol),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = badgeBg,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${surah.number}",
                            color = accentCol,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = surah.name,
                        color = primaryText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${surah.revelationType} • ${surah.ayahCount} آية",
                        color = secondaryText,
                        fontSize = 11.sp
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = badgeBg
            ) {
                Text(
                    text = "فتح",
                    color = accentCol,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }
    }
}
