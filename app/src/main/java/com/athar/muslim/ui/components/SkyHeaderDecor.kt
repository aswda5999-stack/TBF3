package com.athar.muslim.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class IslamicArtType {
    QURAN_BOOK,       // كتاب الله (المصحف الشريف على حامل)
    MASJID_HARAM,     // المسجد الحرام والكعبة المشرفة
    MASJID_NABAWI,    // المسجد النبوي والقبة الخضراء
    DOME_OF_ROCK,     // قبة الصخرة
    MIHRAB_LIGHT,     // محراب النور والقنديل
    ANDALUS_ARCHES,   // أقواس الأندلس
    CRESCENT_MINARET, // مئذنة وهلال
    ISLAMIC_STAR,     // النجمة الثمانية والزخرفة الهندسية
    TASBIH_PEARLS,    // سبحة الذكر
    QURAN_ILLUMINATED // صفحة مصحف مزخرفة
}

data class IslamicBannerSlide(
    val id: Int,
    val title: String,
    val subtitle: String,
    val tag: String,
    val artType: IslamicArtType
)

fun getTenIslamicSlides(): List<IslamicBannerSlide> = listOf(
    IslamicBannerSlide(1, "كتاب الله الكريم", "وَرَتِّلِ الْقُرْآنَ تَرْتِيلًا", "القرآن الكريم", IslamicArtType.QURAN_BOOK),
    IslamicBannerSlide(2, "المسجد الحرام", "قبلة المسلمين ومهوى الأفئدة", "مكة المكرمة", IslamicArtType.MASJID_HARAM),
    IslamicBannerSlide(3, "المسجد النبوي", "اللهم صلِّ وسلم على نبينا محمد", "المدينة المنورة", IslamicArtType.MASJID_NABAWI),
    IslamicBannerSlide(4, "قبة الصخرة", "بارك الله في المسجد الأقصى وما حوله", "القدس الشريف", IslamicArtType.DOME_OF_ROCK),
    IslamicBannerSlide(5, "محراب النور", "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ", "خشوع وطمأنينة", IslamicArtType.MIHRAB_LIGHT),
    IslamicBannerSlide(6, "المصحف المزخرف", "نورٌ وهدايةٌ للقلوب في كل حين", "آيات بينات", IslamicArtType.QURAN_ILLUMINATED),
    IslamicBannerSlide(7, "مآذن التوحيد", "حيّ على الصلاة.. حيّ على الفلاح", "نداء السماء", IslamicArtType.CRESCENT_MINARET),
    IslamicBannerSlide(8, "أقواس العراقة", "عمارة إسلامية تجمع الجمال والسكينة", "فن إسلامي", IslamicArtType.ANDALUS_ARCHES),
    IslamicBannerSlide(9, "سبحة الذاكرين", "سبحان الله وبحمده، سبحان الله العظيم", "ذكر وتسبيح", IslamicArtType.TASBIH_PEARLS),
    IslamicBannerSlide(10, "نجمة الهدى", "زخرفة هندسية تبعث الصفاء في النفس", "أثر مسلم", IslamicArtType.ISLAMIC_STAR)
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
    val slides = remember { getTenIslamicSlides() }
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
        shadowElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 6.dp, bottom = 10.dp)
        ) {
            // الشريط العلوي: زر الإعدادات على اليمين + العنوان + الشارة
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
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
                        border = BorderStroke(1.2.dp, scheme.outlineVariant),
                        modifier = Modifier.size(42.dp)
                    ) {
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "الضبط والإعدادات",
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
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = scheme.primaryContainer,
                    border = BorderStroke(1.dp, scheme.tertiary.copy(alpha = 0.8f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = scheme.tertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${activeIndex + 1} / ${slides.size}",
                            color = scheme.onPrimaryContainer,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // لوحة عرض واحدة فقط في الشاشة تتغير بسلاسة كل 4 ثوانٍ
            Crossfade(
                targetState = currentSlide,
                animationSpec = tween(durationMillis = 600),
                label = "single_banner_crossfade",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
            ) { slide ->
                IslamicArtworkBanner(
                    slide = slide,
                    onClick = { activeIndex = (activeIndex + 1) % slides.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(124.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 10 نقاط احترافية تتمدد وتتلون مع الصورة النشطة
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                slides.forEachIndexed { index, _ ->
                    val isSelected = index == activeIndex
                    val dotWidth by animateDpAsState(
                        targetValue = if (isSelected) 22.dp else 7.dp,
                        animationSpec = tween(300),
                        label = "dot_width"
                    )
                    val dotColor by animateColorAsState(
                        targetValue = if (isSelected) scheme.primary else scheme.outlineVariant,
                        animationSpec = tween(300),
                        label = "dot_color"
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(7.dp)
                            .width(dotWidth)
                            .clip(CircleShape)
                            .background(dotColor)
                            .clickable { activeIndex = index }
                    )
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
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.5.dp, scheme.tertiary.copy(alpha = 0.75f)),
        shadowElevation = 3.dp,
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
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawIslamicScene(
                    artType = slide.artType,
                    goldColor = scheme.tertiary,
                    lightColor = Color.White
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(0.60f)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.Black.copy(alpha = 0.26f),
                    border = BorderStroke(0.8.dp, scheme.tertiary.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = slide.tag,
                        color = scheme.tertiary,
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
                Text(
                    text = slide.subtitle,
                    color = Color.White.copy(alpha = 0.90f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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

    // خلفية نجوم خفيفة وأقواس زينة في كل اللوحات
    drawCircle(
        color = lightColor.copy(alpha = 0.06f),
        radius = h * 0.75f,
        center = Offset(w * 0.82f, h * 0.5f)
    )
    drawCircle(
        color = goldColor.copy(alpha = 0.14f),
        radius = h * 0.52f,
        center = Offset(w * 0.82f, h * 0.5f),
        style = Stroke(width = 1.5.dp.toPx())
    )

    val artCenterX = w * 0.80f
    val baseY = h * 0.88f

    when (artType) {
        IslamicArtType.QURAN_BOOK, IslamicArtType.QURAN_ILLUMINATED -> {
            // رسم حامل المصحف (الرحل) والمصحف الشريف مفتوحاً مع زخرفة ذهبية
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

            // صفحتا المصحف المفتوحتان
            val leftPage = Path().apply {
                moveTo(artCenterX, baseY - 22.dp.toPx())
                quadraticTo(
                    artCenterX - 24.dp.toPx(), baseY - 34.dp.toPx(),
                    artCenterX - 46.dp.toPx(), baseY - 24.dp.toPx()
                )
                lineTo(artCenterX - 42.dp.toPx(), baseY - 58.dp.toPx())
                quadraticTo(
                    artCenterX - 20.dp.toPx(), baseY - 66.dp.toPx(),
                    artCenterX, baseY - 54.dp.toPx()
                )
                close()
            }
            val rightPage = Path().apply {
                moveTo(artCenterX, baseY - 22.dp.toPx())
                quadraticTo(
                    artCenterX + 24.dp.toPx(), baseY - 34.dp.toPx(),
                    artCenterX + 46.dp.toPx(), baseY - 24.dp.toPx()
                )
                lineTo(artCenterX + 42.dp.toPx(), baseY - 58.dp.toPx())
                quadraticTo(
                    artCenterX + 20.dp.toPx(), baseY - 66.dp.toPx(),
                    artCenterX, baseY - 54.dp.toPx()
                )
                close()
            }
            drawPath(leftPage, color = lightColor.copy(alpha = 0.92f))
            drawPath(rightPage, color = lightColor.copy(alpha = 0.96f))
            drawPath(leftPage, color = goldColor, style = Stroke(width = 1.6.dp.toPx()))
            drawPath(rightPage, color = goldColor, style = Stroke(width = 1.6.dp.toPx()))

            // سطور مزخرفة داخل الصفحتين وهالة نور
            for (i in 1..3) {
                val offsetY = (i * 8).dp.toPx()
                drawLine(
                    color = goldColor.copy(alpha = 0.75f),
                    start = Offset(artCenterX - 34.dp.toPx(), baseY - 52.dp.toPx() + offsetY),
                    end = Offset(artCenterX - 8.dp.toPx(), baseY - 50.dp.toPx() + offsetY),
                    strokeWidth = 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = goldColor.copy(alpha = 0.75f),
                    start = Offset(artCenterX + 8.dp.toPx(), baseY - 50.dp.toPx() + offsetY),
                    end = Offset(artCenterX + 34.dp.toPx(), baseY - 52.dp.toPx() + offsetY),
                    strokeWidth = 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            drawStar8(Offset(artCenterX, h * 0.20f), 11.dp.toPx(), goldColor)
        }

        IslamicArtType.MASJID_HARAM -> {
            // الكعبة المشرفة بحزامها الذهبي وأقواس الحرم
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
            // مئذنتان جانبيتان
            drawMinaret(artCenterX - 36.dp.toPx(), baseY, 68.dp.toPx(), goldColor, lightColor)
            drawMinaret(artCenterX + 36.dp.toPx(), baseY, 68.dp.toPx(), goldColor, lightColor)
        }

        IslamicArtType.MASJID_NABAWI, IslamicArtType.DOME_OF_ROCK -> {
            // قبة كبيرة ومئذنتان رشيقتان
            drawDome(artCenterX, baseY, 54.dp.toPx(), 48.dp.toPx(), goldColor, lightColor)
            drawMinaret(artCenterX - 38.dp.toPx(), baseY, 72.dp.toPx(), goldColor, lightColor)
            drawMinaret(artCenterX + 38.dp.toPx(), baseY, 72.dp.toPx(), goldColor, lightColor)
        }

        IslamicArtType.MIHRAB_LIGHT, IslamicArtType.ANDALUS_ARCHES -> {
            // محراب وقنديل معلق
            val archW = 54.dp.toPx()
            val archH = 68.dp.toPx()
            val archPath = Path().apply {
                moveTo(artCenterX - archW / 2f, baseY)
                lineTo(artCenterX - archW / 2f, baseY - archH * 0.55f)
                quadraticTo(artCenterX, baseY - archH * 1.15f, artCenterX + archW / 2f, baseY - archH * 0.55f)
                lineTo(artCenterX + archW / 2f, baseY)
                close()
            }
            drawPath(archPath, color = lightColor.copy(alpha = 0.16f))
            drawPath(archPath, color = goldColor, style = Stroke(width = 2.2.dp.toPx()))
            // قنديل نور وسط المحراب
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
            // حبات سبحة دائرية متصلة
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
