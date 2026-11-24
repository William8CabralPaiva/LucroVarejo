package com.cabral.lucrovarejo.ui.screens

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
import com.cabral.lucrovarejo.ui.components.LOADING_BUTTON_TAG
import com.cabral.lucrovarejo.ui.components.LOADING_INDICATOR_TAG
import com.cabral.lucrovarejo.ui.screens.login.LOGIN_PASSWORD_FIELD_TAG
import com.cabral.lucrovarejo.ui.screens.login.LOGIN_REGISTER_BUTTON_TAG
import com.cabral.lucrovarejo.ui.screens.login.LOGIN_EMAIL_FIELD_TAG
import com.cabral.lucrovarejo.ui.screens.login.LOGIN_FORGOT_PASSWORD_BUTTON_TAG
import com.cabral.lucrovarejo.ui.screens.login.LoginScreenContent
import com.cabral.lucrovarejo.ui.screens.login.LoginUiState
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h891dp")
class LoginScreenUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun displaysLoginFieldsAndDisablesSubmitUntilBothAreFilled() {
        composeRule.setContent {
            LucroVarejoTheme {
                LoginScreenContent(
                    state = LoginUiState(),
                    onEmailChange = {},
                    onPasswordChange = {},
                    onSubmit = {},
                    onRegister = {}
                )
            }
        }

        composeRule.onNodeWithTag(LOGIN_EMAIL_FIELD_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(LOGIN_PASSWORD_FIELD_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(LOADING_BUTTON_TAG).assertIsNotEnabled()
        composeRule.onNodeWithTag(LOGIN_REGISTER_BUTTON_TAG).assertIsDisplayed()
    }

    @Test
    fun forwardsInputAndEnablesSignInAndRegisterNavigation() {
        var email = ""
        var password = ""
        var submitCount = 0
        var registerCount = 0
        composeRule.setContent {
            LucroVarejoTheme {
                var state by remember { mutableStateOf(LoginUiState()) }
                LoginScreenContent(
                    state = state,
                    onEmailChange = {
                        email = it
                        state = state.copy(email = it)
                    },
                    onPasswordChange = {
                        password = it
                        state = state.copy(password = it)
                    },
                    onSubmit = { submitCount++ },
                    onRegister = { registerCount++ }
                )
            }
        }

        composeRule.onNodeWithTag(LOGIN_EMAIL_FIELD_TAG).performTextInput("minhaloja@exemplo.com")
        composeRule.onNodeWithTag(LOGIN_PASSWORD_FIELD_TAG).performTextInput("senha123")
        composeRule.onNodeWithTag(LOADING_BUTTON_TAG).performClick()
        composeRule.onNodeWithTag(LOGIN_REGISTER_BUTTON_TAG).performClick()

        composeRule.runOnIdle {
            assertEquals("minhaloja@exemplo.com", email)
            assertEquals("senha123", password)
            assertEquals(1, submitCount)
            assertEquals(1, registerCount)
        }
    }

    @Test
    fun forgotPasswordButtonTriggersNavigation() {
        var forgotCount = 0
        composeRule.setContent {
            LucroVarejoTheme {
                LoginScreenContent(
                    state = LoginUiState(),
                    onEmailChange = {},
                    onPasswordChange = {},
                    onSubmit = {},
                    onRegister = {},
                    onForgotPassword = { forgotCount++ }
                )
            }
        }

        composeRule.onNodeWithTag(LOGIN_FORGOT_PASSWORD_BUTTON_TAG).performClick()

        composeRule.runOnIdle { assertEquals(1, forgotCount) }
    }

    @Test
    fun loadingStateDisablesFieldsAndShowsProgress() {
        composeRule.setContent {
            LucroVarejoTheme {
                LoginScreenContent(
                    state = LoginUiState(
                        email = "minhaloja@exemplo.com",
                        password = "senha123",
                        isLoading = true
                    ),
                    onEmailChange = {},
                    onPasswordChange = {},
                    onSubmit = {},
                    onRegister = {}
                )
            }
        }

        composeRule.onNodeWithTag(LOGIN_EMAIL_FIELD_TAG).assertIsNotEnabled()
        composeRule.onNodeWithTag(LOGIN_PASSWORD_FIELD_TAG).assertIsNotEnabled()
        composeRule.onNodeWithTag(LOADING_BUTTON_TAG).assertIsNotEnabled()
        composeRule.onNodeWithTag(LOADING_INDICATOR_TAG).assertIsDisplayed()
    }

    @Test
    fun displaysSignInError() {
        composeRule.setContent {
            LucroVarejoTheme {
                LoginScreenContent(
                    state = LoginUiState(errorMessage = "Não foi possível entrar com esses dados."),
                    onEmailChange = {},
                    onPasswordChange = {},
                    onSubmit = {},
                    onRegister = {}
                )
            }
        }

        composeRule.onNodeWithText("Não foi possível entrar com esses dados.").assertIsDisplayed()
    }
}
