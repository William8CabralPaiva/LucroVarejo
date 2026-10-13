package com.cabral.lucrovarejo.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import com.cabral.lucrovarejo.ui.theme.DarkSurfaceVariant
import com.cabral.lucrovarejo.ui.theme.ForestGreenHeader
import com.cabral.lucrovarejo.ui.theme.TextSecondaryDark
import com.cabral.lucrovarejo.ui.theme.TextSecondaryLight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class IconButtonUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun enabledButtonShowsLabelAndInvokesClick() {
        var clickCount = 0
        composeRule.setContent {
            LucroVarejoTheme {
                IconButton(enabled = true, onClick = { clickCount++ })
            }
        }

        composeRule.onNodeWithText("CADASTRAR PRODUTO").assertIsDisplayed()
        composeRule.onNodeWithTag(SAVE_PRODUCT_BUTTON_TAG).performClick()

        composeRule.runOnIdle {
            assertEquals(1, clickCount)
        }
    }

    @Test
    fun disabledButtonIsVisibleAndDoesNotInvokeClick() {
        var clickCount = 0
        composeRule.setContent {
            LucroVarejoTheme {
                IconButton(enabled = false, onClick = { clickCount++ })
            }
        }

        composeRule.onNodeWithText("CADASTRAR PRODUTO").assertIsDisplayed()
        composeRule.onNodeWithTag(SAVE_PRODUCT_BUTTON_TAG).assertIsNotEnabled().performClick()

        composeRule.runOnIdle {
            assertEquals(0, clickCount)
        }
    }

    @Test
    fun statelessContentRendersProvidedLabelAndDisabledState() {
        composeRule.setContent {
            LucroVarejoTheme {
                SaveProductButtonContent(
                    label = "SALVAR ITEM",
                    enabled = false,
                    onClick = {}
                )
            }
        }

        composeRule.onNodeWithText("SALVAR ITEM").assertIsDisplayed()
        composeRule.onNodeWithTag(SAVE_PRODUCT_BUTTON_TAG).assertIsNotEnabled()
    }

    @Test
    fun loadingButtonShowsProgressAndDisablesSubmission() {
        composeRule.setContent {
            LucroVarejoTheme {
                IconButton(enabled = true, isLoading = true, onClick = {})
            }
        }

        composeRule.onNodeWithText("CADASTRANDO...").assertIsDisplayed()
        composeRule.onNodeWithTag(SAVE_PRODUCT_PROGRESS_TAG)
            .assertIsDisplayed()
            .assertWidthIsEqualTo(16.dp)
            .assertHeightIsEqualTo(16.dp)
        composeRule.onNodeWithTag(SAVE_PRODUCT_BUTTON_TAG).assertIsNotEnabled()
    }

    @Test
    fun buttonUsesWhiteContentWhenEnabledAndReadableDisabledColors() {
        val lightPalette = saveProductButtonPalette(isDarkTheme = false)
        val darkPalette = saveProductButtonPalette(isDarkTheme = true)

        assertEquals(ForestGreenHeader, lightPalette.container)
        assertEquals(Color.White, lightPalette.content)
        assertEquals(Color.White, darkPalette.content)
        assertEquals(TextSecondaryLight, lightPalette.disabledContent)
        assertEquals(TextSecondaryDark, darkPalette.disabledContent)
        assertEquals(DarkSurfaceVariant, darkPalette.disabledContainer)
    }
}
