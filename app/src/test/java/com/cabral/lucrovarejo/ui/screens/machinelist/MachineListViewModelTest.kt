package com.cabral.lucrovarejo.ui.screens.machinelist

import com.cabral.lucrovarejo.domain.payment.PaymentTerminalModel
import com.cabral.lucrovarejo.domain.payment.PaymentTerminalRepository
import com.cabral.lucrovarejo.domain.payment.usecase.ArchiveTerminalUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.GetTerminalsUseCase
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MachineListViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeRepository()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = MachineListViewModel(
        GetTerminalsUseCase(repository),
        ArchiveTerminalUseCase(repository)
    )

    @Test
    fun startsLoadingThenReflectsEmissions() = runTest(dispatcher) {
        val vm = viewModel()
        assertTrue(vm.uiState.value.isLoading)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isLoading)

        repository.terminals.value = listOf(PaymentTerminalModel("Stone", key = "k"))
        advanceUntilIdle()

        assertEquals("Stone", vm.uiState.value.items.single().name)
    }

    @Test
    fun flowErrorShowsError() = runTest(dispatcher) {
        repository.failing = true
        val vm = viewModel()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.hasError)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun archiveDelegatesAndSwallowsFailure() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onArchive("k")
        advanceUntilIdle()
        assertEquals(listOf("k"), repository.archived)

        repository.archiveFailure = true
        vm.onArchive("z")
        advanceUntilIdle()
        assertFalse(vm.uiState.value.hasError)
    }

    private class FakeRepository : PaymentTerminalRepository {
        val terminals = MutableStateFlow<List<PaymentTerminalModel>>(emptyList())
        val archived = mutableListOf<String>()
        var failing = false
        var archiveFailure = false

        override suspend fun createTerminal(terminal: PaymentTerminalModel) = terminal
        override suspend fun updateTerminal(terminal: PaymentTerminalModel) = terminal
        override suspend fun archiveTerminal(key: String) {
            if (archiveFailure) error("boom")
            archived += key
        }

        override fun observeTerminals(): Flow<List<PaymentTerminalModel>> =
            if (failing) flow { error("boom") } else terminals

        override fun observeTerminal(key: String): Flow<PaymentTerminalModel?> =
            MutableStateFlow(null)
    }
}
