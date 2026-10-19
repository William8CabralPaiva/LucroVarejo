package com.cabral.lucrovarejo.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.cabral.lucrovarejo.domain.purchase.MonthlyPurchaseSummary
import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.domain.purchase.PurchaseRepository
import com.cabral.lucrovarejo.domain.purchase.usecase.GetLatestPurchasesUseCase
import com.cabral.lucrovarejo.domain.purchase.usecase.GetMonthlyPurchaseSummaryUseCase
import androidx.paging.PagingData
import com.cabral.lucrovarejo.ui.screens.home.HomeViewModel
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import java.util.Date
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
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
                    homeViewModel = HomeViewModel(
                        GetMonthlyPurchaseSummaryUseCase(FakePurchaseRepository()),
                        GetLatestPurchasesUseCase(FakePurchaseRepository())
                    )
                )
            }
        }

        composeRule.onNodeWithText("Vendas do mês").assertIsDisplayed()

        composeRule.onNodeWithText("Vendas").performClick()
        composeRule.onNodeWithText("Suas vendas aparecerão aqui.").assertIsDisplayed()

        composeRule.onNodeWithText("Resumo").performClick()
        composeRule.onNodeWithText("O resumo das vendas aparecerá aqui.").assertIsDisplayed()

        composeRule.onNodeWithText("Configurações").performClick()
        composeRule.onNodeWithText("Modo escuro").assertIsDisplayed()

        composeRule.onNodeWithText("Início").performClick()
        composeRule.onNodeWithText("Vendas do mês").assertIsDisplayed()
    }

    @Test
    fun showAllSalesNavigatesToSalesTab() {
        composeRule.setContent {
            LucroVarejoTheme {
                AppNavGraph(
                    startDestination = LoggedRoutes.HOME,
                    homeViewModel = HomeViewModel(
                        GetMonthlyPurchaseSummaryUseCase(FakePurchaseRepository()),
                        GetLatestPurchasesUseCase(FakePurchaseRepository())
                    )
                )
            }
        }

        composeRule.onNodeWithText("Ver todas")
            .performScrollTo()
            .performClick()

        composeRule.onNodeWithText("Suas vendas aparecerão aqui.").assertIsDisplayed()
    }

    private class FakePurchaseRepository : PurchaseRepository {
        override suspend fun createPurchase(purchase: PurchaseModel): PurchaseModel = purchase

        override suspend fun getMonthlySummary(
            start: Date,
            end: Date
        ): MonthlyPurchaseSummary = MonthlyPurchaseSummary()

        override fun getLatestPurchases(): Flow<PagingData<PurchaseModel>> =
            flowOf(PagingData.empty())
    }
}
