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
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import com.music.bitchord.R
import coil3.compose.AsyncImage

private const val PHOENIX_HANDLE = "phoenixdevellopment75"
private const val PHOENIX_AVATAR = "https://github.com/$PHOENIX_HANDLE.png"
private const val PHOENIX_URL = "https://github.com/$PHOENIX_HANDLE"

private const val DHRUV_HANDLE = "VantaKernal28"
private const val DHRUV_AVATAR = "https://github.com/$DHRUV_HANDLE.png"
private const val DHRUV_URL = "https://github.com/$DHRUV_HANDLE"

private const val REPO_URL = "https://github.com/VantaKernal28/Hush-Music"

import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.text.style.TextAlign

/**
 * About Hush screen laid out in flagship Material 3 Expressive Bento Grid aesthetic:
 * - Hero Bento Row: Hush rounded wordmark (CherryBombOne), adaptive inverted "h" emblem, and Hush 1.0 Stable Release badge
 * - 2-Column Bento Spec Grid: Architecture, Android Version, Device, Audio Engine, Sample Rate, Display Target
 * - Lead Developer Bento Cards for Phoenix & Dhruv Patil
 */
@Composable
fun AboutSheet(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val uriHandler = LocalUriHandler.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val cardBg = if (isDark) Color(0xFF1C1C21) else MaterialTheme.colorScheme.surfaceContainerHigh
    val cardBorder = if (isDark) Color.White.copy(alpha = 0.08f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    val inkColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val subInkColor = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryAccent = MaterialTheme.colorScheme.primary
    val emblemBg = if (isDark) Color.White else Color(0xFF141418)
    val emblemFg = if (isDark) Color(0xFF121215) else Color.White

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Top ambient gradient glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            primaryAccent.copy(alpha = if (isDark) 0.28f else 0.16f),
                            primaryAccent.copy(alpha = if (isDark) 0.10f else 0.05f),
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
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                        .border(1.dp, cardBorder, CircleShape),
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
                    text = "About Hush",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                    ),
                    color = inkColor,
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
                // 1. Flagship Bento Hero Card: CherryBombOne Wordmark + Adaptive Inverted 'h' Emblem + Hush 1.0 Stable Release Badge
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(cardBg)
                            .border(1.dp, primaryAccent.copy(alpha = 0.28f), RoundedCornerShape(28.dp))
                            .padding(22.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(primaryAccent.copy(alpha = 0.16f))
                                        .border(1.dp, primaryAccent.copy(alpha = 0.35f), CircleShape)
                                        .padding(horizontal = 12.dp, vertical = 5.dp),
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = primaryAccent,
                                            modifier = Modifier.size(14.dp),
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "Hush 1.0 · Stable Release",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 0.4.sp,
                                            ),
                                            color = primaryAccent,
                                        )
                                    }
                                }

                                Spacer(Modifier.height(14.dp))

                                Text(
                                    text = "Hush",
                                    fontFamily = FontFamily(Font(R.font.cherry_bomb_one_regular)),
                                    style = MaterialTheme.typography.displaySmall.copy(
                                        fontSize = 42.sp,
                                    ),
                                    color = inkColor,
                                )

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    text = "Pure High-Resolution Audio & Expressive Motion",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = subInkColor,
                                )
                            }

                            Spacer(Modifier.width(16.dp))

                            // Adaptive Inverted Hush "h" Emblem
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(emblemBg)
                                    .border(1.5.dp, primaryAccent.copy(alpha = 0.45f), RoundedCornerShape(24.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "h",
                                    fontFamily = FontFamily(Font(R.font.cherry_bomb_one_regular)),
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontSize = 46.sp,
                                    ),
                                    color = emblemFg,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }

                // 2. Clean 2-Column Bento Grid Specifications (Architecture, Android Version, Device, Audio Engine)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            BentoSpecCard(
                                icon = Icons.Rounded.Memory,
                                label = "Architecture",
                                value = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a",
                                cardBg = cardBg,
                                cardBorder = cardBorder,
                                inkColor = inkColor,
                                accentColor = primaryAccent,
                                modifier = Modifier.weight(1f),
                            )
                            BentoSpecCard(
                                icon = Icons.Rounded.PhoneAndroid,
                                label = "Android Version",
                                value = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                                cardBg = cardBg,
                                cardBorder = cardBorder,
                                inkColor = inkColor,
                                accentColor = primaryAccent,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            BentoSpecCard(
                                icon = Icons.Rounded.CheckCircle,
                                label = "Device",
                                value = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
                                cardBg = cardBg,
                                cardBorder = cardBorder,
                                inkColor = inkColor,
                                accentColor = primaryAccent,
                                modifier = Modifier.weight(1f),
                            )
                            BentoSpecCard(
                                icon = Icons.Rounded.GraphicEq,
                                label = "Audio Engine",
                                value = "32-bit Float DSP · Bit-Exact",
                                cardBg = cardBg,
                                cardBorder = cardBorder,
                                inkColor = inkColor,
                                accentColor = primaryAccent,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            BentoSpecCard(
                                icon = Icons.Rounded.Waves,
                                label = "Sample Rate",
                                value = "48.0 kHz · Direct Output",
                                cardBg = cardBg,
                                cardBorder = cardBorder,
                                inkColor = inkColor,
                                accentColor = primaryAccent,
                                modifier = Modifier.weight(1f),
                            )
                            BentoSpecCard(
                                icon = Icons.Rounded.BatteryChargingFull,
                                label = "Display Target",
                                value = "120Hz Locked Smooth",
                                cardBg = cardBg,
                                cardBorder = cardBorder,
                                inkColor = inkColor,
                                accentColor = primaryAccent,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                // 3. Lead Developers
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(cardBg)
                            .border(1.dp, cardBorder, RoundedCornerShape(24.dp))
                            .padding(20.dp),
                    ) {
                        Text(
                            text = "Lead Developers",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = inkColor,
                        )
                        Spacer(Modifier.height(14.dp))

                        // Phoenix
                        DeveloperCard(
                            name = "Phoenix",
                            handle = "@$PHOENIX_HANDLE",
                            role = "Lead Developer",
                            avatarUrl = PHOENIX_AVATAR,
                            inkColor = inkColor,
                            onClick = { uriHandler.openUri(PHOENIX_URL) },
                        )

                        Spacer(Modifier.height(14.dp))
                        HorizontalDivider(color = cardBorder, thickness = 0.8.dp)
                        Spacer(Modifier.height(14.dp))

                        // Dhruv Patil
                        DeveloperCard(
                            name = "Dhruv Patil",
                            handle = "@$DHRUV_HANDLE",
                            role = "Lead Developer",
                            avatarUrl = DHRUV_AVATAR,
                            inkColor = inkColor,
                            onClick = { uriHandler.openUri(DHRUV_URL) },
                        )
                    }
                }

                // 4. GitHub Repository link
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(cardBg)
                            .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
                            .clickable { uriHandler.openUri(REPO_URL) }
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "GitHub Repository",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = inkColor,
                            )
                            Text(
                                text = "VantaKernal28/Hush-Music",
                                style = MaterialTheme.typography.bodySmall,
                                color = subInkColor,
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = subInkColor,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BentoSpecCard(
    icon: ImageVector,
    label: String,
    value: String,
    cardBg: Color,
    cardBorder: Color,
    inkColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(22.dp))
            .padding(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = inkColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
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
    inkColor: Color,
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
                    color = inkColor,
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
