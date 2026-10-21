package com.cabral.lucrovarejo.ui.screens.product

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cabral.lucrovarejo.domain.product.ProductModel
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import java.math.BigDecimal
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductScreenInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun displaysProductAndAddAction() {
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
        composeRule.onNodeWithContentDescription("Novo produto").performClick()

        composeRule.runOnIdle {
            assertTrue(addProductRequested)
        }
    }
}
