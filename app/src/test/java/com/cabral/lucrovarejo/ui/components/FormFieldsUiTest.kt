package com.cabral.lucrovarejo.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class FormFieldsUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun areaTextFieldShowsPlaceholderThenReportsTypedText() {
        var typed = ""
        composeRule.setContent {
            LucroVarejoTheme {
                AreaTextField(onValueChange = { typed = it })
            }
        }

        composeRule.onNodeWithText("Adicione detalhes sobre o produto...").assertIsDisplayed()
        composeRule.onNodeWithText("Adicione detalhes sobre o produto...").performTextInput("Azul")
        composeRule.runOnIdle { assertEquals("Azul", typed) }
    }

    @Test
    fun dateTextFieldShowsFormattedDateAndRequiredMarker() {
        composeRule.setContent {
            LucroVarejoTheme {
                DateTextFieldContent(dateMillis = 0L, onDateChange = {})
            }
        }

        composeRule.onNodeWithText("01/01/1970").assertIsDisplayed()
        composeRule.onNodeWithText("*").assertIsDisplayed()
    }

    @Test
    fun dateTextFieldShowsPlaceholderWhenDateIsNull() {
        composeRule.setContent {
            LucroVarejoTheme {
                DateTextFieldContent(dateMillis = null, onDateChange = {}, required = false)
            }
        }

        composeRule.onNodeWithText("dd/MM/aaaa").assertIsDisplayed()
        composeRule.onNodeWithText("(opcional)").assertIsDisplayed()
    }

    @Test
    fun dateTextFieldOpensPickerAndCancelClosesIt() {
        composeRule.setContent {
            LucroVarejoTheme {
                DateTextFieldContent(dateMillis = 0L, onDateChange = {})
            }
        }

        composeRule.onNodeWithText("01/01/1970").performClick()
        composeRule.onNodeWithText("OK").assertIsDisplayed()
        composeRule.onNodeWithText("Cancelar").performClick()
        composeRule.onNodeWithText("OK").assertDoesNotExist()
    }

    @Test
    fun loadingFullScreenShowsMessage() {
        composeRule.setContent {
            LucroVarejoTheme {
                LoadingFullScreen(loadingTextId = R.string.purchase_load_products_loading)
            }
        }

        composeRule.onNodeWithText("Buscando produtos...").assertIsDisplayed()
    }

    @Test
    fun prodCodPriceShowsNameCodeAndFormattedCost() {
        var clicks = 0
        composeRule.setContent {
            LucroVarejoTheme {
                ProdCodPriceContent(
                    name = "Bolsa",
                    code = "123",
                    formattedCost = "R$ 89,90",
                    onClick = { clicks++ }
                )
            }
        }

        composeRule.onNodeWithText("Código: 123").assertIsDisplayed()
        composeRule.onNodeWithText("R$ 89,90").assertIsDisplayed()
        composeRule.onNodeWithText("Bolsa").performClick()
        composeRule.runOnIdle { assertEquals(1, clicks) }
    }

    @Test
    fun prodCodPriceHidesCodeWhenEmpty() {
        composeRule.setContent {
            LucroVarejoTheme {
                ProdCodPrice(name = "Bolsa", code = "", cost = BigDecimal("10"))
            }
        }

        composeRule.onNodeWithText("Código: ", substring = true).assertDoesNotExist()
    }
}
