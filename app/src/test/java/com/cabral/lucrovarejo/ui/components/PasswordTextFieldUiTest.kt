package com.cabral.lucrovarejo.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PasswordTextFieldUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun contentFieldReportsVisibilityChangesToItsOwner() {
        composeRule.setContent {
            LucroVarejoTheme {
                var passwordVisible by remember { mutableStateOf(false) }

                PasswordTextFieldContent(
                    value = "segura123",
                    onValueChange = {},
                    labelResId = R.string.register_password_label,
                    showPasswordDescriptionResId = R.string.register_password_show,
                    hidePasswordDescriptionResId = R.string.register_password_hide,
                    passwordVisible = passwordVisible,
                    onPasswordVisibilityChange = { passwordVisible = it }
                )
            }
        }

        composeRule.onNodeWithText("Senha").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Mostrar senha").performClick()
        composeRule.onNodeWithContentDescription("Ocultar senha").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Ocultar senha").performClick()
        composeRule.onNodeWithContentDescription("Mostrar senha").assertIsDisplayed()
    }

    @Test
    fun statefulFieldForwardsInputAndTogglesPasswordVisibility() {
        var currentPassword = ""
        composeRule.setContent {
            LucroVarejoTheme {
                var password by remember { mutableStateOf(currentPassword) }
                PasswordTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        currentPassword = it
                    },
                    labelResId = R.string.register_password_label,
                    showPasswordDescriptionResId = R.string.register_password_show,
                    hidePasswordDescriptionResId = R.string.register_password_hide
                )
            }
        }

        composeRule.onNodeWithText("Senha").performTextInput("senha123")
        composeRule.onNodeWithContentDescription("Mostrar senha").performClick()
        composeRule.onNodeWithContentDescription("Ocultar senha").assertIsDisplayed()

        composeRule.runOnIdle { assertEquals("senha123", currentPassword) }
    }
}
