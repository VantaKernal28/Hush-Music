package com.music.bitchord.ui.player

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.music.bitchord.data.listentogether.PartyMember
import com.music.bitchord.ui.haptics.Haptic
import com.music.bitchord.ui.haptics.rememberHaptics
import dev.chrisbanes.haze.HazeState

/**
 * Spotify-inspired Hush Jam Bottom Sheet:
 * - Live session indicator with pulsing aura
 * - Shareable Jam Code & Session QR code
 * - Connected listener avatar stack
 * - Host queue & sync controls
 */
@Composable
internal fun HushJamSheet(
    hazeState: HazeState,
    onDismiss: () -> Unit,
    onManageParty: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by PlayerPlatform.host.party.collectAsStateWithLifecycle()
    val haptics = rememberHaptics()
    var guestsCanControl by remember { mutableStateOf(!state.controlsLocked) }
    val isHost = state.you?.isHost ?: true
    val sessionCode = state.code ?: "JAM-7721"

    val infiniteTransition = rememberInfiniteTransition(label = "jamPulse")
    val livePulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "livePulse",
    )

    PlayerDrawer(
        hazeState = hazeState,
        title = "Hush Jam",
        onDismiss = onDismiss,
        modifier = modifier,
    ) {
        // --- Live Header Status Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFFFF6E40).copy(alpha = 0.22f),
                            Color(0xFFB388FF).copy(alpha = 0.16f),
                        )
                    )
                )
                .border(
                    1.dp,
                    Color(0xFFFF6E40).copy(alpha = 0.35f),
                    RoundedCornerShape(16.dp),
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .graphicsLayer {
                        scaleX = livePulseScale
                        scaleY = livePulseScale
                    }
                    .clip(CircleShape)
                    .background(Color(0xFFFF6E40)),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (state.members.isNotEmpty()) "LIVE SESSION" else "START A JAM",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFAB91),
                letterSpacing = 1.2.sp,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${state.members.size.coerceAtLeast(1)} listening",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.85f),
            )
        }

        Spacer(Modifier.height(14.dp))

        // --- Session Invite Code & QR Tile ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // QR Code Placeholder / Icon Tile
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.QrCode2,
                    contentDescription = "Jam Session QR",
                    tint = Color.Black,
                    modifier = Modifier.size(48.dp),
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = "Room Invite Code",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f),
                )
                Text(
                    text = sessionCode,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.5.sp,
                )
            }

            IconButton(
                onClick = {
                    haptics.play(Haptic.Tap)
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.10f)),
            ) {
                Icon(
                    imageVector = Icons.Rounded.ContentCopy,
                    contentDescription = "Copy Code",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }

            Spacer(Modifier.width(6.dp))

            IconButton(
                onClick = {
                    haptics.play(Haptic.Tap)
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF6E40)),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Share,
                    contentDescription = "Share Invite",
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // --- Active Listeners Stack ---
        Text(
            text = "CONNECTED LISTENERS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.55f),
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 2.dp, bottom = 8.dp),
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            val membersList = if (state.members.isNotEmpty()) {
                state.members
            } else {
                listOf(
                    PartyMember(
                        memberId = "you",
                        displayName = state.you?.displayName ?: "You (Host)",
                        avatarUrl = state.you?.avatarUrl,
                        isHost = true,
                        connected = true,
                    )
                )
            }

            membersList.forEach { member ->
                val isYou = member.memberId == state.you?.memberId || member.memberId == "you"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = if (isYou) 0.12f else 0.05f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (member.isHost) Color(0xFFFF6E40).copy(alpha = 0.35f)
                                else Color.White.copy(alpha = 0.12f)
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (member.avatarUrl != null) {
                            AsyncImage(
                                model = member.avatarUrl,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp).clip(CircleShape),
                            )
                        } else {
                            Text(
                                text = member.displayName.take(1).uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (member.isHost) Color(0xFFFFAB91) else Color.White,
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(Modifier.weight(1f)) {
                        Text(
                            text = member.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isYou) FontWeight.Bold else FontWeight.Medium,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = if (member.isHost) "Session Host" else "Listener",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (member.isHost) Color(0xFFFFAB91) else Color.White.copy(alpha = 0.55f),
                        )
                    }

                    // Green dot for active sync
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E676)),
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // --- Host Queue & Playback Permissions ---
        if (isHost) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Guests can add & control queue",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                    )
                    Text(
                        text = "Allow listeners to reorder or add songs",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.55f),
                    )
                }
                Switch(
                    checked = guestsCanControl,
                    onCheckedChange = {
                        guestsCanControl = it
                        haptics.play(Haptic.Tap)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = Color(0xFFFF6E40),
                        uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                        uncheckedTrackColor = Color.White.copy(alpha = 0.15f),
                    ),
                )
            }

            Spacer(Modifier.height(10.dp))
        }

        // --- Sync Playback Button ---
        Button(
            onClick = {
                haptics.play(Haptic.Select)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.12f),
                contentColor = Color.White,
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(46.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Sync,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Resync Playback to Host",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(Modifier.height(8.dp))

        // --- Full Settings & Leave Option ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onDismiss()
                    onManageParty()
                }
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Manage party settings or leave Jam",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.6f),
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
