package com.sensiffmax.app.feature.calibration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sensiffmax.app.core.components.*
import com.sensiffmax.app.core.ui.theme.CyberColors
import com.sensiffmax.app.feature.calibration.reaction.ReactionTestViewModel
import com.sensiffmax.app.feature.calibration.reaction.ReactionTestViewModel.TestPhase

/**
 * ReactionTestScreen — Real reaction calibration test.
 *
 * Requirements:
 * - 5 rounds
 * - Random 1–4 second delay
 * - Variable target position
 * - High-precision reaction timing
 * - Early tap detection and penalty
 * - Full cyberpunk HUD
 */
@Composable
fun ReactionTestScreen(
    onNavigateBack: () -> Unit,
    onTestComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReactionTestViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Auto-navigate to results screen when test completes
    LaunchedEffect(uiState.phase) {
        if (uiState.phase == TestPhase.FINISHED) {
            onTestComplete()
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
            // Header with back button and title
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
                        text = "01 REACTION TEST",
                        style = MaterialTheme.typography.titleLarge,
                        color = CyberColors.CyberCyan
                    )
                    Text(
                        text = "5 ROUNDS CALIBRATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberColors.TextSecondary
                    )
                }

                if (uiState.phase != TestPhase.READY) {
                    CyberBadge(
                        text = "FAULTS: ${uiState.earlyTapsCount}",
                        color = if (uiState.earlyTapsCount > 0) CyberColors.NeonRed else CyberColors.TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Progress Bar
            val progress = (uiState.currentRound - 1).toFloat() / uiState.totalRounds
            CyberProgressBar(
                progress = if (uiState.phase == TestPhase.FINISHED) 1f else progress,
                label = "ROUND ${uiState.currentRound} OF ${uiState.totalRounds}",
                showPercentage = false
            )

            Spacer(Modifier.height(16.dp))

            // Status Banner HUD
            ReactionStatusBanner(uiState = uiState)

            Spacer(Modifier.height(16.dp))

            // Interactive Test Area Canvas
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
                                TestPhase.EARLY_TAP_PENALTY -> CyberColors.NeonRed
                                TestPhase.TARGET_ACTIVE -> CyberColors.CyberCyan
                                else -> CyberColors.Border
                            }
                        ),
                        RoundedCornerShape(12.dp)
                    )
                    .drawBehind {
                        // Cyber grid crosshair accents in background
                        val step = 60.dp.toPx()
                        var x = 0f
                        while (x < size.width) {
                            drawLine(
                                color = CyberColors.Border.copy(alpha = 0.25f),
                                start = Offset(x, 0f),
                                end = Offset(x, size.height),
                                strokeWidth = 1f
                            )
                            x += step
                        }
                        var y = 0f
                        while (y < size.height) {
                            drawLine(
                                color = CyberColors.Border.copy(alpha = 0.25f),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1f
                            )
                            y += step
                        }
                    }
                    .then(
                        // Intercept early taps across entire area when waiting
                        if (uiState.phase == TestPhase.WAITING_FOR_TARGET) {
                            Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                viewModel.onEarlyTap()
                            }
                        } else Modifier
                    )
            ) {
                val maxWidthPx = maxWidth
                val maxHeightPx = maxHeight
                val targetSize = 84.dp

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
                                text = "CALIBRATION PROTOCOL",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CyberColors.CyberCyan
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Targets appear with randomized 1–4s delay at variable positions.\n\nTap immediately when the reticle appears.\nAvoid early taps (150ms fault penalty).",
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

                    TestPhase.WAITING_FOR_TARGET -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                            val pulseAlpha by infiniteTransition.animateFloat(
                                initialValue = 0.3f,
                                targetValue = 0.8f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(800, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulseAlpha"
                            )

                            Text(
                                text = "STANDBY...",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = CyberColors.NeonAmber.copy(alpha = pulseAlpha)
                            )
                        }
                    }

                    TestPhase.EARLY_TAP_PENALTY -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(CyberColors.NeonRed.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Fault",
                                    tint = CyberColors.NeonRed,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "EARLY TAP FAULT!",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberColors.NeonRed
                                )
                                Text(
                                    text = "+150ms PENALTY",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CyberColors.NeonRed
                                )
                            }
                        }
                    }

                    TestPhase.TARGET_ACTIVE -> {
                        // Position target at randomized coordinate ratio
                        val offsetX = (maxWidthPx - targetSize) * uiState.targetXRatio
                        val offsetY = (maxHeightPx - targetSize) * uiState.targetYRatio

                        Box(
                            modifier = Modifier
                                .offset(x = offsetX, y = offsetY)
                                .size(targetSize)
                        ) {
                            CyberReactionTarget(
                                onClick = { viewModel.onTargetTapped() }
                            )
                        }
                    }

                    TestPhase.ROUND_FEEDBACK -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${uiState.lastReactionMs} ms",
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberColors.CyberCyan
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = when {
                                        uiState.lastReactionMs < 200 -> "PRO REFLEXES! ⚡"
                                        uiState.lastReactionMs < 250 -> "GREAT SPEED! 🎯"
                                        uiState.lastReactionMs < 300 -> "SOLID TIMING 👍"
                                        else -> "REGISTERED"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
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
                                text = "CALCULATING METRICS...",
                                style = MaterialTheme.typography.titleMedium,
                                color = CyberColors.CyberCyan
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Footer / Abort button
            if (uiState.phase != TestPhase.READY && uiState.phase != TestPhase.FINISHED) {
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
 * HUD banner displaying current test phase status.
 */
private data class BannerStyle(
    val backgroundColor: Color,
    val borderColor: Color,
    val textColor: Color,
    val message: String
)

@Composable
private fun ReactionStatusBanner(uiState: ReactionTestViewModel.UiState) {
    val style = when (uiState.phase) {
        TestPhase.READY -> BannerStyle(
            backgroundColor = CyberColors.SurfaceVariant,
            borderColor = CyberColors.Border,
            textColor = CyberColors.TextSecondary,
            message = "READY FOR PROTOCOL"
        )
        TestPhase.WAITING_FOR_TARGET -> BannerStyle(
            backgroundColor = CyberColors.NeonAmber.copy(alpha = 0.10f),
            borderColor = CyberColors.NeonAmber.copy(alpha = 0.40f),
            textColor = CyberColors.NeonAmber,
            message = "HOLD POSITION · WATCH SCREEN"
        )
        TestPhase.TARGET_ACTIVE -> BannerStyle(
            backgroundColor = CyberColors.CyberCyan.copy(alpha = 0.15f),
            borderColor = CyberColors.CyberCyan,
            textColor = CyberColors.CyberCyan,
            message = "TARGET ACQUIRED · TAP NOW!"
        )
        TestPhase.EARLY_TAP_PENALTY -> BannerStyle(
            backgroundColor = CyberColors.NeonRed.copy(alpha = 0.15f),
            borderColor = CyberColors.NeonRed,
            textColor = CyberColors.NeonRed,
            message = "EARLY TAP DETECTED (+150ms)"
        )
        TestPhase.ROUND_FEEDBACK -> BannerStyle(
            backgroundColor = CyberColors.CyberCyan.copy(alpha = 0.12f),
            borderColor = CyberColors.CyberCyan.copy(alpha = 0.50f),
            textColor = CyberColors.CyberCyan,
            message = "ROUND ${uiState.completedRounds.size} RECORDED: ${uiState.lastReactionMs}ms"
        )
        TestPhase.FINISHED -> BannerStyle(
            backgroundColor = CyberColors.CyberCyan.copy(alpha = 0.20f),
            borderColor = CyberColors.CyberCyan,
            textColor = CyberColors.CyberCyan,
            message = "PROTOCOL COMPLETE"
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
 * Animated Cyber Target for Reaction Testing.
 */
@Composable
private fun CyberReactionTarget(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "targetPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "targetScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing ring
        Box(
            modifier = Modifier
                .fillMaxSize(pulseScale)
                .clip(CircleShape)
                .border(BorderStroke(2.dp, CyberColors.CyberCyan), CircleShape)
        )

        // Middle ring
        Box(
            modifier = Modifier
                .fillMaxSize(0.65f)
                .clip(CircleShape)
                .border(BorderStroke(1.5.dp, CyberColors.CyberCyan.copy(alpha = 0.7f)), CircleShape)
        )

        // Center solid core
        Box(
            modifier = Modifier
                .fillMaxSize(0.35f)
                .clip(CircleShape)
                .background(CyberColors.CyberCyan)
        )
    }
}
