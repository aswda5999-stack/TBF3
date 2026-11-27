@file:OptIn(ExperimentalFoundationApi::class)

package com.athar.muslim.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.athar.muslim.audio.AyahAudioPlayer
import com.athar.muslim.audio.QuranReciters
import com.athar.muslim.data.KingFahadQuranRepository
import com.athar.muslim.data.PageAyah
import com.athar.muslim.data.QuranPageRepository
import com.athar.muslim.data.TajweedRun
import com.athar.muslim.model.QuranSurahMeta
import com.athar.muslim.model.ZikrItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val PREFS_NAME = "athar_muslim_prefs"
private const val KEY_LAST_PAGE = "mushaf_last_page"
private const val KEY_NIGHT = "mushaf_night"
private const val KEY_FONT_SP = "mushaf_font_sp"
private const val KEY_TAJWEED = "mushaf_tajweed"
private const val KEY_LIST_MODE = "mushaf_list_mode"
private const val KEY_RECITER = "mushaf_reciter"
private const val KEY_SCROLL_SPEED = "mushaf_scroll_speed"
private const val BASMALA = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ"
private const val FAVORITE_ID_BASE = 90_000_000
private const val AYAH_TAG = "ayah"

private sealed interface PageBlock {
    data class Header(val surahNumber: Int) : PageBlock
    data object Basmala : PageBlock
    data class Body(val text: AnnotatedString) : PageBlock
    data class Single(val key: Int, val text: AnnotatedString) : PageBlock
}

private fun toArabicDigits(number: Int): String {
    val builder = StringBuilder()
    number.toString().forEach { ch ->
        builder.append(if (ch in '0'..'9') ('\u0660' + (ch - '0')) else ch)
    }
    return builder.toString()
}

private fun tajweedColor(code: Char, night: Boolean): Color? = when (code) {
    'h', 's', 'l' -> Color(0xFFAAAAAA)
    'n' -> if (night) Color(0xFF7FA0FF) else Color(0xFF537FFF)
    'p' -> if (night) Color(0xFF8C97FF) else Color(0xFF4050FF)
    'm' -> if (night) Color(0xFF6F7CFF) else Color(0xFF000EBC)
    'o' -> if (night) Color(0xFF6D8DFF) else Color(0xFF2144C1)
    'q' -> Color(0xFFDD0008)
    'c' -> Color(0xFFD500B7)
    'f' -> Color(0xFF9400A8)
    'w' -> Color(0xFF58B800)
    'i' -> Color(0xFF26BFFD)
    'a', 'u' -> Color(0xFF169200)
    'd', 'b' -> Color(0xFFA1A1A1)
    'g' -> Color(0xFFFF7E1E)
    else -> null
}

private fun AnnotatedString.Builder.appendAyah(
    ayah: PageAyah,
    runs: List<TajweedRun>?,
    night: Boolean,
    accent: Color,
    highlight: Color?
) {
    val start = length
    if (runs != null) {
        runs.forEach { run ->
            val color = run.code?.let { tajweedColor(it, night) }
            if (color != null) {
                withStyle(SpanStyle(color = color)) { append(run.text) }
            } else {
                append(run.text)
            }
        }
    } else {
        append(ayah.text)
    }
    append(" ")
    withStyle(SpanStyle(color = accent)) {
        append("\u06DD" + toArabicDigits(ayah.numberInSurah))
    }
    val end = length
    addStringAnnotation(AYAH_TAG, ayah.key.toString(), start, end)
    if (highlight != null) {
        addStyle(SpanStyle(background = highlight), start, end)
    }
    append(" ")
}

private fun buildBlocks(
    ayahs: List<PageAyah>,
    tajweed: Map<Int, List<TajweedRun>>?,
    night: Boolean,
    accent: Color,
    listMode: Boolean,
    highlightKeys: Set<Int>,
    highlight: Color
): List<PageBlock> {
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
        val runs = tajweed?.get(ayah.key)
        val mark = if (highlightKeys.contains(ayah.key)) highlight else null
        if (listMode) {
            flush()
            val text = buildAnnotatedString { appendAyah(ayah, runs, night, accent, mark) }
            blocks.add(PageBlock.Single(ayah.key, text))
        } else {
            val target = builder ?: AnnotatedString.Builder().also { builder = it }
            target.appendAyah(ayah, runs, night, accent, mark)
        }
    }
    flush()
    return blocks
}

