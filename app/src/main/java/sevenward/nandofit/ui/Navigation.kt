package sevenward.nandofit.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import sevenward.nandofit.ui.screens.ActiveWorkoutScreen
import sevenward.nandofit.ui.screens.HomeScreen
import sevenward.nandofit.ui.screens.ProgressScreen
import sevenward.nandofit.ui.screens.SummaryScreen
import sevenward.nandofit.ui.theme.NandoBlack
import sevenward.nandofit.ui.theme.NandoDarkCard
import sevenward.nandofit.ui.theme.NandoNeonGreen

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val workoutViewModel: WorkoutViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = listOf("home", "progress")

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = currentRoute in bottomBarRoutes,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar(
                    containerColor = NandoDarkCard,
                    contentColor = NandoNeonGreen
                ) {
                    NavigationBarItem(
                        selected = currentRoute == "home",
                        onClick = {
                            navController.navigate("home") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Rutina") },
                        label = { Text("Rutina") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NandoBlack,
                            selectedTextColor = NandoNeonGreen,
                            indicatorColor = NandoNeonGreen,
                            unselectedIconColor = NandoNeonGreen.copy(alpha = 0.5f),
                            unselectedTextColor = NandoNeonGreen.copy(alpha = 0.5f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == "progress",
                        onClick = {
                            navController.navigate("progress") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Timeline, contentDescription = "Progreso") },
                        label = { Text("Progreso") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NandoBlack,
                            selectedTextColor = NandoNeonGreen,
                            indicatorColor = NandoNeonGreen,
                            unselectedIconColor = NandoNeonGreen.copy(alpha = 0.5f),
                            unselectedTextColor = NandoNeonGreen.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = workoutViewModel,
                    onStartWorkout = { navController.navigate("activeWorkout") }
                )
            }
            composable("activeWorkout") {
                ActiveWorkoutScreen(
                    viewModel = workoutViewModel,
                    onWorkoutComplete = {
                        navController.navigate("summary") {
                            popUpTo("home") { inclusive = false }
                        }
                    }
                )
            }
            composable("summary") {
                SummaryScreen(
                    viewModel = workoutViewModel,
                    onHomeReturn = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }
            composable("progress") {
                ProgressScreen(
                    viewModel = workoutViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
