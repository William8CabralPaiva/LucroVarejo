package com.cabral.lucrovarejo.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import com.cabral.lucrovarejo.domain.purchase.MonthlyPurchaseSummary
import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.ui.screens.home.HomeContent
import com.cabral.lucrovarejo.ui.screens.home.HomeUiState
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class HomeScreenUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun displaysMonthlySalesCostAndProfitInExistingAlertComponents() {
        composeRule.setContent {
            LucroVarejoTheme {
                HomeContent(
                    state = HomeUiState(
                        year = 2026,
                        month = 10,
                        summary = MonthlyPurchaseSummary(
                            totalSales = 845_000,
                            totalCost = 520_000,
                            totalProfit = 325_000,
                            productsSold = 32
                        ),
                        isLoading = false
                    ),
                    onMonthYearChange = { _, _ -> },
                    goToConfigScreen = {},
                    goToProductScreen = {},
                    onLogout = {}
                )
            }
        }

        composeRule.onNodeWithText("Vendas do mês").assertIsDisplayed()
        composeRule.onNodeWithText("R$ 8.450,00").assertIsDisplayed()
        composeRule.onNodeWithText("32 produtos vendidos").assertIsDisplayed()
        composeRule.onNodeWithText("Custo desta compra").assertIsDisplayed()
        composeRule.onNodeWithText("R$ 5.200,00").assertIsDisplayed()
        composeRule.onNodeWithText("Lucro desta venda").assertIsDisplayed()
        composeRule.onNodeWithText("R$ 3.250,00").assertIsDisplayed()
    }

    @Test
    fun selectingAnotherMonthForwardsMonthAndYear() {
        var selectedMonthYear: Pair<Int, Int>? = null
        composeRule.setContent {
            LucroVarejoTheme {
                HomeContent(
                    state = HomeUiState(
                        year = 2026,
                        month = 1,
                        isLoading = false
                    ),
                    onMonthYearChange = { year, month ->
                        selectedMonthYear = year to month
                    },
                    goToConfigScreen = {},
                    goToProductScreen = {},
                    onLogout = {}
                )
            }
        }

        composeRule.onNodeWithContentDescription("Mês anterior").performClick()
        composeRule.runOnIdle {
            assertEquals(2025 to 12, selectedMonthYear)
        }
    }

    @Test
    fun displaysLatestSalesAndOpensSalesList() {
        var openedSales = false
        composeRule.setContent {
            LucroVarejoTheme {
                HomeContent(
                    state = HomeUiState(year = 2026, month = 10, isLoading = false),
                    latestPurchases = listOf(
                        PurchaseModel(
                            cod = "A-1",
                            name = "Bolsa Feminina",
                            cost = 5000,
                            description = "",
                            salePrice = 8990,
                            quantity = 1,
                            saleDate = Date(),
                            paymentMethod = PaymentMethods.CASH
                        )
                    ),
                    onMonthYearChange = { _, _ -> },
                    goToConfigScreen = {},
                    goToProductScreen = {},
                    goToSalesScreen = { openedSales = true },
                    onLogout = {}
                )
            }
        }

        composeRule.onNodeWithText("Últimas vendas").assertIsDisplayed()
        composeRule.onNodeWithText("Bolsa Feminina")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("R$ 89,90").assertIsDisplayed()
        composeRule.onNodeWithText("Ver todas")
            .performScrollTo()
            .performClick()
        composeRule.runOnIdle {
            assertEquals(true, openedSales)
        }
    }
}
