package com.music.bitchord.ui.components.liquid

import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceAtMost
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tanh

/* ------------------------------------------------------------------ */
/* Gooey merge (Alpha snap threshold for organic liquid morphing)      */
/* ------------------------------------------------------------------ */

private const val ThresholdSlope = 10f
private const val ThresholdMidpoint255 = 0.5f * 255f

private val ThresholdMatrix = floatArrayOf(
    1f, 0f, 0f, 0f, 0f,
    0f, 1f, 0f, 0f, 0f,
    0f, 0f, 1f, 0f, 0f,
    0f, 0f, 0f, ThresholdSlope, -ThresholdSlope * ThresholdMidpoint255,
)

private val gooeyEffectCache = HashMap<Int, androidx.compose.ui.graphics.RenderEffect>()

/**
 * Blur + steep alpha threshold: overlapping pieces of glass surfaces bleed into each other
 * and snap back to a single organic crisp contour. Android 12+ (RenderEffect).
 */
fun Modifier.gooey(blurRadiusPx: () -> Float): Modifier {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return this
    return graphicsLayer {
        val radius = blurRadiusPx().toInt()
        if (radius > 0) {
            compositingStrategy = CompositingStrategy.Offscreen
            renderEffect = gooeyEffectCache.getOrPut(radius) {
                RenderEffect.createColorFilterEffect(
                    ColorMatrixColorFilter(ThresholdMatrix),
                    RenderEffect.createBlurEffect(radius.toFloat(), radius.toFloat(), Shader.TileMode.DECAL),
                ).asComposeRenderEffect()
            }
        } else {
            compositingStrategy = CompositingStrategy.Auto
            renderEffect = null
        }
    }
}

/* ------------------------------------------------------------------ */
/* Press sheen & interactive highlight gesture inspection              */
/* ------------------------------------------------------------------ */

suspend fun PointerInputScope.inspectDragGestures(
    onDragStart: (down: PointerInputChange) -> Unit = {},
    onDragEnd: (change: PointerInputChange) -> Unit = {},
    onDragCancel: () -> Unit = {},
    onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit,
) {
    awaitEachGesture {
        val initialDown = awaitFirstDown(false, PointerEventPass.Initial)
        val down = awaitFirstDown(false)
        val drag = initialDown

        onDragStart(down)
        onDrag(drag, Offset.Zero)
        val upEvent = drag(
            pointerId = drag.id,
            onDrag = { onDrag(it, it.positionChange()) },
        )
        if (upEvent == null) {
            onDragCancel()
        } else {
            onDragEnd(upEvent)
        }
    }
}

private suspend inline fun AwaitPointerEventScope.drag(
    pointerId: PointerId,
    onDrag: (PointerInputChange) -> Unit,
): PointerInputChange? {
    val isPointerUp = currentEvent.changes.fastFirstOrNull { it.id == pointerId }?.pressed != true
    if (isPointerUp) return null
    var pointer = pointerId
    while (true) {
        val change = awaitDragOrUp(pointer) ?: return null
        if (change.isConsumed) return null
        if (change.changedToUpIgnoreConsumed()) return change
        onDrag(change)
        pointer = change.id
    }
}

private suspend inline fun AwaitPointerEventScope.awaitDragOrUp(
    pointerId: PointerId,
): PointerInputChange? {
    var pointer = pointerId
    while (true) {
        val event = awaitPointerEvent()
        val dragEvent = event.changes.fastFirstOrNull { it.id == pointer } ?: return null
        if (dragEvent.changedToUpIgnoreConsumed()) {
            val otherDown = event.changes.fastFirstOrNull { it.pressed }
            if (otherDown == null) {
                return dragEvent
            } else {
                pointer = otherDown.id
            }
        } else {
            val hasDragged = dragEvent.previousPosition != dragEvent.position
            if (hasDragged) return dragEvent
        }
    }
}