private fun copyToClipboard(context: Context, text: String) {
    runCatching {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("أثر مسلم", text))
        Toast.makeText(context, "تم النسخ", Toast.LENGTH_SHORT).show()
    }
}

private fun shareText(context: Context, text: String) {
    runCatching {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text + "\n\n— تطبيق أثر مسلم")
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة"))
    }
}

// ============================ الشاشة الرئيسية ============================

@Composable
fun MushafReaderScreen(
    soundEnabled: Boolean,
    hapticEnabled: Boolean,
    favoriteIds: Set<Int> = emptySet(),
    onToggleFavorite: (ZikrItem) -> Unit = {},
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
            favoriteIds = favoriteIds,
            onToggleFavorite = onToggleFavorite,
            onClose = { openPage = 0 },
            modifier = modifier
        )
    }
}

// ============================ الفهرس (أسماء السور فقط) ============================

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

// ============================ القارئ ============================

@Composable
private fun MushafReader(
    startPage: Int,
    favoriteIds: Set<Int>,
    onToggleFavorite: (ZikrItem) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val surahs = remember { KingFahadQuranRepository.getFullSurahIndex() }

    var nightMode by remember { mutableStateOf(prefs.getBoolean(KEY_NIGHT, false)) }
    var fontSizeSp by remember { mutableIntStateOf(prefs.getInt(KEY_FONT_SP, 26).coerceIn(12, 60)) }
    var tajweedOn by remember { mutableStateOf(prefs.getBoolean(KEY_TAJWEED, true)) }
    var listMode by remember { mutableStateOf(prefs.getBoolean(KEY_LIST_MODE, false)) }
    var reciterFolder by remember {
        mutableStateOf(QuranReciters.byFolder(prefs.getString(KEY_RECITER, null)).folder)
    }
    var autoScroll by remember { mutableStateOf(false) }
    var scrollSpeed by remember { mutableIntStateOf(prefs.getInt(KEY_SCROLL_SPEED, 3).coerceIn(1, 10)) }

    var showSettings by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<PageAyah?>(null) }
    var tafsirTarget by remember { mutableStateOf<PageAyah?>(null) }
    var playingKey by remember { mutableStateOf<Int?>(null) }
    var fontFamily by remember { mutableStateOf<FontFamily?>(null) }
    val pageSurah = remember { mutableStateMapOf<Int, Int>() }
    val pageData = remember { mutableStateMapOf<Int, List<PageAyah>>() }
    val jobHolder = remember { arrayOfNulls<Job>(1) }

    val pagerState = rememberPagerState(
        initialPage = (startPage - 1).coerceIn(0, QuranPageRepository.PAGE_COUNT - 1),
        pageCount = { QuranPageRepository.PAGE_COUNT }
    )
    val currentPage = pagerState.currentPage + 1

    fun stopPlayback() {
        jobHolder[0]?.cancel()
        jobHolder[0] = null
        AyahAudioPlayer.stop()
        playingKey = null
    }

    fun startPlayback(fromPage: Int, fromAyah: PageAyah) {
        stopPlayback()
        val folder = reciterFolder
        jobHolder[0] = scope.launch {
            var page = fromPage
            var firstKey: Int? = fromAyah.key
            var failed = false
            while (isActive && page <= QuranPageRepository.PAGE_COUNT && !failed) {
                val ayahs = withContext(Dispatchers.IO) {
                    runCatching { QuranPageRepository.loadPage(context, page) }.getOrNull()
                }
                if (ayahs == null) {
                    failed = true
                } else {
                    var started = firstKey == null
                    for (ayah in ayahs) {
                        if (!started) {
                            if (ayah.key == firstKey) started = true else continue
                        }
                        playingKey = ayah.key
                        val ok = AyahAudioPlayer.play(context, folder, ayah.surahNumber, ayah.numberInSurah)
                        if (!ok) {
                            failed = true
                            break
                        }
                    }
                    firstKey = null
                    if (!failed) {
                        page += 1
                        if (page <= QuranPageRepository.PAGE_COUNT) {
                            pagerState.animateScrollToPage(page - 1)
                        }
                    }
                }
            }
            playingKey = null
            if (failed) {
                Toast.makeText(context, "تعذّر تشغيل التلاوة. تأكد من الإنترنت (يلزم مرة واحدة لكل آية).", Toast.LENGTH_LONG).show()
            }
        }
    }

    BackHandler {
        stopPlayback()
        onClose()
    }

    DisposableEffect(Unit) {
        onDispose {
            jobHolder[0]?.cancel()
            AyahAudioPlayer.stop()
        }
    }

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
    val highlightKeys = setOfNotNull(playingKey, selected?.key, tafsirTarget?.key)

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // الشريط العلوي: زران فقط (القائمة + الوضع الليلي)
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
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Rounded.Menu, contentDescription = "الإعدادات", tint = scheme.primary)
                    }
                    Text(
                        text = "الصفحة $currentPage",
                        color = scheme.onSurface,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
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
                    isCurrent = pagerState.currentPage == index,
                    fontFamily = activeFont,
                    fontSizeSp = fontSizeSp,
                    paper = paper,
                    ink = ink,
                    accent = accent,
                    night = nightMode,
                    tajweedOn = tajweedOn,
                    listMode = listMode,
                    highlightKeys = highlightKeys,
                    autoScroll = autoScroll,
                    scrollSpeed = scrollSpeed,
                    surahs = surahs,
                    onLoaded = { page, ayahs ->
                        pageSurah[page] = ayahs.first().surahNumber
                        pageData[page] = ayahs
                    },
                    onAyahTap = { ayah -> selected = ayah },
                    onAutoScrollEnd = {
                        val next = pagerState.currentPage + 1
                        if (next < QuranPageRepository.PAGE_COUNT) {
                            scope.launch { pagerState.animateScrollToPage(next) }
                        } else {
                            autoScroll = false
                        }
                    }
                )
            }
        }

        if (playingKey != null) {
            Surface(
                color = scheme.primary,
                shape = RoundedCornerShape(26.dp),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 18.dp, end = 6.dp)
                ) {
                    Text(
                        text = "جارٍ التلاوة • " + QuranReciters.byFolder(reciterFolder).name,
                        color = scheme.onPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { stopPlayback() }) {
                        Icon(Icons.Rounded.Stop, contentDescription = "إيقاف", tint = scheme.onPrimary)
                    }
                }
            }
        }
    }

    val actionAyah = selected
    if (actionAyah != null) {
        val surahName = surahs.firstOrNull { it.number == actionAyah.surahNumber }?.name.orEmpty()
        val favoriteId = FAVORITE_ID_BASE + actionAyah.key
        val shareLine = actionAyah.text + " ﴿" + toArabicDigits(actionAyah.numberInSurah) + "﴾ [" + surahName + "]"
        AyahActionsDialog(
            ayah = actionAyah,
            surahName = surahName,
            fontFamily = activeFont,
            isFavorite = favoriteIds.contains(favoriteId),
            onDismiss = { selected = null },
            onCopy = {
                copyToClipboard(context, shareLine)
                selected = null
            },
            onShare = {
                shareText(context, shareLine)
                selected = null
            },
            onFavorite = {
                onToggleFavorite(
                    ZikrItem(
                        id = favoriteId,
                        title = surahName + " • آية " + actionAyah.numberInSurah,
                        text = actionAyah.text,
                        count = 1,
                        benefit = "",
                        category = "القرآن الكريم"
                    )
                )
            },
            onTafsir = {
                tafsirTarget = actionAyah
                selected = null
            },
            onPlay = {
                val ayahPage = pageData.entries.firstOrNull { entry ->
                    entry.value.any { it.key == actionAyah.key }
                }?.key ?: currentPage
                selected = null
                startPlayback(ayahPage, actionAyah)
            }
        )
    }

    val tafsirAyah = tafsirTarget
    if (tafsirAyah != null) {
        val ayahPage = pageData.entries.firstOrNull { entry ->
            entry.value.any { it.key == tafsirAyah.key }
        }?.key ?: currentPage
        TafsirDialog(
            ayah = tafsirAyah,
            page = ayahPage,
            fontFamily = activeFont,
            onShare = { text -> shareText(context, text) },
            onDismiss = { tafsirTarget = null }
        )
    }

    if (showSettings) {
        ReaderSettingsDialog(
            listMode = listMode,
            onListMode = { value ->
                listMode = value
                runCatching { prefs.edit().putBoolean(KEY_LIST_MODE, value).apply() }
            },
            fontSizeSp = fontSizeSp,
            onFontSize = { value ->
                fontSizeSp = value
                runCatching { prefs.edit().putInt(KEY_FONT_SP, value).apply() }
            },
            tajweedOn = tajweedOn,
            onTajweed = { value ->
                tajweedOn = value
                runCatching { prefs.edit().putBoolean(KEY_TAJWEED, value).apply() }
            },
            reciterFolder = reciterFolder,
            onReciter = { folder ->
                reciterFolder = folder
                runCatching { prefs.edit().putString(KEY_RECITER, folder).apply() }
            },
            autoScroll = autoScroll,
            onAutoScroll = { value -> autoScroll = value },
            scrollSpeed = scrollSpeed,
            onScrollSpeed = { value ->
                scrollSpeed = value
                runCatching { prefs.edit().putInt(KEY_SCROLL_SPEED, value).apply() }
            },
            onJump = { page ->
                scope.launch {
                    pagerState.scrollToPage(page.coerceIn(1, QuranPageRepository.PAGE_COUNT) - 1)
                }
            },
            onDismiss = { showSettings = false }
        )
    }
}

