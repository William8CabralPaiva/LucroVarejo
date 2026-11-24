package com.cabral.lucrovarejo.data.payment

import com.cabral.lucrovarejo.data.common.withDataFailure
import com.cabral.lucrovarejo.data.payment.datasource.PaymentTerminalRemoteDataSource
import com.cabral.lucrovarejo.data.payment.model.PaymentTerminalRecord
import com.cabral.lucrovarejo.data.payment.model.toModel
import com.cabral.lucrovarejo.data.payment.model.toRequest
import com.cabral.lucrovarejo.domain.payment.PaymentTerminalModel
import com.cabral.lucrovarejo.domain.payment.PaymentTerminalRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class PaymentTerminalRepositoryImpl @Inject constructor(
    private val remoteDataSource: PaymentTerminalRemoteDataSource
) : PaymentTerminalRepository {
    override suspend fun createTerminal(terminal: PaymentTerminalModel): PaymentTerminalModel {
        val request = terminal.toRequest()
        val documentId = withDataFailure { remoteDataSource.createTerminal(request) }
        return request.let { PaymentTerminalRecord(documentId, it).toModel() }
    }

    override suspend fun updateTerminal(terminal: PaymentTerminalModel): PaymentTerminalModel {
        val key = requireNotNull(terminal.key) { "Terminal key is required to update" }
        val request = terminal.toRequest()
        withDataFailure { remoteDataSource.updateTerminal(key, request) }
        return PaymentTerminalRecord(key, request).toModel()
    }

    override suspend fun archiveTerminal(key: String) {
        withDataFailure { remoteDataSource.setTerminalActive(key, false) }
    }

    override fun observeTerminals(): Flow<List<PaymentTerminalModel>> =
        remoteDataSource.observeTerminals()
            .map { records -> records.map { it.toModel() } }
            .withDataFailure()

    override fun observeTerminal(key: String): Flow<PaymentTerminalModel?> =
        remoteDataSource.observeTerminal(key)
            .map { record -> record?.toModel() }
            .withDataFailure()
}
