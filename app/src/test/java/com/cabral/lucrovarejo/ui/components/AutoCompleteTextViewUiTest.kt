package com.cabral.lucrovarejo.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AutoCompleteTextViewUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun filterIgnoresCaseAndReturnsAllWhenQueryIsBlank() {
        val items = listOf("Bolsa (A-1)", "Mochila (B-2)")

        assertEquals(items, filterAutoCompleteItems(items, "  "))
        assertEquals(listOf("Mochila (B-2)"), filterAutoCompleteItems(items, "moch"))
        assertEquals(emptyList<String>(), filterAutoCompleteItems(items, "xyz"))
    }

    @Test
    fun displaysLabelAndPlaceholder() {
        composeRule.setContent {
            LucroVarejoTheme {
                AutoCompleteTextViewContent(
                    selectedItem = null,
                    items = listOf("Bolsa"),
                    onItemSelected = {},
                    textId = R.string.product,
                    placeholderResId = R.string.product_placeholder
                )
            }
        }

        composeRule.onNodeWithText("Produto").assertIsDisplayed()
        composeRule.onNodeWithText("Selecionar Produto").assertIsDisplayed()
    }

    @Test
    fun typingFiltersSuggestionsAndSelectsItem() {
        var selectedItem = ""
        composeRule.setContent {
            LucroVarejoTheme {
                var current by remember { mutableStateOf<String?>(null) }
                AutoCompleteTextViewContent(
                    selectedItem = current,
                    items = listOf("Bolsa", "Mochila"),
                    onItemSelected = {
                        current = it
                        selectedItem = it
                    },
                    textId = R.string.product,
                    placeholderResId = R.string.product_placeholder
                )
            }
        }

        composeRule.onNodeWithTag(AUTO_COMPLETE_TEXT_FIELD_TAG).performTextInput("moch")
        composeRule.onAllNodesWithText("Bolsa").assertCountEquals(0)
        composeRule.onNodeWithText("Mochila").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag(AUTO_COMPLETE_TEXT_FIELD_TAG).performTextInput(" X")

        composeRule.runOnIdle {
            assertEquals("Mochila", selectedItem)
        }
        composeRule.onNodeWithTag(AUTO_COMPLETE_TEXT_FIELD_TAG).assertTextEquals("Mochila X")
    }
}