/**
 * A press-driven additive sheen that follows the user's touch.
 */
class InteractiveHighlight(
    private val animationScope: CoroutineScope,
    private val position: (size: Size, offset: Offset) -> Offset = { _, offset -> offset },
    private val radiusScale: Float = 1.5f,
    private val sheenAlpha: Float = 0.08f,
    private val spotAlpha: Float = 0.15f,
) {
    private val pressProgressAnimationSpec = spring(0.5f, 300f, 0.001f)
    private val positionAnimationSpec = spring(0.5f, 300f, Offset.VisibilityThreshold)
    private val pressProgressAnimation = Animatable(0f, 0.001f)
    private val positionAnimation = Animatable(Offset.Zero, Offset.VectorConverter, Offset.VisibilityThreshold)
    private var startPosition = Offset.Zero

    val pressProgress: Float get() = pressProgressAnimation.value
    val offset: Offset get() = positionAnimation.value - startPosition

    val modifier: Modifier = Modifier.drawWithContent {
        val progress = pressProgressAnimation.value
        if (progress > 0f) {
            drawRect(color = Color.White.copy(alpha = sheenAlpha * progress), blendMode = BlendMode.Plus)
            val pos = position(size, positionAnimation.value)
            val radius = (size.minDimension * radiusScale).coerceAtLeast(1f)
            val spot = Color.White.copy(alpha = spotAlpha * progress)
            drawRect(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(0.0f to spot, 0.5f to spot, 1.0f to Color.White.copy(alpha = 0f)),
                    center = Offset(pos.x.coerceIn(0f, size.width), pos.y.coerceIn(0f, size.height)),
                    radius = radius,
                ),
                blendMode = BlendMode.Plus,
            )
        }
        drawContent()
    }

    val gestureModifier: Modifier = Modifier.pointerInput(animationScope) {
        inspectDragGestures(
            onDragStart = { down ->
                startPosition = down.position
                animationScope.launch {
                    launch { pressProgressAnimation.animateTo(1f, pressProgressAnimationSpec) }
                    launch { positionAnimation.snapTo(startPosition) }
                }
            },
            onDragEnd = { release() },
            onDragCancel = { release() },
        ) { change, _ ->
            animationScope.launch { positionAnimation.snapTo(change.position) }
        }
    }

    private fun release() {
        animationScope.launch {
            launch { pressProgressAnimation.animateTo(0f, pressProgressAnimationSpec) }
            launch { positionAnimation.animateTo(startPosition, positionAnimationSpec) }
        }
    }
}

/**
 * Organic elastic stretch modifier: swells and leans towards drag gestures using hyperbolic tangent curves.
 */
fun elasticStretchLayerBlock(
    interactiveHighlight: InteractiveHighlight,
    magnitude: Float = 1f,
    frame: (() -> Size)? = null,
): GraphicsLayerScope.() -> Unit = {
    val body = frame?.invoke() ?: size
    val width = body.width
    val height = body.height
    if (width > 0f && height > 0f) {
        val progress = interactiveHighlight.pressProgress
        val scale = lerp(1f, 1f + magnitude * 4f.dp.toPx() / height, progress)
        val maxOffset = body.minDimension
        val initialDerivative = 0.05f
        val offset = interactiveHighlight.offset
        translationX = magnitude * maxOffset * tanh(initialDerivative * offset.x / maxOffset)
        translationY = magnitude * maxOffset * tanh(initialDerivative * offset.y / maxOffset)
        val maxDragScale = magnitude * 4f.dp.toPx() / height
        val offsetAngle = atan2(offset.y, offset.x)
        scaleX = scale + maxDragScale * abs(cos(offsetAngle) * offset.x / body.maxDimension) *
            (width / height).fastCoerceAtMost(1f)
        scaleY = scale + maxDragScale * abs(sin(offsetAngle) * offset.y / body.maxDimension) *
            (height / width).fastCoerceAtMost(1f)
    }
}
