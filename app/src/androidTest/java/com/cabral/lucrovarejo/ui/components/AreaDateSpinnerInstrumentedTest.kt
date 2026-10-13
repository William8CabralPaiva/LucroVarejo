package com.cabral.lucrovarejo.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class AreaDateSpinnerInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun categoryCanBeSelectedFromDropdown() {
        var selectedCategory = ""
        composeRule.setContent {
            LucroVarejoTheme {
                var category by remember { mutableStateOf<String?>(null) }
                SpinnerContent(
                    selectedCategory = category,
                    categories = listOf("Bolsas", "Acessórios"),
                    onCategorySelected = {
                        category = it
                        selectedCategory = it
                    }
                )
            }
        }

        composeRule.onNodeWithText("Selecionar categoria").performClick()
        composeRule.onNodeWithText("Acessórios").performClick()
        composeRule.onNodeWithText("Acessórios").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals("Acessórios", selectedCategory)
        }
    }

    @Test
    fun saleDateShowsBrazilianFormatAndDescriptionLabel() {
        val dateMillis = Instant.parse("2026-10-06T00:00:00Z").toEpochMilli()
        composeRule.setContent {
            LucroVarejoTheme {
                DateTextFieldContent(dateMillis = dateMillis, onDateChange = {})
            }
        }

        composeRule.onNodeWithText("Data da venda").assertIsDisplayed()
        composeRule.onNodeWithText("06/10/2026").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals("06/10/2026", formatSaleDatePtBr(dateMillis))
        }
    }
}
