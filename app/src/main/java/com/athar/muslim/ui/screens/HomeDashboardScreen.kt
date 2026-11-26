package com.athar.muslim.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkAdded
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.athar.muslim.ui.components.SoundHelper
import com.athar.muslim.ui.screens.buttons.HomeQuickAction
import com.athar.muslim.ui.screens.buttons.HomeQuickActionRegistry

@Composable
fun HomeDashboardScreen(
    favoritesCount: Int,
    lastReadingTitle: String = "القرآن الكريم • فهرس السور الكامل",
    lastReadingAction: HomeQuickAction = HomeQuickAction.QURAN,
    soundEnabled: Boolean = true,
    hapticEnabled: Boolean = true,
    onActionClick: (HomeQuickAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val view = LocalView.current
    val actions = HomeQuickActionRegistry.orderedActions()
    val rows = actions.chunked(3)

    fun triggerAction(action: HomeQuickAction) {
        runCatching {
            val isZikrBtn = action == HomeQuickAction.AZKAR ||
                action == HomeQuickAction.DUA ||
                action == HomeQuickAction.TASBIH
            if (isZikrBtn) {
                SoundHelper.playZikrPressSound(
                    enabled = soundEnabled,
                    isCompletion = false,
                    view = view,
                    hapticEnabled = hapticEnabled
                )
            } else {
                SoundHelper.playSystemTouchSound(
                    context = context,
                    enabled = soundEnabled,
                    view = view,
                    hapticEnabled = hapticEnabled
                )
            }
        }
        onActionClick(action)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 20.dp)
    ) {
        item(key = "continue_reading_card") {
            Card(
                onClick = { triggerAction(lastReadingAction) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = scheme.surface),
                border = BorderStroke(1.2.dp, scheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = scheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.BookmarkAdded,
                                    contentDescription = "متابعة آخر قراءة",
                                    tint = scheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "متابعة آخر قراءة",
                                color = scheme.onSurface,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = lastReadingTitle,
                                color = scheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = scheme.primaryContainer
                        ) {
                            Text(
                                text = "المفضلة $favoritesCount",
                                color = scheme.onPrimaryContainer,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.ChevronLeft,
                            contentDescription = null,
                            tint = scheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        items(rows, key = { row -> row.first().id }) { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { action ->
                    QuickGridButton(
                        action = action,
                        onClick = { triggerAction(action) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(3 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun QuickGridButton(
    action: HomeQuickAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        border = BorderStroke(1.dp, scheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = modifier.height(98.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = scheme.primaryContainer,
                border = BorderStroke(1.dp, scheme.outlineVariant),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.title,
                        tint = scheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = action.title,
                color = scheme.onSurface,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}
