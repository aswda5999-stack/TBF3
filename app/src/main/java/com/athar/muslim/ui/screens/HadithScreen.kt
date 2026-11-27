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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import com.athar.muslim.data.HadithRepository
import com.athar.muslim.model.HadithLoadResult
import com.athar.muslim.model.HadithTopic
import com.athar.muslim.model.ZikrItem
import com.athar.muslim.ui.components.ZikrCard

@Composable
fun HadithScreen(
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val topics = remember { HadithTopic.sixMainCategories }
    val selectedTopicState = remember { mutableStateOf<HadithTopic?>(null) }
    val selectedTopic = selectedTopicState.value
    val itemsState = remember { mutableStateOf<List<ZikrItem>>(emptyList()) }
    val loadingState = remember { mutableStateOf(false) }
    val errorState = remember { mutableStateOf<String?>(null) }
    val reloadTick = remember { mutableStateOf(0) }
    val doneMap = remember { mutableStateMapOf<Int, Int>() }

    // الرجوع من قائمة الأحاديث إلى قائمة الكتب
    BackHandler(enabled = selectedTopic != null) {
        selectedTopicState.value = null
    }

    LaunchedEffect(selectedTopic, reloadTick.value) {
        val topic = selectedTopic ?: return@LaunchedEffect
        loadingState.value = true
        errorState.value = null
        itemsState.value = emptyList()
        val result = try {
            HadithRepository.loadTopicHadiths(topic)
        } catch (e: Exception) {
            HadithLoadResult.Error(e.message ?: "تعذر تحميل الأحاديث")
        }
        when (result) {
            is HadithLoadResult.Success -> itemsState.value = result.items
            is HadithLoadResult.Error -> errorState.value = result.message
            HadithLoadResult.UnderDevelopment -> errorState.value = "هذا القسم قيد التطوير"
        }
        loadingState.value = false
    }

    if (selectedTopic == null) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 18.dp)
        ) {
            items(topics, key = { it.name }) { topic ->
                Card(
                    onClick = { selectedTopicState.value = topic },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = scheme.surface),
                    border = BorderStroke(1.2.dp, scheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
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
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = topic.icon,
                                        contentDescription = null,
                                        tint = scheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = topic.title,
                                    color = scheme.onSurface,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = topic.subtitle,
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
        }
    } else {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            loadingState.value -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = scheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "جاري تحميل ${selectedTopic.title}…",
                        color = scheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }

            errorState.value != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = errorState.value.orEmpty(),
                        color = scheme.onSurface,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { reloadTick.value = reloadTick.value + 1 },
                        colors = ButtonDefaults.buttonColors(containerColor = scheme.primary)
                    ) {
                        Text("إعادة المحاولة", color = scheme.onPrimary)
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 18.dp)
                ) {
                    items(itemsState.value, key = { it.id }) { entry ->
                        val remaining = doneMap[entry.id] ?: entry.count
                        ZikrCard(
                            item = entry,
                            remainingCount = remaining,
                            onDecrement = {
                                if (remaining > 0) doneMap[entry.id] = remaining - 1
                            },
                            isZikrMode = false,
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
