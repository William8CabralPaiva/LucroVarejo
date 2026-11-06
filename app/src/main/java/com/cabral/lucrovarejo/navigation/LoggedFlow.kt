package com.cabral.lucrovarejo.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.cabral.lucrovarejo.R
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cabral.lucrovarejo.domain.product.ProductModel
import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.domain.payment.PaymentTerminalModel
import com.cabral.lucrovarejo.domain.store.StoreSummary
import com.cabral.lucrovarejo.ui.components.StoreNameDialog
import com.cabral.lucrovarejo.ui.components.SwitchStoreDialog
import com.cabral.lucrovarejo.ui.screens.addpurchase.AddPurchaseScreen
import com.cabral.lucrovarejo.ui.screens.config.ConfigScreen
import com.cabral.lucrovarejo.ui.screens.machinehistory.MachineHistoryScreen
import com.cabral.lucrovarejo.ui.screens.machinehistory.PaymentMachineHistoryArgs
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
import kotlinx.coroutines.launch


@Composable
internal fun LoggedFlow(
    currentThemeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLogout: () -> Unit,
    homeViewModel: HomeViewModel?,
    salesViewModel: SalesViewModel?,
    productViewModel: ProductViewModel?,
    storeName: String? = null,
    stores: List<StoreSummary> = emptyList(),
    activeStoreId: String? = null,
    onSwitchStore: (String) -> Unit = {},
    onCreateStore: (String, (Boolean) -> Unit) -> Unit = { _, _ -> },
    showTips: Boolean = false,
    onDismissTips: () -> Unit = {},
    onUpdateStoreName: (String, (Boolean) -> Unit) -> Unit = { _, _ -> },
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
    var machineTourActive by remember { mutableStateOf(false) }
    var showSwitchStoreDialog by remember { mutableStateOf(false) }
    var showCreateStoreDialog by remember { mutableStateOf(false) }
    // Sair do formulario por outro caminho (ex.: seta de voltar) encerra o tour.
    LaunchedEffect(currentRoute) {
        if (currentRoute != LoggedRoutes.ADD_MACHINE) machineTourActive = false
    }
    val startMachineTour = {
        machineTourActive = true
        navigator.navigateTo(LoggedRoutes.ADD_MACHINE)
    }
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val showMessage: (String) -> Unit = { message ->
        scope.launch { snackBarHostState.showSnackbar(message) }
    }

    Scaffold(
        containerColor = scaffoldContainerColor,
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = ForestGreenHeader,
                    contentColor = Color.White
                )
            }
        },
        topBar = {
            LoggedRouteTopBar(
                route = currentRoute,
                homeTitle = storeName,
                onSwitchStore = { showSwitchStoreDialog = true },
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
                .padding(innerPadding)
                //keyboard screen
                .consumeWindowInsets(innerPadding)
                .imePadding(),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            LoggedDestinationNavHost(
                navController = navController,
                navigator = navigator,
                currentThemeMode = currentThemeMode,
                onThemeModeChange = onThemeModeChange,
                onLogout = onLogout,
                onShowMessage = showMessage,
                homeViewModel = homeViewModel,
                salesViewModel = salesViewModel,
                productViewModel = productViewModel,
                storeName = storeName,
                onOpenSwitchStore = { showSwitchStoreDialog = true },
                onShowTips = startMachineTour,
                machineTourActive = machineTourActive,
                onMachineTourFinish = {
                    machineTourActive = false
                    navigator.navigateBack()
                },
                onUpdateStoreName = onUpdateStoreName
            )
        }
    }

    if (showSwitchStoreDialog) {
        SwitchStoreDialog(
            stores = stores,
            activeStoreId = activeStoreId,
            onSelect = { storeId ->
                showSwitchStoreDialog = false
                onSwitchStore(storeId)
            },
            onCreate = {
                showSwitchStoreDialog = false
                showCreateStoreDialog = true
            },
            onDismiss = { showSwitchStoreDialog = false }
        )
    }

    if (showCreateStoreDialog) {
        StoreNameDialog(
            initialName = "",
            title = R.string.settings_create_store_dialog_title,
            errorMessage = R.string.settings_create_store_error,
            onSave = onCreateStore,
            onDismiss = { showCreateStoreDialog = false }
        )
    }
    if (showTips) {
        AlertDialog(
            onDismissRequest = onDismissTips,
            title = { Text(stringResource(R.string.coach_tour_prompt_title)) },
            text = { Text(stringResource(R.string.coach_tour_prompt_text)) },
            confirmButton = {
                TextButton(onClick = {
                    onDismissTips()
                    startMachineTour()
                }) { Text(stringResource(R.string.coach_tour_prompt_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = onDismissTips) {
                    Text(stringResource(R.string.coach_tour_prompt_dismiss))
                }
            }
        )
    }
}

@Composable
private fun LoggedDestinationNavHost(
    navController: NavHostController,
    navigator: LoggedNavigator,
    currentThemeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLogout: () -> Unit,
    onShowMessage: (String) -> Unit,
    homeViewModel: HomeViewModel?,
    salesViewModel: SalesViewModel?,
    productViewModel: ProductViewModel?,
    storeName: String?,
    onOpenSwitchStore: () -> Unit,
    onShowTips: () -> Unit,
    machineTourActive: Boolean,
    onMachineTourFinish: () -> Unit,
    onUpdateStoreName: (String, (Boolean) -> Unit) -> Unit,
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
                goToSalesScreen = {
                    navigator.navigateToTab(LoggedRoutes.SALES)
                },
                onEditPurchase = navigator::navigateToEditPurchase,
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
                onFinish = navigator::navigateBack
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
                onAddProduct = { navigator.navigateTo(LoggedRoutes.ADD_PRODUCT) },
                onEditPurchase = navigator::navigateToEditPurchase,
                viewModel = salesViewModel
            )
        }

        composable(LoggedRoutes.EDIT_SALES) {
            AddPurchaseScreen(
                purchase = navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<PurchaseModel>(LoggedNavigator.PURCHASE_TO_EDIT_KEY),
                onFinish = navigator::navigateBack
            )
        }

        //NÃ£o descomentar
