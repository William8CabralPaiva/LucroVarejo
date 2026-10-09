package com.cabral.lucrovarejo.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AlertPurchaseUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun alertPurchaseRendersItsMonthlySalesSummary() {
        composeRule.setContent {
            LucroVarejoTheme {
                AlertPurchase(price = 50.90, productsSize = 10)
            }
        }

        composeRule.onNodeWithTag(ALERT_PURCHASE_TAG).assertIsDisplayed()
        composeRule.onNodeWithText("Vendas do mês").assertIsDisplayed()
        composeRule.onNodeWithText("R$ 50,90").assertIsDisplayed()
        composeRule.onNodeWithText("10 produtos vendidos").assertIsDisplayed()
    }
}
