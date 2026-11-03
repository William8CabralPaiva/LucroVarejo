package com.cabral.lucrovarejo.ui.screens.addproduct

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.SnackbarHostState
import com.cabral.lucrovarejo.ui.components.DELETE_PRODUCT_BUTTON_TAG
import com.cabral.lucrovarejo.ui.components.SAVE_PRODUCT_BUTTON_TAG
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AddProductScreenUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun displaysRequiredAndOptionalProductFields() {
        composeRule.setContent {
            LucroVarejoTheme {
                AddProductContent(
                    state = AddProductUiState(),
                    snackBarHostState = remember { SnackbarHostState() },
                    onNameChange = {},
                    onCodeChange = {},
                    onCostChange = {},
                    onDescriptionChange = {},
                    onSave = {}
                )
            }
        }

        composeRule.onNodeWithText("Nome do produto").assertIsDisplayed()
        composeRule.onNodeWithText("Código do Produto").assertIsDisplayed()
        composeRule.onNodeWithText("Valor pago").assertIsDisplayed()
        composeRule.onNodeWithText("Descrição").assertIsDisplayed()
        composeRule.onAllNodesWithText("*").assertCountEquals(2)
        composeRule.onAllNodesWithText("(opcional)").assertCountEquals(2)
        composeRule.onNodeWithTag(SAVE_PRODUCT_BUTTON_TAG).assertIsNotEnabled()
    }

    @Test
    fun validFormEnablesSaveAndLoadingDisablesFieldsAndSave() {
        composeRule.setContent {
            LucroVarejoTheme {
                var state by remember {
                    mutableStateOf(AddProductUiState(name = "Produto", cost = "12.34"))
                }
                AddProductContent(
                    state = state,
                    snackBarHostState = remember { SnackbarHostState() },
                    onNameChange = {},
                    onCodeChange = {},
                    onCostChange = {},
                    onDescriptionChange = {},
                    onSave = { state = state.copy(isLoading = true) }
                )
            }
        }

        composeRule.onNodeWithTag(SAVE_PRODUCT_BUTTON_TAG)
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsEnabled()

        composeRule.onNodeWithTag(SAVE_PRODUCT_BUTTON_TAG)
            .performScrollTo()
            .performClick()
            .assertIsNotEnabled()
    }

    @Test
    fun displaysSuccessSnackbarFromOneShotMessage() {
        composeRule.setContent {
            LucroVarejoTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                LaunchedEffect(snackbarHostState) {
                    snackbarHostState.showSnackbar("Item adicionado com sucesso")
                }
                AddProductContent(
                    state = AddProductUiState(),
                    snackBarHostState = snackbarHostState,
                    onNameChange = {},
                    onCodeChange = {},
                    onCostChange = {},
                    onDescriptionChange = {},
                    onSave = {}
                )
            }
        }

        composeRule.onNodeWithText("Item adicionado com sucesso").assertIsDisplayed()
    }

    @Test
    fun editModeShowsFilledFieldsWithEditAndDeleteButtons() {
        setEditContent()

        composeRule.onNodeWithText("Bolsa Feminina").assertIsDisplayed()
        composeRule.onNodeWithText("7891000123456").assertIsDisplayed()
        composeRule.onNodeWithText("89.90").assertIsDisplayed()
        composeRule.onNodeWithText("EDITAR").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag(DELETE_PRODUCT_BUTTON_TAG).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("CADASTRAR PRODUTO").assertDoesNotExist()
    }

    @Test
    fun editAndDeleteButtonsTriggerTheirActions() {
        val actions = mutableListOf<String>()
        setEditContent(onSave = { actions += "save" }, onDelete = { actions += "delete" })

        composeRule.onNodeWithTag(SAVE_PRODUCT_BUTTON_TAG).performScrollTo().performClick()
        composeRule.onNodeWithTag(DELETE_PRODUCT_BUTTON_TAG).performScrollTo().performClick()

        composeRule.runOnIdle {
            assertEquals(listOf("save", "delete"), actions)
        }
    }

    @Test
    fun deleteDialogAsksConfirmationAndReportsChoice() {
        val actions = mutableListOf<String>()
        setEditContent(
            state = editState.copy(showDeleteDialog = true),
            onDeleteConfirm = { actions += "confirm" },
            onDeleteDismiss = { actions += "dismiss" }
        )

        composeRule.onNodeWithText("Deletar produto").assertIsDisplayed()
        composeRule.onNodeWithText(
            "Tem certeza que deseja deletar Bolsa Feminina? Essa ação não pode ser desfeita."
        ).assertIsDisplayed()
        composeRule.onNodeWithText("Cancelar").performClick()
        composeRule.onNodeWithText("Deletar").performClick()

        composeRule.runOnIdle {
            assertEquals(listOf("dismiss", "confirm"), actions)
        }
    }

    private val editState = AddProductUiState(
        key = "product-key",
        name = "Bolsa Feminina",
        cod = "7891000123456",
        cost = "89.90",
        description = "Bolsa de couro"
    )

    private fun setEditContent(
        state: AddProductUiState = editState,
        onSave: () -> Unit = {},
        onDelete: () -> Unit = {},
        onDeleteConfirm: () -> Unit = {},
        onDeleteDismiss: () -> Unit = {}
    ) {
        composeRule.setContent {
            LucroVarejoTheme {
                AddProductContent(
                    state = state,
                    snackBarHostState = remember { SnackbarHostState() },
                    onNameChange = {},
                    onCodeChange = {},
                    onCostChange = {},
                    onDescriptionChange = {},
                    onSave = onSave,
                    onDelete = onDelete,
                    onDeleteConfirm = onDeleteConfirm,
                    onDeleteDismiss = onDeleteDismiss
                )
            }
        }
    }
}