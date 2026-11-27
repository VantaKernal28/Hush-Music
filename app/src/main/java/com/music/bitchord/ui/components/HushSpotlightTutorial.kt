package com.music.bitchord.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.music.bitchord.ui.components.expressive.ScallopedFlowerShape
import dev.chrisbanes.haze.HazeState

private data class SpotlightStep(
    val title: String,
    val subtitle: String,
    val description: String,
    val gestureHint: String,
    val icon: ImageVector,
    val accent: Color,
    // Normalized cutout center (0f..1f) and size ratio relative to container width/height
    val focusX: Float,
    val focusY: Float,
    val focusWFraction: Float,
    val focusHFraction: Float,
    val cornerDp: Float,
    val cardAtBottomHalf: Boolean,
)

/**
 * Interactive 4-step Spotlight Blur Tutorial shown once after onboarding or
 * whenever re-launched via "Restart Tutorial" in Settings.
 */
@Composable
fun HushSpotlightTutorial(
    hazeState: HazeState?,
    onComplete: () -> Unit,
    onOpenTips: (() -> Unit)? = null,
) {
    BackHandler(enabled = true) { onComplete() }

    val primary = MaterialTheme.colorScheme.primary
    val darkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    val steps = remember(primary) {
        listOf(
            SpotlightStep(
                title = "Playlist Maker",
                subtitle = "Exact Custom Playlists",
                description = "Tap the glowing Playlist Maker pill to curate exact songs, artists, or custom track counts in seconds. It softly fades away when you open Search.",
                gestureHint = "Top-left pill on Home · Fades out in Search",
                icon = Icons.Rounded.AutoAwesome,
                accent = Color(0xFFF59E0B),
                focusX = 0.16f,
                focusY = 0.10f,
                focusWFraction = 0.28f,
                focusHFraction = 0.08f,
                cornerDp = 28f,
                cardAtBottomHalf = true,
            ),
            SpotlightStep(
                title = "Tap Cover for Lyrics & Cards",
                subtitle = "Smooth Flip, Pinch Back & Hush Cards",
                description = "Tap any album cover in the M3E player to glide into synced lyrics, pinch to return to cover art, or export a custom Spotify/Apple-style Hush Lyrics Picture.",
                gestureHint = "Tap Cover -> Lyrics · Pinch (< 0.88x) -> Artwork",
                icon = Icons.Rounded.Lyrics,
                accent = primary,
                focusX = 0.50f,
                focusY = 0.85f,
                focusWFraction = 0.84f,
                focusHFraction = 0.11f,
                cornerDp = 26f,
                cardAtBottomHalf = false,
            ),
            SpotlightStep(
                title = "Remix Studio & 2-Song Mix",
                subtitle = "Live Speed, Pitch, EQ & Local WAV Export",
                description = "Open Remix Studio from player controls to shift tempo and pitch, mix a second live track on Deck B, or save your tracks, playlists, and remixes straight to Music/Hush Music.",
                gestureHint = "Auto-resets when the next song starts",
                icon = Icons.Rounded.Tune,
                accent = Color(0xFF10B981),
                focusX = 0.50f,
                focusY = 0.34f,
                focusWFraction = 0.72f,
                focusHFraction = 0.24f,
                cornerDp = 32f,
                cardAtBottomHalf = true,
            ),
            SpotlightStep(
                title = "StandBy Display & Settings",
                subtitle = "Horizon Desk Clock & Tour Replay",
                description = "Rotate your phone sideways to enter Apple StandBy-style Display Mode with live clock and ambient cover. Revisit Tips & Tricks or Restart Tutorial anytime from Settings.",
                gestureHint = "Rotate to Landscape · Settings -> Restart Tutorial",
                icon = Icons.Rounded.Schedule,
                accent = Color(0xFF38BDF8),
                focusX = 0.86f,
                focusY = 0.90f,
                focusWFraction = 0.20f,
                focusHFraction = 0.08f,
                cornerDp = 24f,
                cardAtBottomHalf = false,
            ),
        )
    }

    var currentStep by remember { mutableIntStateOf(0) }
    val step = steps[currentStep.coerceIn(0, steps.lastIndex)]

    val animFx by animateFloatAsState(step.focusX, tween(340, easing = FastOutSlowInEasing), label = "fx")
    val animFy by animateFloatAsState(step.focusY, tween(340, easing = FastOutSlowInEasing), label = "fy")
    val animFw by animateFloatAsState(step.focusWFraction, tween(340, easing = FastOutSlowInEasing), label = "fw")
    val animFh by animateFloatAsState(step.focusHFraction, tween(340, easing = FastOutSlowInEasing), label = "fh")

    val infinite = rememberInfiniteTransition(label = "spotlightRing")
    val pulse by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ringScale",
    )

    val cardBg = if (darkTheme) Color(0xFF1B1C22).copy(alpha = 0.94f) else Color(0xFFFDFCFD).copy(alpha = 0.96f)
    val cardBorder = if (darkTheme) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.08f)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(950f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),
    ) {
        onOpenTips?.let { Unit }
        // Dimmed scrim with dynamic Spotlight Hole (CompositingStrategy.Offscreen + BlendMode.Clear)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
        ) {
            drawRect(color = Color.Black.copy(alpha = 0.72f))

            val holeW = size.width * animFw
            val holeH = size.height * animFh
            val holeX = (size.width * animFx - holeW / 2f).coerceIn(12f, size.width - holeW - 12f)
            val holeY = (size.height * animFy - holeH / 2f).coerceIn(12f, size.height - holeH - 12f)
            val r = step.cornerDp.dp.toPx()

            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(holeX, holeY),
                size = Size(holeW, holeH),
                cornerRadius = CornerRadius(r, r),
                blendMode = BlendMode.Clear,
            )

            // Pulsing glowing spotlight halo ring
            val pad = (pulse - 1f) * 64f
            drawRoundRect(
                color = step.accent.copy(alpha = 0.85f),
                topLeft = Offset(holeX - pad, holeY - pad),
                size = Size(holeW + pad * 2f, holeH + pad * 2f),
                cornerRadius = CornerRadius(r + pad * 0.5f, r + pad * 0.5f),
                style = Stroke(width = 3.dp.toPx()),
            )
        }

        // Centered Walkthrough Card (positioned smoothly so it never touches the screen bottom)
        val cardAlignment = if (step.cardAtBottomHalf) Alignment.Center else Alignment.Center
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            contentAlignment = cardAlignment,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 368.dp)
                    .fillMaxWidth()
                    .shadow(28.dp, ContinuousCornerShape(30.dp))
                    .clip(ContinuousCornerShape(30.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, ContinuousCornerShape(30.dp))
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Header: Top-left Circular X + Centered Progress Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            .clickable(onClick = onComplete),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Skip Tutorial",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        steps.indices.forEach { idx ->
                            Box(
                                modifier = Modifier
                                    .size(width = if (idx == currentStep) 20.dp else 7.dp, height = 7.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (idx == currentStep) step.accent
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
                                    ),
                            )
                        }
                    }

                    Text(
                        text = "${currentStep + 1}/${steps.size}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = step.accent,
                    )
                }

                Spacer(Modifier.height(16.dp))

                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
                    label = "spotlightStepContent",
                ) { idx ->
                    val s = steps[idx]
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(ScallopedFlowerShape(lobes = 8, innerRadiusRatio = 0.82f))
                                .background(s.accent.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = s.icon,
                                contentDescription = null,
                                tint = s.accent,
                                modifier = Modifier.size(28.dp),
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = s.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 19.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = s.subtitle,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = s.accent,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = s.description,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(s.accent.copy(alpha = 0.12f))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = s.gestureHint,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = s.accent,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (currentStep > 0) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                .clickable { currentStep = (currentStep - 1).coerceAtLeast(0) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "Back",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    } else if (onOpenTips != null && currentStep == steps.lastIndex) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                .clickable {
                                    onComplete()
                                    onOpenTips()
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "All Tips",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                .clickable(onClick = onComplete)
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "Skip",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }

                    val isLast = currentStep >= steps.lastIndex
                    Box(
                        modifier = Modifier
                            .weight(1.3f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(step.accent)
                            .clickable {
                                if (isLast) onComplete() else currentStep++
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (isLast) "Start Listening" else "Next",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}
