package com.music.bitchord.ui.components.expressive

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset

/**
 * Material 3 Expressive (M3E) Motion and Physics System.
 *
 * Implements Google's Material 3 Expressive spring physics specifications:
 * - Spatial and shape transformations: Medium bouncy damping with medium-low stiffness
 * - Tactile press and micro-interactions: Low bouncy damping with snappy medium stiffness
 * - Morphing & overshoot: Custom spring profiles for squish and release recoil
 */
object ExpressiveMotion {

    /** M3E standard expressive easing curve */
    val EmphasizedEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val EmphasizedDecelerateEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val EmphasizedAccelerateEasing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)

    /** Spatial & Shape Transitions: bouncy spring for layout morphing and expanded dialogs */
    val SpatialSpring: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )

    val SpatialDpSpring: SpringSpec<Dp> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )

    val SpatialOffsetSpring: SpringSpec<IntOffset> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )

    /** Tactile Press & Micro-Bounces: snappy tactile spring for button down states */
    val TactilePressSpring: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium,
    )

    val TactilePressDpSpring: SpringSpec<Dp> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium,
    )

    /** Overshoot Spring: used on click release for tactile overshoot bounce (1.03 -> 1.0) */
    val BouncyOvershootSpring: SpringSpec<Float> = spring(
        dampingRatio = 0.55f,
        stiffness = 650f,
    )

    /** Corner Morphing Spring: smooth bouncy transition for corner radius changes */
    val CornerMorphSpring: SpringSpec<Dp> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = 500f,
    )

    /** Seekbar Wave flattening and expanding spring */
    val WavyAmplitudeSpring: SpringSpec<Float> = spring(
        dampingRatio = 0.78f,
        stiffness = 380f,
    )

    val WavyAmplitudeDpSpring: SpringSpec<Dp> = spring(
        dampingRatio = 0.78f,
        stiffness = 380f,
    )

    /** Fast spring for color transitions */
    val FastTween = tween<Float>(durationMillis = 240, easing = EmphasizedDecelerateEasing)
}
