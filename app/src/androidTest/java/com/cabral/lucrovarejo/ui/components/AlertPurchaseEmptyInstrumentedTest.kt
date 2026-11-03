package com.cabral.lucrovarejo.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlertPurchaseEmptyInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun displaysEmptyStateWhenThereAreNoProductsSold() {
        composeRule.setContent {
            LucroVarejoTheme {
                AlertPurchase(price = 0.0, productsSize = 0)
            }
        }

        composeRule.onNodeWithTag(ALERT_PURCHASE_TAG).assertIsDisplayed()
        composeRule.onNodeWithText("Vendas do mês").assertIsDisplayed()
        composeRule.onNodeWithText("R$ 0,00").assertIsDisplayed()
        composeRule.onNodeWithText("Nenhuma venda registrada").assertIsDisplayed()
    }
}
