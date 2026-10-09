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
import com.cabral.lucrovarejo.ui.screens.login.LOGIN_STORE_NAME_FIELD_TAG
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
@Config(sdk = [35])
class LoginScreenUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun displaysLoginFieldsAndDisablesSubmitUntilBothAreFilled() {
        composeRule.setContent {
            LucroVarejoTheme {
                LoginScreenContent(
                    state = LoginUiState(),
                    onStoreNameChange = {},
                    onPasswordChange = {},
                    onSubmit = {},
                    onRegister = {}
                )
            }
        }

        composeRule.onNodeWithText("Login").assertIsDisplayed()
        composeRule.onNodeWithTag(LOGIN_STORE_NAME_FIELD_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(LOGIN_PASSWORD_FIELD_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(LOADING_BUTTON_TAG).assertIsNotEnabled()
        composeRule.onNodeWithTag(LOGIN_REGISTER_BUTTON_TAG).assertIsDisplayed()
    }

    @Test
    fun forwardsInputAndEnablesSignInAndRegisterNavigation() {
        var storeName = ""
        var password = ""
        var submitCount = 0
        var registerCount = 0
        composeRule.setContent {
            LucroVarejoTheme {
                var state by remember { mutableStateOf(LoginUiState()) }
                LoginScreenContent(
                    state = state,
                    onStoreNameChange = {
                        storeName = it
                        state = state.copy(storeName = it)
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

        composeRule.onNodeWithTag(LOGIN_STORE_NAME_FIELD_TAG).performTextInput("minhaloja")
        composeRule.onNodeWithTag(LOGIN_PASSWORD_FIELD_TAG).performTextInput("senha123")
        composeRule.onNodeWithTag(LOADING_BUTTON_TAG).performClick()
        composeRule.onNodeWithTag(LOGIN_REGISTER_BUTTON_TAG).performClick()

        composeRule.runOnIdle {
            assertEquals("minhaloja", storeName)
            assertEquals("senha123", password)
            assertEquals(1, submitCount)
            assertEquals(1, registerCount)
        }
    }

    @Test
    fun loadingStateDisablesFieldsAndShowsProgress() {
        composeRule.setContent {
            LucroVarejoTheme {
                LoginScreenContent(
                    state = LoginUiState(
                        storeName = "minhaloja",
                        password = "senha123",
                        isLoading = true
                    ),
                    onStoreNameChange = {},
                    onPasswordChange = {},
                    onSubmit = {},
                    onRegister = {}
                )
            }
        }

        composeRule.onNodeWithTag(LOGIN_STORE_NAME_FIELD_TAG).assertIsNotEnabled()
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
                    onStoreNameChange = {},
                    onPasswordChange = {},
                    onSubmit = {},
                    onRegister = {}
                )
            }
        }

        composeRule.onNodeWithText("Não foi possível entrar com esses dados.").assertIsDisplayed()
    }
}
