/*
 * Hush Music (Beta 1.3 / Test)
 * Authentic Exhale About Screen & Developer Credits (video_4.mp4).
 */

package com.music.bitchord.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

private const val PHOENIX_HANDLE = "phoenixdevellopment75"
private const val PHOENIX_AVATAR = "https://github.com/$PHOENIX_HANDLE.png"
private const val PHOENIX_URL = "https://github.com/$PHOENIX_HANDLE"

private const val DHRUV_HANDLE = "VantaKernal28"
private const val DHRUV_AVATAR = "https://github.com/$DHRUV_HANDLE.png"
private const val DHRUV_URL = "https://github.com/$DHRUV_HANDLE"

private const val REPO_URL = "https://github.com/VantaKernal28/Hush-Music"

/**
 * About screen laid out to match Exhale (video_4.mp4):
 * - Glowing warm ambient top gradient glow
 * - Hero Card with Hush Music typography and up-to-date version pill
 * - Installed & View Changelog group
 * - System & Hardware specifications card
 * - Lead Developer cards (Phoenix & Dhruv) with clickable GitHub profiles
 */
@Composable
fun AboutSheet(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val uriHandler = LocalUriHandler.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Top ambient gradient glow (Exhale amber/orange aesthetic from video_4.mp4)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE65100).copy(alpha = 0.32f),
                            Color(0xFF8B2500).copy(alpha = 0.16f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            // Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f)),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp),
                    )
                }

                Spacer(Modifier.width(16.dp))

                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(
                    top = 8.dp,
                    bottom = contentPadding.calculateBottomPadding() + 48.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // 1. Hero Poster Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF1C0D02),
                                        Color(0xFF2E1303),
                                        Color(0xFF100702),
                                    ),
                                ),
                            )
                            .border(1.dp, Color(0xFFFF9800).copy(alpha = 0.25f), RoundedCornerShape(32.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        // Ambient luminous wave ring inside card
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFFFB74D).copy(alpha = 0.35f),
                                            Color(0xFFE65100).copy(alpha = 0.15f),
                                            Color.Transparent,
                                        ),
                                    ),
                                ),
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = "Hush Music",
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = (-1).sp,
                                ),
                                color = Color.White,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Hush Music 1.0",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 17.sp,
                                ),
                                color = Color(0xFFFFCC80),
                            )
                            Spacer(Modifier.height(32.dp))
                            // Status capsule pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(percent = 50))
                                    .background(Color.White.copy(alpha = 0.12f))
                                    .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(percent = 50))
                                    .padding(horizontal = 18.dp, vertical = 9.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB74D),
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Hush Music 1.0 is up to date.",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                        ),
                                        color = Color.White,
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Installed & Changelog Group
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0xFF1E1E22))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(22.dp)),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Installed",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = Color.White,
                            )
                            Text(
                                text = "1.0 (Production Release)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.06f),
                            thickness = 0.8.dp,
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { uriHandler.openUri("$REPO_URL/commits/test") }
                                .padding(horizontal = 20.dp, vertical = 18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "View Changelog",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = Color.White,
                            )
                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }

                // 3. System Specs Metadata Card
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0xFF1E1E22))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(22.dp))
                            .padding(20.dp),
                    ) {
                        Text(
                            text = "Hush Music",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                        )
                        Text(
                            text = "Beta 1.3 (Test) · release",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Spacer(Modifier.height(14.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.8.dp)
                        Spacer(Modifier.height(14.dp))

                        SpecsRow(label = "Architecture", value = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a")
                        SpecsRow(label = "Commit", value = "0fe33aa (test)")
                        SpecsRow(label = "Package", value = "com.music.bitchord")
                        SpecsRow(label = "Android Version", value = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                        SpecsRow(label = "Device", value = "${Build.MANUFACTURER} ${Build.MODEL}")
                    }
                }

                // 4. Lead Developers
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0xFF1E1E22))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(22.dp))
                            .padding(20.dp),
                    ) {
                        Text(
                            text = "Lead Developers",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                        )
                        Spacer(Modifier.height(14.dp))

                        // Phoenix
                        DeveloperCard(
                            name = "Phoenix",
                            handle = "@$PHOENIX_HANDLE",
                            role = "Lead Developer & UI/UX Designer",
                            avatarUrl = PHOENIX_AVATAR,
                            onClick = { uriHandler.openUri(PHOENIX_URL) },
                        )

                        Spacer(Modifier.height(14.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.8.dp)
                        Spacer(Modifier.height(14.dp))

                        // Dhruv
                        DeveloperCard(
                            name = "Dhruv",
                            handle = "@$DHRUV_HANDLE",
                            role = "Core Audio & Architecture Lead",
                            avatarUrl = DHRUV_AVATAR,
                            onClick = { uriHandler.openUri(DHRUV_URL) },
                        )
                    }
                }

                // 5. GitHub Repository link
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF1E1E22))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
                            .clickable { uriHandler.openUri(REPO_URL) }
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "GitHub Repository",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White,
                            )
                            Text(
                                text = "VantaKernal28/Hush-Music",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecsRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = Color.White,
        )
    }
}

@Composable
private fun DeveloperCard(
    name: String,
    handle: String,
    role: String,
    avatarUrl: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = avatarUrl,
            contentDescription = "$name Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
        )

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = handle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = role,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
    }
}
