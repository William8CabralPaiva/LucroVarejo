package com.cabral.lucrovarejo.ui.screens.config

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
class AboutItemInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun aboutItemDisplaysVersionAndDispatchesClick() {
        var clickCount = 0
        composeRule.setContent {
            LucroVarejoTheme(themeMode = ThemeMode.DARK) {
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
