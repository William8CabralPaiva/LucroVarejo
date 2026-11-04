package com.cabral.lucrovarejo.ui.screens.machinehistory

import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.payment.PaymentFailure
import com.cabral.lucrovarejo.domain.payment.PaymentMachineModel
import com.cabral.lucrovarejo.domain.payment.PaymentMachineRepository
import com.cabral.lucrovarejo.domain.payment.TerminalPaymentType
import com.cabral.lucrovarejo.domain.payment.usecase.AddPaymentMachineUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.GetFilteredCurrentTariffUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.GetPaymentMachinesUseCase
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MachineHistoryViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeRepository()
    private val slot = PaymentMachineHistoryArgs("t1", TerminalPaymentType.CREDIT, "Visa", 3)

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = MachineHistoryViewModel(
        GetPaymentMachinesUseCase(repository),
        AddPaymentMachineUseCase(repository),
        GetFilteredCurrentTariffUseCase()
    )

    private fun tariff(
        rate: Int,
        from: Long,
        method: TerminalPaymentType = TerminalPaymentType.CREDIT,
        installments: Int? = 3
    ) = PaymentMachineModel("t1", method, "Visa", installments, rate, Date(from))

    @Test
    fun loadSlotFiltersTariffsAndMarksCurrent() = runTest(dispatcher) {
        val past = tariff(200, 1_000)
        val older = tariff(100, 500)
        val future = tariff(300, System.currentTimeMillis() + 10_000_000)
        repository.tariffs.value = listOf(older, future, past, tariff(999, 1, installments = 6))
        val vm = viewModel()
        vm.loadSlot(slot)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(listOf(300, 200, 100), state.tariffs.map { it.rateBps })
        assertEquals(past.historyKey(), state.currentTariffKey)
        assertEquals("Visa", state.brandName)
        assertFalse(state.isLoading)
    }

    @Test
    fun loadSlotIgnoresSameSlotAndReportsFlowError() = runTest(dispatcher) {
        repository.failing = true
        val vm = viewModel()
        vm.loadSlot(slot)
        advanceUntilIdle()

        assertEquals(R.string.machine_history_load_error, vm.uiState.value.loadError)
        assertFalse(vm.uiState.value.isLoading)
        assertEquals(1, repository.observeCalls)

        vm.loadSlot(slot)
        assertEquals(1, repository.observeCalls)
    }

    @Test
    fun dialogLifecycleAndFieldChanges() {
        val vm = viewModel()
        vm.onShowAddDialog()
        assertTrue(vm.uiState.value.showAddDialog)
        assertNotNull(vm.uiState.value.effectiveFromMillis)

        vm.onRateChange("2,49")
        vm.onDateChange(42L)
        assertEquals("2,49", vm.uiState.value.rateText)
        assertEquals(42L, vm.uiState.value.effectiveFromMillis)

        vm.onDismissAddDialog()
        assertFalse(vm.uiState.value.showAddDialog)
    }

    @Test
    fun confirmWithoutSlotDoesNothing() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onConfirmAdd()
        advanceUntilIdle()
        assertTrue(repository.added.isEmpty())
    }

    @Test
    fun confirmValidatesRateAndDate() = runTest(dispatcher) {
        val vm = viewModel()
        vm.loadSlot(slot)
        vm.onShowAddDialog()
        vm.onRateChange("abc")
        vm.onConfirmAdd()
        assertEquals(R.string.terminal_form_tariff_rate_invalid, vm.uiState.value.rateError)

        vm.onRateChange("1.5")
        vm.onConfirmAdd()
        advanceUntilIdle()
        // Data preenchida ao abrir o dialogo; salva normalmente.
        assertEquals(150, repository.added.single().rateBps)
    }

    @Test
    fun confirmSavesAndClosesDialog() = runTest(dispatcher) {
        val vm = viewModel()
        vm.loadSlot(slot)
        vm.onShowAddDialog()
        vm.onRateChange("2,49")
        vm.onDateChange(5_000)
        vm.onConfirmAdd()
        assertTrue(vm.uiState.value.isSaving)
        advanceUntilIdle()

        val saved = repository.added.single()
        assertEquals(249, saved.rateBps)
        assertEquals("Visa", saved.brandId)
        assertEquals(3, saved.installments)
        assertEquals(Date(5_000), saved.effectiveFrom)
        assertFalse(vm.uiState.value.isSaving)
        assertFalse(vm.uiState.value.showAddDialog)
    }

    @Test
    fun duplicateDateAndGenericFailureSetSaveError() = runTest(dispatcher) {
        repository.tariffs.value = listOf(tariff(100, 5_000))
        val vm = viewModel()
        vm.loadSlot(slot)
        vm.onShowAddDialog()
        vm.onRateChange("1")
        vm.onDateChange(5_000)
        vm.onConfirmAdd()
        advanceUntilIdle()
        assertEquals(R.string.terminal_form_tariff_duplicate_date, vm.uiState.value.saveError)
        assertTrue(vm.uiState.value.showAddDialog)

        repository.addFailure = PaymentFailure(PaymentFailure.Reason.NETWORK)
        vm.onDateChange(6_000)
        vm.onConfirmAdd()
        advanceUntilIdle()
        assertEquals(R.string.terminal_form_tariff_save_error, vm.uiState.value.saveError)
        assertNull(vm.uiState.value.rateError)
    }

    private class FakeRepository : PaymentMachineRepository {
        val tariffs = MutableStateFlow<List<PaymentMachineModel>>(emptyList())
        val added = mutableListOf<PaymentMachineModel>()
        var failing = false
        var observeCalls = 0
        var addFailure: Exception? = null

        override suspend fun addTariff(tariff: PaymentMachineModel): PaymentMachineModel {
            addFailure?.let { throw it }
            added += tariff
            return tariff
        }

        override fun observeTariffs(terminalId: String): Flow<List<PaymentMachineModel>> {
            observeCalls++
            return if (failing) flow { error("boom") } else tariffs
        }

        override suspend fun getTariffsSnapshot(terminalId: String) = tariffs.value
    }
}
