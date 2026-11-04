package com.cabral.lucrovarejo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.cabral.lucrovarejo.ui.screens.forgotpassword.ForgotPasswordScreen
import com.cabral.lucrovarejo.ui.screens.home.HomeViewModel
import com.cabral.lucrovarejo.ui.screens.login.LoginScreen
import com.cabral.lucrovarejo.ui.screens.register.RegisterScreen
import com.cabral.lucrovarejo.ui.screens.sales.SalesViewModel
import com.cabral.lucrovarejo.ui.screens.product.ProductViewModel
import com.cabral.lucrovarejo.ui.store.StoreViewModel
import com.cabral.lucrovarejo.ui.theme.ThemeMode

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = NotLoggedRoutes.SPLASH,
    isUserAuthenticated: Boolean = false,
    onSignOut: () -> Unit = {},
    currentThemeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeChange: (ThemeMode) -> Unit = {},
    homeViewModel: HomeViewModel? = null,
    salesViewModel: SalesViewModel? = null,
    productViewModel: ProductViewModel? = null,
    storeViewModel: StoreViewModel? = null,
) {
    AppNavGraphContent(
        navController = navController,
        startDestination = startDestination,
        isUserAuthenticated = isUserAuthenticated,
        onSignOut = onSignOut,
        splashScreen = { onSplashFinished -> LaunchedEffect(Unit) { onSplashFinished() } },
        loginScreen = { goToRegisterScreen, goToLoggedFlow, goToForgotPassword ->
            LoginScreen(goToRegisterScreen, goToLoggedFlow, goToForgotPassword)
        },
        forgotPasswordScreen = { onBack -> ForgotPasswordScreen(onBack) },
        registerScreen = { onBackPress, onRegisterSuccess ->
            RegisterScreen(onBackPress, onRegisterSuccess)
        },
        loggedFlowScreen = { onLogout ->
            LaunchedEffect(storeViewModel) { storeViewModel?.load() }
            val storeState = storeViewModel?.uiState?.collectAsState()?.value
            LoggedFlowScreen(
                currentThemeMode = currentThemeMode,
                onThemeModeChange = onThemeModeChange,
                homeViewModel = homeViewModel,
                salesViewModel = salesViewModel,
                productViewModel = productViewModel,
                storeName = storeState?.displayName,
                onUpdateStoreName = { name, onResult ->
                    storeViewModel?.updateDisplayName(name, onResult) ?: onResult(false)
                },
                onLogout = {
                    storeViewModel?.clear()
                    onLogout()
                }
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
    loginScreen: @Composable (() -> Unit, () -> Unit, () -> Unit) -> Unit,
    registerScreen: @Composable (() -> Unit, () -> Unit) -> Unit,
    loggedFlowScreen: @Composable (() -> Unit) -> Unit,
    forgotPasswordScreen: @Composable (() -> Unit) -> Unit = {},
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
                },
                {
                    navController.navigate(NotLoggedRoutes.FORGOT_PASSWORD)
                }
            )
        }

        composable(NotLoggedRoutes.FORGOT_PASSWORD) {
            forgotPasswordScreen(navController::popBackStack)
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
