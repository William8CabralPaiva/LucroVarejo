package com.cabral.lucrovarejo.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PurchaseItemInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()
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