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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Swipe
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.bitchord.ui.components.HushCenteredCard
import dev.chrisbanes.haze.HazeState

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
 * Centered Frosted Tips & Tricks Card highlighting Hush workflows & hidden gestures.
 */
@Composable
fun TipsAndTricksSheet(
    onDismiss: () -> Unit,
    hazeState: HazeState? = null,
    onOpenAppearance: (() -> Unit)? = null,
    onOpenMixer: (() -> Unit)? = null,
    onOpenAiMaker: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val tips = listOf(
        TipItem(
            title = "Cover Tap Lyrics & Hush Cards",
            category = "NOW PLAYING",
            description = "Tap album art in Material UI mode to flip smoothly into synced lyrics, pinch (< 0.88x) to return to the artwork, or tap Share to style a Spotify/Apple-inspired Hush Lyrics Picture with custom album shapes.",
            icon = Icons.Rounded.MusicNote,
            accentColor = Color(0xFFF472B6),
        ),
        TipItem(
            title = "Remix Studio & 2-Song Mix",
            category = "AUDIO FX",
            description = "Open Remix Studio for Slowed + Reverb, Nightcore, or Vaporwave FX, layer a second live song simultaneously on Deck B, or save your mix as a WAV file locally. Effects auto-reset when the next song plays.",
            icon = Icons.Rounded.Tune,
            accentColor = Color(0xFF34D399),
            actionLabel = onOpenMixer?.let { "Open Studio" },
            onAction = onOpenMixer,
        ),
        TipItem(
            title = "Playlist Maker",
            category = "DISCOVERY",
            description = "Tap the Playlist Maker button to curate bespoke playlists tailored to exact artists, moods, or track counts. The button fades out softly whenever you search.",
            icon = Icons.Rounded.AutoAwesome,
            accentColor = Color(0xFFFBBF24),
            actionLabel = onOpenAiMaker?.let { "Open Maker" },
            onAction = onOpenAiMaker,
        ),
        TipItem(
            title = "StandBy Display Mode",
            category = "HORIZON CLOCK",
            description = "Rotate your phone sideways while docked or charging to enter Apple StandBy-style Display Mode with a giant live clock and ambient rotating artwork.",
            icon = Icons.Rounded.Schedule,
            accentColor = Color(0xFF38BDF8),
        ),
        TipItem(
            title = "Save Tracks & Playlists Locally",
            category = "OFFLINE",
            description = "Open any 3-dot track or playlist menu and choose Save file locally to export directly into your public Music/Hush Music folder.",
            icon = Icons.Rounded.Download,
            accentColor = Color(0xFFA78BFA),
        ),
        TipItem(
            title = "Expressive Material UI & Shapes",
            category = "APPEARANCE",
            description = "Customize album cover geometry with M3E Clovers, Sunny, Scallops, and Wavy/Pulse/Dotted seekbars from Appearance & Edit Controls.",
            icon = Icons.Rounded.Palette,
            accentColor = Color(0xFF60A5FA),
            actionLabel = onOpenAppearance?.let { "Customize" },
            onAction = onOpenAppearance,
        ),
        TipItem(
            title = "Fluid Gestures & Wear OS",
            category = "SHORTCUTS",
            description = "Swipe sideways to skip tracks, swipe down to dismiss the player, or control playback from the crown on Wear OS.",
            icon = Icons.Rounded.Swipe,
            accentColor = Color(0xFFFB7185),
        ),
        TipItem(
            title = "Audiophile Bit-Perfect Output",
            category = "HIGH-RES AUDIO",
            description = "Connect a USB DAC and enable Bit-Perfect Output in Audio Settings to bypass Android resampling for pure lossless PCM.",
            icon = Icons.Rounded.Speed,
            accentColor = Color(0xFF818CF8),
        ),
    )

    HushCenteredCard(
        onDismiss = onDismiss,
        hazeState = hazeState,
        title = "Tips & Tricks",
        subtitle = "Hush Workflows & Gestures",
        maxWidth = 440.dp,
        maxHeight = 640.dp,
        modifier = modifier,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(tips) { tip ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.78f))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                            RoundedCornerShape(18.dp),
                        )
                        .padding(14.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                modifier = Modifier.weight(1f, fill = false),
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
                                Column(modifier = Modifier.padding(end = 8.dp)) {
                                    Text(
                                        text = tip.category,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.9.sp,
                                        ),
                                        color = tip.accentColor,
                                    )
                                    Text(
                                        text = tip.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }

                            if (tip.actionLabel != null && tip.onAction != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(percent = 50))
                                        .background(tip.accentColor.copy(alpha = 0.16f))
                                        .clickable {
                                            onDismiss()
                                            tip.onAction.invoke()
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                ) {
                                    Text(
                                        text = tip.actionLabel,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = tip.accentColor,
                                        maxLines = 1,
                                        softWrap = false,
                                    )
                                }
                            }
                        }

                        Text(
                            text = tip.description,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(4.dp)) }
        }
    }
}
