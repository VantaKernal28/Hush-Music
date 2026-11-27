package com.music.bitchord.ui.components

import android.os.Build
import android.view.WindowManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.music.bitchord.R
import com.music.bitchord.data.SongInfo
import com.music.bitchord.data.SongInfoRepository
import com.music.bitchord.data.compactCount
import com.music.bitchord.data.model.Song

private val INFO_BODY_MAX_HEIGHT = 360.dp

/**
 * Centered floating frosted Song Info card matching Hush's M3E card aesthetic.
 * Renders cleanly inside a centered Window Dialog with blurred backdrop so it works
 * identically over any screen or player window without touching the bottom edge.
 */
@Composable
fun SongInfoDialog(song: Song, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val view = LocalView.current
        SideEffect {
            val window = (view.parent as? DialogWindowProvider)?.window ?: return@SideEffect
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                window.attributes = window.attributes.apply {
                    blurBehindRadius = 48
                }
            }
            window.setDimAmount(0.45f)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                )
                .padding(horizontal = 24.dp, vertical = 32.dp),
            contentAlignment = Alignment.Center,
        ) {
            SongInfoCardContent(song = song, onDismiss = onDismiss)
        }
    }
}

/**
 * Reusable centered Song Info card body used both inside [SongInfoDialog] and directly inside
 * the M3E player overlay.
 */
@Composable
fun SongInfoCardContent(
    song: Song,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var retry by remember(song.videoId) { mutableIntStateOf(0) }
    val info by produceState<SongInfo?>(null, song, retry) {
        value = null
        value = SongInfoRepository.load(song)
    }

    val darkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val cardColor = if (darkTheme) {
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.94f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.96f)
    }
    val rimColor = if (darkTheme) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.08f)
    val closeFill = if (darkTheme) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.07f)
    val onSurface = MaterialTheme.colorScheme.onSurface
    val cardShape = remember { RoundedCornerShape(30.dp) }

    val scale = remember { Animatable(0.88f) }
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 0.76f, stiffness = 400f),
        )
    }

    Column(
        modifier = modifier
            .widthIn(max = 390.dp)
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .shadow(24.dp, cardShape)
            .clip(cardShape)
            .background(cardColor)
            .border(1.dp, rimColor, cardShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            )
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(closeFill)
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.close),
                    tint = onSurface,
                    modifier = Modifier.size(18.dp),
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.song_info),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = onSurface,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurface.copy(alpha = 0.72f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.size(34.dp))
        }

        if (song.artist.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = rimColor, thickness = 0.75.dp)

        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(max = INFO_BODY_MAX_HEIGHT)
                .verticalScroll(rememberScrollState()),
        ) {
            val unavailable = stringResource(R.string.song_info_unavailable)
            val rows = listOf(
                R.string.song_info_album to (info?.album ?: song.albumName),
                R.string.song_info_release_date to (info?.releaseDate ?: info?.releaseYear),
                R.string.song_info_views to info?.views?.let(::compactCount),
                R.string.song_info_likes to info?.likes,
                R.string.song_info_comments to info?.comments,
            )
            rows.forEachIndexed { index, (label, value) ->
                if (index > 0) HorizontalDivider(color = rimColor.copy(alpha = 0.5f), thickness = 0.5.dp)
                SongInfoRow(
                    label = stringResource(label),
                    value = value ?: unavailable.takeIf { info != null },
                )
            }
            if (info?.failed == true) {
                Text(
                    text = stringResource(R.string.song_info_error),
                    modifier = Modifier.padding(top = 6.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 15.sp),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (info?.failed == true) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { retry++ },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.song_info_retry),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = onSurface,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.done),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

/** Label on the left, value on the right; a small spinner stands in for the value while it loads. */
@Composable
private fun SongInfoRow(label: String, value: String?) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = onSurface.copy(alpha = 0.65f),
        )
        Spacer(Modifier.width(12.dp))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            if (value == null) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = onSurface.copy(alpha = 0.5f),
                    strokeWidth = 1.75.dp,
                )
            } else {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = onSurface,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}
