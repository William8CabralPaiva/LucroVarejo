package com.cabral.lucrovarejo.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.assertCountEquals
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ProductOptionUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun itemShowsNameCodeAndCost() {
        composeRule.setContent {
            LucroVarejoTheme {
                ProductOptionItem(name = "Guarda-chuva", code = "001", formattedCost = "R$ 34,00")
            }
        }

        composeRule.onNodeWithText("Guarda-chuva").assertIsDisplayed()
        composeRule.onNodeWithText("Código: 001  •  Custo: R$ 34,00").assertIsDisplayed()
    }

    @Test
    fun detailsOmitCodeWhenBlank() {
        composeRule.setContent {
            LucroVarejoTheme {
                ProductOptionDetails(code = " ", formattedCost = "R$ 34,00")
            }
        }

        composeRule.onNodeWithText("Custo: R$ 34,00").assertIsDisplayed()
        composeRule.onAllNodesWithText("Código", substring = true).assertCountEquals(0)
    }
}
