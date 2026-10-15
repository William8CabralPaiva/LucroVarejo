package com.cabral.lucrovarejo.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IconButtonInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsCustomTextAndIconAndForwardsClick() {
        var clickCount = 0
        composeRule.setContent {
            LucroVarejoTheme {
                IconButton(
                    enabled = true,
                    onClick = { clickCount++ },
                    icon = Icons.Filled.Add,
                    iconContentDescription = "Adicionar venda",
                    text = "Adicionar venda"
                )
            }
        }

        composeRule.onNodeWithText("Adicionar venda").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Adicionar venda").assertIsDisplayed()
        composeRule.onNodeWithTag(SAVE_PRODUCT_BUTTON_TAG).performClick()

        composeRule.runOnIdle {
            assertEquals(1, clickCount)
        }
    }
}
