package com.sensiffmax.app.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sensiffmax.app.core.components.CyberNavItem
import com.sensiffmax.app.core.components.CyberNavigationBar
import com.sensiffmax.app.core.ui.theme.CyberColors
import com.sensiffmax.app.feature.calibration.*
import com.sensiffmax.app.feature.home.HomeScreen
import com.sensiffmax.app.feature.onboarding.OnboardingScreen
import com.sensiffmax.app.feature.profile.PlayerProfileScreen
import com.sensiffmax.app.feature.profiles.ProfilesScreen
import com.sensiffmax.app.feature.recommendation.SensitivityRecommendationScreen
import com.sensiffmax.app.feature.recommendation.TuneSensitivityScreen
import com.sensiffmax.app.feature.settings.SettingsScreen
import com.sensiffmax.app.feature.splash.SplashScreen
import com.sensiffmax.app.feature.training.FlickTrainingScreen
import com.sensiffmax.app.feature.training.ReactionTrainingScreen
import com.sensiffmax.app.feature.training.TrainingLabScreen

val mainBottomNavItems = listOf(
    CyberNavItem(
        label = "Home",
        icon = Icons.Default.Home,
        route = Routes.HOME
    ),
    CyberNavItem(
        label = "Calibrate",
        icon = Icons.Default.Speed,
        route = Routes.CALIBRATION
    ),
    CyberNavItem(
        label = "Train",
        icon = Icons.Default.SportsEsports,
        route = Routes.TRAINING
    ),
    CyberNavItem(
        label = "Profiles",
        icon = Icons.Default.FolderShared,
        route = Routes.PROFILES
    ),
    CyberNavItem(
        label = "Profile",
        icon = Icons.Default.Person,
        route = Routes.PROFILE
    )
)

val bottomBarRoutes = setOf(
    Routes.HOME,
    Routes.CALIBRATION,
    Routes.TRAINING,
    Routes.PROFILES,
    Routes.PROFILE
)

@Composable
fun SensiFFMaxAppContent(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = CyberColors.Background,
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                CyberNavigationBar(
                    items = mainBottomNavItems,
                    selectedRoute = currentRoute ?: Routes.HOME,
                    onItemSelected = { item ->
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SensiFFMaxNavHost(navController = navController)
        }
    }
}

@Composable
fun SensiFFMaxNavHost(
    navController: NavHostController,
    startDestination: String = Routes.SPLASH,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Startup
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onNavigateBack = { navController.popBackStack() },
                onFinishOnboarding = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        // Main Bottom Navigation Tabs
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToCalibration = { navController.navigate(Routes.CALIBRATION) },
                onNavigateToTraining = { navController.navigate(Routes.TRAINING) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.CALIBRATION) {
            CalibrationScreen(
                onStartCalibration = { navController.navigate(Routes.REACTION_TEST) },
                onStartPrecision = { navController.navigate(Routes.PRECISION_TEST) },
                onStartDrag = { navController.navigate(Routes.DRAG_TEST) }
            )
        }

        composable(Routes.TRAINING) {
            TrainingLabScreen(
                onNavigateToFlick = { navController.navigate(Routes.FLICK_TRAINING) },
                onNavigateToReaction = { navController.navigate(Routes.REACTION_TRAINING) }
            )
        }

        composable(Routes.PROFILES) {
            ProfilesScreen(
                onNavigateToCalibration = { navController.navigate(Routes.CALIBRATION) }
            )
        }

        composable(Routes.PROFILE) {
            PlayerProfileScreen(
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        // Calibration Flow
        composable(Routes.REACTION_TEST) {
            ReactionTestScreen(
                onNavigateBack = { navController.popBackStack() },
                onTestComplete = {
                    navController.navigate(Routes.REACTION_RESULTS) {
                        popUpTo(Routes.REACTION_TEST) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REACTION_RESULTS) {
            ReactionResultsScreen(
                onNavigateBack = { navController.popBackStack() },
                onContinueToPrecision = { navController.navigate(Routes.PRECISION_TEST) },
                onRetest = {
                    navController.navigate(Routes.REACTION_TEST) {
                        popUpTo(Routes.REACTION_RESULTS) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.PRECISION_TEST) {
            PrecisionTestScreen(
                onNavigateBack = { navController.popBackStack() },
                onTestComplete = {
                    navController.navigate(Routes.PRECISION_RESULTS) {
                        popUpTo(Routes.PRECISION_TEST) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.PRECISION_RESULTS) {
            PrecisionResultsScreen(
                onNavigateBack = { navController.popBackStack() },
                onContinueToDrag = { navController.navigate(Routes.DRAG_TEST) },
                onRetest = {
                    navController.navigate(Routes.PRECISION_TEST) {
                        popUpTo(Routes.PRECISION_RESULTS) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DRAG_TEST) {
            DragTestScreen(
                onNavigateBack = { navController.popBackStack() },
                onTestComplete = {
                    navController.navigate(Routes.DRAG_RESULTS) {
                        popUpTo(Routes.DRAG_TEST) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DRAG_RESULTS) {
            DragResultsScreen(
                onNavigateBack = { navController.popBackStack() },
                onContinueToCalibrationResults = { navController.navigate(Routes.CALIBRATION_RESULTS) },
                onRetest = {
                    navController.navigate(Routes.DRAG_TEST) {
                        popUpTo(Routes.DRAG_RESULTS) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.CALIBRATION_RESULTS) {
            CalibrationResultsScreen(
                onNavigateBack = { navController.popBackStack() },
                onGenerateSensitivity = { navController.navigate(Routes.SENSITIVITY_RECOMMENDATION) }
            )
        }

        composable(Routes.SENSITIVITY_RECOMMENDATION) {
            SensitivityRecommendationScreen(
                onNavigateBack = { navController.popBackStack() },
                onTuneSensitivity = { navController.navigate(Routes.TUNE_SENSITIVITY) },
                onSaveProfile = {
                    navController.navigate(Routes.PROFILES) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }

        composable(Routes.TUNE_SENSITIVITY) {
            TuneSensitivityScreen(
                onNavigateBack = { navController.popBackStack() },
                onSaveTunedSensitivity = {
                    navController.navigate(Routes.PROFILES) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }

        // Training Sub-screens
        composable(Routes.FLICK_TRAINING) {
            FlickTrainingScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.REACTION_TRAINING) {
            ReactionTrainingScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Settings
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
