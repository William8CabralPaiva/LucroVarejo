package com.cabral.lucrovarejo.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.hasSetTextAction
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class IconTextFieldUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun moneyFieldAllowsDigitsAndOneDecimalPoint() {
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

        composeRule.onNode(hasSetTextAction()).performTextInput("12a.3.4")

        composeRule.runOnIdle {
            assertEquals("12.34", currentValue)
        }
    }

    @Test
    fun textFieldAllowsArbitraryTextAndShowsOptionalMarker() {
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
                    labelResId = R.string.product_description_label,
                    placeholderResId = R.string.product_description_placeholder,
                    icon = Icons.Outlined.ShoppingBag,
                    type = IconTextFieldType.TEXT,
                    required = false
                )
            }
        }

        composeRule.onNodeWithText("Descrição").assertIsDisplayed()
        composeRule.onNodeWithText("(opcional)").assertIsDisplayed()
        composeRule.onNode(hasSetTextAction()).performTextInput("Texto livre 123 !")

        composeRule.runOnIdle {
            assertEquals("Texto livre 123 !", currentValue)
        }
    }

    @Test
    fun statefulMoneyFieldEmitsFilteredValue() {
        var currentValue = ""
        composeRule.setContent {
            LucroVarejoTheme {
                IconTextField(
                    labelResId = R.string.product_paid_value_label,
                    placeholderResId = R.string.product_paid_value_placeholder,
                    icon = Icons.Outlined.MonetizationOn,
                    type = IconTextFieldType.MONEY,
                    required = true,
                    onValueChange = { currentValue = it }
                )
            }
        }

        composeRule.onNode(hasSetTextAction()).performTextInput("1a2.3.4")

        composeRule.runOnIdle {
            assertEquals("12.34", currentValue)
        }
    }
}
