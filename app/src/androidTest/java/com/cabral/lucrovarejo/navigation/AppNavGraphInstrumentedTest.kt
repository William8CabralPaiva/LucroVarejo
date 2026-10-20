package com.cabral.lucrovarejo.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cabral.lucrovarejo.R
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
import com.cabral.lucrovarejo.ui.screens.home.HomeViewModel
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import java.util.Date
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppNavGraphInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun bottomNavigationMovesBetweenLoggedScreens() {
        composeRule.setContent {
            LucroVarejoTheme {
                AppNavGraph(
                    startDestination = LoggedRoutes.HOME,
                    salesViewModel = SalesViewModel(
                        GetSalesPageUseCase(EmptySalesRepository()),
                        ObserveSalesChangesUseCase(EmptySalesRepository())
                    ),
                    homeViewModel = HomeViewModel(
                        getMonthlySummary = GetMonthlyPurchaseSummaryUseCase(FakePurchaseRepository()),
                        getLatestPurchases = GetLatestPurchasesUseCase(FakePurchaseRepository())
                    )
                )
            }
        }

        composeRule.onNodeWithContentDescription(
            label(R.string.month_year_calendar_description)
        ).assertIsDisplayed()
        composeRule.onNodeWithText(label(R.string.bottom_nav_sales)).performClick()
        composeRule.onNodeWithText("Buscar produto...").assertIsDisplayed()
        composeRule.onNodeWithText(label(R.string.bottom_nav_config)).performClick()
        composeRule.onNodeWithText("Modo escuro").assertIsDisplayed()
        composeRule.onNodeWithText(label(R.string.bottom_nav_home)).performClick()
        composeRule.onNodeWithContentDescription(
            label(R.string.month_year_calendar_description)
        ).assertIsDisplayed()
    }

    private fun label(resourceId: Int) = composeRule.activity.getString(resourceId)

    private class EmptySalesRepository : SalesRepository {
        override suspend fun getSalesPage(filter: SalesFilter, cursor: SalesCursor?, pageSize: Int) =
            SalesPage(emptyList(), null)

        override fun observeSalesChanges(filter: SalesFilter) = flowOf(Unit)
    }

    private class FakePurchaseRepository : PurchaseRepository {
        override suspend fun createPurchase(purchase: PurchaseModel): PurchaseModel = purchase

        override fun observeMonthlySummary(
            start: Date,
            end: Date
        ): Flow<MonthlyPurchaseSummary> = flowOf(MonthlyPurchaseSummary())

        override fun observeLatestPurchases(limit: Int): Flow<List<PurchaseModel>> =
            flowOf(emptyList())
    }
}
