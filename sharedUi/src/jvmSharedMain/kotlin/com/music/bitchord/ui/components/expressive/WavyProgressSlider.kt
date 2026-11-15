package com.music.bitchord.ui.components.expressive

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/**
 * Material 3 Expressive (M3E) Wavy Progress Slider.
 *
 * Features:
 * - Active track draws an oscillating sine wave during playback:
 *   y = centerY + amplitude * sin(2 * PI * (x / wavelength) + phase)
 * - Smoothly flattens to a straight line when playback is paused via [ExpressiveMotion.WavyAmplitudeSpring].
 * - Continuous smooth phase animation using an infinite transition.
 * - Inactive track draws a sleek resting line.
 * - Tactile vertical stadium-pill thumb knob (4.dp × 28.dp) that springs in size on touch.
 * - Unified touch & drag gesture handling with generous touch target bounds.
 */
@Composable
fun WavyProgressSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    onValueChangeFinished: (() -> Unit)? = null,
    isPlaying: Boolean = true,
    activeColor: Color = Color.White.copy(alpha = 0.95f),
    inactiveColor: Color = Color.White.copy(alpha = 0.25f),
    thumbColor: Color = Color.White,
    wavelength: Dp = 26.dp,
    waveAmplitude: Dp = 4.5.dp,
    trackStrokeWidth: Dp = 4.dp,
    thumbWidth: Dp = 4.dp,
    thumbHeight: Dp = 26.dp,
) {
    var isDragging by remember { mutableStateOf(false) }

    // Wave phase animation: runs continuously when playing
    val infiniteTransition = rememberInfiniteTransition(label = "waveTransition")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "wavePhase",
    )

    // Smoothly flatten amplitude to 0 when paused or dragged
    val targetAmplitude = if (isPlaying && !isDragging) waveAmplitude else 0.dp
    val animatedAmplitude by animateDpAsState(
        targetValue = targetAmplitude,
        animationSpec = ExpressiveMotion.WavyAmplitudeDpSpring,
        label = "waveAmplitude",
    )

    // Tactile thumb squish/expand on touch
    val currentThumbWidth by animateDpAsState(
        targetValue = if (isDragging) thumbWidth + 2.5.dp else thumbWidth,
        animationSpec = ExpressiveMotion.TactilePressDpSpring,
        label = "thumbWidth",
    )
    val currentThumbHeight by animateDpAsState(
        targetValue = if (isDragging) thumbHeight + 6.dp else thumbHeight,
        animationSpec = ExpressiveMotion.TactilePressDpSpring,
        label = "thumbHeight",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp) // Generous touch target
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    isDragging = true
                    onValueChange((down.position.x / size.width).coerceIn(0f, 1f))

                    while (true) {
                        val event = awaitPointerEvent()
                        val pointer = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!pointer.pressed) {
                            pointer.consume()
                            break
                        }
                        if (pointer.positionChanged()) {
                            onValueChange((pointer.position.x / size.width).coerceIn(0f, 1f))
                            pointer.consume()
                        }
                    }

                    isDragging = false
                    onValueChangeFinished?.invoke()
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(thumbHeight + 10.dp),
        ) {
            val width = size.width
            val centerY = size.height / 2f
            val thumbX = (width * value.coerceIn(0f, 1f)).coerceIn(0f, width)

            val ampPx = animatedAmplitude.toPx()
            val strokePx = trackStrokeWidth.toPx()
            val waveLenPx = wavelength.toPx()
            val twoPi = (2.0 * PI).toFloat()

            // 1. Inactive Track (Right of thumb)
            if (thumbX < width) {
                drawLine(
                    color = inactiveColor,
                    start = Offset(thumbX, centerY),
                    end = Offset(width, centerY),
                    strokeWidth = strokePx,
                    cap = StrokeCap.Round,
                )
            }

            // 2. Active Track (Left of thumb)
            if (thumbX > 0f) {
                if (ampPx <= 0.05f) {
                    // Straight line when flattened
                    drawLine(
                        color = activeColor,
                        start = Offset(0f, centerY),
                        end = Offset(thumbX, centerY),
                        strokeWidth = strokePx,
                        cap = StrokeCap.Round,
                    )
                } else {
                    // Wavy Sine Path
                    val path = Path()
                    path.moveTo(0f, centerY)
                    val stepPx = 3f
                    var currentX = 0f

                    while (currentX < thumbX) {
                        val y = centerY + ampPx * sin(twoPi * (currentX / waveLenPx) + phase)
                        path.lineTo(currentX, y)
                        currentX += stepPx
                    }

                    // Smooth transition to thumb center
                    val finalY = centerY + ampPx * sin(twoPi * (thumbX / waveLenPx) + phase)
                    path.lineTo(thumbX, finalY)

                    drawPath(
                        path = path,
                        color = activeColor,
                        style = Stroke(
                            width = strokePx,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        ),
                    )
                }
            }

            // 3. Tactile Thumb Stadium Pill
            val tw = currentThumbWidth.toPx()
            val th = currentThumbHeight.toPx()
            val thumbLeft = (thumbX - tw / 2f).coerceIn(0f, width - tw)
            val thumbTop = centerY - th / 2f

            drawRoundRect(
                color = thumbColor,
                topLeft = Offset(thumbLeft, thumbTop),
                size = Size(tw, th),
                cornerRadius = CornerRadius(tw / 2f, tw / 2f),
            )
        }
    }
}
