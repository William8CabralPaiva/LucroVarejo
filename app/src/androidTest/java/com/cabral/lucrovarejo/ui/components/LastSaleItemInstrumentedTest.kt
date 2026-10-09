package com.cabral.lucrovarejo.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import com.google.firebase.Timestamp
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

@RunWith(AndroidJUnit4::class)
class LastSaleItemInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun displaysTodaySaleAndHandlesTap() {
        val wasClicked = booleanArrayOf(false)
        val saleTimestamp = Timestamp.now()

        composeRule.setContent {
            LucroVarejoTheme {
                LastSaleItem(
                    productName = "Bolsa Feminina",
                    priceInCents = 8990,
                    timestamp = saleTimestamp,
                    onClick = { wasClicked[0] = true }
                )
            }
        }

        composeRule.onNodeWithText("Bolsa Feminina").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("R$ 89,90").assertIsDisplayed()
        composeRule.onNodeWithText("Hoje").assertIsDisplayed()
        composeRule.runOnIdle { assertTrue(wasClicked[0]) }
    }

    @Test
    fun displaysYesterdayForSaleFromPreviousLocalDay() {
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }.time

        composeRule.setContent {
            LucroVarejoTheme {
                LastSaleItem(
                    productName = "Mochila Escolar",
                    priceInCents = 12990,
                    timestamp = Timestamp(yesterday),
                    onClick = {}
                )
            }
        }

        composeRule.onNodeWithText("Ontem").assertIsDisplayed()
    }
}
