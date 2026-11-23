package com.music.bitchord.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.bitchord.R
import com.music.bitchord.sharedui.resources.Res
import com.music.bitchord.sharedui.resources.ic_google_g
import com.music.bitchord.ui.icons.BitChordIcons
import org.jetbrains.compose.resources.painterResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 3-step setup / onboarding experience modeled after video_1.mp4:
 * 1. IntroPage: Native Compose dot-matrix particle animation + Go button.
 * 2. NamePage: "What should we call you?" name entry.
 * 3. ConnectPage: YouTube Music / Google sign-in with Maker easter egg.
 *
 * All steps share the same continuous, slowly breathing monochrome gradient background
 * (#0D0D0F -> #242428 -> #121215).
 */
@Composable
fun OnboardingScreen(
    signedIn: Boolean,
    initialName: String,
    onNameChosen: (String) -> Unit,
    onSignIn: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var page by rememberSaveable { mutableIntStateOf(PAGE_INTRO) }
    var name by rememberSaveable { mutableStateOf(initialName) }
    val focusManager = LocalFocusManager.current

    BackHandler(enabled = page != PAGE_INTRO) {
        page = if (page == PAGE_CONNECT) PAGE_NAME else PAGE_INTRO
    }

    LaunchedEffect(signedIn, page) {
        if (signedIn && page == PAGE_CONNECT) onFinish()
    }

    Box(
        modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0F))
            .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } },
    ) {
        // Live Breathing Monochrome Gradient Background persistent across all steps
        BreathingMonochromeGradient(Modifier.fillMaxSize())

        AnimatedContent(
            targetState = page,
            transitionSpec = {
                val forward = targetState > initialState
                val slide = tween<IntOffset>(420)
                (slideInHorizontally(slide) { if (forward) it / 3 else -it / 3 } + fadeIn(tween(320, 100)))
                    .togetherWith(
                        slideOutHorizontally(slide) { if (forward) -it / 3 else it / 3 } + fadeOut(tween(200)),
                    )
            },
            label = "onboardingPage",
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding(),
        ) { shown ->
            OnboardingPage {
                when (shown) {
                    PAGE_INTRO -> IntroPage(
                        onGo = { page = PAGE_NAME },
                    )
                    PAGE_NAME -> NamePage(
                        name = name,
                        onNameChange = { name = it.take(NAME_LIMIT) },
                        onNext = next@{
                            val chosen = name.trim()
                            if (chosen.isEmpty()) return@next
                            focusManager.clearFocus()
                            onNameChosen(chosen)
                            if (signedIn) onFinish() else page = PAGE_CONNECT
                        },
                    )
                    else -> ConnectPage(
                        name = name,
                        onBack = { page = PAGE_NAME },
                        onSignIn = onSignIn,
                        onSkip = onFinish,
                    )
                }
            }
        }
    }
}

/**
 * GPU-rendered breathing monochrome gradient shifting smoothly between
 * #0D0D0F, #242428, and #121215 with slow organic orbital translation.
 */
@Composable
private fun BreathingMonochromeGradient(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "monochromeBreathing")
    val translationX by infiniteTransition.animateFloat(
        initialValue = -0.15f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathX",
    )
    val translationY by infiniteTransition.animateFloat(
        initialValue = -0.1f,
        targetValue = 0.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathY",
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val centerX = w * (0.5f + translationX)
        val centerY = h * (0.42f + translationY)
        val radius = maxOf(w, h) * 0.85f

        val radialBrush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF28282D),
                Color(0xFF1B1B1E),
                Color(0xFF0F0F12),
                Color(0xFF0A0A0C),
            ),
            center = Offset(centerX, centerY),
            radius = radius,
        )
        drawRect(brush = radialBrush)
    }
}

@Composable
private fun OnboardingPage(content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .fillMaxSize()
                .widthIn(max = 520.dp)
                .padding(horizontal = 28.dp),
            content = content,
        )
    }
}

/**
 * Step 1: Native Dot-Matrix Particle Formation Animation (video_1.mp4) + Opaque Pure-White "Go" Pill Button.
 */
