package com.lastwave.app.ui.player

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.lastwave.app.data.local.SeekbarStyle
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun WavySeekBar(
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    isTranslucent: Boolean = false,
    trackKey: String? = null,
    showTimeLabels: Boolean = true,
    seekbarStyle: SeekbarStyle = SeekbarStyle.WAVY_FLUID,
    ambientColor: Color? = null,
) {
    val interactionSource = remember(trackKey) { MutableInteractionSource() }
    val dragging by interactionSource.collectIsDraggedAsState()
    var dragPositionMs by remember(trackKey) { mutableFloatStateOf(0f) }

    val boundedDurationMs = durationMs.coerceAtLeast(0L)
    val boundedPositionMs = if (boundedDurationMs > 0L) {
        positionMs.coerceIn(0L, boundedDurationMs)
    } else {
        0L
    }
    val shownMs = if (dragging) {
        dragPositionMs.toLong().coerceIn(0L, boundedDurationMs)
    } else {
        boundedPositionMs
    }
    val shownFraction = if (boundedDurationMs > 0L) {
        (shownMs.toDouble() / boundedDurationMs.toDouble()).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    val primaryColor = ambientColor ?: Color(0xFF00F0FF)
    val secondaryColor = MaterialTheme.colorScheme.secondary.takeIf { ambientColor != null } ?: Color(0xFFFF2E93)
    val inactiveColor = Color.White.copy(alpha = 0.22f)
    val textColor = Color(0xFFCBD5E1)

    val infiniteTransition = rememberInfiniteTransition(label = "WaveAnimation")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "Phase",
    )

    val density = LocalDensity.current
    val activeWaveAmpPx = if (isPlaying && !dragging) with(density) { 7.dp.toPx() } else 0f
    val waveAmpPx by animateFloatAsState(
        targetValue = activeWaveAmpPx,
        animationSpec = spring(stiffness = 300f, dampingRatio = 0.7f),
        label = "waveAmplitude",
    )
    val wavePeriodPx = with(density) { 28.dp.toPx() }

    Column(modifier = modifier.fillMaxWidth()) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val width = size.width
                    val height = size.height
                    val centerY = height / 2f
                    val thumbX = (shownFraction * width).coerceIn(0f, width)

                    when (seekbarStyle) {
                        SeekbarStyle.WAVY_FLUID -> {
                            val trackWidthPx = 4.dp.toPx()

                            // Inactive line
                            drawLine(
                                color = inactiveColor,
                                start = Offset(thumbX, centerY),
                                end = Offset(width, centerY),
                                strokeWidth = trackWidthPx,
                                cap = StrokeCap.Round,
                            )

                            // Active squiggly path
                            if (thumbX > 0f) {
                                val wavePath = Path()
                                wavePath.moveTo(0f, centerY)
                                var x = 0f
                                val step = 2.dp.toPx()
                                while (x <= thumbX) {
                                    val waveY = centerY + sin((x / wavePeriodPx) * 2 * PI + phase).toFloat() * waveAmpPx
                                    wavePath.lineTo(x, waveY)
                                    x += step
                                }
                                wavePath.lineTo(thumbX, centerY)

                                val activeBrush = Brush.horizontalGradient(
                                    colors = listOf(primaryColor, secondaryColor),
                                    startX = 0f,
                                    endX = thumbX,
                                )
                                drawPath(
                                    path = wavePath,
                                    brush = activeBrush,
                                    style = Stroke(width = trackWidthPx, cap = StrokeCap.Round),
                                )
                            }

                            // Glowing thumb
                            drawCircle(
                                color = primaryColor,
                                radius = 8.dp.toPx(),
                                center = Offset(thumbX, centerY),
                            )
                        }

                        SeekbarStyle.SEGMENTED_DASH -> {
                            val segmentCount = 32
                            val gap = 2.dp.toPx()
                            val totalGap = gap * (segmentCount - 1)
                            val segmentWidth = (width - totalGap) / segmentCount
                            val segHeight = 8.dp.toPx()

                            for (i in 0 until segmentCount) {
                                val segLeft = i * (segmentWidth + gap)
                                val isFilled = (segLeft + segmentWidth / 2f) <= thumbX
                                val color = if (isFilled) primaryColor else inactiveColor

                                drawRoundRect(
                                    color = color,
                                    topLeft = Offset(segLeft, centerY - segHeight / 2f),
                                    size = Size(segmentWidth, segHeight),
                                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                                )
                            }
                            drawCircle(
                                color = primaryColor,
                                radius = 8.dp.toPx(),
                                center = Offset(thumbX, centerY),
                            )
                        }

                        SeekbarStyle.MINIMAL_PILL -> {
                            val pillHeight = if (dragging) 8.dp.toPx() else 3.dp.toPx()
                            drawLine(
                                color = inactiveColor,
                                start = Offset(0f, centerY),
                                end = Offset(width, centerY),
                                strokeWidth = pillHeight,
                                cap = StrokeCap.Round,
                            )
                            if (thumbX > 0f) {
                                drawLine(
                                    color = primaryColor,
                                    start = Offset(0f, centerY),
                                    end = Offset(thumbX, centerY),
                                    strokeWidth = pillHeight,
                                    cap = StrokeCap.Round,
                                )
                            }
                            drawCircle(
                                color = primaryColor,
                                radius = if (dragging) 9.dp.toPx() else 5.dp.toPx(),
                                center = Offset(thumbX, centerY),
                            )
                        }

                        SeekbarStyle.STUDIO_CONSOLE -> {
                            val barCount = 56
                            val gap = 2.dp.toPx()
                            val totalGap = gap * (barCount - 1)
                            val barWidth = (width - totalGap) / barCount
                            val maxBarHeight = 32.dp.toPx()

                            for (i in 0 until barCount) {
                                val seed = (trackKey?.hashCode() ?: 42) + i * 31
                                val sinVal = sin(i * 0.2) * 0.4 + 0.5
                                val hashVal = (kotlin.math.abs(seed) % 100) / 100.0
                                val amp = ((sinVal * 0.6 + hashVal * 0.4).toFloat() * 0.85f + 0.15f).coerceIn(0.15f, 1.0f)

                                val barX = i * (barWidth + gap)
                                val isPlayed = (barX + barWidth / 2f) <= thumbX
                                val h = amp * maxBarHeight
                                val barColor = if (isPlayed) primaryColor else inactiveColor

                                drawRoundRect(
                                    color = barColor,
                                    topLeft = Offset(barX, centerY - h / 2f),
                                    size = Size(barWidth, h),
                                    cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx()),
                                )
                            }

                            // Interactive playhead needle
                            drawLine(
                                color = secondaryColor,
                                start = Offset(thumbX, centerY - maxBarHeight / 2f - 2.dp.toPx()),
                                end = Offset(thumbX, centerY + maxBarHeight / 2f + 2.dp.toPx()),
                                strokeWidth = 2.5.dp.toPx(),
                                cap = StrokeCap.Round,
                            )
                        }

                        SeekbarStyle.CAPSULE_PILL -> {
                            val pillH = 12.dp.toPx()
                            val cRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            drawRoundRect(
                                color = inactiveColor,
                                topLeft = Offset(0f, centerY - pillH / 2f),
                                size = Size(width, pillH),
                                cornerRadius = cRadius,
                            )
                            if (thumbX > 0f) {
                                val activeBrush = Brush.horizontalGradient(
                                    colors = listOf(primaryColor, secondaryColor),
                                    startX = 0f,
                                    endX = thumbX,
                                )
                                drawRoundRect(
                                    brush = activeBrush,
                                    topLeft = Offset(0f, centerY - pillH / 2f),
                                    size = Size(thumbX, pillH),
                                    cornerRadius = cRadius,
                                )
                            }
                            drawCircle(
                                color = primaryColor,
                                radius = 9.dp.toPx(),
                                center = Offset(thumbX, centerY),
                            )
                        }

                        SeekbarStyle.PRISM_SPECTRUM -> {
                            val ribbonH = 8.dp.toPx()
                            val cRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            drawRoundRect(
                                color = inactiveColor,
                                topLeft = Offset(0f, centerY - ribbonH / 2f),
                                size = Size(width, ribbonH),
                                cornerRadius = cRadius,
                            )
                            if (thumbX > 0f) {
                                val prismBrush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF00F0FF),
                                        Color(0xFFFF2E93),
                                        Color(0xFFFFC700),
                                    ),
                                    startX = 0f,
                                    endX = thumbX,
                                )
                                drawRoundRect(
                                    brush = prismBrush,
                                    topLeft = Offset(0f, centerY - ribbonH / 2f),
                                    size = Size(thumbX, ribbonH),
                                    cornerRadius = cRadius,
                                )
                            }
                            drawCircle(
                                color = Color(0xFF00F0FF),
                                radius = 9.dp.toPx(),
                                center = Offset(thumbX, centerY),
                            )
                        }

                        SeekbarStyle.NUVIA_WAVEFORM -> {
                            val barCount = 48
                            val gap = 3.dp.toPx()
                            val totalGap = gap * (barCount - 1)
                            val barWidth = (width - totalGap) / barCount
                            val maxBarHeight = 36.dp.toPx()
                            for (i in 0 until barCount) {
                                val barX = i * (barWidth + gap)
                                val isPlayed = (barX + barWidth / 2f) <= thumbX
                                val dynamicAmp = ((sin(i * 0.25 + phase) * 0.4 + 0.6).toFloat() * 0.85f).coerceIn(0.2f, 1f)
                                val h = dynamicAmp * maxBarHeight
                                val barBrush = if (isPlayed) {
                                    Brush.verticalGradient(listOf(primaryColor, secondaryColor))
                                } else {
                                    Brush.verticalGradient(listOf(inactiveColor, inactiveColor.copy(alpha = 0.1f)))
                                }
                                drawRoundRect(
                                    brush = barBrush,
                                    topLeft = Offset(barX, centerY - h / 2f),
                                    size = Size(barWidth, h),
                                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                                )
                            }
                            drawCircle(
                                color = primaryColor,
                                radius = 7.dp.toPx(),
                                center = Offset(thumbX, centerY),
                            )
                        }
                    }
                }

                Slider(
                    value = shownFraction,
                    onValueChange = { frac ->
                        dragPositionMs = frac * boundedDurationMs
                    },
                    onValueChangeFinished = {
                        onSeek(dragPositionMs.toLong().coerceIn(0L, boundedDurationMs))
                    },
                    interactionSource = interactionSource,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.Transparent,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent,
                    ),
                    modifier = Modifier
                        .matchParentSize()
                        .alpha(0f),
                )
            }

            if (showTimeLabels) {
                Spacer(Modifier.height(2.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        formatMs(shownMs),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                    )
                    Text(
                        formatMs(boundedDurationMs),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                    )
                }
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

private fun buildHarmonicSinePath(activeWidth: Float, height: Float, densityPx: Float): Path {
    val path = Path()
    if (activeWidth <= 0f) return path
    val centerY = height / 2f
    val wavePeriodPx = 28f * densityPx
    val waveAmpPx = 7f * densityPx
    path.moveTo(0f, centerY)
    var x = 0f
    val step = 2f * densityPx
    while (x <= activeWidth) {
        val waveY = centerY + sin((x / wavePeriodPx) * 2 * PI).toFloat() * waveAmpPx
        path.lineTo(x, waveY)
        x += step
    }
    path.lineTo(activeWidth, centerY)
    return path
}

@Composable
fun WavySeekbar(
    progress: Float,
    durationMs: Long,
    positionMs: Long,
    onSeek: (Float) -> Unit,
    ambientColor: Color?,
    modifier: Modifier = Modifier,
) {
    // Dynamic palette extracted from album art; fallback to dynamic primary
    val trackAccent = ambientColor ?: MaterialTheme.colorScheme.primary
    val trackSecondary = MaterialTheme.colorScheme.secondary

    val activeWaveBrush = remember(trackAccent, trackSecondary) {
        Brush.horizontalGradient(
            colors = listOf(
                trackAccent,
                trackSecondary,
                trackAccent.copy(alpha = 0.85f),
            ),
        )
    }

    val density = LocalDensity.current.density

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .pointerInput(durationMs, positionMs) {
                detectTapGestures { offset ->
                    if (size.width > 0) {
                        onSeek((offset.x / size.width).coerceIn(0f, 1f))
                    }
                }
            },
    ) {
        val centerY = size.height / 2f
        val activeWidth = size.width * progress.coerceIn(0f, 1f)

        // 1. Inactive baseline
        drawLine(
            color = trackAccent.copy(alpha = 0.22f),
            start = Offset(0f, centerY),
            end = Offset(size.width, centerY),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round,
        )

        // 2. Active harmonic sine wave
        drawPath(
            path = buildHarmonicSinePath(activeWidth, size.height, density),
            brush = activeWaveBrush,
            style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round),
        )

        // 3. Dynamic scrubber thumb
        drawCircle(
            color = trackAccent,
            radius = 7.dp.toPx(),
            center = Offset(activeWidth, centerY),
        )
    }
}