// ============================ صفحة المصحف ============================

@Composable
private fun MushafPage(
    pageNumber: Int,
    isCurrent: Boolean,
    fontFamily: FontFamily,
    fontSizeSp: Int,
    paper: Color,
    ink: Color,
    accent: Color,
    night: Boolean,
    tajweedOn: Boolean,
    listMode: Boolean,
    highlightKeys: Set<Int>,
    autoScroll: Boolean,
    scrollSpeed: Int,
    surahs: List<QuranSurahMeta>,
    onLoaded: (Int, List<PageAyah>) -> Unit,
    onAyahTap: (PageAyah) -> Unit,
    onAutoScrollEnd: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var ayahs by remember(pageNumber) { mutableStateOf(QuranPageRepository.cached(pageNumber)) }
    var tajweed by remember(pageNumber) { mutableStateOf(QuranPageRepository.cachedTajweed(pageNumber)) }
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
                onLoaded(pageNumber, loaded)
            }
        } else {
            onLoaded(pageNumber, existing)
        }
    }

    LaunchedEffect(pageNumber, tajweedOn) {
        if (tajweedOn && tajweed == null) {
            tajweed = withContext(Dispatchers.IO) {
                runCatching { QuranPageRepository.loadTajweedPage(context, pageNumber) }.getOrNull()
            }
        }
    }

    val data = ayahs
    val hasContent = data != null

    LaunchedEffect(isCurrent, autoScroll, scrollSpeed, hasContent) {
        if (isCurrent && autoScroll && hasContent) {
            delay(900)
            while (isActive) {
                val delta = scrollSpeed * 0.6f
                val consumed = scrollState.scrollBy(delta)
                if (consumed < delta * 0.1f) {
                    val wait = if (scrollState.maxValue == 0) (30000L / scrollSpeed) else 1500L
                    delay(wait)
                    onAutoScrollEnd()
                    break
                }
                delay(16)
            }
        }
    }

    val highlight = accent.copy(alpha = 0.38f)
    val activeTajweed = if (tajweedOn) tajweed else null
    val blocks = remember(data, activeTajweed, night, accent, listMode, highlightKeys) {
        if (data == null) {
            emptyList<PageBlock>()
        } else {
            buildBlocks(data, activeTajweed, night, accent, listMode, highlightKeys, highlight)
        }
    }

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
            val firstAyah = data.first()
            val headerSurah = surahs.firstOrNull { it.number == firstAyah.surahNumber }?.name.orEmpty()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (firstAyah.juz > 0) "الجزء " + firstAyah.juz else "",
                        color = ink.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                    Text(text = headerSurah, color = ink.copy(alpha = 0.6f), fontSize = 13.sp)
                }

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
                                    fontSize = (fontSizeSp - 2).coerceAtLeast(14).sp,
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
                                color = accent,
                                fontFamily = fontFamily,
                                fontSize = fontSizeSp.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        is PageBlock.Body -> {
                            AyahBodyText(
                                text = block.text,
                                ink = ink,
                                fontFamily = fontFamily,
                                fontSizeSp = fontSizeSp,
                                onAyahKey = { key -> data.firstOrNull { it.key == key }?.let(onAyahTap) }
                            )
                        }

                        is PageBlock.Single -> {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                AyahBodyText(
                                    text = block.text,
                                    ink = ink,
                                    fontFamily = fontFamily,
                                    fontSizeSp = fontSizeSp,
                                    onAyahKey = { key -> data.firstOrNull { it.key == key }?.let(onAyahTap) }
                                )
                                HorizontalDivider(
                                    color = accent.copy(alpha = 0.25f),
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
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

@Composable
private fun AyahBodyText(
    text: AnnotatedString,
    ink: Color,
    fontFamily: FontFamily,
    fontSizeSp: Int,
    onAyahKey: (Int) -> Unit
) {
    var layout by remember(text) { mutableStateOf<TextLayoutResult?>(null) }
    Text(
        text = text,
        color = ink,
        fontFamily = fontFamily,
        fontSize = fontSizeSp.sp,
        lineHeight = (fontSizeSp * 1.9f).sp,
        textAlign = TextAlign.Justify,
        onTextLayout = { layout = it },
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(text) {
                detectTapGestures { position ->
                    val result = layout
                    if (result != null) {
                        val offset = result.getOffsetForPosition(position)
                        val annotation = text.getStringAnnotations(AYAH_TAG, offset, offset).firstOrNull()
                        val key = annotation?.item?.toIntOrNull()
                        if (key != null) onAyahKey(key)
                    }
                }
            }
    )
}

// ============================ النوافذ ============================

@Composable
private fun AyahActionsDialog(
    ayah: PageAyah,
    surahName: String,
    fontFamily: FontFamily,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onFavorite: () -> Unit,
    onTafsir: () -> Unit,
    onPlay: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = scheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = surahName + " • الآية " + ayah.numberInSurah,
                    color = scheme.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = ayah.text,
                    color = scheme.onSurface,
                    fontFamily = fontFamily,
                    fontSize = 21.sp,
                    lineHeight = 36.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
                HorizontalDivider(color = scheme.outlineVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ActionItem(Icons.Rounded.ContentCopy, "نسخ", false, onCopy)
                    ActionItem(Icons.Rounded.Share, "مشاركة", false, onShare)
                    ActionItem(
                        if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        "مفضلة",
                        isFavorite,
                        onFavorite
                    )
                    ActionItem(Icons.Rounded.MenuBook, "تفسير", false, onTafsir)
                    ActionItem(Icons.Rounded.PlayArrow, "قراءة", false, onPlay)
                }
            }
        }
    }
}

