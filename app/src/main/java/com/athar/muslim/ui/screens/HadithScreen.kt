package com.athar.muslim.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.athar.muslim.data.HadithBook
import com.athar.muslim.data.HadithBooksRepository
import com.athar.muslim.data.HadithSection
import com.athar.muslim.data.HadithSectionResult
import com.athar.muslim.model.ZikrItem
import com.athar.muslim.ui.components.ZikrCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private sealed interface SectionUiState {
    data object Loading : SectionUiState
    data class Ready(val section: HadithSection) : SectionUiState
    data object End : SectionUiState
    data class Failed(val message: String) : SectionUiState
}

@Composable
fun HadithScreen(
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    favoriteIds: Set<Int>,
    onToggleFavorite: (ZikrItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme

    var selectedBook by remember { mutableStateOf<HadithBook?>(null) }
    var sectionNumber by remember { mutableIntStateOf(1) }
    var reloadTick by remember { mutableIntStateOf(0) }
    var uiState by remember { mutableStateOf<SectionUiState>(SectionUiState.Loading) }
    var showPicker by remember { mutableStateOf(false) }
    val doneMap = remember { mutableStateMapOf<Int, Int>() }

    val book = selectedBook

    BackHandler(enabled = book != null) {
        selectedBook = null
        sectionNumber = 1
    }

    LaunchedEffect(book, sectionNumber, reloadTick) {
        val current = book
        if (current != null) {
            uiState = SectionUiState.Loading
            val result = withContext(Dispatchers.IO) {
                try {
                    HadithBooksRepository.loadSection(context, current, sectionNumber)
                } catch (e: Exception) {
                    HadithSectionResult.Error("حدث خطأ غير متوقع. أعد المحاولة.")
                }
            }
            uiState = when (result) {
                is HadithSectionResult.Success -> SectionUiState.Ready(result.section)
                is HadithSectionResult.EndOfBook -> SectionUiState.End
                is HadithSectionResult.Error -> SectionUiState.Failed(result.message)
            }
        }
    }

    if (book == null) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 18.dp)
        ) {
            items(HadithBooksRepository.books, key = { it.edition }) { entry ->
                Card(
                    onClick = {
                        sectionNumber = 1
                        selectedBook = entry
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = scheme.surface),
                    border = BorderStroke(1.dp, scheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
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
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoStories,
                                        contentDescription = null,
                                        tint = scheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.title,
                                    color = scheme.onSurface,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = entry.subtitle,
                                    color = scheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
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
            item {
                Text(
                    text = "النصوص من مستودع hadith-api المفتوح، وتُحمَّل مرة واحدة لكل قسم ثم تعمل دون اتصال.",
                    color = scheme.onSurfaceVariant,
                    fontSize = 11.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                )
            }
        }
    } else {
        Column(modifier = modifier.fillMaxSize()) {
            // شريط التنقل بين الأقسام
            Surface(
                color = scheme.surface,
                border = BorderStroke(1.dp, scheme.outlineVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { if (sectionNumber > 1) sectionNumber -= 1 },
                        enabled = !book.wholeFile && sectionNumber > 1
                    ) {
                        Icon(Icons.Rounded.ChevronRight, contentDescription = "السابق", tint = scheme.primary)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = book.sectionCount > 0) { showPicker = true }
                    ) {
                        Text(
                            text = book.title,
                            color = scheme.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = HadithBooksRepository.sectionLabel(book, sectionNumber),
                            color = scheme.onSurface,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        if (book.sectionCount > 0) {
                            Text(
                                text = "اضغط لاختيار الكتاب",
                                color = scheme.onSurfaceVariant,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                    IconButton(
                        onClick = { if (!book.wholeFile) sectionNumber += 1 },
                        enabled = !book.wholeFile && uiState !is SectionUiState.End &&
                            (book.sectionCount == 0 || sectionNumber < book.sectionCount)
                    ) {
                        Icon(Icons.Rounded.ChevronLeft, contentDescription = "التالي", tint = scheme.primary)
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (val state = uiState) {
                    is SectionUiState.Loading -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = scheme.primary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "جاري تحميل الأحاديث…",
                                color = scheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    }

                    is SectionUiState.End -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "انتهت أقسام هذا الكتاب",
                                color = scheme.onSurface,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { sectionNumber = 1 },
                                colors = ButtonDefaults.buttonColors(containerColor = scheme.primary)
                            ) {
                                Text("العودة لأول الكتاب", color = scheme.onPrimary)
                            }
                        }
                    }

                    is SectionUiState.Failed -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = state.message,
                                color = scheme.onSurface,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { reloadTick += 1 },
                                colors = ButtonDefaults.buttonColors(containerColor = scheme.primary)
                            ) {
                                Text("إعادة المحاولة", color = scheme.onPrimary)
                            }
                        }
                    }

                    is SectionUiState.Ready -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(top = 4.dp, bottom = 18.dp)
                        ) {
                            itemsIndexed(
                                state.section.items,
                                key = { index, entry -> "${entry.id}_$index" }
                            ) { _, entry ->
                                val remaining = doneMap[entry.id] ?: entry.count
                                ZikrCard(
                                    item = entry,
                                    remainingCount = remaining,
                                    onDecrement = { if (remaining > 0) doneMap[entry.id] = remaining - 1 },
                                    isZikrMode = false,
                                    isFavorite = favoriteIds.contains(entry.id),
                                    showFavoriteButton = true,
                                    onToggleFavorite = { onToggleFavorite(entry) },
                                    soundEnabled = soundEnabled,
                                    vibrationEnabled = vibrationEnabled
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPicker && book != null && book.sectionCount > 0) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            containerColor = scheme.surface,
            title = {
                Text(
                    text = "اختيار الكتاب",
                    color = scheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            },
            text = {
                LazyColumn(modifier = Modifier.height(380.dp)) {
                    items((1..book.sectionCount).toList(), key = { it }) { number ->
                        Text(
                            text = HadithBooksRepository.sectionLabel(book, number),
                            color = if (number == sectionNumber) scheme.primary else scheme.onSurface,
                            fontWeight = if (number == sectionNumber) FontWeight.ExtraBold else FontWeight.Normal,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    sectionNumber = number
                                    showPicker = false
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("إغلاق", color = scheme.primary)
                }
            }
        )
    }
}
