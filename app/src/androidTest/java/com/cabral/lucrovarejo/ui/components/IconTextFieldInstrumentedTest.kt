package com.cabral.lucrovarejo.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IconTextFieldInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun moneyFieldFiltersInputAndDisplaysRequiredLabel() {
        var currentValue = ""
        composeRule.setContent {
            LucroVarejoTheme {
                var value by remember { mutableStateOf(currentValue) }
                IconTextFieldContent(
                    value = value,
                    onValueChange = {
                        value = it
                        currentValue = it
                    },
                    labelResId = R.string.product_paid_value_label,
                    placeholderResId = R.string.product_paid_value_placeholder,
                    icon = Icons.Outlined.MonetizationOn,
                    type = IconTextFieldType.MONEY,
                    required = true
                )
            }
        }

        composeRule.onNodeWithText("Valor pago").assertIsDisplayed()
        composeRule.onNodeWithText("*").assertIsDisplayed()
        composeRule.onNode(hasSetTextAction()).performTextInput("9x.8.7")

        composeRule.runOnIdle {
            assertEquals("9.87", currentValue)
        }
    }
}
