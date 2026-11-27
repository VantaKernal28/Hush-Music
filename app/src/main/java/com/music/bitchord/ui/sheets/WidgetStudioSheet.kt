package com.music.bitchord.ui.sheets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.bitchord.ui.components.ContinuousCornerShape
import com.music.bitchord.ui.icons.SfIcons
import com.music.bitchord.widget.CassetteWidget
import com.music.bitchord.widget.ClassicWidget
import com.music.bitchord.widget.M3Widget
import com.music.bitchord.widget.MediaWidgetPill
import com.music.bitchord.widget.MediaWidgetSnapshot
import dev.chrisbanes.haze.HazeState

enum class WidgetStyle(val label: String, val subtitle: String) {
    CASSETTE("Cassette", "Analog tape deck with reels & physical buttons"),
    MATERIAL_3("Material 3", "Material You dynamic color with squircle cover"),
    CLASSIC("Glass", "Translucent frosted glass with specular highlight"),
    PILL("Pill", "Minimalist 4×1 single-row player bar"),
}

enum class CassetteTheme(val id: String, val label: String, val colorHex: String, val bgHex: String) {
    SLATE("slate", "Midnight Slate", "#2B3340", "#F5F2EB"),
    CREAM("cream", "Vintage Cream", "#E5DCCB", "#22252B"),
    BLUSH("blush", "Cherry Blush", "#D97B89", "#F7F0E8"),
    MINT("mint", "Retro Mint", "#5E8E89", "#F2F5F4"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetStudioSheet(
    onDismiss: () -> Unit,
    @Suppress("UNUSED_PARAMETER") hazeState: HazeState? = null,
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedStyle by remember { mutableStateOf(WidgetStyle.CASSETTE) }
    var selectedCassetteTheme by remember {
        val savedId = CassetteWidget.savedThemeId(context)
        mutableStateOf(CassetteTheme.entries.find { it.id == savedId } ?: CassetteTheme.SLATE)
    }

    val snapshot = remember { MediaWidgetSnapshot.load(context) }
    val title = if (snapshot.hasTrack) snapshot.title else "Yurayura Beat Live at TODAr"
    val artist = if (snapshot.hasTrack) snapshot.artist else "ryuki.place • OP-1 Field"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        shape = ContinuousCornerShape(32.dp),
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "Home Screen Widgets",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Customize and pin music widgets to your launcher",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // Style selector pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                WidgetStyle.entries.forEach { style ->
                    val isSelected = selectedStyle == style
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(ContinuousCornerShape(14.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            )
                            .clickable { selectedStyle = style }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = style.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Live Interactive Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9.5f)
                    .clip(ContinuousCornerShape(24.dp))
                    .background(Color(0xFF141418))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), ContinuousCornerShape(24.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedContent(
                    targetState = selectedStyle to selectedCassetteTheme,
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
                    label = "widget_preview",
                ) { (style, cTheme) ->
                    when (style) {
                        WidgetStyle.CASSETTE -> {
                            val bitmap = remember(cTheme, title, artist) {
                                CassetteWidget.drawCassetteBitmap(
                                    widthPx = 800,
                                    heightPx = 460,
                                    title = title,
                                    artist = artist,
                                    isPlaying = true,
                                    tapeColor = android.graphics.Color.parseColor(cTheme.colorHex),
                                    cardBgColor = android.graphics.Color.parseColor(cTheme.bgHex),
                                )
                            }
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Cassette Preview",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(ContinuousCornerShape(20.dp)),
                            )
                        }
                        WidgetStyle.MATERIAL_3 -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.92f)
                                    .height(96.dp)
                                    .clip(ContinuousCornerShape(24.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                        ContinuousCornerShape(24.dp),
                                    )
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(ContinuousCornerShape(16.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFFFF8A65), Color(0xFFFF5252)),
                                                ),
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = SfIcons.MusicNote,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(26.dp),
                                        )
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Text(
                                            text = artist,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 1,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = SfIcons.Play,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                }
                            }
                        }
                        WidgetStyle.CLASSIC -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.94f)
                                    .height(108.dp)
                                    .clip(ContinuousCornerShape(22.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFF232530), Color(0xFF14151B)),
                                        ),
                                    )
                                    .border(1.5.dp, Color.White.copy(alpha = 0.2f), ContinuousCornerShape(22.dp))
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(ContinuousCornerShape(14.dp))
                                            .background(
                                                Brush.radialGradient(
                                                    listOf(Color(0xFF7C4DFF), Color(0xFF3F51B5)),
                                                ),
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = SfIcons.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp),
                                        )
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            color = Color.White,
                                        )
                                        Text(
                                            text = artist,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 1,
                                            color = Color.White.copy(alpha = 0.7f),
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(
                                            imageVector = Icons.Rounded.SkipPrevious,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp),
                                        )
                                        Icon(
                                            imageVector = SfIcons.Play,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp),
                                        )
                                        Icon(
                                            imageVector = Icons.Rounded.SkipNext,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp),
                                        )
                                    }
                                }
                            }
                        }
                        WidgetStyle.PILL -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.95f)
                                    .height(64.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22242C))
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                                    .padding(horizontal = 14.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFF5722)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = SfIcons.MusicNote,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            color = Color.White,
                                        )
                                        Text(
                                            text = artist,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            color = Color.White.copy(alpha = 0.6f),
                                        )
                                    }
                                    Icon(
                                        imageVector = SfIcons.Play,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Cassette color palette selector (if Cassette is selected)
            if (selectedStyle == WidgetStyle.CASSETTE) {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CassetteTheme.entries.forEach { themeOption ->
                        val isPicked = selectedCassetteTheme == themeOption
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(ContinuousCornerShape(14.dp))
                                .background(
                                    if (isPicked) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                )
                                .border(
                                    width = if (isPicked) 2.dp else 1.dp,
                                    color = if (isPicked) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = ContinuousCornerShape(14.dp),
                                )
                                .clickable {
                                    selectedCassetteTheme = themeOption
                                    CassetteWidget.saveTheme(context, themeOption.id)
                                    CassetteWidget.refresh(context)
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(themeOption.colorHex)))
                                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = themeOption.label.split(" ").last(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isPicked) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // "Add to Home Screen" Pin Action Button
            Button(
                onClick = {
                    if (selectedStyle == WidgetStyle.CASSETTE) {
                        CassetteWidget.saveTheme(context, selectedCassetteTheme.id)
                    }
                    pinWidgetToHomeScreen(context, selectedStyle)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = ContinuousCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Add to Home Screen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(14.dp))

            // Guidance footer
            Text(
                text = "Tip: You can also touch & hold any empty area on your phone's Home Screen, tap 'Widgets', scroll to 'Hush Music', and choose from all sizes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
            )

            Spacer(Modifier.height(12.dp))
        }
    }
}

/**
 * Pins the chosen widget directly to the Android home screen launcher via AppWidgetManager.
 */
private fun pinWidgetToHomeScreen(context: Context, style: WidgetStyle) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val providerClass = when (style) {
            WidgetStyle.CASSETTE -> CassetteWidget::class.java
            WidgetStyle.MATERIAL_3 -> M3Widget::class.java
            WidgetStyle.CLASSIC -> ClassicWidget::class.java
            WidgetStyle.PILL -> MediaWidgetPill::class.java
        }
        val provider = ComponentName(context, providerClass)

        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            val pinned = appWidgetManager.requestPinAppWidget(provider, null, null)
            if (pinned) {
                Toast.makeText(context, "Adding widget to Home Screen…", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Touch and hold your Home Screen to place widget", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(
                context,
                "Launcher does not support direct pinning. Touch and hold Home Screen -> Widgets -> Hush Music",
                Toast.LENGTH_LONG,
            ).show()
        }
    } else {
        Toast.makeText(
            context,
            "Touch and hold Home Screen -> Widgets -> Hush Music to add",
            Toast.LENGTH_LONG,
        ).show()
    }
}