@Composable
private fun ColumnScope.IntroPage(
    onGo: () -> Unit,
) {
    val animProgress = remember { Animatable(0f) }
    var settled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
        )
        settled = true
    }

    Spacer(Modifier.weight(0.85f))

    // Dot-Matrix Logo Particle Canvas (video_1.mp4)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        contentAlignment = Alignment.Center,
    ) {
        DotMatrixLogoAnimation(
            progress = animProgress.value,
            settled = settled,
            modifier = Modifier.size(240.dp),
        )
    }

    Spacer(Modifier.height(18.dp))

    // Subtitle text (fades in as dots settle)
    AnimatedVisibility(
        visible = animProgress.value > 0.7f,
        enter = fadeIn(tween(400)),
        exit = fadeOut(),
        modifier = Modifier.align(Alignment.CenterHorizontally),
    ) {
        Text(
            text = "Hush Music",
            style = MaterialTheme.typography.displaySmall.copy(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
            ),
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }

    Spacer(Modifier.weight(1.15f))

    // Solid Opaque Pure-White Pill Button: "Go" (#FFFFFF fill, #0D0D0F bold text)
    AnimatedVisibility(
        visible = settled,
        enter = fadeIn(spring(stiffness = 400f)) + slideInVertically(spring(stiffness = 350f)) { it / 2 },
        exit = fadeOut(tween(150)),
    ) {
        PillButton(
            label = "Go",
            onClick = onGo,
            primary = true,
        )
    }

    Spacer(Modifier.height(28.dp))
}

/**
 * Exact Canvas 10-particle physics engine matching video_1.mp4:
 * 10 dots orbit, spiral, converge, and snap into the Hush Music dot-matrix "h" mark + side soundbars.
 */
@Composable
private fun DotMatrixLogoAnimation(
    progress: Float,
    settled: Boolean,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dotBreathing")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dotBreath",
    )

    // The 10 Target Coordinates (normalized to scale 1.0f) forming Hush Music "h" + side soundbars
    val targetCoords = remember {
        listOf(
            Offset(-40f, 4f),   // 0: Left soundbar dot
            Offset(40f, 4f),    // 1: Right soundbar dot
            Offset(-16f, -36f), // 2: "h" stem top
            Offset(-16f, -16f), // 3: "h" stem upper-mid
            Offset(-16f, 4f),   // 4: "h" stem center
            Offset(-16f, 24f),  // 5: "h" stem bottom
            Offset(0f, -16f),   // 6: "h" arch apex
            Offset(0f, 4f),     // 7: "h" center dot
            Offset(16f, 4f),    // 8: "h" right curve
            Offset(16f, 24f),   // 9: "h" right leg
        )
    }

    // Base orbit angles and radii for the 10 particles
    val orbitData = remember {
        List(10) { i ->
            val angle = (i.toFloat() * (2.0 * PI / 10.0)).toFloat()
            val radius = 58f + ((i % 3) * 6f)
            val speed = 1.6f + ((i % 2) * 0.4f)
            Triple(angle, radius, speed)
        }
    }

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val s = (size.minDimension / 140f) * (if (settled) breathScale else 1f)
        val dotRadius = 4.8.dp.toPx()

        for (i in 0 until 10) {
            val target = targetCoords[i]
            val (baseAngle, baseRadius, speed) = orbitData[i]

            val currentOffset = if (progress < 0.65f) {
                // Phase 1: Orbital spinning around center
                val t = progress / 0.65f
                val currentAngle = baseAngle + t * speed * PI.toFloat()
                val currentRadius = (baseRadius * (1f - t * 0.25f)) * s
                Offset(
                    cx + currentRadius * cos(currentAngle),
                    cy + currentRadius * sin(currentAngle),
                )
            } else {
                // Phase 2: Converge & snap into target dot-matrix position
                val tSnap = ((progress - 0.65f) / 0.35f).coerceIn(0f, 1f)
                val ease = FastOutSlowInEasing.transform(tSnap)

                // Exit orbital position
                val exitAngle = baseAngle + speed * PI.toFloat()
                val exitRadius = (baseRadius * 0.75f) * s
                val startX = cx + exitRadius * cos(exitAngle)
                val startY = cy + exitRadius * sin(exitAngle)

                val targetX = cx + target.x * s
                val targetY = cy + target.y * s

                Offset(
                    startX + (targetX - startX) * ease,
                    startY + (targetY - startY) * ease,
                )
            }

            // Draw outer ambient halo
            drawCircle(
                color = Color.White.copy(alpha = 0.18f),
                radius = dotRadius * 1.55f,
                center = currentOffset,
            )
            // Draw crisp solid particle
            drawCircle(
                color = Color.White.copy(alpha = 0.95f),
                radius = dotRadius,
                center = currentOffset,
            )
        }
    }
}

