package com.cabral.lucrovarejo.data.payment

import com.cabral.lucrovarejo.data.payment.datasource.PaymentMachineRemoteDataSource
import com.cabral.lucrovarejo.data.payment.model.PaymentMachineRecord
import com.cabral.lucrovarejo.data.payment.model.toModel
import com.cabral.lucrovarejo.data.payment.model.toRequest
import com.cabral.lucrovarejo.domain.payment.PaymentFailure
import com.cabral.lucrovarejo.domain.payment.PaymentMachineModel
import com.cabral.lucrovarejo.domain.payment.PaymentMachineRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

@Singleton
class PaymentMachineRepositoryImpl @Inject constructor(
    private val remoteDataSource: PaymentMachineRemoteDataSource
) : PaymentMachineRepository {
    override suspend fun addTariff(tariff: PaymentMachineModel): PaymentMachineModel {
        val request = tariff.toRequest()
        val documentId = withPaymentFailure {
            remoteDataSource.addTariff(tariff.terminalId, request)
        }
        return PaymentMachineRecord(documentId, tariff.terminalId, request).toModel()
    }

    override fun observeTariffs(terminalId: String): Flow<List<PaymentMachineModel>> =
        remoteDataSource.observeTariffs(terminalId)
            .map { records -> records.map { it.toModel() } }
            .catch { failure -> throw failure.toPaymentFailure() }

    override suspend fun getTariffsSnapshot(terminalId: String): List<PaymentMachineModel> =
        withPaymentFailure {
            remoteDataSource.getTariffsSnapshot(terminalId).map { it.toModel() }
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
