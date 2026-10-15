package com.cabral.lucrovarejo.ui.screens.config

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
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
class AboutItemUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun displaysAboutTitleAndVersionAndForwardsClick() {
        var clickCount = 0
        composeRule.setContent {
            LucroVarejoTheme(themeMode = ThemeMode.LIGHT) {
                AboutItem(version = "1.2.3", onClick = { clickCount++ })
            }
        }

        composeRule.onNodeWithText("Sobre o app").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("Versão 1.2.3").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals(1, clickCount)
        }
    }
}
