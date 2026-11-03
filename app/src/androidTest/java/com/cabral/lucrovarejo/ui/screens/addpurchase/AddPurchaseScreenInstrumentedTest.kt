package com.cabral.lucrovarejo.ui.screens.addpurchase

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cabral.lucrovarejo.domain.product.ProductModel
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import java.math.BigDecimal
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddPurchaseScreenInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsFullScreenLoadingUntilProductsAreLoaded() {
        composeRule.setContent {
            LucroVarejoTheme {
                AddPurchaseScreenContent(
                    state = AddPurchaseUiState(isLoadingProducts = true)
                )
            }
        }

        composeRule.onNodeWithText("Buscando produtos...").assertIsDisplayed()
        composeRule.onAllNodesWithText("Valor vendido").assertCountEquals(0)
    }

    @Test
    fun showsPurchaseFormAfterProductsAreLoaded() {
        val product = ProductModel(
            key = "product-1",
            cod = "A-1",
            name = "Bolsa Feminina",
            cost = BigDecimal("50.00"),
            description = "Bolsa"
        )
        composeRule.setContent {
            LucroVarejoTheme {
                AddPurchaseScreenContent(
                    state = AddPurchaseUiState(
                        products = listOf(product),
                        form = PurchaseFormState(selectedProduct = product),
                        isLoadingProducts = false
                    )
                )
            }
        }

        composeRule.onAllNodesWithText("Buscando produtos...").assertCountEquals(0)
        composeRule.onNodeWithText("Bolsa Feminina (A-1)").assertIsDisplayed()
        composeRule.onNodeWithText("Valor vendido").assertIsDisplayed()
    }

    @Test
    fun editModeHidesRegisterButtonAndShowsDeleteConfirmation() {
        val product = ProductModel(key = "p", cod = "A-1", name = "Bolsa Feminina", cost = BigDecimal("50.00"), description = "Bolsa")
        composeRule.setContent {
            LucroVarejoTheme {
                AddPurchaseScreenContent(
                    state = AddPurchaseUiState(
                        key = "purchase-key",
                        products = listOf(product),
                        form = PurchaseFormState(selectedProduct = product, salePrice = "89.90"),
                        isLoadingProducts = false,
                        showDeleteDialog = true
                    )
                )
            }
        }

        composeRule.onAllNodesWithText("REGISTRAR VENDA").assertCountEquals(0)
        composeRule.onNodeWithText("Deletar venda").assertIsDisplayed()
    }
}