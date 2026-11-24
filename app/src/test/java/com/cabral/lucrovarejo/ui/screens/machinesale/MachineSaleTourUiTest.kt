package com.cabral.lucrovarejo.ui.screens.machinesale

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.cabral.lucrovarejo.domain.payment.TerminalPaymentType
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MachineSaleTourUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun tourWalksThroughCreditWithNextButtonOnEveryStep() {
        var finished = 0
        composeRule.setContent {
            var state by remember { mutableStateOf(MachineSaleUiState()) }
            LucroVarejoTheme {
                MachineSaleContent(
                    state = state,
                    onNameChange = { state = state.copy(name = it) },
                    onToggleMethod = {
                        state = state.copy(selectedMethods = state.selectedMethods + it)
                    },
                    onCashChargesTariffToggle = {},
                    onAddBrand = { state = state.copy(availableBrands = state.availableBrands + it) },
                    onToggleDebitBrand = {},
                    onToggleCreditBrand = {
                        state = state.copy(selectedCreditBrandIds = state.selectedCreditBrandIds + it)
                    },
                    onAddCreditInstallment = { brand, n ->
                        state = state.copy(
                            creditInstallmentsByBrand = mapOf(brand to listOf(n)),
                            tariffEntries = state.tariffEntries + TariffEntryUi(
                                method = TerminalPaymentType.CREDIT,
                                brandId = brand,
                                installments = n
                            )
                        )
                    },
                    onTariffRateChange = { _, _ -> },
                    onTariffDateChange = { _, _ -> },
                    onSave = {},
                    tourActive = true,
                    onTourFinish = { finished++ }
                )
            }
        }

        composeRule.onNodeWithText("Dê um nome à maquininha").assertExists()
        composeRule.onNodeWithText("Próximo").performClick()

        composeRule.onNodeWithText("Abra a aba Crédito").assertExists()
        composeRule.onNodeWithText("Próximo").performClick()

        composeRule.onNodeWithText("Ative o crédito").assertExists()
        composeRule.onNodeWithText("Próximo").performClick()

        composeRule.onNodeWithText("Cadastre uma bandeira").assertExists()
        composeRule.onNodeWithText("Próximo").assertIsNotEnabled()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput("Visa")
        composeRule.onNodeWithText("Próximo").assertIsEnabled().performClick()

        composeRule.onNodeWithText("Marque a bandeira").assertExists()
        composeRule.onNodeWithText("Próximo").performClick()

        composeRule.onNodeWithText("Adicione as parcelas").assertExists()
        composeRule.onNodeWithText("Próximo").performClick()

        composeRule.onNodeWithText("Preencha a tarifa").assertExists()
        composeRule.onNodeWithText("Próximo").performClick()

        composeRule.onNodeWithText("Salve a maquininha").assertExists()
        composeRule.onNodeWithText("Entendi").performClick()
        composeRule.runOnIdle { assertEquals(1, finished) }
    }
}
