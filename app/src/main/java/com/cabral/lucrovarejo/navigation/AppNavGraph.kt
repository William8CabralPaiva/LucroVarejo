package com.cabral.lucrovarejo.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
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
            LoginScreen(
                goToRegisterScreen,
                {
                    // Apenas o login manual abre as dicas; a sessao restaurada pela splash nao.
                    storeViewModel?.showTips()
                    goToLoggedFlow()
                },
                goToForgotPassword
            )
        },
        forgotPasswordScreen = { onBack -> ForgotPasswordScreen(onBack) },
        registerScreen = { onBackPress, onRegisterSuccess ->
            RegisterScreen(onBackPress, onRegisterSuccess)
        },
        loggedFlowScreen = { onLogout ->
            LaunchedEffect(storeViewModel) { storeViewModel?.load() }
            val storeState = storeViewModel?.uiState?.collectAsState()?.value
            // A chave recria o fluxo logado ao trocar de loja, e as telas passam a observar a nova loja.
            key(storeState?.storeEpoch ?: 0) {
                LoggedFlow(
                    currentThemeMode = currentThemeMode,
                    onThemeModeChange = onThemeModeChange,
                    homeViewModel = homeViewModel,
                    salesViewModel = salesViewModel,
                    productViewModel = productViewModel,
                    storeName = storeState?.displayName,
                    storeNameRequired = storeState?.let {
                        it.nameLoaded && it.displayName.isNullOrBlank()
                    } == true,
                    stores = storeState?.stores.orEmpty(),
                    activeStoreId = storeState?.activeStoreId,
                    onSwitchStore = { storeViewModel?.selectStore(it) },
                    onCreateStore = { name, onResult ->
                        storeViewModel?.createStore(name, onResult) ?: onResult(false)
                    },
                    showTips = storeState?.showTips == true,
                    onDismissTips = { storeViewModel?.dismissTips() },
                    onUpdateStoreName = { name, onResult ->
                        storeViewModel?.updateDisplayName(name, onResult) ?: onResult(false)
                    },
                    onLogout = {
                        storeViewModel?.clear()
                        onLogout()
                    }
                )
            }
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
    val focusManager = LocalFocusManager.current
    val backgroundTapInteraction = remember { MutableInteractionSource() }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.clickable(
            interactionSource = backgroundTapInteraction,
            indication = null
        ) { focusManager.clearFocus() }
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
