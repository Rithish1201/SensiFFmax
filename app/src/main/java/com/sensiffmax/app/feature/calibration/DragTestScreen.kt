package com.sensiffmax.app.feature.calibration

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sensiffmax.app.core.components.*
import com.sensiffmax.app.core.ui.theme.CyberColors
import com.sensiffmax.app.feature.calibration.drag.DragTestViewModel
import com.sensiffmax.app.feature.calibration.drag.DragTestViewModel.TestPhase
import com.sensiffmax.app.feature.calibration.drag.engine.DragScoringEngine
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * DragTestScreen — Real continuous path tracking calibration test.
 *
 * Requirements:
 * - 20s continuous tracking along a smooth parametric trajectory
 * - Real-time comparison between finger position and moving target center
 * - Live HUD showing Time, Accuracy, and Error offset
 * - Vector tracking beam between finger and target
 * - Abort and restart controls
 */
@Composable
fun DragTestScreen(
    onNavigateBack: () -> Unit,
    onTestComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DragTestViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val density = LocalDensity.current

    // Auto-navigate to results when tracking completes
    LaunchedEffect(uiState.phase) {
        if (uiState.phase == TestPhase.FINISHED) {
            delay(400L)
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
                        text = "03 DRAG TEST",
                        style = MaterialTheme.typography.titleLarge,
                        color = CyberColors.CyberCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "PATH TRACKING PROTOCOL",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberColors.TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Progress bar
            CyberProgressBar(
                progress = uiState.progress,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // Minimal Live Telemetry Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberColors.Surface, RoundedCornerShape(8.dp))
                    .border(BorderStroke(1.dp, CyberColors.Border), RoundedCornerShape(8.dp))
                    .padding(vertical = 10.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format(Locale.US, "%.1fs", uiState.remainingSeconds),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CyberColors.CyberCyan
                    )
                    Text(
                        text = "TIME",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberColors.TextSecondary
                    )
                }

                // Accuracy
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${uiState.liveAccuracy.toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.liveAccuracy >= 75f) CyberColors.Success else CyberColors.Orange
                    )
                    Text(
                        text = "ACCURACY",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberColors.TextSecondary
                    )
                }

                // Error
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val errorText = uiState.currentErrorPx?.let {
                        String.format(Locale.US, "%.0f px", it)
                    } ?: "--"
                    Text(
                        text = errorText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isOnTarget) CyberColors.Success else CyberColors.AlertRed
                    )
                    Text(
                        text = "ERROR",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberColors.TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Status Banner
            val statusColor: Color = when {
                uiState.phase != TestPhase.TRACKING -> CyberColors.TextSecondary
                uiState.isOnTarget -> CyberColors.Success
                uiState.isTouching -> CyberColors.Orange
                else -> CyberColors.AlertRed
            }
            val statusText = when {
                uiState.phase != TestPhase.TRACKING -> "STANDBY · INITIATE WHEN READY"
                uiState.isOnTarget -> "TARGET LOCKED · MAINTAIN TRACK"
                uiState.isTouching -> "COURSE CORRECTION · ALIGN TO TARGET"
                else -> "SIGNAL LOST · TOUCH AND DRAG TO TRACK"
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                    .border(BorderStroke(1.dp, statusColor.copy(alpha = 0.4f)), RoundedCornerShape(6.dp))
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }

            Spacer(Modifier.height(12.dp))

            // Main Interactive Tracking Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberColors.Elevated)
                    .border(BorderStroke(1.dp, CyberColors.Border), RoundedCornerShape(12.dp))
                    .onSizeChanged { size ->
                        val radiusPx = with(density) { 38.dp.toPx() }
                        viewModel.setBounds(size.width.toFloat(), size.height.toFloat(), radiusPx)
                    }
                    .pointerInput(uiState.phase) {
                        if (uiState.phase == TestPhase.TRACKING) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                viewModel.onTouchDown(down.position.x, down.position.y)
                                do {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull()
                                    if (change != null && change.pressed) {
                                        viewModel.onTouchMove(change.position.x, change.position.y)
                                    }
                                } while (event.changes.any { it.pressed })
                                viewModel.onTouchUp()
                            }
                        }
                    }
            ) {
                // Tracking Canvas: Grid, Trajectory, Laser Line, and Target Reticle
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    if (w <= 0 || h <= 0) return@Canvas

                    // Cyber tactical grid
                    val gridStep = 48.dp.toPx()
                    var x = 0f
                    while (x < w) {
                        drawLine(
                            color = CyberColors.Border.copy(alpha = 0.35f),
                            start = Offset(x, 0f),
                            end = Offset(x, h),
                            strokeWidth = 1f
                        )
                        x += gridStep
                    }
                    var y = 0f
                    while (y < h) {
                        drawLine(
                            color = CyberColors.Border.copy(alpha = 0.35f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f
                        )
                        y += gridStep
                    }

                    // Trajectory preview curve
                    val path = Path()
                    val steps = 120
                    for (i in 0..steps) {
                        val p = i.toFloat() / steps.toFloat()
                        val pt = DragScoringEngine.calculateTrajectoryPoint(p, w, h)
                        if (i == 0) path.moveTo(pt.first, pt.second)
                        else path.lineTo(pt.first, pt.second)
                    }
                    drawPath(
                        path = path,
                        color = CyberColors.CyberCyan.copy(alpha = 0.18f),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f))
                        )
                    )

                    // Laser tracking beam from finger to target center
                    val fX = uiState.fingerX
                    val fY = uiState.fingerY
                    if (uiState.isTouching && fX != null && fY != null) {
                        val beamColor: Color = if (uiState.isOnTarget) {
                            CyberColors.Success.copy(alpha = 0.7f)
                        } else {
                            CyberColors.AlertRed.copy(alpha = 0.6f)
                        }

                        drawLine(
                            color = beamColor,
                            start = Offset(fX, fY),
                            end = Offset(uiState.targetX, uiState.targetY),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Finger touch indicator ring
                        drawCircle(
                            color = beamColor,
                            radius = 16.dp.toPx(),
                            center = Offset(fX, fY),
                            style = Stroke(width = 2.dp.toPx())
                        )
                        drawCircle(
                            color = beamColor.copy(alpha = 0.4f),
                            radius = 5.dp.toPx(),
                            center = Offset(fX, fY)
                        )
                    }

                    // Target outer tolerance ring
                    val targetCenter = Offset(uiState.targetX, uiState.targetY)
                    val ringColor: Color = if (uiState.isOnTarget) CyberColors.Success else CyberColors.CyberCyan

                    // Glow background
                    drawCircle(
                        color = ringColor.copy(alpha = 0.15f),
                        radius = uiState.targetRadiusPx,
                        center = targetCenter
                    )
                    // Outer ring
                    drawCircle(
                        color = ringColor,
                        radius = uiState.targetRadiusPx,
                        center = targetCenter,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    // Concentric inner ring
                    drawCircle(
                        color = ringColor.copy(alpha = 0.6f),
                        radius = uiState.targetRadiusPx * 0.55f,
                        center = targetCenter,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    // Bullseye center dot
                    drawCircle(
                        color = ringColor,
                        radius = 6.dp.toPx(),
                        center = targetCenter
                    )

                    // Tactical crosshairs
                    val chLen = 10.dp.toPx()
                    drawLine(
                        color = ringColor.copy(alpha = 0.8f),
                        start = Offset(targetCenter.x - uiState.targetRadiusPx - chLen, targetCenter.y),
                        end = Offset(targetCenter.x - uiState.targetRadiusPx, targetCenter.y),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = ringColor.copy(alpha = 0.8f),
                        start = Offset(targetCenter.x + uiState.targetRadiusPx, targetCenter.y),
                        end = Offset(targetCenter.x + uiState.targetRadiusPx + chLen, targetCenter.y),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = ringColor.copy(alpha = 0.8f),
                        start = Offset(targetCenter.x, targetCenter.y - uiState.targetRadiusPx - chLen),
                        end = Offset(targetCenter.x, targetCenter.y - uiState.targetRadiusPx),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = ringColor.copy(alpha = 0.8f),
                        start = Offset(targetCenter.x, targetCenter.y + uiState.targetRadiusPx),
                        end = Offset(targetCenter.x, targetCenter.y + uiState.targetRadiusPx + chLen),
                        strokeWidth = 2f
                    )
                }

                // Ready overlay modal
                if (uiState.phase == TestPhase.READY) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .align(Alignment.Center)
                            .background(CyberColors.Surface.copy(alpha = 0.96f), RoundedCornerShape(12.dp))
                            .border(BorderStroke(1.dp, CyberColors.CyberCyan.copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "TRACKING PROTOCOL",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CyberColors.CyberCyan
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "A moving target traverses a tactical path across the grid for 20 seconds.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CyberColors.TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Press and HOLD your finger inside the circle.\nContinuously follow it as it moves.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = CyberColors.TextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Simple taps will NOT generate a passing score.",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberColors.Orange,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(20.dp))
                            CyberButton(
                                text = "INITIATE PROTOCOL",
                                onClick = { viewModel.startTest() },
                                modifier = Modifier.fillMaxWidth(0.85f)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Abort & Restart Button
            CyberOutlinedButton(
                text = "ABORT & RESTART",
                onClick = { viewModel.abortAndRestart() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
