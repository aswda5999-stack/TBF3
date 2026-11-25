package com.athar.muslim.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class IslamicBannerSlide(
    val id: Int,
    val title: String,
    val tag: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color,
    val motifType: Int
)

fun defaultIslamicBannerSlides(
    primary: Color,
    secondary: Color,
    accent: Color
): List<IslamicBannerSlide> = listOf(
    IslamicBannerSlide(1, "المسجد الحرام", "قبلة المسلمين", primary, secondary, accent, 0),
    IslamicBannerSlide(2, "المسجد النبوي", "طيبة الطيبة", secondary, primary, accent, 1),
    IslamicBannerSlide(3, "قبة الصخرة", "القدس الشريف", primary, secondary, accent, 2),
    IslamicBannerSlide(4, "مسجد قرطبة", "أقواس الأندلس", secondary, primary, accent, 3),
    IslamicBannerSlide(5, "الجامع الأموي", "عراقة الشام", primary, secondary, accent, 0),
    IslamicBannerSlide(6, "مسجد السلطان أحمد", "قباب ومآذن", secondary, primary, accent, 1),
    IslamicBannerSlide(7, "الجامع الأزهر", "منارة العلم", primary, secondary, accent, 2),
    IslamicBannerSlide(8, "زخرفة هندسية", "فن إسلامي", secondary, primary, accent, 3),
    IslamicBannerSlide(9, "هلال ونجوم", "سكينة الليل", primary, secondary, accent, 0),
    IslamicBannerSlide(10, "محراب النور", "خشوع وطمأنينة", secondary, primary, accent, 1)
)

@Composable
fun SkyHeaderDecor(
    title: String,
    subtitleBadge: String,
    autoSlideEnabled: Boolean = true,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val slides = remember(scheme.primary, scheme.secondary, scheme.tertiary) {
        defaultIslamicBannerSlides(
            primary = scheme.primary,
            secondary = scheme.secondary,
            accent = scheme.tertiary
        )
    }

    var activeIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(autoSlideEnabled, slides.size) {
        if (!autoSlideEnabled || slides.isEmpty()) return@LaunchedEffect
        while (true) {
            delay(4000L)
            activeIndex = (activeIndex + 1) % slides.size
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
                .padding(top = 8.dp, bottom = 10.dp)
        ) {
            // الصف العلوي: زر الإعدادات منقول إلى الجهة اليمنى (أول عنصر في RTL) وبجانبه العنوان والشارة
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = scheme.primaryContainer,
                        border = BorderStroke(1.dp, scheme.outlineVariant),
                        modifier = Modifier.size(40.dp)
                    ) {
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "الإعدادات",
                                tint = scheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            color = scheme.onSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subtitleBadge,
                            color = scheme.onSurfaceVariant,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = scheme.primaryContainer,
                    border = BorderStroke(1.dp, scheme.tertiary.copy(alpha = 0.7f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Mosque,
                            contentDescription = null,
                            tint = scheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${activeIndex + 1}/${slides.size}",
                            color = scheme.onPrimaryContainer,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // لوحة العرض الرئيسية للصورة النشطة (تتقلب كل 4 ثوانٍ وتعيد من البداية)
            Crossfade(
                targetState = currentSlide,
                animationSpec = tween(durationMillis = 550),
                label = "islamic_banner_crossfade",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) { slide ->
                IslamicArtworkCard(
                    slide = slide,
                    isHighlighted = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(104.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // شريط الـ 10 صور الإسلامية المصغرة أسفل الهيدر لتصفحها أو رؤية التسلسل
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(slides, key = { _, item -> item.id }) { index, slide ->
                    val selected = index == activeIndex
                    Surface(
                        onClick = { activeIndex = index },
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) scheme.primaryContainer else scheme.surfaceVariant.copy(alpha = 0.55f),
                        border = BorderStroke(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) scheme.tertiary else scheme.outlineVariant
                        ),
                        modifier = Modifier
                            .width(92.dp)
                            .height(54.dp)
                    ) {
                        IslamicArtworkCard(
                            slide = slide,
                            isHighlighted = selected,
                            compact = true,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IslamicArtworkCard(
    slide: IslamicBannerSlide,
    isHighlighted: Boolean,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(if (compact) 14.dp else 18.dp),
        border = BorderStroke(
            width = if (isHighlighted) 1.5.dp else 1.dp,
            color = if (isHighlighted) slide.accentColor else slide.primaryColor.copy(alpha = 0.25f)
        ),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(slide.primaryColor, slide.secondaryColor)
                    )
                )
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // قبة ومسجد هندسي مرسوم بالكود بدقة عالية دون الحاجة لملفات خارجية مفقودة
                val domeWidth = w * if (compact) 0.42f else 0.34f
                val domeHeight = h * if (compact) 0.58f else 0.64f
                val domeLeft = w * if (compact) 0.54f else 0.62f
                val domeTop = h - domeHeight

                val domePath = Path().apply {
                    moveTo(domeLeft, h)
                    lineTo(domeLeft, domeTop + domeHeight * 0.45f)
                    cubicTo(
                        domeLeft, domeTop,
                        domeLeft + domeWidth, domeTop,
                        domeLeft + domeWidth, domeTop + domeHeight * 0.45f
                    )
                    lineTo(domeLeft + domeWidth, h)
                    close()
                }
                drawPath(
                    path = domePath,
                    color = Color.White.copy(alpha = 0.16f)
                )
                drawPath(
                    path = domePath,
                    color = slide.accentColor.copy(alpha = 0.75f),
                    style = Stroke(width = if (compact) 1.5.dp.toPx() else 2.dp.toPx())
                )

                // مئذنة رشيقة بجانب القبة
                val minaretX = domeLeft - (w * 0.07f)
                val minaretTop = h * 0.18f
                drawLine(
                    color = slide.accentColor.copy(alpha = 0.8f),
                    start = Offset(minaretX, h),
                    end = Offset(minaretX, minaretTop),
                    strokeWidth = if (compact) 2.5.dp.toPx() else 3.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = slide.accentColor,
                    radius = if (compact) 3.dp.toPx() else 4.5.dp.toPx(),
                    center = Offset(minaretX, minaretTop - 4.dp.toPx())
                )

                // زخرفة نجمية إسلامية
                val cx = if (compact) w * 0.24f else w * 0.16f
                val cy = h * 0.30f
                val rOuter = if (compact) 8.dp.toPx() else 13.dp.toPx()
                val rInner = rOuter * 0.48f
                val starPath = Path()
                val points = 16
                for (i in 0 until points) {
                    val r = if (i % 2 == 0) rOuter else rInner
                    val angle = i * PI / 8.0
                    val px = cx + (r * cos(angle)).toFloat()
                    val py = cy + (r * sin(angle)).toFloat()
                    if (i == 0) starPath.moveTo(px, py) else starPath.lineTo(px, py)
                }
                starPath.close()
                drawPath(
                    path = starPath,
                    color = slide.accentColor.copy(alpha = 0.45f)
                )

                if (!compact) {
                    drawArc(
                        color = Color.White.copy(alpha = 0.12f),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(w * 0.38f, h * 0.45f),
                        size = Size(w * 0.16f, h * 0.55f),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
            }

            if (!compact) {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.Black.copy(alpha = 0.22f)
                    ) {
                        Text(
                            text = slide.tag,
                            color = slide.accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = slide.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 6.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${slide.id}. ${slide.title}",
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
