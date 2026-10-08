package com.cabral.lucrovarejo.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoadingButtonUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun loadingButtonShowsProgressAndIsDisabledDuringLoading() {
        composeRule.setContent {
            LucroVarejoTheme {
                LoadingButton(
                    textId = R.string.register_submit,
                    isLoading = true,
                    onClick = {}
                )
            }
        }

        composeRule.onNodeWithTag(LOADING_INDICATOR_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(LOADING_BUTTON_TAG).assertIsNotEnabled()
        composeRule.onAllNodesWithText("Cadastrar").assertCountEquals(0)
    }

    @Test
    fun idleButtonDisplaysTextAndInvokesClickHandler() {
        var clickCount = 0
        composeRule.setContent {
            LucroVarejoTheme {
                LoadingButton(
                    textId = R.string.register_submit,
                    onClick = { clickCount++ }
                )
            }
        }

        composeRule.onNodeWithText("Cadastrar").assertIsDisplayed().performClick()
        composeRule.runOnIdle { assertEquals(1, clickCount) }
    }
}
