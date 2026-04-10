package ai.nuwa.app.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ai.nuwa.app.ui.navigation.BottomNavItem
import ai.nuwa.app.ui.navigation.Screen
import ai.nuwa.app.ui.screens.*

@Composable
fun NuwaApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val viewModel: MainViewModel = viewModel()
    
    var hasCompletedOnboarding by remember { mutableStateOf(false) }
    
    val showBottomBar = currentDestination?.route in listOf(
        Screen.Home.route,
        Screen.Tasks.route,
        Screen.Skills.route,
        Screen.Profile.route
    )
    
    val messages by viewModel.messages.collectAsState()
    val currentTask by viewModel.currentTask.collectAsState()
    val serviceStatus by viewModel.serviceStatus.collectAsState()
    val isModelLoaded by viewModel.isModelLoaded.collectAsState()
    val modelStatus by viewModel.modelStatus.collectAsState()
    
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    BottomNavItem.entries.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { 
                            it.route == item.screen.route 
                        } == true
                        
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = getIconForNavItem(item, selected),
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(
                route = Screen.Splash.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                SplashScreen(
                    onNavigateToOnboarding = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToHome = {
                        hasCompletedOnboarding = true
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(
                route = Screen.Onboarding.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                OnboardingScreen(
                    onComplete = {
                        hasCompletedOnboarding = true
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(
                route = Screen.Home.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                HomeScreen(
                    messages = messages,
                    currentTask = currentTask,
                    serviceStatus = serviceStatus,
                    isModelLoaded = isModelLoaded,
                    modelStatus = modelStatus,
                    onSendMessage = { text -> viewModel.sendMessage(text) },
                    onCancelTask = { viewModel.cancelTask() },
                    onNavigateToWorldState = { navController.navigate(Screen.WorldState.route) },
                    onNavigateToTasks = { navController.navigate(Screen.Tasks.route) },
                    onNavigateToModelManager = { navController.navigate(Screen.ModelManager.route) }
                )
            }
            
            composable(
                route = Screen.Tasks.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                TasksScreen(
                    tasks = listOfNotNull(currentTask)
                )
            }
            
            composable(
                route = Screen.Skills.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                SkillsScreen()
            }
            
            composable(
                route = Screen.Profile.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                ProfileScreen(
                    currentModelName = if (isModelLoaded) modelStatus?.substringAfter("已加载: ")?.trim() else null,
                    learnedSkillsCount = 0,
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToModelManager = { navController.navigate(Screen.ModelManager.route) },
                    onNavigateToGrowth = { navController.navigate(Screen.Growth.route) }
                )
            }
            
            composable(
                route = Screen.WorldState.route,
                enterTransition = { slideInVertically { it } },
                exitTransition = { slideOutVertically { it } }
            ) {
                WorldStateSheet(
                    isAccessibilityEnabled = serviceStatus == "已连接",
                    foregroundApp = "",
                    onRequestAccessibility = { },
                    onDismiss = { navController.popBackStack() }
                )
            }
            
            composable(
                route = Screen.Settings.route,
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { it } }
            ) {
                SettingsScreen(
                    isAccessibilityEnabled = serviceStatus == "已连接",
                    onRequestAccessibility = { },
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable(
                route = Screen.ModelManager.route,
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { it } }
            ) {
                ModelManagerScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable(
                route = Screen.Growth.route,
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { it } }
            ) {
                GrowthScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable(
                route = Screen.NegotiationDemo.route,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                NegotiationDialogDemo()
            }
        }
    }
}

private fun getIconForNavItem(item: BottomNavItem, selected: Boolean): ImageVector {
    return when (item) {
        BottomNavItem.HOME -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
        BottomNavItem.TASKS -> if (selected) Icons.Filled.Checklist else Icons.Outlined.Checklist
        BottomNavItem.SKILLS -> if (selected) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome
        BottomNavItem.PROFILE -> if (selected) Icons.Filled.Person else Icons.Outlined.Person
    }
}
