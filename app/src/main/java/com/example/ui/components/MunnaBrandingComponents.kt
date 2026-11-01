package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speech.MicStatus
import com.example.ui.theme.MunnaCardBorder
import com.example.ui.theme.MunnaCardElevated
import com.example.ui.theme.MunnaCardNavy
import com.example.ui.theme.MunnaCyanGlow
import com.example.ui.theme.MunnaCyanSoft
import com.example.ui.theme.MunnaErrorRed
import com.example.ui.theme.MunnaNavyDeep
import com.example.ui.theme.MunnaSkyBlue
import com.example.ui.theme.MunnaSkyBlueBright
import com.example.ui.theme.MunnaSkyBlueDeep
import com.example.ui.theme.MunnaSuccessGreen
import com.example.ui.theme.MunnaTextSecondary
import com.example.ui.theme.MunnaTextWhite
import kotlin.math.roundToInt

/**
 * Custom Canvas-drawn Munna AI friendly 3D-styled robot mascot logo
 * with glowing antenna orb, sky-blue headphones, visor, happy cyan eyes, and 'M' chest badge.
 */
@Composable
fun MunnaRobotLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    isAnimated: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "robot_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = if (isAnimated) 1.0f else 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eye_glow"
    )

    Box(
        modifier = modifier
            .size(size)
            .shadow(6.dp, CircleShape, ambientColor = MunnaCyanGlow, spotColor = MunnaSkyBlue)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(MunnaSkyBlueDeep, MunnaCardNavy, MunnaNavyDeep)
                )
            )
            .border(1.8.dp, MunnaCyanGlow.copy(alpha = 0.85f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
            val w = this.size.width
            val h = this.size.height

            // 1. Antenna stem & glowing cyan orb at top
            drawRoundRect(
                color = Color(0xFF94A3B8),
                topLeft = Offset(w * 0.47f, h * 0.08f),
                size = Size(w * 0.06f, h * 0.12f),
                cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
            )
            drawCircle(
                color = MunnaCyanGlow.copy(alpha = glowAlpha),
                radius = w * 0.065f,
                center = Offset(w * 0.50f, h * 0.07f)
            )

            // 2. Side headphones (left & right)
            drawRoundRect(
                color = MunnaSkyBlueBright,
                topLeft = Offset(w * 0.05f, h * 0.30f),
                size = Size(w * 0.12f, h * 0.32f),
                cornerRadius = CornerRadius(w * 0.05f, w * 0.05f)
            )
            drawRoundRect(
                color = MunnaSkyBlueBright,
                topLeft = Offset(w * 0.83f, h * 0.30f),
                size = Size(w * 0.12f, h * 0.32f),
                cornerRadius = CornerRadius(w * 0.05f, w * 0.05f)
            )

            // 3. Robot white helmet
            drawRoundRect(
                color = Color(0xFFF8FAFC),
                topLeft = Offset(w * 0.15f, h * 0.17f),
                size = Size(w * 0.70f, h * 0.56f),
                cornerRadius = CornerRadius(w * 0.24f, w * 0.24f)
            )

            // 4. Dark navy visor screen
            drawRoundRect(
                color = Color(0xFF071124),
                topLeft = Offset(w * 0.22f, h * 0.26f),
                size = Size(w * 0.56f, h * 0.38f),
                cornerRadius = CornerRadius(w * 0.16f, w * 0.16f)
            )

            // 5. Happy glowing cyan eyes (curved arcs)
            val eyeColor = MunnaCyanGlow.copy(alpha = glowAlpha)
            drawArc(
                color = eyeColor,
                startAngle = 195f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(w * 0.28f, h * 0.35f),
                size = Size(w * 0.16f, h * 0.15f),
                style = Stroke(width = w * 0.065f, cap = StrokeCap.Round)
            )
            drawArc(
                color = eyeColor,
                startAngle = 195f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(w * 0.56f, h * 0.35f),
                size = Size(w * 0.16f, h * 0.15f),
                style = Stroke(width = w * 0.065f, cap = StrokeCap.Round)
            )

            // 6. Friendly smile arc in visor
            drawArc(
                color = MunnaSkyBlue.copy(alpha = 0.95f),
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(w * 0.41f, h * 0.46f),
                size = Size(w * 0.18f, h * 0.09f),
                style = Stroke(width = w * 0.045f, cap = StrokeCap.Round)
            )

            // 7. Robot chest collar & glowing 'M' badge at bottom
            drawRoundRect(
                color = Color(0xFFE2E8F0),
                topLeft = Offset(w * 0.28f, h * 0.74f),
                size = Size(w * 0.44f, h * 0.20f),
                cornerRadius = CornerRadius(w * 0.10f, w * 0.10f)
            )
            drawCircle(
                color = MunnaSkyBlueDeep,
                radius = w * 0.11f,
                center = Offset(w * 0.50f, h * 0.84f)
            )
            // Draw 'M' inside chest badge
            val mPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.44f, h * 0.89f)
                lineTo(w * 0.44f, h * 0.79f)
                lineTo(w * 0.50f, h * 0.85f)
                lineTo(w * 0.56f, h * 0.79f)
                lineTo(w * 0.56f, h * 0.89f)
            }
            drawPath(
                path = mPath,
                color = Color.White,
                style = Stroke(width = w * 0.035f, cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * Central Animated Microphone Button with soft cyan glow and sound-wave bars (Requirement 3 & 13).
 */
@Composable
fun CentralAnimatedMicrophone(
    status: MicStatus,
    rmsDb: Float,
    isSpeaking: Boolean,
    onMicClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isActive = status == MicStatus.LISTENING || status == MicStatus.PROCESSING || isSpeaking
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")

    val outerScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isActive) 1.24f else 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isActive) 750 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "outer_scale"
    )

    val middleScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isActive) 1.14f else 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isActive) 550 else 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "middle_scale"
    )

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_phase"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Left Sound Wave Equalizer Bars
            SoundWaveBars(
                isActive = isActive,
                wavePhase = wavePhase + rmsDb * 0.5f,
                mirrored = false
            )

            Spacer(modifier = Modifier.width(18.dp))

            // Central Glowing Microphone Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(164.dp)
            ) {
                // Outer Soft Cyan Glow Halo
                Box(
                    modifier = Modifier
                        .size(152.dp)
                        .scale(outerScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MunnaCyanGlow.copy(alpha = if (isActive) 0.34f else 0.16f),
                                    MunnaSkyBlue.copy(alpha = if (isActive) 0.14f else 0.05f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Middle Glow Ring
                Box(
                    modifier = Modifier
                        .size(126.dp)
                        .scale(middleScale)
                        .clip(CircleShape)
                        .background(
                            MunnaSkyBlue.copy(alpha = if (isActive) 0.26f else 0.12f)
                        )
                        .border(
                            width = 1.5.dp,
                            color = MunnaCyanGlow.copy(alpha = if (isActive) 0.65f else 0.30f),
                            shape = CircleShape
                        )
                )

                // Core Tappable Circular Button (104.dp -> comfortably exceeds 48.dp accessibility target)
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .shadow(
                            elevation = if (isActive) 22.dp else 12.dp,
                            shape = CircleShape,
                            ambientColor = MunnaCyanGlow,
                            spotColor = MunnaSkyBlue
                        )
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = when (status) {
                                    MicStatus.LISTENING -> listOf(MunnaCyanGlow, MunnaSkyBlueBright, MunnaSkyBlueDeep)
                                    MicStatus.ERROR -> listOf(Color(0xFFEF4444), Color(0xFFB91C1C))
                                    MicStatus.COMPLETED -> listOf(MunnaSuccessGreen, MunnaSkyBlueDeep)
                                    else -> listOf(MunnaSkyBlue, MunnaSkyBlueBright, Color(0xFF0369A1))
                                }
                            )
                        )
                        .border(
                            width = 2.5.dp,
                            color = MunnaCyanSoft.copy(alpha = 0.85f),
                            shape = CircleShape
                        )
                        .clickable { onMicClick() }
                        .semantics {
                            contentDescription = "बोलने के लिए माइक बटन दबाएँ (Tap microphone to speak)"
                        }
                        .testTag("central_mic_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (status == MicStatus.LISTENING) Icons.Default.GraphicEq else Icons.Default.Mic,
                        contentDescription = null,
                        tint = MunnaTextWhite,
                        modifier = Modifier.size(46.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(18.dp))

            // Right Sound Wave Equalizer Bars
            SoundWaveBars(
                isActive = isActive,
                wavePhase = wavePhase + rmsDb * 0.5f,
                mirrored = true
            )
        }

        // Cancel Listening Pill when actively listening
        if (status == MicStatus.LISTENING) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                onClick = onCancelClick,
                shape = RoundedCornerShape(50),
                color = MunnaErrorRed.copy(alpha = 0.18f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MunnaErrorRed.copy(alpha = 0.6f)),
                modifier = Modifier.testTag("cancel_listening_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "सुनना बंद करें",
                        tint = MunnaErrorRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "सुनना रद्द करें (Cancel)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MunnaErrorRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SoundWaveBars(
    isActive: Boolean,
    wavePhase: Float,
    mirrored: Boolean
) {
    val baseHeights = if (mirrored) {
        listOf(34.dp, 22.dp, 14.dp, 26.dp)
    } else {
        listOf(26.dp, 14.dp, 22.dp, 34.dp)
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        baseHeights.forEachIndexed { idx, baseHeight ->
            val multiplier = if (isActive) {
                val factor = ((wavePhase + idx * 0.22f) % 1.0f).coerceIn(0.35f, 1.25f)
                factor
            } else {
                0.45f
            }
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(baseHeight * multiplier)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isActive) MunnaCyanGlow else MunnaSkyBlue.copy(alpha = 0.38f)
                    )
            )
        }
    }
}

