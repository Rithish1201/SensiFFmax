package com.sensiffmax.app.feature.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sensiffmax.app.core.components.*
import com.sensiffmax.app.core.data.model.*
import com.sensiffmax.app.core.ui.theme.CyberColors
import com.sensiffmax.app.feature.onboarding.OnboardingViewModel.OnboardingStep

/**
 * OnboardingScreen — Multi-step player configuration flow.
 *
 * Steps:
 * 1. Welcome — Game confirmation
 * 2. Play Style — Combat approach selection
 * 3. Finger Setup — Control layout selection
 * 4. Device Scan — Automatic hardware detection
 * 5. Confirmation — Review & save profile
 *
 * All business logic in OnboardingViewModel.
 */
@Composable
fun OnboardingScreen(
    onNavigateBack: () -> Unit,
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Navigate to Home when onboarding is complete
    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) {
            onFinishOnboarding()
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
            // Top bar with back button and step indicator
            OnboardingTopBar(
                uiState = uiState,
                onBack = {
                    if (uiState.canGoBack) {
                        viewModel.goToPreviousStep()
                    } else {
                        onNavigateBack()
                    }
                }
            )

            Spacer(Modifier.height(8.dp))

            // Step progress bar
            CyberProgressBar(
                progress = uiState.progress,
                modifier = Modifier.fillMaxWidth(),
                label = "STEP ${uiState.stepIndex + 1} OF ${uiState.totalSteps}",
                showPercentage = true
            )

            Spacer(Modifier.height(20.dp))

            // Step content with cross-fade animation
            AnimatedContent(
                targetState = uiState.currentStep,
                modifier = Modifier.weight(1f),
                transitionSpec = {
                    fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 4 } togetherWith
                            fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { -it / 4 }
                },
                label = "stepTransition"
            ) { step ->
                when (step) {
                    OnboardingStep.WELCOME -> WelcomeStep()
                    OnboardingStep.PLAY_STYLE -> PlayStyleStep(
                        selectedStyle = uiState.playStyle,
                        onSelect = viewModel::selectPlayStyle
                    )
                    OnboardingStep.FINGER_SETUP -> FingerSetupStep(
                        selectedSetup = uiState.fingerSetup,
                        onSelect = viewModel::selectFingerSetup
                    )
                    OnboardingStep.DEVICE_SCAN -> DeviceScanStep(
                        deviceProfile = uiState.deviceProfile
                    )
                    OnboardingStep.CONFIRMATION -> ConfirmationStep(
                        uiState = uiState
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Bottom navigation buttons
            OnboardingBottomBar(
                uiState = uiState,
                onNext = viewModel::goToNextStep,
                onComplete = viewModel::completeOnboarding
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun OnboardingTopBar(
    uiState: OnboardingViewModel.OnboardingUiState,
    onBack: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        CyberIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            onClick = onBack
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = getStepTitle(uiState.currentStep),
                style = MaterialTheme.typography.titleLarge,
                color = CyberColors.CyberCyan
            )
            Text(
                text = getStepSubtitle(uiState.currentStep),
                style = MaterialTheme.typography.labelSmall,
                color = CyberColors.TextSecondary
            )
        }
    }
}

@Composable
private fun OnboardingBottomBar(
    uiState: OnboardingViewModel.OnboardingUiState,
    onNext: () -> Unit,
    onComplete: () -> Unit
) {
    if (uiState.currentStep == OnboardingStep.CONFIRMATION) {
        CyberButton(
            text = "DEPLOY CONFIGURATION",
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth(),
            isLoading = uiState.isSaving
        )
    } else {
        CyberButton(
            text = "CONTINUE",
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// =====================================================
// STEP 1: WELCOME
// =====================================================

@Composable
private fun WelcomeStep() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            glowEnabled = true
        ) {
            CyberSectionHeader(
                title = "TARGET GAME",
                subtitle = "Sensitivity profiles optimized for"
            )
            Spacer(Modifier.height(12.dp))

            CyberChip(
                text = "FREE FIRE",
                selected = true,
                onClick = {}
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "SensiFFMax calibrates sensitivity settings specifically for Free Fire's control system.",
                style = MaterialTheme.typography.bodySmall,
                color = CyberColors.TextTertiary
            )
        }

        Spacer(Modifier.height(16.dp))

        CyberCard(modifier = Modifier.fillMaxWidth()) {
            CyberSectionHeader(
                title = "HOW IT WORKS",
                subtitle = "Calibration pipeline"
            )
            Spacer(Modifier.height(12.dp))

            OnboardingInfoRow(
                step = "01",
                title = "CALIBRATE",
                description = "Short reaction, precision & tracking tests"
            )
            Spacer(Modifier.height(10.dp))
            OnboardingInfoRow(
                step = "02",
                title = "ANALYZE",
                description = "Algorithm computes your baseline metrics"
            )
            Spacer(Modifier.height(10.dp))
            OnboardingInfoRow(
                step = "03",
                title = "RECOMMEND",
                description = "Personalized sensitivity values generated"
            )
            Spacer(Modifier.height(10.dp))
            OnboardingInfoRow(
                step = "04",
                title = "TUNE & TRAIN",
                description = "Fine-tune and practice in the training lab"
            )
        }
    }
}

@Composable
private fun OnboardingInfoRow(
    step: String,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = step,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = CyberColors.CyberCyan,
            modifier = Modifier.width(28.dp)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = CyberColors.TextPrimary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = CyberColors.TextSecondary
            )
        }
    }
}

