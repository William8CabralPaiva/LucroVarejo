package com.cabral.lucrovarejo.ui.screens.product

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
import com.cabral.lucrovarejo.ui.components.SAVE_PRODUCT_BUTTON_TAG
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ProductScreenUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun displaysRequiredAndOptionalProductFields() {
        composeRule.setContent {
            LucroVarejoTheme {
                ProductContent(
                    state = ProductUiState(),
                    snackbarHostState = remember { SnackbarHostState() },
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
                    mutableStateOf(ProductUiState(name = "Produto", cost = "12.34"))
                }
                ProductContent(
                    state = state,
                    snackbarHostState = remember { SnackbarHostState() },
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
                ProductContent(
                    state = ProductUiState(),
                    snackbarHostState = snackbarHostState,
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
}
