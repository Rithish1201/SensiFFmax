package com.sensiffmax.app.feature.recommendation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sensiffmax.app.core.components.*
import com.sensiffmax.app.core.ui.theme.CyberColors
import com.sensiffmax.app.feature.calibration.CalibrationSessionStateHolder

@Composable
fun TuneSensitivityScreen(
    onNavigateBack: () -> Unit,
    onSaveTunedSensitivity: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Read the current session values to initialize sliders
    val sessionResult by CalibrationSessionStateHolder.sessionResult.collectAsState()

    // Derive initial values from session (using same engine logic inline for simplicity)
    // These are mutable local state for the sliders
    var general by remember {
        mutableFloatStateOf(
            computeQuickSensitivity(sessionResult.overallScore, 1.0f)
        )
    }
    var redDot by remember {
        mutableFloatStateOf(
            computeQuickSensitivity(sessionResult.overallScore, 0.92f)
        )
    }
    var scope2x by remember {
        mutableFloatStateOf(
            computeQuickSensitivity(sessionResult.overallScore, 0.80f)
        )
    }
    var scope4x by remember {
        mutableFloatStateOf(
            computeQuickSensitivity(sessionResult.overallScore, 0.65f)
        )
    }
    var sniper by remember {
        mutableFloatStateOf(
            computeQuickSensitivity(sessionResult.overallScore, 0.50f)
        )
    }
    var freeLook by remember {
        mutableFloatStateOf(
            computeQuickSensitivity(sessionResult.overallScore, 0.72f)
        )
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
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                CyberIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    onClick = onNavigateBack
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = "TUNE SENSITIVITY",
                        style = MaterialTheme.typography.titleLarge,
                        color = CyberColors.CyberCyan
                    )
                    Text(
                        text = "MANUAL VALUE ADJUSTMENT",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberColors.TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            CyberCard(modifier = Modifier.fillMaxWidth()) {
                CyberSlider(
                    value = general,
                    onValueChange = { general = it },
                    label = "General Sensitivity",
                    valueRange = 10f..200f
                )
                Spacer(Modifier.height(12.dp))
                CyberSlider(
                    value = redDot,
                    onValueChange = { redDot = it },
                    label = "Red Dot",
                    valueRange = 10f..200f
                )
                Spacer(Modifier.height(12.dp))
                CyberSlider(
                    value = scope2x,
                    onValueChange = { scope2x = it },
                    label = "2x Scope",
                    valueRange = 10f..200f
                )
                Spacer(Modifier.height(12.dp))
                CyberSlider(
                    value = scope4x,
                    onValueChange = { scope4x = it },
                    label = "4x Scope",
                    valueRange = 10f..200f
                )
                Spacer(Modifier.height(12.dp))
                CyberSlider(
                    value = sniper,
                    onValueChange = { sniper = it },
                    label = "Sniper Scope",
                    valueRange = 10f..200f
                )
                Spacer(Modifier.height(12.dp))
                CyberSlider(
                    value = freeLook,
                    onValueChange = { freeLook = it },
                    label = "Free Look",
                    valueRange = 10f..200f
                )
            }

            Spacer(Modifier.height(24.dp))

            CyberButton(
                text = "APPLY & SAVE PROFILE",
                onClick = onSaveTunedSensitivity,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

/**
 * Quick sensitivity calculation for tuning screen initialization.
 * Maps overall calibration score to a sensitivity value with scope attenuation.
 */
private fun computeQuickSensitivity(overallScore: Int, scopeFactor: Float): Float {
    val base = 50f + (overallScore.coerceIn(0, 100) / 100f) * 100f
    return (base * scopeFactor).coerceIn(10f, 200f)
}
