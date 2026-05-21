package bg.pm.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas

private val RunePurple = Color(0xFF8D35FF)
private val RunePurpleDeep = Color(0xFF53207E)
private val RunePurpleSoft = Color(0xFFD3AEFF)
private val RuneBlack = Color(0xFF050505)
private val RuneBlackSoft = Color(0xFF111111)

@Composable
fun RuneBrand(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    showWordmark: Boolean = true,
) {
    val iconSize = if (compact) 38.dp else 68.dp
    val titleSize = if (compact) 21.sp else 34.sp
    val subtitleSize = if (compact) 8.sp else 10.sp
    val spacing = if (compact) 10.dp else 12.dp

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        RuneSeal(sealSize = iconSize)

        if (showWordmark) {
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = "Glyph",
                    color = RunePurpleSoft,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = titleSize,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = if (compact) 0.6.sp else 0.8.sp,
                    )
                )
                Text(
                    text = "RUNE SYSTEM",
                    color = RunePurple.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = subtitleSize,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = if (compact) 2.2.sp else 3.2.sp,
                    )
                )
            }
        }
    }
}

@Composable
private fun RuneSeal(sealSize: Dp) {
    val cornerRadius = if (sealSize <= 44.dp) 12.dp else 18.dp
    val padding = if (sealSize <= 44.dp) 7.dp else 10.dp

    Box(
        modifier = Modifier
            .size(sealSize)
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(
                    colors = listOf(RuneBlack, RuneBlackSoft),
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            )
            .border(1.dp, RunePurple.copy(alpha = 0.45f), RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val framePath = Path().apply {
                moveTo(size.width * 0.50f, size.height * 0.05f)
                lineTo(size.width * 0.86f, size.height * 0.24f)
                lineTo(size.width * 0.86f, size.height * 0.76f)
                lineTo(size.width * 0.50f, size.height * 0.95f)
                lineTo(size.width * 0.14f, size.height * 0.76f)
                lineTo(size.width * 0.14f, size.height * 0.24f)
                close()
            }
            val glyphPath = Path().apply {
                moveTo(size.width * 0.57f, size.height * 0.16f)
                lineTo(size.width * 0.38f, size.height * 0.40f)
                lineTo(size.width * 0.55f, size.height * 0.40f)
                lineTo(size.width * 0.40f, size.height * 0.80f)

                moveTo(size.width * 0.55f, size.height * 0.40f)
                lineTo(size.width * 0.73f, size.height * 0.56f)

                moveTo(size.width * 0.34f, size.height * 0.56f)
                lineTo(size.width * 0.66f, size.height * 0.56f)
            }

            val frameStroke = size.minDimension * 0.032f
            val strokeWidth = size.minDimension * 0.082f
            val gradient = Brush.linearGradient(
                colors = listOf(RunePurpleSoft, RunePurple, RunePurpleDeep),
                start = Offset(size.width * 0.30f, size.height * 0.18f),
                end = Offset(size.width * 0.76f, size.height * 0.82f)
            )

            drawPath(
                path = framePath,
                brush = SolidColor(RunePurple.copy(alpha = 0.20f)),
                style = Stroke(width = frameStroke * 2.2f, join = StrokeJoin.Round)
            )
            drawPath(
                path = framePath,
                brush = SolidColor(RunePurple.copy(alpha = 0.65f)),
                style = Stroke(width = frameStroke, join = StrokeJoin.Round)
            )
            drawPath(
                path = glyphPath,
                color = RunePurple.copy(alpha = 0.16f),
                style = Stroke(
                    width = strokeWidth * 1.8f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
            drawPath(
                path = glyphPath,
                brush = gradient,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
            drawCircle(
                color = RunePurpleSoft,
                radius = size.minDimension * 0.032f,
                center = Offset(size.width * 0.72f, size.height * 0.25f)
            )
        }
    }
}