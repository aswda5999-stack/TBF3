@file:OptIn(ExperimentalFoundationApi::class)

package com.athar.muslim.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.athar.muslim.data.KingFahadQuranRepository
import com.athar.muslim.data.PageAyah
import com.athar.muslim.data.QuranPageRepository
import com.athar.muslim.model.QuranSurahMeta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val PREFS_NAME = "athar_muslim_prefs"
private const val KEY_LAST_PAGE = "mushaf_last_page"
private const val KEY_NIGHT = "mushaf_night"
private const val KEY_FONT_SP = "mushaf_font_sp"
private const val BASMALA = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ"

private sealed interface PageBlock {
    data class Header(val surahNumber: Int) : PageBlock
    data object Basmala : PageBlock
    data class Body(val text: AnnotatedString) : PageBlock
}

private fun toArabicDigits(number: Int): String {
    val builder = StringBuilder()
    number.toString().forEach { ch ->
        builder.append(if (ch in '0'..'9') ('\u0660' + (ch - '0')) else ch)
    }
    return builder.toString()
}

private fun buildBlocks(ayahs: List<PageAyah>, accent: Color): List<PageBlock> {
    val blocks = mutableListOf<PageBlock>()
    var builder: AnnotatedString.Builder? = null

    fun flush() {
        val current = builder
        if (current != null) {
            blocks.add(PageBlock.Body(current.toAnnotatedString()))
            builder = null
        }
    }

    for (ayah in ayahs) {
        if (ayah.numberInSurah == 1) {
            flush()
            blocks.add(PageBlock.Header(ayah.surahNumber))
            if (ayah.surahNumber != 1 && ayah.surahNumber != 9) {
                blocks.add(PageBlock.Basmala)
            }
        }
        val target = builder ?: AnnotatedString.Builder().also { builder = it }
        target.append(ayah.text)
        target.append(" ")
        target.withStyle(SpanStyle(color = accent)) {
            append("\u06DD" + toArabicDigits(ayah.numberInSurah))
        }
        target.append(" ")
    }
    flush()
    return blocks
}

@Composable
fun MushafReaderScreen(
    soundEnabled: Boolean,
    hapticEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var openPage by remember { mutableIntStateOf(0) }

    if (openPage <= 0) {
        MushafIndex(
            lastPage = prefs.getInt(KEY_LAST_PAGE, 0),
            onOpenPage = { page -> openPage = page.coerceIn(1, QuranPageRepository.PAGE_COUNT) },
            modifier = modifier
        )
    } else {
        MushafReader(
            startPage = openPage,
            onClose = { openPage = 0 },
            modifier = modifier
        )
    }
}

