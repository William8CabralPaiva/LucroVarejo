package com.cabral.lucrovarejo.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cabral.lucrovarejo.ui.screens.SummaryScreen
import com.cabral.lucrovarejo.ui.screens.addpurchase.AddPurchaseScreen
import com.cabral.lucrovarejo.ui.screens.config.ConfigScreen
import com.cabral.lucrovarejo.ui.screens.home.HomeScreen
import com.cabral.lucrovarejo.ui.screens.home.HomeViewModel
import com.cabral.lucrovarejo.ui.screens.addproduct.AddProductScreen
import com.cabral.lucrovarejo.ui.screens.product.ProductScreen
import com.cabral.lucrovarejo.ui.screens.product.ProductViewModel
import com.cabral.lucrovarejo.ui.screens.sales.SalesScreen
import com.cabral.lucrovarejo.ui.screens.sales.SalesViewModel
import com.cabral.lucrovarejo.ui.theme.ThemeMode


@Composable
internal fun LoggedFlowScreen(
    currentThemeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onLogout: () -> Unit,
    homeViewModel: HomeViewModel?,
    salesViewModel: SalesViewModel?,
    productViewModel: ProductViewModel?,
) {
    val navController = rememberNavController()
    val navigator = remember(navController) { LoggedNavigator(navController) }
    val currentRoute = navController.currentBackStackEntryAsState()
        .value
        ?.destination
        ?.route
        ?: LoggedRoutes.HOME

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
                onNavigate = navigator::navigateToTab
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
                currentThemeMode = currentThemeMode,
                onThemeModeChanged = onThemeModeChanged,
                onLogout = onLogout,
                homeViewModel = homeViewModel,
                salesViewModel = salesViewModel,
                productViewModel = productViewModel
            )
        }
    }
}

@Composable
private fun LoggedDestinationNavHost(
    navController: NavHostController,
    navigator: LoggedNavigator,
    currentThemeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onLogout: () -> Unit,
    homeViewModel: HomeViewModel?,
    salesViewModel: SalesViewModel?,
    productViewModel: ProductViewModel?,
) {
    // O Home recebe novas vendas por Flow (listener do Firestore); por isso a navegacao nao
    // precisa sinalizar "salvou" nem pedir refresh ao voltar do cadastro.
    NavHost(
        navController = navController,
        startDestination = LoggedRoutes.HOME,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(LoggedRoutes.HOME) {
            HomeScreen(
                goToProductScreen = {
                    navigator.navigateTo(LoggedRoutes.ADD_PURCHASE)
                },
                goToSalesScreen = {
                    navigator.navigateToTab(LoggedRoutes.SALES)
                },
                viewModel = homeViewModel
            )
        }

        composable(LoggedRoutes.ADD_PURCHASE) {
            AddPurchaseScreen()
        }

        composable(LoggedRoutes.EDIT_PRODUCT) {
            ProductScreen(
                onAddProduct = { navigator.navigateTo(LoggedRoutes.ADD_PRODUCT) },
                viewModel = productViewModel
            )
        }

        composable(LoggedRoutes.PRODUCTS) {
            ProductScreen(
                onAddProduct = { navigator.navigateTo(LoggedRoutes.ADD_PRODUCT) },
                viewModel = productViewModel
            )
        }

        composable(LoggedRoutes.ADD_PRODUCT) {
            AddProductScreen()
        }

        composable(LoggedRoutes.SALES) {
            SalesScreen(
                onAddPurchase = { navigator.navigateTo(LoggedRoutes.ADD_PURCHASE) },
                viewModel = salesViewModel
            )
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
    // Abas da bottom navigation: nunca empilham uma sobre a outra, apenas trocam de aba.
    fun navigateToTab(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
        }
    }

    // Telas fora da bottom navigation: abrem por cima da atual e o voltar retorna a ela.
    fun navigateTo(route: String) {
        navController.navigate(route) { launchSingleTop = true }
    }

    fun navigateBack() {
        navController.popBackStack()
    }
}
