package com.cabral.lucrovarejo.data.purchase

import com.cabral.lucrovarejo.data.purchase.datasource.PurchaseRemoteDataSource
import com.cabral.lucrovarejo.data.purchase.model.PurchaseRequest
import com.cabral.lucrovarejo.domain.purchase.PurchaseFailure
import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.domain.purchase.PurchaseRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException

@Singleton
class PurchaseRepositoryImpl @Inject constructor(
    private val remoteDataSource: PurchaseRemoteDataSource
) : PurchaseRepository {
    override suspend fun createPurchase(purchase: PurchaseModel): PurchaseModel {
        val request = PurchaseRequest.from(purchase)
        val documentId = withPurchaseFailure {
            remoteDataSource.createPurchase(request)
        }
        return purchase.copy(id = documentId)
    }

    private suspend fun <T> withPurchaseFailure(
        operation: suspend () -> T
    ): T = try {
        operation()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (failure: PurchaseFailure) {
        throw failure
    } catch (failure: FirebaseFirestoreException) {
        val reason = when (failure.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                PurchaseFailure.Reason.PERMISSION_DENIED
            FirebaseFirestoreException.Code.UNAVAILABLE ->
                PurchaseFailure.Reason.NETWORK
            else -> PurchaseFailure.Reason.UNKNOWN
        }
        throw PurchaseFailure(reason, failure)
    } catch (failure: Exception) {
        throw PurchaseFailure(PurchaseFailure.Reason.UNKNOWN, failure)
    }
}
