package com.cabral.lucrovarejo.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.components.LOADING_BUTTON_TAG
import com.cabral.lucrovarejo.ui.components.LOADING_INDICATOR_TAG
import com.cabral.lucrovarejo.ui.screens.register.REGISTER_BACK_BUTTON_TAG
import com.cabral.lucrovarejo.ui.screens.register.REGISTER_CONFIRM_PASSWORD_FIELD_TAG
import com.cabral.lucrovarejo.ui.screens.register.REGISTER_EMAIL_FIELD_TAG
import com.cabral.lucrovarejo.ui.screens.register.REGISTER_PASSWORD_FIELD_TAG
import com.cabral.lucrovarejo.ui.screens.register.REGISTER_STORE_NAME_FIELD_TAG
import com.cabral.lucrovarejo.ui.screens.register.REGISTER_TITLE_TAG
import com.cabral.lucrovarejo.ui.screens.register.RegisterScreenContent
import com.cabral.lucrovarejo.ui.screens.register.RegisterUiState
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegisterScreenUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun displaysTheRequiredRegistrationFields() {
        composeRule.setContent {
            LucroVarejoTheme {
                RegisterScreenContent(
                    state = RegisterUiState(),
                    onStoreNameChange = {},
                    onEmailChange = {},
                    onPasswordChange = {},
                    onConfirmPasswordChange = {},
                    onSubmit = {},
                    onBackPress = {}
                )
            }
        }

        composeRule.onNodeWithTag(REGISTER_TITLE_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(REGISTER_STORE_NAME_FIELD_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(REGISTER_EMAIL_FIELD_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(REGISTER_PASSWORD_FIELD_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(REGISTER_CONFIRM_PASSWORD_FIELD_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(LOADING_BUTTON_TAG).assertIsNotEnabled()
        composeRule.onNodeWithContentDescription("Mostrar senha").performClick()
        composeRule.onNodeWithContentDescription("Ocultar senha").assertIsDisplayed()
    }

    @Test
    fun inputChangesAreForwardedAndValidFormCanBeSubmitted() {
        var storeName = ""
        var email = ""
        var submitCount = 0
        var backCount = 0
        composeRule.setContent {
            LucroVarejoTheme {
                var formState by remember {
                    mutableStateOf(
                        RegisterUiState(
                            storeName = "",
                            email = "",
                            password = "senha123",
                            confirmPassword = "senha123"
                        )
                    )
                }
                RegisterScreenContent(
                    state = formState,
                    onStoreNameChange = {
                        storeName = it
                        formState = formState.copy(storeName = it)
                    },
                    onEmailChange = {
                        email = it
                        formState = formState.copy(email = it)
                    },
                    onPasswordChange = {},
                    onConfirmPasswordChange = {},
                    onSubmit = { submitCount++ },
                    onBackPress = { backCount++ }
                )
            }
        }

        composeRule.onNodeWithTag(REGISTER_STORE_NAME_FIELD_TAG).performTextInput("minhaloja")
        composeRule.onNodeWithTag(REGISTER_EMAIL_FIELD_TAG).performTextInput("loja@example.com")
        composeRule.onNodeWithTag(LOADING_BUTTON_TAG).assertIsDisplayed().performClick()
        composeRule.onNodeWithTag(REGISTER_BACK_BUTTON_TAG).performClick()

        composeRule.runOnIdle {
            assertEquals("minhaloja", storeName)
            assertEquals("loja@example.com", email)
            assertEquals(1, submitCount)
            assertEquals(1, backCount)
        }
    }

    @Test
    fun loadingStateDisablesFormActionsAndShowsProgress() {
        composeRule.setContent {
            LucroVarejoTheme {
                RegisterScreenContent(
                    state = RegisterUiState(
                        storeName = "minhaloja",
                        email = "loja@example.com",
                        password = "senha123",
                        confirmPassword = "senha123",
                        isLoading = true
                    ),
                    onStoreNameChange = {},
                    onEmailChange = {},
                    onPasswordChange = {},
                    onConfirmPasswordChange = {},
                    onSubmit = {},
                    onBackPress = {}
                )
            }
        }

        composeRule.onNodeWithTag(LOADING_INDICATOR_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(REGISTER_STORE_NAME_FIELD_TAG).assertIsNotEnabled()
        composeRule.onNodeWithTag(REGISTER_EMAIL_FIELD_TAG).assertIsNotEnabled()
        composeRule.onNodeWithTag(REGISTER_PASSWORD_FIELD_TAG).assertIsNotEnabled()
        composeRule.onNodeWithTag(REGISTER_CONFIRM_PASSWORD_FIELD_TAG).assertIsNotEnabled()
        composeRule.onNodeWithTag(LOADING_BUTTON_TAG).assertIsNotEnabled()
        composeRule.onNodeWithTag(REGISTER_BACK_BUTTON_TAG).assertIsNotEnabled()
    }

    @Test
    fun displaysFieldAndServerErrors() {
        composeRule.setContent {
            LucroVarejoTheme {
                RegisterScreenContent(
                    state = RegisterUiState(
                        storeNameError = R.string.register_store_required,
                        emailError = R.string.register_email_invalid,
                        passwordError = R.string.register_password_too_short,
                        confirmPasswordError = R.string.register_password_mismatch,
                        errorMessage = R.string.register_error_network
                    ),
                    onStoreNameChange = {},
                    onEmailChange = {},
                    onPasswordChange = {},
                    onConfirmPasswordChange = {},
                    onSubmit = {},
                    onBackPress = {}
                )
            }
        }

        listOf(
            R.string.register_store_required,
            R.string.register_email_invalid,
            R.string.register_password_too_short,
            R.string.register_password_mismatch,
            R.string.register_error_network
        ).forEach { stringId ->
            composeRule.onNodeWithText(composeRule.activity.getString(stringId)).assertIsDisplayed()
        }
    }
}
