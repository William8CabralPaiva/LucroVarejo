package com.cabral.lucrovarejo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.cabral.lucrovarejo.ui.screens.home.HomeViewModel
import com.cabral.lucrovarejo.ui.screens.login.LoginScreen
import com.cabral.lucrovarejo.ui.screens.register.RegisterScreen
import com.cabral.lucrovarejo.ui.screens.sales.SalesViewModel
import com.cabral.lucrovarejo.ui.screens.product.ProductViewModel
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
    salesViewModel: SalesViewModel? = null,
    productViewModel: ProductViewModel? = null,
) {
    AppNavGraphContent(
        navController = navController,
        startDestination = startDestination,
        isUserAuthenticated = isUserAuthenticated,
        onSignOut = onSignOut,
        splashScreen = { onSplashFinished -> LaunchedEffect(Unit) { onSplashFinished() } },
        loginScreen = { goToRegisterScreen, goToLoggedFlow ->
            LoginScreen(goToRegisterScreen, goToLoggedFlow)
        },
        registerScreen = { onBackPress, onRegisterSuccess ->
            RegisterScreen(onBackPress, onRegisterSuccess)
        },
        loggedFlowScreen = { onLogout ->
            LoggedFlowScreen(
                currentThemeMode = currentThemeMode,
                onThemeModeChanged = onThemeModeChanged,
                homeViewModel = homeViewModel,
                salesViewModel = salesViewModel,
                productViewModel = productViewModel,
                onLogout = onLogout
            )
        }
    )
}

@Composable
internal fun AppNavGraphContent(
    navController: NavHostController,
    startDestination: String,
    isUserAuthenticated: Boolean,
    onSignOut: () -> Unit,
    splashScreen: @Composable (() -> Unit) -> Unit,
    loginScreen: @Composable (() -> Unit, () -> Unit) -> Unit,
    registerScreen: @Composable (() -> Unit, () -> Unit) -> Unit,
    loggedFlowScreen: @Composable (() -> Unit) -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NotLoggedRoutes.SPLASH) {
            splashScreen {
                navController.navigate(
                    if (isUserAuthenticated) LoggedRoutes.HOME else NotLoggedRoutes.LOGIN
                ) {
                    popUpTo(NotLoggedRoutes.SPLASH) { inclusive = true }
                }
            }
        }

        composable(NotLoggedRoutes.LOGIN) {
            loginScreen(
                {
                    navController.navigate(NotLoggedRoutes.REGISTER)
                },
                {
                    navController.navigate(LoggedRoutes.HOME) {
                        popUpTo(NotLoggedRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(NotLoggedRoutes.REGISTER) {
            registerScreen(
                navController::popBackStack,
                {
                    navController.navigate(LoggedRoutes.HOME) {
                        popUpTo(NotLoggedRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(LoggedRoutes.HOME) {
            loggedFlowScreen {
                onSignOut()
                navController.navigate(NotLoggedRoutes.LOGIN) {
                    popUpTo(LoggedRoutes.HOME) { inclusive = true }
                }
            }
        }
    }
}
