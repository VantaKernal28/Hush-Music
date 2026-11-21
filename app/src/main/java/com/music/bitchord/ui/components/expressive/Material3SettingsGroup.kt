/*
 * Ported from cromaguy/Rhythm (GPL-3.0)
 * Material 3 Expressive grouped settings cards with dynamic corner radii and luminous palettes.
 */

package com.music.bitchord.ui.components.expressive

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Stock Android / Pixel Settings Badge Palette with uniform luminous colors across light & dark themes.
 */
data class SettingsBadgePalette(
    val container: Color,
    val tint: Color,
) {
    fun containerColor(isDark: Boolean = false): Color = container
    fun iconTintColor(isDark: Boolean = false): Color = tint
}

/**
 * Predefined Stock Android / Pixel Settings Badge Palettes (Material 3 Expressive)
 */
object SettingsPalettes {
    val Amber = SettingsBadgePalette(
        container = Color(0xFFFFDCBE),
        tint = Color(0xFF7C4113),
    )
    val Yellow = SettingsBadgePalette(
        container = Color(0xFFFFF0A4),
        tint = Color(0xFF624F00),
    )
    val Emerald = SettingsBadgePalette(
        container = Color(0xFFC4EED0),
        tint = Color(0xFF18532A),
    )
    val Cyan = SettingsBadgePalette(
        container = Color(0xFFC0EAE6),
        tint = Color(0xFF006A6F),
    )
    val SkyBlue = SettingsBadgePalette(
        container = Color(0xFFC2E7FF),
        tint = Color(0xFF004D77),
    )
    val Indigo = SettingsBadgePalette(
        container = Color(0xFFD6E3FF),
        tint = Color(0xFF1D5FA9),
    )
    val Purple = SettingsBadgePalette(
        container = Color(0xFFEEDCFF),
        tint = Color(0xFF5D367B),
    )
    val Rose = SettingsBadgePalette(
        container = Color(0xFFF7D8EB),
        tint = Color(0xFF82276C),
    )
    val Coral = SettingsBadgePalette(
        container = Color(0xFFFFD8E2),
        tint = Color(0xFF962846),
    )
    val Lime = SettingsBadgePalette(
        container = Color(0xFFE0F3B8),
        tint = Color(0xFF3B5606),
    )
    val Slate = SettingsBadgePalette(
        container = Color(0xFFE2E2E6),
        tint = Color(0xFF44474E),
    )
    val Teal = SettingsBadgePalette(
        container = Color(0xFFBCEBEB),
        tint = Color(0xFF006874),
    )
    val Orange = SettingsBadgePalette(
        container = Color(0xFFFFDAC5),
        tint = Color(0xFF763710),
    )
}

fun getSectionPalette(title: String?): SettingsBadgePalette {
    if (title.isNullOrBlank()) return SettingsPalettes.Purple
    val lower = title.lowercase()
    return when {
        lower.contains("appearance") || lower.contains("theme") || lower.contains("look") || lower.contains("style") -> SettingsPalettes.Purple
        lower.contains("home") || lower.contains("widget") || lower.contains("order") -> SettingsPalettes.Orange
        lower.contains("interface") || lower.contains("navigation") || lower.contains("control") || lower.contains("gesture") -> SettingsPalettes.SkyBlue
        lower.contains("queue") || lower.contains("playback") || lower.contains("volume") -> SettingsPalettes.Emerald
        lower.contains("audio") || lower.contains("sound") || lower.contains("lyrics") || lower.contains("equalizer") -> SettingsPalettes.Rose
        lower.contains("library") || lower.contains("media") || lower.contains("playlist") -> SettingsPalettes.Amber
        lower.contains("notification") || lower.contains("alert") -> SettingsPalettes.Coral
        lower.contains("storage") || lower.contains("cache") || lower.contains("backup") || lower.contains("guard") -> SettingsPalettes.Yellow
        lower.contains("about") || lower.contains("info") || lower.contains("version") -> SettingsPalettes.Slate
        lower.contains("performance") || lower.contains("battery") || lower.contains("power") -> SettingsPalettes.Lime
        lower.contains("network") || lower.contains("source") || lower.contains("account") -> SettingsPalettes.SkyBlue
        else -> SettingsPalettes.Purple
    }
}

fun getIconPalette(icon: Any?): SettingsBadgePalette {
    val rawName = when (icon) {
        is ImageVector -> icon.name
        else -> null
    }
    if (rawName.isNullOrBlank()) return SettingsPalettes.Purple
    val lower = rawName.lowercase()
    return when {
        lower.contains("palette") || lower.contains("brush") || lower.contains("color") || lower.contains("theme") -> SettingsPalettes.Purple
        lower.contains("album") || lower.contains("image") || lower.contains("artist") || lower.contains("person") -> SettingsPalettes.Rose
        lower.contains("sound") || lower.contains("volume") || lower.contains("audio") || lower.contains("music") -> SettingsPalettes.Coral
        lower.contains("network") || lower.contains("wifi") || lower.contains("download") || lower.contains("search") -> SettingsPalettes.SkyBlue
        lower.contains("queue") || lower.contains("list") || lower.contains("shuffle") || lower.contains("repeat") -> SettingsPalettes.Indigo
        lower.contains("brightness") || lower.contains("gradient") || lower.contains("display") -> SettingsPalettes.Amber
        lower.contains("home") || lower.contains("clock") || lower.contains("time") -> SettingsPalettes.Orange
        lower.contains("storage") || lower.contains("folder") || lower.contains("file") -> SettingsPalettes.Yellow
        lower.contains("lock") || lower.contains("shield") || lower.contains("security") -> SettingsPalettes.Emerald
        lower.contains("bolt") || lower.contains("speed") || lower.contains("power") -> SettingsPalettes.Lime
        lower.contains("lyrics") || lower.contains("tune") -> SettingsPalettes.Cyan
        else -> SettingsPalettes.Slate
    }
}

