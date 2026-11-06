package com.cabral.lucrovarejo.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.onAllNodesWithText
import com.cabral.lucrovarejo.domain.auth.AuthRepository
import com.cabral.lucrovarejo.domain.auth.usecase.GetStoreDisplayNameUseCase
import com.cabral.lucrovarejo.domain.auth.usecase.UpdateStoreDisplayNameUseCase
import com.cabral.lucrovarejo.domain.product.ProductModel
import com.cabral.lucrovarejo.domain.product.ProductRepository
import com.cabral.lucrovarejo.domain.product.usecase.GetProductsUseCase
import com.cabral.lucrovarejo.domain.purchase.MonthlyPurchaseSummary
import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.domain.purchase.PurchaseRepository
import com.cabral.lucrovarejo.domain.purchase.SalesCursor
import com.cabral.lucrovarejo.domain.purchase.SalesFilter
import com.cabral.lucrovarejo.domain.purchase.SalesPage
import com.cabral.lucrovarejo.domain.purchase.SalesRepository
import com.cabral.lucrovarejo.domain.purchase.usecase.GetLatestPurchasesUseCase
import com.cabral.lucrovarejo.domain.purchase.usecase.GetMonthlyPurchaseSummaryUseCase
import com.cabral.lucrovarejo.domain.purchase.usecase.GetSalesPageUseCase
import com.cabral.lucrovarejo.domain.purchase.usecase.ObserveSalesChangesUseCase
import com.cabral.lucrovarejo.domain.store.StoreRepository
import com.cabral.lucrovarejo.domain.store.StoreSelection
import com.cabral.lucrovarejo.domain.store.StoreSummary
import com.cabral.lucrovarejo.domain.store.usecase.CreateStoreUseCase
import com.cabral.lucrovarejo.domain.store.usecase.GetStoreSelectionUseCase
import com.cabral.lucrovarejo.domain.store.usecase.SwitchStoreUseCase
import com.cabral.lucrovarejo.ui.screens.home.HomeViewModel
import com.cabral.lucrovarejo.ui.screens.product.ProductViewModel
import com.cabral.lucrovarejo.ui.screens.sales.SalesViewModel
import com.cabral.lucrovarejo.ui.store.StoreViewModel
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import java.util.Date
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppNavGraphStoreFlowUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val stores = FakeStores()
    private val auth = FakeAuth()
    private val storeViewModel = StoreViewModel(
        GetStoreDisplayNameUseCase(auth),
        UpdateStoreDisplayNameUseCase(auth),
        GetStoreSelectionUseCase(stores),
        CreateStoreUseCase(stores),
        SwitchStoreUseCase(stores)
    )

    private fun setGraph() {
        composeRule.setContent {
            LucroVarejoTheme {
                AppNavGraph(
                    startDestination = LoggedRoutes.HOME,
                    isUserAuthenticated = true,
                    storeViewModel = storeViewModel,
                    salesViewModel = SalesViewModel(
                        GetSalesPageUseCase(EmptySales()),
                        ObserveSalesChangesUseCase(EmptySales()),
                        GetProductsUseCase(EmptyProducts())
                    ),
                    productViewModel = ProductViewModel(GetProductsUseCase(EmptyProducts())),
                    homeViewModel = HomeViewModel(
                        GetMonthlyPurchaseSummaryUseCase(EmptyPurchases()),
                        GetLatestPurchasesUseCase(EmptyPurchases())
                    )
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun openSwitchDialog() {
        composeRule.onNodeWithText("Configurações").performClick()
        composeRule.onNodeWithText("Trocar loja").performScrollTo().performClick()
    }

    @Test
    fun loadsStoreNameIntoHomeTitle() {
        setGraph()

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Loja A").fetchSemanticsNodes().isNotEmpty()
        }
        assertEquals("a", storeViewModel.uiState.value.activeStoreId)
    }

    @Test
    fun tipsPromptCanBeDismissed() {
        setGraph()

        composeRule.runOnUiThread { storeViewModel.showTips() }
        composeRule.onNodeWithText("Aprenda a cadastrar o crédito").assertIsDisplayed()

        composeRule.onNodeWithText("Agora não").performClick()
        composeRule.waitForIdle()
        assertEquals(false, storeViewModel.uiState.value.showTips)
    }

    @Test
    fun switchingStoreUpdatesViewModelAndRecreatesFlow() {
        setGraph()
        composeRule.waitUntil(5_000) { storeViewModel.uiState.value.stores.size == 2 }

        openSwitchDialog()
        composeRule.onNodeWithText("Escolha a loja").assertIsDisplayed()
        composeRule.onNodeWithText("Loja B").performClick()
        composeRule.waitForIdle()

        assertEquals(listOf("b"), stores.selected)
        assertEquals(1, storeViewModel.uiState.value.storeEpoch)
    }

    @Test
    fun creatingStoreFromDialogCallsRepository() {
        setGraph()
        composeRule.waitUntil(5_000) { storeViewModel.uiState.value.stores.size == 2 }

        openSwitchDialog()
        composeRule.onNodeWithText("Criar nova loja").performClick()
        composeRule.onNode(hasText("Nome da loja") and hasSetTextAction()).performTextInput("Loja C")
        composeRule.onNodeWithText("Salvar").performClick()
        composeRule.waitUntil(5_000) { stores.created.isNotEmpty() }

        assertEquals(listOf("Loja C"), stores.created)
        assertTrue(storeViewModel.uiState.value.stores.any { it.displayName == "Loja C" })
    }

    private class FakeStores : StoreRepository {
        val selected = mutableListOf<String>()
        val created = mutableListOf<String>()

        override suspend fun getStoreSelection() = StoreSelection(
            listOf(StoreSummary("a", "Loja A"), StoreSummary("b", "Loja B")),
            "a"
        )

        override suspend fun createStore(displayName: String): StoreSummary {
            created += displayName
            return StoreSummary("c", displayName)
        }

        override fun selectStore(storeId: String) {
            selected += storeId
        }
    }

    private class FakeAuth(
        delegate: AuthRepository = mock(AuthRepository::class.java)
    ) : AuthRepository by delegate {
        override suspend fun getStoreDisplayName(): String? = "Loja A"
        override suspend fun updateStoreDisplayName(displayName: String) = Unit
    }

    private class EmptySales : SalesRepository {
        override suspend fun getSalesPage(filter: SalesFilter, cursor: SalesCursor?, pageSize: Int) =
            SalesPage(emptyList(), null)

        override fun observeSalesChanges(filter: SalesFilter) = flowOf(Unit)
    }

    private class EmptyProducts : ProductRepository {
        override suspend fun updateProduct(product: ProductModel): ProductModel = product
        override suspend fun deleteProduct(key: String) = Unit
        override suspend fun createProduct(product: ProductModel): ProductModel = product
        override fun observeProducts() = flowOf(emptyList<ProductModel>())
    }

    private class EmptyPurchases : PurchaseRepository {
        override suspend fun updatePurchase(purchase: PurchaseModel): PurchaseModel = purchase
        override suspend fun deletePurchase(key: String) = Unit
        override suspend fun createPurchase(purchase: PurchaseModel): PurchaseModel = purchase
        override fun observeMonthlySummary(start: Date, end: Date): Flow<MonthlyPurchaseSummary> =
            flowOf(MonthlyPurchaseSummary())

        override fun observeLatestPurchases(limit: Int): Flow<List<PurchaseModel>> = flowOf(emptyList())
    }
}
