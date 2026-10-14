package com.cabral.lucrovarejo.ui.screens.addpurchase

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import com.cabral.lucrovarejo.domain.product.ProductModel
import com.cabral.lucrovarejo.ui.components.SAVE_PRODUCT_BUTTON_TAG
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AddPurchaseScreenUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val product = ProductModel(
        id = "product-1",
        cod = "A-1",
        name = "Bolsa Feminina",
        cost = BigDecimal("50.00"),
        description = "Bolsa"
    )

    @Test
    fun displaysFullScreenLoadingInsteadOfPurchaseForm() {
        composeRule.setContent {
            LucroVarejoTheme {
                AddPurchaseScreenContent(state = AddPurchaseUiState(isLoadingProducts = true))
            }
        }

        composeRule.onNodeWithText("Buscando produtos...").assertIsDisplayed()
        composeRule.onAllNodesWithText("Valor vendido").assertCountEquals(0)
        composeRule.onAllNodesWithText("REGISTRAR VENDA").assertCountEquals(0)
    }

    @Test
    fun displaysPurchaseFormAfterProductsLoad() {
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
        composeRule.onNodeWithText("Pagamento").assertIsDisplayed()
    }

    @Test
    fun invokesSaveWhenValidPurchaseFormIsSubmitted() {
        var saveCount = 0
        composeRule.setContent {
            LucroVarejoTheme {
                AddPurchaseScreenContent(
                    state = AddPurchaseUiState(
                        products = listOf(product),
                        form = PurchaseFormState(
                            selectedProduct = product,
                            salePrice = "89,90",
                            paymentMethod = PaymentMethods.PIX
                        ),
                        isLoadingProducts = false
                    ),
                    onSave = { saveCount++ }
                )
            }
        }

        composeRule.onNodeWithTag(SAVE_PRODUCT_BUTTON_TAG)
            .performScrollTo()
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, saveCount)
        }
    }
}
