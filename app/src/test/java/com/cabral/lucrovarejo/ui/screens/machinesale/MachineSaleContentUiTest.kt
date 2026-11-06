package com.cabral.lucrovarejo.ui.screens.machinesale

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.payment.TerminalPaymentType
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h1400dp")
class MachineSaleContentUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val calls = mutableListOf<String>()

    private fun render(
        state: MachineSaleUiState,
        onOpenHistory: (TariffEntryUi) -> Unit = { calls += "history:${it.id}" }
    ) {
        composeRule.setContent {
            LucroVarejoTheme {
                MachineSaleContent(
                    state = state,
                    onNameChange = { calls += "name:$it" },
                    onToggleMethod = { calls += "method:$it" },
                    onCashChargesTariffToggle = { calls += "cash:$it" },
                    onAddBrand = { calls += "addBrand:$it" },
                    onToggleDebitBrand = { calls += "debit:$it" },
                    onToggleCreditBrand = { calls += "credit:$it" },
                    onAddCreditInstallment = { brand, n -> calls += "installment:$brand:$n" },
                    onTariffRateChange = { id, text -> calls += "rate:$id:$text" },
                    onTariffDateChange = { id, _ -> calls += "date:$id" },
                    onSave = { calls += "save:$it" },
                    onOpenMachineHistory = onOpenHistory
                )
            }
        }
    }

    private fun tab(label: String) = composeRule.onAllNodesWithText(label).onFirst().performClick()

    @Test
    fun loadingHidesSaveButtons() {
        render(MachineSaleUiState(isLoading = true))

        composeRule.onNodeWithText("Salvar").assertDoesNotExist()
        composeRule.onNodeWithText("Salvar e voltar").assertDoesNotExist()
    }

    @Test
    fun saveButtonsRequireNameAndMethodAndForwardCloseFlag() {
        render(MachineSaleUiState(name = "Stone", selectedMethods = setOf(TerminalPaymentType.PIX)))

        composeRule.onNodeWithText("Salvar").assertIsEnabled().performClick()
        composeRule.onNodeWithText("Salvar e voltar").performClick()

        assertEquals(listOf("save:false", "save:true"), calls)
    }

    @Test
    fun bothSaveButtonsAreDisabledWhileSavingWithoutClosing() = assertBothDisabled(closeAfterSave = false)

    @Test
    fun bothSaveButtonsAreDisabledWhileSavingAndClosing() = assertBothDisabled(closeAfterSave = true)

    private fun assertBothDisabled(closeAfterSave: Boolean) {
        render(
            MachineSaleUiState(
                name = "Stone",
                selectedMethods = setOf(TerminalPaymentType.PIX),
                isSaving = true,
                closeAfterSave = closeAfterSave
            )
        )

        composeRule.onNodeWithTag("save-product-button").assertIsNotEnabled()
        composeRule.onNodeWithTag("save-and-back-machine-button").assertIsNotEnabled()
    }

    @Test
    fun saveDisabledWhenFormIncompleteAndShowsErrors() {
        render(
            MachineSaleUiState(
                nameError = R.string.terminal_form_name_required,
                methodsError = R.string.terminal_form_methods_required,
                saveError = R.string.terminal_form_save_error
            )
        )

        composeRule.onNodeWithText("Salvar").assertIsNotEnabled()
        composeRule.onNodeWithText("Informe o nome da maquininha.").assertExists()
        composeRule.onNodeWithText("Selecione ao menos um tipo de pagamento.").assertExists()
        composeRule.onNodeWithText("Não foi possível salvar a maquininha. Tente novamente.").assertExists()
    }

    @Test
    fun nameInputIsForwarded() {
        render(MachineSaleUiState())

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput("Cielo")

        assertTrue(calls.any { it.startsWith("name:") && it.contains("Cielo") })
    }

    @Test
    fun enableSwitchTogglesCurrentMethod() {
        render(MachineSaleUiState())

        tab("Pix")
        composeRule.onNodeWithText("Habilitar este tipo de pagamento").assertExists()
        composeRule.onNode(androidx.compose.ui.test.isToggleable()).performClick()

        assertEquals(listOf("method:PIX"), calls)
    }

    @Test
    fun cashShowsTariffToggleAndEntryFields() {
        render(
            MachineSaleUiState(
                selectedMethods = setOf(TerminalPaymentType.CASH),
                cashChargesTariff = true,
                tariffEntries = listOf(
                    TariffEntryUi(TerminalPaymentType.CASH, rateText = "1.00", effectiveFromMillis = 0L, currentRateBps = 100)
                )
            ),
            onOpenHistory = { calls += "history:${it.method}" }
        )

        composeRule.onNodeWithText("Cobrar tarifa?").assertExists()
        composeRule.onNodeWithText("Tarifa atual: 1.00%").assertExists()
        composeRule.onNodeWithText("Ver histórico de tarifas").performClick()

        assertEquals(listOf("history:CASH"), calls)
    }

    @Test
    fun pixEntryWithoutCurrentTariffShowsPlaceholderText() {
        render(
            MachineSaleUiState(
                selectedMethods = setOf(TerminalPaymentType.PIX),
                tariffEntries = listOf(TariffEntryUi(TerminalPaymentType.PIX, effectiveFromMillis = 0L))
            )
        )

        tab("Pix")
        composeRule.onNodeWithText("Sem tarifa cadastrada").assertExists()
        composeRule.onNodeWithText("Ver histórico de tarifas").assertDoesNotExist()
    }

    @Test
    fun debitTabAddsAndTogglesBrands() {
        render(
            MachineSaleUiState(
                selectedMethods = setOf(TerminalPaymentType.DEBIT),
                availableBrands = listOf("Visa", "Master"),
                selectedDebitBrandIds = setOf("Visa"),
                tariffEntries = listOf(
                    TariffEntryUi(TerminalPaymentType.DEBIT, "Visa", "Visa", effectiveFromMillis = 0L)
                )
            )
        )

        tab("Débito")
        composeRule.onNodeWithText("Master").performClick()
        composeRule.onAllNodes(hasSetTextAction())[1]
            .performTextInput("Elo")
        composeRule.onNodeWithText("Adicionar bandeira").performClick()

        assertTrue(calls.contains("debit:Master"))
        assertTrue(calls.contains("addBrand:Elo"))
    }

    @Test
    fun debitTabWithoutBrandsShowsEmptyHint() {
        render(MachineSaleUiState(selectedMethods = setOf(TerminalPaymentType.DEBIT)))

        tab("Débito")
        composeRule.onNodeWithText("Nenhuma bandeira cadastrada para esta maquininha ainda.")
            .assertExists()
    }

    @Test
    fun creditTabShowsInstallmentsAndAddsNewOne() {
        render(
            MachineSaleUiState(
                selectedMethods = setOf(TerminalPaymentType.CREDIT),
                availableBrands = listOf("Visa"),
                selectedCreditBrandIds = setOf("Visa"),
                creditInstallmentsByBrand = mapOf("Visa" to listOf(1)),
                tariffEntries = listOf(
                    TariffEntryUi(
                        TerminalPaymentType.CREDIT, "Visa", "Visa", 1,
                        rateText = "3.49", effectiveFromMillis = 0L, currentRateBps = 349
                    )
                )
            )
        )

        tab("Crédito")
        composeRule.onNodeWithText("Parcelas: 1x").assertExists()
        composeRule.onNodeWithText("Tarifa do crédito em 1x (Visa)").assertExists()

        composeRule.onNodeWithText("Adicionar parcela").performClick()
        composeRule.onNodeWithText("Selecionar parcela").performClick()
        composeRule.onNodeWithText("2x").performClick()

        assertTrue(calls.contains("installment:Visa:2"))
    }

    @Test
    fun rateInputIsForwardedWithEntryId() {
        render(
            MachineSaleUiState(
                selectedMethods = setOf(TerminalPaymentType.PIX),
                tariffEntries = listOf(TariffEntryUi(TerminalPaymentType.PIX, effectiveFromMillis = 0L))
            )
        )

        tab("Pix")
        composeRule.onAllNodes(hasSetTextAction())[1].performTextInput("2")

        assertTrue(calls.any { it.startsWith("rate:PIX||:") })
    }
}
