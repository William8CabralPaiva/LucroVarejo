package com.cabral.lucrovarejo.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cabral.lucrovarejo.ui.screens.config.ConfigScreen
import com.cabral.lucrovarejo.ui.screens.home.HomeScreen
import com.cabral.lucrovarejo.ui.screens.login.LoginScreen
import com.cabral.lucrovarejo.ui.screens.register.RegisterScreen
import com.cabral.lucrovarejo.ui.screens.SalesScreen
import com.cabral.lucrovarejo.ui.screens.SplashScreen
import com.cabral.lucrovarejo.ui.screens.SummaryScreen
import com.cabral.lucrovarejo.ui.screens.addpurchase.AddPurchaseScreen
import com.cabral.lucrovarejo.ui.screens.home.HomeViewModel
import com.cabral.lucrovarejo.ui.screens.product.ProductScreen
import com.cabral.lucrovarejo.ui.theme.ThemeMode

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = NotLoggedRoutes.SPLASH,
    isUserAuthenticated: Boolean = false,
    onSignOut: () -> Unit = {},
    currentThemeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeChanged: (ThemeMode) -> Unit = {},
    homeViewModel: HomeViewModel? = null,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NotLoggedRoutes.SPLASH) {
            SplashScreen(
                onSplashFinished = {
                    val destination = if (isUserAuthenticated) {
                        LoggedRoutes.HOME
                    } else {
                        NotLoggedRoutes.LOGIN
                    }
                    navController.navigate(destination) {
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
                },
                onRegisterSuccess = {
                    navController.navigate(LoggedRoutes.HOME) {
                        popUpTo(NotLoggedRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(LoggedRoutes.HOME) {
            LoggedFlowScreen(
                currentThemeMode = currentThemeMode,
                onThemeModeChanged = onThemeModeChanged,
                homeViewModel = homeViewModel,
                onLogout = {
                    onSignOut()
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
    onLogout: () -> Unit,
    homeViewModel: HomeViewModel?,
) {
    val innerNavController = rememberNavController()
    val currentRoute =
        innerNavController.currentBackStackEntryAsState().value?.destination?.route
            ?: LoggedRoutes.HOME

    Scaffold(
        topBar = {
            LoggedRouteTopBar(
                route = currentRoute,
                onBackPress = { innerNavController.popBackStack() }
            )
        },
        bottomBar = {
            BottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    innerNavController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(innerNavController.graph.startDestinationId) {
                            saveState = true
                        }
                    }
                }
            )
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
                    goToProductScreen = {
                        //TODO alterar
                        innerNavController.navigate(LoggedRoutes.ADD_PURCHASE)
                    },
                    onLogout = onLogout,
                    viewModel = homeViewModel
                )
            }

            composable(LoggedRoutes.ADD_PURCHASE) {
                AddPurchaseScreen(onPurchaseSaved = { innerNavController.popBackStack() })
            }

            composable(LoggedRoutes.PRODUCT) {
                ProductScreen()
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
                    onLogout = onLogout
                )
            }
        }
    }
}
