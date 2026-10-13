package com.cabral.lucrovarejo.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AreaDateSpinnerUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun categorySpinnerDisplaysAndReturnsSelectedCategory() {
        var selectedCategory = ""
        composeRule.setContent {
            LucroVarejoTheme {
                var category by remember { mutableStateOf<String?>(null) }
                SpinnerContent(
                    selectedCategory = category,
                    categories = listOf("Bolsas", "Mochilas"),
                    onCategorySelected = {
                        category = it
                        selectedCategory = it
                    }
                )
            }
        }

        composeRule.onNodeWithText("(opcional)").assertIsDisplayed()
        composeRule.onNodeWithText("Selecionar categoria").performClick()
        composeRule.onNodeWithText("Mochilas").performClick()

        composeRule.runOnIdle {
            assertEquals("Mochilas", selectedCategory)
        }
    }

    @Test
    fun saleDateDisplaysBrazilianDateFormatAndRequiredMarker() {
        val dateMillis = Instant.parse("2026-10-06T00:00:00Z").toEpochMilli()
        composeRule.setContent {
            LucroVarejoTheme {
                DateTextFieldContent(
                    dateMillis = dateMillis,
                    onDateChange = {}
                )
            }
        }

        composeRule.onNodeWithText("Data da venda").assertIsDisplayed()
        composeRule.onNodeWithText("*").assertIsDisplayed()
        composeRule.onNodeWithText("06/10/2026").assertIsDisplayed()
        assertEquals("06/10/2026", formatSaleDatePtBr(dateMillis))
    }

    @Test
    fun descriptionFieldSupportsMultilineText() {
        var description = ""
        composeRule.setContent {
            LucroVarejoTheme {
                var value by remember { mutableStateOf(description) }
                AreaTextFieldContent(
                    value = value,
                    onValueChange = {
                        value = it
                        description = it
                    }
                )
            }
        }

        composeRule.onNodeWithText("(opcional)").assertIsDisplayed()
        composeRule.onNodeWithText("Adicione detalhes sobre o produto...").assertIsDisplayed()
        composeRule.onNode(hasSetTextAction()).performTextInput("Detalhes do produto")
        composeRule.runOnIdle {
            assertEquals("Detalhes do produto", description)
        }
        composeRule.onAllNodesWithText("Adicione detalhes sobre o produto...").assertCountEquals(0)
    }
}
