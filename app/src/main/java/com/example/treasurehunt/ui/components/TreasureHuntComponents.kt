package com.example.treasurehunt.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.treasurehunt.R
/**
 *  Reid Pettibone
 *  CS 492
 *  OSU
 * */
@Composable
fun CompassGlyph(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val stroke = size.minDimension * 0.08f
        drawCircle(
            color = color,
            style = Stroke(width = stroke)
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.32f, size.height * 0.68f),
            end = Offset(size.width * 0.7f, size.height * 0.3f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.58f, size.height * 0.32f),
            end = Offset(size.width * 0.7f, size.height * 0.3f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.68f, size.height * 0.42f),
            end = Offset(size.width * 0.7f, size.height * 0.3f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun RibbonBanner(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            val border = 5.dp.toPx()
            val centerTop = size.height * 0.22f
            val centerBottom = size.height * 0.8f
            val left = size.width * 0.12f
            val right = size.width * 0.88f

            drawRoundRect(
                color = Color.White,
                topLeft = Offset(left, centerTop),
                size = androidx.compose.ui.geometry.Size(right - left, centerBottom - centerTop),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                style = Fill
            )
            drawRoundRect(
                color = Color.Black,
                topLeft = Offset(left, centerTop),
                size = androidx.compose.ui.geometry.Size(right - left, centerBottom - centerTop),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                style = Stroke(width = border)
            )

            val leftTail = Path().apply {
                moveTo(left, centerBottom - 3.dp.toPx())
                lineTo(left - 34.dp.toPx(), centerBottom + 10.dp.toPx())
                lineTo(left - 26.dp.toPx(), centerBottom + 2.dp.toPx())
                lineTo(left - 34.dp.toPx(), centerBottom - 8.dp.toPx())
                close()
            }
            val rightTail = Path().apply {
                moveTo(right, centerBottom - 3.dp.toPx())
                lineTo(right + 34.dp.toPx(), centerBottom + 10.dp.toPx())
                lineTo(right + 26.dp.toPx(), centerBottom + 2.dp.toPx())
                lineTo(right + 34.dp.toPx(), centerBottom - 8.dp.toPx())
                close()
            }
            drawPath(path = leftTail, color = Color.White, style = Fill)
            drawPath(path = rightTail, color = Color.White, style = Fill)
            drawPath(path = leftTail, color = Color.Black, style = Stroke(width = border))
            drawPath(path = rightTail, color = Color.Black, style = Stroke(width = border))
        }
        Text(
            text = text,
            color = Color.Black,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.2.sp
            )
        )
    }
}

@Composable
fun StarCircleButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .size(44.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = Color(0xFFE5CC62),
        contentColor = Color.Black,
        border = BorderStroke(1.5.dp, Color.Black)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Stickers",
                modifier = Modifier.size(25.dp)
            )
        }
    }
}

@Composable
fun HandDrawnArrow(
    modifier: Modifier = Modifier,
    color: Color = Color.Black
) {
    Canvas(modifier = modifier.size(30.dp)) {
        drawLine(
            color = color,
            start = Offset(size.width * 0.15f, size.height * 0.35f),
            end = Offset(size.width * 0.78f, size.height * 0.28f),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.78f, size.height * 0.28f),
            end = Offset(size.width * 0.6f, size.height * 0.12f),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.78f, size.height * 0.28f),
            end = Offset(size.width * 0.6f, size.height * 0.45f),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun ModeBadge(
    label: String,
    background: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = background,
        contentColor = contentColor,
        shape = CircleShape
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    background: Color,
    border: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = background),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(2.dp, border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
fun StatusBanner(
    message: String,
    modifier: Modifier = Modifier,
    isWarning: Boolean = true
) {
    val background = if (isWarning) Color(0xFFFFF0EF) else Color(0xFFF3F8FF)
    val border = if (isWarning) Color(0xFFE6B3AF) else Color(0xFF9BC7FF)
    val content = if (isWarning) Color(0xFFC72E2E) else Color(0xFF245FA9)
    AppCard(
        modifier = modifier,
        background = background,
        border = border
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = content
        )
    }
}

@Composable
fun AnimatedTimerChip(
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFE9B06D),
        contentColor = Color.White,
        border = BorderStroke(2.dp, Color.Black)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

@Composable
fun RotatingPointer(
    rotationDegrees: Float,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF914500)
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .rotate(rotationDegrees),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "^",
            color = color,
            fontSize = 48.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
}

@Composable
fun StickerBadge(
    stickerId: String,
    unlocked: Boolean,
    modifier: Modifier = Modifier
) {
    val alpha = if (unlocked) 1f else 0.34f
    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .border(1.5.dp, Color.Black, CircleShape),
        shape = CircleShape,
        color = Color.Black
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = stickerDrawableResId(stickerId)),
                contentDescription = "$stickerId sticker",
                modifier = Modifier
                    .size(68.dp)
                    .alpha(alpha),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@DrawableRes
private fun stickerDrawableResId(stickerId: String): Int = when (stickerId.lowercase()) {
    "gift", "present" -> R.drawable.sticker_present
    "crown" -> R.drawable.sticker_crown
    "gem", "diamond" -> R.drawable.sticker_gem
    "wand" -> R.drawable.sticker_wand
    "map" -> R.drawable.sticker_map
    "compass" -> R.drawable.sticker_compass
    "trophy" -> R.drawable.sticker_trophy
    "star" -> R.drawable.sticker_star
    "key" -> R.drawable.sticker_key
    "candy" -> R.drawable.sticker_candy
    else -> R.drawable.sticker_star
}

@Composable
fun PaperPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(2.dp))
            .border(2.dp, Color.Black, RoundedCornerShape(2.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

@Composable
fun BulletLine(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "o",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            ),
            modifier = Modifier.padding(end = 10.dp)
                               .offset(y = (-2).dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
        )
    }
}

@Composable
fun UnderlineTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .height(2.dp)
                .fillMaxWidth(0.72f)
                .background(Color.Black)
        )
    }
}
