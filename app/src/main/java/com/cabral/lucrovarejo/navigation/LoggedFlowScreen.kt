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
import com.cabral.lucrovarejo.domain.product.ProductModel
import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.domain.payment.PaymentTerminalModel
import com.cabral.lucrovarejo.ui.screens.SummaryScreen
import com.cabral.lucrovarejo.ui.screens.addpurchase.AddPurchaseScreen
import com.cabral.lucrovarejo.ui.screens.config.ConfigScreen
import com.cabral.lucrovarejo.ui.screens.machinehistory.MachineHistoryScreen
import com.cabral.lucrovarejo.ui.screens.machinehistory.FeeSlotKey
import com.cabral.lucrovarejo.ui.screens.home.HomeScreen
import com.cabral.lucrovarejo.ui.screens.home.HomeViewModel
import com.cabral.lucrovarejo.ui.screens.addproduct.AddProductScreen
import com.cabral.lucrovarejo.ui.screens.product.ProductScreen
import com.cabral.lucrovarejo.ui.screens.product.ProductViewModel
import com.cabral.lucrovarejo.ui.screens.sales.SalesScreen
import com.cabral.lucrovarejo.ui.screens.sales.SalesViewModel
import com.cabral.lucrovarejo.ui.screens.machinesale.MachineSaleScreen
import com.cabral.lucrovarejo.ui.screens.machinelist.MachineListScreen
import com.cabral.lucrovarejo.ui.theme.ForestGreenHeader
import com.cabral.lucrovarejo.ui.theme.LocalIsDarkTheme
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

    val isDark = LocalIsDarkTheme.current
    val scaffoldContainerColor = if (isDark) ForestGreenHeader else MaterialTheme.colorScheme.primary

    Scaffold(
        containerColor = scaffoldContainerColor,
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
                    navigator.navigateTo(LoggedRoutes.ADD_PRODUCT)
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
            AddProductScreen(
                product = navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<ProductModel>(LoggedNavigator.PRODUCT_TO_EDIT_KEY),
                onFinished = navigator::navigateBack
            )
        }

        composable(LoggedRoutes.PRODUCTS) {
            ProductScreen(
                onAddProduct = { navigator.navigateTo(LoggedRoutes.ADD_PRODUCT) },
                onEditProduct = navigator::navigateToEditProduct,
                viewModel = productViewModel
            )
        }

        composable(LoggedRoutes.ADD_PRODUCT) {
            AddProductScreen()
        }

        composable(LoggedRoutes.SALES) {
            SalesScreen(
                onAddPurchase = { navigator.navigateTo(LoggedRoutes.ADD_PURCHASE) },
                onEditPurchase = navigator::navigateToEditPurchase,
                viewModel = salesViewModel
            )
        }

        composable(LoggedRoutes.EDIT_SALES) {
            AddPurchaseScreen(
                purchase = navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<PurchaseModel>(LoggedNavigator.PURCHASE_TO_EDIT_KEY),
                onFinished = navigator::navigateBack
            )
        }

        //NÃ£o descomentar
//        composable(LoggedRoutes.SUMMARY) {
//            SummaryScreen()
//        }

        composable(LoggedRoutes.CONFIG) {
            ConfigScreen(
                currentThemeMode = currentThemeMode,
                onThemeModeChanged = onThemeModeChanged,
                onLogout = onLogout,
                onNavigateToTerminals = { navigator.navigateTo(LoggedRoutes.MACHINE_LIST) }
            )
        }

        composable(LoggedRoutes.MACHINE_LIST) {
            MachineListScreen(
                onAddTerminal = { navigator.navigateTo(LoggedRoutes.ADD_MACHINE) },
                onEditTerminal = navigator::navigateToEditTerminal
            )
        }

        composable(LoggedRoutes.ADD_MACHINE) {
            MachineSaleScreen(
                onFinished = navigator::navigateBack,
                onOpenMachineHistory = navigator::navigateToMachineHistory
            )
        }

        composable(LoggedRoutes.EDIT_MACHINE) {
            MachineSaleScreen(
                terminal = navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<PaymentTerminalModel>(LoggedNavigator.TERMINAL_TO_EDIT_KEY),
                onFinished = navigator::navigateBack,
                onOpenMachineHistory = navigator::navigateToMachineHistory
            )
        }

        composable(LoggedRoutes.MACHINE_HISTORY) {
            val slot = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<FeeSlotKey>(LoggedNavigator.FEE_SLOT_KEY)
            if (slot != null) {
                MachineHistoryScreen(slot = slot)
            }
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

    // O produto vai no SavedStateHandle da tela atual e a tela de edicao le pelo previousBackStackEntry.
    fun navigateToEditProduct(product: ProductModel) {
        navController.currentBackStackEntry?.savedStateHandle?.set(PRODUCT_TO_EDIT_KEY, product)
        navigateTo(LoggedRoutes.EDIT_PRODUCT)
    }

    fun navigateToEditPurchase(purchase: PurchaseModel) {
        navController.currentBackStackEntry?.savedStateHandle?.set(PURCHASE_TO_EDIT_KEY, purchase)
        navigateTo(LoggedRoutes.EDIT_SALES)
    }

    fun navigateToEditTerminal(terminal: PaymentTerminalModel) {
        navController.currentBackStackEntry?.savedStateHandle?.set(TERMINAL_TO_EDIT_KEY, terminal)
        navigateTo(LoggedRoutes.EDIT_MACHINE)
    }

    fun navigateToMachineHistory(slot: FeeSlotKey) {
        navController.currentBackStackEntry?.savedStateHandle?.set(FEE_SLOT_KEY, slot)
        navigateTo(LoggedRoutes.MACHINE_HISTORY)
    }

    fun navigateBack() {
        navController.popBackStack()
    }

    companion object {
        const val PRODUCT_TO_EDIT_KEY = "product_to_edit"
        const val PURCHASE_TO_EDIT_KEY = "purchase_to_edit"
        const val TERMINAL_TO_EDIT_KEY = "terminal_to_edit"
        const val FEE_SLOT_KEY = "fee_slot_key"
    }
}
