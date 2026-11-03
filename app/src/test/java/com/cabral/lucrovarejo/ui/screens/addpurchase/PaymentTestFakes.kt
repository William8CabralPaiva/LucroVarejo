package com.cabral.lucrovarejo.ui.screens.addpurchase

import com.cabral.lucrovarejo.domain.payment.PaymentMachineModel
import com.cabral.lucrovarejo.domain.payment.PaymentMachineRepository
import com.cabral.lucrovarejo.domain.payment.PaymentTerminalModel
import com.cabral.lucrovarejo.domain.payment.PaymentTerminalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class FakeTerminalRepository(
    private val terminals: List<PaymentTerminalModel> = emptyList()
) : PaymentTerminalRepository {
    override suspend fun createTerminal(terminal: PaymentTerminalModel) = terminal
    override suspend fun updateTerminal(terminal: PaymentTerminalModel) = terminal
    override suspend fun archiveTerminal(key: String) = Unit
    override fun observeTerminals(): Flow<List<PaymentTerminalModel>> = flowOf(terminals)
    override fun observeTerminal(key: String): Flow<PaymentTerminalModel?> =
        flowOf(terminals.firstOrNull { it.key == key })
}

internal class FakeTariffRepository(
    private val tariffsByTerminal: Map<String, List<PaymentMachineModel>> = emptyMap()
) : PaymentMachineRepository {
    override suspend fun addTariff(tariff: PaymentMachineModel) = tariff
    override fun observeTariffs(terminalId: String): Flow<List<PaymentMachineModel>> =
        flowOf(tariffsByTerminal[terminalId].orEmpty())

    override suspend fun getTariffsSnapshot(terminalId: String): List<PaymentMachineModel> =
        tariffsByTerminal[terminalId].orEmpty()
}

internal val NoTerminalsRepository = FakeTerminalRepository()
internal val NoTariffsRepository = FakeTariffRepository()
