package com.cabral.lucrovarejo.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuantityFieldInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun quantityButtonsUpdateDisplayedValue() {
        var reportedQuantity = 1
        composeRule.setContent {
            LucroVarejoTheme {
                var quantity by remember { mutableIntStateOf(1) }
                QuantityFieldContent(
                    value = quantity,
                    onValueChange = {
                        quantity = it
                        reportedQuantity = it
                    }
                )
            }
        }

        composeRule.onNodeWithText("Quantidade").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Aumentar quantidade").performClick()

        composeRule.onNodeWithText("2").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals(2, reportedQuantity)
        }
    }
}
