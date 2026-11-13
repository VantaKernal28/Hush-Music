package com.music.bitchord.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.bitchord.ui.theme.HushSunsetGradient
import com.music.bitchord.ui.theme.BitChordTheme

/**
 * ----------------------------------------------------------------------------
 * Multi-Device Jetpack Compose Previews for Hush Music:
 * 1. Phone (390 x 844 portrait)
 * 2. Tablet (1024 x 768 adaptive split-pane layout)
 * 3. Android TV (1920 x 1080 10-foot landscape UI with focus highlights)
 * 4. Wear OS Watch (454 x 454 circular viewport with compact controls)
 * ----------------------------------------------------------------------------
 */

// ============================================================================
// 1. Phone Preview (390 x 844)
// ============================================================================
@Preview(
    name = "1. Phone - Hush Music (390x844)",
    device = "spec:width=390dp,height=844dp,dpi=460",
    showBackground = true,
    backgroundColor = 0xFF0A0A0C,
)
@Composable
fun PreviewPhoneHushMusic() {
    BitChordTheme(darkTheme = true) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0A0C))
                .padding(16.dp),
        ) {
            Column(Modifier.fillMaxSize()) {
                // Personalized Greeting Header
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "Good evening, Alex",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                        Text(
                            text = "Hush Music · Studio Lossless",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFFAB91),
                        )
                    }
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(HushSunsetGradient),
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Spotify-style Filter Chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Music", "Podcasts", "Made For You").forEachIndexed { index, filter ->
                        Box(
                            Modifier
                                .clip(CircleShape)
                                .background(if (index == 0) Color(0xFFFF6E40) else Color(0xFF1E1E24))
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = filter,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (index == 0) Color.Black else Color.White,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 2-Column Recent Grid
                Text(
                    text = "QUICK ACCESS",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Midnight City", "Starboy", "After Hours", "Resonance").chunked(2).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { title ->
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1E1E24)),
                                ) {
                                    Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            Modifier
                                                .size(52.dp)
                                                .background(HushSunsetGradient),
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                // Docked Mini-Player with Hush Jam Pill
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .clip(RoundedCornerShape(29.dp))
                        .background(Color(0xFF1E1E24))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(29.dp))
                        .padding(horizontal = 14.dp),
                ) {
                    Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(HushSunsetGradient),
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "Midnight City",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                            Text(
                                text = "M83 · Lossless 24-bit",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.6f),
                            )
                        }
                        Box(
                            Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFFF6E40).copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = "Jam · 3",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFFAB91),
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Rounded.Pause,
                            contentDescription = "Pause",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// 2. Tablet Split-Pane Preview (1024 x 768)
// ============================================================================
@Preview(
    name = "2. Tablet - Hush Music (1024x768 Split-Pane)",
    device = "spec:width=1024dp,height=768dp,dpi=240",
    showBackground = true,
    backgroundColor = 0xFF0A0A0C,
)
@Composable
fun PreviewTabletHushMusic() {
    BitChordTheme(darkTheme = true) {
        Row(
            Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0A0C)),
        ) {
            // Left Navigation Rail
            Column(
                Modifier
                    .width(240.dp)
                    .fillMaxSize()
                    .background(Color(0xFF121214))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Text(
                    text = "Hush Music",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6E40),
                )
                Spacer(Modifier.height(12.dp))
                listOf("Home", "Search", "Your Library", "Hush Jam").forEachIndexed { i, item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (i == 0) Color.White.copy(alpha = 0.1f) else Color.Transparent)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Icon(
                            imageVector = if (i == 0) Icons.Rounded.Home else if (i == 1) Icons.Rounded.Search else if (i == 2) Icons.Rounded.LibraryMusic else Icons.Rounded.Groups,
                            contentDescription = null,
                            tint = if (i == 0) Color(0xFFFF6E40) else Color.White.copy(alpha = 0.7f),
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (i == 0) FontWeight.Bold else FontWeight.Medium,
                            color = Color.White,
                        )
                    }
                }
            }

            // Right Content Area
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(24.dp),
            ) {
                Text(
                    text = "Good evening, Alex",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    listOf("Synthwave Chill", "Studio Master Hi-Res", "Discover Weekly", "Deep Focus").forEach { shelf ->
                        Box(
                            Modifier
                                .weight(1f)
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E1E24))
                                .padding(12.dp),
                        ) {
                            Column {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(HushSunsetGradient),
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = shelf,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 3. Android TV 10-Foot UI Preview (1920 x 1080)
// ============================================================================
@Preview(
    name = "3. Android TV - Hush Music (1920x1080 10-Foot UI)",
    device = "spec:width=1920dp,height=1080dp,dpi=320",
    showBackground = true,
    backgroundColor = 0xFF0A0A0C,
)
@Composable
fun PreviewAndroidTvHushMusic() {
    BitChordTheme(darkTheme = true) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0A0C))
                .padding(48.dp),
        ) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Hush Music TV",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFF6E40),
                    )
                    Text(
                        text = "Lossless 96kHz / 24-bit Output",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.6f),
                    )
                }

                Spacer(Modifier.height(32.dp))

                // Focused Shelf Card Row with 10-Foot Focus Highlight
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    listOf("Featured Station", "Hush Jam Party", "Chillout Lounge", "Acoustic Evening").forEachIndexed { index, card ->
                        val isFocused = index == 0
                        Box(
                            Modifier
                                .weight(1f)
                                .height(260.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isFocused) Color(0xFF26262E) else Color(0xFF1E1E24))
                                .border(
                                    if (isFocused) 3.dp else 1.dp,
                                    if (isFocused) Color(0xFFFF6E40) else Color.White.copy(alpha = 0.1f),
                                    RoundedCornerShape(16.dp),
                                )
                                .padding(16.dp),
                        ) {
                            Column {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(170.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(HushSunsetGradient),
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = card,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isFocused) Color(0xFFFFAB91) else Color.White,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 4. Wear OS Circular Watch Preview (454 x 454)
// ============================================================================
@Preview(
    name = "4. Wear OS Watch - Hush Music (454x454 Circle)",
    device = "id:wearos_small_round",
    showBackground = true,
    backgroundColor = 0xFF000000,
)
@Composable
fun PreviewWearOsWatchHushMusic() {
    BitChordTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .size(227.dp) // standard Compose wear round size in dp
                .clip(CircleShape)
                .background(Color.Black)
                .border(2.dp, Color(0xFFFF6E40).copy(alpha = 0.35f), CircleShape)
                .padding(18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Midnight City",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                )
                Text(
                    text = "M83 · Jam Live (3)",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = Color(0xFFFFAB91),
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Groups,
                            contentDescription = "Jam",
                            tint = Color(0xFFFF6E40),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Box(
                        Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(HushSunsetGradient),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(26.dp),
                        )
                    }
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FastForward,
                            contentDescription = "Skip",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}
