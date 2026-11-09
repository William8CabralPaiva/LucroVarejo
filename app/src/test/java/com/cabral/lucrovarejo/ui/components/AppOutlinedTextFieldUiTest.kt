package com.cabral.lucrovarejo.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppOutlinedTextFieldUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun displaysLabelAndUpdatesValueOnInput() {
        var textValue by mutableStateOf("")

        composeRule.setContent {
            LucroVarejoTheme {
                AppOutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    label = { Text("Nome da Loja") },
                    modifier = Modifier.testTag("app_text_field_tag")
                )
            }
        }

        composeRule.onNodeWithTag("app_text_field_tag")
            .assertIsDisplayed()
            .performTextInput("Minha Loja")

        assertEquals("Minha Loja", textValue)
    }

    @Test
    fun supportsEmailKeyboardOptions() {
        var emailValue by mutableStateOf("")

        composeRule.setContent {
            LucroVarejoTheme {
                AppOutlinedTextField(
                    value = emailValue,
                    onValueChange = { emailValue = it },
                    keyboardOptions = AppKeyboardDefaults.email,
                    modifier = Modifier.testTag("email_tag")
                )
            }
        }

        composeRule.onNodeWithTag("email_tag")
            .assertIsDisplayed()
            .performTextInput("teste@exemplo.com")

        assertEquals("teste@exemplo.com", emailValue)
    }
}
