package com.music.bitchord.ui.components.expressive

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Scalloped / Multi-Lobe Flower Shape for Material 3 Expressive.
 *
 * Mathematically defines an N-lobed floral contour with smooth cubic Bezier
 * tangents between inner and outer radii.
 */
class ScallopedFlowerShape(
    val lobes: Int = 12,
    val innerRadiusRatio: Float = 0.84f,
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val path = Path()
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val outerRadius = minOf(centerX, centerY)
        val innerRadius = outerRadius * innerRadiusRatio

        if (outerRadius <= 0f) return Outline.Rectangle(Rect(0f, 0f, size.width, size.height))

        val totalPoints = lobes * 2
        val angleStep = (2.0 * PI / totalPoints).toFloat()

        val points = Array(totalPoints) { i ->
            val angle = i * angleStep - (PI / 2.0).toFloat()
            val r = if (i % 2 == 0) outerRadius else innerRadius
            Offset(
                x = centerX + r * cos(angle),
                y = centerY + r * sin(angle),
            )
        }

        path.moveTo(points[0].x, points[0].y)

        for (i in 0 until totalPoints) {
            val p0 = points[(i - 1 + totalPoints) % totalPoints]
            val p1 = points[i]
            val p2 = points[(i + 1) % totalPoints]
            val p3 = points[(i + 2) % totalPoints]

            // Catmull-Rom to Cubic Bezier conversion for organic curves
            val cp1X = p1.x + (p2.x - p0.x) / 6f
            val cp1Y = p1.y + (p2.y - p0.y) / 6f
            val cp2X = p2.x - (p3.x - p1.x) / 6f
            val cp2Y = p2.y - (p3.y - p1.y) / 6f

            path.cubicTo(cp1X, cp1Y, cp2X, cp2Y, p2.x, p2.y)
        }

        path.close()
        return Outline.Generic(path)
    }
}

/**
 * Material 3 Expressive Squircle shape with continuous corner curvature.
 */
class SquircleShape(val cornerRadius: Dp = 26.dp) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val px = with(density) { cornerRadius.toPx() }.coerceAtMost(minOf(size.width, size.height) / 2f)
        val path = Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    rect = Rect(0f, 0f, size.width, size.height),
                    topLeft = androidx.compose.ui.geometry.CornerRadius(px, px),
                    topRight = androidx.compose.ui.geometry.CornerRadius(px, px),
                    bottomRight = androidx.compose.ui.geometry.CornerRadius(px, px),
                    bottomLeft = androidx.compose.ui.geometry.CornerRadius(px, px),
                ),
            )
        }
        return Outline.Generic(path)
    }
}

/**
 * Flagship Material 3 Expressive Scalloped Floating Action Button.
 *
 * Uses the scalloped floral shape with a warm dynamic tonal gradient, subtle
 * rotating ambient glow, and tactile squish physics.
 */
@Composable
fun ScallopedFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Rounded.PlayArrow,
    contentDescription: String? = null,
    size: Dp = 68.dp,
    iconSize: Dp? = null,
    containerColor: Color = Color(0xFFFA586A),
    contentColor: Color = Color.White,
    ambientRotation: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = remember { ScallopedFlowerShape(lobes = 12, innerRadiusRatio = 0.85f) }

    val infiniteTransition = rememberInfiniteTransition(label = "scallopedRotation")
    val animatedRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "fabSpin",
    )
    val rotation = if (ambientRotation) animatedRotation else 0f

    Box(
        modifier = modifier
            .size(size)
            .shadow(16.dp, shape, spotColor = containerColor.copy(alpha = 0.5f))
            .expressiveSquish(interactionSource, pressScale = 0.90f)
            .expressiveClickable(interactionSource = interactionSource, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        // Scalloped multi-lobe background with rotating gradient
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { rotationZ = rotation }
                .clip(shape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            containerColor,
                            containerColor.copy(alpha = 0.88f),
                            containerColor.copy(alpha = 0.75f),
                        ),
                    ),
                )
                .border(1.5.dp, Color.White.copy(alpha = 0.25f), shape),
        )

        // Center Icon
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(iconSize ?: (size * 0.44f)),
        )
    }
}
