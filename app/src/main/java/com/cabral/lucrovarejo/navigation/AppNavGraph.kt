package com.cabral.lucrovarejo.navigation

import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cabral.lucrovarejo.ui.screens.ConfigScreen
import com.cabral.lucrovarejo.ui.screens.HomeScreen
import com.cabral.lucrovarejo.ui.screens.LoginScreen
import com.cabral.lucrovarejo.ui.screens.RegisterScreen
import com.cabral.lucrovarejo.ui.screens.SalesScreen
import com.cabral.lucrovarejo.ui.screens.SplashScreen
import com.cabral.lucrovarejo.ui.screens.SummaryScreen
import com.cabral.lucrovarejo.ui.theme.ThemeMode

private data class LoggedBottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = NotLoggedRoutes.SPLASH,
    currentThemeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeChanged: (ThemeMode) -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NotLoggedRoutes.SPLASH) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(NotLoggedRoutes.LOGIN) {
                        popUpTo(NotLoggedRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(NotLoggedRoutes.LOGIN) {
            LoginScreen(
                goToRegisterScreen = {
                    navController.navigate(NotLoggedRoutes.REGISTER)
                },
                goToLoggedFlow = {
                    navController.navigate(LoggedRoutes.HOME) {
                        popUpTo(NotLoggedRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(NotLoggedRoutes.REGISTER) {
            RegisterScreen(
                onBackPress = {
                    navController.popBackStack()
                }
            )
        }

        composable(LoggedRoutes.HOME) {
            LoggedFlowScreen(
                currentThemeMode = currentThemeMode,
                onThemeModeChanged = onThemeModeChanged,
                onLogout = {
                    navController.navigate(NotLoggedRoutes.LOGIN) {
                        popUpTo(LoggedRoutes.HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
private fun LoggedFlowScreen(
    currentThemeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onLogout: () -> Unit
) {
    val innerNavController = rememberNavController()
    val dividerColor = MaterialTheme.colorScheme.outline
    val items = listOf(
        LoggedBottomNavItem(route = LoggedRoutes.HOME, label = "Início", icon = Icons.Default.Home),
        LoggedBottomNavItem(route = LoggedRoutes.SALES, label = "Vendas", icon = Icons.AutoMirrored.Filled.ReceiptLong),
        LoggedBottomNavItem(route = LoggedRoutes.SUMMARY, label = "Resumo", icon = Icons.Default.BarChart)
    )

    Scaffold(
        bottomBar = {
            val currentRoute = innerNavController.currentBackStackEntryAsState().value?.destination?.route
                ?: LoggedRoutes.HOME
            if (items.any { it.route == currentRoute }) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    modifier = Modifier.drawBehind {
                        drawLine(
                            color = dividerColor,
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                ) {
                    items.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                innerNavController.navigate(item.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(innerNavController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = innerNavController,
            startDestination = LoggedRoutes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(LoggedRoutes.HOME) {
                HomeScreen(
                    goToConfigScreen = {
                        innerNavController.navigate(LoggedRoutes.CONFIG)
                    },
                    onLogout = onLogout
                )
            }

            composable(LoggedRoutes.SALES) {
                SalesScreen()
            }

            composable(LoggedRoutes.SUMMARY) {
                SummaryScreen()
            }

            composable(LoggedRoutes.CONFIG) {
                ConfigScreen(
                    currentThemeMode = currentThemeMode,
                    onThemeModeChanged = onThemeModeChanged,
                    onBackPress = {
                        innerNavController.popBackStack()
                    }
                )
            }
        }
    }
}
