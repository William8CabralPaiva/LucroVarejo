package com.cabral.lucrovarejo.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import com.cabral.lucrovarejo.ui.theme.ThemeMode
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MonthYearSelectorInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun displaysAndNavigatesMonthInDarkMode() {
        composeRule.setContent {
            var year by remember { mutableStateOf(2026) }
            var month by remember { mutableStateOf(10) }
            LucroVarejoTheme(themeMode = ThemeMode.DARK) {
                MonthYearSelector(
                    year = year,
                    month = month,
                    onMonthYearChange = { newYear, newMonth ->
                        year = newYear
                        month = newMonth
                    }
                )
            }
        }

        composeRule.onNodeWithText("Outubro 2026").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Próximo mês").performClick()
        composeRule.onNodeWithText("Novembro 2026").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Mês anterior").performClick()
        composeRule.onNodeWithText("Outubro 2026").assertIsDisplayed()
    }
}
