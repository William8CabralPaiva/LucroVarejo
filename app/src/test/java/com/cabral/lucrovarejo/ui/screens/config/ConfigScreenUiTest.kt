package com.cabral.lucrovarejo.ui.screens.config

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
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
                    onThemeModeChange = {
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

        composeRule.onNodeWithText("Desconectar").performScrollTo().performClick()
        composeRule.runOnIdle {
            assertEquals(1, logoutCount)
        }
    }

    @Test
    fun storeNameItemOpensDialogAndSendsTrimmedName() {
        var savedName: String? = null
        composeRule.setContent {
            LucroVarejoTheme {
                ConfigScreen(
                    currentThemeMode = ThemeMode.LIGHT,
                    onThemeModeChange = {},
                    storeName = "Loja Antiga",
                    onUpdateStoreName = { name, onResult ->
                        savedName = name
                        onResult(true)
                    }
                )
            }
        }

        composeRule.onNodeWithText("Loja Antiga").performClick()
        composeRule.onNodeWithText("Alterar nome da loja").assertIsDisplayed()
        composeRule.onNode(hasSetTextAction()).performTextReplacement("Loja Nova")
        composeRule.onNodeWithText("Salvar").performClick()

        composeRule.runOnIdle { assertEquals("Loja Nova", savedName) }
        composeRule.onAllNodesWithText("Alterar nome da loja").assertCountEquals(0)
    }

    @Test
    fun tipsItemRequestsTour() {
        var tipsRequested = 0
        composeRule.setContent {
            LucroVarejoTheme {
                ConfigScreen(
                    currentThemeMode = ThemeMode.LIGHT,
                    onThemeModeChange = {},
                    onShowTips = { tipsRequested++ }
                )
            }
        }

        composeRule.onNodeWithText("Dicas").performScrollTo().performClick()

        composeRule.runOnIdle { assertEquals(1, tipsRequested) }
    }

    @Test
    fun aboutItemDisplaysVersionAndOpensAboutDialog() {
        composeRule.setContent {
            LucroVarejoTheme {
                ConfigScreen(
                    currentThemeMode = ThemeMode.LIGHT,
                    onThemeModeChange = {},
                    onLogout = {}
                )
            }
        }

        val versionText = "Versão ${BuildConfig.VERSION_NAME}"
        composeRule.onNodeWithText("Sobre o app").performScrollTo().assertIsDisplayed().performClick()
        composeRule.onAllNodesWithText(versionText).assertCountEquals(2)
        composeRule.onNodeWithText("Fechar").performClick()
        composeRule.onAllNodesWithText(versionText).assertCountEquals(1)
        composeRule.onAllNodesWithText("Sobre o app").assertCountEquals(1)
    }
}
