package com.music.bitchord.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Swipe
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Watch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.bitchord.ui.components.expressive.ScallopedFlowerShape
import kotlinx.coroutines.launch

data class TipItem(
    val title: String,
    val category: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
)

/**
 * Interactive Tips & Tricks Sheet for Hush 1.0.
 *
 * Educates listeners on Liquid Glass tuning, procedural progress lines,
 * dynamic stretchy lyrics, Song Mixer presets, AI playlist maker,
 * gesture navigation, and Wear OS companion features.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipsAndTricksSheet(
    onDismiss: () -> Unit,
    onOpenAppearance: (() -> Unit)? = null,
    onOpenMixer: (() -> Unit)? = null,
    onOpenAiMaker: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val tips = listOf(
        TipItem(
            title = "High-Sensitivity Liquid Glass",
            category = "APPEARANCE",
            description = "In Settings -> Appearance, adjust Depth Effect, Transparency, Refraction, and Blur Percentage to customize your signature frosted liquid glass dock.",
            icon = Icons.Rounded.Palette,
            accentColor = Color(0xFF38BDF8),
            actionLabel = onOpenAppearance?.let { "Tune Glass" },
            onAction = onOpenAppearance,
        ),
        TipItem(
            title = "Procedural 120 FPS Seekbars",
            category = "PLAYER STYLING",
            description = "Material UI mode features four procedural progress line styles: Wavy, Straight, Dotted, and Pulse. Switch styles anytime in Player Settings.",
            icon = Icons.Rounded.GraphicEq,
            accentColor = Color(0xFFA78BFA),
        ),
        TipItem(
            title = "Dynamic Stretchy Lyrics",
            category = "NOW PLAYING",
            description = "Tap the lyrics dock button or artwork to open Google Dialer caller-card styled synced lyrics that smoothly stretch and adapt to your album art colors.",
            icon = Icons.Rounded.MusicNote,
            accentColor = Color(0xFFF472B6),
        ),
        TipItem(
            title = "Song Mixer & Remix Studio",
            category = "AUDIO FX",
            description = "Create Slowed + Reverb, Nightcore, or Vaporwave remixes in real-time with playback tempo (0.5x–1.5x), pitch shifting, and dual-track crossfade sliders.",
            icon = Icons.Rounded.Tune,
            accentColor = Color(0xFF34D399),
            actionLabel = onOpenMixer?.let { "Open Mixer" },
            onAction = onOpenMixer,
        ),
        TipItem(
            title = "AI Music Maker",
            category = "DISCOVERY",
            description = "Tap the top-left AI sparkle button or long-press the Explore tab to generate bespoke 20+ track playlists tailored to your mood, activity, or custom prompts.",
            icon = Icons.Rounded.AutoAwesome,
            accentColor = Color(0xFFFBBF24),
            actionLabel = onOpenAiMaker?.let { "Try AI Maker" },
            onAction = onOpenAiMaker,
        ),
        TipItem(
            title = "Fluid Gesture Navigation",
            category = "SHORTCUTS",
            description = "Swipe down anywhere on the mini-player or player sheet to dismiss. Swipe horizontally to skip tracks. Long-press any song for instant quick actions.",
            icon = Icons.Rounded.Swipe,
            accentColor = Color(0xFF60A5FA),
        ),
        TipItem(
            title = "Wear OS Watch Companion",
            category = "CONNECTIVITY",
            description = "Install Hush on your Wear OS watch to control playback from your wrist, adjust volume via the crown, and stream audio directly to your watch speaker.",
            icon = Icons.Rounded.Watch,
            accentColor = Color(0xFFFB7185),
        ),
        TipItem(
            title = "Audiophile Bit-Perfect Output",
            category = "HIGH-RES AUDIO",
            description = "Plug in an external USB DAC and enable Bit-Perfect Output in Audio Settings to bypass Android resampling and stream pure lossless audio.",
            icon = Icons.Rounded.Speed,
            accentColor = Color(0xFF818CF8),
        ),
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 6.dp,
        modifier = modifier,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(ScallopedFlowerShape(lobes = 8, innerRadiusRatio = 0.82f))
                                .background(Color(0xFFF59E0B)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lightbulb,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Column {
                            Text(
                                text = "Tips & Tricks",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Pro workflows & hidden features in Hush 1.0",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Tip Cards
            items(tips) { tip ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                            RoundedCornerShape(20.dp),
                        )
                        .padding(16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(tip.accentColor.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = tip.icon,
                                        contentDescription = null,
                                        tint = tip.accentColor,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                                Column {
                                    Text(
                                        text = tip.category,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp,
                                        ),
                                        color = tip.accentColor,
                                    )
                                    Text(
                                        text = tip.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }

                            if (tip.actionLabel != null && tip.onAction != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(percent = 50))
                                        .background(tip.accentColor.copy(alpha = 0.15f))
                                        .clickable {
                                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                                onDismiss()
                                                tip.onAction.invoke()
                                            }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                ) {
                                    Text(
                                        text = tip.actionLabel,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = tip.accentColor,
                                    )
                                }
                            }
                        }

                        Text(
                            text = tip.description,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
