package com.cabral.lucrovarejo.data.payment

import com.cabral.lucrovarejo.data.payment.datasource.CardBrandRemoteDataSource
import com.cabral.lucrovarejo.data.payment.model.CardBrandRecord
import com.cabral.lucrovarejo.data.payment.model.toModel
import com.cabral.lucrovarejo.data.payment.model.toRequest
import com.cabral.lucrovarejo.domain.payment.CardBrandModel
import com.cabral.lucrovarejo.domain.payment.CardBrandRepository
import com.cabral.lucrovarejo.domain.payment.PaymentFailure
import com.google.firebase.firestore.FirebaseFirestoreException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

@Singleton
class CardBrandRepositoryImpl @Inject constructor(
    private val remoteDataSource: CardBrandRemoteDataSource
) : CardBrandRepository {
    override suspend fun createCardBrand(brand: CardBrandModel): CardBrandModel {
        val request = brand.toRequest()
        val documentId = withPaymentFailure { remoteDataSource.createCardBrand(request) }
        return CardBrandRecord(documentId, request).toModel()
    }

    override suspend fun renameCardBrand(key: String, name: String): CardBrandModel {
        withPaymentFailure { remoteDataSource.renameCardBrand(key, name) }
        return CardBrandModel(key = key, name = name, active = true)
    }

    override suspend fun archiveCardBrand(key: String) {
        withPaymentFailure { remoteDataSource.setCardBrandActive(key, false) }
    }

    override fun observeCardBrands(): Flow<List<CardBrandModel>> =
        remoteDataSource.observeCardBrands()
            .map { records -> records.map { it.toModel() } }
            .catch { failure -> throw failure.toPaymentFailure() }

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

internal fun Throwable.toPaymentFailure(): PaymentFailure = when (this) {
    is PaymentFailure -> this
    is FirebaseFirestoreException -> {
        val reason = when (code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                PaymentFailure.Reason.PERMISSION_DENIED
            FirebaseFirestoreException.Code.UNAVAILABLE -> PaymentFailure.Reason.NETWORK
            else -> PaymentFailure.Reason.UNKNOWN
        }
        PaymentFailure(reason, this)
    }
    else -> PaymentFailure(PaymentFailure.Reason.UNKNOWN, this)
}
