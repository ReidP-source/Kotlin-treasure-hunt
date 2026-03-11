package com.example.treasurehunt

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.treasurehunt.model.AdventureDifficulty
import com.example.treasurehunt.navigation.TreasureHuntRoutes
import com.example.treasurehunt.ui.screens.CompletionScreen
import com.example.treasurehunt.ui.screens.HomeScreen
import com.example.treasurehunt.ui.screens.HuntScreen
import com.example.treasurehunt.ui.screens.PermissionsScreen
import com.example.treasurehunt.ui.screens.StickerCollectionDialog
import com.example.treasurehunt.viewmodel.TreasureHuntViewModel
import kotlinx.coroutines.delay

/**
 *  Reid Pettibone
 *  CS 492
 *  OSU
 * */
const val transitionDelay: Long = 500L
@Composable
fun TreasureHuntApp(
    viewModel: TreasureHuntViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refreshPermissionState()
    }
    val navigateHome = {
        navController.navigate(TreasureHuntRoutes.Home) {
            popUpTo(TreasureHuntRoutes.Home) { inclusive = false }
            launchSingleTop = true
        }
    }
    val navigateReturningHome = {
        if (navController.currentDestination?.route != TreasureHuntRoutes.ReturningHome) {
            val currentDestinationId = navController.currentDestination?.id
            navController.navigate(TreasureHuntRoutes.ReturningHome) {
                if (currentDestinationId != null) {
                    popUpTo(currentDestinationId) { inclusive = true }
                }
                launchSingleTop = true
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissionState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    DisposableEffect(navController) {
        val listener =
            androidx.navigation.NavController.OnDestinationChangedListener { _, destination, _ ->
                Log.d("NavTrace", "navigated_to=${destination.route}")
            }
        navController.addOnDestinationChangedListener(listener)
        onDispose { navController.removeOnDestinationChangedListener(listener) }
    }

    LaunchedEffect(uiState.completion) {
        if (uiState.completion != null && navController.currentDestination?.route != TreasureHuntRoutes.Completion) {
            navController.navigate(TreasureHuntRoutes.Completion) {
                launchSingleTop = true
            }
        }
    }

    if (uiState.isLoading || uiState.content == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7E8D1)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = uiState.stickyMessage ?: "Loading treasure map...",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF8E4100)
            )
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = TreasureHuntRoutes.Home
    ) {
        composable(TreasureHuntRoutes.Permissions) {
            PermissionsScreen(
                permissionStatus = uiState.permissionStatus,
                content = uiState.content,
                onRequestPermissions = {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                onContinue = {
                    viewModel.refreshPermissionState()
                    navigateHome()
                }
            )
        }

        composable(TreasureHuntRoutes.Home) {
            LaunchedEffect(uiState.permissionStatus.hasAnyLocationPermission) {
                if (viewModel.consumeStartupPermissionGate(uiState.permissionStatus.hasAnyLocationPermission)) {
                    navController.navigate(TreasureHuntRoutes.Permissions) {
                        launchSingleTop = true
                    }
                }
            }
            val content = uiState.content
            if (content != null) {
                HomeScreen(
                    content = content,
                    permissionStatus = uiState.permissionStatus,
                    progress = uiState.progress,
                    onStorySelected = {
                        navController.navigate(TreasureHuntRoutes.GeneratingStory) {
                            launchSingleTop = true
                        }
                    },
                    onAdventureSelected = { difficulty ->
                        navController.navigate(TreasureHuntRoutes.generatingAdventure(difficulty)) {
                            launchSingleTop = true
                        }
                    },
                    onStickerCollection = {
                        navController.navigate(TreasureHuntRoutes.Stickers)
                    },
                    onPermissionsSelected = {
                        navController.navigate(TreasureHuntRoutes.Permissions) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        composable(TreasureHuntRoutes.GeneratingStory) {
            LaunchedEffect(Unit) {
                viewModel.prepareStoryHunt()
                delay(transitionDelay)
                navController.navigate(TreasureHuntRoutes.Story) {
                    popUpTo(TreasureHuntRoutes.GeneratingStory) { inclusive = true }
                    launchSingleTop = true
                }
            }
            TransitionScreen(label = "Generating hunt...")
        }

        composable(
            route = TreasureHuntRoutes.GeneratingAdventurePattern,
            arguments = listOf(navArgument("difficulty") { type = NavType.StringType })
        ) { entry ->
            val difficultyKey = entry.arguments?.getString("difficulty")
            val difficulty = AdventureDifficulty.fromStorageKey(difficultyKey ?: AdventureDifficulty.EASY.storageKey)
            LaunchedEffect(difficulty) {
                viewModel.prepareAdventureHunt(difficulty)
                delay(transitionDelay)
                navController.navigate(TreasureHuntRoutes.adventure(difficulty)) {
                    popUpTo(TreasureHuntRoutes.generatingAdventure(difficulty)) { inclusive = true }
                    launchSingleTop = true
                }
            }
            TransitionScreen(label = "Generating hunt...")
        }

        composable(TreasureHuntRoutes.ReturningHome) {
            LaunchedEffect(Unit) {
                viewModel.abandonActiveHunt()
                delay(transitionDelay)
                if (!navController.popBackStack(TreasureHuntRoutes.Home, inclusive = false)) {
                    navigateHome()
                }
            }
            TransitionScreen(label = "Returning home...")
        }

        composable(TreasureHuntRoutes.Story) {
            val hunt = uiState.activeHunt
            if (hunt == null) {
                TransitionScreen(label = if (uiState.completion != null) "Preparing completion..." else "Returning home...")
                return@composable
            }
            HuntScreen(
                hunt = hunt,
                guidanceState = viewModel.guidanceState,
                onBack = navigateReturningHome,
                onStart = viewModel::startActiveHunt,
                onFoundIt = viewModel::checkFoundIt,
                onShowHint = viewModel::showHint,
                onDismissHint = viewModel::dismissHint,
                onCompassActiveChange = viewModel::setCompassActive
            )
        }

        composable(
            route = TreasureHuntRoutes.AdventurePattern,
            arguments = listOf(navArgument("difficulty") { type = NavType.StringType })
        ) {
            val hunt = uiState.activeHunt
            if (hunt == null) {
                TransitionScreen(label = if (uiState.completion != null) "Preparing completion..." else "Returning home...")
                return@composable
            }
            HuntScreen(
                hunt = hunt,
                guidanceState = viewModel.guidanceState,
                onBack = navigateReturningHome,
                onStart = viewModel::startActiveHunt,
                onFoundIt = viewModel::checkFoundIt,
                onShowHint = viewModel::showHint,
                onDismissHint = viewModel::dismissHint,
                onCompassActiveChange = viewModel::setCompassActive
            )
        }

        composable(TreasureHuntRoutes.Completion) {
            val completion = uiState.completion
            if (completion == null) {
                LaunchedEffect(Unit) { navigateHome() }
                return@composable
            }
            val content = uiState.content
            if (content != null) {
                CompletionScreen(
                    completion = completion,
                    content = content,
                    onHome = {
                        viewModel.clearCompletion()
                        navigateHome()
                    }
                )
            }
        }

        dialog(TreasureHuntRoutes.Stickers) {
            val content = uiState.content
            if (content != null) {
                StickerCollectionDialog(
                    content = content,
                    progress = uiState.progress,
                    onDismiss = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun TransitionScreen(label: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE4CC66)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF3A1A11)
        )
    }
}