/**
 * Step 2: Name Entry ("What should we call you?") on Live Monochrome Gradient.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColumnScope.NamePage(
    name: String,
    onNameChange: (String) -> Unit,
    onNext: () -> Unit,
) {
    val keyboardUp = WindowInsets.isImeVisible
    AnimatedVisibility(
        visible = !keyboardUp,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        Column {
            Spacer(Modifier.height(44.dp))
            Image(
                painter = androidx.compose.ui.res.painterResource(R.drawable.ic_hush_transparent_logo),
                contentDescription = "Hush Music",
                modifier = Modifier.size(48.dp),
            )
        }
    }
    Spacer(Modifier.height(20.dp))
    Title(stringResource(R.string.onboarding_name_prompt))

    Spacer(Modifier.weight(1f))

    NameField(value = name, onValueChange = onNameChange, onDone = onNext)
    Spacer(Modifier.height(28.dp))
    PillButton(
        label = stringResource(R.string.onboarding_next),
        onClick = onNext,
        enabled = name.isNotBlank(),
        primary = true,
    )
    Spacer(Modifier.height(16.dp))
}

/**
 * Step 3: Google Sign-In on Live Monochrome Gradient with "Welcome, Maker" Easter Egg.
 */
@Composable
private fun ColumnScope.ConnectPage(
    name: String,
    onBack: () -> Unit,
    onSignIn: () -> Unit,
    onSkip: () -> Unit,
) {
    val isMaker = name.trim().equals("phoenixeditz1357@gmail.com", ignoreCase = true) ||
        name.trim().contains("phoenix", ignoreCase = true)

    Spacer(Modifier.height(16.dp))
    BackButton(onBack)
    Spacer(Modifier.height(20.dp))
    Title(stringResource(R.string.onboarding_connect_title))

    if (isMaker) {
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color(0xFFEAB308).copy(alpha = 0.22f))
                .padding(horizontal = 14.dp, vertical = 6.dp),
        ) {
            Text(
                text = "Welcome, Maker 👑",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFDE047),
                ),
            )
        }
    }

    Spacer(Modifier.height(14.dp))

    Spacer(Modifier.weight(1f))

    PillButton(
        label = stringResource(R.string.onboarding_sign_in_google),
        onClick = onSignIn,
        primary = true,
        leading = {
            Image(
                painter = painterResource(Res.drawable.ic_google_g),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        },
    )
    Spacer(Modifier.height(12.dp))
    PillButton(
        label = stringResource(R.string.onboarding_skip_sign_in),
        onClick = onSkip,
        primary = false,
    )
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun Title(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.displayLarge.copy(
            fontSize = 38.sp,
            lineHeight = 42.sp,
            letterSpacing = (-1.1).sp,
        ),
        color = Color.White,
    )
}

@Composable
private fun NameField(value: String, onValueChange: (String) -> Unit, onDone: () -> Unit) {
    val style = MaterialTheme.typography.displayLarge.copy(
        fontSize = 26.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.6).sp,
        color = Color.White,
    )
    Box(
        Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.14f))
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (value.isEmpty()) {
            Text(
                text = stringResource(R.string.onboarding_name_hint),
                style = style,
                color = Color.White.copy(alpha = 0.38f),
                maxLines = 1,
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = style,
            cursorBrush = SolidColor(Color.White),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(onNext = { onDone() }),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Solid Pure-White Pill Button (#FFFFFF fill, #0D0D0F bold text) for primary actions.
 */
@Composable
private fun PillButton(
    label: String,
    onClick: () -> Unit,
    primary: Boolean,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, spring(stiffness = 900f), label = "pillScale")
    val alpha by animateFloatAsState(if (enabled) 1f else 0.4f, tween(180), label = "pillAlpha")

    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .clip(CircleShape)
            .background(if (primary) Color.White else Color.White.copy(alpha = 0.16f))
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(10.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontSize = 17.sp,
            fontWeight = FontWeight.W700,
            color = if (primary) Color(0xFF0D0D0F) else Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
private fun BackButton(onClick: () -> Unit) {
    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.16f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = BitChordIcons.ChevronRight,
            contentDescription = stringResource(R.string.onboarding_back),
            tint = Color.White,
            modifier = Modifier
                .size(18.dp)
                .graphicsLayer { scaleX = -1f },
        )
    }
}

private const val PAGE_INTRO = 0
private const val PAGE_NAME = 1
private const val PAGE_CONNECT = 2
private const val NAME_LIMIT = 40
