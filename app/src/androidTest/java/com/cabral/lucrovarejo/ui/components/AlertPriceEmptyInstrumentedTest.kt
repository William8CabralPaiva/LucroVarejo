package com.cabral.lucrovarejo.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlertPriceEmptyInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun profitAlertDisplaysEmptyStateWhenPriceIsZero() {
        composeRule.setContent {
            LucroVarejoTheme {
                AlertPrice(price = 0.0, state = AlertPriceState.PROFIT)
            }
        }

        composeRule.onNodeWithText("Lucro desta venda").assertIsDisplayed()
        composeRule.onNodeWithText("R$ 0,00").assertIsDisplayed()
        composeRule.onNodeWithText("Nenhuma venda registrada").assertIsDisplayed()
    }

    @Test
    fun purchaseAlertDisplaysEmptyStateWhenPriceIsZero() {
        composeRule.setContent {
            LucroVarejoTheme {
                AlertPrice(price = 0.0, state = AlertPriceState.PURCHASE)
            }
        }

        composeRule.onNodeWithText("Custo desta compra").assertIsDisplayed()
        composeRule.onNodeWithText("R$ 0,00").assertIsDisplayed()
        composeRule.onNodeWithText("Nenhuma venda registrada").assertIsDisplayed()
    }
}
