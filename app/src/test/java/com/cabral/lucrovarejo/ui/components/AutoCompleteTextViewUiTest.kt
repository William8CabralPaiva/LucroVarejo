package com.cabral.lucrovarejo.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
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
import kotlinx.collections.immutable.persistentListOf
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
        val items = persistentListOf("Bolsa (A-1)", "Mochila (B-2)")

        assertEquals(items, filterAutoCompleteItems(items, "  "))
        assertEquals(listOf("Mochila (B-2)"), filterAutoCompleteItems(items, "moch"))
        assertEquals(emptyList<String>(), filterAutoCompleteItems(items, "xyz"))
    }

    @Test
    fun displaysLabelAndPlaceholder() {
        composeRule.setContent {
            LucroVarejoTheme {
                AutoCompleteTextView(
                    selectedItem = null,
                    items = persistentListOf("Bolsa"),
                    onItemSelect = {},
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
                AutoCompleteTextView(
                    selectedItem = current,
                    items = persistentListOf("Bolsa", "Mochila"),
                    onItemSelect = {
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

    @Test
    fun customItemContentShowsNameCodeAndCost() {
        var selectedItem = ""
        composeRule.setContent {
            LucroVarejoTheme {
                AutoCompleteTextView(
                    selectedItem = null,
                    items = persistentListOf("Bolsa (A-1)"),
                    onItemSelect = { selectedItem = it },
                    textId = R.string.product,
                    placeholderResId = R.string.product_placeholder,
                    itemContent = {
                        Column {
                            Text("Bolsa")
                            Text("Código: A-1  •  Custo: R$ 10,00")
                        }
                    }
                )
            }
        }

        composeRule.onNodeWithTag(AUTO_COMPLETE_TEXT_FIELD_TAG).performTextInput("bol")
        composeRule.onNodeWithText("Código: A-1  •  Custo: R$ 10,00").assertIsDisplayed()
        composeRule.onNodeWithText("Bolsa").performClick()

        composeRule.runOnIdle {
            assertEquals("Bolsa (A-1)", selectedItem)
        }
        composeRule.onNodeWithTag(AUTO_COMPLETE_TEXT_FIELD_TAG).assertTextEquals("Bolsa (A-1)")
    }

    @Test
    fun itemsWithSameTextAreSelectedByKey() {
        var selectedKey = ""
        composeRule.setContent {
            LucroVarejoTheme {
                AutoCompleteTextView(
                    selectedItem = null,
                    items = persistentListOf("Guarda-chuva", "Guarda-chuva"),
                    onItemSelect = {},
                    itemKeys = persistentListOf("k34", "k70"),
                    onItemKeySelect = { selectedKey = it },
                    textId = R.string.product,
                    placeholderResId = R.string.product_placeholder,
                    itemContent = { key -> Text("Custo $key") }
                )
            }
        }

        composeRule.onNodeWithTag(AUTO_COMPLETE_TEXT_FIELD_TAG).performTextInput("guarda")
        composeRule.onNodeWithText("Custo k34").assertIsDisplayed()
        composeRule.onNodeWithText("Custo k70").assertIsDisplayed().performClick()

        composeRule.runOnIdle {
            assertEquals("k70", selectedKey)
        }
    }

    @Test
    fun selectedItemContentShowsBelowEditableNameOnlyWhileSelected() {
        composeRule.setContent {
            LucroVarejoTheme {
                AutoCompleteTextView(
                    selectedItem = "Bolsa",
                    items = persistentListOf("Bolsa"),
                    onItemSelect = {},
                    textId = R.string.product,
                    placeholderResId = R.string.product_placeholder,
                    selectedItemContent = { Text("Código: A-1") }
                )
            }
        }

        composeRule.onNodeWithText("Código: A-1", substring = true).assertIsDisplayed()
        composeRule.onNodeWithTag(AUTO_COMPLETE_TEXT_FIELD_TAG).performTextInput("x")
        composeRule.onAllNodesWithText("Código: A-1").assertCountEquals(0)
    }
}
