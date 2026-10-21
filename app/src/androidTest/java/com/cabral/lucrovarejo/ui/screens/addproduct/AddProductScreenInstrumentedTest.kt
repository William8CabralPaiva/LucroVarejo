package com.cabral.lucrovarejo.ui.screens.addproduct

import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddProductScreenInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun editModeShowsFilledFieldsAndDeleteRequiresConfirmation() {
        val actions = mutableListOf<String>()
        composeRule.setContent {
            LucroVarejoTheme {
                AddProductContent(
                    state = AddProductUiState(
                        key = "product-key",
                        name = "Bolsa Feminina",
                        cod = "7891000123456",
                        cost = "89.90",
                        showDeleteDialog = true
                    ),
                    snackBarHostState = remember { SnackbarHostState() },
                    onNameChange = {},
                    onCodeChange = {},
                    onCostChange = {},
                    onDescriptionChange = {},
                    onSave = { actions += "save" },
                    onDelete = { actions += "delete" },
                    onDeleteConfirm = { actions += "confirm" },
                    onDeleteDismiss = { actions += "dismiss" }
                )
            }
        }

        composeRule.onNodeWithText("Deletar produto").assertIsDisplayed()
        composeRule.onNodeWithText("Cancelar").performClick()
        composeRule.onNodeWithText("Deletar").performClick()

        composeRule.runOnIdle {
            assertEquals(listOf("dismiss", "confirm"), actions)
        }
    }

    @Test
    fun editModeShowsEditAndDeleteButtons() {
        composeRule.setContent {
            LucroVarejoTheme {
                AddProductContent(
                    state = AddProductUiState(
                        key = "product-key",
                        name = "Bolsa Feminina",
                        cost = "89.90"
                    ),
                    snackBarHostState = remember { SnackbarHostState() },
                    onNameChange = {},
                    onCodeChange = {},
                    onCostChange = {},
                    onDescriptionChange = {},
                    onSave = {}
                )
            }
        }

        composeRule.onNodeWithText("Bolsa Feminina").assertIsDisplayed()
        composeRule.onNodeWithText("EDITAR").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("DELETAR").performScrollTo().assertIsDisplayed()
    }
}
