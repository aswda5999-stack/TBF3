package com.athar.muslim.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.athar.muslim.ui.theme.*

@Composable
fun SkyHeaderDecor(
    title: String,
    subtitleBadge: String,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(176.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(SkyTopStart, SkyTopMid, SkyBackground)
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val domeCenterX = w * 0.84f
            val domeBaseY = h * 0.72f
            val domeRadius = w * 0.11f

            drawRect(
                color = SkyOrnament.copy(alpha = 0.75f),
                topLeft = Offset(domeCenterX - domeRadius * 1.15f, domeBaseY),
                size = Size(domeRadius * 2.3f, h * 0.25f)
            )
            drawArc(
                color = SkyOrnament.copy(alpha = 0.85f),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(domeCenterX - domeRadius, domeBaseY - domeRadius),
                size = Size(domeRadius * 2f, domeRadius * 2f)
            )
            drawRect(
                color = SkyOrnament.copy(alpha = 0.8f),
                topLeft = Offset(domeCenterX + domeRadius * 1.25f, domeBaseY - h * 0.26f),
                size = Size(w * 0.03f, h * 0.45f)
            )
            drawArc(
                color = SkyOrnament.copy(alpha = 0.85f),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(domeCenterX + domeRadius * 1.2f, domeBaseY - h * 0.30f),
                size = Size(w * 0.04f, w * 0.05f)
            )

            drawCircle(
                color = SkyOrnament.copy(alpha = 0.75f),
                radius = w * 0.08f,
                center = Offset(w * 0.06f, h * 0.70f)
            )
            drawCircle(
                color = SkyOrnament.copy(alpha = 0.65f),
                radius = w * 0.11f,
                center = Offset(w * 0.14f, h * 0.74f)
            )

            val birds = listOf(
                Offset(w * 0.24f, h * 0.20f) to 10f,
                Offset(w * 0.29f, h * 0.15f) to 8f,
                Offset(w * 0.70f, h * 0.16f) to 9f,
                Offset(w * 0.75f, h * 0.21f) to 7f
            )
            birds.forEach { (origin, span) ->
                val birdPath = Path().apply {
                    moveTo(origin.x - span, origin.y + span * 0.35f)
                    cubicTo(
                        origin.x - span * 0.5f, origin.y - span * 0.35f,
                        origin.x - span * 0.2f, origin.y - span * 0.1f,
                        origin.x, origin.y + span * 0.2f
                    )
                    cubicTo(
                        origin.x + span * 0.2f, origin.y - span * 0.1f,
                        origin.x + span * 0.5f, origin.y - span * 0.35f,
                        origin.x + span, origin.y + span * 0.35f
                    )
                }
                drawPath(
                    path = birdPath,
                    color = BluePrimary.copy(alpha = 0.55f),
                    style = Stroke(width = 2.2f, cap = StrokeCap.Round)
                )
            }

            val wavePath = Path().apply {
                moveTo(0f, h * 0.82f)
                cubicTo(w * 0.15f, h * 0.73f, w * 0.35f, h * 0.75f, w * 0.5f, h * 0.84f)
                cubicTo(w * 0.68f, h * 0.94f, w * 0.86f, h * 0.88f, w, h * 0.80f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = wavePath,
                color = SkyBackground
            )
        }

        Surface(
            onClick = onSettingsClick,
            shape = CircleShape,
            color = SkySurface.copy(alpha = 0.92f),
            border = BorderStroke(1.5.dp, SkyBorder),
            shadowElevation = 4.dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 16.dp, top = 10.dp)
                .size(46.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "الإعدادات",
                    tint = BlueRoyal,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 26.dp,
                    topEnd = 26.dp,
                    bottomStart = 18.dp,
                    bottomEnd = 18.dp
                ),
                border = BorderStroke(2.dp, GoldAccent),
                shadowElevation = 5.dp,
                modifier = Modifier
                    .width(215.dp)
                    .height(62.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(BluePrimary, BlueRoyal)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = SkySurface,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(22.dp)
                        .height(2.dp)
                        .background(GoldAccent.copy(alpha = 0.85f))
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = SkySurface,
                    border = BorderStroke(1.5.dp, SkyBorder),
                    shadowElevation = 3.dp
                ) {
                    Text(
                        text = subtitleBadge,
                        color = NavyDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 5.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .width(22.dp)
                        .height(2.dp)
                        .background(GoldAccent.copy(alpha = 0.85f))
                )
            }
        }
    }
}
