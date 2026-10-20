package com.cabral.lucrovarejo.ui.screens.product

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.cabral.lucrovarejo.domain.product.ProductModel
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import java.math.BigDecimal
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ProductScreenUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun displaysProductCodeCostAndAddAction() {
        var addProductRequested = false
        composeRule.setContent {
            LucroVarejoTheme {
                ProductContent(
                    state = ProductUiState(
                        isLoading = false,
                        items = listOf(
                            ProductModel(
                                id = "1",
                                cod = "7891000123456",
                                name = "Bolsa Feminina",
                                cost = BigDecimal("89.90"),
                                description = ""
                            )
                        )
                    ),
                    onQueryChange = {},
                    onAddProduct = { addProductRequested = true }
                )
            }
        }

        composeRule.onNodeWithText("Bolsa Feminina").assertIsDisplayed()
        composeRule.onNodeWithText("Código: 7891000123456").assertIsDisplayed()
        composeRule.onNodeWithText("R$ 89,90").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Novo produto").performClick()

        composeRule.runOnIdle {
            assertTrue(addProductRequested)
        }
    }
}