enum class SeekbarStyleVariant {
    ANDROID_17_WAVY_FLUID,
    VIVI_CAPSULE_STADIUM,
    ANALOG_MIXING_CONSOLE,
    METRO_SEGMENTED_DASH,
    CIDER_MINIMAL_NEEDLE,
    PRISM_NEON_RIBBON,
    NUVIA_LIVE_WAVEFORM
}

@Composable
fun WavySeekBar(
    progress: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    style: SeekbarStyleVariant = SeekbarStyleVariant.ANDROID_17_WAVY_FLUID,
    accentColors: List<Color> = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
) {
    var width by remember { mutableFloatStateOf(1f) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onSeek((offset.x / size.width).coerceIn(0f, 1f))
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        onSeek((change.position.x / size.width).coerceIn(0f, 1f))
                    }
                )
            }
    ) {
        width = size.width
        val h = size.height
        val centerY = h / 2f
        val currentX = (width * progress.coerceIn(0f, 1f))
        val activeBrush = Brush.horizontalGradient(accentColors, 0f, width)
        val trackBg = Color(0xFF2A2E3D)

        when (style) {
            SeekbarStyleVariant.ANDROID_17_WAVY_FLUID -> {
                // Background Track
                drawLine(trackBg, Offset(0f, centerY), Offset(width, centerY), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
                // Wavy Animated Sine Path
                val path = Path()
                path.moveTo(0f, centerY)
                var x = 0f
                val waveLength = 28f
                val amplitude = 5.dp.toPx()
                while (x <= currentX) {
                    val y = centerY + sin(x / waveLength * (2 * Math.PI.toFloat())) * amplitude
                    path.lineTo(x, y)
                    x += 2f
                }
                drawPath(path, activeBrush, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
                drawCircle(accentColors.last(), radius = 6.dp.toPx(), center = Offset(currentX, centerY))
            }

            SeekbarStyleVariant.VIVI_CAPSULE_STADIUM -> {
                // Broad Stadium Pill Track
                drawRoundRect(trackBg, Offset(0f, centerY - 6.dp.toPx()), Size(width, 12.dp.toPx()), CornerRadius(6.dp.toPx()))
                drawRoundRect(activeBrush, Offset(0f, centerY - 6.dp.toPx()), Size(currentX, 12.dp.toPx()), CornerRadius(6.dp.toPx()))
                drawCircle(Color.White, radius = 8.dp.toPx(), center = Offset(currentX, centerY))
            }

            SeekbarStyleVariant.ANALOG_MIXING_CONSOLE -> {
                // Segmented LED VU Meter style
                val segments = 36
                val segWidth = width / segments
                for (i in 0 until segments) {
                    val segX = i * segWidth
                    val isLit = segX <= currentX
                    val color = if (isLit) activeBrush else androidx.compose.ui.graphics.SolidColor(trackBg)
                    drawRoundRect(
                        brush = color,
                        topLeft = Offset(segX + 2f, centerY - 8.dp.toPx()),
                        size = Size(segWidth - 4f, 16.dp.toPx()),
                        cornerRadius = CornerRadius(2.dp.toPx())
                    )
                }
            }

            SeekbarStyleVariant.METRO_SEGMENTED_DASH -> {
                val totalDashes = 24
                val dashW = width / totalDashes
                for (i in 0 until totalDashes) {
                    val start = i * dashW
                    val lit = start <= currentX
                    drawLine(
                        color = if (lit) accentColors.first() else trackBg,
                        start = Offset(start + 3f, centerY),
                        end = Offset(start + dashW - 3f, centerY),
                        strokeWidth = 6.dp.toPx(),
                        cap = StrokeCap.Square
                    )
                }
            }

            SeekbarStyleVariant.CIDER_MINIMAL_NEEDLE -> {
                drawLine(trackBg, Offset(0f, centerY), Offset(width, centerY), strokeWidth = 2.dp.toPx())
                drawLine(activeBrush, Offset(0f, centerY), Offset(currentX, centerY), strokeWidth = 2.dp.toPx())
                drawLine(Color.White, Offset(currentX, centerY - 10.dp.toPx()), Offset(currentX, centerY + 10.dp.toPx()), strokeWidth = 2.dp.toPx())
            }

            SeekbarStyleVariant.PRISM_NEON_RIBBON -> {
                drawLine(trackBg, Offset(0f, centerY), Offset(width, centerY), strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
                drawLine(activeBrush, Offset(0f, centerY), Offset(currentX, centerY), strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
                drawCircle(Color.White, radius = 7.dp.toPx(), center = Offset(currentX, centerY))
            }

            SeekbarStyleVariant.NUVIA_LIVE_WAVEFORM -> {
                val bars = 40
                val barW = width / bars
                for (i in 0 until bars) {
                    val barX = i * barW
                    val isPast = barX <= currentX
                    val heightFactor = (sin(i * 0.5f) * 0.5f + 0.5f) * 14.dp.toPx() + 4.dp.toPx()
                    drawLine(
                        color = if (isPast) accentColors.first() else trackBg,
                        start = Offset(barX, centerY - heightFactor),
                        end = Offset(barX, centerY + heightFactor),
                        strokeWidth = barW * 0.6f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
