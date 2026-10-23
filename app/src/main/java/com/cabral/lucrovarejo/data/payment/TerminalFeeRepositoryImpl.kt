package com.cabral.lucrovarejo.data.payment

import com.cabral.lucrovarejo.data.payment.datasource.TerminalFeeRemoteDataSource
import com.cabral.lucrovarejo.data.payment.model.TerminalFeeRecord
import com.cabral.lucrovarejo.data.payment.model.toModel
import com.cabral.lucrovarejo.data.payment.model.toRequest
import com.cabral.lucrovarejo.domain.payment.PaymentFailure
import com.cabral.lucrovarejo.domain.payment.TerminalFeeModel
import com.cabral.lucrovarejo.domain.payment.TerminalFeeRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

@Singleton
class TerminalFeeRepositoryImpl @Inject constructor(
    private val remoteDataSource: TerminalFeeRemoteDataSource
) : TerminalFeeRepository {
    override suspend fun addFee(fee: TerminalFeeModel): TerminalFeeModel {
        val request = fee.toRequest()
        val documentId = withPaymentFailure {
            remoteDataSource.addFee(fee.terminalId, request)
        }
        return TerminalFeeRecord(documentId, fee.terminalId, request).toModel()
    }

    override fun observeFees(terminalId: String): Flow<List<TerminalFeeModel>> =
        remoteDataSource.observeFees(terminalId)
            .map { records -> records.map { it.toModel() } }
            .catch { failure -> throw failure.toPaymentFailure() }

    override suspend fun getFeesSnapshot(terminalId: String): List<TerminalFeeModel> =
        withPaymentFailure {
            remoteDataSource.getFeesSnapshot(terminalId).map { it.toModel() }
        }

    private suspend fun <T> withPaymentFailure(
        operation: suspend () -> T
    ): T = try {
        operation()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (failure: PaymentFailure) {
        throw failure
    } catch (failure: Exception) {
        throw failure.toPaymentFailure()
    }
}
