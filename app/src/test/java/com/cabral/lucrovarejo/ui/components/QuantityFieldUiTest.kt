package com.cabral.lucrovarejo.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.hasSetTextAction
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class QuantityFieldUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun buttonsChangeQuantityWithinAllowedRange() {
        var quantityAtCallback = 1
        composeRule.setContent {
            LucroVarejoTheme {
                var quantity by remember { mutableIntStateOf(1) }
                QuantityFieldContent(
                    value = quantity,
                    onValueChange = {
                        quantity = it
                        quantityAtCallback = it
                    }
                )
            }
        }

        composeRule.onNodeWithContentDescription("Diminuir quantidade")
            .assertIsNotEnabled()
        composeRule.onNodeWithContentDescription("Aumentar quantidade").performClick()
        composeRule.onNodeWithContentDescription("Aumentar quantidade").performClick()
        composeRule.onNodeWithContentDescription("Diminuir quantidade").performClick()

        composeRule.runOnIdle {
            assertEquals(2, quantityAtCallback)
        }
    }

    @Test
    fun quantityInputKeepsOnlyIntegersBetweenOneAndOneHundred() {
        var quantityAtCallback = 5
        composeRule.setContent {
            LucroVarejoTheme {
                var quantity by remember { mutableIntStateOf(5) }
                QuantityFieldContent(
                    value = quantity,
                    onValueChange = {
                        quantity = it
                        quantityAtCallback = it
                    }
                )
            }
        }

        composeRule.onNode(hasSetTextAction()).performTextReplacement("12a")
        composeRule.runOnIdle {
            assertEquals(12, quantityAtCallback)
        }

        composeRule.onNode(hasSetTextAction()).performTextReplacement("101")
        composeRule.runOnIdle {
            assertEquals(12, quantityAtCallback)
        }
    }

    @Test
    fun statefulQuantityFieldClampsInitialValueAndStopsAtMaximum() {
        composeRule.setContent {
            LucroVarejoTheme {
                QuantityField(
                    initialValue = 101
                )
            }
        }

        composeRule.onNodeWithText("100").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Aumentar quantidade")
            .assertIsNotEnabled()
    }
}
