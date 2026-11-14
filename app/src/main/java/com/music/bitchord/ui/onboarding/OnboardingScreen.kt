@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.music.bitchord.ui.onboarding

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.bitchord.R
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.ui.theme.CherryBombOne
import com.music.bitchord.ui.theme.HushAccents
import com.music.bitchord.ui.theme.resolveAccentColor
import kotlinx.coroutines.delay

/**
 * Onboarding flow phases:
 * - Phase A: Native Compose launch entrance animation & horizontal blur exit
 * - Phase B: Clean setup screens modeled after Inspired setup.jpg
 * - Phase C: Native Compose post-name reveal with blur interpolation and progressive wordmark reveal
 */
private enum class OnboardingPhase {
    ENTRANCE,
    STEP_NAME_ACCENT,
    POST_NAME_REVEAL,
    STEP_VIBE_AUDIO,
    STEP_ACCOUNT,
}

private const val NAME_LIMIT = 24

// Serene cool slate / muted blue canvas from Inspired setup.jpg
private val CanvasTop = Color(0xFF8CA8B5)
private val CanvasBottom = Color(0xFF9FB7C3)

@Composable
fun OnboardingScreen(
    signedIn: Boolean,
    initialName: String,
    onNameChosen: (String) -> Unit,
    onSignIn: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var phase by remember { mutableStateOf(OnboardingPhase.ENTRANCE) }
    var name by remember { mutableStateOf(initialName) }
    var selectedGenres by remember {
        mutableStateOf(AppSettings.selectedGenres.value.ifEmpty { setOf("Lo-Fi", "Electronic") })
    }
    var currentAccentId by remember { mutableStateOf(AppSettings.themeAccent.value) }
    var audioQuality by remember { mutableStateOf("320") }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    val activeAccentColor = resolveAccentColor(currentAccentId, dark = false)

    BackHandler(enabled = phase != OnboardingPhase.ENTRANCE && phase != OnboardingPhase.POST_NAME_REVEAL) {
        when (phase) {
            OnboardingPhase.STEP_ACCOUNT -> phase = OnboardingPhase.STEP_VIBE_AUDIO
            OnboardingPhase.STEP_VIBE_AUDIO -> phase = OnboardingPhase.STEP_NAME_ACCENT
            else -> {}
        }
    }

    LaunchedEffect(signedIn, phase) {
        if (signedIn && phase == OnboardingPhase.STEP_ACCOUNT) {
            onFinish()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } },
    ) {
        when (phase) {
            OnboardingPhase.ENTRANCE -> {
                EntranceLaunchAnimation(
                    onAnimationComplete = { phase = OnboardingPhase.STEP_NAME_ACCENT },
                )
            }

            OnboardingPhase.POST_NAME_REVEAL -> {
                PostNameIdentityRevealAnimation(
                    onAnimationComplete = { phase = OnboardingPhase.STEP_VIBE_AUDIO },
                )
            }

            else -> {
                // Phase B: Clean Setup UI inspired by Inspired setup.jpg
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(listOf(CanvasTop, CanvasBottom)),
                        )
                        .systemBarsPadding()
                        .imePadding(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Top bar: HOME and BACK / step labels
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "HOME",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.85f),
                                letterSpacing = 1.2.sp,
                            )
                            if (phase == OnboardingPhase.STEP_VIBE_AUDIO || phase == OnboardingPhase.STEP_ACCOUNT) {
                                Text(
                                    text = "BACK",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White.copy(alpha = 0.85f),
                                    letterSpacing = 1.2.sp,
                                    modifier = Modifier.clickable {
                                        if (phase == OnboardingPhase.STEP_ACCOUNT) phase = OnboardingPhase.STEP_VIBE_AUDIO
                                        else phase = OnboardingPhase.STEP_NAME_ACCENT
                                    },
                                )
                            } else {
                                Spacer(Modifier.width(32.dp))
                            }
                        }

                        // Centered Hush emblem & brand typography
                        Spacer(Modifier.height(18.dp))
                        Image(
                            painter = painterResource(R.drawable.ic_hush_transparent_logo),
                            contentDescription = "Hush Music",
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(22.dp)),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "hush music",
                            fontFamily = CherryBombOne,
                            fontSize = 32.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            letterSpacing = (-0.5).sp,
                        )

                        // 3-dot pagination indicator
                        Spacer(Modifier.height(14.dp))
                        PaginationDots(
                            currentIndex = when (phase) {
                                OnboardingPhase.STEP_NAME_ACCENT -> 0
                                OnboardingPhase.STEP_VIBE_AUDIO -> 1
                                else -> 2
                            },
                            accentColor = activeAccentColor,
                        )

                        Spacer(Modifier.height(16.dp))

                        // Dynamic content area per step
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            when (phase) {
                                OnboardingPhase.STEP_NAME_ACCENT -> {
                                    StepNameAndAccentContent(
                                        name = name,
                                        onNameChange = { name = it.take(NAME_LIMIT) },
                                        selectedAccent = currentAccentId,
                                        onAccentChange = {
                                            currentAccentId = it
                                            AppSettings.setThemeAccent(it)
                                        },
                                        onDone = {
                                            val chosen = name.trim().ifBlank { "Listener" }
                                            focusManager.clearFocus()
                                            onNameChosen(chosen)
                                            phase = OnboardingPhase.POST_NAME_REVEAL
                                        },
                                    )
                                }

                                OnboardingPhase.STEP_VIBE_AUDIO -> {
                                    StepVibeAudioContent(
                                        selectedGenres = selectedGenres,
                                        onToggleGenre = { genre ->
                                            selectedGenres = if (selectedGenres.contains(genre)) {
                                                if (selectedGenres.size > 1) selectedGenres - genre else selectedGenres
                                            } else {
                                                if (selectedGenres.size < 4) selectedGenres + genre else selectedGenres
                                            }
                                            AppSettings.setSelectedGenres(selectedGenres)
                                        },
                                        audioQuality = audioQuality,
                                        onQualityChange = { audioQuality = it },
                                        accentColor = activeAccentColor,
                                    )
                                }

                                OnboardingPhase.STEP_ACCOUNT -> {
                                    StepAccountContent(
                                        accentColor = activeAccentColor,
                                    )
                                }

                                else -> {}
                            }
                        }

                        // Pill button actions at bottom (Matching Inspired setup.jpg)
                        Spacer(Modifier.height(12.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth().widthIn(max = 380.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            when (phase) {
                                OnboardingPhase.STEP_NAME_ACCENT -> {
                                    PillButtonPrimary(
                                        text = "Continue",
                                        accentColor = activeAccentColor,
                                        onClick = {
                                            val chosen = name.trim().ifBlank { "Listener" }
                                            focusManager.clearFocus()
                                            onNameChosen(chosen)
                                            phase = OnboardingPhase.POST_NAME_REVEAL
                                        },
                                    )
                                    PillButtonSecondary(
                                        text = "Skip",
                                        onClick = {
                                            val chosen = name.trim().ifBlank { "Listener" }
                                            onNameChosen(chosen)
                                            phase = OnboardingPhase.STEP_VIBE_AUDIO
                                        },
                                    )
                                }

                                OnboardingPhase.STEP_VIBE_AUDIO -> {
                                    PillButtonPrimary(
                                        text = "Continue",
                                        accentColor = activeAccentColor,
                                        onClick = { phase = OnboardingPhase.STEP_ACCOUNT },
                                    )
                                    PillButtonSecondary(
                                        text = "Back",
                                        onClick = { phase = OnboardingPhase.STEP_NAME_ACCENT },
                                    )
                                }

                                OnboardingPhase.STEP_ACCOUNT -> {
                                    PillButtonPrimary(
                                        text = "Sign in with Google",
                                        accentColor = activeAccentColor,
                                        hasGoogleIcon = true,
                                        onClick = onSignIn,
                                    )
                                    PillButtonSecondary(
                                        text = "Start Listening Offline",
                                        onClick = onFinish,
                                    )
                                }

                                else -> {}
                            }

                            // Terms caption
                            Text(
                                text = "By continuing you accept our Terms of Service and Privacy Policy",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.60f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Phase A: Native Compose Entrance Animation inspired by Back out [799BDC4].mp4
 * Smooth ease-out logo entrance, balanced pause, then horizontal blur exit into setup.
 */
@Composable
private fun EntranceLaunchAnimation(
    onAnimationComplete: () -> Unit,
) {
    val scale = remember { Animatable(0.92f) }
    val alpha = remember { Animatable(0f) }
    val exitOffset = remember { Animatable(0f) }
    val exitBlur = remember { Animatable(0f) }
    val exitAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // Logo Entrance
        scale.animateTo(1.0f, animationSpec = tween(650, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        alpha.animateTo(1.0f, animationSpec = tween(550, easing = FastOutSlowInEasing))
        delay(600)
        // Exit Transition with horizontal blur
        exitBlur.animateTo(16f, animationSpec = tween(420, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        delay(1150)
        exitOffset.animateTo(-100f, animationSpec = tween(380, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        delay(1200)
        exitAlpha.animateTo(0f, animationSpec = tween(350, easing = FastOutSlowInEasing))
        onAnimationComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(CanvasTop, CanvasBottom)),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .graphicsLayer {
                    translationX = exitOffset.value
                    this.alpha = alpha.value * exitAlpha.value
                    scaleX = scale.value
                    scaleY = scale.value
                }
                .blur(exitBlur.value.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_hush_transparent_logo),
                contentDescription = "Hush Music",
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(26.dp)),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Hush Music",
                fontFamily = CherryBombOne,
                fontSize = 38.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.5).sp,
            )
        }
    }
}

/**
 * Phase C: Post-Name Entry Identity Reveal Animation
 * Plain pure white (#FFFFFF) background, blur radius down to 0dp, scale 0.92 -> 1.0f,
 * and progressive left-to-right wordmark reveal in CherryBombOne-Regular.ttf.
 */
@Composable
private fun PostNameIdentityRevealAnimation(
    onAnimationComplete: () -> Unit,
) {
    val logoProgress = remember { Animatable(0f) }
    val textProgress = remember { Animatable(0f) }
    val fadeOutAll = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        delay(200) // Brief hold on white
        // Logo blur-to-sharp resolution & subtle scale
        logoProgress.animateTo(1f, animationSpec = tween(850, easing = FastOutSlowInEasing))
        // Progressive left-to-right wordmark reveal
        textProgress.animateTo(1f, animationSpec = tween(900, easing = FastOutSlowInEasing))
        delay(850)
        fadeOutAll.animateTo(0f, animationSpec = tween(350, easing = FastOutSlowInEasing))
        onAnimationComplete()
    }

    val iconAlpha = logoProgress.value
    val iconScale = 0.92f + 0.08f * logoProgress.value
    val iconBlur = (26f * (1f - logoProgress.value)).dp

    val brandName = "Hush Music"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .graphicsLayer { alpha = fadeOutAll.value },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Blurred-to-sharp silhouette resolving smoothly
            Image(
                painter = painterResource(R.drawable.ic_hush_transparent_logo),
                contentDescription = "Hush Music",
                modifier = Modifier
                    .size(92.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                        alpha = iconAlpha
                    }
                    .blur(iconBlur)
                    .clip(RoundedCornerShape(26.dp)),
            )

            Spacer(Modifier.height(20.dp))

            // Progressive Left-to-Right Wordmark Reveal
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                brandName.forEachIndexed { index, char ->
                    val charThreshold = index.toFloat() / brandName.length.toFloat()
                    val charProgress = ((textProgress.value - charThreshold * 0.45f) / 0.55f).coerceIn(0f, 1f)
                    val charAlpha = charProgress
                    val charBlur = (14f * (1f - charProgress)).dp

                    Text(
                        text = char.toString(),
                        fontFamily = CherryBombOne,
                        fontSize = 36.sp,
                        color = Color(0xFF16181D),
                        modifier = Modifier
                            .graphicsLayer { alpha = charAlpha }
                            .blur(charBlur),
                    )
                }
            }
        }
    }
}

