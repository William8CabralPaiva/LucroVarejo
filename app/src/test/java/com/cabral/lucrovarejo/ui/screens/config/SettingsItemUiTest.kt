package com.cabral.lucrovarejo.ui.screens.config

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ModeNight
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import com.cabral.lucrovarejo.ui.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SettingsItemUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun displaysSwitchItemAndForwardsToggleChanges() {
        var checkedValue = false
        composeRule.setContent {
            var checked by remember { mutableStateOf(checkedValue) }
            LucroVarejoTheme(themeMode = ThemeMode.LIGHT) {
                SettingsItem(
                    icon = Icons.Outlined.ModeNight,
                    iconTint = Color.Green,
                    title = "Modo escuro",
                    subtitle = "Ativa o tema escuro",
                    checked = checked,
                    onCheckedChange = {
                        checked = it
                        checkedValue = it
                    }
                )
            }
        }

        composeRule.onNodeWithText("Modo escuro").assertIsDisplayed()
        composeRule.onNodeWithText("Ativa o tema escuro").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Modo escuro").performClick()

        composeRule.runOnIdle {
            assertEquals(true, checkedValue)
        }
    }

    @Test
    fun displaysClickableSettingsItemAndForwardsClick() {
        var clickCount = 0
        composeRule.setContent {
            LucroVarejoTheme {
                SettingsItem(
                    icon = Icons.Outlined.ModeNight,
                    iconTint = Color.Green,
                    title = "Desconectar",
                    subtitle = "Sair da conta",
                    onClick = { clickCount++ }
                )
            }
        }

        composeRule.onNodeWithText("Desconectar").assertIsDisplayed().performClick()
        composeRule.runOnIdle {
            assertEquals(1, clickCount)
        }
    }
}
