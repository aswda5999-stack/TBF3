package com.athar.muslim.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class IslamicArtType {
    QURAN_BOOK,
    MASJID_HARAM,
    MASJID_NABAWI,
    DOME_OF_ROCK,
    MIHRAB_LIGHT,
    ANDALUS_ARCHES,
    CRESCENT_MINARET,
    ISLAMIC_STAR,
    TASBIH_PEARLS,
    QURAN_ILLUMINATED
}

data class IslamicBannerSlide(
    val id: Int,
    val drawableName: String,
    val artType: IslamicArtType
)

fun getTenIslamicSlides(): List<IslamicBannerSlide> = listOf(
    IslamicBannerSlide(1, "islamic_slide_one", IslamicArtType.QURAN_BOOK),
    IslamicBannerSlide(2, "islamic_slide_two", IslamicArtType.MASJID_HARAM),
    IslamicBannerSlide(3, "islamic_slide_three", IslamicArtType.MASJID_NABAWI),
    IslamicBannerSlide(4, "islamic_slide_four", IslamicArtType.DOME_OF_ROCK),
    IslamicBannerSlide(5, "islamic_slide_five", IslamicArtType.MIHRAB_LIGHT),
    IslamicBannerSlide(6, "islamic_slide_six", IslamicArtType.QURAN_ILLUMINATED),
    IslamicBannerSlide(7, "islamic_slide_seven", IslamicArtType.CRESCENT_MINARET),
    IslamicBannerSlide(8, "islamic_slide_eight", IslamicArtType.ANDALUS_ARCHES),
    IslamicBannerSlide(9, "islamic_slide_nine", IslamicArtType.TASBIH_PEARLS),
    IslamicBannerSlide(10, "islamic_slide_ten", IslamicArtType.ISLAMIC_STAR)
)