/** 3-Dot Pagination Indicator matching Inspired setup.jpg */
@Composable
private fun PaginationDots(
    currentIndex: Int,
    accentColor: Color,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { index ->
            val active = index == currentIndex
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(if (active) 18.dp else 6.dp)
                    .clip(CircleShape)
                    .background(
                        if (active) Color.White else Color.White.copy(alpha = 0.35f),
                    ),
            )
        }
    }
}

/** Step 1: Display Name + Circular Accent Swatches right at the start */
@Composable
private fun StepNameAndAccentContent(
    name: String,
    onNameChange: (String) -> Unit,
    selectedAccent: String,
    onAccentChange: (String) -> Unit,
    onDone: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "What should we call you?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(10.dp))

        // Clean input pill matching Inspired setup.jpg
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 380.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(Color.White.copy(alpha = 0.18f))
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)), RoundedCornerShape(26.dp))
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (name.isEmpty()) {
                Text(
                    text = "Your name or handle",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.65f),
                )
            }
            BasicTextField(
                value = name,
                onValueChange = onNameChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                ),
                cursorBrush = SolidColor(Color.White),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { onDone() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(24.dp))

        // Accent Preference circular selector right at the start
        Text(
            text = "Choose your accent",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HushAccents.forEach { swatch ->
                val selected = swatch.id == selectedAccent
                val swatchColor = if (swatch.isDynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    dynamicDarkColorScheme(context).primary
                } else swatch.primary

                Column(
                    modifier = Modifier
                        .clickable { onAccentChange(swatch.id) }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(swatchColor)
                            .then(
                                if (selected) {
                                    Modifier.border(2.5.dp, Color.White, CircleShape)
                                } else {
                                    Modifier.border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                }
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (selected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = stringResource(R.string.selected),
                                tint = if (swatchColor.luminance() > 0.5f) Color.Black else Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                    Text(
                        text = swatch.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = if (selected) Color.White else Color.White.copy(alpha = 0.70f),
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(top = 4.dp).widthIn(max = 62.dp),
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

/** Step 2: Vibe & Audio Quality preferences */
@Composable
private fun StepVibeAudioContent(
    selectedGenres: Set<String>,
    onToggleGenre: (String) -> Unit,
    audioQuality: String,
    onQualityChange: (String) -> Unit,
    accentColor: Color,
) {
    val genres = listOf("Lo-Fi", "Electronic", "Indie", "Ambient", "Jazz", "Rock", "Classical", "R&B", "Pop", "Hip-Hop")
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Tailor your sound",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth().widthIn(max = 380.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            genres.forEach { genre ->
                val selected = selectedGenres.contains(genre)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (selected) Color.White.copy(alpha = 0.90f) else Color.White.copy(alpha = 0.16f),
                        )
                        .border(
                            BorderStroke(1.dp, if (selected) Color.White else Color.White.copy(alpha = 0.35f)),
                            RoundedCornerShape(20.dp),
                        )
                        .clickable { onToggleGenre(genre) }
                        .padding(horizontal = 16.dp, vertical = 9.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = genre,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) Color(0xFF16181D) else Color.White,
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Streaming Fidelity",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(10.dp))

        val options = listOf(
            "128" to "Standard (128 kbps)",
            "320" to "High (320 kbps)",
            "flac" to "Lossless (FLAC / Hi-Res)",
        )
        Column(
            modifier = Modifier.fillMaxWidth().widthIn(max = 380.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { (id, label) ->
                val selected = audioQuality == id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selected) Color.White.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.08f))
                        .border(BorderStroke(1.dp, if (selected) Color.White else Color.White.copy(alpha = 0.25f)), RoundedCornerShape(16.dp))
                        .clickable { onQualityChange(id) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .border(BorderStroke(1.5.dp, if (selected) Color.White else Color.White.copy(alpha = 0.4f)), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Step 3: Google Account Connection with Hush Music cards (ZERO Spotify cards) */
@Composable
private fun StepAccountContent(
    accentColor: Color,
) {
    Column(
        modifier = Modifier.fillMaxWidth().widthIn(max = 380.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Centered Hush Music emblem card
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(110.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.16f))
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_hush_transparent_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp)),
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Hush Music",
                        fontFamily = CherryBombOne,
                        fontSize = 22.sp,
                        color = Color.White,
                    )
                    Text(
                        text = "Lossless Audio Engine",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Connect Your Account",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Link your YouTube Music / Google account to stream your personal library, playlists, and tailored mixes.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.80f),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

/** Soft-filled primary pill button matching Inspired setup.jpg */
@Composable
private fun PillButtonPrimary(
    text: String,
    accentColor: Color,
    hasGoogleIcon: Boolean = false,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
        label = "pillPrimaryScale",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(26.dp))
            .background(Color.White.copy(alpha = 0.92f))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (hasGoogleIcon) {
                Image(
                    painter = painterResource(R.drawable.ic_google_g),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF16181D),
            )
        }
    }
}

/** Thin outlined secondary pill button matching Inspired setup.jpg */
@Composable
private fun PillButtonSecondary(
    text: String,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
        label = "pillSecondaryScale",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(26.dp))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.55f)), RoundedCornerShape(26.dp))
            .background(Color.Transparent)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = Color.White,
        )
    }
}
