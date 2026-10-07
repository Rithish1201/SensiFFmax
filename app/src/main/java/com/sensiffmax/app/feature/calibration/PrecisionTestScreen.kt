package com.sensiffmax.app.feature.calibration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sensiffmax.app.core.components.*
import com.sensiffmax.app.core.ui.theme.CyberColors
import com.sensiffmax.app.feature.calibration.precision.PrecisionTestViewModel
import com.sensiffmax.app.feature.calibration.precision.PrecisionTestViewModel.TestPhase
import com.sensiffmax.app.feature.calibration.precision.engine.PrecisionScoringEngine
import kotlinx.coroutines.delay

/**
 * PrecisionTestScreen — Real precision calibration test with geometric hit detection.
 *
 * Requirements:
 * - 10 rounds
 * - Random target position per round
 * - Actual geometric hit detection: distance(tap, center) <= radius
 * - Tap outside target = MISS
 * - Live HIT/MISS/ACCURACY telemetry
 * - Cyberpunk HUD
 */
@Composable
fun PrecisionTestScreen(
    onNavigateBack: () -> Unit,
    onTestComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PrecisionTestViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Auto-navigate to results when test completes
    LaunchedEffect(uiState.phase) {
        if (uiState.phase == TestPhase.FINISHED) {
            delay(400L) // Brief pause before navigating
            onTestComplete()
        }
    }

    // Auto-advance to next round after feedback
    LaunchedEffect(uiState.phase) {
        if (uiState.phase == TestPhase.ROUND_FEEDBACK) {
            delay(600L)
            viewModel.advanceToNextRound()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = CyberColors.Background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CyberIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    onClick = onNavigateBack
                )
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "02 PRECISION TEST",
                        style = MaterialTheme.typography.titleLarge,
                        color = CyberColors.CyberCyan
                    )
                    Text(
                        text = "${uiState.totalRounds} ROUNDS TARGET ACQUISITION",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberColors.TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Progress Bar
            val progress = if (uiState.phase == TestPhase.READY) {
                0f
            } else {
                (uiState.currentRound - 1).toFloat() / uiState.totalRounds
            }
            CyberProgressBar(
                progress = if (uiState.phase == TestPhase.FINISHED) 1f else progress,
                label = "ROUND ${uiState.currentRound} OF ${uiState.totalRounds}",
                showPercentage = false
            )

            Spacer(Modifier.height(12.dp))

            // Live Telemetry Strip
            if (uiState.phase != TestPhase.READY) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberColors.Surface)
                        .border(
                            BorderStroke(1.dp, CyberColors.Border),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TelemetryStat(
                        label = "HITS",
                        value = "${uiState.hits}",
                        color = CyberColors.Success
                    )
                    TelemetryStat(
                        label = "MISSES",
                        value = "${uiState.misses}",
                        color = if (uiState.misses > 0) CyberColors.NeonRed else CyberColors.TextSecondary
                    )
                    TelemetryStat(
                        label = "ACCURACY",
                        value = "${uiState.accuracy.toInt()}%",
                        color = CyberColors.CyberCyan
                    )
                }

                Spacer(Modifier.height(12.dp))
            }

            // Status Banner
            PrecisionStatusBanner(uiState = uiState)

            Spacer(Modifier.height(12.dp))

            // Interactive Play Area Canvas
            val density = LocalDensity.current
            val targetRadiusDp = PrecisionScoringEngine.TARGET_RADIUS_DP
            val targetRadiusPx = with(density) { targetRadiusDp.dp.toPx() }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberColors.Surface)
                    .border(
                        BorderStroke(
                            1.dp,
                            when (uiState.phase) {
                                TestPhase.TARGET_ACTIVE -> CyberColors.CyberCyan.copy(alpha = 0.4f)
                                TestPhase.ROUND_FEEDBACK -> if (uiState.lastWasHit) CyberColors.Success else CyberColors.NeonRed
                                else -> CyberColors.Border
                            }
                        ),
                        RoundedCornerShape(12.dp)
                    )
                    .drawBehind {
                        // Subtle cyber grid
                        val step = 60.dp.toPx()
                        var x = 0f
                        while (x < size.width) {
                            drawLine(
                                color = CyberColors.Border.copy(alpha = 0.20f),
                                start = Offset(x, 0f),
                                end = Offset(x, size.height),
                                strokeWidth = 1f
                            )
                            x += step
                        }
                        var y = 0f
                        while (y < size.height) {
                            drawLine(
                                color = CyberColors.Border.copy(alpha = 0.20f),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1f
                            )
                            y += step
                        }
                    }
                    .then(
                        if (uiState.phase == TestPhase.TARGET_ACTIVE) {
                            Modifier.pointerInput(uiState.currentRound) {
                                detectTapGestures { offset ->
                                    viewModel.onAreaTapped(
                                        tapXPx = offset.x,
                                        tapYPx = offset.y,
                                        areaWidthPx = size.width.toFloat(),
                                        areaHeightPx = size.height.toFloat(),
                                        targetRadiusPx = targetRadiusPx
                                    )
                                }
                            }
                        } else Modifier
                    )
            ) {
                val areaWidthDp = maxWidth
                val areaHeightDp = maxHeight
                val targetDiameterDp = (targetRadiusDp * 2).dp

                when (uiState.phase) {
                    TestPhase.READY -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "PRECISION PROTOCOL",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CyberColors.CyberCyan
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Targets appear at random positions.\n\nTap INSIDE the circle = HIT\nTap OUTSIDE the circle = MISS\n\nAim for center for best score.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CyberColors.TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(24.dp))
                            CyberButton(
                                text = "INITIATE PROTOCOL",
                                onClick = { viewModel.startTest() }
                            )
                        }
                    }

                    TestPhase.TARGET_ACTIVE -> {
                        // Position target using ratios
                        val targetOffsetX = (areaWidthDp - targetDiameterDp) * uiState.targetXRatio
                        val targetOffsetY = (areaHeightDp - targetDiameterDp) * uiState.targetYRatio

                        Box(
                            modifier = Modifier
                                .offset(x = targetOffsetX, y = targetOffsetY)
                                .size(targetDiameterDp)
                        ) {
                            CyberPrecisionTarget()
                        }
                    }

                    TestPhase.ROUND_FEEDBACK -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    if (uiState.lastWasHit) CyberColors.Success.copy(alpha = 0.08f)
                                    else CyberColors.NeonRed.copy(alpha = 0.08f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (uiState.lastWasHit) Icons.Default.Done else Icons.Default.Close,
                                    contentDescription = null,
                                    tint = if (uiState.lastWasHit) CyberColors.Success else CyberColors.NeonRed,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = if (uiState.lastWasHit) "HIT!" else "MISS",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.lastWasHit) CyberColors.Success else CyberColors.NeonRed
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Error: %.1f px".format(uiState.lastDistancePx),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CyberColors.TextSecondary
                                )
                            }
                        }
                    }

                    TestPhase.FINISHED -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ANALYZING PRECISION...",
                                style = MaterialTheme.typography.titleMedium,
                                color = CyberColors.CyberCyan
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Footer abort button
            if (uiState.phase == TestPhase.TARGET_ACTIVE || uiState.phase == TestPhase.ROUND_FEEDBACK) {
                CyberOutlinedButton(
                    text = "ABORT & RESTART",
                    onClick = { viewModel.restartTest() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Small inline telemetry stat used in the live strip.
 */
@Composable
private fun TelemetryStat(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = CyberColors.TextSecondary
        )
    }
}

/**
 * HUD banner showing current precision test phase status.
 */
private data class PrecisionBannerStyle(
    val backgroundColor: Color,
    val borderColor: Color,
    val textColor: Color,
    val message: String
)

@Composable
private fun PrecisionStatusBanner(uiState: PrecisionTestViewModel.UiState) {
    val style = when (uiState.phase) {
        TestPhase.READY -> PrecisionBannerStyle(
            backgroundColor = CyberColors.SurfaceVariant,
            borderColor = CyberColors.Border,
            textColor = CyberColors.TextSecondary,
            message = "READY FOR PRECISION PROTOCOL"
        )
        TestPhase.TARGET_ACTIVE -> PrecisionBannerStyle(
            backgroundColor = CyberColors.CyberCyan.copy(alpha = 0.10f),
            borderColor = CyberColors.CyberCyan.copy(alpha = 0.40f),
            textColor = CyberColors.CyberCyan,
            message = "TARGET ACTIVE · TAP INSIDE THE CIRCLE"
        )
        TestPhase.ROUND_FEEDBACK -> PrecisionBannerStyle(
            backgroundColor = if (uiState.lastWasHit) CyberColors.Success.copy(alpha = 0.12f)
                             else CyberColors.NeonRed.copy(alpha = 0.12f),
            borderColor = if (uiState.lastWasHit) CyberColors.Success else CyberColors.NeonRed,
            textColor = if (uiState.lastWasHit) CyberColors.Success else CyberColors.NeonRed,
            message = if (uiState.lastWasHit) "HIT · ${uiState.lastDistancePx.toInt()}px FROM CENTER"
                     else "MISS · ${uiState.lastDistancePx.toInt()}px AWAY"
        )
        TestPhase.FINISHED -> PrecisionBannerStyle(
            backgroundColor = CyberColors.CyberCyan.copy(alpha = 0.20f),
            borderColor = CyberColors.CyberCyan,
            textColor = CyberColors.CyberCyan,
            message = "PRECISION PROTOCOL COMPLETE"
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(style.backgroundColor)
            .border(BorderStroke(1.dp, style.borderColor), RoundedCornerShape(8.dp))
            .padding(vertical = 10.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = style.message,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = style.textColor
        )
    }
}

/**
 * Animated Cyber Target for Precision Testing — concentric ring design.
 */
@Composable
private fun CyberPrecisionTarget(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "precisionPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "precisionScale"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing ring
        Box(
            modifier = Modifier
                .fillMaxSize(pulseScale)
                .clip(CircleShape)
                .border(BorderStroke(2.dp, CyberColors.CyberCyan.copy(alpha = 0.6f)), CircleShape)
        )

        // Middle ring
        Box(
            modifier = Modifier
                .fillMaxSize(0.65f)
                .clip(CircleShape)
                .border(BorderStroke(1.5.dp, CyberColors.CyberCyan.copy(alpha = 0.8f)), CircleShape)
        )

        // Inner filled center
        Box(
            modifier = Modifier
                .fillMaxSize(0.35f)
                .clip(CircleShape)
                .background(CyberColors.CyberCyan)
        )
    }
}
