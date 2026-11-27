package com.athar.muslim.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.athar.muslim.model.ZikrItem
import com.athar.muslim.ui.components.ZikrCard

@Composable
fun FavoritesScreen(
    items: List<ZikrItem>,
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    onToggleFavorite: (ZikrItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val doneMap = remember { mutableStateMapOf<Int, Int>() }

    if (items.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "لا توجد عناصر في المفضلة بعد",
                color = scheme.onSurface,
                fontSize = 17.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "اضغط على القلب في أي بطاقة (ذكر أو دعاء أو حديث) لتجدها هنا.",
                color = scheme.onSurfaceVariant,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 18.dp)
        ) {
            items(items, key = { it.id }) { entry ->
                val remaining = doneMap[entry.id] ?: entry.count
                ZikrCard(
                    item = entry,
                    remainingCount = remaining,
                    onDecrement = { if (remaining > 0) doneMap[entry.id] = remaining - 1 },
                    isZikrMode = entry.count > 1,
                    isFavorite = true,
                    showFavoriteButton = true,
                    onToggleFavorite = { onToggleFavorite(entry) },
                    soundEnabled = soundEnabled,
                    vibrationEnabled = vibrationEnabled
                )
            }
        }
    }
}
