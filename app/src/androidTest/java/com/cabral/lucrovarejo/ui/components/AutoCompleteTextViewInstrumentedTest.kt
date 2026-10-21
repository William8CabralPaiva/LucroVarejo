package com.cabral.lucrovarejo.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AutoCompleteTextViewInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun typingFiltersSuggestionsAndSelectsItem() {
        var selectedItem = ""
        composeRule.setContent {
            LucroVarejoTheme {
                var current by remember { mutableStateOf<String?>(null) }
                AutoCompleteTextViewContent(
                    selectedItem = current,
                    items = listOf("Bolsa", "Acessório"),
                    onItemSelected = {
                        current = it
                        selectedItem = it
                    },
                    textId = R.string.product,
                    placeholderResId = R.string.product_placeholder
                )
            }
        }

        composeRule.onNodeWithText("Produto").assertIsDisplayed()
        composeRule.onNodeWithText("Selecionar Produto").assertIsDisplayed()
        composeRule.onNodeWithTag(AUTO_COMPLETE_TEXT_FIELD_TAG).performTextInput("aces")
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText("Bolsa").assertCountEquals(0)
        composeRule.onNodeWithText("Acessório").performClick()

        composeRule.runOnIdle {
            assertEquals("Acessório", selectedItem)
        }
    }
}
