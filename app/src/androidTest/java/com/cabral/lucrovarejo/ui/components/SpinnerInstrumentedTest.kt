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
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class SpinnerInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun spinnerDisplaysLabelAndPlaceholderAndReturnsSelectedItem() {
        var selectedItem = ""
        composeRule.setContent {
            LucroVarejoTheme {
                var currentItem by remember { mutableStateOf<String?>(null) }
                SpinnerContent(
                    selectedItem = currentItem,
                    items = listOf("Bolsa", "Acessório"),
                    onItemSelected = {
                        currentItem = it
                        selectedItem = it
                    },
                    textId = R.string.product,
                    placeholderResId = R.string.product_placeholder
                )
            }
        }

        composeRule.onNodeWithText("Produto").assertIsDisplayed()
        composeRule.onNodeWithText("Selecionar Produto").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("Acessório").performClick()
        composeRule.onNodeWithText("Acessório").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals("Acessório", selectedItem)
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
