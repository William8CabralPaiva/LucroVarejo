package com.cabral.lucrovarejo.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cabral.lucrovarejo.ui.screens.SalesScreen
import com.cabral.lucrovarejo.ui.screens.SummaryScreen
import com.cabral.lucrovarejo.ui.screens.addpurchase.AddPurchaseScreen
import com.cabral.lucrovarejo.ui.screens.config.ConfigScreen
import com.cabral.lucrovarejo.ui.screens.home.HomeScreen
import com.cabral.lucrovarejo.ui.screens.home.HomeViewModel
import com.cabral.lucrovarejo.ui.screens.product.ProductScreen
import com.cabral.lucrovarejo.ui.theme.ThemeMode

private const val HOME_REFRESH_REQUESTED_KEY = "home_refresh_requested"

@Composable
internal fun LoggedFlowScreen(
    currentThemeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onLogout: () -> Unit,
    homeViewModel: HomeViewModel?,
) {
    val navController = rememberNavController()
    val navigator = remember(navController) { LoggedNavigator(navController) }
    val currentRoute = navController.currentBackStackEntryAsState()
        .value
        ?.destination
        ?.route
        ?: LoggedRoutes.HOME

    LaunchedEffect(currentRoute) {
        navigator.onRouteChanged(currentRoute)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.primary,
        topBar = {
            LoggedRouteTopBar(
                route = currentRoute,
                onBackPress = navigator::navigateBack
            )
        },
        bottomBar = {
            BottomNavBar(
                currentRoute = currentRoute,
                onNavigate = navigator::navigateTo
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            LoggedDestinationNavHost(
                navController = navController,
                navigator = navigator,
                currentRoute = currentRoute,
                currentThemeMode = currentThemeMode,
                onThemeModeChanged = onThemeModeChanged,
                onLogout = onLogout,
                homeViewModel = homeViewModel
            )
        }
    }
}

@Composable
private fun LoggedDestinationNavHost(
    navController: NavHostController,
    navigator: LoggedNavigator,
    currentRoute: String,
    currentThemeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onLogout: () -> Unit,
    homeViewModel: HomeViewModel?,
) {
    NavHost(
        navController = navController,
        startDestination = LoggedRoutes.HOME,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(LoggedRoutes.HOME) { backStackEntry ->
            val refreshRequested by backStackEntry.savedStateHandle
                .getStateFlow(HOME_REFRESH_REQUESTED_KEY, false)
                .collectAsState()

            HomeScreen(
                goToProductScreen = {
                    navigator.navigateTo(LoggedRoutes.ADD_PURCHASE)
                },
                goToSalesScreen = {
                    navigator.navigateTo(LoggedRoutes.SALES)
                },
                refreshRequested = refreshRequested,
                onRefreshHandled = {
                    backStackEntry.savedStateHandle[HOME_REFRESH_REQUESTED_KEY] = false
                },
                viewModel = homeViewModel
            )
        }

        composable(LoggedRoutes.ADD_PURCHASE) {
            AddPurchaseScreen(
                onPurchaseSaved = {
                    navigator.requestHomeRefresh()
                    navigator.navigateBack()
                }
            )
        }

        composable(LoggedRoutes.PRODUCT) {
            ProductScreen(
                onProductSaved = navigator::markProductSaved
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
                onLogout = onLogout
            )
        }
    }
}

private class LoggedNavigator(
    private val navController: NavHostController,
) {
    private var previousRoute: String? = null
    private var hasSavedProduct = false

    fun navigateTo(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
        }
    }

    fun markProductSaved() {
        hasSavedProduct = true
    }

    fun requestHomeRefresh() {
        navController.getBackStackEntry(LoggedRoutes.HOME)
            .savedStateHandle[HOME_REFRESH_REQUESTED_KEY] = true
    }

    fun onRouteChanged(currentRoute: String) {
        if (
            previousRoute == LoggedRoutes.PRODUCT &&
            currentRoute == LoggedRoutes.HOME &&
            hasSavedProduct
        ) {
            requestHomeRefresh()
            hasSavedProduct = false
        }
        previousRoute = currentRoute
    }

    fun navigateBack() {
        navController.popBackStack()
    }
}
