package com.cabral.lucrovarejo.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cabral.lucrovarejo.ui.screens.ConfigScreen
import com.cabral.lucrovarejo.ui.screens.HomeScreen
import com.cabral.lucrovarejo.ui.screens.LoginScreen
import com.cabral.lucrovarejo.ui.screens.RegisterScreen
import com.cabral.lucrovarejo.ui.screens.SplashScreen
import com.cabral.lucrovarejo.ui.theme.ThemeMode

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
            HomeScreen(
                goToConfigScreen = {
                    navController.navigate(LoggedRoutes.CONFIG)
                },
                onLogout = {
                    navController.navigate(NotLoggedRoutes.LOGIN) {
                        popUpTo(LoggedRoutes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(LoggedRoutes.CONFIG) {
            ConfigScreen(
                currentThemeMode = currentThemeMode,
                onThemeModeChanged = onThemeModeChanged,
                onBackPress = {
                    navController.popBackStack()
                }
            )
        }
    }
}