/**
 * Material 3 Expressive settings group — card stack with dynamic corner radii.
 *
 * Single item → fully rounded 24dp
 * First item → 24dp top, 6dp bottom
 * Middle items → 6dp all
 * Last item → 6dp top, 24dp bottom
 */
@Composable
fun Material3SettingsGroup(
    title: String? = null,
    items: List<Material3SettingsItem>,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    palette: SettingsBadgePalette? = null,
    itemShape: Shape? = null,
    lastItemShape: Shape? = null,
    iconShape: Shape? = null,
    modifier: Modifier = Modifier,
) {
    val sectionPalette = palette ?: if (title != null) getSectionPalette(title) else null

    Column(modifier = modifier.fillMaxWidth()) {
        title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp, top = 18.dp),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items.forEachIndexed { index, item ->
                val isLast = index == items.size - 1
                val shape = when {
                    lastItemShape != null && isLast -> lastItemShape
                    itemShape != null -> itemShape
                    items.size == 1 -> RoundedCornerShape(24.dp)
                    index == 0 -> RoundedCornerShape(
                        topStart = 24.dp, topEnd = 24.dp,
                        bottomStart = 6.dp, bottomEnd = 6.dp,
                    )
                    isLast -> RoundedCornerShape(
                        topStart = 6.dp, topEnd = 6.dp,
                        bottomStart = 24.dp, bottomEnd = 24.dp,
                    )
                    else -> RoundedCornerShape(6.dp)
                }

                val itemPalette = item.palette ?: sectionPalette ?: getIconPalette(item.icon)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    shape = shape,
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Material3SettingsItemRow(
                        item = item,
                        fallbackPalette = itemPalette,
                        fallbackShape = item.iconShape ?: iconShape ?: CircleShape,
                    )
                }
            }
        }
    }
}

@Composable
private fun Material3SettingsItemRow(
    item: Material3SettingsItem,
    fallbackPalette: SettingsBadgePalette? = null,
    fallbackShape: Shape = CircleShape,
) {
    val isDark = isSystemInDarkTheme()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val itemPalette = item.palette ?: fallbackPalette

    val defaultIconBg = when {
        item.palette != null -> item.palette.containerColor(isDark)
        fallbackPalette != null -> fallbackPalette.containerColor(isDark)
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    val defaultIconTint = when {
        item.palette != null -> item.palette.iconTintColor(isDark)
        fallbackPalette != null -> fallbackPalette.iconTintColor(isDark)
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "setting_item_scale",
    )

    val iconBgColor by animateColorAsState(
        targetValue = when {
            !item.enabled -> MaterialTheme.colorScheme.surfaceContainerHighest
            isPressed -> (item.iconBackgroundTint ?: defaultIconBg).copy(alpha = 0.86f)
            else -> (item.iconBackgroundTint ?: defaultIconBg)
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "icon_bg_color",
    )

    val iconColor by animateColorAsState(
        targetValue = if (!item.enabled) {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        } else {
            (item.iconTint ?: defaultIconTint)
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "icon_color",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                enabled = item.enabled && item.onClick != null,
                interactionSource = interactionSource,
                indication = null,
                onClick = { item.onClick?.invoke() },
            )
            .padding(horizontal = 20.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (item.leadingContent != null) {
            item.leadingContent.invoke()
            Spacer(modifier = Modifier.width(16.dp))
        } else item.icon?.let { icon ->
            Surface(
                modifier = Modifier.size(40.dp),
                shape = item.iconShape ?: fallbackShape,
                color = iconBgColor,
                tonalElevation = if (item.isHighlighted) 2.dp else 0.dp,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    if (icon is ImageVector) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            ProvideTextStyle(
                MaterialTheme.typography.titleMedium.copy(
                    color = if (!item.enabled) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                ),
            ) {
                item.title()
            }

            item.description?.let { desc ->
                Spacer(modifier = Modifier.height(2.dp))
                ProvideTextStyle(
                    MaterialTheme.typography.bodyMedium.copy(
                        color = if (!item.enabled) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    ),
                ) {
                    desc()
                }
            }
        }

        item.trailingContent?.let { trailing ->
            Spacer(modifier = Modifier.width(8.dp))
            trailing()
        }
    }
}

/**
 * Data class for a Material 3 Expressive settings item.
 */
data class Material3SettingsItem(
    val icon: Any? = null,
    val iconTint: Color? = null,
    val iconBackgroundTint: Color? = null,
    val palette: SettingsBadgePalette? = null,
    val title: @Composable () -> Unit,
    val description: (@Composable () -> Unit)? = null,
    val trailingContent: (@Composable () -> Unit)? = null,
    val isHighlighted: Boolean = false,
    val iconShape: Shape? = null,
    val enabled: Boolean = true,
    val leadingContent: (@Composable () -> Unit)? = null,
    val onClick: (() -> Unit)? = null,
)