//        composable(LoggedRoutes.SUMMARY) {
//            SummaryScreen()
//        }

        composable(LoggedRoutes.CONFIG) {
            ConfigScreen(
                currentThemeMode = currentThemeMode,
                onThemeModeChange = onThemeModeChange,
                onLogout = onLogout,
                onNavigateToTerminals = { navigator.navigateTo(LoggedRoutes.MACHINE_LIST) },
                storeName = storeName,
                onOpenSwitchStore = onOpenSwitchStore,
                onShowTips = onShowTips,
                onUpdateStoreName = onUpdateStoreName
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
                onFinish = navigator::navigateBackToConfig,
                onShowMessage = onShowMessage,
                onOpenMachineHistory = navigator::navigateToMachineHistory,
                tourActive = machineTourActive,
                onTourFinish = onMachineTourFinish
            )
        }

        composable(LoggedRoutes.EDIT_MACHINE) {
            MachineSaleScreen(
                terminal = navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<PaymentTerminalModel>(LoggedNavigator.TERMINAL_TO_EDIT_KEY),
                onFinish = navigator::navigateBackToConfig,
                onShowMessage = onShowMessage,
                onOpenMachineHistory = navigator::navigateToMachineHistory
            )
        }

        composable(LoggedRoutes.MACHINE_HISTORY) {
            val slot = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<PaymentMachineHistoryArgs>(LoggedNavigator.TARIFF_SLOT_KEY)
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

    fun navigateToMachineHistory(slot: PaymentMachineHistoryArgs) {
        navController.currentBackStackEntry?.savedStateHandle?.set(TARIFF_SLOT_KEY, slot)
        navigateTo(LoggedRoutes.MACHINE_HISTORY)
    }

    fun navigateBack() {
        navController.popBackStack()
    }

    // Volta direto para Configuracoes, descartando a lista de maquininhas e o formulario.
    fun navigateBackToConfig() {
        if (!navController.popBackStack(LoggedRoutes.CONFIG, inclusive = false)) {
            navController.popBackStack()
        }
    }

    companion object {
        const val PRODUCT_TO_EDIT_KEY = "product_to_edit"
        const val PURCHASE_TO_EDIT_KEY = "purchase_to_edit"
        const val TERMINAL_TO_EDIT_KEY = "terminal_to_edit"
        const val TARIFF_SLOT_KEY = "tariff_slot_key"
    }
}
