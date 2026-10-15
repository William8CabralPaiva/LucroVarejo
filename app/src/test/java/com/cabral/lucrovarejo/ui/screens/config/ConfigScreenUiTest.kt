package com.cabral.lucrovarejo.ui.screens.config

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.cabral.lucrovarejo.BuildConfig
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
class ConfigScreenUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun darkModeSwitchChangesThemeAndLogoutActionIsForwarded() {
        var themeMode = ThemeMode.SYSTEM
        var logoutCount = 0
        composeRule.setContent {
            var currentMode by remember { mutableStateOf(themeMode) }
            LucroVarejoTheme(themeMode = currentMode) {
                ConfigScreen(
                    currentThemeMode = currentMode,
                    onThemeModeChanged = {
                        currentMode = it
                        themeMode = it
                    },
                    onLogout = { logoutCount++ }
                )
            }
        }

        composeRule.onNodeWithContentDescription("Modo escuro").performClick()
        composeRule.runOnIdle {
            assertEquals(ThemeMode.DARK, themeMode)
        }

        composeRule.onNodeWithContentDescription("Modo escuro").performClick()
        composeRule.runOnIdle {
            assertEquals(ThemeMode.LIGHT, themeMode)
        }

        composeRule.onNodeWithText("Desconectar").performClick()
        composeRule.runOnIdle {
            assertEquals(1, logoutCount)
        }
    }

    @Test
    fun aboutItemDisplaysVersionAndOpensAboutDialog() {
        composeRule.setContent {
            LucroVarejoTheme {
                ConfigScreen(
                    currentThemeMode = ThemeMode.LIGHT,
                    onThemeModeChanged = {},
                    onLogout = {}
                )
            }
        }

        val versionText = "Versão ${BuildConfig.VERSION_NAME}"
        composeRule.onNodeWithText("Sobre o app").assertIsDisplayed().performClick()
        composeRule.onAllNodesWithText(versionText).assertCountEquals(2)
        composeRule.onNodeWithText("Fechar").performClick()
        composeRule.onAllNodesWithText(versionText).assertCountEquals(1)
        composeRule.onAllNodesWithText("Sobre o app").assertCountEquals(1)
    }
}
