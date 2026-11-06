package com.cabral.lucrovarejo.ui.components

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppSwitchUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun clickTogglesCheckedState() {
        composeRule.setContent {
            LucroVarejoTheme {
                var checked by remember { mutableStateOf(false) }
                AppSwitch(checked = checked, onCheckedChange = { checked = it })
            }
        }

        composeRule.onNode(isToggleable()).assertIsOff().performClick().assertIsOn()
    }

    @Test
    fun disabledSwitchIsNotEnabled() {
        composeRule.setContent {
            LucroVarejoTheme {
                AppSwitch(checked = true, onCheckedChange = {}, enabled = false)
            }
        }

        composeRule.onNode(isToggleable()).assertIsNotEnabled().assertIsOn()
    }
}