@Composable
private fun ActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = if (active) scheme.tertiary else scheme.primaryContainer,
            modifier = Modifier.size(46.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (active) scheme.onTertiary else scheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, color = scheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TafsirDialog(
    ayah: PageAyah,
    page: Int,
    fontFamily: FontFamily,
    onShare: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    var tafsir by remember(ayah.key) { mutableStateOf(QuranPageRepository.cachedTafsir(page)?.get(ayah.key)) }
    var failed by remember(ayah.key) { mutableStateOf(false) }
    var retryTick by remember(ayah.key) { mutableIntStateOf(0) }

    LaunchedEffect(ayah.key, retryTick) {
        if (tafsir == null) {
            failed = false
            val map = withContext(Dispatchers.IO) {
                runCatching { QuranPageRepository.loadTafsirPage(context, page) }.getOrNull()
            }
            val text = map?.get(ayah.key)
            if (text == null) failed = true else tafsir = text
        }
    }

    val ayahLine = ayah.text + " ﴿" + toArabicDigits(ayah.numberInSurah) + "﴾"
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = scheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = ayahLine,
                    color = scheme.onSurface,
                    fontFamily = fontFamily,
                    fontSize = 22.sp,
                    lineHeight = 38.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                HorizontalDivider(color = scheme.outlineVariant)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp, max = 300.dp)
                ) {
                    val text = tafsir
                    if (text != null) {
                        Text(
                            text = text,
                            color = scheme.onSurface,
                            fontSize = 16.sp,
                            lineHeight = 28.sp,
                            textAlign = TextAlign.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        )
                    } else if (failed) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "تعذّر تحميل التفسير. تأكد من الإنترنت (يلزم مرة واحدة لكل صفحة).",
                                color = scheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            TextButton(onClick = { retryTick += 1 }) {
                                Text("إعادة المحاولة", color = scheme.primary)
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = scheme.primary)
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val body = tafsir
                            onShare(if (body == null) ayahLine else ayahLine + "\n\n" + body)
                        },
                        enabled = tafsir != null,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("مشاركة", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = scheme.primary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("حسناً", color = scheme.onPrimary, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionTitle(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 17.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SettingsSwitchRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun ModeOption(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (selected) scheme.primaryContainer else scheme.surface,
        border = BorderStroke(1.dp, if (selected) scheme.primary else scheme.outlineVariant),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            RadioButton(selected = selected, onClick = null)
            Text(text = label, color = scheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ReaderSettingsDialog(
    listMode: Boolean,
    onListMode: (Boolean) -> Unit,
    fontSizeSp: Int,
    onFontSize: (Int) -> Unit,
    tajweedOn: Boolean,
    onTajweed: (Boolean) -> Unit,
    reciterFolder: String,
    onReciter: (String) -> Unit,
    autoScroll: Boolean,
    onAutoScroll: (Boolean) -> Unit,
    scrollSpeed: Int,
    onScrollSpeed: (Int) -> Unit,
    onJump: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    var reciterMenu by remember { mutableStateOf(false) }
    var jumpText by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = scheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
                .padding(horizontal = 14.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SettingsSectionTitle("نمط عرض الآيات")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    ModeOption("صفحة", !listMode, { onListMode(false) }, Modifier.weight(1f))
                    ModeOption("قائمة", listMode, { onListMode(true) }, Modifier.weight(1f))
                }
                HorizontalDivider(color = scheme.outlineVariant)

                SettingsSectionTitle("خيارات الخط")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("حجم الخط", color = scheme.onSurface, fontSize = 15.sp)
                    Text(
                        text = fontSizeSp.toString(),
                        color = scheme.primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Slider(
                    value = fontSizeSp.toFloat(),
                    onValueChange = { onFontSize(it.toInt()) },
                    valueRange = 12f..60f,
                    steps = 47
                )
                HorizontalDivider(color = scheme.outlineVariant)

                SettingsSwitchRow("إظهار التجويد", tajweedOn, onTajweed)
                HorizontalDivider(color = scheme.outlineVariant)

                SettingsSectionTitle("القارئ")
                Box {
                    OutlinedButton(
                        onClick = { reciterMenu = true },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = QuranReciters.byFolder(reciterFolder).name,
                            color = scheme.onSurface,
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text("▾", color = scheme.primary, fontSize = 16.sp)
                    }
                    DropdownMenu(expanded = reciterMenu, onDismissRequest = { reciterMenu = false }) {
                        QuranReciters.all.forEach { reciter ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = reciter.name,
                                        fontWeight = if (reciter.folder == reciterFolder) FontWeight.ExtraBold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onReciter(reciter.folder)
                                    reciterMenu = false
                                }
                            )
                        }
                    }
                }
                HorizontalDivider(color = scheme.outlineVariant)

                SettingsSwitchRow("التمرير التلقائي", autoScroll, onAutoScroll)
                if (autoScroll) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("سرعة التمرير", color = scheme.onSurface, fontSize = 15.sp)
                        Text(
                            text = scrollSpeed.toString(),
                            color = scheme.primary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Slider(
                        value = scrollSpeed.toFloat(),
                        onValueChange = { onScrollSpeed(it.toInt().coerceIn(1, 10)) },
                        valueRange = 1f..10f,
                        steps = 8
                    )
                }
                HorizontalDivider(color = scheme.outlineVariant)

                SettingsSectionTitle("الانتقال إلى صفحة")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = jumpText,
                        onValueChange = { value -> jumpText = value.filter { it.isDigit() }.take(3) },
                        singleLine = true,
                        placeholder = { Text("من 1 إلى " + QuranPageRepository.PAGE_COUNT) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            val target = jumpText.toIntOrNull()
                            if (target != null) {
                                onJump(target)
                                jumpText = ""
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = scheme.primary)
                    ) {
                        Text("انتقال", color = scheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = scheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("تم", color = scheme.onPrimary, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}
