package com.cabral.lucrovarejo.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.cabral.lucrovarejo.domain.purchase.MonthlyPurchaseSummary
import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.domain.purchase.PurchaseRepository
import com.cabral.lucrovarejo.domain.purchase.SalesCursor
import com.cabral.lucrovarejo.domain.purchase.SalesFilter
import com.cabral.lucrovarejo.domain.purchase.SalesPage
import com.cabral.lucrovarejo.domain.purchase.SalesRepository
import com.cabral.lucrovarejo.domain.purchase.usecase.GetSalesPageUseCase
import com.cabral.lucrovarejo.domain.purchase.usecase.ObserveSalesChangesUseCase
import com.cabral.lucrovarejo.ui.screens.sales.SalesViewModel
import com.cabral.lucrovarejo.domain.purchase.usecase.GetLatestPurchasesUseCase
import com.cabral.lucrovarejo.domain.purchase.usecase.GetMonthlyPurchaseSummaryUseCase
import com.cabral.lucrovarejo.domain.product.ProductModel
import com.cabral.lucrovarejo.domain.product.ProductRepository
import com.cabral.lucrovarejo.domain.product.usecase.GetProductsUseCase
import com.cabral.lucrovarejo.ui.screens.home.HomeScreen
import com.cabral.lucrovarejo.ui.screens.home.HomeViewModel
import com.cabral.lucrovarejo.ui.screens.product.ProductViewModel
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import java.util.Date
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppNavGraphUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun bottomNavigationOpensEachLoggedDestination() {
        composeRule.setContent {
            LucroVarejoTheme {
                AppNavGraph(
                    startDestination = LoggedRoutes.HOME,
                    salesViewModel = SalesViewModel(
                        GetSalesPageUseCase(EmptySalesRepository()),
                        ObserveSalesChangesUseCase(EmptySalesRepository())
                    ),
                    productViewModel = ProductViewModel(GetProductsUseCase(EmptyProductRepository())),
                    homeViewModel = HomeViewModel(
                        GetMonthlyPurchaseSummaryUseCase(FakePurchaseRepository()),
                        GetLatestPurchasesUseCase(FakePurchaseRepository())
                    )
                )
            }
        }

        composeRule.onNodeWithText("Vendas do mês").assertIsDisplayed()

        composeRule.onNodeWithText("Vendas").performClick()
        composeRule.onNodeWithText("Buscar produto...").assertIsDisplayed()

        composeRule.onNodeWithText("Produtos").performClick()
        composeRule.onNodeWithText("Nenhum produto encontrado.").assertIsDisplayed()

        composeRule.onNodeWithText("Configurações").performClick()
        composeRule.onNodeWithText("Modo escuro").assertIsDisplayed()

        composeRule.onNodeWithText("Início").performClick()
        composeRule.onNodeWithText("Vendas do mês").assertIsDisplayed()
    }

    @Test
    fun showAllSalesRequestsSalesNavigation() {
        var salesNavigationRequested = false

        composeRule.setContent {
            LucroVarejoTheme {
                HomeScreen(
                    goToProductScreen = {},
                    goToSalesScreen = { salesNavigationRequested = true },
                    viewModel = HomeViewModel(
                        GetMonthlyPurchaseSummaryUseCase(FakePurchaseRepository()),
                        GetLatestPurchasesUseCase(FakePurchaseRepository())
                    )
                )
            }
        }

        composeRule
            .onNode(hasText("Ver todas").and(hasClickAction()))
            .performClick()

        composeRule.runOnIdle {
            assertTrue(salesNavigationRequested)
        }
    }

    private class EmptySalesRepository : SalesRepository {
        override suspend fun getSalesPage(filter: SalesFilter, cursor: SalesCursor?, pageSize: Int) =
            SalesPage(emptyList(), null)

        override fun observeSalesChanges(filter: SalesFilter) = flowOf(Unit)
    }

    private class EmptyProductRepository : ProductRepository {
    override suspend fun updateProduct(product: ProductModel): ProductModel = product
    override suspend fun deleteProduct(key: String) = Unit
        override suspend fun createProduct(product: ProductModel): ProductModel = product

        override fun observeProducts() = flowOf(emptyList<ProductModel>())
    }

    private class FakePurchaseRepository : PurchaseRepository {
    override suspend fun updatePurchase(purchase: PurchaseModel): PurchaseModel = purchase
    override suspend fun deletePurchase(key: String) = Unit
        override suspend fun createPurchase(purchase: PurchaseModel): PurchaseModel = purchase

        override fun observeMonthlySummary(
            start: Date,
            end: Date
        ): Flow<MonthlyPurchaseSummary> = flowOf(MonthlyPurchaseSummary())

        override fun observeLatestPurchases(limit: Int): Flow<List<PurchaseModel>> =
            flowOf(emptyList())
    }
}
