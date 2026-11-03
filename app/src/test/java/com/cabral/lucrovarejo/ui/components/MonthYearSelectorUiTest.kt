package com.cabral.lucrovarejo.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
class MonthYearSelectorUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun previousAndNextChangeMonthAcrossYearBoundary() {
        var selectedYear = 2026
        var selectedMonth = 1
        composeRule.setContent {
            var year by remember { mutableStateOf(selectedYear) }
            var month by remember { mutableStateOf(selectedMonth) }
            LucroVarejoTheme(themeMode = ThemeMode.DARK) {
                MonthYearSelector(
                    year = year,
                    month = month,
                    onMonthYearChange = { newYear, newMonth ->
                        selectedYear = newYear
                        selectedMonth = newMonth
                        year = newYear
                        month = newMonth
                    }
                )
            }
        }

        composeRule.onNodeWithText("Janeiro 2026").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Mês anterior").performClick()
        composeRule.runOnIdle {
            assertEquals(2025, selectedYear)
            assertEquals(12, selectedMonth)
        }
        composeRule.onNodeWithText("Dezembro 2025").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Próximo mês").performClick()
        composeRule.runOnIdle {
            assertEquals(2026, selectedYear)
            assertEquals(1, selectedMonth)
        }
        composeRule.onNodeWithText("Janeiro 2026").assertIsDisplayed()
    }
}
