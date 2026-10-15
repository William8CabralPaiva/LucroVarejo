package com.cabral.lucrovarejo.ui.screens.config

import androidx.activity.ComponentActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ModeNight
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import com.cabral.lucrovarejo.ui.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsItemInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun switchSettingsItemDisplaysContentAndDispatchesToggle() {
        var checkedValue = false
        composeRule.setContent {
            var checked by remember { mutableStateOf(checkedValue) }
            LucroVarejoTheme(themeMode = ThemeMode.DARK) {
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
    fun clickableSettingsItemDispatchesClick() {
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

        composeRule.onNodeWithText("Desconectar").performClick()
        composeRule.runOnIdle {
            assertEquals(1, clickCount)
        }
    }
}
