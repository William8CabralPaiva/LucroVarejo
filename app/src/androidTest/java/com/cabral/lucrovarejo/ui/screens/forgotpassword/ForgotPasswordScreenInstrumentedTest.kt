package com.cabral.lucrovarejo.ui.screens.forgotpassword

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.components.LOADING_BUTTON_TAG
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ForgotPasswordScreenInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun submitsEmailAndShowsConfirmation() {
        var submittedEmail = ""
        composeRule.setContent {
            LucroVarejoTheme {
                val state = remember { mutableStateOf(ForgotPasswordUiState()) }
                ForgotPasswordScreenContent(
                    state = state.value,
                    onEmailChange = { state.value = state.value.copy(email = it) },
                    onSubmit = {
                        submittedEmail = state.value.email
                        state.value = state.value.copy(isEmailSent = true)
                    },
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithTag(FORGOT_PASSWORD_EMAIL_FIELD_TAG)
            .performTextInput("owner@example.com")
        composeRule.onNodeWithTag(LOADING_BUTTON_TAG).performClick()

        composeRule.onNodeWithTag(FORGOT_PASSWORD_SUCCESS_TAG).assertIsDisplayed()
        composeRule.runOnIdle { assertEquals("owner@example.com", submittedEmail) }
    }

    @Test
    fun backButtonIsDisplayedAndClickable() {
        var backCount = 0
        composeRule.setContent {
            LucroVarejoTheme {
                ForgotPasswordScreenContent(
                    state = ForgotPasswordUiState(),
                    onEmailChange = {},
                    onSubmit = {},
                    onBack = { backCount++ }
                )
            }
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.navigate_back))
            .performClick()

        composeRule.runOnIdle { assertEquals(1, backCount) }
    }
}
