package com.cabral.lucrovarejo.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.store.StoreSummary
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class StoreDialogsUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun switchDialogListsStoresAndReportsSelection() {
        var selected: String? = null
        composeRule.setContent {
            LucroVarejoTheme {
                SwitchStoreDialog(
                    stores = listOf(StoreSummary("1", "Loja Centro"), StoreSummary("2", "")),
                    activeStoreId = "1",
                    onSelect = { selected = it },
                    onCreate = {},
                    onDismiss = {}
                )
            }
        }

        composeRule.onNodeWithText("Loja Centro").assertIsDisplayed()
        composeRule.onNodeWithText("Loja sem nome").assertIsDisplayed().performClick()
        composeRule.runOnIdle { assertEquals("2", selected) }
    }

    @Test
    fun switchDialogCreateButtonInvokesCallback() {
        var created = false
        composeRule.setContent {
            LucroVarejoTheme {
                SwitchStoreDialog(
                    stores = emptyList(),
                    activeStoreId = null,
                    onSelect = {},
                    onCreate = { created = true },
                    onDismiss = {}
                )
            }
        }

        composeRule.onNodeWithText("Criar nova loja").performClick()
        composeRule.runOnIdle { assertTrue(created) }
    }

    @Test
    fun nameDialogDisablesSaveWhenNameIsBlank() {
        composeRule.setContent {
            LucroVarejoTheme {
                StoreNameDialog(
                    initialName = "",
                    title = R.string.settings_create_store_dialog_title,
                    errorMessage = R.string.settings_create_store_error,
                    onSave = { _, _ -> },
                    onDismiss = {}
                )
            }
        }

        composeRule.onNodeWithText("Salvar").assertIsNotEnabled()
    }

    @Test
    fun nameDialogSavesTypedNameAndDismissesOnSuccess() {
        var savedName: String? = null
        var dismissed = false
        composeRule.setContent {
            LucroVarejoTheme {
                StoreNameDialog(
                    initialName = "",
                    title = R.string.settings_create_store_dialog_title,
                    errorMessage = R.string.settings_create_store_error,
                    onSave = { name, done ->
                        savedName = name
                        done(true)
                    },
                    onDismiss = { dismissed = true }
                )
            }
        }

        composeRule.onNodeWithText("Nome da loja").performTextInput("Loja Nova")
        composeRule.onNodeWithText("Salvar").assertIsEnabled().performClick()
        composeRule.runOnIdle {
            assertEquals("Loja Nova", savedName)
            assertTrue(dismissed)
        }
    }

    @Test
    fun nameDialogShowsErrorAndStaysOpenOnFailure() {
        var dismissed = false
        composeRule.setContent {
            LucroVarejoTheme {
                StoreNameDialog(
                    initialName = "Loja",
                    title = R.string.settings_create_store_dialog_title,
                    errorMessage = R.string.settings_create_store_error,
                    onSave = { _, done -> done(false) },
                    onDismiss = { dismissed = true }
                )
            }
        }

        composeRule.onNodeWithText("Salvar").performClick()
        composeRule.onNodeWithText("Não foi possível criar a loja. Tente novamente.")
            .assertIsDisplayed()
        composeRule.runOnIdle { assertTrue(!dismissed) }
    }
}
