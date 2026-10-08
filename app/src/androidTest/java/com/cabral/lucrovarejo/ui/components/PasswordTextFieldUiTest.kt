package com.cabral.lucrovarejo.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PasswordTextFieldUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun statelessFieldReportsVisibilityChangesToItsOwner() {
        composeRule.setContent {
            LucroVarejoTheme {
                var passwordVisible by remember { mutableStateOf(false) }

                PasswordTextFieldStateless(
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
}