// =====================================================
// STEP 2: PLAY STYLE
// =====================================================

@Composable
private fun PlayStyleStep(
    selectedStyle: PlayStyle,
    onSelect: (PlayStyle) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Your play style determines the sensitivity bias applied to your calibration results.",
            style = MaterialTheme.typography.bodyMedium,
            color = CyberColors.TextSecondary
        )

        Spacer(Modifier.height(16.dp))

        PlayStyle.entries.forEach { style ->
            val isSelected = style == selectedStyle
            CyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clickable { onSelect(style) },
                glowEnabled = isSelected,
                borderColor = if (isSelected) CyberColors.CyberCyan.copy(alpha = 0.5f) else CyberColors.Border
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelect(style) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = CyberColors.CyberCyan,
                            unselectedColor = CyberColors.TextTertiary
                        )
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = style.displayName.uppercase(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) CyberColors.CyberCyan else CyberColors.TextPrimary
                        )
                        Text(
                            text = style.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberColors.TextSecondary
                        )
                    }
                    if (isSelected) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = CyberColors.CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// =====================================================
// STEP 3: FINGER SETUP
// =====================================================

@Composable
private fun FingerSetupStep(
    selectedSetup: FingerSetup,
    onSelect: (FingerSetup) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Your finger count affects how sensitivity values scale across different control zones.",
            style = MaterialTheme.typography.bodyMedium,
            color = CyberColors.TextSecondary
        )

        Spacer(Modifier.height(16.dp))

        FingerSetup.entries.forEach { setup ->
            val isSelected = setup == selectedSetup
            CyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clickable { onSelect(setup) },
                glowEnabled = isSelected,
                borderColor = if (isSelected) CyberColors.CyberCyan.copy(alpha = 0.5f) else CyberColors.Border
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelect(setup) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = CyberColors.CyberCyan,
                            unselectedColor = CyberColors.TextTertiary
                        )
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = setup.displayName.uppercase(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) CyberColors.CyberCyan else CyberColors.TextPrimary
                        )
                        Text(
                            text = setup.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberColors.TextSecondary
                        )
                    }
                    if (isSelected) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = CyberColors.CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// =====================================================
// STEP 4: DEVICE SCAN
// =====================================================

