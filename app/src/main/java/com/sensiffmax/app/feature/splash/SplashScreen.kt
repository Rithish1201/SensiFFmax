package com.sensiffmax.app.feature.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sensiffmax.app.core.components.CyberProgressBar
import com.sensiffmax.app.core.ui.theme.CyberColors
import kotlinx.coroutines.delay

/**
 * SplashScreen — Entry point of SensiFFMax.
 *
 * Shows animated branding, then auto-navigates based on onboarding status:
 * - First launch: → Onboarding
 * - Returning user: → Home
 */
@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    splashViewModel: SplashViewModel = viewModel()
) {
    val destination by splashViewModel.destination.collectAsState()

    // Animate splash for a minimum display time, then navigate
    var splashReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1800L) // Minimum splash display time for branding
        splashReady = true
    }

    // Navigate when both splash timer and DataStore read are done
    LaunchedEffect(splashReady, destination) {
        if (splashReady && destination !is SplashViewModel.SplashDestination.Loading) {
            when (destination) {
                is SplashViewModel.SplashDestination.Onboarding -> onNavigateToOnboarding()
                is SplashViewModel.SplashDestination.Home -> onNavigateToHome()
                else -> {} // Loading — wait
            }
        }
    }

    // Animated progress for the splash loading bar
    val progressAnimation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progressAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1600, easing = FastOutSlowInEasing)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splashPulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = CyberColors.Background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.weight(1f))

            Text(
                text = "SENSIFFMAX",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = CyberColors.CyberCyan,
                modifier = Modifier.alpha(alpha)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "CALIBRATE. TUNE. DOMINATE.",
                style = MaterialTheme.typography.labelMedium,
                color = CyberColors.TextSecondary,
                letterSpacing = MaterialTheme.typography.labelMedium.letterSpacing
            )

            Spacer(Modifier.height(24.dp))

            CyberProgressBar(
                progress = progressAnimation.value,
                modifier = Modifier.width(180.dp),
                animate = false // We control animation via Animatable
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "INITIALIZING SYSTEMS...",
                style = MaterialTheme.typography.labelSmall,
                color = CyberColors.TextTertiary
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = "Not affiliated with or endorsed by Garena.",
                style = MaterialTheme.typography.bodySmall,
                color = CyberColors.TextTertiary,
                textAlign = TextAlign.Center
            )
        }
    }
}
