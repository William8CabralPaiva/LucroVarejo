package com.cabral.lucrovarejo.data.purchase.datasource

import com.cabral.lucrovarejo.data.purchase.model.PurchaseRequest
import com.cabral.lucrovarejo.data.purchase.model.PurchaseRecord
import com.cabral.lucrovarejo.data.purchase.model.PurchaseSummaryRecord
import com.cabral.lucrovarejo.domain.auth.AuthRepository
import com.cabral.lucrovarejo.domain.purchase.PurchaseFailure
import com.cabral.lucrovarejo.domain.purchase.PurchasePagination
import androidx.paging.PagingSource
import androidx.paging.PagingState
import java.util.Date
import javax.inject.Inject

class FirebasePurchaseRemoteDataSource @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestoreClient: PurchaseFirestoreClient
) : PurchaseRemoteDataSource {
    override suspend fun createPurchase(request: PurchaseRequest): String {
        val storeKey = authRepository.storeKey
            ?: throw PurchaseFailure(PurchaseFailure.Reason.UNAUTHENTICATED)
        return firestoreClient.savePurchase(storeKey, request)
    }

    override suspend fun getPurchasesBetween(
        start: Date,
        end: Date
    ): List<PurchaseSummaryRecord> {
        val storeKey = authRepository.storeKey
            ?: throw PurchaseFailure(PurchaseFailure.Reason.UNAUTHENTICATED)
        return firestoreClient.getPurchasesBetween(storeKey, start, end)
    }

    override fun getLatestPurchasesPagingSource(): PagingSource<PurchaseCursor, PurchaseRecord> =
        FirebasePurchasePagingSource(authRepository, firestoreClient)

    private class FirebasePurchasePagingSource(
        private val authRepository: AuthRepository,
        private val firestoreClient: PurchaseFirestoreClient
    ) : PagingSource<PurchaseCursor, PurchaseRecord>() {
        override suspend fun load(
            params: LoadParams<PurchaseCursor>
        ): LoadResult<PurchaseCursor, PurchaseRecord> {
            return try {
                val storeKey = authRepository.storeKey
                    ?: throw PurchaseFailure(PurchaseFailure.Reason.UNAUTHENTICATED)
                val pageSize = minOf(
                    params.loadSize,
                    PurchasePagination.PAGE_SIZE
                )
                val page = firestoreClient.getLatestPurchasesPage(
                    storeKey = storeKey,
                    startAfter = params.key?.document,
                    limit = pageSize.toLong()
                )
                LoadResult.Page(
                    data = page.purchases,
                    prevKey = null,
                    nextKey = if (page.purchases.size == pageSize) {
                        page.lastDocument?.let(::PurchaseCursor)
                    } else {
                        null
                    }
                )
            } catch (cancellation: kotlinx.coroutines.CancellationException) {
                throw cancellation
            } catch (failure: Exception) {
                LoadResult.Error(failure)
            }
        }

        override fun getRefreshKey(
            state: PagingState<PurchaseCursor, PurchaseRecord>
        ): PurchaseCursor? = null
    }
}
