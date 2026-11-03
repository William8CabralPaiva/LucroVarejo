package com.cabral.lucrovarejo.ui.screens.product

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.cabral.lucrovarejo.domain.product.ProductModel
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import java.math.BigDecimal
import org.junit.Assert.assertEquals
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
                                key = "1",
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

    @Test
    fun invokesEditProductWhenItemClicked() {
        var editProductRequested = false
        composeRule.setContent {
            LucroVarejoTheme {
                ProductContent(
                    state = ProductUiState(
                        isLoading = false,
                        items = listOf(
                            ProductModel(
                                key = "1",
                                cod = "7891000123456",
                                name = "Bolsa Feminina",
                                cost = BigDecimal("89.90"),
                                description = ""
                            )
                        )
                    ),
                    onQueryChange = {},
                    onAddProduct = {},
                    onEditProduct = { editProductRequested = true }
                )
            }
        }

        composeRule.onNodeWithText("Bolsa Feminina").performClick()

        composeRule.runOnIdle {
            assertTrue(editProductRequested)
        }
    }

    @Test
    fun clickingProductReportsItForEditing() {
        var edited: ProductModel? = null
        val product = ProductModel(
            key = "1",
            cod = "7891000123456",
            name = "Bolsa Feminina",
            cost = BigDecimal("89.90"),
            description = ""
        )
        composeRule.setContent {
            LucroVarejoTheme {
                ProductContent(
                    state = ProductUiState(isLoading = false, items = listOf(product)),
                    onQueryChange = {},
                    onAddProduct = {},
                    onEditProduct = { edited = it }
                )
            }
        }

        composeRule.onNodeWithText("Bolsa Feminina").performClick()

        composeRule.runOnIdle {
            assertEquals(product, edited)
        }
    }

    @Test
    fun displaysEmptyMessageWhenThereAreNoProducts() {
        composeRule.setContent {
            LucroVarejoTheme {
                ProductContent(
                    state = ProductUiState(isLoading = false),
                    onQueryChange = {},
                    onAddProduct = {}
                )
            }
        }

        composeRule.onNodeWithText("Nenhum produto encontrado.").assertIsDisplayed()
    }

    @Test
    fun displaysErrorMessageWhenLoadFails() {
        composeRule.setContent {
            LucroVarejoTheme {
                ProductContent(
                    state = ProductUiState(isLoading = false, hasError = true),
                    onQueryChange = {},
                    onAddProduct = {}
                )
            }
        }

        composeRule.onNodeWithText("Não foi possível carregar os produtos.").assertIsDisplayed()
    }

    @Test
    fun reportsSearchQueryChanges() {
        var query = ""
        composeRule.setContent {
            LucroVarejoTheme {
                ProductContent(
                    state = ProductUiState(isLoading = false),
                    onQueryChange = { query = it },
                    onAddProduct = {}
                )
            }
        }

        composeRule.onNodeWithText("Buscar produto...").performTextInput("bolsa")

        composeRule.runOnIdle {
            assertEquals("bolsa", query)
        }
    }
}