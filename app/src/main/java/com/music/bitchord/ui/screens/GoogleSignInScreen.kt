package com.music.bitchord.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.bitchord.R
import com.music.bitchord.sharedui.resources.Res
import com.music.bitchord.sharedui.resources.ic_google_g
import org.jetbrains.compose.resources.painterResource as composePainterResource
import com.music.bitchord.ui.icons.BitChordIcons

/**
 * High-craft dedicated Google Sign-In & YouTube Music Account Connection screen,
 * inspired by video_4 / original-3260dc0d23f4dc0d8b96391dbd76f100.mp4.
 *
 * Features:
 * - Clean high-contrast header with step/close and skip controls
 * - Dynamic kinetic floating artist/album cards drifting with spring physics
 * - Tactile Material 3 Expressive frosted glass "Sign in with Google" button
 * - Seamless integration with YouTube Music core session management
 */
@Composable
fun GoogleSignInScreen(
    onSignIn: () -> Unit,
    onSkip: () -> Unit,
    onBack: (() -> Unit)? = null,
    stepLabel: String? = null,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090A0D))
            .systemBarsPadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        // Subtle ambient radial glow behind the center stage
        Box(
            modifier = Modifier
                .size(450.dp)
                .align(Alignment.Center)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF6E40).copy(alpha = 0.12f),
                            Color(0xFF7E57C2).copy(alpha = 0.08f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 520.dp)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header: Back/Close, step label, and Skip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (onBack != null) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.10f))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), CircleShape)
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = BitChordIcons.ChevronRight,
                            contentDescription = stringResource(R.string.onboarding_back),
                            tint = Color.White,
                            modifier = Modifier
                                .size(16.dp)
                                .graphicsLayer { scaleX = -1f },
                        )
                    }
                } else {
                    Spacer(Modifier.size(38.dp))
                }

                if (stepLabel != null) {
                    Text(
                        text = stepLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.W600,
                        color = Color.White.copy(alpha = 0.50f),
                        letterSpacing = 0.5.sp,
                    )
                } else {
                    Spacer(Modifier.width(8.dp))
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable(onClick = onSkip)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_skip_sign_in),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.W600,
                        color = Color.White.copy(alpha = 0.75f),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Title & Subtitle with Google 'G' Badge
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = composePainterResource(Res.drawable.ic_google_g),
                            contentDescription = "Google",
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Connect Google",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 28.sp,
                            lineHeight = 34.sp,
                            letterSpacing = (-0.5).sp,
                        ),
                        fontWeight = FontWeight.W800,
                        color = Color.White,
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Link your account to stream your YouTube Music library, liked songs, and tailored daily mixes.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color.White.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            Spacer(Modifier.height(20.dp))

            // Center Stage: Dynamic Kinetic Floating Artist & Album Cards
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                KineticFloatingCardsStage()
            }

            Spacer(Modifier.height(16.dp))

            // Bottom Actions: Tactile Frosted Glass Sign in with Google Button
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TactileGoogleButton(
                    label = "Sign in with Google",
                    onClick = onSignIn,
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "You can still browse and stream offline without an account.",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.40f),
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

/**
 * Center stage with dynamic floating artist cards drifting with spring physics,
 * matching the motion aesthetic of video_4 / original-3260dc0d23f4dc0d8b96391dbd76f100.mp4.
 */
@Composable
private fun KineticFloatingCardsStage() {
    val infiniteTransition = rememberInfiniteTransition(label = "kineticFloating")

    val floatOffset1 by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "float1",
    )

    val floatOffset2 by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(4100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "float2",
    )

    val floatOffset3 by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 9f,
        animationSpec = infiniteRepeatable(
            animation = tween(3700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "float3",
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Card 1: Sigur Rós (Warm terracotta/orange gradient, tilted -3 deg)
        KineticCardItem(
            title = "Sigur Rós",
            category = "Post-Rock / Ambient",
            gradient = listOf(Color(0xFFD48B68), Color(0xFFB06642)),
            rotationAngle = -3.2f,
            offsetY = floatOffset1,
            iconTint = Color(0xFFFFD180),
        )

        // Card 2: Tame Impala (Vibrant electric royal sky blue, tilted +3.8 deg)
        KineticCardItem(
            title = "Tame Impala",
            category = "Psychedelic / Synth",
            gradient = listOf(Color(0xFF4A90E2), Color(0xFF2E6AB3)),
            rotationAngle = 3.6f,
            offsetY = floatOffset2,
            iconTint = Color(0xFF80D8FF),
        )

        // Card 3: Sampha (Deep plum/burgundy wine, tilted -2 deg)
        KineticCardItem(
            title = "Sampha",
            category = "Neo-Soul / Electronic",
            gradient = listOf(Color(0xFF7E2A4C), Color(0xFF4D142B)),
            rotationAngle = -2.1f,
            offsetY = floatOffset3,
            iconTint = Color(0xFFFF80AB),
        )
    }
}

@Composable
private fun KineticCardItem(
    title: String,
    category: String,
    gradient: List<Color>,
    rotationAngle: Float,
    offsetY: Float,
    iconTint: Color,
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
        label = "pressCardScale",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .height(72.dp)
            .graphicsLayer {
                rotationZ = rotationAngle
                translationY = offsetY
                scaleX = pressScale
                scaleY = pressScale
            }
            .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = gradient.first().copy(alpha = 0.45f))
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(gradient))
            .border(BorderStroke(1.2.dp, Color.White.copy(alpha = 0.35f)), RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = {},
            )
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.W800,
                fontSize = 20.sp,
                color = Color.White,
                letterSpacing = (-0.4).sp,
            )
            Text(
                text = category,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.W600,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.82f),
            )
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.20f))
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.30f)), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = null,
                tint = Color.Transparent,
                modifier = Modifier.size(0.dp),
            )
            // Glowing music emblem
            Text(
                text = "♪",
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun TactileGoogleButton(
    label: String,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.60f, stiffness = 600f),
        label = "btnScale",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(18.dp, CircleShape, spotColor = Color(0xFFFF6E40).copy(alpha = 0.35f))
            .clip(CircleShape)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color.White,
                        Color(0xFFFFF4EE),
                    ),
                ),
            )
            .border(
                BorderStroke(
                    1.5.dp,
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFFFFAB91),
                            Color(0xFFFF6E40),
                        ),
                    ),
                ),
                CircleShape,
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = composePainterResource(Res.drawable.ic_google_g),
            contentDescription = null,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontSize = 17.sp,
            fontWeight = FontWeight.W800,
            color = Color(0xFF16171B),
        )
    }
}
