package com.cabral.lucrovarejo.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PurchaseItemUiTest {
    @get:Rule
    val composeRule = createComposeRule()
    @Test
    fun displaysNamePriceCostProfitAndQuantityAndHandlesTap() {
        val wasClicked = booleanArrayOf(false)
        composeRule.setContent {
            LucroVarejoTheme {
                PurchaseItem(
                    productName = "Bolsa Feminina",
                    salePriceInCents = 8990,
                    costInCents = 5000,
                    quantity = 1,
                    onClick = { wasClicked[0] = true }
                )
            }
        }

        composeRule.onNodeWithText("Bolsa Feminina").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("R$ 89,90").assertIsDisplayed()
        composeRule.onNodeWithText("Custo: R$ 50,00 \u2022 Lucro: R$ 39,90").assertIsDisplayed()
        composeRule.onNodeWithText("1 un.").assertIsDisplayed()
        composeRule.runOnIdle { assertTrue(wasClicked[0]) }
    }
}