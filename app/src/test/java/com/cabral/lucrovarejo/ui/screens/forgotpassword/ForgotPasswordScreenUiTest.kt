package com.cabral.lucrovarejo.ui.screens.forgotpassword

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.components.LOADING_BUTTON_TAG
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h891dp")
class ForgotPasswordScreenUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun disablesSubmitUntilEmailIsFilled() {
        composeRule.setContent {
            LucroVarejoTheme {
                ForgotPasswordScreenContent(
                    state = ForgotPasswordUiState(),
                    onEmailChange = {},
                    onSubmit = {},
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithTag(FORGOT_PASSWORD_EMAIL_FIELD_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(LOADING_BUTTON_TAG).assertIsNotEnabled()
    }

    @Test
    fun forwardsInputSubmitAndBack() {
        var email = ""
        var submitCount = 0
        var backCount = 0
        composeRule.setContent {
            LucroVarejoTheme {
                var state by remember { mutableStateOf(ForgotPasswordUiState()) }
                ForgotPasswordScreenContent(
                    state = state,
                    onEmailChange = {
                        email = it
                        state = state.copy(email = it)
                    },
                    onSubmit = { submitCount++ },
                    onBack = { backCount++ }
                )
            }
        }

        composeRule.onNodeWithTag(FORGOT_PASSWORD_EMAIL_FIELD_TAG)
            .performTextInput("owner@example.com")
        composeRule.onNodeWithTag(LOADING_BUTTON_TAG).performClick()
        composeRule.onNodeWithText("Voltar").performClick()

        composeRule.runOnIdle {
            assertEquals("owner@example.com", email)
            assertEquals(1, submitCount)
            assertEquals(1, backCount)
        }
    }

    @Test
    fun showsSuccessAndErrorMessages() {
        composeRule.setContent {
            LucroVarejoTheme {
                ForgotPasswordScreenContent(
                    state = ForgotPasswordUiState(
                        email = "owner@example.com",
                        isEmailSent = true,
                        errorMessage = R.string.forgot_password_error
                    ),
                    onEmailChange = {},
                    onSubmit = {},
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithTag(FORGOT_PASSWORD_SUCCESS_TAG).assertIsDisplayed()
        composeRule.onNodeWithText(
            ApplicationProvider.getApplicationContext<Context>()
                .getString(R.string.forgot_password_error)
        ).assertIsDisplayed()
    }
}