@Composable
fun SkyHeaderDecor(
    title: String,
    subtitleBadge: String,
    showBannerCarousel: Boolean = false,
    autoSlideEnabled: Boolean = true,
    soundEnabled: Boolean = true,
    hapticEnabled: Boolean = true,
    brandTitleFontFamily: FontFamily = FontFamily.Serif,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val view = LocalView.current
    val slides = remember { getTenIslamicSlides() }
    val indexState = remember { mutableIntStateOf(0) }
    val activeIndex = indexState.intValue

    LaunchedEffect(showBannerCarousel, autoSlideEnabled, slides.size) {
        if (!showBannerCarousel || !autoSlideEnabled || slides.size <= 1) return@LaunchedEffect
        while (true) {
            delay(5000L)
            indexState.intValue = (indexState.intValue + 1) % slides.size
        }
    }

    val currentSlide = slides[activeIndex]

    Surface(
        color = scheme.surface,
        shadowElevation = 3.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 8.dp, bottom = if (showBannerCarousel) 10.dp else 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
            ) {
                Text(
                    text = title,
                    color = scheme.onSurface,
                    fontFamily = brandTitleFontFamily,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitleBadge,
                    color = scheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (showBannerCarousel) {
                Spacer(modifier = Modifier.height(10.dp))

                AnimatedContent(
                    targetState = currentSlide,
                    transitionSpec = {
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
                        ) togetherWith slideOutHorizontally(
                            targetOffsetX = { fullWidth -> -fullWidth },
                            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
                        )
                    },
                    label = "slide_carousel",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                ) { slide ->
                    IslamicArtworkBanner(
                        slide = slide,
                        onClick = {
                            SoundHelper.playSystemTouchSound(
                                context = context,
                                enabled = soundEnabled,
                                view = view,
                                hapticEnabled = hapticEnabled
                            )
                            indexState.intValue = (indexState.intValue + 1) % slides.size
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(128.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    slides.forEachIndexed { index, _ ->
                        val isSelected = index == activeIndex
                        val dotWidth = animateDpAsState(
                            targetValue = if (isSelected) 22.dp else 7.dp,
                            animationSpec = tween(250),
                            label = "dot_width"
                        ).value
                        val dotColor = animateColorAsState(
                            targetValue = if (isSelected) scheme.primary else scheme.outlineVariant,
                            animationSpec = tween(250),
                            label = "dot_color"
                        ).value

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .height(7.dp)
                                .width(dotWidth)
                                .clip(CircleShape)
                                .background(dotColor)
                                .clickable {
                                    SoundHelper.playSystemTouchSound(
                                        context = context,
                                        enabled = soundEnabled,
                                        view = view,
                                        hapticEnabled = hapticEnabled
                                    )
                                    indexState.intValue = index
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IslamicArtworkBanner(
    slide: IslamicBannerSlide,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val resId = remember(slide.drawableName) {
        context.resources.getIdentifier(slide.drawableName, "drawable", context.packageName)
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.4.dp, scheme.tertiary.copy(alpha = 0.7f)),
        shadowElevation = 2.dp,
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            scheme.primary,
                            scheme.secondary,
                            scheme.primary.copy(alpha = 0.92f)
                        )
                    )
                )
        ) {
            if (resId != 0) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawIslamicScene(
                        artType = slide.artType,
                        goldColor = scheme.tertiary,
                        lightColor = Color.White
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawIslamicScene(
    artType: IslamicArtType,
    goldColor: Color,
    lightColor: Color
) {
    val w = size.width
    val h = size.height

    drawCircle(
        color = lightColor.copy(alpha = 0.08f),
        radius = h * 0.72f,
        center = Offset(w * 0.5f, h * 0.5f)
    )
    drawCircle(
        color = goldColor.copy(alpha = 0.16f),
        radius = h * 0.50f,
        center = Offset(w * 0.5f, h * 0.5f),
        style = Stroke(width = 1.5.dp.toPx())
    )

    val artCenterX = w * 0.5f
    val baseY = h * 0.88f

    when (artType) {
        IslamicArtType.QURAN_BOOK, IslamicArtType.QURAN_ILLUMINATED -> {
            val standPath = Path().apply {
                moveTo(artCenterX - 34.dp.toPx(), baseY)
                lineTo(artCenterX + 26.dp.toPx(), baseY - 26.dp.toPx())
                moveTo(artCenterX + 34.dp.toPx(), baseY)
                lineTo(artCenterX - 26.dp.toPx(), baseY - 26.dp.toPx())
            }
            drawPath(
                path = standPath,
                color = goldColor,
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
            )

            val leftPage = Path().apply {
                moveTo(artCenterX, baseY - 22.dp.toPx())
                cubicTo(
                    artCenterX - 16.dp.toPx(), baseY - 30.dp.toPx(),
                    artCenterX - 32.dp.toPx(), baseY - 30.dp.toPx(),
                    artCenterX - 46.dp.toPx(), baseY - 24.dp.toPx()
                )
                lineTo(artCenterX - 42.dp.toPx(), baseY - 58.dp.toPx())
                cubicTo(
                    artCenterX - 28.dp.toPx(), baseY - 64.dp.toPx(),
                    artCenterX - 14.dp.toPx(), baseY - 62.dp.toPx(),
                    artCenterX, baseY - 54.dp.toPx()
                )
                close()
            }
            val rightPage = Path().apply {
                moveTo(artCenterX, baseY - 22.dp.toPx())
                cubicTo(
                    artCenterX + 16.dp.toPx(), baseY - 30.dp.toPx(),
                    artCenterX + 32.dp.toPx(), baseY - 30.dp.toPx(),
                    artCenterX + 46.dp.toPx(), baseY - 24.dp.toPx()
                )
                lineTo(artCenterX + 42.dp.toPx(), baseY - 58.dp.toPx())
                cubicTo(
                    artCenterX + 28.dp.toPx(), baseY - 64.dp.toPx(),
                    artCenterX + 14.dp.toPx(), baseY - 62.dp.toPx(),
                    artCenterX, baseY - 54.dp.toPx()
                )
                close()
            }
            drawPath(leftPage, color = lightColor.copy(alpha = 0.92f))
            drawPath(rightPage, color = lightColor.copy(alpha = 0.96f))
            drawPath(leftPage, color = goldColor, style = Stroke(width = 1.6.dp.toPx()))
            drawPath(rightPage, color = goldColor, style = Stroke(width = 1.6.dp.toPx()))
            drawStar8(Offset(artCenterX, h * 0.20f), 11.dp.toPx(), goldColor)
        }

        IslamicArtType.MASJID_HARAM -> {
            val kWidth = 44.dp.toPx()
            val kHeight = 50.dp.toPx()
            val kLeft = artCenterX - kWidth / 2f
            val kTop = baseY - kHeight
            drawRoundRect(
                color = Color(0xFF14181F),
                topLeft = Offset(kLeft, kTop),
                size = Size(kWidth, kHeight),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
            drawRect(
                color = goldColor,
                topLeft = Offset(kLeft, kTop + 11.dp.toPx()),
                size = Size(kWidth, 6.dp.toPx())
            )
            drawRect(
                color = goldColor.copy(alpha = 0.85f),
                topLeft = Offset(kLeft + 8.dp.toPx(), kTop + 24.dp.toPx()),
                size = Size(9.dp.toPx(), 15.dp.toPx())
            )
            drawMinaret(artCenterX - 36.dp.toPx(), baseY, 68.dp.toPx(), goldColor, lightColor)
            drawMinaret(artCenterX + 36.dp.toPx(), baseY, 68.dp.toPx(), goldColor, lightColor)
        }

        IslamicArtType.MASJID_NABAWI, IslamicArtType.DOME_OF_ROCK -> {
            drawDome(artCenterX, baseY, 54.dp.toPx(), 48.dp.toPx(), goldColor, lightColor)
            drawMinaret(artCenterX - 38.dp.toPx(), baseY, 72.dp.toPx(), goldColor, lightColor)
            drawMinaret(artCenterX + 38.dp.toPx(), baseY, 72.dp.toPx(), goldColor, lightColor)
        }

        IslamicArtType.MIHRAB_LIGHT, IslamicArtType.ANDALUS_ARCHES -> {
            val archW = 54.dp.toPx()
            val archH = 68.dp.toPx()
            val archPath = Path().apply {
                moveTo(artCenterX - archW / 2f, baseY)
                lineTo(artCenterX - archW / 2f, baseY - archH * 0.55f)
                cubicTo(
                    artCenterX - archW / 2f, baseY - archH * 1.05f,
                    artCenterX + archW / 2f, baseY - archH * 1.05f,
                    artCenterX + archW / 2f, baseY - archH * 0.55f
                )
                lineTo(artCenterX + archW / 2f, baseY)
                close()
            }
            drawPath(archPath, color = lightColor.copy(alpha = 0.16f))
            drawPath(archPath, color = goldColor, style = Stroke(width = 2.2.dp.toPx()))
            drawLine(
                color = goldColor,
                start = Offset(artCenterX, baseY - archH * 0.82f),
                end = Offset(artCenterX, baseY - archH * 0.45f),
                strokeWidth = 1.5.dp.toPx()
            )
            drawCircle(
                color = goldColor,
                radius = 7.dp.toPx(),
                center = Offset(artCenterX, baseY - archH * 0.40f)
            )
        }

        IslamicArtType.CRESCENT_MINARET -> {
            drawMinaret(artCenterX - 18.dp.toPx(), baseY, 76.dp.toPx(), goldColor, lightColor)
            drawCrescent(Offset(artCenterX + 22.dp.toPx(), h * 0.34f), 18.dp.toPx(), goldColor)
        }

        IslamicArtType.TASBIH_PEARLS -> {
            val radius = 28.dp.toPx()
            val center = Offset(artCenterX, h * 0.50f)
            drawCircle(
                color = goldColor.copy(alpha = 0.45f),
                radius = radius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )
            for (i in 0 until 14) {
                val angle = (i * 2.0 * PI / 14.0)
                val bx = center.x + (radius * cos(angle)).toFloat()
                val by = center.y + (radius * sin(angle)).toFloat()
                drawCircle(
                    color = if (i % 3 == 0) goldColor else lightColor.copy(alpha = 0.9f),
                    radius = 4.dp.toPx(),
                    center = Offset(bx, by)
                )
            }
        }

        IslamicArtType.ISLAMIC_STAR -> {
            drawStar8(Offset(artCenterX, h * 0.50f), 32.dp.toPx(), goldColor)
            drawStar8(Offset(artCenterX, h * 0.50f), 18.dp.toPx(), lightColor.copy(alpha = 0.85f))
        }
    }
}

private fun DrawScope.drawDome(
    centerX: Float,
    baseY: Float,
    width: Float,
    height: Float,
    goldColor: Color,
    lightColor: Color
) {
    val left = centerX - width / 2f
    val top = baseY - height
    val path = Path().apply {
        moveTo(left, baseY)
        lineTo(left, top + height * 0.45f)
        cubicTo(left, top, left + width, top, left + width, top + height * 0.45f)
        lineTo(left + width, baseY)
        close()
    }
    drawPath(path, color = lightColor.copy(alpha = 0.22f))
    drawPath(path, color = goldColor, style = Stroke(width = 2.dp.toPx()))
    drawLine(
        color = goldColor,
        start = Offset(centerX, top + 2.dp.toPx()),
        end = Offset(centerX, top - 10.dp.toPx()),
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawMinaret(
    x: Float,
    baseY: Float,
    height: Float,
    goldColor: Color,
    lightColor: Color
) {
    val topY = baseY - height
    drawLine(
        color = lightColor.copy(alpha = 0.85f),
        start = Offset(x, baseY),
        end = Offset(x, topY + 10.dp.toPx()),
        strokeWidth = 5.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = goldColor,
        start = Offset(x - 5.dp.toPx(), topY + 22.dp.toPx()),
        end = Offset(x + 5.dp.toPx(), topY + 22.dp.toPx()),
        strokeWidth = 2.5.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawCircle(
        color = goldColor,
        radius = 4.dp.toPx(),
        center = Offset(x, topY + 5.dp.toPx())
    )
}

private fun DrawScope.drawStar8(center: Offset, outerRadius: Float, color: Color) {
    val innerRadius = outerRadius * 0.48f
    val path = Path()
    for (i in 0 until 16) {
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val angle = i * PI / 8.0
        val px = center.x + (r * cos(angle)).toFloat()
        val py = center.y + (r * sin(angle)).toFloat()
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
    drawPath(path, color = color.copy(alpha = 0.30f))
    drawPath(path, color = color, style = Stroke(width = 1.8.dp.toPx()))
}

private fun DrawScope.drawCrescent(center: Offset, radius: Float, color: Color) {
    drawCircle(color = color, radius = radius, center = center)
    drawCircle(
        color = Color(0xFF0F4C81),
        radius = radius * 0.78f,
        center = Offset(center.x - radius * 0.35f, center.y - radius * 0.15f)
    )
}