@Composable
private fun MushafIndex(
    lastPage: Int,
    onOpenPage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val surahs = remember { KingFahadQuranRepository.getFullSurahIndex() }
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) {
        val q = query.trim()
        if (q.isBlank()) surahs else surahs.filter { it.name.contains(q) || it.number.toString() == q }
    }

    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            singleLine = true,
            placeholder = {
                Text(text = "ابحث باسم السورة أو رقمها...", color = scheme.onSurfaceVariant, fontSize = 12.sp)
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 18.dp)
        ) {
            if (lastPage > 0 && query.isBlank()) {
                item(key = "continue") {
                    Button(
                        onClick = { onOpenPage(lastPage) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = scheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Text(
                            text = "متابعة القراءة من صفحة $lastPage",
                            color = scheme.onPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            items(filtered, key = { it.number }) { surah ->
                SurahRow(surah = surah, onClick = { onOpenPage(QuranPageRepository.startPageOf(surah.number)) })
            }
        }
    }
}

@Composable
private fun SurahRow(surah: QuranSurahMeta, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        border = BorderStroke(1.dp, scheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
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
                    color = scheme.primaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = surah.number.toString(),
                            color = scheme.onPrimaryContainer,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }
                Text(
                    text = surah.name,
                    color = scheme.onSurface,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Icon(
                imageVector = Icons.Rounded.ChevronLeft,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun MushafReader(
    startPage: Int,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val surahs = remember { KingFahadQuranRepository.getFullSurahIndex() }

    var nightMode by remember { mutableStateOf(prefs.getBoolean(KEY_NIGHT, false)) }
    var fontSizeSp by remember { mutableIntStateOf(prefs.getInt(KEY_FONT_SP, 26).coerceIn(18, 40)) }
    var showJump by remember { mutableStateOf(false) }
    var jumpText by remember { mutableStateOf("") }
    var fontFamily by remember { mutableStateOf<FontFamily?>(null) }
    val pageSurah = remember { mutableStateMapOf<Int, Int>() }

    val pagerState = rememberPagerState(
        initialPage = (startPage - 1).coerceIn(0, QuranPageRepository.PAGE_COUNT - 1),
        pageCount = { QuranPageRepository.PAGE_COUNT }
    )
    val currentPage = pagerState.currentPage + 1

    BackHandler { onClose() }

    LaunchedEffect(Unit) {
        val cached = QuranPageRepository.loadCachedTypeface(context)
        if (cached != null) {
            fontFamily = FontFamily(cached)
        } else {
            val downloaded = withContext(Dispatchers.IO) { QuranPageRepository.downloadFont(context) }
            if (downloaded != null) fontFamily = FontFamily(downloaded)
        }
    }

    LaunchedEffect(currentPage) {
        runCatching { prefs.edit().putInt(KEY_LAST_PAGE, currentPage).apply() }
        withContext(Dispatchers.IO) {
            runCatching { QuranPageRepository.loadPage(context, currentPage + 1) }
            runCatching { QuranPageRepository.loadPage(context, currentPage - 1) }
        }
    }

    val paper = if (nightMode) Color(0xFF0B1420) else Color(0xFFFBF6E9)
    val ink = if (nightMode) Color(0xFFE8E2D0) else Color(0xFF1A1A1A)
    val accent = if (nightMode) Color(0xFFD4AF37) else Color(0xFF8B6914)
    val activeFont = fontFamily ?: FontFamily.Serif

    val titleSurah = pageSurah[currentPage]?.let { number ->
        surahs.firstOrNull { it.number == number }?.name
    }

    Column(modifier = modifier.fillMaxSize()) {
        Surface(
            color = scheme.surface,
            border = BorderStroke(1.dp, scheme.outlineVariant),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "الفهرس", tint = scheme.primary)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = titleSurah ?: "المصحف الشريف",
                        color = scheme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                    Text(
                        text = "صفحة $currentPage من ${QuranPageRepository.PAGE_COUNT}",
                        color = scheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
                TextButton(onClick = { showJump = true }) {
                    Text("انتقال", color = scheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                TextButton(
                    onClick = {
                        fontSizeSp = (fontSizeSp - 2).coerceAtLeast(18)
                        runCatching { prefs.edit().putInt(KEY_FONT_SP, fontSizeSp).apply() }
                    }
                ) {
                    Text("أ-", color = scheme.primary, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                }
                TextButton(
                    onClick = {
                        fontSizeSp = (fontSizeSp + 2).coerceAtMost(40)
                        runCatching { prefs.edit().putInt(KEY_FONT_SP, fontSizeSp).apply() }
                    }
                ) {
                    Text("أ+", color = scheme.primary, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                }
                IconButton(
                    onClick = {
                        nightMode = !nightMode
                        runCatching { prefs.edit().putBoolean(KEY_NIGHT, nightMode).apply() }
                    }
                ) {
                    Icon(
                        imageVector = if (nightMode) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                        contentDescription = "الوضع الليلي",
                        tint = scheme.primary
                    )
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) { index ->
            MushafPage(
                pageNumber = index + 1,
                fontFamily = activeFont,
                fontSizeSp = fontSizeSp,
                paper = paper,
                ink = ink,
                accent = accent,
                surahs = surahs,
                onLoaded = { page, surah -> pageSurah[page] = surah }
            )
        }
    }

    if (showJump) {
        AlertDialog(
            onDismissRequest = { showJump = false },
            containerColor = scheme.surface,
            title = {
                Text(
                    text = "الانتقال إلى صفحة",
                    color = scheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            },
            text = {
                OutlinedTextField(
                    value = jumpText,
                    onValueChange = { value -> jumpText = value.filter { it.isDigit() }.take(3) },
                    singleLine = true,
                    placeholder = { Text("من 1 إلى ${QuranPageRepository.PAGE_COUNT}") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val target = jumpText.toIntOrNull()
                        showJump = false
                        jumpText = ""
                        if (target != null) {
                            val index = target.coerceIn(1, QuranPageRepository.PAGE_COUNT) - 1
                            scope.launch { pagerState.scrollToPage(index) }
                        }
                    }
                ) {
                    Text("انتقال", color = scheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showJump = false }) {
                    Text("إلغاء", color = scheme.onSurface)
                }
            }
        )
    }
}

@Composable
private fun MushafPage(
    pageNumber: Int,
    fontFamily: FontFamily,
    fontSizeSp: Int,
    paper: Color,
    ink: Color,
    accent: Color,
    surahs: List<QuranSurahMeta>,
    onLoaded: (Int, Int) -> Unit
) {
    val context = LocalContext.current
    var ayahs by remember(pageNumber) { mutableStateOf(QuranPageRepository.cached(pageNumber)) }
    var failed by remember(pageNumber) { mutableStateOf(false) }
    var retryTick by remember(pageNumber) { mutableIntStateOf(0) }

    LaunchedEffect(pageNumber, retryTick) {
        val existing = ayahs
        if (existing == null) {
            failed = false
            val loaded = withContext(Dispatchers.IO) {
                runCatching { QuranPageRepository.loadPage(context, pageNumber) }.getOrNull()
            }
            if (loaded == null) {
                failed = true
            } else {
                ayahs = loaded
                onLoaded(pageNumber, loaded.first().surahNumber)
            }
        } else {
            onLoaded(pageNumber, existing.first().surahNumber)
        }
    }

    val data = ayahs
    val blocks = remember(data, accent) { if (data == null) emptyList<PageBlock>() else buildBlocks(data, accent) }

    Surface(
        color = paper,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, accent.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxSize()
    ) {
        if (data == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (failed) {
                    Text(
                        text = "تعذّر تحميل الصفحة $pageNumber. تأكد من الإنترنت (يلزم مرة واحدة فقط لكل صفحة).",
                        color = ink,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { retryTick += 1 },
                        colors = ButtonDefaults.buttonColors(containerColor = accent)
                    ) {
                        Text("إعادة المحاولة", color = paper, fontWeight = FontWeight.Bold)
                    }
                } else {
                    CircularProgressIndicator(color = accent)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                blocks.forEach { block ->
                    when (block) {
                        is PageBlock.Header -> {
                            val name = surahs.firstOrNull { it.number == block.surahNumber }?.name.orEmpty()
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = accent.copy(alpha = 0.14f),
                                border = BorderStroke(1.dp, accent),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = name,
                                    color = ink,
                                    fontFamily = fontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (fontSizeSp - 2).sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                )
                            }
                        }

                        is PageBlock.Basmala -> {
                            Text(
                                text = BASMALA,
                                color = ink,
                                fontFamily = fontFamily,
                                fontSize = fontSizeSp.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        is PageBlock.Body -> {
                            Text(
                                text = block.text,
                                color = ink,
                                fontFamily = fontFamily,
                                fontSize = fontSizeSp.sp,
                                lineHeight = (fontSizeSp * 1.9f).sp,
                                textAlign = TextAlign.Justify,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                Text(
                    text = toArabicDigits(pageNumber),
                    color = accent,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                )
            }
        }
    }
}