@Composable
private fun DeviceScanStep(
    deviceProfile: DeviceProfile
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Your device hardware has been scanned automatically. No permissions required.",
            style = MaterialTheme.typography.bodyMedium,
            color = CyberColors.TextSecondary
        )

        Spacer(Modifier.height(16.dp))

        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            glowEnabled = true
        ) {
            CyberSectionHeader(
                title = "HARDWARE DETECTED",
                subtitle = "Display & performance metrics"
            )

            Spacer(Modifier.height(16.dp))

            DeviceStatRow(
                label = "RESOLUTION",
                value = "${deviceProfile.screenWidthPx} × ${deviceProfile.screenHeightPx} px"
            )
            Spacer(Modifier.height(12.dp))
            DeviceStatRow(
                label = "DENSITY",
                value = "${deviceProfile.screenDensityDpi} DPI"
            )
            Spacer(Modifier.height(12.dp))
            DeviceStatRow(
                label = "REFRESH RATE",
                value = "${deviceProfile.refreshRate.toInt()} Hz"
            )
        }

        Spacer(Modifier.height(16.dp))

        CyberCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = CyberColors.CyanDim,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Screen dimensions and refresh rate are used to optimize sensitivity scaling. Higher refresh rates allow finer sensitivity adjustments.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberColors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun DeviceStatRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = CyberColors.TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = CyberColors.CyberCyan
        )
    }
}

// =====================================================
// STEP 5: CONFIRMATION
// =====================================================

@Composable
private fun ConfirmationStep(
    uiState: OnboardingViewModel.OnboardingUiState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Review your configuration before deploying. You can change these later in Settings.",
            style = MaterialTheme.typography.bodyMedium,
            color = CyberColors.TextSecondary
        )

        Spacer(Modifier.height(16.dp))

        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            glowEnabled = true
        ) {
            CyberSectionHeader(
                title = "PLAYER PROFILE",
                subtitle = "Configuration summary"
            )

            Spacer(Modifier.height(16.dp))

            ConfirmationRow(label = "GAME", value = uiState.game.displayName)
            Spacer(Modifier.height(10.dp))
            ConfirmationRow(label = "PLAY STYLE", value = uiState.playStyle.displayName)
            Spacer(Modifier.height(10.dp))
            ConfirmationRow(label = "FINGER SETUP", value = uiState.fingerSetup.displayName)
            Spacer(Modifier.height(10.dp))
            ConfirmationRow(
                label = "DISPLAY",
                value = "${uiState.deviceProfile.screenWidthPx}×${uiState.deviceProfile.screenHeightPx}"
            )
            Spacer(Modifier.height(10.dp))
            ConfirmationRow(
                label = "REFRESH RATE",
                value = "${uiState.deviceProfile.refreshRate.toInt()} Hz"
            )
        }

        Spacer(Modifier.height(16.dp))

        CyberCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.RocketLaunch,
                    contentDescription = null,
                    tint = CyberColors.Success,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Once deployed, head to the Calibration tab to run your first sensitivity test. Results will generate personalized sensitivity values.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberColors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun ConfirmationRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = CyberColors.TextSecondary
        )
        Text(
            text = value.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = CyberColors.TextPrimary
        )
    }
}

// =====================================================
// HELPERS
// =====================================================

private fun getStepTitle(step: OnboardingStep): String = when (step) {
    OnboardingStep.WELCOME -> "WELCOME"
    OnboardingStep.PLAY_STYLE -> "PLAY STYLE"
    OnboardingStep.FINGER_SETUP -> "FINGER SETUP"
    OnboardingStep.DEVICE_SCAN -> "DEVICE SCAN"
    OnboardingStep.CONFIRMATION -> "CONFIRM"
}

private fun getStepSubtitle(step: OnboardingStep): String = when (step) {
    OnboardingStep.WELCOME -> "MISSION BRIEFING"
    OnboardingStep.PLAY_STYLE -> "COMBAT APPROACH"
    OnboardingStep.FINGER_SETUP -> "CONTROL LAYOUT"
    OnboardingStep.DEVICE_SCAN -> "HARDWARE DETECTION"
    OnboardingStep.CONFIRMATION -> "DEPLOY PROFILE"
}