/**
 * Draggable Floating Microphone button near the right edge of the screen (Requirement 6).
 *
 * - Sky-blue circular button with white microphone icon and soft cyan glow.
 * - Draggable position so it never covers important controls.
 * - Tap to start/stop voice recognition.
 * - Option to hide/stop the floating button.
 */
@Composable
fun DraggableFloatingMicOverlay(
    status: MicStatus,
    onTapMic: () -> Unit,
    onHideFloatingMic: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val isListening = status == MicStatus.LISTENING
    val infiniteTransition = rememberInfiniteTransition(label = "floating_mic_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isListening) 1.15f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating_pulse"
    )

    Column(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX = (offsetX + dragAmount.x).coerceIn(-700f, 40f)
                    offsetY = (offsetY + dragAmount.y).coerceIn(-900f, 400f)
                }
            }
            .testTag("floating_mic_container"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Active status micro-pill
        Surface(
            color = MunnaCardElevated.copy(alpha = 0.92f),
            shape = RoundedCornerShape(50),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isListening) MunnaCyanGlow else MunnaCardBorder
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isListening) "सुन रहा हूँ…" else "मुन्ना माइक",
                    fontSize = 10.sp,
                    color = if (isListening) MunnaCyanSoft else MunnaTextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "फ़्लोटिंग माइक छुपाएँ (Hide floating mic)",
                    tint = MunnaTextSecondary,
                    modifier = Modifier
                        .size(13.dp)
                        .clickable { onHideFloatingMic() }
                        .testTag("hide_floating_mic_button")
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Sky-blue circular floating mic button with soft cyan glow
        Box(
            modifier = Modifier
                .size(56.dp)
                .scale(pulseScale)
                .shadow(
                    elevation = 14.dp,
                    shape = CircleShape,
                    ambientColor = MunnaCyanGlow,
                    spotColor = MunnaSkyBlue
                )
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = if (isListening) {
                            listOf(MunnaCyanGlow, MunnaSkyBlueBright)
                        } else {
                            listOf(MunnaSkyBlue, MunnaSkyBlueDeep)
                        }
                    )
                )
                .border(2.dp, MunnaCyanSoft, CircleShape)
                .clickable { onTapMic() }
                .semantics {
                    contentDescription = "फ़्लोटिंग माइक दबाएँ (Floating Microphone)"
                }
                .testTag("floating_mic_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
