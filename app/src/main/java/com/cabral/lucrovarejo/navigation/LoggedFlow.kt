package com.cabral.lucrovarejo.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.product.ProductModel
import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.domain.payment.PaymentTerminalModel
import com.cabral.lucrovarejo.domain.store.StoreSummary
import com.cabral.lucrovarejo.ui.components.COIN_SWAP_MS
import com.cabral.lucrovarejo.ui.components.CoinTransition
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
    storeNameRequired: Boolean = false,
) {
    val navController = rememberNavController()
    val navigator = remember(navController) {
        LoggedNavigator(navController)
    }

    val currentRoute = navController.currentBackStackEntryAsState()
        .value
        ?.destination
        ?.route
        ?: LoggedRoutes.HOME

    val isDark = LocalIsDarkTheme.current
    val scaffoldContainerColor =
        if (isDark) ForestGreenHeader else MaterialTheme.colorScheme.primary

    var machineTourActive by remember { mutableStateOf(false) }
    var showSwitchStoreDialog by remember { mutableStateOf(false) }
    var showCreateStoreDialog by remember { mutableStateOf(false) }

    // Sair do formulario por outro caminho (ex.: seta de voltar) encerra o tour.
    LaunchedEffect(currentRoute) {
        if (currentRoute != LoggedRoutes.ADD_MACHINE) {
            machineTourActive = false
        }
    }

    val startMachineTour = {
        machineTourActive = true
        navigator.navigateTo(LoggedRoutes.ADD_MACHINE)
    }

    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val showMessage: (String) -> Unit = { message ->
        scope.launch {
            snackBarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
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
                onSwitchStore = {
                    showSwitchStoreDialog = true
                },
                onBackPress = navigator::navigateBack
            )
        },
        bottomBar = {}
    ) { innerPadding ->
        // A barra é overlay: o espaço dela é reservado por tela (só nas abas), então a tela que
        // está saindo na animação não muda de tamanho.
        val density = LocalDensity.current
        var barHeight by remember { mutableStateOf(0.dp) }

        val tabBottomPadding = (
                barHeight - WindowInsets.navigationBars
                    .asPaddingValues()
                    .calculateBottomPadding()
                ).coerceAtLeast(0.dp)

        Box(modifier = Modifier.fillMaxSize()) {

            // Faixa verde atrás dos cantos arredondados.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = innerPadding.calculateTopPadding())
                    .height(dimensionResource(R.dimen.spacing_20dp))
                    .background(scaffoldContainerColor)
            )

            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .imePadding(),
                shape = RoundedCornerShape(
                    topStart = dimensionResource(R.dimen.radius_20dp),
                    topEnd = dimensionResource(R.dimen.radius_20dp)
                ),
                color = MaterialTheme.colorScheme.background,
                shadowElevation = dimensionResource(R.dimen.spacing_4dp)
            ) {
                LoggedDestinationNavHost(
                    navController = navController,
                    navigator = navigator,
                    tabBottomPadding = tabBottomPadding,
                    currentThemeMode = currentThemeMode,
                    onThemeModeChange = onThemeModeChange,
                    onLogout = onLogout,
                    onShowMessage = showMessage,
                    homeViewModel = homeViewModel,
                    salesViewModel = salesViewModel,
                    productViewModel = productViewModel,
                    storeName = storeName,
                    onOpenSwitchStore = {
                        showSwitchStoreDialog = true
                    },
                    onShowTips = startMachineTour,
                    machineTourActive = machineTourActive,
                    onMachineTourFinish = {
                        machineTourActive = false
                        navigator.navigateBack()
                    },
                    onUpdateStoreName = onUpdateStoreName
                )
            }

            // Animação exclusiva do cadastro de venda.
            CoinTransition(
                trigger = currentRoute,
                enabled = currentRoute == LoggedRoutes.ADD_PURCHASE
            )

            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = navigator::navigateToTab,
                    modifier = Modifier.onSizeChanged { size ->
                        if (size.height > 0) {
                            barHeight = with(density) {
                                size.height.toDp()
                            }
                        }
                    }
                )
            }
        }
    }

    if (storeNameRequired) {
        // Obrigatorio: sem nome da loja nao da para usar o app (ex.: cadastro Google interrompido).
        StoreNameDialog(
            initialName = "",
            title = R.string.google_store_name_title,
            errorMessage = R.string.settings_store_name_error,
            onSave = onUpdateStoreName,
            onDismiss = {},
            cancelable = false
        )
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
            onDismiss = {
                showSwitchStoreDialog = false
            }
        )
    }

    if (showCreateStoreDialog) {
        StoreNameDialog(
            initialName = "",
            title = R.string.settings_create_store_dialog_title,
            errorMessage = R.string.settings_create_store_error,
            onSave = onCreateStore,
            onDismiss = {
                showCreateStoreDialog = false
            }
        )
    }

    if (showTips) {
        AlertDialog(
            onDismissRequest = onDismissTips,
            title = {
                Text(stringResource(R.string.coach_tour_prompt_title))
            },
            text = {
                Text(stringResource(R.string.coach_tour_prompt_text))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDismissTips()
                        startMachineTour()
                    }
                ) {
                    Text(stringResource(R.string.coach_tour_prompt_confirm))
                }
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
    tabBottomPadding: Dp,
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
    // O Home recebe novas vendas por Flow do Firestore.
    NavHost(
        navController = navController,
        startDestination = LoggedRoutes.HOME,
        modifier = Modifier.fillMaxSize(),

        enterTransition = {
            if (targetState.destination.route == LoggedRoutes.ADD_PURCHASE) {
                fadeIn(tween(1, delayMillis = COIN_SWAP_MS))
            } else {
                EnterTransition.None
            }
        },

        exitTransition = {
            if (targetState.destination.route == LoggedRoutes.ADD_PURCHASE) {
                fadeOut(tween(1, delayMillis = COIN_SWAP_MS))
            } else {
                ExitTransition.None
            }
        },

        popEnterTransition = {
            if (targetState.destination.route == LoggedRoutes.ADD_PURCHASE) {
                fadeIn(tween(1, delayMillis = COIN_SWAP_MS))
            } else {
                EnterTransition.None
            }
        },

        popExitTransition = {
            if (targetState.destination.route == LoggedRoutes.ADD_PURCHASE) {
                fadeOut(tween(1, delayMillis = COIN_SWAP_MS))
            } else {
                ExitTransition.None
            }
        }
    ) {

        composable(LoggedRoutes.HOME) {
            Box(Modifier.padding(bottom = tabBottomPadding)) {
                HomeScreen(
                    goToSalesScreen = {
                        navigator.navigateToTab(LoggedRoutes.SALES)
                    },
                    onEditPurchase = navigator::navigateToEditPurchase,
                    viewModel = homeViewModel
                )
            }
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
            Box(Modifier.padding(bottom = tabBottomPadding)) {
                ProductScreen(
                    onAddProduct = {
                        navigator.navigateTo(LoggedRoutes.ADD_PRODUCT)
                    },
                    onEditProduct = navigator::navigateToEditProduct,
                    viewModel = productViewModel
                )
            }
        }

        composable(LoggedRoutes.ADD_PRODUCT) {
            AddProductScreen()
        }

        composable(LoggedRoutes.SALES) {
            Box(Modifier.padding(bottom = tabBottomPadding)) {
                SalesScreen(
                    onAddPurchase = {
                        navigator.navigateTo(LoggedRoutes.ADD_PURCHASE)
                    },
                    onAddProduct = {
                        navigator.navigateTo(LoggedRoutes.ADD_PRODUCT)
                    },
                    onEditPurchase = navigator::navigateToEditPurchase,
                    viewModel = salesViewModel
                )
            }
        }

        composable(LoggedRoutes.EDIT_SALES) {
            AddPurchaseScreen(
                purchase = navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<PurchaseModel>(LoggedNavigator.PURCHASE_TO_EDIT_KEY),
                onFinish = navigator::navigateBack
            )
        }

        // Não descomentar.
        // composable(LoggedRoutes.SUMMARY) {
        //     SummaryScreen()
        // }

        composable(LoggedRoutes.CONFIG) {
            Box(Modifier.padding(bottom = tabBottomPadding)) {
                ConfigScreen(
                    currentThemeMode = currentThemeMode,
                    onThemeModeChange = onThemeModeChange,
                    onLogout = onLogout,
                    onNavigateToTerminals = {
                        navigator.navigateTo(LoggedRoutes.MACHINE_LIST)
                    },
                    storeName = storeName,
                    onOpenSwitchStore = onOpenSwitchStore,
                    onShowTips = onShowTips,
                    onUpdateStoreName = onUpdateStoreName
                )
            }
        }

        composable(LoggedRoutes.MACHINE_LIST) {
            MachineListScreen(
                onAddTerminal = {
                    navigator.navigateTo(LoggedRoutes.ADD_MACHINE)
                },
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
    // Abas da bottom navigation: não empilham uma sobre a outra.
    fun navigateToTab(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
        }
    }

    // Telas fora da bottom navigation.
    fun navigateTo(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
        }
    }

    fun navigateToEditProduct(product: ProductModel) {
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.set(PRODUCT_TO_EDIT_KEY, product)

        navigateTo(LoggedRoutes.EDIT_PRODUCT)
    }

    fun navigateToEditPurchase(purchase: PurchaseModel) {
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.set(PURCHASE_TO_EDIT_KEY, purchase)

        navigateTo(LoggedRoutes.EDIT_SALES)
    }

    fun navigateToEditTerminal(terminal: PaymentTerminalModel) {
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.set(TERMINAL_TO_EDIT_KEY, terminal)

        navigateTo(LoggedRoutes.EDIT_MACHINE)
    }

    fun navigateToMachineHistory(slot: PaymentMachineHistoryArgs) {
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.set(TARIFF_SLOT_KEY, slot)

        navigateTo(LoggedRoutes.MACHINE_HISTORY)
    }

    fun navigateBack() {
        navController.popBackStack()
    }

    // Volta diretamente para Configurações.
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