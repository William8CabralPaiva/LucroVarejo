package com.cabral.lucrovarejo.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AlertPriceUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun profitAlertDisplaysTitleAndFormattedPrice() {
        composeRule.setContent {
            LucroVarejoTheme {
                AlertPrice(price = 50.90, state = AlertPriceState.PROFIT)
            }
        }

        composeRule.onNodeWithText("Lucro desta venda").assertIsDisplayed()
        composeRule.onNodeWithText("R$ 50,90").assertIsDisplayed()
    }

    @Test
    fun purchaseAlertDisplaysTitleAndFormattedPrice() {
        composeRule.setContent {
            LucroVarejoTheme {
                AlertPrice(price = 50.90, state = AlertPriceState.PURCHASE)
            }
        }

        composeRule.onNodeWithText("Custo desta compra").assertIsDisplayed()
        composeRule.onNodeWithText("R$ 50,90").assertIsDisplayed()
    }
}
