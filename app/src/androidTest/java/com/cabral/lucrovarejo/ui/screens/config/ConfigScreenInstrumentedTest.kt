package com.cabral.lucrovarejo.ui.screens.config

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
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
class ConfigScreenInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun darkModeCanBeEnabledAndAboutItemIsVisible() {
        var selectedTheme = ThemeMode.LIGHT
        composeRule.setContent {
            var currentTheme by remember { mutableStateOf(selectedTheme) }
            LucroVarejoTheme(themeMode = currentTheme) {
                ConfigScreen(
                    currentThemeMode = currentTheme,
                    onThemeModeChanged = {
                        selectedTheme = it
                        currentTheme = it
                    },
                    onLogout = {}
                )
            }
        }

        composeRule.onNodeWithContentDescription("Modo escuro")
            .assertIsDisplayed()
            .performClick()
            .assertIsOn()
        composeRule.onNodeWithText("Sobre o app").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals(ThemeMode.DARK, selectedTheme)
        }
    }
}
