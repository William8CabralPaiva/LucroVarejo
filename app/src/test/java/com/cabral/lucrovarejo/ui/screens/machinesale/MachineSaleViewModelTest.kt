package com.cabral.lucrovarejo.ui.screens.machinesale

import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.payment.PaymentFailure
import com.cabral.lucrovarejo.domain.payment.PaymentMachineModel
import com.cabral.lucrovarejo.domain.payment.PaymentMachineRepository
import com.cabral.lucrovarejo.domain.payment.PaymentTerminalModel
import com.cabral.lucrovarejo.domain.payment.PaymentTerminalRepository
import com.cabral.lucrovarejo.domain.payment.TerminalPaymentType
import com.cabral.lucrovarejo.domain.payment.usecase.AddPaymentMachineUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.CreateTerminalUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.GetFilteredCurrentTariffUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.GetPaymentMachinesUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.UpdateTerminalUseCase
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MachineSaleViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val terminals = FakeTerminals()
    private val tariffs = FakeTariffs()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = MachineSaleViewModel(
        GetPaymentMachinesUseCase(tariffs),
        CreateTerminalUseCase(terminals),
        UpdateTerminalUseCase(terminals),
        AddPaymentMachineUseCase(tariffs),
        GetFilteredCurrentTariffUseCase()
    )

    private fun MachineSaleViewModel.entry(id: String) =
        uiState.value.tariffEntries.first { it.id == id }

    @Test
    fun loadTerminalPrefillsFormFromExistingTariffs() = runTest(dispatcher) {
        tariffs.stored += PaymentMachineModel("t1", TerminalPaymentType.CASH, null, null, 100, Date(1_000))
        tariffs.stored += PaymentMachineModel("t1", TerminalPaymentType.DEBIT, "Visa", null, 199, Date(1_000))
        tariffs.stored += PaymentMachineModel("t1", TerminalPaymentType.CREDIT, "Visa", 2, 349, Date(1_000))
        tariffs.stored += PaymentMachineModel("t1", TerminalPaymentType.CREDIT, "Visa", 1, 299, Date(1_000))
        val vm = viewModel()

        vm.loadTerminal(null)
        assertNull(vm.uiState.value.terminalId)

        vm.loadTerminal(
            PaymentTerminalModel(
                "Stone",
                enabledMethods = TerminalPaymentType.entries.toSet(),
                brands = listOf("Visa"),
                key = "t1"
            )
        )
        vm.loadTerminal(PaymentTerminalModel("Stone", key = "t1"))
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("Stone", state.name)
        assertTrue(state.cashChargesTariff)
        assertEquals(setOf("Visa"), state.selectedDebitBrandIds)
        assertEquals(listOf(1, 2), state.creditInstallmentsByBrand["Visa"])
        assertEquals("1.00", vm.entry("CASH||").rateText)
        assertEquals("1.99", vm.entry("DEBIT|Visa|").rateText)
        assertEquals("3.49", vm.entry("CREDIT|Visa|2").rateText)
        assertEquals(349, vm.entry("CREDIT|Visa|2").currentRateBps)
        assertFalse(state.isLoading)
    }

    @Test
    fun togglesBrandsAndInstallmentsBuildEntries() {
        val vm = viewModel()
        vm.onToggleMethod(TerminalPaymentType.PIX)
        vm.onToggleMethod(TerminalPaymentType.DEBIT)
        vm.onToggleMethod(TerminalPaymentType.CREDIT)
        vm.onToggleMethod(TerminalPaymentType.CASH)
        vm.onCashChargesTariffToggle(true)
        vm.onAddBrand("  ")
        vm.onAddBrand("Visa")
        vm.onAddBrand("visa")
        vm.onToggleDebitBrand("Visa")
        vm.onToggleCreditBrand("Visa")
        vm.onAddCreditInstallment("Visa", 3)
        vm.onAddCreditInstallment("Visa", 1)
        vm.onAddCreditInstallment("Visa", 3)

        assertEquals(listOf("Visa"), vm.uiState.value.availableBrands)
        assertEquals(
            setOf("CASH||", "PIX||", "DEBIT|Visa|", "CREDIT|Visa|1", "CREDIT|Visa|3"),
            vm.uiState.value.tariffEntries.map { it.id }.toSet()
        )
        assertEquals(5, vm.uiState.value.tariffEntries.size)

        vm.onToggleMethod(TerminalPaymentType.PIX)
        vm.onToggleDebitBrand("Visa")
        vm.onToggleCreditBrand("Visa")
        vm.onCashChargesTariffToggle(false)
        assertTrue(vm.uiState.value.tariffEntries.isEmpty())
    }

    @Test
    fun rateAndDateEditsAreKeptAcrossRecompute() {
        val vm = viewModel()
        vm.onToggleMethod(TerminalPaymentType.PIX)
        vm.onTariffRateChange("PIX||", "1,5")
        vm.onTariffDateChange("PIX||", 123L)
        vm.onToggleMethod(TerminalPaymentType.DEBIT)

        assertEquals("1,5", vm.entry("PIX||").rateText)
        assertEquals(123L, vm.entry("PIX||").effectiveFromMillis)
    }

    @Test
    fun saveValidatesNameMethodsAndRates() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onSave(false)
        assertEquals(R.string.terminal_form_name_required, vm.uiState.value.nameError)

        vm.onNameChange("Stone")
        vm.onSave(false)
        assertEquals(R.string.terminal_form_methods_required, vm.uiState.value.methodsError)

        vm.onToggleMethod(TerminalPaymentType.PIX)
        vm.onTariffRateChange("PIX||", "abc")
        vm.onSave(false)
        advanceUntilIdle()

        assertEquals(R.string.terminal_form_tariff_rate_invalid, vm.entry("PIX||").error)
        assertTrue(terminals.created.isEmpty())
    }

    @Test
    fun saveCreatesTerminalAndOnlyChangedTariffsThenReloads() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onNameChange("  Stone ")
        vm.onToggleMethod(TerminalPaymentType.PIX)
        vm.onToggleMethod(TerminalPaymentType.DEBIT)
        vm.onAddBrand("Visa")
        vm.onToggleDebitBrand("Visa")
        vm.onTariffRateChange("PIX||", "0,99")
        vm.onSave(false)
        assertTrue(vm.uiState.value.isSaving)
        advanceUntilIdle()

        assertEquals("Stone", terminals.created.single().name)
        assertEquals("new-key", vm.uiState.value.terminalId)
        assertEquals(listOf(99), tariffs.added.map { it.rateBps })
        assertEquals("new-key", tariffs.added.single().terminalId)
        assertFalse(vm.uiState.value.isSaving)
        assertEquals(Unit, vm.saved.first())

        vm.onSave(false)
        advanceUntilIdle()
        assertEquals(1, terminals.created.size)
        assertEquals("Stone", terminals.updated.single().name)
    }

    @Test
    fun saveAndCloseEmitsCompletion() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onNameChange("Stone")
        vm.onToggleMethod(TerminalPaymentType.PIX)
        vm.onSave(true)
        advanceUntilIdle()

        assertEquals(Unit, vm.completions.first())
        assertFalse(vm.uiState.value.isSaving)
    }

    @Test
    fun saveFailuresSetErrors() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onNameChange("Stone")
        vm.onToggleMethod(TerminalPaymentType.PIX)
        vm.onTariffRateChange("PIX||", "1")
        vm.onTariffDateChange("PIX||", 1_000)
        tariffs.stored += PaymentMachineModel("new-key", TerminalPaymentType.PIX, null, null, 5, Date(1_000))
        vm.onSave(false)
        advanceUntilIdle()
        assertEquals(R.string.terminal_form_tariff_duplicate_date, vm.uiState.value.saveError)

        terminals.failure = PaymentFailure(PaymentFailure.Reason.NETWORK)
        vm.onSave(false)
        advanceUntilIdle()
        assertEquals(R.string.terminal_form_save_error, vm.uiState.value.saveError)
        assertFalse(vm.uiState.value.isSaving)
    }

    private class FakeTerminals : PaymentTerminalRepository {
        val created = mutableListOf<PaymentTerminalModel>()
        val updated = mutableListOf<PaymentTerminalModel>()
        var failure: Exception? = null

        override suspend fun createTerminal(terminal: PaymentTerminalModel): PaymentTerminalModel {
            failure?.let { throw it }
            created += terminal
            return terminal.copy(key = "new-key")
        }

        override suspend fun updateTerminal(terminal: PaymentTerminalModel): PaymentTerminalModel {
            failure?.let { throw it }
            updated += terminal
            return terminal
        }

        override suspend fun archiveTerminal(key: String) = Unit
        override fun observeTerminals(): Flow<List<PaymentTerminalModel>> = MutableStateFlow(emptyList())
        override fun observeTerminal(key: String): Flow<PaymentTerminalModel?> = MutableStateFlow(null)
    }

    private class FakeTariffs : PaymentMachineRepository {
        val stored = mutableListOf<PaymentMachineModel>()
        val added = mutableListOf<PaymentMachineModel>()

        override suspend fun addTariff(tariff: PaymentMachineModel): PaymentMachineModel {
            added += tariff
            stored += tariff
            return tariff
        }

        override fun observeTariffs(terminalId: String): Flow<List<PaymentMachineModel>> =
            MutableStateFlow(stored.filter { it.terminalId == terminalId })

        override suspend fun getTariffsSnapshot(terminalId: String) =
            stored.filter { it.terminalId == terminalId }
    }
}
