package com.cabral.lucrovarejo.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cabral.lucrovarejo.ui.screens.register.RegisterScreenContent
import com.cabral.lucrovarejo.ui.screens.register.RegisterUiState
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
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

        composeRule.onNodeWithText("Cadastro da loja").assertIsDisplayed()
        composeRule.onNodeWithText("Loja").assertIsDisplayed()
        composeRule.onNodeWithText("E-mail").assertIsDisplayed()
        composeRule.onNodeWithText("Senha").assertIsDisplayed()
        composeRule.onNodeWithText("Confirmar senha").assertIsDisplayed()
        composeRule.onNodeWithText("Cadastrar").assertIsNotEnabled()
        composeRule.onNodeWithContentDescription("Mostrar senha").performClick()
        composeRule.onNodeWithContentDescription("Ocultar senha").assertIsDisplayed()
    }
}
